package com.fitnexus.backend.dto;

public record ChatMessageResponse(
        Long id,
        String content,
        boolean mine,        // sent by the viewer
        String senderName,
        boolean read,
        String status,       // for own messages: "sent" | "delivered" | "read" (null for received)
        String createdAt,
        String attachmentUrl,   // null for text-only messages
        String attachmentName,
        String attachmentType   // "IMAGE" | "FILE"
) {
}
