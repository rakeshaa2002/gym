package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.DietPlan;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.DietPlanService;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/diet-plans")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class DietPlanController {
    private final DietPlanService dietPlanService;

    @GetMapping
    public ResponseEntity<?> getDietPlans() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Diet plans retrieved successfully",
                    dietPlanService.getAll(),
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving diet plans", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/diet-plans", "Error retrieving diet plans: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDietPlan(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Diet plan retrieved successfully",
                    dietPlanService.getById(id),
                    LocalDateTime.now().toString()
            ));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/diet-plans/" + id, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/diet-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving diet plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/diet-plans/" + id, "Error retrieving diet plan: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDietPlan(@RequestBody DietPlan request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(),
                    "Diet plan created successfully",
                    dietPlanService.create(request),
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/diet-plans", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating diet plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/diet-plans", "Error creating diet plan: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDietPlan(@PathVariable Long id, @RequestBody DietPlan request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Diet plan updated successfully",
                    dietPlanService.update(id, request),
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/diet-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating diet plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/diet-plans/" + id, "Error updating diet plan: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDietPlan(@PathVariable Long id) {
        try {
            dietPlanService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(),
                    "Diet plan deleted successfully",
                    null,
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error deleting diet plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/diet-plans/" + id, "Error deleting diet plan: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                message,
                LocalDateTime.now().toString(),
                path
        ));
    }
}
