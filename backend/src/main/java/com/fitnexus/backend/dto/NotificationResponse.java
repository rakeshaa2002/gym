package com.fitnexus.backend.dto;

/**
 * A single header notification. Some are persisted rows, others are derived live
 * (pending approvals, upcoming schedules). The {@code key} is a stable identifier
 * so the client can track read/dismissed state across refetches.
 */
public record NotificationResponse(
        String key,      // stable identifier, e.g. "n-12", "approval-28", "onboard-28"
        String type,     // APPROVAL | SCHEDULE | INFO — drives the icon/colour on the client
        String title,
        String message,
        String time,     // human-friendly label, e.g. "08:30" or "Jun 18"
        String link      // route the client should navigate to when clicked
) {
}
