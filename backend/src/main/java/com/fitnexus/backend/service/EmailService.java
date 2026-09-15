package com.fitnexus.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    // Present only when spring.mail.host is configured; null in demo mode.
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:no-reply@fitnexus.test}")
    private String from;

    public boolean isConfigured() {
        return mailSender != null && mailHost != null && !mailHost.isBlank();
    }

    /** Sends an email; if SMTP isn't configured, logs the content so flows still work in demo mode. */
    public void send(String to, String subject, String body) {
        if (!isConfigured()) {
            log.warn("[EMAIL DEMO MODE] To: {} | {} | {}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Could not send email: " + e.getMessage(), e);
        }
    }

    public void sendOtp(String to, String otp, String purposeLabel) {
        String subject = "FitNexus " + purposeLabel + " code";
        String body = "Your FitNexus " + purposeLabel + " code is: " + otp
                + "\n\nThis code expires shortly. If you didn't request it, please ignore this email.";
        send(to, subject, body);
    }
}
