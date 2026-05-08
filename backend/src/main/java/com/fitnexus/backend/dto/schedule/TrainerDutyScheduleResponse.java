package com.fitnexus.backend.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDutyScheduleResponse {
    private Long id;
    private ScheduleUserSummary trainer;
    private ScheduleUserSummary assignedBy;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
