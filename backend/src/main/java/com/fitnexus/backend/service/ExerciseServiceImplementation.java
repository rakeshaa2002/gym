package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.BodyPart;
import com.fitnexus.backend.entity.Exercise;
import com.fitnexus.backend.entity.WorkoutType;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.BodyPartRepository;
import com.fitnexus.backend.repository.ExerciseRepository;
import com.fitnexus.backend.repository.WorkoutTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseServiceImplementation implements ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final BodyPartRepository bodyPartRepository;

    @Override
    public List<Exercise> getAll() {
        return exerciseRepository.findAllByOrderByUpdatedAtDescIdDesc();
    }

    @Override
    public Exercise getById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Exercise not found"));
    }

    @Override
    public Exercise create(Exercise request) {
        Exercise entity = new Exercise();
        copyFields(request, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return exerciseRepository.save(entity);
    }

    @Override
    public Exercise update(Long id, Exercise request) {
        Exercise entity = getById(id);
        copyFields(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        return exerciseRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new InvalidOperationException("Exercise not found");
        }
        exerciseRepository.deleteById(id);
    }

    private void copyFields(Exercise source, Exercise target) {
        if (source == null || source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException("Exercise name is required");
        }

        String name = source.getName().trim();
        Long targetId = target.getId();
        if (targetId == null ? exerciseRepository.existsByNameIgnoreCase(name) : exerciseRepository.existsByNameIgnoreCaseAndIdNot(name, targetId)) {
            throw new IllegalArgumentException("Exercise name already exists");
        }

        WorkoutType workoutType = resolveWorkoutType(source.getWorkoutType());
        BodyPart bodyPart = resolveBodyPart(source.getBodyPart());

        target.setName(name);
        target.setDescription(source.getDescription());
        target.setWorkoutType(workoutType);
        target.setBodyPart(bodyPart);
        target.setDifficulty(normalizeStatus(source.getDifficulty(), "Medium"));
        target.setEquipment(source.getEquipment());
        target.setImage(source.getImage());
        target.setVideoUrl(source.getVideoUrl());
        target.setCaloriesBurned(numberOrDefault(source.getCaloriesBurned(), 0));
        target.setSets(numberOrDefault(source.getSets(), 0));
        target.setReps(numberOrDefault(source.getReps(), 0));
        target.setDurationMinutes(numberOrDefault(source.getDurationMinutes(), 0));
        target.setInstructions(source.getInstructions() == null ? new ArrayList<>() : new ArrayList<>(source.getInstructions()));
        target.setStatus(normalizeStatus(source.getStatus(), "ACTIVE"));
    }

    private WorkoutType resolveWorkoutType(WorkoutType workoutTypeRef) {
        if (workoutTypeRef == null || workoutTypeRef.getId() == null) {
            throw new IllegalArgumentException("Workout type is required");
        }
        return workoutTypeRepository.findById(workoutTypeRef.getId())
                .orElseThrow(() -> new InvalidOperationException("Workout type not found"));
    }

    private BodyPart resolveBodyPart(BodyPart bodyPartRef) {
        if (bodyPartRef == null || bodyPartRef.getId() == null) {
            throw new IllegalArgumentException("Body part is required");
        }
        return bodyPartRepository.findById(bodyPartRef.getId())
                .orElseThrow(() -> new InvalidOperationException("Body part not found"));
    }

    private String normalizeStatus(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int numberOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
