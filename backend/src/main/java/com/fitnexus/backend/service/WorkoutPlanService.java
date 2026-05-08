package com.fitnexus.backend.service;

import com.fitnexus.backend.entity.WorkoutPlan;

import java.util.List;

public interface WorkoutPlanService {
    List<WorkoutPlan> getAll();

    WorkoutPlan getById(Long id);

    WorkoutPlan create(WorkoutPlan request);

    WorkoutPlan update(Long id, WorkoutPlan request);

    void delete(Long id);
}
