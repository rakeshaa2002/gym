package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/public/leads")
@CrossOrigin(origins = "*") // Allow requests from any public website
@RequiredArgsConstructor
@Slf4j
public class PublicLeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<?> captureWebsiteLead(@RequestBody Lead leadRequest) {
        log.info("Received public website lead: {}", leadRequest.getName());
        try {
            // Force the source to WEBSITE
            leadRequest.setSource("WEBSITE");
            // Set default status
            leadRequest.setStatus("NEW");

            // Normally we'd calculate this via AI, but for now we set default scores
            leadRequest.setLeadScore(70);
            leadRequest.setConversionProbability(45);

            Lead createdLead = leadService.createLead(leadRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(),
                    "Lead captured successfully via Website",
                    createdLead,
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to capture website lead", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An error occurred while processing your request.",
                    LocalDateTime.now().toString(),
                    "/api/public/leads"
            ));
        }
    }
}
