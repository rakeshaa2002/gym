package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSummaryResponse {
    private Integer workoutMinutesThisWeek;
    private Integer workoutsThisWeek;
    private Integer caloriesThisWeek;
    private Double latestWeight;
    private Double weightGoal;
    private Integer weightProgressPercent;
    private Integer latestHeartRate;
    private Integer latestHealthScore;
    private List<WeightPoint> weightSeries;
    private List<GoalResponse> goals;

    // Dashboard "Overview" cards
    private Integer latestSteps;
    private Integer stepsGoal;
    private Double latestWaterLiters;
    private Double waterGoalLiters;
    private Integer caloriesToday;
    private Integer caloriesGoal;
    private List<DayActivity> weeklyActivity;

    @Data
    @AllArgsConstructor
    public static class WeightPoint {
        private LocalDate date;
        private Double weight;
    }

    @Data
    @AllArgsConstructor
    public static class DayActivity {
        private String label;   // Mon, Tue, ...
        private Integer minutes;
    }
}
