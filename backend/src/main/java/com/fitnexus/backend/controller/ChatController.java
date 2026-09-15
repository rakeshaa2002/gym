package com.fitnexus.backend.controller;

import com.fitnexus.backend.dto.ApiErrorResponse;
import com.fitnexus.backend.dto.ApiSuccessResponse;
import com.fitnexus.backend.dto.SendMessageRequest;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://gym.infitoolz.com", "https://gym.infitoolz.com"})
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    /** Member: their conversation with their personal trainer. */
    @GetMapping("/my")
    public ResponseEntity<?> myThread() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Chat retrieved", chatService.getMyThread(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/my", e.getMessage());
        } catch (Exception e) {
            log.error("Chat thread error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/my", "Error loading chat: " + e.getMessage());
        }
    }

    @PostMapping("/my")
    public ResponseEntity<?> sendAsMember(@RequestBody SendMessageRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Message sent", chatService.sendAsMember(
                    request == null ? null : request.getContent(),
                    request == null ? null : request.getAttachmentUrl(),
                    request == null ? null : request.getAttachmentName(),
                    request == null ? null : request.getAttachmentType()), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/my", e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/chat/my", e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/chat/my", e.getMessage());
        } catch (Exception e) {
            log.error("Send message error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/my", "Error sending message: " + e.getMessage());
        }
    }

    /** Trainer: list of member conversations. */
    @GetMapping("/conversations")
    public ResponseEntity<?> conversations() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Conversations retrieved", chatService.getConversations(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/conversations", e.getMessage());
        } catch (Exception e) {
            log.error("Conversations error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/conversations", "Error loading conversations: " + e.getMessage());
        }
    }

    @GetMapping("/conversation/{memberId}")
    public ResponseEntity<?> conversation(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Conversation retrieved", chatService.getConversation(memberId), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/conversation/" + memberId, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/chat/conversation/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Conversation error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/conversation/" + memberId, "Error loading conversation: " + e.getMessage());
        }
    }

    @PostMapping("/conversation/{memberId}")
    public ResponseEntity<?> sendAsTrainer(@PathVariable Long memberId, @RequestBody SendMessageRequest request) {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Message sent", chatService.sendAsTrainer(memberId,
                    request == null ? null : request.getContent(),
                    request == null ? null : request.getAttachmentUrl(),
                    request == null ? null : request.getAttachmentName(),
                    request == null ? null : request.getAttachmentType()), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/conversation/" + memberId, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "/api/chat/conversation/" + memberId, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, "/api/chat/conversation/" + memberId, e.getMessage());
        } catch (Exception e) {
            log.error("Trainer send error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/conversation/" + memberId, "Error sending message: " + e.getMessage());
        }
    }

    /* ---------------- Admin / manager oversight (read-only) ---------------- */

    /** Admin/Manager: every trainer with a chat summary, to pick from. */
    @GetMapping("/admin/trainers")
    public ResponseEntity<?> oversightTrainers() {
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Trainers retrieved", chatService.getTrainersForOversight(), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, "/api/chat/admin/trainers", e.getMessage());
        } catch (Exception e) {
            log.error("Oversight trainers error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "/api/chat/admin/trainers", "Error loading trainers: " + e.getMessage());
        }
    }

    /** Admin/Manager: the member conversations of a chosen trainer. */
    @GetMapping("/admin/trainer/{trainerId}/conversations")
    public ResponseEntity<?> oversightConversations(@PathVariable Long trainerId) {
        String path = "/api/chat/admin/trainer/" + trainerId + "/conversations";
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Conversations retrieved", chatService.getConversationsForOversight(trainerId), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, path, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, path, e.getMessage());
        } catch (Exception e) {
            log.error("Oversight conversations error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, path, "Error loading conversations: " + e.getMessage());
        }
    }

    /** Admin/Manager: the messages between a trainer and one of their members (read-only). */
    @GetMapping("/admin/trainer/{trainerId}/conversation/{memberId}")
    public ResponseEntity<?> oversightConversation(@PathVariable Long trainerId, @PathVariable Long memberId) {
        String path = "/api/chat/admin/trainer/" + trainerId + "/conversation/" + memberId;
        try {
            return ResponseEntity.ok(new ApiSuccessResponse<>(HttpStatus.OK.value(), "Conversation retrieved", chatService.getOversightConversation(trainerId, memberId), LocalDateTime.now().toString()));
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, path, e.getMessage());
        } catch (InvalidOperationException e) {
            return error(HttpStatus.NOT_FOUND, path, e.getMessage());
        } catch (Exception e) {
            log.error("Oversight conversation error", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, path, "Error loading conversation: " + e.getMessage());
        }
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String path, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message, LocalDateTime.now().toString(), path));
    }
}
