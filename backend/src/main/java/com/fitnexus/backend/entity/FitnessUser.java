package com.fitnexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FitnessUser {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users account;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "blood_group", length = 3)
    private String bloodGroup;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "medical_conditions", length = 500)
    private String medicalConditions;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;

    /** Member profile photo (uploaded file path) — collected at registration. */
    @Column(name = "photo_path", length = 500)
    private String photoPath;

    /** Member ID proof document (e.g. Aadhaar) — uploaded file path. */
    @Column(name = "id_proof_path", length = 500)
    private String idProofPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_trainer_id")
    private Trainer assignedTrainer;

    @Column(name = "details_completed", nullable = false)
    private Boolean detailsCompleted = false;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "fitness_goals", length = 300)
    private String fitnessGoals;

    @Column(name = "dietary_preferences", length = 300)
    private String dietaryPreferences;

    @Column(name = "injuries_or_limitations", length = 500)
    private String injuriesOrLimitations;

    @Column(name = "body_fat")
    private Double bodyFat;

    @Column(name = "is_frozen", nullable = false, columnDefinition = "boolean default false")
    private Boolean isFrozen = false;

    @Column(name = "referred_by", length = 100)
    private String referredBy;

    @Column(name = "membership_plan", length = 20)
    private String membershipPlan = "BASIC";

    @Column(name = "membership_expiry")
    private java.time.LocalDate membershipExpiry;

    /** The configurable plan tier this member is on (source of truth for unlimited access). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id")
    private MembershipPlan membershipPlanRef;

    /** Daily check-in window assigned to this member (ignored when the plan is unlimited). */
    @Column(name = "access_start_time")
    private java.time.LocalTime accessStartTime;

    @Column(name = "access_end_time")
    private java.time.LocalTime accessEndTime;

    /** Last time we alerted staff that this member has no slot assigned (throttles the alert). */
    @Column(name = "last_slot_request_at")
    private LocalDateTime lastSlotRequestAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_corporate_hr_id")
    private Users assignedCorporateHr;

    @Column(name = "corporate_department", length = 100)
    private String corporateDepartment;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }
}

