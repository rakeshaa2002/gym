package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.AdminOverviewResponse;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.AttendanceRepository;
import com.fitnexus.backend.repository.DietPlanRepository;
import com.fitnexus.backend.repository.ExerciseRepository;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.repository.WorkoutPlanRepository;
import com.fitnexus.backend.entity.Transaction;
import com.fitnexus.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImplementation {
    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final AttendanceRepository attendanceRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final DietPlanRepository dietPlanRepository;
    private final TransactionRepository transactionRepository;

    private static final List<Role> STAFF_ROLES =
            List.of(Role.SUPER_ADMIN, Role.ADMIN, Role.MANAGER, Role.TRAINER);

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private boolean isStaff(Role role) {
        return STAFF_ROLES.contains(role);
    }

    /** Fitness users visible to the requester. */
    private List<FitnessUser> visibleFitnessUsers(Users requester) {
        return switch (requester.getRole()) {
            case SUPER_ADMIN -> fitnessUserRepository.findAll();
            case ADMIN -> fitnessUserRepository.findByAccount_Admin_Id(requester.getId());
            case MANAGER -> fitnessUserRepository.findByAccount_Manager_Id(requester.getId());
            case TRAINER -> java.util.stream.Stream.concat(
                    fitnessUserRepository.findByAssignedTrainer_Account_Id(requester.getId()).stream(),
                    java.util.stream.Stream.concat(
                            fitnessUserRepository.findByAccount_Trainer_Id(requester.getId()).stream(),
                            fitnessUserRepository.findByAccount_CreatedBy_Id(requester.getId()).stream()))
                    .collect(Collectors.toMap(FitnessUser::getId, u -> u, (a, b) -> a, java.util.LinkedHashMap::new))
                    .values().stream().toList();
            default -> List.of();
        };
    }

    /** Customer accounts visible to the requester — same hierarchy rule as the Users list. */
    private List<Users> visibleMemberAccounts(Users requester) {
        return visibleFitnessUsers(requester).stream()
                .map(FitnessUser::getAccount)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public AdminOverviewResponse getOverview() {
        Users requester = getCurrentUser();
        if (!isStaff(requester.getRole())) {
            throw new SecurityException("You do not have permission to view the admin overview");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        // Member stats are scoped to what this requester can see (same rule as the
        // Users list), so the dashboard and the Users list agree.
        List<FitnessUser> fitnessUsers = visibleFitnessUsers(requester);
        List<Users> members = fitnessUsers.stream()
                .map(FitnessUser::getAccount)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        long totalMembers = members.size();
        long activeMembers = members.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long inactiveMembers = totalMembers - activeMembers;
        long newMembersThisMonth = members.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth)).count();
        long totalStaff = userRepository.countByRoleIn(STAFF_ROLES);

        long checkedInToday = attendanceRepository.countByAttendanceDate(today);
        long currentlyInGym = attendanceRepository.countByAttendanceDateAndCheckOutTimeIsNull(today);

        // 7-day attendance trend, zero-filled.
        Map<LocalDate, Long> grouped = new HashMap<>();
        for (Object[] row : attendanceRepository.countGroupedByDateSince(today.minusDays(6))) {
            grouped.put((LocalDate) row[0], (Long) row[1]);
        }
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE");
        List<AdminOverviewResponse.DayCount> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            trend.add(new AdminOverviewResponse.DayCount(
                    day.format(dayFmt), grouped.getOrDefault(day, 0L)));
        }

        // Financial metrics calculations
        LocalDate lowerBound = today.minusDays(15);
        LocalDate upperBound = today.plusDays(15);

        double renewalRevenueDue = fitnessUsers.stream()
                .filter(u -> u.getMembershipExpiry() != null &&
                             !u.getMembershipExpiry().isBefore(lowerBound) &&
                             !u.getMembershipExpiry().isAfter(upperBound))
                .mapToDouble(u -> u.getMembershipPlanRef() != null ? u.getMembershipPlanRef().getPrice() : 0.0)
                .sum();

        List<Long> memberIds = members.stream().map(Users::getId).collect(Collectors.toList());
        List<Transaction> transactions = transactionRepository.findAllWithPlanAndMember();
        List<Transaction> visibleTransactions = transactions.stream()
                .filter(t -> t.getMember() != null && memberIds.contains(t.getMember().getId()))
                .collect(Collectors.toList());


        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        double revenueToday = visibleTransactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()) &&
                             t.getTransactionDate() != null &&
                             !t.getTransactionDate().isBefore(startOfToday) &&
                             t.getTransactionDate().isBefore(endOfToday))
                .mapToDouble(Transaction::getAmount)
                .sum();

        LocalDateTime startOfThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        double revenueThisMonth = visibleTransactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()) &&
                             t.getTransactionDate() != null &&
                             !t.getTransactionDate().isBefore(startOfThisMonth) &&
                             t.getTransactionDate().isBefore(endOfThisMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double pendingCollections = visibleTransactions.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double ptRevenue = visibleTransactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()) &&
                             t.getPlan() != null &&
                             (t.getPlan().getName().toUpperCase().contains("PT") ||
                              t.getPlan().getName().toUpperCase().contains("PERSONAL TRAINING") ||
                              t.getPlan().getCode().toUpperCase().contains("PT") ||
                              t.getPlan().getCode().toUpperCase().contains("PERSONAL")))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double supplementRevenue = visibleTransactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()) &&
                             t.getPlan() != null &&
                             (t.getPlan().getName().toUpperCase().contains("SUPPLEMENT") ||
                              t.getPlan().getName().toUpperCase().contains("PROTEIN") ||
                              t.getPlan().getName().toUpperCase().contains("WHEY") ||
                              t.getPlan().getCode().toUpperCase().contains("SUPPLEMENT") ||
                              t.getPlan().getCode().toUpperCase().contains("PROTEIN") ||
                              t.getPlan().getCode().toUpperCase().contains("WHEY")))
                .mapToDouble(Transaction::getAmount)
                .sum();

        List<AdminOverviewResponse.RecentMember> recentMembers =
                members.stream()
                        .sorted(java.util.Comparator.comparing(
                                Users::getCreatedAt,
                                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                        .limit(5)
                        .map(u -> new AdminOverviewResponse.RecentMember(
                                u.getId(), u.getName(), u.getEmail(),
                                u.getIsActive(), u.getCreatedAt()))
                        .collect(Collectors.toList());

        return new AdminOverviewResponse(
                totalMembers, activeMembers, inactiveMembers, newMembersThisMonth, totalStaff,
                checkedInToday, currentlyInGym, trend,
                workoutPlanRepository.count(), exerciseRepository.count(), dietPlanRepository.count(),
                revenueToday, revenueThisMonth, pendingCollections, renewalRevenueDue, ptRevenue, supplementRevenue,
                recentMembers);
    }
}
