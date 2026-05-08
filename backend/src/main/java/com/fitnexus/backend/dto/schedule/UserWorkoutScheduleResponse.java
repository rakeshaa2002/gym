package com.fitnexus.backend.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkoutScheduleResponse {
    private Long id;
    private ScheduleUserSummary trainer;
    private ScheduleUserSummary user;
    private ScheduleWorkoutPlanSummary workoutPlan;
    private ScheduleWorkoutTypeSummary workoutType;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String repeatType;
    private String location;
    private String completionStatus;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
