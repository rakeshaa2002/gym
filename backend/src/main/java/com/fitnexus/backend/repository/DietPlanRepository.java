package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findAllByOrderByUpdatedAtDescIdDesc();

    boolean existsByNameIgnoreCase(String name);

    long deleteByNameIgnoreCase(String name);
}
