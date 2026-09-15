package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.Inventory;
import com.fitnexus.backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAllByOrderByItemNameAsc();
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found with id: " + id));
    }

    public Inventory createInventory(Inventory item) {
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("IN_STOCK");
        }
        item.setLastMaintained(LocalDateTime.now());
        return inventoryRepository.save(item);
    }

    public Inventory updateInventory(Long id, Inventory itemDetails) {
        Inventory item = getInventoryById(id);
        item.setItemName(itemDetails.getItemName());
        item.setCategory(itemDetails.getCategory());
        item.setQuantity(itemDetails.getQuantity());
        item.setStatus(itemDetails.getStatus());
        item.setNotes(itemDetails.getNotes());
        
        if (itemDetails.getLastMaintained() != null) {
            item.setLastMaintained(itemDetails.getLastMaintained());
        } else {
            item.setLastMaintained(LocalDateTime.now());
        }
        
        return inventoryRepository.save(item);
    }

    public void deleteInventory(Long id) {
        Inventory item = getInventoryById(id);
        inventoryRepository.delete(item);
    }
}
