package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Double weight;
    private Double height;
    private String bloodGroup;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    private String city;
    private String medicalConditions;
    private String emergencyContact;
    private String emergencyPhone;
    private Boolean isActive;
    private Boolean isApproved;
    private String assignedTrainerName;
}