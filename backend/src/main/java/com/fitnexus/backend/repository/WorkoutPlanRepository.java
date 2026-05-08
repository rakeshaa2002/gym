package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    @Query("""
            select distinct wp
            from WorkoutPlan wp
            left join fetch wp.exercises e
            left join fetch e.workoutType
            left join fetch e.bodyPart
            order by wp.updatedAt desc, wp.id desc
            """)
    List<WorkoutPlan> findAllByOrderByUpdatedAtDescIdDesc();

    @Query("""
            select distinct wp
            from WorkoutPlan wp
            left join fetch wp.exercises e
            left join fetch e.workoutType
            left join fetch e.bodyPart
            where wp.id = :id
            """)
    java.util.Optional<WorkoutPlan> findById(@Param("id") Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
