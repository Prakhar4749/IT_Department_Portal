package com.notificationService.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationService.DTO.ApiResponse;
import com.notificationService.DTO.HodDetailsResponse;
import com.notificationService.DTO.NotificationEvent;
import com.notificationService.clients.AdminClient;

import com.notificationService.entities.Notification;
import com.notificationService.repositopries.NotificationRepository;
import com.notificationService.services.EmailService;
import com.notificationService.utils.EmailTemplateBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AdminClient adminClient;
    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"notification.otp", "notification.user", "notification.system"}, groupId = "notification-group")
    public void consume(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("📥 Consumed Event: {} | Type: {}", event.getEventId(), event.getEventType());

            switch (event.getEventType()) {
                case "OTP_EMAIL_VERIFICATION" -> sendOtpEmail(event, "Verify Your Email", "Welcome! Your signup verification code is:");
                case "OTP_PASSWORD_RESET" -> sendOtpEmail(event, "Reset Password Request", "Security Alert: Your password reset code is:");
                case "USER_REGISTERED" -> handleDualRegistrationNotification(event);
                case "ACCOUNT_APPROVED" -> handleAccountApproved(event);
                case "PASSWORD_CHANGED" -> handlePasswordReset(event);
                default -> log.warn("⚠️ Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            // In a production system, you would send this message to a Dead Letter Topic (DLT) here
            log.error("❌ Error processing notification: {}", e.getMessage(), e);
        }
    }

// ... inside NotificationConsumer.java ...

    private void sendOtpEmail(NotificationEvent event, String subject, String prefix) {
        String otp = (String) event.getPayload().get("otp");
        // USE THE BUILDER
        String html = EmailTemplateBuilder.buildOtpEmail(subject, prefix, otp);
        emailService.sendEmail(event.getTargetEmail(), subject, html);
    }

    private void handleDualRegistrationNotification(NotificationEvent event) {
        Map<String, Object> payload = event.getPayload();
        String userEmail = (String) payload.get("userEmail");
        String enrollmentNo = (String) payload.get("enrollmentNo");

        // 1. Send Email to the Student (USE BUILDER)
        String userHtml = EmailTemplateBuilder.buildWelcomePendingEmail(enrollmentNo);
        emailService.sendEmail(userEmail, "Registration Successful - Pending Approval", userHtml);

        // 2. Notify HOD
        try {
            Long deptId = Long.valueOf(payload.get("departmentId").toString());
            ApiResponse<HodDetailsResponse> response = adminClient.getHodDetails(deptId);
            HodDetailsResponse hod = response.getData();

            if (hod != null && hod.getEmail() != null) {
                // USE BUILDER for HOD
                String hodHtml = EmailTemplateBuilder.buildHodActionRequiredEmail(userEmail, enrollmentNo);
                emailService.sendEmail(hod.getEmail(), "Action Required: New Student Registration", hodHtml);

                NotificationEvent hodEvent = NotificationEvent.builder()
                        .targetUserId(hod.getUserId())
                        .payload(payload)
                        .build();

                saveAndSendInApp(hodEvent, "New Student Approval Required", "Student " + enrollmentNo + " is waiting for approval.", "INFO");
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch HOD details or send notification: {}", e.getMessage());
        }
    }

    private void handleAccountApproved(NotificationEvent event) {
        // USE BUILDER
        String html = EmailTemplateBuilder.buildAccountApprovedEmail();
        emailService.sendEmail(event.getTargetEmail(), "Welcome to the University Platform", html);
        saveAndSendInApp(event, "Account Approved", "Congratulations! Your account has been approved.", "SUCCESS");
    }

    private void handlePasswordReset(NotificationEvent event) {
        // USE BUILDER
        String html = EmailTemplateBuilder.buildSecurityAlertEmail();
        emailService.sendEmail(event.getTargetEmail(), "Security Alert: Password Changed", html);
    }

    private void saveAndSendInApp(NotificationEvent event, String title, String message, String type) {
        Notification n = Notification.builder()
                .userId(event.getTargetUserId())
                .title(title)
                .message(message)
                .isRead(false)
                .metadata(event.getPayload().toString())
                .build();

        repository.save(n);
        simpMessagingTemplate.convertAndSend("/topic/notifications/" + event.getTargetUserId(), n);
    }
}