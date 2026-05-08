package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.BodyPart;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.BodyPartService;
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
@RequestMapping("/api/body-parts")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class BodyPartController {
    private final BodyPartService bodyPartService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Body parts retrieved successfully", bodyPartService.getAll(), LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error retrieving body parts", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/body-parts", "Error retrieving body parts: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Body part retrieved successfully", bodyPartService.getById(id), LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/body-parts/" + id, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/body-parts/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving body part", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/body-parts/" + id, "Error retrieving body part: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BodyPart request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(HttpStatus.CREATED.value(), "Body part created successfully", bodyPartService.create(request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/body-parts", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating body part", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/body-parts", "Error creating body part: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody BodyPart request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Body part updated successfully", bodyPartService.update(id, request), LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/body-parts/" + id, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/body-parts/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating body part", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/body-parts/" + id, "Error updating body part: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            bodyPartService.delete(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Body part deleted successfully", null, LocalDateTime.now().toString()));
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/body-parts/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting body part", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/body-parts/" + id, "Error deleting body part: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
