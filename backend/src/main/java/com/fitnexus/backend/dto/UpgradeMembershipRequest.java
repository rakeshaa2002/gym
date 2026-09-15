package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class UpgradeMembershipRequest {
    private String plan;     // target plan (PREMIUM)
    private Integer months;  // duration to add (default 1)
}
