package com.fitnexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private String description;
    private String status = "ACTIVE";
}
