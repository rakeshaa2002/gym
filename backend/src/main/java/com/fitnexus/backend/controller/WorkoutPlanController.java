package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.WorkoutPlan;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.WorkoutPlanService;
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
@RequestMapping("/api/workout-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanController {
    private final WorkoutPlanService workoutPlanService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout plans retrieved successfully", workoutPlanService.getAll(), LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving workout plans", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-plans", "Error retrieving workout plans: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout plan retrieved successfully", workoutPlanService.getById(id), LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-plans/" + id, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving workout plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-plans/" + id, "Error retrieving workout plan: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody WorkoutPlan request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Workout plan created successfully", workoutPlanService.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-plans", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating workout plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-plans", "Error creating workout plan: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody WorkoutPlan request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout plan updated successfully", workoutPlanService.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-plans/" + id, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating workout plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-plans/" + id, "Error updating workout plan: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            workoutPlanService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout plan deleted successfully", null, LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-plans/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting workout plan", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-plans/" + id, "Error deleting workout plan: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
