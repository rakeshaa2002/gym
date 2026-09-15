package com.fitnexus.backend.dto;

import com.fitnexus.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String email;
    private String name;
    private String token;
    private Role role;
}
