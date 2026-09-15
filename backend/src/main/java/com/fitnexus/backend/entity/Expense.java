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
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // EQUIPMENT, UTILITIES, MAINTENANCE, RENT, SALARY, MARKETING, OTHER

    @Column(nullable = false)
    private Double amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // CASH, CHEQUE, BANK_TRANSFER, CREDIT_CARD

    @Column(name = "vendor_name", length = 100)
    private String vendorName;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // Invoice number, receipt number, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users createdBy;

    @Column(nullable = false, length = 20)
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED

    @Column(length = 500)
    private String notes;

    @Column(name = "receipt_file_path", length = 255)
    private String receiptFilePath; // Path to uploaded receipt image/document

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
