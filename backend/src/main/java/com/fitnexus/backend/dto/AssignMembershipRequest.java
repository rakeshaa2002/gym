package com.fitnexus.backend.dto;

import lombok.Data;

/** Staff request to put a member on a plan and assign their daily check-in window. */
@Data
public class AssignMembershipRequest {
    private Long planId;
    private String accessStartTime; // "HH:mm" — required for time-restricted plans
    private String accessEndTime;   // "HH:mm"
    private Integer months;         // optional override; defaults to the plan's duration
}
