package com.fitnexus.backend.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUserSummary {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Long branchId;
}
