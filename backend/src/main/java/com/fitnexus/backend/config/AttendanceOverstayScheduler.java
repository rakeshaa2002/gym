package com.fitnexus.backend.config;

import com.fitnexus.backend.entity.Attendance;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.AttendanceRepository;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Periodically scans members who are still inside the gym and flags anyone who has
 * stayed past their plan's per-visit limit (e.g. Basic = 1 hour). On a first breach
 * it notifies the member's admin ("not exited yet") and the member themselves
 * ("upgrade for longer access"). Premium / unlimited plans are never flagged.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceOverstayScheduler {

    private final AttendanceRepository attendanceRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** Runs every 5 minutes by default (configurable), after a 1-minute startup delay. */
    @Scheduled(fixedDelayString = "${attendance.overstay-check-ms:300000}", initialDelay = 60000)
    @Transactional
    public void flagOverstays() {
        LocalDateTime now = LocalDateTime.now();
        for (Attendance a : attendanceRepository.findByCheckOutTimeIsNull()) {
            try {
                if (Boolean.TRUE.equals(a.getOverstayNotified())) {
                    continue;
                }
                Users member = a.getUser();
                if (member == null || member.getRole() != Role.USER || a.getCheckInTime() == null) {
                    continue;
                }
                FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElse(null);
                MembershipPlan plan = fu != null ? fu.getMembershipPlanRef() : null;
                Integer limit = plan != null ? plan.getMaxSessionMinutes() : null;
                if (limit == null || limit <= 0) {
                    continue; // unlimited session — no overstay
                }
                long minutes = Duration.between(a.getCheckInTime(), now).toMinutes();
                if (minutes < limit) {
                    continue;
                }
                String planName = plan.getName() != null ? plan.getName() : "your";
                notifyAdmins(member, planName, limit, minutes);
                notificationService.createForUser(member, "ATTENDANCE", "Session time exceeded",
                        "You've passed your " + planName + " plan's " + limit
                                + "-minute visit limit. Upgrade your plan to extend your time at the gym.",
                        "/membership");
                a.setOverstayNotified(true);
                a.setUpdatedAt(now);
                attendanceRepository.save(a);
            } catch (Exception e) {
                log.warn("Overstay check failed for attendance {}", a.getId(), e);
            }
        }
    }

    private void notifyAdmins(Users member, String planName, int limit, long minutes) {
        String title = "Member overstay";
        String message = member.getName() + " has been in the gym " + minutes + " min — over the "
                + limit + "-min limit of the " + planName + " plan — and hasn't checked out.";
        Users admin = member.getAdmin();
        if (admin != null) {
            notificationService.createForUser(admin, "ATTENDANCE", title, message, "/attendance");
            return;
        }
        // No linked admin — fall back to all admins / super admins.
        Stream.concat(userRepository.findByRole(Role.ADMIN).stream(),
                        userRepository.findByRole(Role.SUPER_ADMIN).stream())
                .forEach(a -> notificationService.createForUser(a, "ATTENDANCE", title, message, "/attendance"));
    }
}
