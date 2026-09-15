package com.fitnexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trainer_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Trainer trainer;

    @Column(name = "payment_period_start", nullable = false)
    private LocalDate paymentPeriodStart;

    @Column(name = "payment_period_end", nullable = false)
    private LocalDate paymentPeriodEnd;

    @Column(name = "base_salary", nullable = false)
    private Double baseSalary;

    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount = 0.0;

    @Column(name = "bonus_amount", nullable = false)
    private Double bonusAmount = 0.0;

    @Column(name = "deduction_amount", nullable = false)
    private Double deductionAmount = 0.0;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // BANK_TRANSFER, CASH, RAZORPAY, PHONEPE, UPI

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, PROCESSING, PAID, FAILED

    @Column(name = "assigned_members_count", nullable = false)
    private Integer assignedMembersCount = 0;

    @Column(name = "sessions_conducted", nullable = false)
    private Integer sessionsConducted = 0;

    @Column(name = "attendance_percentage", nullable = false)
    private Double attendancePercentage = 0.0;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
