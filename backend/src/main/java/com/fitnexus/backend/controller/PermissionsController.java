package com.fitnexus.backend.controller;

import com.fitnexus.backend.config.JwtService;
import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.CurrentUserPermissionsResponse;
import com.fitnexus.backend.dto.PermissionsMatrixResponse;
import com.fitnexus.backend.dto.PermissionMatrixUpdateRequest;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class PermissionsController {

    private final PermissionService permissionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private Users getCurrentUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Authorization token required");
        }

        String token = authHeader.substring(7).trim();
        String email = jwtService.extractEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Invalid token user"));
    }

    private void requireSuperAdmin(Users user) {
        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new SecurityException("Only Super Admin can manage permissions");
        }
    }

    @GetMapping("/permissions/me")
    public ResponseEntity<?> getCurrentUserPermissions(@RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUser(authHeader);
            CurrentUserPermissionsResponse response = permissionService.getCurrentUserPermissions(user.getEmail());
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Permissions retrieved successfully",
                    response,
                    LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/me"));
        } catch (Exception e) {
            log.error("Error retrieving current user permissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving permissions: " + e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/me"));
        }
    }

    @GetMapping("/permissions/matrix")
    public ResponseEntity<?> getMatrix(@RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUser(authHeader);
            requireSuperAdmin(user);

            PermissionsMatrixResponse response = permissionService.getMatrix();
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Permission matrix retrieved successfully",
                    response,
                    LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/matrix"));
        } catch (Exception e) {
            log.error("Error retrieving permission matrix", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving permission matrix: " + e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/matrix"));
        }
    }

    @PutMapping("/permissions/matrix")
    public ResponseEntity<?> saveMatrix(
            @RequestHeader("Authorization") String authHeader,
            @org.springframework.web.bind.annotation.RequestBody PermissionMatrixUpdateRequest request
    ) {
        try {
            Users user = getCurrentUser(authHeader);
            requireSuperAdmin(user);

            permissionService.saveMatrix(request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Permission matrix saved successfully",
                    null,
                    LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/matrix"));
        } catch (Exception e) {
            log.error("Error saving permission matrix", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error saving permission matrix: " + e.getMessage(), LocalDateTime.now().toString(), "/api/access/permissions/matrix"));
        }
    }
}
