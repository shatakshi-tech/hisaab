package com.hisaab.hisaab.dto;

import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    private Long createdByUserId;
}