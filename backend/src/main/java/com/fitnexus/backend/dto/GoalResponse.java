package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String name;
    private String category;
    private Integer sets;
    private Integer reps;
    private Integer rest;
    private Double weight;
    private Integer calories;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    private Integer progressPercent;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
