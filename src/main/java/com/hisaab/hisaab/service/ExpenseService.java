package com.hisaab.hisaab.service;

import com.hisaab.hisaab.dto.AddExpenseRequest;
import com.hisaab.hisaab.dto.ExpenseResponse;
import com.hisaab.hisaab.entity.*;
import com.hisaab.hisaab.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private GroupSummaryService groupSummaryService;

    @Transactional
    public ExpenseResponse addExpense(AddExpenseRequest request) {

        // 1. Fetch related entities
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        // 2. Create and save the Expense
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setCategory(request.getCategory());
        expense = expenseRepository.save(expense);
        auditService.log(paidBy, "EXPENSE_CREATED", "Expense", expense.getId(), null, expense);

        // 3. Calculate equal split
        int participantCount = request.getParticipantIds().size();
        BigDecimal shareAmount = request.getAmount()
                .divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

        // 4. Create ExpenseShare for each participant
        for (Long participantId : request.getParticipantIds()) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found: " + participantId));

            ExpenseShare share = new ExpenseShare();
            share.setExpense(expense);
            share.setUser(participant);
            share.setShareAmount(shareAmount);
            // if this participant IS the payer, their share is "settled" already
            share.setSettled(participant.getId().equals(paidBy.getId()));
            expenseShareRepository.save(share);

            // 5. Update balances (skip if participant paid for themselves)
            if (!participant.getId().equals(paidBy.getId())) {
                updateBalance(group, participant, paidBy, shareAmount);
            }
        }
        groupSummaryService.evictSummaryCache(group.getId());

        // 6. Return response
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                paidBy.getName(),
                expense.getCategory(),
                expense.getCreatedAt()
        );
    }

    /**
     * Updates the running balance: owedByUser owes owedToUser `amount` more.
     * If a reverse balance exists (owedToUser already owed owedByUser),
     * we net it out instead of having both directions simultaneously.
     */
    private void updateBalance(Group group, User owedByUser, User owedToUser, BigDecimal amount) {

        // Check if owedByUser already owes owedToUser something
        Optional<Balance> existing = balanceRepository
                .findByGroupIdAndOwedByIdAndOwedToId(group.getId(), owedByUser.getId(), owedToUser.getId());

        // Check the reverse direction too
        Optional<Balance> reverse = balanceRepository
                .findByGroupIdAndOwedByIdAndOwedToId(group.getId(), owedToUser.getId(), owedByUser.getId());

        if (reverse.isPresent()) {
            // Net it out against the reverse balance
            Balance reverseBalance = reverse.get();
            BigDecimal newReverseAmount = reverseBalance.getAmount().subtract(amount);

            if (newReverseAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Reverse balance still positive, just reduce it
                reverseBalance.setAmount(newReverseAmount);
                balanceRepository.save(reverseBalance);
            } else if (newReverseAmount.compareTo(BigDecimal.ZERO) < 0) {
                // Flips direction — delete reverse, create/update forward with absolute value
                balanceRepository.delete(reverseBalance);
                upsertBalance(group, owedByUser, owedToUser, newReverseAmount.abs(), existing);
            } else {
                // Exactly cancels out
                balanceRepository.delete(reverseBalance);
            }
        } else {
            // No reverse balance — just add to (or create) the forward balance
            upsertBalance(group, owedByUser, owedToUser, amount, existing);
        }
    }

    private void upsertBalance(Group group, User owedByUser, User owedToUser,
                               BigDecimal amountToAdd, Optional<Balance> existing) {
        if (existing.isPresent()) {
            Balance balance = existing.get();
            balance.setAmount(balance.getAmount().add(amountToAdd));
            balanceRepository.save(balance); // @Version checked here
        } else {
            Balance balance = new Balance();
            balance.setGroup(group);
            balance.setOwedBy(owedByUser);
            balance.setOwedTo(owedToUser);
            balance.setAmount(amountToAdd);
            balanceRepository.save(balance);
        }
    }
}