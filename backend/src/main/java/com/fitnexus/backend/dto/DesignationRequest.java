package com.fitnexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesignationRequest {
    @NotBlank(message = "Designation name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private String description;
    private String level; // e.g., JUNIOR, SENIOR, LEAD, MANAGER, HEAD
    private Integer salary = 0;

    private String status = "ACTIVE";
}
