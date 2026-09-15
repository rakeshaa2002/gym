package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.UserWorkoutSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserWorkoutScheduleRepository extends JpaRepository<UserWorkoutSchedule, Long> {
    List<UserWorkoutSchedule> findAllByOrderByUpdatedAtDescIdDesc();

    List<UserWorkoutSchedule> findByUser_IdAndStartDateTimeAfterOrderByStartDateTimeAsc(Long userId, LocalDateTime after);

    List<UserWorkoutSchedule> findByUser_IdOrderByStartDateTimeAsc(Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM UserWorkoutSchedule s WHERE s.user.id = :userId OR s.trainer.id = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
