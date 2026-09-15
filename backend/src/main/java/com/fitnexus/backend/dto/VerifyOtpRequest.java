package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;
    private String purpose; // RESET | VERIFY
}
