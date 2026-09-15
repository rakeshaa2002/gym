package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.MembershipPlanRequest;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/membership-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class MembershipPlanController {
    private final MembershipPlanService membershipPlanService;

    /** List plans. Pass ?activeOnly=true for the member-facing / assignment dropdown. */
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly) {
        try {
            var data = activeOnly ? membershipPlanService.getActive() : membershipPlanService.getAll();
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership plans retrieved", data, LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving membership plans", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership-plans", "Error retrieving membership plans: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership plan retrieved", membershipPlanService.getById(id), LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/membership-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving membership plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership-plans/" + id, "Error retrieving membership plan: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MembershipPlanRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Membership plan created", membershipPlanService.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/membership-plans", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating membership plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership-plans", "Error creating membership plan: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MembershipPlanRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership plan updated", membershipPlanService.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/membership-plans/" + id, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/membership-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating membership plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership-plans/" + id, "Error updating membership plan: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            membershipPlanService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership plan deleted", null, LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/membership-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting membership plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership-plans/" + id, "Error deleting membership plan: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
