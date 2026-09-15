package com.fitnexus.backend.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String content;
    // Optional attachment (image or document) already uploaded via /api/uploads/chat-attachments.
    private String attachmentUrl;
    private String attachmentName;
    private String attachmentType; // "IMAGE" or "FILE"
}
