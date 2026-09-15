package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallResponse {
    private Long callId;
    private String callType;    // "AUDIO" | "VIDEO"
    private String status;      // "RINGING" | "ACCEPTED" | "REJECTED" | "ENDED" | "MISSED"
    private String initiator;   // "MEMBER" | "TRAINER"
    private Long partnerId;
    private String partnerName;
    private Integer durationSeconds;
    private String createdAt;
}
