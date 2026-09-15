package com.fitnexus.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CorporateBmiDataDto {
    private CurrentBmi current;
    private List<BmiHistoryEntry> history;
    private List<BmiDistribution> distribution;
    
    @Data
    public static class CurrentBmi {
        private Double bmi;
        private String category;
        private Double height;
        private Double weight;
        private String lastUpdated;
    }
    
    @Data
    public static class BmiHistoryEntry {
        private String date;
        private Double bmi;
        private Double weight;
        private String category;
    }
    
    @Data
    public static class BmiDistribution {
        private String category;
        private Integer count;
        private Double pct;
    }
}
