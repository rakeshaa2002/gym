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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * A member's request to be moved to a different plan (admin-assisted upgrade).
 * Staff see PENDING requests and resolve them by assigning the plan.
 */
@Entity
@Table(name = "membership_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_plan_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MembershipPlan requestedPlan;

    @Column(length = 500)
    private String note;

    /** PENDING until staff assign a plan (or dismiss it), then RESOLVED. */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
