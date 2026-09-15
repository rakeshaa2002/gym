package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private Long id;
    private String receiptNumber;
    private Long memberId;
    private String memberName;
    private Double amount;
    private String paymentMethod;
    private LocalDate receiptDate;
    private String paymentReference;
    private String notes;
}
