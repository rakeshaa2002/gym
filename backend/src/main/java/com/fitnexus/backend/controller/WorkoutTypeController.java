package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.WorkoutType;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.WorkoutTypeService;
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
@RequestMapping("/api/workout-types")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class WorkoutTypeController {
    private final WorkoutTypeService workoutTypeService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout types retrieved successfully", workoutTypeService.getAll(), LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving workout types", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-types", "Error retrieving workout types: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout type retrieved successfully", workoutTypeService.getById(id), LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-types/" + id, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-types/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving workout type", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-types/" + id, "Error retrieving workout type: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody WorkoutType request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Workout type created successfully", workoutTypeService.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-types", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating workout type", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-types", "Error creating workout type: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody WorkoutType request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout type updated successfully", workoutTypeService.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/workout-types/" + id, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-types/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating workout type", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-types/" + id, "Error updating workout type: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            workoutTypeService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout type deleted successfully", null, LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/workout-types/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting workout type", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/workout-types/" + id, "Error deleting workout type: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
