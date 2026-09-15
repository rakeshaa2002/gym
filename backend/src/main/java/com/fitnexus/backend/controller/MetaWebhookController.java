package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/webhooks/meta")
@RequiredArgsConstructor
@Slf4j
public class MetaWebhookController {

    private final LeadService leadService;

    // Ideally loaded from application.properties
    @Value("${meta.webhook.verify_token:fitnexus_meta_secret_2026}")
    private String verifyToken;

    /**
     * Step 1: Verification
     * When configuring the webhook in the Meta App Dashboard, Meta will send a GET request
     * with these parameters to verify that this endpoint is authentic.
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        log.info("Received Meta Webhook verification request");
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verified successfully!");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed. Token mismatch.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    /**
     * Step 2: Data Reception
     * Meta sends a POST request whenever a user submits a Lead Generation form on Facebook or Instagram.
     */
    @PostMapping
    public ResponseEntity<?> receiveLeadData(@RequestBody Map<String, Object> payload) {
        log.info("Received Meta Webhook payload: {}", payload);
        
        try {
            // In a real production scenario, the payload only contains a 'leadgen_id'.
            // We would need to make a REST call to the Meta Graph API using that ID to fetch the actual Name/Email/Phone.
            // Example:
            // String leadgenId = extractLeadgenId(payload);
            // MetaLeadData data = metaGraphApiService.fetchLeadDetails(leadgenId);

            // For the scope of this implementation, we simulate processing the retrieved data:
            Random random = new Random();
            boolean isInstagram = random.nextBoolean(); // Simulate checking source ad platform

            Lead newLead = new Lead();
            newLead.setName("Meta Lead User");
            newLead.setPhone("+1555" + (100000 + random.nextInt(900000))); // Mock random phone
            newLead.setEmail("meta.lead@example.com");
            newLead.setSource(isInstagram ? "INSTAGRAM" : "FACEBOOK");
            newLead.setStatus("NEW");
            newLead.setNotes("Automatically captured from Meta Lead Ads");
            
            // AI mock metrics
            newLead.setLeadScore(85);
            newLead.setConversionProbability(75);

            leadService.createLead(newLead);
            log.info("Successfully processed and saved Meta lead from {}", newLead.getSource());

            // Acknowledge receipt to Meta immediately (200 OK) so they don't retry.
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            log.error("Failed to process Meta webhook", e);
            // It's recommended to return 200 OK even on error to stop Meta from spamming retries, 
            // but we log it internally.
            return ResponseEntity.ok("EVENT_RECEIVED_WITH_ERROR");
        }
    }
}
