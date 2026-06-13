package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.SettlementResponse;
import com.hisaab.hisaab.dto.UserBalanceResponse;
import com.hisaab.hisaab.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping("/api/groups/{groupId}/settlements")
    public List<SettlementResponse> getSettlements(@PathVariable Long groupId) {
        return settlementService.getSettlements(groupId);
    }

    // NEW ENDPOINT
    @GetMapping("/api/users/{userId}/balances")
    public List<UserBalanceResponse> getUserBalances(@PathVariable Long userId) {
        return settlementService.getUserBalances(userId);
    }
}