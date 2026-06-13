package com.hisaab.hisaab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserBalanceResponse {
    private Long groupId;
    private String groupName;
    private Long otherUserId;
    private String otherUserName;
    private BigDecimal amount;
    private String direction; // "YOU_OWE" or "OWES_YOU"
}
