package com.fitnexus.backend.controller;

import com.fitnexus.backend.config.JwtService;
import com.fitnexus.backend.dto.*;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.service.OrganizationHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/api/org")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class OrganizationHierarchyController {

    private final OrganizationHierarchyService orgService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // ============ AUTH HELPERS ============

    private Users getCurrentUserFromAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Authorization token required");
        }

        String token = authHeader.substring(7).trim();
        String email = jwtService.extractEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Invalid token user"));
    }

    private void requireRole(Users user, String... allowedRoles) {
        String role = String.valueOf(user.getRole()).toUpperCase();
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(r -> r.equalsIgnoreCase(role));
        if (!allowed) {
            throw new SecurityException("Unauthorized: Role " + role + " cannot perform this action");
        }
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String path, BindingResult bindingResult) {
        String message = bindingResult.getFieldError() != null
                ? bindingResult.getFieldError().getDefaultMessage()
                : "Invalid request";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        LocalDateTime.now().toString(),
                        path
                ));
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        message,
                        LocalDateTime.now().toString(),
                        path
                ));
    }

    // ============ HEAD OFFICE ============

    @GetMapping("/head-offices")
    public ResponseEntity<?> getAllHeadOffices(@RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN");

            var data = orgService.getAllHeadOffices();
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Head offices retrieved successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/head-offices", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving head offices", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/head-offices", "Error retrieving head offices: " + e.getMessage());
        }
    }

    @PostMapping("/head-offices")
    public ResponseEntity<?> createHeadOffice(
            @Valid @RequestBody HeadOfficeRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/head-offices", bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN");

            var data = orgService.createHeadOffice(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), "Head office created successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/head-offices", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating head office", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/head-offices", "Error creating head office: " + e.getMessage());
        }
    }

    @PutMapping("/head-offices/{id}")
    public ResponseEntity<?> updateHeadOffice(
            @PathVariable Long id,
            @Valid @RequestBody HeadOfficeRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/head-offices/" + id, bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN");

            var data = orgService.updateHeadOffice(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Head office updated successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/head-offices/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating head office", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/head-offices/" + id, "Error updating head office: " + e.getMessage());
        }
    }

    @DeleteMapping("/head-offices/{id}")
    public ResponseEntity<?> deleteHeadOffice(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN");

            orgService.deleteHeadOffice(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Head office deleted successfully", null, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/head-offices/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting head office", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/head-offices/" + id, "Error deleting head office: " + e.getMessage());
        }
    }

    // ============ BRANCH ============

    @GetMapping("/branches")
    public ResponseEntity<?> getBranches(
            @RequestParam(required = false) Long headOfficeId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN", "MANAGER");

            var data = (headOfficeId != null)
                    ? orgService.getBranchesByHeadOffice(headOfficeId)
                    : orgService.getAllBranches();

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Branches retrieved successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/branches", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving branches", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/branches", "Error retrieving branches: " + e.getMessage());
        }
    }

    @PostMapping("/branches")
    public ResponseEntity<?> createBranch(
            @Valid @RequestBody BranchRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/branches", bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.createBranch(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), "Branch created successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/branches", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating branch", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/branches", "Error creating branch: " + e.getMessage());
        }
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<?> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/branches/" + id, bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.updateBranch(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Branch updated successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/branches/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating branch", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/branches/" + id, "Error updating branch: " + e.getMessage());
        }
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            orgService.deleteBranch(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Branch deleted successfully", null, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/branches/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting branch", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/branches/" + id, "Error deleting branch: " + e.getMessage());
        }
    }

    // ============ DEPARTMENT ============

    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments(
            @RequestParam(required = false) Long branchId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN", "MANAGER");

            var data = (branchId != null)
                    ? orgService.getDepartmentsByBranch(branchId)
                    : orgService.getAllDepartments();

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Departments retrieved successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/departments", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving departments", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/departments", "Error retrieving departments: " + e.getMessage());
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/departments", bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.createDepartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), "Department created successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/departments", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating department", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/departments", "Error creating department: " + e.getMessage());
        }
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/departments/" + id, bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.updateDepartment(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Department updated successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/departments/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating department", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/departments/" + id, "Error updating department: " + e.getMessage());
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            orgService.deleteDepartment(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Department deleted successfully", null, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/departments/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting department", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/departments/" + id, "Error deleting department: " + e.getMessage());
        }
    }

    // ============ TEAM ============

    @GetMapping("/teams")
    public ResponseEntity<?> getTeams(
            @RequestParam(required = false) Long departmentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN", "MANAGER");

            var data = (departmentId != null)
                    ? orgService.getTeamsByDepartment(departmentId)
                    : orgService.getAllTeams();

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Teams retrieved successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/teams", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving teams", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/teams", "Error retrieving teams: " + e.getMessage());
        }
    }

    @PostMapping("/teams")
    public ResponseEntity<?> createTeam(
            @Valid @RequestBody TeamRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/teams", bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.createTeam(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), "Team created successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/teams", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating team", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/teams", "Error creating team: " + e.getMessage());
        }
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<?> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/teams/" + id, bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.updateTeam(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Team updated successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/teams/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating team", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/teams/" + id, "Error updating team: " + e.getMessage());
        }
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            orgService.deleteTeam(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Team deleted successfully", null, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/teams/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting team", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/teams/" + id, "Error deleting team: " + e.getMessage());
        }
    }

    // ============ DESIGNATION ============

    @GetMapping("/designations")
    public ResponseEntity<?> getDesignations(
            @RequestParam(required = false) Long departmentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN", "MANAGER");

            var data = (departmentId != null)
                    ? orgService.getDesignationsByDepartment(departmentId)
                    : orgService.getAllDesignations();

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Designations retrieved successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/designations", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving designations", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/designations", "Error retrieving designations: " + e.getMessage());
        }
    }

    @PostMapping("/designations")
    public ResponseEntity<?> createDesignation(
            @Valid @RequestBody DesignationRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/designations", bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.createDesignation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), "Designation created successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/designations", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating designation", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/designations", "Error creating designation: " + e.getMessage());
        }
    }

    @PutMapping("/designations/{id}")
    public ResponseEntity<?> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (bindingResult.hasErrors()) return badRequest("/api/org/designations/" + id, bindingResult);

        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            var data = orgService.updateDesignation(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Designation updated successfully", data, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/designations/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating designation", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/designations/" + id, "Error updating designation: " + e.getMessage());
        }
    }

    @DeleteMapping("/designations/{id}")
    public ResponseEntity<?> deleteDesignation(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            requireRole(user, "SUPER_ADMIN", "ADMIN");

            orgService.deleteDesignation(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Designation deleted successfully", null, LocalDateTime.now().toString()
            ));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/org/designations/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting designation", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/designations/" + id, "Error deleting designation: " + e.getMessage());
        }
    }

    // ============ CASCADE ============

    @GetMapping("/cascade/branches-for-employee")
    public ResponseEntity<?> getBranchesForEmployee(@RequestHeader("Authorization") String authHeader) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            var data = orgService.getBranchesForEmployee(user.getId());

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Branches retrieved", data, LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving branches for employee", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/cascade/branches-for-employee", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/cascade/departments-for-employee")
    public ResponseEntity<?> getDepartmentsForEmployee(
            @RequestParam Long branchId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            var data = orgService.getDepartmentsForEmployee(branchId, user.getId());

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Departments retrieved", data, LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving departments for employee", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/cascade/departments-for-employee", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/cascade/teams-for-employee")
    public ResponseEntity<?> getTeamsForEmployee(
            @RequestParam Long departmentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            var data = orgService.getTeamsForEmployee(departmentId, user.getId());

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Teams retrieved", data, LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving teams for employee", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/cascade/teams-for-employee", "Error: " + e.getMessage());
        }
    }

    @GetMapping("/cascade/designations-for-employee")
    public ResponseEntity<?> getDesignationsForEmployee(
            @RequestParam Long departmentId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Users user = getCurrentUserFromAuthHeader(authHeader);
            var data = orgService.getDesignationsForEmployee(departmentId, user.getId());

            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), "Designations retrieved", data, LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving designations for employee", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/org/cascade/designations-for-employee", "Error: " + e.getMessage());
        }
    }
}
