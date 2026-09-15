package com.fitnexus.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressEntryRequest {
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
}
