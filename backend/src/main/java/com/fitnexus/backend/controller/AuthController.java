package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.service.AuthService;
import com.fitnexus.backend.service.AuthOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
                "http://localhost:3000",
        "http://gym.infitoolz.com",
        "https://gym.infitoolz.com"
})
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final AuthOtpService authOtpService;

    private ResponseEntity<?> ok(String message, Object data) {
        return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), message, data, java.time.LocalDateTime.now().toString()));
    }

    private ResponseEntity<?> err(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, java.time.LocalDateTime.now().toString(), path));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            var result = authOtpService.forgotPassword(request == null ? null : request.getEmail());
            return ok("If an account exists for that email, a reset code has been sent.", result);
        } catch (IllegalArgumentException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/auth/forgot-password", e.getMessage());
        } catch (Exception e) {
            log.error("forgot-password error", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/auth/forgot-password", "Could not process request: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            boolean valid = request != null && authOtpService.verifyOtp(request.getEmail(), request.getOtp(), request.getPurpose());
            if (!valid) {
                return err(HttpStatus.BAD_REQUEST, "/api/auth/verify-otp", "Invalid or expired code");
            }
            return ok("Code verified", java.util.Map.of("verified", true));
        } catch (Exception e) {
            log.error("verify-otp error", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/auth/verify-otp", "Could not verify code: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authOtpService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ok("Password updated successfully. You can now sign in.", null);
        } catch (IllegalArgumentException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/auth/reset-password", e.getMessage());
        } catch (Exception e) {
            log.error("reset-password error", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/auth/reset-password", "Could not reset password: " + e.getMessage());
        }
    }

    @PostMapping("/send-email-otp")
    public ResponseEntity<?> sendEmailOtp(@RequestBody ForgotPasswordRequest request) {
        try {
            var result = authOtpService.sendEmailVerification(request == null ? null : request.getEmail());
            return ok("Verification code sent.", result);
        } catch (IllegalArgumentException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/auth/send-email-otp", e.getMessage());
        } catch (Exception e) {
            log.error("send-email-otp error", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/auth/send-email-otp", "Could not send code: " + e.getMessage());
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyOtpRequest request) {
        try {
            authOtpService.verifyEmail(request.getEmail(), request.getOtp());
            return ok("Email verified successfully.", null);
        } catch (IllegalArgumentException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/auth/verify-email", e.getMessage());
        } catch (Exception e) {
            log.error("verify-email error", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/auth/verify-email", "Could not verify email: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/auth/login"));
            }

            LoginResponse response = authService.login(request);

            if (response.getToken() != null) {
                log.info("User logged in successfully: {}", request.getEmail());
                return ResponseEntity.ok(new ApiSuccessResponse<>(
                        HttpStatus.OK.value(),
                        "Login successful",
                        response,
                        java.time.LocalDateTime.now().toString()
                ));
            } else {
                log.warn("Failed login attempt for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Invalid email or password",
                                java.time.LocalDateTime.now().toString(), "/api/auth/login"));
            }
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred during login: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/auth/login"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/auth/signup"));
            }

            RegisterResponse response = authService.register(request);

            if (response.isSuccess()) {
                log.info("User registered successfully: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiSuccessResponse<>(
                                HttpStatus.CREATED.value(),
                                response.getMessage(),
                                response,
                                java.time.LocalDateTime.now().toString()
                        ));
            } else {
                log.warn("Registration failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(),
                                response.getMessage(),
                                java.time.LocalDateTime.now().toString(), "/api/auth/signup"));
            }
        } catch (Exception e) {
            log.error("Registration error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred during registration: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/auth/signup"));
        }
    }

}
