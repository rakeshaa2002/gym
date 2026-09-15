package com.fitnexus.backend.dto;

import com.fitnexus.backend.entity.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CorporateHrResponse {
    private Long id;
    private String companyName;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
