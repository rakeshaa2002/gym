package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.AccessDecisionResponse;
import com.fitnexus.backend.dto.AttendanceResponse;
import com.fitnexus.backend.dto.DeviceCheckInRequest;
import com.fitnexus.backend.entity.Attendance;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Trainer;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.AttendanceRepository;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImplementation {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final NotificationService notificationService;

    @Value("${attendance.device-key:fitnexus-device-key-2026}")
    private String deviceKey;

    /** Minutes of leeway on either side of the assigned check-in window. */
    @Value("${attendance.access-grace-minutes:0}")
    private int graceMinutes;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private boolean isStaff(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.MANAGER || role == Role.TRAINER;
    }

    private AttendanceResponse toResponse(Attendance a) {
        Users u = a.getUser();
        return new AttendanceResponse(
                a.getId(),
                u == null ? null : u.getId(),
                u == null ? null : u.getName(),
                u == null ? null : u.getEmail(),
                a.getAttendanceDate(),
                a.getCheckInTime(),
                a.getCheckOutTime(),
                a.getMethod(),
                a.getDeviceId(),
                a.getStatus()
        );
    }

    /**
     * Core toggle: an open record (no check-out) becomes a check-out; otherwise a
     * new check-in is created. Returns the access decision for the door/turnstile.
     */
    private AccessDecisionResponse recordScan(Users member, String method, String deviceId) {
        if (member == null) {
            return new AccessDecisionResponse(false, false, "DENIED", null, null, LocalDateTime.now(),
                    "Access denied: member not recognized");
        }
        if (Boolean.FALSE.equals(member.getIsActive())) {
            return new AccessDecisionResponse(false, false, "DENIED", member.getId(), member.getName(), LocalDateTime.now(),
                    "Access denied: membership is inactive");
        }

        LocalDateTime now = LocalDateTime.now();
        Attendance open = attendanceRepository
                .findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(member)
                .orElse(null);

        // An open record always means this scan is an exit — never blocked.
        if (open != null) {
            open.setCheckOutTime(now);
            open.setStatus("CHECKED_OUT");
            open.setUpdatedAt(now);
            attendanceRepository.save(open);
            return new AccessDecisionResponse(true, true, "CHECK_OUT", member.getId(), member.getName(), now,
                    "Goodbye, " + member.getName() + "! Checked out.");
        }

        // Check-in: members may only enter within their assigned plan timing.
        AccessCheck access = checkAccess(member, now.toLocalTime());
        if (access.denial() != null) {
            if (access.missingSlot()) {
                notifyStaffNeedSlot(member); // ask admin/manager to assign a window
            }
            return new AccessDecisionResponse(false, false, "DENIED", member.getId(), member.getName(), now, access.denial());
        }

        Attendance entry = new Attendance();
        entry.setUser(member);
        entry.setAttendanceDate(LocalDate.now());
        entry.setCheckInTime(now);
        entry.setMethod(method == null ? "FINGERPRINT" : method);
        entry.setDeviceId(deviceId);
        entry.setStatus("CHECKED_IN");
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        attendanceRepository.save(entry);

        notifyTrainerOfCheckIn(member, now);

        return new AccessDecisionResponse(true, true, "CHECK_IN", member.getId(), member.getName(), now,
                "Welcome, " + member.getName() + "! Gym open.");
    }

    /** Outcome of the check-in time check: a denial message (null = allowed) and whether
     *  the reason was a missing slot (so we can alert staff to assign one). */
    private record AccessCheck(String denial, boolean missingSlot) {
        static final AccessCheck ALLOWED = new AccessCheck(null, false);
    }

    /**
     * Decides whether a member may check in right now. Staff and unlimited-access
     * (Premium) members can enter any time. Every other plan is restricted to its
     * assigned daily slot: outside that slot — or with no slot assigned yet —
     * check-in is rejected.
     */
    private AccessCheck checkAccess(Users member, LocalTime now) {
        if (member.getRole() != Role.USER) {
            return AccessCheck.ALLOWED;
        }
        FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElse(null);
        MembershipPlan plan = fu != null ? fu.getMembershipPlanRef() : null;
        boolean unlimited = plan != null
                ? Boolean.TRUE.equals(plan.getUnlimitedAccess())
                : (fu != null && "PREMIUM".equalsIgnoreCase(fu.getMembershipPlan()));
        if (unlimited) {
            return AccessCheck.ALLOWED; // Premium / unlimited — any time, any day.
        }
        if (fu == null || fu.getAccessStartTime() == null || fu.getAccessEndTime() == null) {
            // Restricted plan but no window set — staff must assign one first.
            return new AccessCheck(
                    "No check-in time slot is assigned to you yet. "
                            + "Please contact your admin to set up your slot timing.",
                    true);
        }
        LocalTime start = fu.getAccessStartTime();
        LocalTime end = fu.getAccessEndTime();
        boolean within = !now.isBefore(start.minusMinutes(graceMinutes))
                && !now.isAfter(end.plusMinutes(graceMinutes));
        if (within) {
            return AccessCheck.ALLOWED;
        }
        return new AccessCheck("Wrong check-in time. Your slot is "
                + start.format(HHMM) + "–" + end.format(HHMM)
                + ". Please come during your slot, or contact your admin to change it.", false);
    }

    /**
     * Alerts the member's admin / manager / assigned trainer that the member tried to
     * check in but has no time slot, so they can assign one. Throttled to once every
     * 6 hours per member to avoid spamming on repeated attempts.
     */
    private void notifyStaffNeedSlot(Users member) {
        try {
            FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElse(null);
            if (fu == null) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            if (fu.getLastSlotRequestAt() != null && fu.getLastSlotRequestAt().isAfter(now.minusHours(6))) {
                return; // already alerted recently
            }
            String planName = fu.getMembershipPlanRef() != null ? fu.getMembershipPlanRef().getName() : "their";
            String title = "Member needs a time slot";
            String message = member.getName() + " tried to check in but has no time slot assigned on the "
                    + planName + " plan. Assign one on the Attendance page so they can enter.";

            java.util.Set<Long> notified = new java.util.HashSet<>();
            notifyStaff(member.getAdmin(), notified, title, message);
            notifyStaff(member.getManager(), notified, title, message);
            if (fu.getAssignedTrainer() != null) {
                notifyStaff(fu.getAssignedTrainer().getAccount(), notified, title, message);
            }
            if (notified.isEmpty()) {
                // No one linked — fall back to all admins.
                userRepository.findByRole(Role.ADMIN)
                        .forEach(a -> notifyStaff(a, notified, title, message));
            }

            fu.setLastSlotRequestAt(now);
            fitnessUserRepository.save(fu);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Slot-request notification failed", e);
        }
    }

    private void notifyStaff(Users recipient, java.util.Set<Long> notified, String title, String message) {
        if (recipient != null && recipient.getId() != null && notified.add(recipient.getId())) {
            notificationService.createForUser(recipient, "ATTENDANCE", title, message, "/attendance");
        }
    }

    /** Notifies the member's assigned trainer that they have just checked in. */
    private void notifyTrainerOfCheckIn(Users member, LocalDateTime when) {
        try {
            FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElse(null);
            Users trainerAccount = null;
            if (fu != null && fu.getAssignedTrainer() != null) {
                Trainer t = fu.getAssignedTrainer();
                trainerAccount = t.getAccount();
            }
            if (trainerAccount == null) {
                trainerAccount = member.getTrainer();
            }
            if (trainerAccount == null) {
                return;
            }
            notificationService.createForUser(trainerAccount, "ATTENDANCE", "Member checked in",
                    member.getName() + " checked in at " + when.toLocalTime().format(HHMM) + ".", "/attendance");
        } catch (Exception e) {
            // A notification failure must never block the gate.
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Trainer check-in notification failed", e);
        }
    }

    /** Called by the fingerprint terminal (public endpoint, guarded by a device key). */
    public AccessDecisionResponse deviceCheckIn(DeviceCheckInRequest request, String providedKey) {
        if (deviceKey != null && !deviceKey.isBlank() && !deviceKey.equals(providedKey)) {
            throw new SecurityException("Invalid device key");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Users member = null;
        if (request.getFingerprintId() != null && !request.getFingerprintId().isBlank()) {
            member = userRepository.findByFingerprintId(request.getFingerprintId().trim()).orElse(null);
        }
        if (member == null && request.getUserId() != null) {
            member = userRepository.findById(request.getUserId()).orElse(null);
        }
        if (member == null && request.getEmail() != null && !request.getEmail().isBlank()) {
            member = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
        }

        return recordScan(member, "FINGERPRINT", request.getDeviceId());
    }

    /**
     * Kiosk self check-in at the entrance screen. The member identifies themselves
     * by fingerprint ID, email, or member ID; staff is logged in on the kiosk machine.
     */
    public AccessDecisionResponse kioskCheckIn(String identifier) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to run the check-in kiosk");
        }
        if (identifier == null || identifier.isBlank()) {
            return new AccessDecisionResponse(false, false, "DENIED", null, null, LocalDateTime.now(),
                    "Please scan or enter your member ID");
        }
        String value = identifier.trim();

        // Resolve by fingerprint ID, member code, numeric id, finally email.
        Users member = userRepository.findByFingerprintId(value).orElse(null);
        // Member code like "FNX00028" (alpha prefix + account id).
        if (member == null && value.toUpperCase().startsWith("FNX")) {
            String digits = value.replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                member = userRepository.findById(Long.parseLong(digits)).orElse(null);
            }
        }
        if (member == null && value.matches("\\d+")) {
            member = userRepository.findById(Long.parseLong(value)).orElse(null);
        }
        if (member == null) {
            member = userRepository.findByEmail(value).orElse(null);
        }
        return recordScan(member, "KIOSK", null);
    }

    /** Member self check-in/out from the app (PIN/app method) — records for the logged-in user. */
    public AccessDecisionResponse selfCheckIn() {
        Users me = getCurrentUser();
        return recordScan(me, "APP", null);
    }

    /** Staff records attendance manually (kiosk backup / no hardware). */
    public AccessDecisionResponse manualCheckIn(Long memberId) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to record attendance");
        }
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        return recordScan(member, "MANUAL", null);
    }

    public void enrollFingerprint(Long memberId, String fingerprintId) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to enroll members");
        }
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        String value = fingerprintId == null || fingerprintId.isBlank() ? null : fingerprintId.trim();
        if (value != null) {
            userRepository.findByFingerprintId(value)
                    .filter(other -> !Objects.equals(other.getId(), member.getId()))
                    .ifPresent(other -> { throw new IllegalArgumentException("This fingerprint ID is already enrolled for another member"); });
        }
        member.setFingerprintId(value);
        userRepository.save(member);
    }

    public List<AttendanceResponse> getMyAttendance() {
        Users me = getCurrentUser();
        return attendanceRepository.findByUserOrderByCheckInTimeDesc(me).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * All active members, for the staff enrollment/kiosk dropdowns. Unlike the
     * hierarchy-scoped customer list, any staff at the desk can enroll any member.
     */
    public List<com.fitnexus.backend.dto.AttendanceMemberResponse> getEnrollableMembers() {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to view members");
        }
        return userRepository.findByRole(Role.USER).stream()
                .filter(u -> !Boolean.FALSE.equals(u.getIsActive()))
                .sorted(java.util.Comparator.comparing(u -> u.getName() == null ? "" : u.getName().toLowerCase()))
                .map(u -> new com.fitnexus.backend.dto.AttendanceMemberResponse(u.getId(), u.getName(), u.getEmail()))
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAll(LocalDate date) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to view attendance records");
        }
        List<Attendance> records = (date != null)
                ? attendanceRepository.findByAttendanceDateOrderByCheckInTimeDesc(date)
                : attendanceRepository.findAllByOrderByCheckInTimeDesc();
        return records.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AttendanceResponse> getToday() {
        return getAll(LocalDate.now());
    }
}
