package com.fitnexus.backend.dto.schedule;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserWorkoutScheduleRequest {
    private Long trainerId;
    private Long userId;
    private Long workoutPlanId;
    private Long workoutTypeId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String repeatType;
    private String location;
    private String completionStatus;
    private String notes;
    private String status;
}
