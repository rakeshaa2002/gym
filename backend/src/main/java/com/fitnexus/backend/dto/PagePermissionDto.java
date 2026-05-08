package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagePermissionDto {
    private String pageKey;
    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
}
