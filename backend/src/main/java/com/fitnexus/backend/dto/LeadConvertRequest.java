package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class LeadConvertRequest {
    private String password;
    private Long assignedTrainerId;
    private Long branchId;
    private String membershipPlanCode; // BASIC, STANDARD, PREMIUM, etc.
}
