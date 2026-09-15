package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.DeviceCheckInRequest;
import com.fitnexus.backend.dto.EnrollFingerprintRequest;
import com.fitnexus.backend.dto.KioskCheckInRequest;
import com.fitnexus.backend.service.AttendanceServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {
    private final AttendanceServiceImplementation attendanceServiceImplementation;

    /** Public webhook for the fingerprint terminal. Guarded by the X-Device-Key header. */
    @PostMapping("/checkin")
    public ResponseEntity<?> deviceCheckIn(@RequestBody DeviceCheckInRequest request,
                                           @RequestHeader(value = "X-Device-Key", required = false) String deviceKey) {
        try {
            var decision = attendanceServiceImplementation.deviceCheckIn(request, deviceKey);
            HttpStatus status = decision.isAccessGranted() ? HttpStatus.OK : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(new ApiSuccessResponse<>(status.value(),
                    decision.getMessage(), decision, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.UNAUTHORIZED, "/api/attendance/checkin", e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/attendance/checkin", e.getMessage());
        } catch (Exception e) {
            log.error("Device check-in error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/checkin", "Error processing check-in: " + e.getMessage());
        }
    }

    @PostMapping("/kiosk")
    public ResponseEntity<?> kioskCheckIn(@RequestBody KioskCheckInRequest request) {
        try {
            var decision = attendanceServiceImplementation.kioskCheckIn(request == null ? null : request.getIdentifier());
            HttpStatus status = decision.isAccessGranted() ? HttpStatus.OK : HttpStatus.OK;
            return ResponseEntity.status(status).body(new ApiSuccessResponse<>(status.value(), decision.getMessage(), decision, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/kiosk", e.getMessage());
        } catch (Exception e) {
            log.error("Kiosk check-in error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/kiosk", "Error processing check-in: " + e.getMessage());
        }
    }

    @PostMapping("/self")
    public ResponseEntity<?> selfCheckIn() {
        try {
            var decision = attendanceServiceImplementation.selfCheckIn();
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), decision.getMessage(), decision, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/self", e.getMessage());
        } catch (Exception e) {
            log.error("Self check-in error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/self", "Error recording attendance: " + e.getMessage());
        }
    }

    @PostMapping("/manual/{memberId}")
    public ResponseEntity<?> manualCheckIn(@PathVariable Long memberId) {
        try {
            var decision = attendanceServiceImplementation.manualCheckIn(memberId);
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), decision.getMessage(), decision, LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/manual/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Manual check-in error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/manual/" + memberId, "Error recording attendance: " + e.getMessage());
        }
    }

    @PutMapping("/enroll/{memberId}")
    public ResponseEntity<?> enroll(@PathVariable Long memberId, @RequestBody EnrollFingerprintRequest request) {
        try {
            attendanceServiceImplementation.enrollFingerprint(memberId, request == null ? null : request.getFingerprintId());
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Fingerprint ID saved", null, LocalDateTime.now().toString()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/attendance/enroll/" + memberId, e.getMessage());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/enroll/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Enroll error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/enroll/" + memberId, "Error enrolling member: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> myAttendance() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Attendance retrieved successfully", attendanceServiceImplementation.getMyAttendance(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/me", e.getMessage());
        } catch (Exception e) {
            log.error("My attendance error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/me", "Error retrieving attendance: " + e.getMessage());
        }
    }

    @GetMapping("/members")
    public ResponseEntity<?> members() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Members retrieved successfully", attendanceServiceImplementation.getEnrollableMembers(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/members", e.getMessage());
        } catch (Exception e) {
            log.error("Attendance members error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/members", "Error retrieving members: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> all(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Attendance retrieved successfully", attendanceServiceImplementation.getAll(date), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance", e.getMessage());
        } catch (Exception e) {
            log.error("Attendance list error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance", "Error retrieving attendance: " + e.getMessage());
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> today() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Today's attendance retrieved successfully", attendanceServiceImplementation.getToday(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/attendance/today", e.getMessage());
        } catch (Exception e) {
            log.error("Today attendance error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/attendance/today", "Error retrieving attendance: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
