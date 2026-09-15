package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String keyId;
    private long amount;      // in paise
    private String currency;
    private Integer months;
    private String name;
    private String description;
    private long originalAmount; // in paise, before any upgrade credit
    private long creditApplied;  // in paise, unused value of the current plan credited toward this upgrade
}
