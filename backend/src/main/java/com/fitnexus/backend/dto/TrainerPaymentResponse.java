package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerPaymentResponse {
    private Long id;
    private Long trainerId;
    private String trainerName;
    private LocalDate paymentPeriodStart;
    private LocalDate paymentPeriodEnd;
    private Double baseSalary;
    private Double commissionAmount;
    private Double bonusAmount;
    private Double deductionAmount;
    private Double totalAmount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String transactionReference;
    private String status;
    private Integer assignedMembersCount;
    private Integer sessionsConducted;
    private Double attendancePercentage;
    private String notes;
}
