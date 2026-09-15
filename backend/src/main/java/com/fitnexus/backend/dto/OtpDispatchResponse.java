package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpDispatchResponse {
    private boolean delivered;  // true if emailed via SMTP, false if demo mode
    private String devOtp;      // populated only in demo mode (no SMTP) for testing
}
