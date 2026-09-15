package com.fitnexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * One message in the wellness chat between a member and their personal trainer.
 * A conversation is the (member, trainer) pair; {@code senderMember} marks direction.
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users trainer;

    /** true = sent by the member, false = sent by the trainer. */
    @Column(name = "sender_member", nullable = false)
    private boolean senderMember;

    @Column(nullable = false, length = 2000)
    private String content;

    /** Public URL of an attached image/document, or null for a plain text message. */
    @Column(name = "attachment_url", length = 512)
    private String attachmentUrl;

    /** Original filename of the attachment, shown to the recipient. */
    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    /** "IMAGE" (render inline) or "FILE" (download chip); null for text-only messages. */
    @Column(name = "attachment_type", length = 16)
    private String attachmentType;

    /** Read by the recipient. */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
