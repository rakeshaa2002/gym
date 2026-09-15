package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.AssignMembershipRequest;
import com.fitnexus.backend.dto.CreateOrderRequest;
import com.fitnexus.backend.dto.MembershipResponse;
import com.fitnexus.backend.dto.OrderResponse;
import com.fitnexus.backend.dto.MembershipRequestResponse;
import com.fitnexus.backend.dto.UpgradeMembershipRequest;
import com.fitnexus.backend.dto.VerifyPaymentRequest;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.MembershipPlan;
import com.fitnexus.backend.entity.MembershipRequest;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.MembershipPlanRepository;
import com.fitnexus.backend.repository.MembershipRequestRepository;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.entity.Transaction;
import com.fitnexus.backend.repository.TransactionRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImplementation {
    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipRequestRepository membershipRequestRepository;
    private final RazorpayService razorpayService;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @Value("${membership.premium-price:999}")
    private int premiumPrice; // rupees per month

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

    private MembershipResponse buildResponse(Users me) {
        if (me.getRole() != Role.USER) {
            return new MembershipResponse("STAFF", null, null, false, false,
                    null, "Staff", true, null, null, 0);
        }
        FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElse(null);
        MembershipPlan planRef = fu != null ? fu.getMembershipPlanRef() : null;
        String planCode = (fu != null && fu.getMembershipPlan() != null) ? fu.getMembershipPlan() : "BASIC";
        String planName = planRef != null ? planRef.getName() : planCode;
        LocalDate expiry = fu != null ? fu.getMembershipExpiry() : null;
        boolean active = expiry == null || !expiry.isBefore(LocalDate.now());
        boolean unlimited = planRef != null
                ? Boolean.TRUE.equals(planRef.getUnlimitedAccess())
                : "PREMIUM".equalsIgnoreCase(planCode);
        // Legacy "premium" flag: a non-free / unlimited tier that is still active.
        boolean premium = active && (unlimited || "PREMIUM".equalsIgnoreCase(planCode));
        Long daysLeft = (expiry != null && active) ? ChronoUnit.DAYS.between(LocalDate.now(), expiry) : null;
        String start = (fu != null && fu.getAccessStartTime() != null) ? fu.getAccessStartTime().format(HHMM) : null;
        String end = (fu != null && fu.getAccessEndTime() != null) ? fu.getAccessEndTime().format(HHMM) : null;
        Long planId = planRef != null ? planRef.getId() : null;
        int maxSession = (planRef != null && planRef.getMaxSessionMinutes() != null) ? planRef.getMaxSessionMinutes() : 0;
        return new MembershipResponse(planCode, expiry, daysLeft, premium, true,
                planId, planName, unlimited, unlimited ? null : start, unlimited ? null : end, maxSession);
    }

    public MembershipResponse getMyMembership() {
        return buildResponse(getCurrentUser());
    }

    /** Staff view of any member's membership (for the assign UI). */
    public MembershipResponse getMemberMembership(Long memberId) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to view memberships");
        }
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        return buildResponse(member);
    }

    /**
     * Staff assigns a plan and (for time-restricted plans) a daily check-in window
     * to a member. Notifies the member their plan/timing changed.
     */
    public MembershipResponse assignMembership(Long memberId, AssignMembershipRequest request) {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to assign memberships");
        }
        if (request == null || request.getPlanId() == null) {
            throw new IllegalArgumentException("A plan is required");
        }
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        if (member.getRole() != Role.USER) {
            throw new IllegalArgumentException("Only members can be assigned a membership");
        }
        MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));

        boolean unlimited = Boolean.TRUE.equals(plan.getUnlimitedAccess());
        LocalTime start = parseTime(request.getAccessStartTime());
        LocalTime end = parseTime(request.getAccessEndTime());
        if (!unlimited) {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Start and end time are required for this plan");
            }
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("End time must be after start time");
            }
        }

        FitnessUser fu = fitnessUserRepository.findById(member.getId()).orElseGet(() -> {
            FitnessUser n = new FitnessUser();
            n.setAccount(member);
            return n;
        });
        // Carry the member's existing paid amount forward as bonus days on the new plan
        // (admin upgrades aren't charged, so the unused value becomes extra time). Computed
        // BEFORE we overwrite the plan, since it reads the member's current plan.
        long creditDays = upgradeCreditDays(fu, plan);
        fu.setMembershipPlanRef(plan);
        fu.setMembershipPlan(plan.getCode());
        fu.setAccessStartTime(unlimited ? null : start);
        fu.setAccessEndTime(unlimited ? null : end);
        int months = (request.getMonths() != null && request.getMonths() > 0) ? request.getMonths() : 1;
        if (plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            fu.setMembershipExpiry(LocalDate.now().plusDays((long) plan.getDurationDays() * months + creditDays));
        } else {
            fu.setMembershipExpiry(null); // lifetime
        }
        fitnessUserRepository.save(fu);

        // Log transaction
        Transaction tx = new Transaction();
        tx.setMember(member);
        tx.setPlan(plan);
        tx.setAmount(plan.getPrice() != null ? (double) plan.getPrice() * months : 0.0);
        tx.setMonths(months);
        tx.setPaymentMethod("CASH");
        tx.setStatus("SUCCESS");
        tx.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(tx);

        // Assigning a plan resolves any pending upgrade request from this member.
        membershipRequestRepository.findByMember_IdAndStatus(member.getId(), "PENDING").forEach(this::resolve);

        String window = unlimited
                ? "unlimited access (any time)"
                : "check-in window " + start.format(HHMM) + "–" + end.format(HHMM);
        String credited = creditDays > 0 ? " (+" + creditDays + " days credited from your previous plan)" : "";
        notificationService.createForUser(member, "INFO", "Membership updated",
                "You are now on the " + plan.getName() + " plan with " + window + credited + ".", "/membership");

        return buildResponse(member);
    }

    /** Mock activation (no payment gateway): activates a plan and extends the expiry. */
    public MembershipResponse upgrade(UpgradeMembershipRequest request) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can hold a gym membership");
        }
        MembershipPlan plan = resolvePlanByCode(request != null ? request.getPlan() : null, "PREMIUM");
        return activatePlan(me, plan, normalizeMonths(request != null ? request.getMonths() : null), "MOCK_UPGRADE", null, null);
    }

    /** Creates a Razorpay order for the chosen plan and billing period (1/3/12 months). */
    public OrderResponse createOrder(CreateOrderRequest request) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can purchase a gym membership");
        }
        if (request == null || request.getPlanId() == null) {
            throw new IllegalArgumentException("A plan is required");
        }
        MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));
        if (plan.getPrice() == null || plan.getPrice() <= 0) {
            throw new IllegalArgumentException("This plan is free and cannot be purchased");
        }
        if (!razorpayService.isConfigured()) {
            throw new IllegalStateException("Online payment is not configured. Please contact the gym.");
        }
        int months = normalizeMonths(request.getMonths());
        long fullPrice = priceForPeriod(plan, months);
        // Credit the unused value of the member's current plan toward this upgrade.
        FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElse(null);
        long credit = upgradeCreditRupees(fu, plan);
        long payable = Math.max(0, fullPrice - credit);
        long amountPaise = Math.max(payable, 1) * 100L; // the gateway needs a positive amount
        String receipt = "mem_" + me.getId() + "_" + System.currentTimeMillis();
        String orderId = razorpayService.createOrder(amountPaise, "INR", receipt);
        String description = credit > 0
                ? plan.getName() + " upgrade — ₹" + fullPrice + " less ₹" + credit + " credit = ₹" + payable
                : plan.getName() + " Membership (" + periodLabel(months) + ")";
        return new OrderResponse(orderId, razorpayService.getKeyId(), amountPaise, "INR", months,
                "FitNexus Gym", description, fullPrice * 100L, credit * 100L);
    }

    /**
     * Unused rupee value of the member's current active paid plan, credited when they
     * switch to a <em>different</em> plan (upgrade). The remaining days are valued at the
     * current plan's daily rate. Returns 0 when there's nothing to credit (no plan, free
     * plan, expired, or simply renewing the same plan).
     */
    private long upgradeCreditRupees(FitnessUser fu, MembershipPlan newPlan) {
        if (fu == null) {
            return 0;
        }
        MembershipPlan current = fu.getMembershipPlanRef();
        LocalDate expiry = fu.getMembershipExpiry();
        if (current == null || expiry == null || current.getPrice() == null || current.getPrice() <= 0) {
            return 0;
        }
        // Same-plan renewals extend the expiry instead (handled in activatePlan) — no credit.
        if (newPlan != null && current.getId() != null && current.getId().equals(newPlan.getId())) {
            return 0;
        }
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
        if (daysLeft <= 0) {
            return 0;
        }
        int durationDays = (current.getDurationDays() != null && current.getDurationDays() > 0)
                ? current.getDurationDays() : 30;
        double dailyRate = (double) current.getPrice() / durationDays;
        return Math.round(daysLeft * dailyRate);
    }

    /** The unused value of the current plan, expressed as bonus days on the new plan. Used by
     *  admin-assisted upgrades (no payment), so the member's existing amount becomes extra time. */
    private long upgradeCreditDays(FitnessUser fu, MembershipPlan newPlan) {
        long creditRupees = upgradeCreditRupees(fu, newPlan);
        if (creditRupees <= 0 || newPlan == null || newPlan.getPrice() == null || newPlan.getPrice() <= 0) {
            return 0;
        }
        int newDuration = (newPlan.getDurationDays() != null && newPlan.getDurationDays() > 0)
                ? newPlan.getDurationDays() : 30;
        double newDailyRate = (double) newPlan.getPrice() / newDuration;
        if (newDailyRate <= 0) {
            return 0;
        }
        return Math.round(creditRupees / newDailyRate);
    }

    /**
     * A member asks staff to move them to a different plan (admin-assisted upgrade). Notifies
     * their admin / manager / assigned trainer (falling back to all admins) so staff can assign
     * the new plan on the Attendance page, where the existing paid time is credited automatically.
     */
    public void requestPlanChange(Long planId, String note) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can request a plan change");
        }
        MembershipPlan plan = planId != null ? membershipPlanRepository.findById(planId).orElse(null) : null;
        String planName = plan != null ? plan.getName() : "a different";

        // Persist (or update) a single PENDING request for this member so staff have an
        // actionable list, not just a transient notification.
        List<MembershipRequest> existing = membershipRequestRepository.findByMember_IdAndStatus(me.getId(), "PENDING");
        MembershipRequest req = existing.isEmpty() ? new MembershipRequest() : existing.get(0);
        req.setMember(me);
        req.setRequestedPlan(plan);
        req.setNote(note != null && !note.isBlank() ? note.trim() : null);
        req.setStatus("PENDING");
        if (req.getCreatedAt() == null) {
            req.setCreatedAt(LocalDateTime.now());
        }
        membershipRequestRepository.save(req);

        // Notifications are best-effort — never let a notification problem fail the
        // request itself (the persisted request above is what staff act on).
        try {
            String title = "Plan upgrade request";
            StringBuilder message = new StringBuilder(me.getName() + " requests an upgrade to the " + planName
                    + " plan; their current paid time will be credited. Approve it on the Attendance page.");
            if (note != null && !note.isBlank()) {
                message.append(" Note: ").append(note.trim());
            }
            String body = message.toString();

            java.util.Set<Long> notified = new java.util.HashSet<>();
            notifyStaff(me.getAdmin(), notified, title, body);
            notifyStaff(me.getManager(), notified, title, body);
            FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElse(null);
            if (fu != null && fu.getAssignedTrainer() != null) {
                notifyStaff(fu.getAssignedTrainer().getAccount(), notified, title, body);
            }
            // Always also alert all admins/super-admins so the request never goes unseen.
            userRepository.findByRole(Role.SUPER_ADMIN).forEach(a -> notifyStaff(a, notified, title, body));
            userRepository.findByRole(Role.ADMIN).forEach(a -> notifyStaff(a, notified, title, body));
        } catch (Exception ignored) {
            // request is already saved; staff will still see it in the requests list
        }
    }

    /** Staff: list members' pending plan-upgrade requests (newest first). */
    public List<MembershipRequestResponse> getPendingRequests() {
        Users me = getCurrentUser();
        if (!isStaff(me.getRole())) {
            throw new SecurityException("Only staff can view plan requests");
        }
        return membershipRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING").stream()
                .map(r -> {
                    Users m = r.getMember();
                    FitnessUser fu = m != null ? fitnessUserRepository.findById(m.getId()).orElse(null) : null;
                    String currentPlan = fu != null && fu.getMembershipPlanRef() != null
                            ? fu.getMembershipPlanRef().getName()
                            : (fu != null && fu.getMembershipPlan() != null ? fu.getMembershipPlan() : "Basic");
                    return new MembershipRequestResponse(
                            r.getId(),
                            m != null ? m.getId() : null,
                            m != null ? m.getName() : "Member",
                            m != null ? m.getEmail() : null,
                            currentPlan,
                            r.getRequestedPlan() != null ? r.getRequestedPlan().getId() : null,
                            r.getRequestedPlan() != null ? r.getRequestedPlan().getName() : "Higher plan",
                            r.getNote(),
                            r.getCreatedAt() != null ? r.getCreatedAt().format(STAMP) : "");
                })
                .toList();
    }

    /** Staff: dismiss a pending request without assigning a plan. Notifies the member. */
    public void dismissRequest(Long requestId) {
        Users me = getCurrentUser();
        if (!isStaff(me.getRole())) {
            throw new SecurityException("Only staff can manage plan requests");
        }
        membershipRequestRepository.findById(requestId).ifPresent(req -> {
            resolve(req);
            // Best-effort: tell the member their request was reviewed and not approved.
            try {
                String planName = req.getRequestedPlan() != null ? req.getRequestedPlan().getName() : "the requested";
                notificationService.createForUser(req.getMember(), "INFO", "Plan request declined",
                        "Your request to switch to the " + planName + " plan was not approved. "
                                + "Contact your gym staff for details.", "/membership");
            } catch (Exception ignored) {
                // dismissal is already persisted; a notification failure must not undo it
            }
        });
    }

    private void resolve(MembershipRequest req) {
        req.setStatus("RESOLVED");
        req.setResolvedAt(LocalDateTime.now());
        membershipRequestRepository.save(req);
    }

    private void notifyStaff(Users staff, java.util.Set<Long> notified, String title, String message) {
        if (staff == null || staff.getId() == null || notified.contains(staff.getId())) {
            return;
        }
        notified.add(staff.getId());
        notificationService.createForUser(staff, "INFO", title, message, "/attendance");
    }

    /** Verifies the Razorpay payment signature and, if valid, activates the purchased plan. */
    public MembershipResponse verifyAndActivate(VerifyPaymentRequest request) {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can purchase a gym membership");
        }
        if (request == null || !razorpayService.verifySignature(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            throw new SecurityException("Payment verification failed");
        }
        MembershipPlan plan = resolvePlan(request.getPlanId(), "PREMIUM");
        return activatePlan(me, plan, normalizeMonths(request.getMonths()), "RAZORPAY", request.getRazorpayOrderId(), request.getRazorpayPaymentId());
    }

    /** Puts the member on a plan and extends their expiry by the billing period. */
    private MembershipResponse activatePlan(Users me, MembershipPlan plan, int months, String method, String rzpOrderId, String rzpPaymentId) {
        FitnessUser fu = fitnessUserRepository.findById(me.getId()).orElseGet(() -> {
            FitnessUser n = new FitnessUser();
            n.setAccount(me);
            return n;
        });
        // Renewing the SAME plan extends from the current expiry. Switching plans (an
        // upgrade) starts fresh from today, because the unused value of the old plan was
        // already cashed out as a credit at checkout (see upgradeCreditRupees).
        boolean samePlan = fu.getMembershipPlanRef() != null && plan.getId() != null
                && plan.getId().equals(fu.getMembershipPlanRef().getId());
        LocalDate base = (samePlan && fu.getMembershipExpiry() != null
                && !fu.getMembershipExpiry().isBefore(LocalDate.now()))
                ? fu.getMembershipExpiry() : LocalDate.now();
        fu.setMembershipPlanRef(plan);
        fu.setMembershipPlan(plan.getCode());
        fu.setMembershipExpiry(base.plusMonths(months));
        if (Boolean.TRUE.equals(plan.getUnlimitedAccess())) {
            fu.setAccessStartTime(null);
            fu.setAccessEndTime(null);
        }
        fitnessUserRepository.save(fu);

        // Log transaction
        Transaction tx = new Transaction();
        tx.setMember(me);
        tx.setPlan(plan);
        tx.setAmount(plan.getPrice() != null ? (double) priceForPeriod(plan, months) : 0.0);
        tx.setMonths(months);
        tx.setPaymentMethod(method);
        tx.setStatus("SUCCESS");
        tx.setTransactionDate(LocalDateTime.now());
        tx.setRazorpayOrderId(rzpOrderId);
        tx.setRazorpayPaymentId(rzpPaymentId);
        transactionRepository.save(tx);

        notificationService.createForUser(me, "INFO", "Membership activated",
                "Your " + plan.getName() + " plan is active for " + periodLabel(months) + ".", "/membership");
        return buildResponse(me);
    }

    private MembershipPlan resolvePlan(Long id, String fallbackCode) {
        if (id != null) {
            return membershipPlanRepository.findById(id)
                    .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));
        }
        return membershipPlanRepository.findByCodeIgnoreCase(fallbackCode)
                .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));
    }

    private MembershipPlan resolvePlanByCode(String code, String fallbackCode) {
        String c = (code != null && !code.isBlank()) ? code : fallbackCode;
        return membershipPlanRepository.findByCodeIgnoreCase(c)
                .orElseThrow(() -> new InvalidOperationException("Membership plan not found"));
    }

    private int normalizeMonths(Integer months) {
        if (months == null || months <= 0) {
            return 1;
        }
        return months; // 1 monthly, 3 quarterly, 12 yearly (any positive value accepted)
    }

    /** Discounted total price (in rupees) for the whole billing period. */
    private long priceForPeriod(MembershipPlan plan, int months) {
        long base = (long) plan.getPrice() * months;
        double discount = switch (months) {
            case 3 -> 0.10;   // quarterly: 10% off
            case 12 -> 0.20;  // yearly: 20% off
            default -> 0.0;
        };
        return Math.round(base * (1 - discount));
    }

    private String periodLabel(int months) {
        return switch (months) {
            case 1 -> "1 month";
            case 3 -> "3 months";
            case 12 -> "12 months";
            default -> months + " months";
        };
    }

    public MembershipResponse cancel() {
        Users me = getCurrentUser();
        if (me.getRole() != Role.USER) {
            throw new SecurityException("Only members can hold a gym membership");
        }
        fitnessUserRepository.findById(me.getId()).ifPresent(fu -> {
            fu.setMembershipPlan("BASIC");
            membershipPlanRepository.findByCodeIgnoreCase("BASIC").ifPresent(fu::setMembershipPlanRef);
            fu.setMembershipExpiry(null);
            fitnessUserRepository.save(fu);
        });
        return buildResponse(me);
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time '" + value + "', expected HH:mm");
        }
    }
}
