package com.hisaab.hisaab.service;

import com.hisaab.hisaab.dto.SettlementResponse;
import com.hisaab.hisaab.entity.Balance;
import com.hisaab.hisaab.entity.User;
import com.hisaab.hisaab.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SettlementService {

    @Autowired
    private BalanceRepository balanceRepository;

    public List<SettlementResponse> getSettlements(Long groupId) {

        List<Balance> balances = balanceRepository.findByGroupId(groupId);

        // Step 1: Compute net balance per user
        // net[user] = total owed TO them - total they owe
        Map<Long, BigDecimal> net = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>(); // to retrieve User objects for names

        for (Balance b : balances) {
            Long owedByUserId = b.getOwedBy().getId();
            Long owedToUserId = b.getOwedTo().getId();
            BigDecimal amount = b.getAmount();

            net.merge(owedByUserId, amount.negate(), BigDecimal::add); // debtor: -amount
            net.merge(owedToUserId, amount, BigDecimal::add);          // creditor: +amount

            userMap.putIfAbsent(owedByUserId, b.getOwedBy());
            userMap.putIfAbsent(owedToUserId, b.getOwedTo());
        }

        // Step 2: Separate into creditors (net > 0) and debtors (net < 0)
        // Use max-heaps (PriorityQueue) for greedy matching
        PriorityQueue<Map.Entry<Long, BigDecimal>> creditors =
                new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue())); // max-heap

        PriorityQueue<Map.Entry<Long, BigDecimal>> debtors =
                new PriorityQueue<>((a, b) -> a.getValue().compareTo(b.getValue())); // min-heap (most negative first)

        for (Map.Entry<Long, BigDecimal> entry : net.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
            // net == 0 means user is already settled, skip
        }

        // Step 3: Greedily match largest debtor with largest creditor
        List<SettlementResponse> result = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.poll();
            Map.Entry<Long, BigDecimal> creditor = creditors.poll();

            BigDecimal debtAmount = debtor.getValue().abs();   // how much debtor owes overall
            BigDecimal creditAmount = creditor.getValue();      // how much creditor is owed overall

            BigDecimal settleAmount = debtAmount.min(creditAmount);

            User fromUser = userMap.get(debtor.getKey());
            User toUser = userMap.get(creditor.getKey());

            result.add(new SettlementResponse(
                    fromUser.getId(), fromUser.getName(),
                    toUser.getId(), toUser.getName(),
                    settleAmount
            ));

            // Update remaining balances
            BigDecimal remainingDebt = debtAmount.subtract(settleAmount);
            BigDecimal remainingCredit = creditAmount.subtract(settleAmount);

            if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                debtors.add(Map.entry(debtor.getKey(), remainingDebt.negate()));
            }
            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(Map.entry(creditor.getKey(), remainingCredit));
            }
        }

        return result;
    }
}
