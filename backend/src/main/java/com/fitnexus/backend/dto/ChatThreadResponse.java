package com.fitnexus.backend.dto;

import java.util.List;

/** The member's conversation with their personal trainer. */
public record ChatThreadResponse(
        boolean enabled,          // plan includes trainer chat
        String partnerName,       // the trainer's name (null if none assigned)
        boolean partnerOnline,    // trainer active within the presence window
        String partnerLastSeen,   // human-friendly last-seen text (null when online/unknown)
        String notice,            // shown when disabled or no trainer assigned
        List<ChatMessageResponse> messages
) {
}
