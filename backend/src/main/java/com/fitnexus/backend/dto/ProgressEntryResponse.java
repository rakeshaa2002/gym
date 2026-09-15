package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProgressEntryResponse {
    private Long id;
    private LocalDate entryDate;
    private Double weightKg;
    private Integer heartRateBpm;
    private Integer healthScore;
    private Integer workoutMinutes;
    private Integer workoutsCompleted;
    private Integer caloriesBurned;
    private Integer steps;
    private Double waterLiters;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
