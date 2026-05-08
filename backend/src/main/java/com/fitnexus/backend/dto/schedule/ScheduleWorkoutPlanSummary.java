package com.fitnexus.backend.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleWorkoutPlanSummary {
    private Long id;
    private String name;
    private String mainImage;
}
