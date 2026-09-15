package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.AssignMembershipRequest;
import com.fitnexus.backend.dto.CreateOrderRequest;
import com.fitnexus.backend.dto.PlanChangeRequest;
import com.fitnexus.backend.dto.UpgradeMembershipRequest;
import com.fitnexus.backend.dto.VerifyPaymentRequest;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.MembershipServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/membership")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class MembershipController {
    private final MembershipServiceImplementation membershipServiceImplementation;

    @GetMapping("/me")
    public ResponseEntity<?> myMembership() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership retrieved", membershipServiceImplementation.getMyMembership(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/me", e.getMessage());
        } catch (Exception e) {
            log.error("Membership fetch error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/me", "Error retrieving membership: " + e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> memberMembership(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership retrieved", membershipServiceImplementation.getMemberMembership(memberId), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/member/" + memberId, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/membership/member/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Member membership fetch error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/member/" + memberId, "Error retrieving membership: " + e.getMessage());
        }
    }

    @PostMapping("/assign/{memberId}")
    public ResponseEntity<?> assign(@PathVariable Long memberId, @RequestBody AssignMembershipRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership assigned", membershipServiceImplementation.assignMembership(memberId, request), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/assign/" + memberId, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/membership/assign/" + memberId, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/membership/assign/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Assign membership error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/assign/" + memberId, "Error assigning membership: " + e.getMessage());
        }
    }

    @PostMapping("/request-change")
    public ResponseEntity<?> requestChange(@RequestBody PlanChangeRequest request) {
        try {
            membershipServiceImplementation.requestPlanChange(
                    request == null ? null : request.getPlanId(),
                    request == null ? null : request.getNote());
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(),
                    "Your upgrade request has been sent to the gym staff.", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/request-change", e.getMessage());
        } catch (Exception e) {
            log.error("Plan change request error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/request-change", "Error sending request: " + e.getMessage());
        }
    }

    /** Staff: list members' pending plan-upgrade requests. */
    @GetMapping("/requests")
    public ResponseEntity<?> pendingRequests() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Requests retrieved",
                    membershipServiceImplementation.getPendingRequests(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/requests", e.getMessage());
        } catch (Exception e) {
            log.error("Pending requests error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/requests", "Error retrieving requests: " + e.getMessage());
        }
    }

    /** Staff: dismiss a pending request without assigning a plan. */
    @PostMapping("/requests/{requestId}/dismiss")
    public ResponseEntity<?> dismissRequest(@PathVariable Long requestId) {
        try {
            membershipServiceImplementation.dismissRequest(requestId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Request dismissed", null, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/requests/" + requestId + "/dismiss", e.getMessage());
        } catch (Exception e) {
            log.error("Dismiss request error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/requests/" + requestId + "/dismiss", "Error dismissing request: " + e.getMessage());
        }
    }

    @PostMapping("/upgrade")
    public ResponseEntity<?> upgrade(@RequestBody(required = false) UpgradeMembershipRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Premium membership activated", membershipServiceImplementation.upgrade(request), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/upgrade", e.getMessage());
        } catch (Exception e) {
            log.error("Membership upgrade error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/upgrade", "Error upgrading membership: " + e.getMessage());
        }
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody(required = false) CreateOrderRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Order created", membershipServiceImplementation.createOrder(request), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/create-order", e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "/api/membership/create-order", e.getMessage());
        } catch (Exception e) {
            log.error("Create order error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/create-order", "Error creating order: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Payment verified — Premium activated", membershipServiceImplementation.verifyAndActivate(request), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/membership/verify-payment", e.getMessage());
        } catch (Exception e) {
            log.error("Verify payment error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/verify-payment", "Error verifying payment: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Membership cancelled", membershipServiceImplementation.cancel(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/membership/cancel", e.getMessage());
        } catch (Exception e) {
            log.error("Membership cancel error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/membership/cancel", "Error cancelling membership: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
