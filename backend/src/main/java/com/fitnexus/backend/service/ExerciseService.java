package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.Exercise;

import java.util.List;

public interface ExerciseService {
    List<Exercise> getAll();

    Exercise getById(Long id);

    Exercise create(Exercise request);

    Exercise update(Long id, Exercise request);

    void delete(Long id);
}
