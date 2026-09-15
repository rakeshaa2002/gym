package com.fitnexus.backend.config;

import com.fitnexus.backend.entity.FitnessUser;
import com.fitnexus.backend.entity.Notification;
import com.fitnexus.backend.entity.Attendance;
import com.fitnexus.backend.entity.UserWorkoutSchedule;
import com.fitnexus.backend.repository.FitnessUserRepository;
import com.fitnexus.backend.repository.NotificationRepository;
import com.fitnexus.backend.repository.AttendanceRepository;
import com.fitnexus.backend.repository.UserWorkoutScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChurnNotificationScheduler {

    private final FitnessUserRepository fitnessUserRepository;
    private final NotificationRepository notificationRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserWorkoutScheduleRepository userWorkoutScheduleRepository;

    // Run every day at 10 AM (and on app startup for demo purposes if needed, but cron is fine)
    // To see it immediately in dev, we could use fixedRate, but sticking to cron
    @Scheduled(cron = "0 0 10 * * ?")
    public void generateChurnAndRenewalNotifications() {
        log.info("Running daily churn and renewal notification check...");
        List<FitnessUser> allUsers = fitnessUserRepository.findAll();
        LocalDate today = LocalDate.now();

        for (FitnessUser fu : allUsers) {
            if (fu.getAccount() == null || !Boolean.TRUE.equals(fu.getAccount().getIsActive())) {
                continue; // Skip inactive
            }

            // Check membership expiry
            if (fu.getMembershipExpiry() != null) {
                LocalDate expiry = fu.getMembershipExpiry();
                long daysLeft = ChronoUnit.DAYS.between(today, expiry);

                if (daysLeft == 30 || daysLeft == 15 || daysLeft == 7 || daysLeft == 3 || daysLeft == 0) {
                    sendExpiryNotification(fu, (int) daysLeft);
                }
            }

            // Check irregular attendance (Not visited > 10 days)
            java.util.Optional<Attendance> lastAttendance = attendanceRepository.findFirstByUserOrderByCheckInTimeDesc(fu.getAccount());
            if (lastAttendance.isPresent()) {
                LocalDate lastVisitDate = lastAttendance.get().getCheckInTime().toLocalDate();
                if (ChronoUnit.DAYS.between(lastVisitDate, today) == 10) {
                    sendIrregularAttendanceNotification(fu, 10);
                }
            }

            // Check poor workout completion
            List<UserWorkoutSchedule> schedules = userWorkoutScheduleRepository.findByUser_IdOrderByStartDateTimeAsc(fu.getAccount().getId());
            if (!schedules.isEmpty()) {
                long completed = schedules.stream()
                        .filter(s -> "COMPLETED".equalsIgnoreCase(s.getCompletionStatus()))
                        .count();
                double completionRate = (double) completed / schedules.size() * 100;
                if (completionRate < 30.0 && schedules.size() >= 5) { // Only notify if they have at least 5 schedules
                    sendLowPerformanceNotification(fu, completionRate);
                }
            }
        }
        log.info("Completed daily churn and renewal notification check.");
    }

    private void sendIrregularAttendanceNotification(FitnessUser fu, int daysLeft) {
        String type = "WARNING";
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        if (notificationRepository.existsByRecipient_IdAndTypeAndCreatedAtAfter(fu.getAccount().getId(), type, startOfDay.minusDays(7))) {
            return; // Don't spam, wait at least 7 days before sending another warning
        }

        String title = "We Miss You!";
        String message = "You haven't visited the gym in 10 days. Consistency is key to reaching your goals! Come in today!";

        Notification n = new Notification();
        n.setRecipient(fu.getAccount());
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink("/schedule");

        notificationRepository.save(n);

        log.info("Auto WhatsApp to {}: Hi {}, we miss you! You haven't visited in 10 days. Let's get back on track!",
                fu.getPhone() != null ? fu.getPhone() : fu.getAccount().getEmail(),
                fu.getFirstName());
    }

    private void sendLowPerformanceNotification(FitnessUser fu, double completionRate) {
        String type = "WARNING";
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        if (notificationRepository.existsByRecipient_IdAndTypeAndCreatedAtAfter(fu.getAccount().getId(), type, startOfDay.minusDays(14))) {
            return; // Send this max once every 14 days
        }

        String title = "Workout Consistency Alert";
        String message = String.format("Your workout completion is at %.0f%%. Book a session with your trainer if you need help adjusting your plan!", completionRate);

        Notification n = new Notification();
        n.setRecipient(fu.getAccount());
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink("/workout");

        notificationRepository.save(n);

        log.info("Auto WhatsApp to {}: Hi {}, your workout completion is lower than usual. If you need help modifying your plan, please reach out to your trainer!",
                fu.getPhone() != null ? fu.getPhone() : fu.getAccount().getEmail(),
                fu.getFirstName());
    }

    private void sendExpiryNotification(FitnessUser fu, int daysLeft) {
        String type = "INFO";
        // Check if we already sent a notification recently to prevent spam
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        if (notificationRepository.existsByRecipient_IdAndTypeAndCreatedAtAfter(fu.getAccount().getId(), type, startOfDay)) {
            return;
        }

        String title = "Membership Expiring Soon";
        String message = daysLeft == 0 
            ? "Your membership expires TODAY. Renew now and get a 10% discount!"
            : String.format("Your membership expires in %d days. Renew now and get a 10%% discount!", daysLeft);

        Notification n = new Notification();
        n.setRecipient(fu.getAccount());
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink("/membership");

        notificationRepository.save(n);

        // Simulate auto-WhatsApp
        log.info("Auto WhatsApp to {}: Hi {}, Your membership expires on {}. Renew now and get 10% discount.",
                fu.getPhone() != null ? fu.getPhone() : fu.getAccount().getEmail(),
                fu.getFirstName(),
                fu.getMembershipExpiry());
    }
}
