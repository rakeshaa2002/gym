package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.schedule.ScheduleUserSummary;
import com.fitnexus.backend.dto.schedule.ScheduleWorkoutPlanSummary;
import com.fitnexus.backend.dto.schedule.ScheduleWorkoutTypeSummary;
import com.fitnexus.backend.dto.schedule.UserWorkoutScheduleRequest;
import com.fitnexus.backend.dto.schedule.UserWorkoutScheduleResponse;
import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.UserWorkoutSchedule;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.entity.WorkoutPlan;
import com.fitnexus.backend.entity.WorkoutType;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.UserRepository;
import com.fitnexus.backend.repository.UserWorkoutScheduleRepository;
import com.fitnexus.backend.repository.WorkoutPlanRepository;
import com.fitnexus.backend.repository.WorkoutTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWorkoutScheduleServiceImplementation {
    private final UserWorkoutScheduleRepository userWorkoutScheduleRepository;
    private final UserRepository userRepository;
    private final FitnessUserRepository fitnessUserRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutTypeRepository workoutTypeRepository;

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private boolean isManagerOrAbove(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.MANAGER;
    }

    private boolean isManageRole(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.MANAGER || role == Role.TRAINER;
    }

    private boolean canAccessUser(Users requester, Users user) {
        if (requester.getRole() == Role.SUPER_ADMIN) return true;
        if (requester.getRole() == Role.ADMIN) {
            return user.getAdmin() != null && Objects.equals(user.getAdmin().getId(), requester.getId());
        }
        if (requester.getRole() == Role.MANAGER) {
            return user.getManager() != null && Objects.equals(user.getManager().getId(), requester.getId());
        }
        if (requester.getRole() == Role.TRAINER) {
            return isTrainerOwnedCustomer(user.getId(), requester.getId());
        }
        return false;
    }

    private boolean isTrainerOwnedCustomer(Long userId, Long trainerId) {
        if (userId == null || trainerId == null) {
            return false;
        }

        return fitnessUserRepository.findById(userId)
                .map(customer -> {
                    if (customer.getAssignedTrainer() != null && Objects.equals(customer.getAssignedTrainer().getId(), trainerId)) {
                        return true;
                    }

                    Users account = customer.getAccount();
                    if (account == null) {
                        return false;
                    }

                    if (account.getTrainer() != null && Objects.equals(account.getTrainer().getId(), trainerId)) {
                        return true;
                    }

                    return account.getCreatedBy() != null && Objects.equals(account.getCreatedBy().getId(), trainerId);
                })
                .orElse(false);
    }

    private Users resolveTrainer(Long trainerId, Users requester) {
        Long targetId = trainerId;
        if (requester.getRole() == Role.TRAINER) {
            targetId = requester.getId();
        }
        if (targetId == null) {
            throw new IllegalArgumentException("Trainer is required");
        }

        Users trainer = userRepository.findById(targetId)
                .orElseThrow(() -> new InvalidOperationException("Trainer not found"));
        if (trainer.getRole() != Role.TRAINER) {
            throw new IllegalArgumentException("Selected user is not a trainer");
        }
        if (requester.getRole() != Role.SUPER_ADMIN && !canAccessTrainer(requester, trainer)) {
            throw new SecurityException("You do not have permission to assign this trainer");
        }
        return trainer;
    }

    private boolean canAccessTrainer(Users requester, Users trainer) {
        if (requester.getRole() == Role.SUPER_ADMIN) return true;
        if (requester.getRole() == Role.ADMIN) {
            return trainer.getAdmin() != null && Objects.equals(trainer.getAdmin().getId(), requester.getId());
        }
        if (requester.getRole() == Role.MANAGER) {
            return trainer.getManager() != null && Objects.equals(trainer.getManager().getId(), requester.getId());
        }
        if (requester.getRole() == Role.TRAINER) {
            return Objects.equals(trainer.getId(), requester.getId());
        }
        return false;
    }

    private Users resolveUser(Long userId, Users requester) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidOperationException("User not found"));
        if (user.getRole() != Role.USER) {
            throw new IllegalArgumentException("Selected account is not a user");
        }
        if (!canAccessUser(requester, user)) {
            throw new SecurityException("You do not have permission to assign this user");
        }
        return user;
    }

    private WorkoutPlan resolveWorkoutPlan(Long workoutPlanId) {
        if (workoutPlanId == null) {
            throw new IllegalArgumentException("Workout plan is required");
        }
        return workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new InvalidOperationException("Workout plan not found"));
    }

    private WorkoutType resolveWorkoutType(Long workoutTypeId, WorkoutPlan workoutPlan) {
        if (workoutTypeId != null) {
            return workoutTypeRepository.findById(workoutTypeId)
                    .orElseThrow(() -> new InvalidOperationException("Workout type not found"));
        }
        if (workoutPlan != null && workoutPlan.getExercises() != null) {
            return workoutPlan.getExercises().stream()
                    .map(exercise -> exercise.getWorkoutType())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private ScheduleUserSummary toUserSummary(Users user) {
        if (user == null) return null;
        return new ScheduleUserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                user.getBranchId()
        );
    }

    private ScheduleWorkoutPlanSummary toPlanSummary(WorkoutPlan plan) {
        if (plan == null) return null;
        return new ScheduleWorkoutPlanSummary(
                plan.getId(),
                plan.getName(),
                plan.getMainImage()
        );
    }

    private ScheduleWorkoutTypeSummary toWorkoutTypeSummary(WorkoutType workoutType) {
        if (workoutType == null) return null;
        return new ScheduleWorkoutTypeSummary(workoutType.getId(), workoutType.getName());
    }

    private UserWorkoutScheduleResponse toResponse(UserWorkoutSchedule schedule) {
        return new UserWorkoutScheduleResponse(
                schedule.getId(),
                toUserSummary(schedule.getTrainer()),
                toUserSummary(schedule.getUser()),
                toPlanSummary(schedule.getWorkoutPlan()),
                toWorkoutTypeSummary(schedule.getWorkoutType()),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartDateTime(),
                schedule.getEndDateTime(),
                schedule.getRepeatType(),
                schedule.getLocation(),
                schedule.getCompletionStatus(),
                schedule.getNotes(),
                schedule.getStatus(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }

    private void copyFields(UserWorkoutScheduleRequest request, UserWorkoutSchedule target, Users requester) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            throw new IllegalArgumentException("Start and end time are required");
        }
        if (request.getEndDateTime().isBefore(request.getStartDateTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Users trainer = resolveTrainer(request.getTrainerId(), requester);
        Users user = resolveUser(request.getUserId(), requester);
        WorkoutPlan workoutPlan = resolveWorkoutPlan(request.getWorkoutPlanId());
        WorkoutType workoutType = resolveWorkoutType(request.getWorkoutTypeId(), workoutPlan);

        if (workoutType == null) {
            throw new IllegalArgumentException("Workout type is required");
        }

        target.setTrainer(trainer);
        target.setUser(user);
        target.setWorkoutPlan(workoutPlan);
        target.setWorkoutType(workoutType);
        target.setTitle(request.getTitle().trim());
        target.setDescription(request.getDescription());
        target.setStartDateTime(request.getStartDateTime());
        target.setEndDateTime(request.getEndDateTime());
        target.setRepeatType(normalizeText(request.getRepeatType(), "None"));
        target.setLocation(request.getLocation());
        target.setCompletionStatus(normalizeText(request.getCompletionStatus(), "PENDING"));
        target.setNotes(request.getNotes());
        target.setStatus(normalizeText(request.getStatus(), "ACTIVE"));
        target.setUpdatedAt(LocalDateTime.now());
    }

    private String normalizeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private boolean isVisibleToRequester(Users requester, UserWorkoutSchedule schedule) {
        if (requester.getRole() == Role.SUPER_ADMIN) return true;
        if (requester.getRole() == Role.ADMIN) {
            return schedule.getUser() != null && schedule.getUser().getAdmin() != null && Objects.equals(schedule.getUser().getAdmin().getId(), requester.getId());
        }
        if (requester.getRole() == Role.MANAGER) {
            return schedule.getUser() != null && schedule.getUser().getManager() != null && Objects.equals(schedule.getUser().getManager().getId(), requester.getId());
        }
        if (requester.getRole() == Role.TRAINER) {
            return schedule.getUser() != null && isTrainerOwnedCustomer(schedule.getUser().getId(), requester.getId());
        }
        return requester.getRole() == Role.USER && schedule.getUser() != null && Objects.equals(schedule.getUser().getId(), requester.getId());
    }

    public List<UserWorkoutScheduleResponse> getAll() {
        Users requester = getCurrentUser();
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to view workout schedules");
        }
        return userWorkoutScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                .filter(schedule -> isVisibleToRequester(requester, schedule))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserWorkoutScheduleResponse> getMyCreated() {
        Users requester = getCurrentUser();
        if (requester.getRole() == Role.TRAINER) {
            return userWorkoutScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                    .filter(schedule -> schedule.getTrainer() != null && Objects.equals(schedule.getTrainer().getId(), requester.getId()))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to view created workout schedules");
        }
        return getAll();
    }

    public List<UserWorkoutScheduleResponse> getMyUsers() {
        Users requester = getCurrentUser();
        if (requester.getRole() == Role.TRAINER) {
            return userWorkoutScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                    .filter(schedule -> schedule.getUser() != null && isTrainerOwnedCustomer(schedule.getUser().getId(), requester.getId()))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to view user workout schedules");
        }
        return getAll();
    }

    public List<UserWorkoutScheduleResponse> getMySchedule() {
        Users requester = getCurrentUser();
        if (requester.getRole() != Role.USER) {
            throw new SecurityException("Only users can access their personal schedule");
        }
        return userWorkoutScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                .filter(schedule -> schedule.getUser() != null && Objects.equals(schedule.getUser().getId(), requester.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserWorkoutScheduleResponse create(UserWorkoutScheduleRequest request) {
        Users requester = getCurrentUser();
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to create workout schedules");
        }
        UserWorkoutSchedule entity = new UserWorkoutSchedule();
        entity.setCreatedAt(LocalDateTime.now());
        copyFields(request, entity, requester);
        return toResponse(userWorkoutScheduleRepository.save(entity));
    }

    public UserWorkoutScheduleResponse update(Long id, UserWorkoutScheduleRequest request) {
        Users requester = getCurrentUser();
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to update workout schedules");
        }
        UserWorkoutSchedule entity = userWorkoutScheduleRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Workout schedule not found"));
        copyFields(request, entity, requester);
        return toResponse(userWorkoutScheduleRepository.save(entity));
    }

    public void delete(Long id) {
        Users requester = getCurrentUser();
        if (!isManageRole(requester.getRole())) {
            throw new SecurityException("You do not have permission to delete workout schedules");
        }
        UserWorkoutSchedule entity = userWorkoutScheduleRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Workout schedule not found"));
        if (requester.getRole() != Role.SUPER_ADMIN && !isVisibleToRequester(requester, entity)) {
            throw new SecurityException("You do not have permission to delete this workout schedule");
        }
        userWorkoutScheduleRepository.delete(entity);
    }
}
