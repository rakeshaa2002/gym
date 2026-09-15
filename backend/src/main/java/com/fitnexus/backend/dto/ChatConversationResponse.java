package com.fitnexus.backend.dto;

/** One member conversation in the trainer's chat list. */
public record ChatConversationResponse(
        Long memberId,
        String memberName,
        String memberEmail,
        String lastMessage,
        String lastAt,
        long unread,
        boolean memberOnline,    // member active within the presence window
        String memberLastSeen    // human-friendly last-seen text (null when online/unknown)
) {
}
