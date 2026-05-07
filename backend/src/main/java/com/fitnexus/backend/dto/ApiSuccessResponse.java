package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccessResponse<T> {
    private int status;
    private String message;
    private T data;
    private String timestamp;
}
