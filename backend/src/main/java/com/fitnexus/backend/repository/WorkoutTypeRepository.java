package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.WorkoutType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {
    List<WorkoutType> findAllByOrderByUpdatedAtDescIdDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
