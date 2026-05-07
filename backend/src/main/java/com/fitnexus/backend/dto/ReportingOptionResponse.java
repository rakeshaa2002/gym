package com.fitnexus.backend.dto;

import com.fitnexus.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportingOptionResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long branchId;
}
