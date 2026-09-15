package com.fitnexus.backend.dto;

/** Minimal member info for the attendance enrollment / kiosk dropdowns. */
public record AttendanceMemberResponse(Long id, String name, String email) {
}
