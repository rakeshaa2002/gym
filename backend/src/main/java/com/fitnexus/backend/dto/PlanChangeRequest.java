package com.fitnexus.backend.dto;

import lombok.Data;

/** A member's request to be moved to a different plan (admin-assisted upgrade). */
@Data
public class PlanChangeRequest {
    private Long planId;  // the plan the member wants
    private String note;  // optional message to staff
}
