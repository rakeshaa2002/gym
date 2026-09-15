package com.fitnexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * An admin-configurable subscription tier. A plan decides how long the membership
 * lasts and — crucially for attendance — whether the member may check in at any
 * time ({@code unlimitedAccess}) or is restricted to the daily time window that
 * staff assign to them individually on their profile.
 */
@Entity
@Table(name = "membership_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable machine code, e.g. BASIC / STANDARD / PREMIUM (kept on the member too). */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 300)
    private String description;

    /** Price in rupees for one billing period. */
    @Column(nullable = false)
    private Integer price = 0;

    /** Membership length in days; 0 means no expiry (lifetime). */
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 30;

    /** Max minutes a member may stay per visit; 0/null = unlimited (no overstay check). */
    @Column(name = "max_session_minutes")
    private Integer maxSessionMinutes = 0;

    /** When true the member can check in any time; the assigned window is ignored. */
    @Column(name = "unlimited_access", nullable = false)
    private Boolean unlimitedAccess = false;

    /** Grants the member wellness chat with their personal trainer. Nullable so adding
     *  the column to an existing table won't fail; read defensively as false. */
    @Column(name = "trainer_chat")
    private Boolean trainerChat = false;

    /** Newline-separated feature bullets shown on the plan card. */
    @Column(length = 1000)
    private String features;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
