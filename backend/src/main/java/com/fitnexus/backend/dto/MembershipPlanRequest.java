package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class MembershipPlanRequest {
    private String code;
    private String name;
    private String description;
    private Integer price;
    private Integer durationDays;
    private Integer maxSessionMinutes;
    private Boolean unlimitedAccess;
    private Boolean trainerChat;
    private String features; // newline-separated bullets
    private Boolean active;
}
