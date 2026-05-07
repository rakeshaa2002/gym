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

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }
}

