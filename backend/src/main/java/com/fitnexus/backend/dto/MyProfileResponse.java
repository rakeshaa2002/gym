package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MyProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String phone;
    private String address;
    private String city;
    private String bio;
    private String fitnessGoals;
    private boolean hasFitnessProfile;
}
