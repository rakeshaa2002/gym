package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.entity.Inventory;
import com.fitnexus.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> getAllInventory() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Inventory retrieved successfully", 
                    inventoryService.getAllInventory(), 
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error retrieving inventory", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/inventory", e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Inventory item retrieved successfully", 
                    inventoryService.getInventoryById(id), 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/inventory/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving inventory item: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/inventory/" + id, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createInventory(@RequestBody Inventory item) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(
                    HttpStatus.CREATED.value(), 
                    "Inventory item created successfully", 
                    inventoryService.createInventory(item), 
                    LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Error creating inventory item", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/inventory", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @RequestBody Inventory itemDetails) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Inventory item updated successfully", 
                    inventoryService.updateInventory(id, itemDetails), 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/inventory/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating inventory item: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/inventory/" + id, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(
                    HttpStatus.OK.value(), 
                    "Inventory item deleted successfully", 
                    null, 
                    LocalDateTime.now().toString()
            ));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.NOT_FOUND, "/api/inventory/" + id, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting inventory item: {}", id, e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/inventory/" + id, e.getMessage());
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
