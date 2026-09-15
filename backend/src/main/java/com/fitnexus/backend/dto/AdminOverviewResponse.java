package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {
    // Member & staff counts
    private long totalMembers;
    private long activeMembers;
    private long inactiveMembers;
    private long newMembersThisMonth;
    private long totalStaff;

    // Attendance (today)
    private long checkedInToday;
    private long currentlyInGym;
    private List<DayCount> attendanceTrend; // last 7 days

    // Catalog counts
    private long workoutPlans;
    private long exercises;
    private long dietPlans;

    // Revenue metrics
    private double revenueToday;
    private double revenueThisMonth;
    private double pendingCollections;
    private double renewalRevenueDue;
    private double ptRevenue;
    private double supplementRevenue;

    // Recent members
    private List<RecentMember> recentMembers;

    @Data
    @AllArgsConstructor
    public static class DayCount {
        private String label;   // Mon, Tue, ...
        private long count;
    }

    @Data
    @AllArgsConstructor
    public static class RecentMember {
        private Long id;
        private String name;
        private String email;
        private Boolean active;
        private LocalDateTime joinedAt;
    }
}
