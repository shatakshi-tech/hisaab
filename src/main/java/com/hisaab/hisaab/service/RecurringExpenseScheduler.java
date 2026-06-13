package com.hisaab.hisaab.service;

import com.hisaab.hisaab.dto.AddExpenseRequest;
import com.hisaab.hisaab.entity.RecurringExpense;
import com.hisaab.hisaab.repository.GrpMemberRepository;
import com.hisaab.hisaab.repository.RecurringExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringExpenseScheduler {

    @Autowired
    private RecurringExpenseRepository recurringExpenseRepository;

    @Autowired
    private GrpMemberRepository grpMemberRepository;

    @Autowired
    private ExpenseService expenseService;

    // Runs every day at midnight: cron = "sec min hour day month weekday"
    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringExpenses() {
        int today = LocalDate.now().getDayOfMonth();
        LocalDate todayDate = LocalDate.now();

        List<RecurringExpense> dueToday = recurringExpenseRepository.findByDayOfMonth(today);

        for (RecurringExpense re : dueToday) {
            // Skip if already run today (avoid duplicates on restart)
            if (re.getLastRunDate() != null && re.getLastRunDate().equals(todayDate)) {
                continue;
            }

            // Get all group members to split equally
            List<Long> participantIds = grpMemberRepository.findByGroupId(re.getGroup().getId())
                    .stream()
                    .map(gm -> gm.getUser().getId())
                    .collect(Collectors.toList());

            AddExpenseRequest request = new AddExpenseRequest();
            request.setDescription(re.getDescription() + " (auto-generated)");
            request.setAmount(re.getAmount());
            request.setGroupId(re.getGroup().getId());
            request.setPaidById(re.getPaidBy().getId());
            request.setCategory(re.getCategory());
            request.setParticipantIds(participantIds);

            expenseService.addExpense(request);

            re.setLastRunDate(todayDate);
            recurringExpenseRepository.save(re);
        }
    }
}