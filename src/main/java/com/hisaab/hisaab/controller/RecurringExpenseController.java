package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.RecurringExpenseRequest;
import com.hisaab.hisaab.entity.Group;
import com.hisaab.hisaab.entity.RecurringExpense;
import com.hisaab.hisaab.entity.User;
import com.hisaab.hisaab.repository.GroupRepository;
import com.hisaab.hisaab.repository.RecurringExpenseRepository;
import com.hisaab.hisaab.repository.UserRepository;
import com.hisaab.hisaab.service.RecurringExpenseScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring-expenses")
public class RecurringExpenseController {

    @Autowired private RecurringExpenseRepository recurringExpenseRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RecurringExpenseScheduler recurringExpenseScheduler;


    @PostMapping("/run-now")
    public ResponseEntity<String> runNow() {
        recurringExpenseScheduler.processRecurringExpenses();
        return ResponseEntity.ok("Recurring expenses processed");
    }

    @PostMapping
    public ResponseEntity<RecurringExpense> create(@RequestBody RecurringExpenseRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User paidBy = userRepository.findById(request.getPaidById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RecurringExpense re = new RecurringExpense();
        re.setDescription(request.getDescription());
        re.setAmount(request.getAmount());
        re.setGroup(group);
        re.setPaidBy(paidBy);
        re.setCategory(request.getCategory());
        re.setDayOfMonth(request.getDayOfMonth());

        return ResponseEntity.ok(recurringExpenseRepository.save(re));
    }
}
