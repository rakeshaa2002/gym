package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.schedule.TrainerDutyScheduleRequest;
import com.fitnexus.backend.service.TrainerDutyScheduleServiceImplementation;
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
@RequestMapping("/api/trainer-duty-schedules")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class TrainerDutyScheduleController {
    private final TrainerDutyScheduleServiceImplementation trainerDutyScheduleServiceImplementation;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Trainer duty schedules retrieved successfully", trainerDutyScheduleServiceImplementation.getAll(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/trainer-duty-schedules", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving trainer duty schedules", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/trainer-duty-schedules", "Error retrieving trainer duty schedules: " + e.getMessage());
        }
    }

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeam() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Trainer team schedules retrieved successfully", trainerDutyScheduleServiceImplementation.getMyTeam(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/trainer-duty-schedules/my-team", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving trainer team schedules", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/trainer-duty-schedules/my-team", "Error retrieving trainer team schedules: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TrainerDutyScheduleRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Trainer duty schedule created successfully", trainerDutyScheduleServiceImplementation.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/trainer-duty-schedules", e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/trainer-duty-schedules", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating trainer duty schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/trainer-duty-schedules", "Error creating trainer duty schedule: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TrainerDutyScheduleRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Trainer duty schedule updated successfully", trainerDutyScheduleServiceImplementation.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/trainer-duty-schedules/" + id, e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/trainer-duty-schedules/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating trainer duty schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/trainer-duty-schedules/" + id, "Error updating trainer duty schedule: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            trainerDutyScheduleServiceImplementation.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Trainer duty schedule deleted successfully", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/trainer-duty-schedules/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting trainer duty schedule", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/trainer-duty-schedules/" + id, "Error deleting trainer duty schedule: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
