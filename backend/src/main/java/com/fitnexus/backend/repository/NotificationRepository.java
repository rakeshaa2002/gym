package com.fitnexus.backend.repository;

import com.fitnexus.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByRecipient_IdOrderByCreatedAtDesc(Long recipientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Notification n WHERE n.recipient.id = :userId")
    void deleteByRecipientId(@org.springframework.data.repository.query.Param("userId") Long userId);

    boolean existsByRecipient_IdAndTypeAndCreatedAtAfter(Long recipientId, String type, java.time.LocalDateTime after);
}
