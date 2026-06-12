package com.hisaab.hisaab.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AddExpenseRequest {
    private String description;
    private BigDecimal amount;
    private Long groupId;
    private Long paidById;
    private String category;
    private List<Long> participantIds; // who shares this expense
}