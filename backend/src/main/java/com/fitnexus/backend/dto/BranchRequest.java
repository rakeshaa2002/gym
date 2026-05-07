package com.fitnexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    @NotBlank(message = "Branch name is required")
    private String name;

    @NotNull(message = "Head office ID is required")
    private Long headOfficeId;

    private String location;
    private String address;
    private String phone;
    private String email;
    private String managerName;

    private String status = "ACTIVE";
}
