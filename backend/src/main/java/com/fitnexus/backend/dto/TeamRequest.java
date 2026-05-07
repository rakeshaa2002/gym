package com.fitnexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {
    @NotBlank(message = "Team name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private String description;
    private String teamLead;
    private Integer memberCount = 0;

    private String status = "ACTIVE";
}
