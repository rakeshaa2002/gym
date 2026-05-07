package com.fitnexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeadOfficeRequest {
    @NotBlank(message = "Head office name is required")
    private String name;

    private String location;
    private String address;
    private String phone;
    private String email;

    private String status = "ACTIVE";
}
