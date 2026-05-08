package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionPageDto {
    private String pageKey;
    private String label;
    private String routePath;
    private String category;
    private Integer sortOrder;
}
