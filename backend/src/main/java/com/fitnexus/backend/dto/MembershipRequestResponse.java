package com.fitnexus.backend.dto;

/** A pending plan-change request shown to staff. */
public record MembershipRequestResponse(
        Long id,
        Long memberId,
        String memberName,
        String memberEmail,
        String currentPlan,    // the member's current plan name
        Long requestedPlanId,
        String requestedPlan,  // the plan they want
        String note,
        String createdAt
) {
}
