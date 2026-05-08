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
@CrossOrigin(origins = "http://localhost:5173")
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
}

