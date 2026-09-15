package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.ChatConversationResponse;
import com.fitnexus.backend.dto.ChatMessageResponse;
import com.fitnexus.backend.dto.ChatThreadResponse;
import com.fitnexus.backend.dto.ChatTrainerResponse;
import com.fitnexus.backend.entity.ChatMessage;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Trainer;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.ChatMessageRepository;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.TrainerRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    /** A user is "online" if they polled the chat within this window. */
    private static final long ONLINE_WINDOW_SECONDS = 75;

    /** Roles allowed to read (but not participate in) any trainer's conversations. */
    private static final Set<Role> OVERSIGHT_ROLES = EnumSet.of(Role.SUPER_ADMIN, Role.ADMIN, Role.MANAGER);

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private Users resolveTrainer(Users member, FitnessUser fu) {
        if (fu != null && fu.getAssignedTrainer() != null && fu.getAssignedTrainer().getAccount() != null) {
            return fu.getAssignedTrainer().getAccount();
        }
        return member.getTrainer();
    }

    private boolean hasTrainerChat(FitnessUser fu) {
        return fu != null && fu.getMembershipPlanRef() != null
                && Boolean.TRUE.equals(fu.getMembershipPlanRef().getTrainerChat());
    }

    /** Stamp the current user as just-active so the other party sees them online. */
    private void touchPresence(Users me) {
        me.setLastSeenAt(LocalDateTime.now());
        userRepository.save(me);
    }

    private boolean isOnline(LocalDateTime lastSeen) {
        return lastSeen != null && lastSeen.isAfter(LocalDateTime.now().minusSeconds(ONLINE_WINDOW_SECONDS));
    }

    /** Null when online (the UI shows "Online"); otherwise "last seen MMM d, HH:mm". */
    private String lastSeenText(LocalDateTime lastSeen) {
        if (lastSeen == null || isOnline(lastSeen)) {
            return null;
        }
        return "last seen " + lastSeen.format(STAMP);
    }

    private ChatMessageResponse toResponse(ChatMessage m, boolean viewerIsMember, String memberName,
                                           String trainerName, LocalDateTime partnerLastSeen) {
        boolean mine = viewerIsMember == m.isSenderMember();
        String senderName = m.isSenderMember() ? memberName : trainerName;
        // WhatsApp-style ticks only apply to messages the viewer sent.
        String status = null;
        if (mine) {
            if (m.isRead()) {
                status = "read";
            } else if (partnerLastSeen != null && m.getCreatedAt() != null
                    && partnerLastSeen.isAfter(m.getCreatedAt())) {
                status = "delivered";
            } else {
                status = "sent";
            }
        }
        return new ChatMessageResponse(m.getId(), m.getContent(), mine, senderName, m.isRead(), status,
                m.getCreatedAt() != null ? m.getCreatedAt().format(STAMP) : "",
                m.getAttachmentUrl(), m.getAttachmentName(), m.getAttachmentType());
    }

    /* ---------------- Member side ---------------- */

    public ChatThreadResponse getMyThread() {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members have a wellness chat");
        }
        touchPresence(me);
        FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElse(null);
        if (!hasTrainerChat(fu)) {
            return new ChatThreadResponse(false, null, false, null,
                    "Wellness chat with a personal trainer isn't part of your plan. Upgrade to unlock it.",
                    List.of());
        }
        Users trainer = resolveTrainer(me, fu);
        if (trainer == null) {
            return new ChatThreadResponse(true, null, false, null,
                    "No personal trainer is assigned to you yet. Please contact the gym.", List.of());
        }
        List<ChatMessage> msgs = chatMessageRepository
                .findByMember_IdAndTrainer_IdOrderByCreatedAtAsc(me.getId(), trainer.getId());
        // Mark trainer -> member messages as read.
        List<ChatMessage> unread = msgs.stream().filter(m -> !m.isSenderMember() && !m.isRead()).toList();
        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setRead(true));
            chatMessageRepository.saveAll(unread);
        }
        LocalDateTime trainerLastSeen = trainer.getLastSeenAt();
        List<ChatMessageResponse> out = msgs.stream()
                .map(m -> toResponse(m, true, me.getName(), trainer.getName(), trainerLastSeen)).toList();
        return new ChatThreadResponse(true, trainer.getName(), isOnline(trainerLastSeen),
                lastSeenText(trainerLastSeen), null, out);
    }

    public ChatMessageResponse sendAsMember(String content, String attachmentUrl, String attachmentName,
                                            String attachmentType) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can send messages here");
        }
        String text = requireMessage(content, attachmentUrl);
        FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElse(null);
        if (!hasTrainerChat(fu)) {
            throw new SecurityException("Your plan does not include wellness chat. Please upgrade.");
        }
        Users trainer = resolveTrainer(me, fu);
        if (trainer == null) {
            throw new InvalidOperationException("No personal trainer is assigned to you yet.");
        }
        ChatMessage saved = persist(me, trainer, true, text, attachmentUrl, attachmentName, attachmentType);
        notificationService.createForUser(trainer, "CHAT", "New message from " + me.getName(),
                notifPreview(text, attachmentType), "/wellness-chat");
        return toResponse(saved, true, me.getName(), trainer.getName(), trainer.getLastSeenAt());
    }

    /* ---------------- Trainer side ---------------- */

    public List<ChatConversationResponse> getConversations() {
        Users me = getCurrentUser();
        if (me.getRole() != Role.TRAINER) {
            throw new SecurityException("Only trainers have member conversations");
        }
        touchPresence(me);
        return buildConversations(me.getId());
    }

    /** Build the member-conversation list for a trainer (shared by the trainer view and oversight). */
    private List<ChatConversationResponse> buildConversations(Long trainerId) {
        List<FitnessUser> members = fitnessUserRepository.findByAssignedTrainer_Account_Id(trainerId);
        List<ChatConversationResponse> out = new ArrayList<>();
        for (FitnessUser fu : members) {
            Users account = fu.getAccount();
            if (account == null) {
                continue;
            }
            ChatMessage last = chatMessageRepository
                    .findFirstByMember_IdAndTrainer_IdOrderByCreatedAtDesc(account.getId(), trainerId)
                    .orElse(null);
            long unread = chatMessageRepository
                    .countByMember_IdAndTrainer_IdAndSenderMemberTrueAndReadFalse(account.getId(), trainerId);
            LocalDateTime memberLastSeen = account.getLastSeenAt();
            out.add(new ChatConversationResponse(
                    account.getId(),
                    account.getName(),
                    account.getEmail(),
                    last != null ? last.getContent() : null,
                    last != null && last.getCreatedAt() != null ? last.getCreatedAt().format(STAMP) : null,
                    unread,
                    isOnline(memberLastSeen),
                    lastSeenText(memberLastSeen)));
        }
        // Most recent activity first; members with no messages fall to the bottom.
        out.sort(Comparator.comparing((ChatConversationResponse c) -> c.lastAt() == null ? "" : c.lastAt()).reversed());
        return out;
    }

    public List<ChatMessageResponse> getConversation(Long memberId) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.TRAINER) {
            throw new SecurityException("Only trainers have member conversations");
        }
        touchPresence(me);
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        List<ChatMessage> msgs = chatMessageRepository
                .findByMember_IdAndTrainer_IdOrderByCreatedAtAsc(memberId, me.getId());
        // Mark member -> trainer messages as read.
        List<ChatMessage> unread = msgs.stream().filter(m -> m.isSenderMember() && !m.isRead()).toList();
        if (!unread.isEmpty()) {
            unread.forEach(m -> m.setRead(true));
            chatMessageRepository.saveAll(unread);
        }
        LocalDateTime memberLastSeen = member.getLastSeenAt();
        return msgs.stream().map(m -> toResponse(m, false, member.getName(), me.getName(), memberLastSeen)).toList();
    }

    public ChatMessageResponse sendAsTrainer(Long memberId, String content, String attachmentUrl,
                                             String attachmentName, String attachmentType) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.TRAINER) {
            throw new SecurityException("Only trainers can reply here");
        }
        String text = requireMessage(content, attachmentUrl);
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        ChatMessage saved = persist(member, me, false, text, attachmentUrl, attachmentName, attachmentType);
        notificationService.createForUser(member, "CHAT", "Message from your trainer",
                notifPreview(text, attachmentType), "/wellness-chat");
        return toResponse(saved, false, member.getName(), me.getName(), member.getLastSeenAt());
    }

    /* ---------------- Admin / manager oversight (read-only) ---------------- */

    private void requireOversight() {
        Users me = getCurrentUser();
        if (!OVERSIGHT_ROLES.contains(me.getRole())) {
            throw new SecurityException("You don't have access to chat oversight");
        }
    }

    /** All trainers with their conversation summary, for the oversight trainer picker. */
    public List<ChatTrainerResponse> getTrainersForOversight() {
        requireOversight();
        List<ChatTrainerResponse> out = new ArrayList<>();
        for (Trainer trainer : trainerRepository.findAll()) {
            Users account = trainer.getAccount();
            if (account == null) {
                continue;
            }
            Long trainerId = account.getId();
            long memberCount = fitnessUserRepository.countByAssignedTrainer_Account_Id(trainerId);
            long unread = chatMessageRepository.countByTrainer_IdAndSenderMemberTrueAndReadFalse(trainerId);
            ChatMessage last = chatMessageRepository
                    .findFirstByTrainer_IdOrderByCreatedAtDesc(trainerId).orElse(null);
            LocalDateTime trainerLastSeen = account.getLastSeenAt();
            out.add(new ChatTrainerResponse(
                    trainerId,
                    account.getName(),
                    account.getEmail(),
                    memberCount,
                    unread,
                    last != null && last.getCreatedAt() != null ? last.getCreatedAt().format(STAMP) : null,
                    isOnline(trainerLastSeen),
                    lastSeenText(trainerLastSeen)));
        }
        // Most recent activity first; trainers with no chats fall to the bottom.
        out.sort(Comparator.comparing((ChatTrainerResponse t) -> t.lastAt() == null ? "" : t.lastAt()).reversed());
        return out;
    }

    /** A chosen trainer's member conversations (read-only). */
    public List<ChatConversationResponse> getConversationsForOversight(Long trainerId) {
        requireOversight();
        Users trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new InvalidOperationException("Trainer not found"));
        if (trainer.getRole() != Role.TRAINER) {
            throw new InvalidOperationException("That account is not a trainer");
        }
        return buildConversations(trainerId);
    }

    /**
     * The messages between a given trainer and member, for oversight. Read-only: unlike the
     * member/trainer views this never marks messages read or stamps presence, so simply looking
     * doesn't disturb the participants' unread badges or last-seen.
     */
    public List<ChatMessageResponse> getOversightConversation(Long trainerId, Long memberId) {
        requireOversight();
        Users trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new InvalidOperationException("Trainer not found"));
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        List<ChatMessage> msgs = chatMessageRepository
                .findByMember_IdAndTrainer_IdOrderByCreatedAtAsc(memberId, trainerId);
        // Show it from the trainer's vantage point (their replies on the right, member on the left).
        LocalDateTime memberLastSeen = member.getLastSeenAt();
        return msgs.stream()
                .map(m -> toResponse(m, false, member.getName(), trainer.getName(), memberLastSeen)).toList();
    }

    /* ---------------- helpers ---------------- */

    private ChatMessage persist(Users member, Users trainer, boolean senderMember, String content,
                                String attachmentUrl, String attachmentName, String attachmentType) {
        ChatMessage m = new ChatMessage();
        m.setMember(member);
        m.setTrainer(trainer);
        m.setSenderMember(senderMember);
        m.setContent(content == null ? "" : content);
        if (attachmentUrl != null && !attachmentUrl.isBlank()) {
            m.setAttachmentUrl(attachmentUrl.trim());
            m.setAttachmentName(attachmentName != null && !attachmentName.isBlank()
                    ? attachmentName.trim() : "attachment");
            m.setAttachmentType("IMAGE".equalsIgnoreCase(attachmentType) ? "IMAGE" : "FILE");
        }
        m.setRead(false);
        m.setCreatedAt(LocalDateTime.now());
        return chatMessageRepository.save(m);
    }

    /**
     * A message needs either text or an attachment. The attachment URL must point at our own
     * uploads folder (the client gets it from {@code /api/uploads/chat-attachments}) so a caller
     * can't smuggle an arbitrary external link through the chat.
     */
    private String requireMessage(String content, String attachmentUrl) {
        String trimmed = content == null ? "" : content.trim();
        boolean hasAttachment = attachmentUrl != null && !attachmentUrl.isBlank();
        if (trimmed.isEmpty() && !hasAttachment) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (hasAttachment && !attachmentUrl.trim().startsWith("/uploads/")) {
            throw new IllegalArgumentException("Invalid attachment reference");
        }
        return trimmed.length() > 2000 ? trimmed.substring(0, 2000) : trimmed;
    }

    /** Notification line: the text, or a stand-in label when the message is attachment-only. */
    private String notifPreview(String content, String attachmentType) {
        if (content != null && !content.isBlank()) {
            return content.length() > 120 ? content.substring(0, 117) + "..." : content;
        }
        return "IMAGE".equalsIgnoreCase(attachmentType) ? "📷 Photo" : "📎 Attachment";
    }
}
