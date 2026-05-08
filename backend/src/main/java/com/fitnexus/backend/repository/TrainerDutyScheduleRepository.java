package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.TrainerDutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerDutyScheduleRepository extends JpaRepository<TrainerDutySchedule, Long> {
    List<TrainerDutySchedule> findAllByOrderByUpdatedAtDescIdDesc();
}
