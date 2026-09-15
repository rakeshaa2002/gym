package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long memberId;
    private String memberName;
    private Double subtotal;
    private Double taxAmount;
    private Double totalAmount;
    private Double discountAmount;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String paymentStatus;
    private Double paidAmount;
    private String status;
    private String notes;
}
