package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.WorkoutType;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.WorkoutTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutTypeServiceImplementation implements WorkoutTypeService {
    private final WorkoutTypeRepository workoutTypeRepository;

    @Override
    public List<WorkoutType> getAll() {
        return workoutTypeRepository.findAllByOrderByUpdatedAtDescIdDesc();
    }

    @Override
    public WorkoutType getById(Long id) {
        return workoutTypeRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Workout type not found"));
    }

    @Override
    public WorkoutType create(WorkoutType request) {
        WorkoutType entity = new WorkoutType();
        copyFields(request, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return workoutTypeRepository.save(entity);
    }

    @Override
    public WorkoutType update(Long id, WorkoutType request) {
        WorkoutType entity = getById(id);
        copyFields(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        return workoutTypeRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        if (!workoutTypeRepository.existsById(id)) {
            throw new InvalidOperationException("Workout type not found");
        }
        workoutTypeRepository.deleteById(id);
    }

    private void copyFields(WorkoutType source, WorkoutType target) {
        if (source == null || source.getName() == null || source.getName().isBlank()) {
            throw new IllegalArgumentException("Workout type name is required");
        }
        String name = source.getName().trim();
        Long targetId = target.getId();
        if (targetId == null ? workoutTypeRepository.existsByNameIgnoreCase(name) : workoutTypeRepository.existsByNameIgnoreCaseAndIdNot(name, targetId)) {
            throw new IllegalArgumentException("Workout type name already exists");
        }

        target.setName(name);
        target.setDescription(source.getDescription());
        target.setStatus(normalizeStatus(source.getStatus()));
    }

    private String normalizeStatus(String value) {
        return value == null || value.isBlank() ? "ACTIVE" : value.trim().toUpperCase();
    }
}
