package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.InitiateCallRequest;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.CallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallService callService;

    private ResponseEntity<ApiSuccessResponse<?>> ok(String message, Object data) {
        return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), message, data, LocalDateTime.now().toString()));
    }

    private ResponseEntity<ApiErrorResponse> err(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }

    /** Expose STUN/TURN server configuration for WebRTC peer connections. */
    @GetMapping("/ice-servers")
    public ResponseEntity<?> getIceServers() {
        try {
            return ok("ICE servers retrieved", callService.getIceServers());
        } catch (Exception e) {
            log.error("Error fetching ICE servers", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/ice-servers", "Could not fetch ICE servers");
        }
    }

    /** Member initiates a call to their personal trainer. */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateCall(@RequestBody InitiateCallRequest request) {
        try {
            String callType = request == null ? "AUDIO" : request.getCallType();
            return ok("Call initiated", callService.initiateCall(callType));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/initiate", e.getMessage());
        } catch (InvalidOperationException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/initiate", e.getMessage());
        } catch (IllegalArgumentException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/initiate", e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating call", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/initiate", "Could not initiate call");
        }
    }

    /** Trainer initiates a call to a specific member. */
    @PostMapping("/initiate/{memberId}")
    public ResponseEntity<?> initiateCallToMember(@PathVariable Long memberId, @RequestBody InitiateCallRequest request) {
        try {
            String callType = request == null ? "AUDIO" : request.getCallType();
            return ok("Call initiated", callService.initiateCallAsTrainer(memberId, callType));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/initiate/" + memberId, e.getMessage());
        } catch (InvalidOperationException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/initiate/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating call to member", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/initiate/" + memberId, "Could not initiate call");
        }
    }

    /** Accept an incoming call. */
    @PostMapping("/{callId}/accept")
    public ResponseEntity<?> acceptCall(@PathVariable Long callId) {
        try {
            return ok("Call accepted", callService.acceptCall(callId));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/" + callId + "/accept", e.getMessage());
        } catch (InvalidOperationException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/" + callId + "/accept", e.getMessage());
        } catch (Exception e) {
            log.error("Error accepting call", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/" + callId + "/accept", "Could not accept call");
        }
    }

    /** Reject an incoming call. */
    @PostMapping("/{callId}/reject")
    public ResponseEntity<?> rejectCall(@PathVariable Long callId) {
        try {
            return ok("Call rejected", callService.rejectCall(callId));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/" + callId + "/reject", e.getMessage());
        } catch (InvalidOperationException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/" + callId + "/reject", e.getMessage());
        } catch (Exception e) {
            log.error("Error rejecting call", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/" + callId + "/reject", "Could not reject call");
        }
    }

    /** End an active call. */
    @PostMapping("/{callId}/end")
    public ResponseEntity<?> endCall(@PathVariable Long callId) {
        try {
            return ok("Call ended", callService.endCall(callId));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/" + callId + "/end", e.getMessage());
        } catch (InvalidOperationException e) {
            return err(HttpStatus.BAD_REQUEST, "/api/calls/" + callId + "/end", e.getMessage());
        } catch (Exception e) {
            log.error("Error ending call", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/" + callId + "/end", "Could not end call");
        }
    }

    /** Poll for pending incoming calls. */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCalls() {
        try {
            return ok("Pending calls retrieved", callService.getPendingCalls());
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/pending", e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching pending calls", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/pending", "Could not fetch pending calls");
        }
    }

    /** Get call history with a partner. */
    @GetMapping("/history/{partnerId}")
    public ResponseEntity<?> getCallHistory(@PathVariable Long partnerId) {
        try {
            return ok("Call history retrieved", callService.getCallHistory(partnerId));
        } catch (SecurityException e) {
            return err(HttpStatus.FORBIDDEN, "/api/calls/history/" + partnerId, e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching call history", e);
            return err(HttpStatus.INTERNAL_SERVER_ERROR, "/api/calls/history/" + partnerId, "Could not fetch call history");
        }
    }
}
