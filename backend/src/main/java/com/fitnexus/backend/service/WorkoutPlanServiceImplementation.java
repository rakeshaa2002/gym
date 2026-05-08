package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.Exercise;
import com.fitnexus.backend.entity.WorkoutPlan;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.ExerciseRepository;
import com.fitnexus.backend.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutPlanServiceImplementation implements WorkoutPlanService {
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    public List<WorkoutPlan> getAll() {
        return workoutPlanRepository.findAllByOrderByUpdatedAtDescIdDesc();
    }

    @Override
    public WorkoutPlan getById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Workout plan not found"));
    }

    @Override
    public WorkoutPlan create(WorkoutPlan request) {
        WorkoutPlan entity = new WorkoutPlan();
        copyFields(request, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return workoutPlanRepository.save(entity);
    }

    @Override
    public WorkoutPlan update(Long id, WorkoutPlan request) {
        WorkoutPlan entity = getById(id);
        copyFields(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        return workoutPlanRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        if (!workoutPlanRepository.existsById(id)) {
            throw new InvalidOperationException("Workout plan not found");
        }
        workoutPlanRepository.deleteById(id);
    }

    private void copyFields(WorkoutPlan source, WorkoutPlan target) {
        if (source == null || source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException("Workout plan name is required");
        }

        String name = source.getName().trim();
        Long targetId = target.getId();
        if (targetId == null ? workoutPlanRepository.existsByNameIgnoreCase(name) : workoutPlanRepository.existsByNameIgnoreCaseAndIdNot(name, targetId)) {
            throw new IllegalArgumentException("Workout plan name already exists");
        }

        target.setName(name);
        target.setDescription(source.getDescription());
        target.setGoal(source.getGoal());
        target.setLevel(normalizeStatus(source.getLevel(), "Beginner"));
        target.setDurationWeeks(numberOrDefault(source.getDurationWeeks(), 0));
        target.setDaysPerWeek(numberOrDefault(source.getDaysPerWeek(), 0));
        target.setEstimatedTimeMinutes(numberOrDefault(source.getEstimatedTimeMinutes(), 0));
        target.setStatus(normalizeStatus(source.getStatus(), "ACTIVE"));
        target.setExercises(resolveExercises(source.getExercises()));
        target.setNotes(source.getNotes());
        target.setMainImage(source.getMainImage());
    }

    private List<Exercise> resolveExercises(List<Exercise> sourceExercises) {
        if (sourceExercises == null || sourceExercises.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> ids = sourceExercises.stream()
                .map(Exercise::getId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<Exercise> resolved = exerciseRepository.findAllById(ids);
        if (resolved.size() != ids.size()) {
            throw new InvalidOperationException("One or more exercises were not found");
        }
        return new ArrayList<>(resolved);
    }

    private String normalizeStatus(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int numberOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
