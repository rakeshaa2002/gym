package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.CallResponse;
import com.fitnexus.backend.dto.IceServerResponse;
import com.fitnexus.backend.entity.CallLog;
import com.fitnexus.backend.entity.ChatMessage;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.CallLogRepository;
import com.fitnexus.backend.repository.ChatMessageRepository;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CallService {

    private final CallLogRepository callLogRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @Value("${webrtc.stun.server:stun:stun.l.google.com:19302}")
    private String stunServer;

    @Value("${webrtc.turn.url:}")
    private String turnUrl;

    @Value("${webrtc.turn.username:}")
    private String turnUsername;

    @Value("${webrtc.turn.credential:}")
    private String turnCredential;

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private Users resolveTrainer(Users member) {
        FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElse(null);
        if (fu != null && fu.getAssignedTrainer() != null && fu.getAssignedTrainer().getAccount() != null) {
            return fu.getAssignedTrainer().getAccount();
        }
        return member.getTrainer();
    }

    private Users resolveMember(Users trainer) {
        // When a trainer initiates a call to a specific member, the member ID comes from the request.
        return null; // handled in the specific methods
    }

    /** Expose configured ICE (STUN/TURN) servers to the frontend. */
    public List<IceServerResponse> getIceServers() {
        List<IceServerResponse> servers = new ArrayList<>();
        // Always include the configured STUN server.
        servers.add(new IceServerResponse(new String[]{stunServer}, "", ""));
        // If a TURN server is configured, include it with credentials.
        if (turnUrl != null && !turnUrl.isBlank()) {
            servers.add(new IceServerResponse(new String[]{turnUrl}, turnUsername, turnCredential));
        }
        return servers;
    }

    /**
     * Member initiates a call to their personal trainer.
     */
    public CallResponse initiateCall(String callType) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can initiate a call here");
        }
        Users trainer = resolveTrainer(me);
        if (trainer == null) {
            throw new InvalidOperationException("No personal trainer is assigned to you yet.");
        }
        // Check for already-ringing call to avoid duplicates
        Optional<CallLog> existing = callLogRepository
                .findFirstByMember_IdAndStatusOrderByCreatedAtDesc(me.getId(), "RINGING");
        if (existing.isPresent()) {
            throw new InvalidOperationException("You already have an active call request.");
        }

        CallLog call = new CallLog();
        call.setMember(me);
        call.setTrainer(trainer);
        call.setInitiator("MEMBER");
        call.setCallType("VIDEO".equalsIgnoreCase(callType) ? "VIDEO" : "AUDIO");
        call.setStatus("RINGING");
        call.setRoomName("call_" + me.getId() + "_" + trainer.getId() + "_" + System.currentTimeMillis());
        call.setCreatedAt(LocalDateTime.now());
        callLogRepository.save(call);

        // Notify trainer via WebSocket
        CallResponse payload = toResponse(call, trainer, me.getName());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(trainer.getId()), "/queue/call/incoming", payload);

        // Also send a system notification
        notificationService.createForUser(trainer, "CALL",
                (call.getCallType().equals("VIDEO") ? "📹 " : "📞 ") + "Incoming call from " + me.getName(),
                call.getCallType().equals("VIDEO") ? "Video call" : "Audio call",
                "/wellness-chat");

        return toResponse(call, trainer, me.getName());
    }

    /**
     * Trainer initiates a call to a specific member.
     */
    public CallResponse initiateCallAsTrainer(Long memberId, String callType) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.TRAINER) {
            throw new SecurityException("Only trainers can initiate calls to members");
        }
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));

        Optional<CallLog> existing = callLogRepository
                .findFirstByMember_IdAndStatusOrderByCreatedAtDesc(memberId, "RINGING");
        if (existing.isPresent()) {
            throw new InvalidOperationException("There is already an active call with this member.");
        }

        CallLog call = new CallLog();
        call.setMember(member);
        call.setTrainer(me);
        call.setInitiator("TRAINER");
        call.setCallType("VIDEO".equalsIgnoreCase(callType) ? "VIDEO" : "AUDIO");
        call.setStatus("RINGING");
        call.setRoomName("call_" + member.getId() + "_" + me.getId() + "_" + System.currentTimeMillis());
        call.setCreatedAt(LocalDateTime.now());
        callLogRepository.save(call);

        // Notify member via WebSocket
        CallResponse payload = toResponse(call, member, me.getName());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(member.getId()), "/queue/call/incoming", payload);

        notificationService.createForUser(member, "CALL",
                (call.getCallType().equals("VIDEO") ? "📹 " : "📞 ") + "Incoming call from trainer " + me.getName(),
                call.getCallType().equals("VIDEO") ? "Video call" : "Audio call",
                "/wellness-chat");

        return toResponse(call, member, me.getName());
    }

    /**
     * Accept an incoming call.
     */
    public CallResponse acceptCall(Long callId) {
        Users me = getCurrentUser();
        CallLog call = callLogRepository.findById(callId)
                .orElseThrow(() -> new InvalidOperationException("Call not found"));

        if (!"RINGING".equals(call.getStatus())) {
            throw new InvalidOperationException("Call is no longer ringing");
        }
        // Verify the callee is the one accepting
        boolean isTrainer = me.getRole() == Role.TRAINER;
        boolean isMember = me.getRole() == Role.USER;
        if ((isTrainer && !call.getTrainer().getId().equals(me.getId())) ||
            (isMember && !call.getMember().getId().equals(me.getId()))) {
            throw new SecurityException("You are not the recipient of this call");
        }

        call.setStatus("ACCEPTED");
        call.setAcceptedAt(LocalDateTime.now());
        callLogRepository.save(call);

        // Notify the caller that the call was accepted
        Users caller = call.getInitiator().equals("MEMBER") ? call.getMember() : call.getTrainer();
        CallResponse payload = toResponse(call, caller, me.getName());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(caller.getId()), "/queue/call/accepted", payload);

        return toResponse(call, caller, me.getName());
    }

    /**
     * Reject an incoming call.
     */
    public CallResponse rejectCall(Long callId) {
        Users me = getCurrentUser();
        CallLog call = callLogRepository.findById(callId)
                .orElseThrow(() -> new InvalidOperationException("Call not found"));

        if (!"RINGING".equals(call.getStatus())) {
            throw new InvalidOperationException("Call is no longer active");
        }
        boolean isTrainer = me.getRole() == Role.TRAINER;
        boolean isMember = me.getRole() == Role.USER;
        if ((isTrainer && !call.getTrainer().getId().equals(me.getId())) ||
            (isMember && !call.getMember().getId().equals(me.getId()))) {
            throw new SecurityException("You are not the recipient of this call");
        }

        call.setStatus("REJECTED");
        call.setEndedAt(LocalDateTime.now());
        callLogRepository.save(call);

        // Notify the caller
        Users caller = call.getInitiator().equals("MEMBER") ? call.getMember() : call.getTrainer();
        CallResponse payload = toResponse(call, caller, me.getName());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(caller.getId()), "/queue/call/rejected", payload);

        // Log as a chat message
        logCallToChat(call, "Call rejected");

        return toResponse(call, caller, me.getName());
    }

    /**
     * End an active call (either party).
     */
    public CallResponse endCall(Long callId) {
        Users me = getCurrentUser();
        CallLog call = callLogRepository.findById(callId)
                .orElseThrow(() -> new InvalidOperationException("Call not found"));

        if (!"ACCEPTED".equals(call.getStatus()) && !"RINGING".equals(call.getStatus())) {
            throw new InvalidOperationException("Call is not active");
        }

        boolean wasRinging = "RINGING".equals(call.getStatus());
        call.setStatus(wasRinging ? "MISSED" : "ENDED");
        call.setEndedAt(LocalDateTime.now());

        if (call.getAcceptedAt() != null) {
            long seconds = Duration.between(call.getAcceptedAt(), call.getEndedAt()).getSeconds();
            call.setDurationSeconds((int) seconds);
        }
        callLogRepository.save(call);

        // Notify the other party
        Users other = call.getMember().getId().equals(me.getId()) ? call.getTrainer() : call.getMember();
        CallResponse payload = toResponse(call, other, me.getName());
        String dest = wasRinging ? "/queue/call/missed" : "/queue/call/ended";
        messagingTemplate.convertAndSendToUser(
                String.valueOf(other.getId()), dest, payload);

        // Log call to chat if it was answered
        if (!wasRinging) {
            int mins = call.getDurationSeconds() != null ? call.getDurationSeconds() / 60 : 0;
            int secs = call.getDurationSeconds() != null ? call.getDurationSeconds() % 60 : 0;
            String durationStr = mins > 0 ? mins + " min " + secs + " sec" : secs + " sec";
            logCallToChat(call,
                    (call.getCallType().equals("VIDEO") ? "📹 " : "📞 ") + "Call ended · " + durationStr);
        }

        return toResponse(call, other, me.getName());
    }

    /**
     * Check for any pending incoming calls (used by the polling mechanism).
     */
    public List<CallResponse> getPendingCalls() {
        Users me = getCurrentUser();
        List<CallResponse> result = new ArrayList<>();
        // Check if someone is calling me
        Optional<CallLog> asMember = callLogRepository
                .findFirstByMember_IdAndStatusOrderByCreatedAtDesc(me.getId(), "RINGING");
        asMember.ifPresent(call -> {
            Users caller = call.getTrainer();
            result.add(toResponse(call, caller, caller.getName()));
        });
        Optional<CallLog> asTrainer = callLogRepository
                .findFirstByTrainer_IdAndStatusOrderByCreatedAtDesc(me.getId(), "RINGING");
        asTrainer.ifPresent(call -> {
            Users caller = call.getMember();
            result.add(toResponse(call, caller, caller.getName()));
        });
        return result;
    }

    /** Get call history for a member-trainer pair (for display in chat). */
    public List<CallResponse> getCallHistory(Long partnerId) {
        Users me = getCurrentUser();
        List<CallLog> logs;
        if (me.getRole() == Role.USER) {
            logs = callLogRepository.findByMember_IdAndTrainer_IdOrderByCreatedAtDesc(me.getId(), partnerId);
        } else if (me.getRole() == Role.TRAINER) {
            logs = callLogRepository.findByMember_IdAndTrainer_IdOrderByCreatedAtDesc(partnerId, me.getId());
        } else {
            throw new SecurityException("Only members and trainers have call history");
        }
        return logs.stream()
                .map(c -> toResponse(c,
                        c.getMember().getId().equals(me.getId()) ? c.getTrainer() : c.getMember(),
                        null))
                .toList();
    }

    /* ---------------- helpers ---------------- */

    private CallResponse toResponse(CallLog call, Users partner, String partnerName) {
        return new CallResponse(
                call.getId(),
                call.getCallType(),
                call.getStatus(),
                call.getInitiator(),
                partner.getId(),
                partnerName != null ? partnerName : partner.getName(),
                call.getDurationSeconds(),
                call.getCreatedAt() != null ? call.getCreatedAt().format(STAMP) : ""
        );
    }

    /** Write a call event as a chat message so it appears in the conversation. */
    private void logCallToChat(CallLog call, String summary) {
        Users member = call.getMember();
        Users trainer = call.getTrainer();
        boolean senderIsMember = call.getInitiator().equals("MEMBER");

        ChatMessage msg = new ChatMessage();
        msg.setMember(member);
        msg.setTrainer(trainer);
        msg.setSenderMember(senderIsMember);
        msg.setContent(summary);
        msg.setRead(true); // call logs are informational, no read-receipt needed
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(msg);
    }
}
