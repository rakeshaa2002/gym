package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.schedule.ScheduleUserSummary;
import com.fitnexus.backend.dto.schedule.TrainerDutyScheduleRequest;
import com.fitnexus.backend.dto.schedule.TrainerDutyScheduleResponse;
import com.fitnexus.backend.entity.Role;
import com.fitnexus.backend.entity.TrainerDutySchedule;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.TrainerDutyScheduleRepository;
import com.fitnexus.backend.repository.UserRepository;
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
public class TrainerDutyScheduleServiceImplementation {

    private final TrainerDutyScheduleRepository trainerDutyScheduleRepository;
    private final UserRepository userRepository;

    private int roleLevel(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> 5;
            case ADMIN -> 4;
            case MANAGER -> 3;
            case TRAINER -> 2;
            case USER -> 1;
            default -> 0;
        };
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private void requireManagementAccess(Users requester) {
        if (requester.getRole() == Role.USER || requester.getRole() == Role.TRAINER) {
            throw new SecurityException("You do not have permission to manage trainer duty schedules");
        }
    }

    private boolean canAccessTrainer(Users requester, Users trainer) {
        if (requester == null || trainer == null) return false;
        if (requester.getRole() == Role.SUPER_ADMIN) return true;
        if (requester.getRole() == Role.ADMIN) {
            return trainer.getAdmin() != null && Objects.equals(trainer.getAdmin().getId(), requester.getId());
        }
        if (requester.getRole() == Role.MANAGER) {
            return trainer.getManager() != null && Objects.equals(trainer.getManager().getId(), requester.getId());
        }
        return false;
    }

    private void requireTrainerAssignmentAccess(Users requester, Users trainer) {
        requireManagementAccess(requester);
        if (requester.getRole() != Role.SUPER_ADMIN && !canAccessTrainer(requester, trainer)) {
            throw new SecurityException("You do not have permission to assign this trainer");
        }
    }

    private Users resolveTrainer(Long trainerId, Users requester) {
        if (trainerId == null) {
            throw new IllegalArgumentException("Trainer is required");
        }
        Users trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new InvalidOperationException("Trainer not found"));
        if (trainer.getRole() != Role.TRAINER) {
            throw new IllegalArgumentException("Selected user is not a trainer");
        }
        requireTrainerAssignmentAccess(requester, trainer);
        return trainer;
    }

    private Users resolveCurrentAssignedBy() {
        return getCurrentUser();
    }

    private boolean isVisibleSchedule(Users requester, TrainerDutySchedule schedule) {
        if (requester.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        Users trainer = schedule.getTrainer();
        if (requester.getRole() == Role.TRAINER) {
            return trainer != null && Objects.equals(trainer.getId(), requester.getId());
        }
        if (requester.getRole() == Role.ADMIN) {
            return trainer != null && trainer.getAdmin() != null && Objects.equals(trainer.getAdmin().getId(), requester.getId());
        }
        if (requester.getRole() == Role.MANAGER) {
            return trainer != null && trainer.getManager() != null && Objects.equals(trainer.getManager().getId(), requester.getId());
        }
        return false;
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

    private TrainerDutyScheduleResponse toResponse(TrainerDutySchedule schedule) {
        return new TrainerDutyScheduleResponse(
                schedule.getId(),
                toUserSummary(schedule.getTrainer()),
                toUserSummary(schedule.getAssignedBy()),
                schedule.getBranch(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartDateTime(),
                schedule.getEndDateTime(),
                schedule.getRepeatType(),
                schedule.getShiftType(),
                schedule.getLocation(),
                schedule.getStatus(),
                schedule.getNotes(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }

    private void copyFields(TrainerDutySchedule request, TrainerDutySchedule target, Users requester) {
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

        target.setTrainer(resolveTrainer(request.getTrainer() == null ? null : request.getTrainer().getId(), requester));
        target.setAssignedBy(resolveCurrentAssignedBy());
        target.setBranch(request.getBranch());
        target.setTitle(request.getTitle().trim());
        target.setDescription(request.getDescription());
        target.setStartDateTime(request.getStartDateTime());
        target.setEndDateTime(request.getEndDateTime());
        target.setRepeatType(normalizeText(request.getRepeatType(), "None"));
        target.setShiftType(normalizeText(request.getShiftType(), "General"));
        target.setLocation(request.getLocation());
        target.setStatus(normalizeText(request.getStatus(), "ACTIVE"));
        target.setNotes(request.getNotes());
        target.setUpdatedAt(LocalDateTime.now());
    }

    private String normalizeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public List<TrainerDutyScheduleResponse> getAll() {
        Users requester = getCurrentUser();
        return trainerDutyScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                .filter(schedule -> isVisibleSchedule(requester, schedule))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TrainerDutyScheduleResponse> getMyTeam() {
        Users requester = getCurrentUser();
        return trainerDutyScheduleRepository.findAllByOrderByUpdatedAtDescIdDesc().stream()
                .filter(schedule -> isVisibleSchedule(requester, schedule))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TrainerDutyScheduleResponse create(TrainerDutyScheduleRequest request) {
        Users requester = getCurrentUser();
        requireManagementAccess(requester);

        TrainerDutySchedule entity = new TrainerDutySchedule();
        entity.setCreatedAt(LocalDateTime.now());
        copyFields(toEntity(request), entity, requester);
        return toResponse(trainerDutyScheduleRepository.save(entity));
    }

    public TrainerDutyScheduleResponse update(Long id, TrainerDutyScheduleRequest request) {
        Users requester = getCurrentUser();
        requireManagementAccess(requester);
        TrainerDutySchedule entity = trainerDutyScheduleRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Trainer duty schedule not found"));
        copyFields(toEntity(request), entity, requester);
        return toResponse(trainerDutyScheduleRepository.save(entity));
    }

    public void delete(Long id) {
        Users requester = getCurrentUser();
        requireManagementAccess(requester);
        TrainerDutySchedule entity = trainerDutyScheduleRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Trainer duty schedule not found"));
        if (!isVisibleSchedule(requester, entity) && requester.getRole() != Role.SUPER_ADMIN) {
            throw new SecurityException("You do not have permission to delete this trainer duty schedule");
        }
        trainerDutyScheduleRepository.delete(entity);
    }

    private TrainerDutySchedule toEntity(TrainerDutyScheduleRequest request) {
        TrainerDutySchedule entity = new TrainerDutySchedule();
        Users trainer = request.getTrainerId() == null ? null : new Users();
        if (trainer != null) {
            trainer.setId(request.getTrainerId());
        }
        entity.setTrainer(trainer);
        entity.setBranch(request.getBranch());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDateTime(request.getStartDateTime());
        entity.setEndDateTime(request.getEndDateTime());
        entity.setRepeatType(request.getRepeatType());
        entity.setShiftType(request.getShiftType());
        entity.setLocation(request.getLocation());
        entity.setStatus(request.getStatus());
        entity.setNotes(request.getNotes());
        return entity;
    }
}
