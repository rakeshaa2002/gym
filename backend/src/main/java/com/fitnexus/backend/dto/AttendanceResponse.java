package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String method;
    private String deviceId;
    private String status;
}
