package com.hisaab.hisaab.controller;

import com.hisaab.hisaab.dto.SettlementResponse;
import com.hisaab.hisaab.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping("/{groupId}/settlements")
    public List<SettlementResponse> getSettlements(@PathVariable Long groupId) {
        return settlementService.getSettlements(groupId);
    }
}
