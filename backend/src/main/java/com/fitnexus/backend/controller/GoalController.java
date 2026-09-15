package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.GoalRequest;
import com.fitnexus.backend.service.GoalServiceImplementation;
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
@RequestMapping("/api/goals")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class GoalController {
    private final GoalServiceImplementation goalServiceImplementation;

    @GetMapping
    public ResponseEntity<?> getMyGoals() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Goals retrieved successfully", goalServiceImplementation.getMyGoals(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/goals", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving goals", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/goals", "Error retrieving goals: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GoalRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Goal created successfully", goalServiceImplementation.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/goals", e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/goals", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating goal", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/goals", "Error creating goal: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GoalRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Goal updated successfully", goalServiceImplementation.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/goals/" + id, e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/goals/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating goal", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/goals/" + id, "Error updating goal: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            goalServiceImplementation.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Goal deleted successfully", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/goals/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting goal", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/goals/" + id, "Error deleting goal: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
