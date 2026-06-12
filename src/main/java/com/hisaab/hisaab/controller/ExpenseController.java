package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.AddExpenseRequest;
import com.hisaab.hisaab.dto.ExpenseResponse;
import com.hisaab.hisaab.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@RequestBody AddExpenseRequest request) {
        ExpenseResponse response = expenseService.addExpense(request);
        return ResponseEntity.ok(response);
    }
}