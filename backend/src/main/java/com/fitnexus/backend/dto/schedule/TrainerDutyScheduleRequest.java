package com.fitnexus.backend.dto.schedule;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainerDutyScheduleRequest {
    private Long trainerId;
    private String branch;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String repeatType;
    private String shiftType;
    private String location;
    private String status;
    private String notes;
}
