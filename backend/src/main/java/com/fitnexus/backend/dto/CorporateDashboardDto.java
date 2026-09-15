package com.fitnexus.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CorporateDashboardDto {
    private Long totalEmployees;
    private Long activeParticipants;
    private Long wellnessScore;
    private String topDepartment;
    private List<Integer> wellnessScoreTrend;
}
