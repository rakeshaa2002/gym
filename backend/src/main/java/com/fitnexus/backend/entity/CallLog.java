package com.fitnexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks a single audio/video call session between a member and their trainer.
 * Each record lives long enough to coordinate the call (RINGING -> ACCEPTED -> ENDED,
 * or RINGING -> REJECTED / MISSED). Finished calls remain as call-history entries
 * so they can be shown in the chat UI.
 */
@Entity
@Table(name = "call_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The member participating in the call. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Users member;

    /** The trainer participating in the call. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Users trainer;

    /** Who initiated the call: "MEMBER" or "TRAINER". */
    @Column(name = "initiator", nullable = false, length = 16)
    private String initiator;

    /** "AUDIO" or "VIDEO". */
    @Column(name = "call_type", nullable = false, length = 16)
    private String callType;

    /**
     * RINGING   — caller initiated, awaiting response
     * ACCEPTED  — callee answered, call is active
     * REJECTED  — callee declined
     * ENDED     — either party ended an active call
     * MISSED    — caller hung up before callee answered
     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /** Unique room identifier used to correlate signals for this call session. */
    @Column(name = "room_name", length = 128)
    private String roomName;

    /** Call duration in seconds (set when the call ends). */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
