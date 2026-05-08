package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.WorkoutType;

import java.util.List;

public interface WorkoutTypeService {
    List<WorkoutType> getAll();

    WorkoutType getById(Long id);

    WorkoutType create(WorkoutType request);

    WorkoutType update(Long id, WorkoutType request);

    void delete(Long id);
}
