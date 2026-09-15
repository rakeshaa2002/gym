package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerPerformanceResponse {
    private Long trainerId;
    private String name;
    private int assignedMembers;
    private int attendancePercentage;
    private int retentionPercentage;
    private double revenueGenerated;
    private double rating;
    private int transformations;
    private int performanceScore;
}
