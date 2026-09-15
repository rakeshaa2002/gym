package com.fitnexus.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MyProfileRequest {
    private String name;
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
}
