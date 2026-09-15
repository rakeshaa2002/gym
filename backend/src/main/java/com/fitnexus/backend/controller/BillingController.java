package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.Transaction;
import com.fitnexus.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    private final TransactionRepository transactionRepository;

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions() {
        try {
            List<Transaction> transactions = transactionRepository.findAllByOrderByTransactionDateDesc();
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Transactions retrieved successfully", 
                    transactions, 
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving transactions", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/billing/transactions", e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(), 
                message, 
                LocalDateTime.now().toString(), 
                path
        ));
    }
}
