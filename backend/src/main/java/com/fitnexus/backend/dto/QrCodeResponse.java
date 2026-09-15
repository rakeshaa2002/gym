package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** A time-limited QR payload for member check-in; the client re-fetches when it expires. */
@Data
@AllArgsConstructor
public class QrCodeResponse {
    private String value;            // what the QR encodes: "<memberId>.<otp>"
    private int periodSeconds;       // rotation period (30s)
    private int expiresInSeconds;    // seconds left in the current window
}
