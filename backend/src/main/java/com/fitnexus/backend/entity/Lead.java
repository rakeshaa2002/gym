package com.fitnexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 30)
    private String status = "NEW"; // NEW, CONTACTED, INTERESTED, TRIAL_BOOKED, TRIAL_COMPLETED, NEGOTIATION, WON, LOST, ARCHIVED

    @Column(length = 30)
    private String source = "WALK_IN"; // WALK_IN, FACEBOOK, INSTAGRAM, GOOGLE_ADS, WEBSITE, WHATSAPP, REFERRAL, CORPORATE, EVENTS


    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Double expectedRevenue;

    private Integer conversionProbability;

    private LocalDateTime trialDate;

    private LocalDateTime nextFollowUp;

    @Column(length = 50)
    private String preferredTime;

    @Column(length = 100)
    private String fitnessGoal;

    @Column(length = 100)
    private String interestedPackage;

    private Integer leadScore; // AI feature

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Users assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
