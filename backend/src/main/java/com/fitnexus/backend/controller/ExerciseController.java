package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.Exercise;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.ExerciseService;
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
@RequestMapping("/api/exercises")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Exercises retrieved successfully", exerciseService.getAll(), LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving exercises", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/exercises", "Error retrieving exercises: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Exercise retrieved successfully", exerciseService.getById(id), LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/exercises/" + id, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/exercises/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving exercise", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/exercises/" + id, "Error retrieving exercise: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Exercise request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Exercise created successfully", exerciseService.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/exercises", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating exercise", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/exercises", "Error creating exercise: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Exercise request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Exercise updated successfully", exerciseService.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/exercises/" + id, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/exercises/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating exercise", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/exercises/" + id, "Error updating exercise: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            exerciseService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Exercise deleted successfully", null, LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/exercises/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting exercise", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/exercises/" + id, "Error deleting exercise: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
