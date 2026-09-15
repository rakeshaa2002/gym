package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.schedule.UserWorkoutScheduleRequest;
import com.fitnexus.backend.service.UserWorkoutScheduleServiceImplementation;
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
@RequestMapping("/api/user-workout-schedules")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class UserWorkoutScheduleController {
    private final UserWorkoutScheduleServiceImplementation userWorkoutScheduleServiceImplementation;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout schedules retrieved successfully", userWorkoutScheduleServiceImplementation.getAll(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving workout schedules", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules", "Error retrieving workout schedules: " + e.getMessage());
        }
    }

    @GetMapping("/my-created")
    public ResponseEntity<?> getMyCreated() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Created workout schedules retrieved successfully", userWorkoutScheduleServiceImplementation.getMyCreated(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules/my-created", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving created workout schedules", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules/my-created", "Error retrieving created workout schedules: " + e.getMessage());
        }
    }

    @GetMapping("/my-users")
    public ResponseEntity<?> getMyUsers() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "User workout schedules retrieved successfully", userWorkoutScheduleServiceImplementation.getMyUsers(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules/my-users", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving user workout schedules", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules/my-users", "Error retrieving user workout schedules: " + e.getMessage());
        }
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<?> getMySchedule() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "My workout schedule retrieved successfully", userWorkoutScheduleServiceImplementation.getMySchedule(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules/my-schedule", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving my workout schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules/my-schedule", "Error retrieving my workout schedule: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserWorkoutScheduleRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Workout schedule created successfully", userWorkoutScheduleServiceImplementation.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/user-workout-schedules", e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating workout schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules", "Error creating workout schedule: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserWorkoutScheduleRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout schedule updated successfully", userWorkoutScheduleServiceImplementation.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/user-workout-schedules/" + id, e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating workout schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules/" + id, "Error updating workout schedule: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            userWorkoutScheduleServiceImplementation.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Workout schedule deleted successfully", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/user-workout-schedules/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting workout schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/user-workout-schedules/" + id, "Error deleting workout schedule: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
