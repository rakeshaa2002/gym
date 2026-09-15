package com.fitnexus.backend.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Relays WebRTC signaling messages (SDP offers/answers, ICE candidates) between
 * peers via STOMP over WebSocket. The frontend publishes to /app/call.signal and
 * the server forwards to the intended recipient on /user/{recipientId}/queue/call/signal.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CallSignalHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call.signal")
    public void handleSignal(@Payload SignalMessage message) {
        if (message == null || message.getRecipientId() == null) {
            log.warn("Received invalid call signal: {}", message);
            return;
        }
        log.debug("Relaying {} signal to user {}", message.getType(), message.getRecipientId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getRecipientId()),
                "/queue/call/signal",
                message
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalMessage {
        /** The user ID this signal should be forwarded to. */
        private Long recipientId;
        /** "offer" | "answer" | "ice-candidate" | "end" */
        private String type;
        /** The SDP or ICE candidate payload. */
        private Object payload;
        /** The call ID these signals belong to. */
        private Long callId;
    }
}
