package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.DietPlan;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.DietPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DietPlanService {
    private final DietPlanRepository dietPlanRepository;

    public List<DietPlan> getAll() {
        return dietPlanRepository.findAllByOrderByUpdatedAtDescIdDesc();
    }

    public DietPlan getById(Long id) {
        return dietPlanRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Diet plan not found"));
    }

    public DietPlan create(DietPlan request) {
        DietPlan plan = new DietPlan();
        copyEditableFields(request, plan);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return dietPlanRepository.save(plan);
    }

    public DietPlan update(Long id, DietPlan request) {
        DietPlan plan = dietPlanRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Diet plan not found"));
        copyEditableFields(request, plan);
        plan.setUpdatedAt(LocalDateTime.now());
        return dietPlanRepository.save(plan);
    }

    public void delete(Long id) {
        if (!dietPlanRepository.existsById(id)) {
            throw new InvalidOperationException("Diet plan not found");
        }
        dietPlanRepository.deleteById(id);
    }

    private void copyEditableFields(DietPlan source, DietPlan target) {
        if (source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException("Diet plan name is required");
        }

        target.setName(source.getName().trim());
        target.setDescription(source.getDescription());
        target.setEatTime(source.getEatTime());
        target.setPrepTime(numberOrDefault(source.getPrepTime(), 0));
        target.setCookTime(numberOrDefault(source.getCookTime(), 0));
        target.setDifficulty(source.getDifficulty() == null || source.getDifficulty().isBlank() ? "Medium" : source.getDifficulty());
        target.setTotalSteps(Math.max(numberOrDefault(source.getTotalSteps(), 1), 1));
        target.setHealthScore(clamp(numberOrDefault(source.getHealthScore(), 85), 0, 100));
        target.setCalories(numberOrDefault(source.getCalories(), 0));
        target.setProtein(numberOrDefault(source.getProtein(), 0));
        target.setCarbs(numberOrDefault(source.getCarbs(), 0));
        target.setFats(numberOrDefault(source.getFats(), 0));
        target.setCholesterol(numberOrDefault(source.getCholesterol(), 0));
        target.setSodium(numberOrDefault(source.getSodium(), 0));
        target.setPotassium(numberOrDefault(source.getPotassium(), 0));
        target.setVitaminA(numberOrDefault(source.getVitaminA(), 0));
        target.setVitaminC(numberOrDefault(source.getVitaminC(), 0));
        target.setCalcium(numberOrDefault(source.getCalcium(), 0));
        target.setIron(numberOrDefault(source.getIron(), 0));
        target.setIngredients(source.getIngredients() == null ? new ArrayList<>() : new ArrayList<>(source.getIngredients()));
        target.setDirections(source.getDirections() == null ? new ArrayList<>() : new ArrayList<>(source.getDirections()));
        target.setTools(source.getTools() == null ? new ArrayList<>() : new ArrayList<>(source.getTools()));
        target.setNotes(source.getNotes());
        target.setMainImage(source.getMainImage());
        target.setGalleryImages(source.getGalleryImages() == null ? new ArrayList<>() : new ArrayList<>(source.getGalleryImages()));
        target.setStatus(source.getStatus() == null || source.getStatus().isBlank() ? "ACTIVE" : source.getStatus());
    }

    private int numberOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
