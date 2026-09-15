package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.NotificationResponse;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Notification;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.NotificationRepository;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.repository.UserWorkoutScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the header notification feed for the signed-in user. Combines persisted
 * event rows (e.g. an admin assigning a plan, via {@link #createForUser}) with
 * notifications derived live from current state (pending approvals, onboarding
 * completions for staff; upcoming sessions for members).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final UserWorkoutScheduleRepository userWorkoutScheduleRepository;
    private final NotificationRepository notificationRepository;

    private static final DateTimeFormatter DAY_TIME = DateTimeFormatter.ofPattern("MMM d, HH:mm");
    private static final long ONBOARDING_WINDOW_DAYS = 7;

    private Users currentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    public List<NotificationResponse> getMyNotifications() {
        Users me = currentAccount();
        return switch (me.getRole()) {
            case SUPER_ADMIN, ADMIN, MANAGER, TRAINER, CORPORATE_HR, COUNSELOR -> staffNotifications(me);
            case USER -> memberNotifications(me);
        };
    }

    /** Persists a notification addressed to one user (called when an event happens). */
    public void createForUser(Users recipient, String type, String title, String message, String link) {
        if (recipient == null) {
            return;
        }
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    /** Staff: persisted events (e.g. a member check-in) + approvals + recent onboarding. */
    private List<NotificationResponse> staffNotifications(Users me) {
        List<FitnessUser> visible = visibleCustomersFor(me);
        List<NotificationResponse> result = new ArrayList<>();

        // Persisted events addressed to this staff member (newest first), e.g. attendance.
        notificationRepository.findTop50ByRecipient_IdOrderByCreatedAtDesc(me.getId())
                .forEach(n -> result.add(new NotificationResponse(
                        "n-" + n.getId(),
                        n.getType(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getCreatedAt() != null ? n.getCreatedAt().format(DAY_TIME) : "",
                        n.getLink())));

        // Customers awaiting approval.
        visible.stream()
                .filter(c -> c.getAccount() != null && Boolean.FALSE.equals(c.getAccount().getIsApproved()))
                .forEach(c -> {
                    String email = c.getAccount().getEmail();
                    result.add(new NotificationResponse("approval-" + c.getId(), "APPROVAL", "Pending approval",
                            displayName(c) + (email == null || email.isBlank() ? "" : " · " + email),
                            "Awaiting approval", "/users"));
                });

        // Members who finished onboarding in the last week.
        LocalDateTime since = LocalDateTime.now().minusDays(ONBOARDING_WINDOW_DAYS);
        visible.stream()
                .filter(c -> c.getAccount() != null && c.getAccount().getOnboardingCompletedAt() != null
                        && c.getAccount().getOnboardingCompletedAt().isAfter(since))
                .sorted(Comparator.comparing((FitnessUser c) -> c.getAccount().getOnboardingCompletedAt()).reversed())
                .forEach(c -> result.add(new NotificationResponse("onboard-" + c.getId(), "INFO", "Onboarding completed",
                        displayName(c) + " finished setting up their profile",
                        c.getAccount().getOnboardingCompletedAt().format(DAY_TIME), "/users")));

        return result;
    }

    private List<FitnessUser> visibleCustomersFor(Users me) {
        return switch (me.getRole()) {
            case SUPER_ADMIN -> fitnessUserRepository.findAll();
            case ADMIN -> fitnessUserRepository.findByAccount_Admin_Id(me.getId());
            case MANAGER -> fitnessUserRepository.findByAccount_Manager_Id(me.getId());
            case CORPORATE_HR -> fitnessUserRepository.findByAssignedCorporateHrId(me.getId());
            case TRAINER -> Stream.concat(
                    fitnessUserRepository.findByAssignedTrainer_Account_Id(me.getId()).stream(),
                    Stream.concat(
                            fitnessUserRepository.findByAccount_Trainer_Id(me.getId()).stream(),
                            fitnessUserRepository.findByAccount_CreatedBy_Id(me.getId()).stream()))
                    .collect(Collectors.toMap(FitnessUser::getId, u -> u, (a, b) -> a, LinkedHashMap::new))
                    .values().stream().toList();
            default -> List.of();
        };
    }

    /** Member: persisted event notifications (plan assignments, etc.) + upcoming sessions. */
    private List<NotificationResponse> memberNotifications(Users me) {
        List<NotificationResponse> result = new ArrayList<>();

        // Persisted events addressed to this member (newest first).
        notificationRepository.findTop50ByRecipient_IdOrderByCreatedAtDesc(me.getId())
                .forEach(n -> result.add(new NotificationResponse(
                        "n-" + n.getId(),
                        n.getType(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getCreatedAt() != null ? n.getCreatedAt().format(DAY_TIME) : "",
                        n.getLink())));

        // Upcoming scheduled sessions (derived live).
        userWorkoutScheduleRepository
                .findByUser_IdAndStartDateTimeAfterOrderByStartDateTimeAsc(me.getId(), LocalDateTime.now())
                .stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getStatus()))
                .limit(8)
                .forEach(s -> result.add(new NotificationResponse(
                        "sched-" + s.getId(),
                        "SCHEDULE",
                        s.getTitle() != null ? s.getTitle() : "Upcoming session",
                        s.getLocation() != null && !s.getLocation().isBlank()
                                ? "at " + s.getLocation() : "Scheduled workout",
                        s.getStartDateTime().format(DAY_TIME),
                        "/my-schedule")));
        return result;
    }

    private String displayName(FitnessUser c) {
        String name = Stream.of(c.getFirstName(), c.getLastName())
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining(" "));
        if (!name.isBlank()) {
            return name;
        }
        if (c.getAccount() != null && c.getAccount().getName() != null && !c.getAccount().getName().isBlank()) {
            return c.getAccount().getName();
        }
        return c.getAccount() != null && c.getAccount().getEmail() != null ? c.getAccount().getEmail() : "Member";
    }
}
