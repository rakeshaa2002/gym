package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.UserWorkoutSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWorkoutScheduleRepository extends JpaRepository<UserWorkoutSchedule, Long> {
    List<UserWorkoutSchedule> findAllByOrderByUpdatedAtDescIdDesc();
}
