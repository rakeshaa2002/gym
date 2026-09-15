package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.LeadConvertRequest;
import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class LeadController {
    private final LeadService leadService;

    @GetMapping
    public ResponseEntity<?> getAllLeads() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Leads retrieved successfully", 
                    leadService.getAllLeads(), 
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving leads", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads", e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Lead retrieved successfully", 
                    leadService.getLeadById(id), 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/leads/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving lead: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + id, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createLead(@RequestBody Lead lead) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), 
                    "Lead created successfully", 
                    leadService.createLead(lead), 
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error creating lead", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @RequestBody Lead leadDetails) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Lead updated successfully", 
                    leadService.updateLead(id, leadDetails), 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/leads/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating lead: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + id, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        try {
            leadService.deleteLead(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Lead deleted successfully", 
                    null, 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/leads/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting lead: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + id, e.getMessage());
        }
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertLeadToMember(@PathVariable Long id, @RequestBody LeadConvertRequest request) {
        try {
            Users member = leadService.convertLeadToMember(id, request);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Lead successfully converted to member", 
                    member.getId(), 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/leads/" + id + "/convert", e.getMessage());
        } catch (Exception e) {
            log.error("Error converting lead to member: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + id + "/convert", e.getMessage());
        }
    }

    @PostMapping("/{id}/ai/generate-reply")
    public ResponseEntity<?> generateAiReply(@PathVariable Long id) {
        try {
            String draft = leadService.generateAiReply(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "AI reply generated", 
                    draft, 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/leads/" + id + "/ai/generate-reply", e.getMessage());
        } catch (Exception e) {
            log.error("Error generating AI reply for lead: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/leads/" + id + "/ai/generate-reply", e.getMessage());
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
