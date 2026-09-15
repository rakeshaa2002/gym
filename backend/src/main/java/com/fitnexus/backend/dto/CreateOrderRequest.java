package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long planId;    // which plan to purchase
    private Integer months; // billing period in months: 1 (monthly), 3 (quarterly), 12 (yearly)
}
