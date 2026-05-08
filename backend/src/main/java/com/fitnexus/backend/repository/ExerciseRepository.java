package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @EntityGraph(attributePaths = {"workoutType", "bodyPart", "instructions"})
    List<Exercise> findAllByOrderByUpdatedAtDescIdDesc();

    @EntityGraph(attributePaths = {"workoutType", "bodyPart", "instructions"})
    java.util.Optional<Exercise> findById(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
