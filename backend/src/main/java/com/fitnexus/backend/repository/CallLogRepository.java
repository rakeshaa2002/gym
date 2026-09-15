package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    /** Find the most recent still-ringing call for a user (either as member or trainer). */
    Optional<CallLog> findFirstByMember_IdAndStatusOrderByCreatedAtDesc(Long memberId, String status);

    Optional<CallLog> findFirstByTrainer_IdAndStatusOrderByCreatedAtDesc(Long trainerId, String status);

    /** All calls (history) for a member–trainer pair, newest first. */
    List<CallLog> findByMember_IdAndTrainer_IdOrderByCreatedAtDesc(Long memberId, Long trainerId);
}
