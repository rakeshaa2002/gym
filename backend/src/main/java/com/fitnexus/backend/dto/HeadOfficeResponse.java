package com.fitnexus.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeadOfficeResponse {
    private Long id;
    private String name;
    private String location;
    private String address;
    private String phone;
    private String email;
    private String status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}