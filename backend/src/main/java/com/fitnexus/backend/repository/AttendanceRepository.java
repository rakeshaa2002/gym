package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Attendance;
import com.fitnexus.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserOrderByCheckInTimeDesc(Users user);

    List<Attendance> findByAttendanceDateOrderByCheckInTimeDesc(LocalDate date);

    List<Attendance> findAllByOrderByCheckInTimeDesc();

    Optional<Attendance> findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Users user);

    Optional<Attendance> findFirstByUserOrderByCheckInTimeDesc(Users user);

    /** Members currently inside (no check-out yet) — used by the overstay scheduler. */
    List<Attendance> findByCheckOutTimeIsNull();

    // Admin dashboard aggregates
    long countByAttendanceDate(LocalDate date);

    long countByAttendanceDateAndCheckOutTimeIsNull(LocalDate date);

    @Query("SELECT a.attendanceDate, COUNT(a) FROM Attendance a WHERE a.attendanceDate >= :from GROUP BY a.attendanceDate")
    List<Object[]> countGroupedByDateSince(@Param("from") LocalDate from);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Attendance a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT a.user.id) FROM Attendance a WHERE a.user.id IN :userIds AND a.attendanceDate >= :from")
    long countDistinctUsersByUserIdInAndDateAfter(@Param("userIds") List<Long> userIds, @Param("from") LocalDate from);
}
