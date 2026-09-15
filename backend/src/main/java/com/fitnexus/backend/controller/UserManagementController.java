package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.service.UserManagementServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {
    private final UserManagementServiceImplementation userManagementServiceImplementation;

    // ============ SUPER ADMIN ENDPOINTS ============ 

    @PostMapping("/super-admin")
    public ResponseEntity<?> createSuperAdmin(@Valid @RequestBody CreateSuperAdminRequest request,
                                              @RequestParam Long creatorId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/super-admin"));
            }
            SuperAdminResponse response = userManagementServiceImplementation.createSuperAdmin(request, creatorId);
            log.info("SuperAdmin created: {} by SuperAdmin: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "SuperAdmin created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid super admin creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin"));
        } catch (SecurityException e) {
            log.warn("Unauthorized super admin creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin"));
        } catch (Exception e) {
            log.error("Error creating super admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating super admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin"));
        }
    }

    @GetMapping("/super-admin/{superAdminId}")
    public ResponseEntity<?> getSuperAdmin(@PathVariable Long superAdminId, @RequestParam Long requesterId) {
        try {
            SuperAdminResponse response = userManagementServiceImplementation.getSuperAdmin(superAdminId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "SuperAdmin retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to super admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        } catch (Exception e) {
            log.error("Error retrieving super admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving super admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        }
    }

    @PutMapping("/super-admin/{superAdminId}")
    public ResponseEntity<?> updateSuperAdmin(@PathVariable Long superAdminId,
                                              @Valid @RequestBody CreateSuperAdminRequest request,
                                              @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
            }

            SuperAdminResponse response = userManagementServiceImplementation.updateSuperAdmin(superAdminId, request, updaterId);
            log.info("SuperAdmin updated: {} by user: {}", superAdminId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "SuperAdmin updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized super admin update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        } catch (Exception e) {
            log.error("Error updating super admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating super admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        }
    }

    @DeleteMapping("/super-admin/{superAdminId}")
    public ResponseEntity<?> deleteSuperAdmin(@PathVariable Long superAdminId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteSuperAdmin(superAdminId, deleterId);
            log.info("SuperAdmin deleted: {} by user: {}", superAdminId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "SuperAdmin deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized super admin deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        } catch (Exception e) {
            log.error("Error deleting super admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting super admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admin/" + superAdminId));
        }
    }

    @GetMapping("/super-admins")
    public ResponseEntity<?> getAllSuperAdmins(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllSuperAdmins(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "SuperAdmins retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to super admins list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admins"));
        } catch (Exception e) {
            log.error("Error retrieving super admins: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving super admins: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/super-admins"));
        }
    }

    // ============ ADMIN ENDPOINTS ============

    @PostMapping("/admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request,
                                         @RequestParam Long creatorId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/admin"));
            }
            AdminResponse response = userManagementServiceImplementation.createAdmin(request, creatorId);
            log.info("Admin created: {} by user: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Admin created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid admin creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin"));
        } catch (SecurityException e) {
            log.warn("Unauthorized admin creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin"));
        } catch (Exception e) {
            log.error("Error creating admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin"));
        }
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getAdmin(@PathVariable Long adminId, @RequestParam Long requesterId) {
        try {
            AdminResponse response = userManagementServiceImplementation.getAdmin(adminId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Admin retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        } catch (Exception e) {
            log.error("Error retrieving admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        }
    }

    @PutMapping("/admin/{adminId}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long adminId,
                                         @Valid @RequestBody CreateAdminRequest request,
                                         @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
            }

            AdminResponse response = userManagementServiceImplementation.updateAdmin(adminId, request, updaterId);
            log.info("Admin updated: {} by user: {}", adminId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Admin updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized admin update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        } catch (Exception e) {
            log.error("Error updating admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        }
    }

    @DeleteMapping("/admin/{adminId}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteAdmin(adminId, deleterId);
            log.info("Admin deleted: {} by user: {}", adminId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Admin deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized admin deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        } catch (Exception e) {
            log.error("Error deleting admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting admin: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admin/" + adminId));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllAdmins(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Admins retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to admins list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admins"));
        } catch (Exception e) {
            log.error("Error retrieving admins: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving admins: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/admins"));
        }
    }

    // ============ CORPORATE HR ENDPOINTS ============

    @PostMapping("/corporate-hr")
    public ResponseEntity<?> createCorporateHr(@Valid @RequestBody CreateCorporateHrRequest request,
                                               @RequestParam Long creatorId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
            }
            CorporateHrResponse response = userManagementServiceImplementation.createCorporateHr(request, creatorId);
            log.info("Corporate HR created: {} by user: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Corporate HR created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid corporate hr creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
        } catch (SecurityException e) {
            log.warn("Unauthorized corporate hr creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
        } catch (Exception e) {
            log.error("Error creating corporate hr: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating corporate hr: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
        }
    }

    // ============ MANAGER ENDPOINTS ============

    @PostMapping("/manager")
    public ResponseEntity<?> createManager(@Valid @RequestBody CreateAdminRequest request,
                                           @RequestParam Long creatorId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/manager"));
            }
            AdminResponse response = userManagementServiceImplementation.createManager(request, creatorId);
            log.info("Manager created: {} by user: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Manager created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid manager creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager"));
        } catch (SecurityException e) {
            log.warn("Unauthorized manager creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager"));
        } catch (Exception e) {
            log.error("Error creating manager: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating manager: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager"));
        }
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<?> getManager(@PathVariable Long managerId, @RequestParam Long requesterId) {
        try {
            AdminResponse response = userManagementServiceImplementation.getManager(managerId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Manager retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to manager: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        } catch (Exception e) {
            log.error("Error retrieving manager: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving manager: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        }
    }

    @PutMapping("/manager/{managerId}")
    public ResponseEntity<?> updateManager(@PathVariable Long managerId,
                                           @Valid @RequestBody CreateAdminRequest request,
                                           @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
            }

            AdminResponse response = userManagementServiceImplementation.updateManager(managerId, request, updaterId);
            log.info("Manager updated: {} by user: {}", managerId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Manager updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized manager update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        } catch (Exception e) {
            log.error("Error updating manager: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating manager: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        }
    }

    @DeleteMapping("/manager/{managerId}")
    public ResponseEntity<?> deleteManager(@PathVariable Long managerId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteManager(managerId, deleterId);
            log.info("Manager deleted: {} by user: {}", managerId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Manager deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized manager deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        } catch (Exception e) {
            log.error("Error deleting manager: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting manager: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/manager/" + managerId));
        }
    }

    @GetMapping("/managers")
    public ResponseEntity<?> getAllManagers(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllManagers(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Managers retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to managers list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/managers"));
        } catch (Exception e) {
            log.error("Error retrieving managers: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving managers: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/managers"));
        }
    }

    // ============ TRAINER ENDPOINTS ============

    @PostMapping("/trainer")
    public ResponseEntity<?> createTrainer(@Valid @RequestBody CreateTrainerRequest request,
                                           @RequestParam Long creatorId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/trainer"));
            }

            TrainerResponse response = userManagementServiceImplementation.createTrainer(request, creatorId);
            log.info("Trainer created: {} by user: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Trainer created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid trainer creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer"));
        } catch (SecurityException e) {
            log.warn("Unauthorized trainer creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer"));
        } catch (Exception e) {
            log.error("Error creating trainer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating trainer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer"));
        }
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<?> getTrainer(@PathVariable Long trainerId, @RequestParam Long requesterId) {
        try {
            TrainerResponse response = userManagementServiceImplementation.getTrainer(trainerId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Trainer retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to trainer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        } catch (Exception e) {
            log.error("Error retrieving trainer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving trainer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        }
    }

    @PutMapping("/trainer/{trainerId}")
    public ResponseEntity<?> updateTrainer(@PathVariable Long trainerId,
                                           @Valid @RequestBody CreateTrainerRequest request,
                                           @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
            }

            TrainerResponse response = userManagementServiceImplementation.updateTrainer(trainerId, request, updaterId);
            log.info("Trainer updated: {} by user: {}", trainerId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Trainer updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized trainer update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        } catch (Exception e) {
            log.error("Error updating trainer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating trainer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        }
    }

    @DeleteMapping("/trainer/{trainerId}")
    public ResponseEntity<?> deleteTrainer(@PathVariable Long trainerId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteTrainer(trainerId, deleterId);
            log.info("Trainer deleted: {} by user: {}", trainerId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Trainer deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized trainer deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        } catch (Exception e) {
            log.error("Error deleting trainer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting trainer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainer/" + trainerId));
        }
    }

    @GetMapping("/trainers")
    public ResponseEntity<?> getAllTrainers(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllTrainers(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Trainers retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to trainers list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainers"));
        } catch (Exception e) {
            log.error("Error retrieving trainers: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving trainers: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/trainers"));
        }
    }

    // ============ COUNSELOR ENDPOINTS ============

    @PostMapping("/counselor")
    public ResponseEntity<?> createCounselor(@Valid @RequestBody CreateCounselorRequest request,
                                           BindingResult bindingResult, @RequestParam Long creatorId) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/counselor"));
            }

            CounselorResponse response = userManagementServiceImplementation.createCounselor(request, creatorId);
            log.info("Counselor created: {} by user: {}", request.getEmail(), creatorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Counselor created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid counselor creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor"));
        } catch (SecurityException e) {
            log.warn("Unauthorized counselor creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor"));
        } catch (Exception e) {
            log.error("Error creating counselor: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating counselor: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor"));
        }
    }

    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getCounselor(@PathVariable Long counselorId, @RequestParam Long requesterId) {
        try {
            CounselorResponse response = userManagementServiceImplementation.getCounselor(counselorId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Counselor retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to counselor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        } catch (Exception e) {
            log.error("Error retrieving counselor: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving counselor: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        }
    }

    @PutMapping("/counselor/{counselorId}")
    public ResponseEntity<?> updateCounselor(@PathVariable Long counselorId,
                                           @Valid @RequestBody CreateCounselorRequest request,
                                           BindingResult bindingResult, @RequestParam Long updaterId) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
            }

            CounselorResponse response = userManagementServiceImplementation.updateCounselor(counselorId, request, updaterId);
            log.info("Counselor updated: {} by user: {}", counselorId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Counselor updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized counselor update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        } catch (Exception e) {
            log.error("Error updating counselor: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating counselor: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        }
    }

    @DeleteMapping("/counselor/{counselorId}")
    public ResponseEntity<?> deleteCounselor(@PathVariable Long counselorId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteCounselor(counselorId, deleterId);
            log.info("Counselor deleted: {} by user: {}", counselorId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Counselor deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized counselor deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        } catch (Exception e) {
            log.error("Error deleting counselor: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting counselor: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselor/" + counselorId));
        }
    }

    @GetMapping("/counselors")
    public ResponseEntity<?> getAllCounselors(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllCounselors(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Counselors retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to counselors list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselors"));
        } catch (Exception e) {
            log.error("Error retrieving counselors: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving counselors: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/counselors"));
        }
    }

    // ============ CUSTOMER ENDPOINTS ============

    @PostMapping("/customer/self-register")
    public ResponseEntity<?> createCustomerSelf(@Valid @RequestBody CreateCustomerSelfRequest request,
                                                BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/customer/self-register"));
            }

            CustomerResponse response = userManagementServiceImplementation.createCustomerSelf(request);
            log.info("Customer self-registered: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Customer registered successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid customer registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/self-register"));
        } catch (Exception e) {
            log.error("Error registering customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error registering customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/self-register"));
        }
    }

    @PostMapping("/customer/by-trainer")
    public ResponseEntity<?> createCustomerByTrainer(@Valid @RequestBody CreateCustomerByTrainerRequest request,
                                                     @RequestParam Long trainerId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/customer/by-trainer"));
            }

            CustomerResponse response = userManagementServiceImplementation.createCustomerByTrainer(request, trainerId);
            log.info("Customer created by trainer: {} for email: {}", trainerId, request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(),
                            "Customer created successfully", response,
                            java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid customer creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/by-trainer"));
        } catch (SecurityException e) {
            log.warn("Unauthorized customer creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/by-trainer"));
        } catch (Exception e) {
            log.error("Error creating customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error creating customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/by-trainer"));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable Long customerId, @RequestParam Long requesterId) {
        try {
            CustomerResponse response = userManagementServiceImplementation.getCustomer(customerId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customer retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        } catch (Exception e) {
            log.error("Error retrieving customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        }
    }

    @PutMapping("/customer/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long customerId,
                                            @Valid @RequestBody UpdateCustomerDetailsRequest request,
                                            @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
            }

            CustomerResponse response = userManagementServiceImplementation.updateCustomer(customerId, request, updaterId);
            log.info("Customer updated: {} by user: {}", customerId, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customer updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized customer update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        } catch (Exception e) {
            log.error("Error updating customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        }
    }

    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteCustomer(customerId, deleterId);
            log.info("Customer deleted: {} by user: {}", customerId, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customer deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized customer deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        } catch (Exception e) {
            log.error("Error deleting customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId));
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllCustomers(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customers retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to customers list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers"));
        } catch (Exception e) {
            log.error("Error retrieving customers: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving customers: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers"));
        }
    }

    @GetMapping("/customers/assigned-to/{trainerId}")
    public ResponseEntity<?> getCustomersAssignedToTrainer(@PathVariable Long trainerId) {
        try {
            var response = userManagementServiceImplementation.getCustomersAssignedToTrainer(trainerId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customers retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving customers for trainer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving customers: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers/assigned-to/" + trainerId));
        }
    }

    /**
     * Returns members whose membership expires within the next {@code days} days.
     * Useful for the Renewal Management dashboard.
     */
    @GetMapping("/customers/expiring")
    public ResponseEntity<?> getExpiringMembers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getExpiringMembers(days, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Expiring members retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to expiring members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers/expiring"));
        } catch (Exception e) {
            log.error("Error retrieving expiring members: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving expiring members: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers/expiring"));
        }
    }

    /**
     * Returns members at risk of churn and the associated reasons.
     * Useful for the Churn Dashboard.
     */
    @GetMapping("/customers/at-risk")
    public ResponseEntity<?> getAtRiskMembers(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAtRiskMembers(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "At-risk members retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to at-risk members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers/at-risk"));
        } catch (Exception e) {
            log.error("Error retrieving at-risk members: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving at-risk members: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customers/at-risk"));
        }
    }

    /**
     * GET /api/trainers/performance
     * Returns aggregated performance metrics for all visible trainers, sorted by score.
     */
    @GetMapping("/trainers/performance")
    public ResponseEntity<?> getTrainerPerformances(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getTrainerPerformances(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Trainer performances retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to trainer performances: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/trainers/performance"));
        } catch (Exception e) {
            log.error("Error retrieving trainer performances: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving trainer performances: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/trainers/performance"));
        }
    }

    @PostMapping("/customer/{customerId}/activate")
    public ResponseEntity<?> activateCustomer(@PathVariable Long customerId, @RequestParam Long trainerId) {
        try {
            CustomerResponse response = userManagementServiceImplementation.activateCustomer(customerId, trainerId);
            log.info("Customer activated: {} by trainer: {}", customerId, trainerId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Customer activated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized customer activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId + "/activate"));
        } catch (Exception e) {
            log.error("Error activating customer: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error activating customer: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/customer/" + customerId + "/activate"));
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> setUserStatus(@PathVariable Long userId,
                                           @RequestParam boolean active,
                                           @RequestParam Long updaterId) {
        try {
            userManagementServiceImplementation.setUserActiveStatus(userId, active, updaterId);
            log.info("User {} status set to {} by {}", userId, active ? "ACTIVE" : "INACTIVE", updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    active ? "User activated successfully" : "User deactivated successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized status change for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/status"));
        } catch (Exception e) {
            log.error("Error changing user status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error changing user status: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/status"));
        }
    }

    @PutMapping("/{userId}/assign-diet/{dietPlanId}")
    public ResponseEntity<?> assignDietPlan(@PathVariable Long userId, @PathVariable Long dietPlanId) {
        try {
            var response = userManagementServiceImplementation.assignDietPlanToUser(userId, dietPlanId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Diet plan assigned successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized diet plan assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-diet/" + dietPlanId));
        } catch (com.fitnexus.backend.exception.InvalidOperationException e) {
            log.warn("Invalid diet plan assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-diet/" + dietPlanId));
        } catch (Exception e) {
            log.error("Error assigning diet plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error assigning diet plan: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-diet/" + dietPlanId));
        }
    }

    @GetMapping("/me/diet-plan")
    public ResponseEntity<?> getMyDietPlan() {
        try {
            var response = userManagementServiceImplementation.getMyDietPlan();
            if (response == null) {
                return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                        "No diet plan has been assigned to you yet.",
                        null,
                        java.time.LocalDateTime.now().toString()));
            }

            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Diet plan retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to my diet plan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/diet-plan"));
        } catch (Exception e) {
            log.error("Error retrieving my diet plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving diet plan: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/diet-plan"));
        }
    }

    @PutMapping("/{userId}/assign-workout/{workoutPlanId}")
    public ResponseEntity<?> assignWorkoutPlan(@PathVariable Long userId, @PathVariable Long workoutPlanId) {
        try {
            var response = userManagementServiceImplementation.assignWorkoutPlanToUser(userId, workoutPlanId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Workout plan assigned successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized workout plan assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-workout/" + workoutPlanId));
        } catch (com.fitnexus.backend.exception.InvalidOperationException e) {
            log.warn("Invalid workout plan assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-workout/" + workoutPlanId));
        } catch (Exception e) {
            log.error("Error assigning workout plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error assigning workout plan: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/" + userId + "/assign-workout/" + workoutPlanId));
        }
    }

    @GetMapping("/me/workout-plan")
    public ResponseEntity<?> getMyWorkoutPlan() {
        try {
            var response = userManagementServiceImplementation.getMyWorkoutPlan();
            if (response == null) {
                return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                        "No workout plan has been assigned to you yet.",
                        null,
                        java.time.LocalDateTime.now().toString()));
            }

            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Workout plan retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to my workout plan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/workout-plan"));
        } catch (Exception e) {
            log.error("Error retrieving my workout plan: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving workout plan: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/workout-plan"));
        }
    }
    @GetMapping("/me/profile")
    public ResponseEntity<?> getMyProfile() {
        try {
            var response = userManagementServiceImplementation.getMyProfile();
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Profile retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/profile"));
        } catch (Exception e) {
            log.error("Error retrieving my profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving profile: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/profile"));
        }
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody MyProfileRequest request) {
        try {
            var response = userManagementServiceImplementation.updateMyProfile(request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Profile updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/profile"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/profile"));
        } catch (Exception e) {
            log.error("Error updating my profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating profile: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/profile"));
        }
    }

    @PostMapping("/me/onboarding-complete")
    public ResponseEntity<?> completeOnboarding() {
        try {
            userManagementServiceImplementation.completeOnboarding();
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Onboarding marked complete", null, java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/onboarding-complete"));
        } catch (Exception e) {
            log.error("Error completing onboarding: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error completing onboarding: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/me/onboarding-complete"));
        }
    }

    @GetMapping("/reporting-options")
    public ResponseEntity<?> getReportingOptions(@RequestParam Role role,
                                                 @RequestParam(required = false) Long branchId,
                                                 @RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getReportingOptions(role, branchId, requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Reporting options retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized reporting options request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/reporting-options"));
        } catch (Exception e) {
            log.error("Error retrieving reporting options: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving reporting options: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/reporting-options"));
        }
    }

    // ============ CORPORATE HR ENDPOINTS ============

    @PutMapping("/corporate-hr/{id}")
    public ResponseEntity<?> updateCorporateHr(@PathVariable Long id,
                                               @Valid @RequestBody CreateCorporateHrRequest request,
                                               @RequestParam Long updaterId, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors() && !Boolean.TRUE.equals(request.getKeepPassword())) {
                String errorMessage = bindingResult.getFieldError().getDefaultMessage();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                                java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr/" + id));
            }

            CorporateHrResponse response = userManagementServiceImplementation.updateCorporateHr(id, request, updaterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Corporate HR Account updated successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized Corporate HR update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr/" + id));
        } catch (Exception e) {
            log.error("Error updating Corporate HR: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error updating Corporate HR account: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr/" + id));
        }
    }

    @DeleteMapping("/corporate-hr/{id}")
    public ResponseEntity<?> deleteCorporateHr(@PathVariable Long id, @RequestParam Long deleterId) {
        try {
            userManagementServiceImplementation.deleteCorporateHr(id, deleterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Corporate HR Account deleted successfully", null,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized Corporate HR deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr/" + id));
        } catch (Exception e) {
            log.error("Error deleting Corporate HR: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error deleting Corporate HR account: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr/" + id));
        }
    }

    @GetMapping("/corporate-hr")
    public ResponseEntity<?> getAllCorporateHrs(@RequestParam Long requesterId) {
        try {
            var response = userManagementServiceImplementation.getAllCorporateHrs(requesterId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Corporate HR accounts retrieved successfully", response,
                    java.time.LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            log.warn("Unauthorized access to corporate HR list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
        } catch (Exception e) {
            log.error("Error retrieving corporate HR accounts: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error retrieving corporate HR accounts: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(), "/api/users/corporate-hr"));
        }
    }
}

