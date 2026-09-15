package com.fitnexus.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerByTrainerRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    private String password;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name should only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name should only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "300.0", message = "Weight cannot exceed 300 kg")
    private Double weight;

    @NotNull(message = "Height cannot be null")
    @DecimalMin(value = "100.0", message = "Height must be at least 100 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    private Double height;

    @NotBlank(message = "Blood group cannot be blank")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be valid (A+, A-, B+, B-, AB+, AB-, O+, O-)")
    private String bloodGroup;

    @NotNull(message = "Age cannot be null")
    @Min(value = 13, message = "Age must be at least 13")
    @Max(value = 120, message = "Age cannot exceed 120")
    private Integer age;

    @NotBlank(message = "Gender cannot be blank")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid (10-15 digits, optional + prefix)")
    private String phone;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    @NotBlank(message = "City cannot be blank")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @Size(max = 500, message = "Medical conditions cannot exceed 500 characters")
    private String medicalConditions;

    @NotBlank(message = "Emergency contact cannot be blank")
    @Size(min = 2, max = 100, message = "Emergency contact must be between 2 and 100 characters")
    private String emergencyContact;

    @NotBlank(message = "Emergency phone cannot be blank")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency phone should be valid (10-15 digits, optional + prefix)")
    private String emergencyPhone;

    private Long teamId;
    private Long assignedTrainerId;

    // Photo (required in the UI) and ID proof are uploaded first; the client sends
    // back the stored file paths here.
    private String photoPath;
    private String idProofPath;
    private Double bodyFat;
    private Boolean isFrozen;
    private String referredBy;
}


