package com.hisaab.hisaab.service;

import com.hisaab.hisaab.dto.SettlementResponse;
import com.hisaab.hisaab.dto.UserBalanceResponse;
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
        // ... existing code unchanged ...
        List<Balance> balances = balanceRepository.findByGroupId(groupId);

        Map<Long, BigDecimal> net = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (Balance b : balances) {
            Long owedByUserId = b.getOwedBy().getId();
            Long owedToUserId = b.getOwedTo().getId();
            BigDecimal amount = b.getAmount();

            net.merge(owedByUserId, amount.negate(), BigDecimal::add);
            net.merge(owedToUserId, amount, BigDecimal::add);

            userMap.putIfAbsent(owedByUserId, b.getOwedBy());
            userMap.putIfAbsent(owedToUserId, b.getOwedTo());
        }

        PriorityQueue<Map.Entry<Long, BigDecimal>> creditors =
                new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

        PriorityQueue<Map.Entry<Long, BigDecimal>> debtors =
                new PriorityQueue<>((a, b) -> a.getValue().compareTo(b.getValue()));

        for (Map.Entry<Long, BigDecimal> entry : net.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }

        List<SettlementResponse> result = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.poll();
            Map.Entry<Long, BigDecimal> creditor = creditors.poll();

            BigDecimal debtAmount = debtor.getValue().abs();
            BigDecimal creditAmount = creditor.getValue();

            BigDecimal settleAmount = debtAmount.min(creditAmount);

            User fromUser = userMap.get(debtor.getKey());
            User toUser = userMap.get(creditor.getKey());

            result.add(new SettlementResponse(
                    fromUser.getId(), fromUser.getName(),
                    toUser.getId(), toUser.getName(),
                    settleAmount
            ));

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

    // NEW METHOD
    public List<UserBalanceResponse> getUserBalances(Long userId) {
        List<UserBalanceResponse> result = new ArrayList<>();

        // Balances where this user OWES someone
        for (Balance b : balanceRepository.findByOwedById(userId)) {
            result.add(new UserBalanceResponse(
                    b.getGroup().getId(), b.getGroup().getName(),
                    b.getOwedTo().getId(), b.getOwedTo().getName(),
                    b.getAmount(), "YOU_OWE"
            ));
        }

        // Balances where someone OWES this user
        for (Balance b : balanceRepository.findByOwedToId(userId)) {
            result.add(new UserBalanceResponse(
                    b.getGroup().getId(), b.getGroup().getName(),
                    b.getOwedBy().getId(), b.getOwedBy().getName(),
                    b.getAmount(), "OWES_YOU"
            ));
        }

        return result;
    }
}