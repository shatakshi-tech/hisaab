package com.hisaab.hisaab.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecurringExpenseRequest {
    private String description;
    private BigDecimal amount;
    private Long groupId;
    private Long paidById;
    private String category;
    private Integer dayOfMonth;
}