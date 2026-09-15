package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class GoalRequest {
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
    private String status;
    private String notes;
}
