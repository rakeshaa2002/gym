package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class CorporateEmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String status;
    private String lastVisit;
}
