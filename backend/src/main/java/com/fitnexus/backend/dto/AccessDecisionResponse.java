package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Returned to the fingerprint terminal. {@code openGate} tells the device whether
 * to trigger its relay and open the turnstile/door.
 */
@Data
@AllArgsConstructor
public class AccessDecisionResponse {
    private boolean accessGranted;
    private boolean openGate;
    private String action;       // CHECK_IN | CHECK_OUT | DENIED
    private Long memberId;
    private String memberName;
    private LocalDateTime time;
    private String message;
}
