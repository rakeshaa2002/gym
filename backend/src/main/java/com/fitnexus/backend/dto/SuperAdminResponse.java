package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdminResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;

    /**
     * Get the full name of the SuperAdmin
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get status as string
     */
    public String getStatus() {
        return isActive != null && isActive ? "ACTIVE" : "INACTIVE";
    }
}