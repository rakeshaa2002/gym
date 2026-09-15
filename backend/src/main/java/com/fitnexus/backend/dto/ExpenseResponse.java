package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private String category;
    private Double amount;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String vendorName;
    private String referenceNumber;
    private Long createdById;
    private String createdByName;
    private String status;
    private String notes;
    private String receiptFilePath;
}
