package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

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