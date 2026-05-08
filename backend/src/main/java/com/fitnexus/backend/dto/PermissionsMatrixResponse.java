package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsMatrixResponse {
    private List<PermissionPageDto> pages;
    private List<RolePermissionsDto> roles;
}
