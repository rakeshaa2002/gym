package com.fitnexus.backend.dto;

import lombok.Data;

/**
 * Kiosk self check-in: the member identifies themselves at the entrance screen.
 * Identifier can be their fingerprint ID, email, or member ID.
 */
@Data
public class KioskCheckInRequest {
    private String identifier;
}
