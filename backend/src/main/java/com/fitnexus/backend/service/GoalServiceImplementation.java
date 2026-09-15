package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.GoalRequest;
import com.fitnexus.backend.dto.GoalResponse;
import com.fitnexus.backend.entity.Goal;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.GoalRepository;
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
public class GoalServiceImplementation {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private Integer computeProgress(Goal goal) {
        if (goal.getTargetValue() != null && goal.getTargetValue() > 0) {
            double current = goal.getCurrentValue() == null ? 0 : goal.getCurrentValue();
            int percent = (int) Math.round((current / goal.getTargetValue()) * 100);
            return Math.max(0, Math.min(100, percent));
        }
        if ("Completed".equalsIgnoreCase(goal.getStatus())) return 100;
        if ("In Progress".equalsIgnoreCase(goal.getStatus())) return 50;
        return 0;
    }

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getCategory(),
                goal.getSets(),
                goal.getReps(),
                goal.getRest(),
                goal.getWeight(),
                goal.getCalories(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getUnit(),
                computeProgress(goal),
                goal.getStatus(),
                goal.getNotes(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private void copyFields(GoalRequest request, Goal target) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Goal name is required");
        }
        target.setName(request.getName().trim());
        target.setCategory(request.getCategory());
        target.setSets(request.getSets());
        target.setReps(request.getReps());
        target.setRest(request.getRest());
        target.setWeight(request.getWeight());
        target.setCalories(request.getCalories());
        target.setTargetValue(request.getTargetValue());
        target.setCurrentValue(request.getCurrentValue());
        target.setUnit(request.getUnit());
        target.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "Not Started" : request.getStatus().trim());
        target.setNotes(request.getNotes());
        target.setUpdatedAt(LocalDateTime.now());
    }

    public List<GoalResponse> getMyGoals() {
        Users user = getCurrentUser();
        return goalRepository.findByUserOrderByIdDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse create(GoalRequest request) {
        Users user = getCurrentUser();
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setCreatedAt(LocalDateTime.now());
        copyFields(request, goal);
        return toResponse(goalRepository.save(goal));
    }

    public GoalResponse update(Long id, GoalRequest request) {
        Users user = getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Goal not found"));
        if (goal.getUser() == null || !Objects.equals(goal.getUser().getId(), user.getId())) {
            throw new SecurityException("You do not have permission to modify this goal");
        }
        copyFields(request, goal);
        return toResponse(goalRepository.save(goal));
    }

    public void delete(Long id) {
        Users user = getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Goal not found"));
        if (goal.getUser() == null || !Objects.equals(goal.getUser().getId(), user.getId())) {
            throw new SecurityException("You do not have permission to delete this goal");
        }
        goalRepository.delete(goal);
    }
}
