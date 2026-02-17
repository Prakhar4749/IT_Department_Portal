package com.authService.AOP;

import com.authService.DTO.NotificationEvent;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.services.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(sendNotification)", returning = "result")
    public void handleNotification(JoinPoint joinPoint, SendNotification sendNotification, Object result) {
        try {
            // 1. Initialize Event Envelope
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(sendNotification.eventType())
                    .timestamp(LocalDateTime.now())
                    .priority("MEDIUM")
                    .payload(new HashMap<>())
                    .build();

            // -----------------------------------------------------------------
            // LOGIC BLOCK: Populate Event based on what method triggered it
            // -----------------------------------------------------------------

            // CASE 1: OTP Generation (Result is OtpEvent)
            if (result instanceof OtpService.OtpEvent) {
                OtpService.OtpEvent otpData = (OtpService.OtpEvent) result;
                event.setTargetEmail(otpData.getEmail());
                event.getPayload().put("otp", otpData.getOtpCode());
                event.setPriority("HIGH");
            }

            // CASE 2: Admin Creation (We need the RAW password from args!)
            else if ("ADMIN_USER_CREATED".equals(sendNotification.eventType())) {
                if (result instanceof User) {
                    User user = (User) result;
                    event.setTargetUserId(user.getId());
                    event.setTargetEmail(user.getEmail());

                    // Extract Raw Password from the Method Arguments (SignupRequest)
                    if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignupRequest) {
                        SignupRequest req = (SignupRequest) joinPoint.getArgs()[0];
                        event.getPayload().put("tempPassword", req.getPassword()); // <--- CRITICAL
                        event.getPayload().put("role", req.getRole().toString());
                    }
                }
            }

            else if ("USER_REGISTERED".equals(sendNotification.eventType())) {
                if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof SignupRequest) {
                    SignupRequest req = (SignupRequest) joinPoint.getArgs()[0];

                    // We DO NOT set a single target email here. We pack the data needed for BOTH emails.
                    event.getPayload().put("userEmail", req.getEmail());
                    event.getPayload().put("departmentId", req.getDepartmentId()); // Critical for HOD lookup
                    event.getPayload().put("enrollmentNo", req.getEnrollmentNo());
                }
            }

            // CASE 4: Status Change / Approval (Result is User)
            else if (result instanceof User) {
                User user = (User) result;
                event.setTargetUserId(user.getId());
                event.setTargetEmail(user.getEmail());
                event.getPayload().put("status", user.getStatus().toString());

                if ("ACCOUNT_APPROVED".equals(sendNotification.eventType())) {
                    event.setPriority("HIGH");
                }
            }

            // CASE 5: Password Changed (Result is String, Args has Email)
            else if ("PASSWORD_CHANGED".equals(sendNotification.eventType())) {
                // Method signature: resetPassword(email, otp, newPassword)
                if (joinPoint.getArgs().length > 0) {
                    String email = (String) joinPoint.getArgs()[0];
                    event.setTargetEmail(email);
                    event.setPriority("HIGH");
                }
            }
            // -----------------------------------------------------------------

            // 3. Serialize & Send
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(sendNotification.topic(), jsonMessage);

            log.info("✅ AOP Published Event: {} for {}", event.getEventType(), event.getTargetEmail());

        } catch (Exception e) {
            log.error("❌ AOP Notification Failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}