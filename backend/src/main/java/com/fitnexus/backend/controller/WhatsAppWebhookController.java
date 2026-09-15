package com.fitnexus.backend.controller;

import com.fitnexus.backend.entity.Lead;
import com.fitnexus.backend.service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final LeadService leadService;

    @Value("${whatsapp.webhook.verify_token:fitnexus_wa_secret_2026}")
    private String verifyToken;

    /**
     * Webhook Verification for WhatsApp Business API
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        log.info("Received WhatsApp Webhook verification request");
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WhatsApp Webhook verified successfully!");
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }

    /**
     * Handle incoming WhatsApp messages
     */
    @PostMapping
    public ResponseEntity<?> receiveWhatsAppMessage(@RequestBody Map<String, Object> payload) {
        log.info("Received WhatsApp Webhook payload: {}", payload);

        try {
            // In a real application, we would parse the JSON payload to extract:
            // - Sender's Phone Number
            // - Sender's Profile Name
            // - The actual message text

            // Example Parsing logic:
            // String phoneNumber = extractPhoneNumber(payload);
            // String profileName = extractProfileName(payload);
            
            // Mocking the extraction process
            Random random = new Random();
            String phoneNumber = "+1234" + (100000 + random.nextInt(900000));
            String profileName = "WhatsApp User " + random.nextInt(100);

            // We must check if this phone number already exists in our system.
            // If it does, we don't create a new lead (it might be an existing member or lead).
            // For this mock, we assume it's a new lead every time.

            Lead newLead = new Lead();
            newLead.setName(profileName);
            newLead.setPhone(phoneNumber);
            newLead.setSource("WHATSAPP");
            newLead.setStatus("NEW");
            newLead.setNotes("Initiated contact via WhatsApp. Need to reply to their message.");

            // AI mock metrics
            newLead.setLeadScore(90); // High intent because they messaged us directly
            newLead.setConversionProbability(80);

            leadService.createLead(newLead);
            log.info("Successfully captured new WhatsApp lead: {}", profileName);

            // Return 200 OK to WhatsApp so they know we received the message
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            log.error("Failed to process WhatsApp webhook", e);
            return ResponseEntity.ok("EVENT_RECEIVED_WITH_ERROR");
        }
    }
}
