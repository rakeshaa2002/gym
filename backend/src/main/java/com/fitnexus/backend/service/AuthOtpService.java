package com.fitnexus.backend.service;

import com.fitnexus.backend.dto.OtpDispatchResponse;
import com.fitnexus.backend.entity.EmailOtp;
import com.fitnexus.backend.entity.Users;
import com.fitnexus.backend.repository.EmailOtpRepository;
import com.fitnexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthOtpService {
    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private int expiryMinutes;

    private static final String RESET = "RESET";
    private static final String VERIFY = "VERIFY";

    private String norm(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private String createOtp(String email, String purpose) {
        EmailOtp otp = new EmailOtp();
        otp.setEmail(email);
        otp.setOtp(generateOtp());
        otp.setPurpose(purpose);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otp.setUsed(false);
        otp.setCreatedAt(LocalDateTime.now());
        emailOtpRepository.save(otp);
        return otp.getOtp();
    }

    private OtpDispatchResponse dispatch(String otp) {
        boolean delivered = emailService.isConfigured();
        return new OtpDispatchResponse(delivered, delivered ? null : otp);
    }

    /** Always returns success (doesn't reveal whether the email exists). */
    public OtpDispatchResponse forgotPassword(String rawEmail) {
        String email = norm(rawEmail);
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmail(email).isEmpty()) {
            // Pretend success; nothing sent (don't reveal whether the email exists).
            return new OtpDispatchResponse(emailService.isConfigured(), null);
        }
        String otp = createOtp(email, RESET);
        emailService.sendOtp(email, otp, "password reset");
        return dispatch(otp);
    }

    public OtpDispatchResponse sendEmailVerification(String rawEmail) {
        String email = norm(rawEmail);
        // Validate the account exists (throws if not); the entity itself isn't needed here.
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));
        String otp = createOtp(email, VERIFY);
        emailService.sendOtp(email, otp, "email verification");
        return dispatch(otp);
    }

    private boolean isValid(String email, String otp, String purpose) {
        if (otp == null || otp.isBlank()) return false;
        return emailOtpRepository.findTopByEmailAndPurposeAndUsedFalseOrderByIdDesc(norm(email), purpose)
                .filter(o -> o.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(o -> o.getOtp().equals(otp.trim()))
                .isPresent();
    }

    /** Non-consuming validity check (used by the Verify Pin step). */
    public boolean verifyOtp(String email, String otp, String purpose) {
        String p = VERIFY.equalsIgnoreCase(purpose) ? VERIFY : RESET;
        return isValid(email, otp, p);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        EmailOtp record = emailOtpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByIdDesc(norm(email), RESET)
                .filter(o -> o.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(o -> o.getOtp().equals(otp == null ? null : otp.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired code"));

        Users user = userRepository.findByEmail(norm(email))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        record.setUsed(true);
        emailOtpRepository.save(record);
    }

    public void verifyEmail(String email, String otp) {
        EmailOtp record = emailOtpRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByIdDesc(norm(email), VERIFY)
                .filter(o -> o.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(o -> o.getOtp().equals(otp == null ? null : otp.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired code"));

        Users user = userRepository.findByEmail(norm(email))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        user.setEmailVerified(true);
        userRepository.save(user);

        record.setUsed(true);
        emailOtpRepository.save(record);
    }
}
