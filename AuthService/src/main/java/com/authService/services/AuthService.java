package com.authService.services;

import com.authService.AOP.SendNotification;
import com.authService.DTO.AuthResponse;
import com.authService.DTO.LoginRequest;
import com.authService.DTO.SignupRequest;
import com.authService.entities.User;
import com.authService.enums.AccountStatus;
import com.authService.exceptions.*;
import com.authService.jwtSecurity.JwtService;
import com.authService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "USER_REGISTERED")
    public User registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists.");
        }

        boolean isOtpValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!isOtpValid) {
            throw new InvalidOtpException("Invalid or Expired OTP. Please request a new one.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enrollmentNo(request.getEnrollmentNo())
                .collegeId(request.getCollegeId())
                .departmentId(request.getDepartmentId())
                .role(request.getRole())
                .status(AccountStatus.PENDING)
                .isEmailVerified(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    @SendNotification(topic = "notification.system", eventType = "STATUS_CHANGED")
    public User updateUserStatus(String email, AccountStatus newStatus) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidOtpException("Invalid email or password."); // Reusing or create BadCredentials custom exception
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.isPasswordChangeRequired()) {
            // Throws specifically so the frontend can catch the exact error code
            throw new PasswordResetRequiredException("You must change your password before logging in.");
        }
        if (user.getStatus() != AccountStatus.APPROVED) {
            throw new AccountStatusException("Your account status is currently: " + user.getStatus());
        }

        return new AuthResponse(jwtService.generateToken(user), user.getStatus().toString());
    }

    @SendNotification(topic = "notification.otp", eventType = "OTP_PASSWORD_RESET")
    public OtpService.OtpEvent forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("User not found with this email.");
        }
        return otpService.generateAndSendOtp(email);
    }

    @SendNotification(topic = "notification.otp", eventType = "OTP_EMAIL_VERIFICATION")
    public OtpService.OtpEvent verifyEmail(String email) {
        return otpService.generateAndSendOtp(email);
    }

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "PASSWORD_CHANGED")
    public String resetPassword(String email, String otp, String newPassword) {
        boolean isOtpValid = otpService.validateOtp(email, otp);
        if (!isOtpValid) {
            throw new InvalidOtpException("Invalid or Expired OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password reset successfully. You can now login.";
    }

    @Transactional
    @SendNotification(topic = "notification.user", eventType = "ADMIN_USER_CREATED")
    public User createAdminUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .isPasswordChangeRequired(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));
        } catch (BadCredentialsException e) {
            throw new InvalidOtpException("Incorrect old password.");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
    }
}