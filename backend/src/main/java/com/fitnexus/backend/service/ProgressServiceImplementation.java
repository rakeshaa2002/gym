package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.GoalResponse;
import com.fitnexus.backend.dto.ProgressEntryRequest;
import com.fitnexus.backend.dto.ProgressEntryResponse;
import com.fitnexus.backend.dto.ProgressSummaryResponse;
import com.fitnexus.backend.entity.Goal;
import com.fitnexus.backend.entity.ProgressEntry;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.exception.InvalidOperationException;
import com.fitnexus.backend.repository.GoalRepository;
import com.fitnexus.backend.repository.ProgressEntryRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressServiceImplementation {
    private final ProgressEntryRepository progressEntryRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalServiceImplementation goalServiceImplementation;

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("You are not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private ProgressEntryResponse toResponse(ProgressEntry entry) {
        return new ProgressEntryResponse(
                entry.getId(),
                entry.getEntryDate(),
                entry.getWeightKg(),
                entry.getHeartRateBpm(),
                entry.getHealthScore(),
                entry.getWorkoutMinutes(),
                entry.getWorkoutsCompleted(),
                entry.getCaloriesBurned(),
                entry.getSteps(),
                entry.getWaterLiters(),
                entry.getNotes(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    private void copyFields(ProgressEntryRequest request, ProgressEntry target) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        target.setEntryDate(request.getEntryDate() == null ? LocalDate.now() : request.getEntryDate());
        target.setWeightKg(request.getWeightKg());
        target.setHeartRateBpm(request.getHeartRateBpm());
        target.setHealthScore(request.getHealthScore());
        target.setWorkoutMinutes(request.getWorkoutMinutes());
        target.setWorkoutsCompleted(request.getWorkoutsCompleted());
        target.setCaloriesBurned(request.getCaloriesBurned());
        target.setSteps(request.getSteps());
        target.setWaterLiters(request.getWaterLiters());
        target.setNotes(request.getNotes());
        target.setUpdatedAt(LocalDateTime.now());
    }

    public List<ProgressEntryResponse> getMyEntries() {
        Users user = getCurrentUser();
        return progressEntryRepository.findByUserOrderByEntryDateDescIdDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProgressEntryResponse> getMemberEntries(Long memberId) {
        Users member = userRepository.findById(memberId)
                .orElseThrow(() -> new InvalidOperationException("Member not found"));
        return progressEntryRepository.findByUserOrderByEntryDateDescIdDesc(member).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProgressEntryResponse create(ProgressEntryRequest request) {
        Users user = getCurrentUser();
        ProgressEntry entry = new ProgressEntry();
        entry.setUser(user);
        entry.setCreatedAt(LocalDateTime.now());
        copyFields(request, entry);
        return toResponse(progressEntryRepository.save(entry));
    }

    public void delete(Long id) {
        Users user = getCurrentUser();
        ProgressEntry entry = progressEntryRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Progress entry not found"));
        if (entry.getUser() == null || !Objects.equals(entry.getUser().getId(), user.getId())) {
            throw new SecurityException("You do not have permission to delete this entry");
        }
        progressEntryRepository.delete(entry);
    }

    private int sum(List<ProgressEntry> entries, java.util.function.ToIntFunction<ProgressEntry> getter) {
        return entries.stream().mapToInt(getter).sum();
    }

    public ProgressSummaryResponse getSummary() {
        Users user = getCurrentUser();
        List<ProgressEntry> entries = progressEntryRepository.findByUserOrderByEntryDateDescIdDesc(user);

        LocalDate weekStart = LocalDate.now().minusDays(6);
        List<ProgressEntry> thisWeek = entries.stream()
                .filter(e -> e.getEntryDate() != null && !e.getEntryDate().isBefore(weekStart))
                .collect(Collectors.toList());

        int workoutMinutesThisWeek = sum(thisWeek, e -> e.getWorkoutMinutes() == null ? 0 : e.getWorkoutMinutes());
        int workoutsThisWeek = sum(thisWeek, e -> e.getWorkoutsCompleted() == null ? 0 : e.getWorkoutsCompleted());
        int caloriesThisWeek = sum(thisWeek, e -> e.getCaloriesBurned() == null ? 0 : e.getCaloriesBurned());

        ProgressEntry latest = entries.isEmpty() ? null : entries.get(0);
        Double latestWeight = latest == null ? null : latest.getWeightKg();
        Integer latestHeartRate = latest == null ? null : latest.getHeartRateBpm();
        Integer latestHealthScore = latest == null ? null : latest.getHealthScore();

        // Weight series in ascending date order (oldest -> newest) for charting.
        List<ProgressSummaryResponse.WeightPoint> weightSeries = entries.stream()
                .filter(e -> e.getWeightKg() != null && e.getEntryDate() != null)
                .sorted(Comparator.comparing(ProgressEntry::getEntryDate))
                .map(e -> new ProgressSummaryResponse.WeightPoint(e.getEntryDate(), e.getWeightKg()))
                .collect(Collectors.toList());

        // Weight goal comes from a goal categorised "Weight" (if the user set one).
        List<Goal> goals = goalRepository.findByUserOrderByIdDesc(user);
        Double weightGoal = goals.stream()
                .filter(g -> g.getCategory() != null && g.getCategory().equalsIgnoreCase("Weight") && g.getTargetValue() != null)
                .map(Goal::getTargetValue)
                .findFirst()
                .orElse(null);

        Integer weightProgressPercent = null;
        if (weightGoal != null && latestWeight != null && !weightSeries.isEmpty()) {
            double startWeight = weightSeries.get(0).getWeight();
            if (Math.abs(startWeight - weightGoal) > 0.0001) {
                double percent = ((startWeight - latestWeight) / (startWeight - weightGoal)) * 100.0;
                weightProgressPercent = (int) Math.max(0, Math.min(100, Math.round(percent)));
            } else {
                weightProgressPercent = 100;
            }
        }

        List<GoalResponse> goalResponses = goalServiceImplementation.getMyGoals();

        // Dashboard fields
        LocalDate today = LocalDate.now();
        Integer latestSteps = latest == null ? null : latest.getSteps();
        Double latestWater = latest == null ? null : latest.getWaterLiters();
        int caloriesToday = sum(
                entries.stream().filter(e -> today.equals(e.getEntryDate())).collect(Collectors.toList()),
                e -> e.getCaloriesBurned() == null ? 0 : e.getCaloriesBurned());

        // Weekly activity: workout minutes per day for the last 7 days (Mon-style labels).
        java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("EEE");
        List<ProgressSummaryResponse.DayActivity> weeklyActivity = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int minutes = sum(
                    entries.stream().filter(e -> day.equals(e.getEntryDate())).collect(Collectors.toList()),
                    e -> e.getWorkoutMinutes() == null ? 0 : e.getWorkoutMinutes());
            weeklyActivity.add(new ProgressSummaryResponse.DayActivity(day.format(dayFmt), minutes));
        }

        ProgressSummaryResponse response = new ProgressSummaryResponse();
        response.setWorkoutMinutesThisWeek(workoutMinutesThisWeek);
        response.setWorkoutsThisWeek(workoutsThisWeek);
        response.setCaloriesThisWeek(caloriesThisWeek);
        response.setLatestWeight(latestWeight);
        response.setWeightGoal(weightGoal);
        response.setWeightProgressPercent(weightProgressPercent);
        response.setLatestHeartRate(latestHeartRate);
        response.setLatestHealthScore(latestHealthScore);
        response.setWeightSeries(new ArrayList<>(weightSeries));
        response.setGoals(goalResponses);
        response.setLatestSteps(latestSteps);
        response.setStepsGoal(8000);
        response.setLatestWaterLiters(latestWater);
        response.setWaterGoalLiters(3.0);
        response.setCaloriesToday(caloriesToday);
        response.setCaloriesGoal(2500);
        response.setWeeklyActivity(weeklyActivity);
        return response;
    }
}
