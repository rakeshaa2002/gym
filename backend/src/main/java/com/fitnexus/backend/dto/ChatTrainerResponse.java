package com.fitnexus.backend.dto;

/** One trainer row in the admin/manager chat-oversight list. */
public record ChatTrainerResponse(
        Long trainerId,          // trainer's account id (same id used by the chat endpoints)
        String trainerName,
        String trainerEmail,
        long memberCount,        // members assigned to this trainer
        long unread,             // member -> trainer messages the trainer hasn't read yet
        String lastAt,           // last message time across all their conversations (null if none)
        boolean online,          // trainer active within the presence window
        String lastSeen          // human-friendly last-seen text (null when online/unknown)
) {
}
