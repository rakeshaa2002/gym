package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByMember_IdAndTrainer_IdOrderByCreatedAtAsc(Long memberId, Long trainerId);

    Optional<ChatMessage> findFirstByMember_IdAndTrainer_IdOrderByCreatedAtDesc(Long memberId, Long trainerId);

    /** Unread messages the member hasn't seen (sent by the trainer). */
    long countByMember_IdAndTrainer_IdAndSenderMemberFalseAndReadFalse(Long memberId, Long trainerId);

    /** Unread messages the trainer hasn't seen (sent by the member). */
    long countByMember_IdAndTrainer_IdAndSenderMemberTrueAndReadFalse(Long memberId, Long trainerId);

    /** Latest message across all of a trainer's conversations (for the oversight list). */
    Optional<ChatMessage> findFirstByTrainer_IdOrderByCreatedAtDesc(Long trainerId);

    /** Member -> trainer messages the trainer hasn't read yet, across all their members. */
    long countByTrainer_IdAndSenderMemberTrueAndReadFalse(Long trainerId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ChatMessage c WHERE c.member.id = :memberId OR c.trainer.id = :memberId")
    void deleteByMemberId(@org.springframework.data.repository.query.Param("memberId") Long memberId);
}
