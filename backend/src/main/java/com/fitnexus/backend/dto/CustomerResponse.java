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
    private Long headOfficeId;
    private Long branchId;
    private Long departmentId;
    private Long teamId;
    private Long designationId;
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
    private Long assignedTrainerId;
    private String assignedTrainerName;
    private Long trainerId;
    private Long createdById;
    private String photoPath;
    private String idProofPath;
    private Long assignedDietPlanId;
    private String assignedDietPlanName;
    private Long assignedWorkoutPlanId;
    private String assignedWorkoutPlanName;
    private Double bodyFat;
    private Boolean isFrozen;
    private String referredBy;
    private java.time.LocalDate membershipExpiry;
    private String membershipPlan;
    private Long membershipPlanId;
    private String accessStartTime;
    private String accessEndTime;
}
