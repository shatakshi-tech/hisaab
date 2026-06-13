package com.hisaab.hisaab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupSummaryResponse {
    private Long groupId;
    private String groupName;
    private BigDecimal totalExpenses;
    private List<SettlementResponse> settlements;
}
