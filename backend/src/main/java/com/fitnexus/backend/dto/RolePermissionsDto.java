package com.fitnexus.backend.dto;

import com.fitnexus.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionsDto {
    private Role role;
    private List<PagePermissionDto> permissions;
}
