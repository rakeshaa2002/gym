package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.ProgressEntryRequest;
import com.fitnexus.backend.service.ProgressServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class ProgressController {
    private final ProgressServiceImplementation progressServiceImplementation;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Progress summary retrieved successfully", progressServiceImplementation.getSummary(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/progress/summary", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving progress summary", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/progress/summary", "Error retrieving progress summary: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyEntries() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Progress entries retrieved successfully", progressServiceImplementation.getMyEntries(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/progress", e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving progress entries", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/progress", "Error retrieving progress entries: " + e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getMemberEntries(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Member progress entries retrieved successfully", progressServiceImplementation.getMemberEntries(memberId), LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving progress entries for member " + memberId, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/progress/member/" + memberId, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProgressEntryRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Progress entry created successfully", progressServiceImplementation.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/progress", e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/progress", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating progress entry", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/progress", "Error creating progress entry: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            progressServiceImplementation.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Progress entry deleted successfully", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/progress/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting progress entry", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/progress/" + id, "Error deleting progress entry: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
