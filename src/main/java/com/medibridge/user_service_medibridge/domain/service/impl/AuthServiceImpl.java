package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.request.*;
import com.medibridge.user_service_medibridge.api.dto.response.AuthResponse;
import com.medibridge.user_service_medibridge.api.dto.response.UserResponse;
import com.medibridge.user_service_medibridge.domain.entity.PatientProfile;
import com.medibridge.user_service_medibridge.domain.entity.PhlebotomistProfile;
import com.medibridge.user_service_medibridge.domain.entity.RefreshToken;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.PatientProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.PhlebotomistProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.RefreshTokenRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.AuthService;
import com.medibridge.user_service_medibridge.domain.service.EmailService;
import com.medibridge.user_service_medibridge.exception.custom.*;
import com.medibridge.user_service_medibridge.security.jwt.JwtTokenProvider;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of AuthService
 * 
 * Handles authentication, registration, and password management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PatientProfileRepository patientProfileRepository;
        private final PhlebotomistProfileRepository phlebotomistProfileRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final EmailService emailService;

        @Override
        public AuthResponse registerPatient(RegisterPatientRequest request) {
                log.info("Registering new patient with email: {}", request.getEmail());

                // Check if email already exists
                if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                        throw new DuplicateEmailException(request.getEmail());
                }

                // Check if username already exists
                if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
                        throw new DuplicateEmailException("Username already exists: " + request.getUsername());
                }

                // Create User entity
                User user = User.builder()
                                .username(request.getUsername())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(Role.PATIENT)
                                .status(UserStatus.PENDING_VERIFICATION)
                                .emailVerified(false)
                                .phoneVerified(false)
                                .twoFactorEnabled(false)
                                .failedLoginAttempts(0)
                                .deleted(false)
                                .createdAt(LocalDateTime.now())
                                .createdBy("self-registration")
                                .build();

                // Generate email verification token
                String verificationToken = UUID.randomUUID().toString();
                user.setEmailVerificationToken(verificationToken);
                user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

                // Send verification email
                emailService.sendEmail(
                                user.getEmail(),
                                "Verify your email",
                                "Your verification token is: " + verificationToken);

                // Save user
                user = userRepository.save(user);
                log.info("User created with ID: {}", user.getUserId());

                // Create Patient Profile
                PatientProfile patientProfile = PatientProfile.builder()
                                .user(user)
                                .firstName(request.getFirstName())
                                .middleName(request.getMiddleName())
                                .lastName(request.getLastName())
                                .dateOfBirth(request.getDateOfBirth())
                                .gender(request.getGender())
                                .phoneNumber(request.getPhoneNumber())
                                .addressLine1(request.getAddressLine1())
                                .addressLine2(request.getAddressLine2())
                                .city(request.getCity())
                                .state(request.getState())
                                .postalCode(request.getPostalCode())
                                .country(request.getCountry() != null ? request.getCountry() : "India")
                                .emergencyContactName(request.getEmergencyContactName())
                                .emergencyContactRelationship(request.getEmergencyContactRelationship())
                                .emergencyContactPhone(request.getEmergencyContactPhone())
                                .consentForTreatment(request.getTermsAccepted())
                                .consentForDataSharing(request.getPrivacyPolicyAccepted())
                                .profileCompleted(false)
                                .profileVerified(false)
                                .deleted(false)
                                .createdAt(LocalDateTime.now())
                                .createdBy("self-registration")
                                .build();

                patientProfileRepository.save(patientProfile);
                log.info("Patient profile created for user: {}", user.getUserId());

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                                user.getUserId(),
                                user.getEmail(),
                                user.getRole().name());
                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

                // Save refresh token
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                                .user(user)
                                .token(refreshToken)
                                .expiresAt(LocalDateTime.now().plusDays(7))
                                .revoked(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                refreshTokenRepository.save(refreshTokenEntity);

                // Build response
                UserResponse userResponse = UserResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .emailVerified(user.isEmailVerified())
                                .phoneVerified(user.isPhoneVerified())
                                .twoFactorEnabled(user.isTwoFactorEnabled())
                                .createdAt(user.getCreatedAt())
                                .build();

                log.info("Patient registration successful for: {}", user.getEmail());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                                .user(userResponse)
                                .build();
        }

        @Override
        public AuthResponse login(LoginRequest request) {
                log.info("Login attempt for: {}", request.getEmailOrUsername());

                // Find user by email or username
                User user = userRepository.findByEmailOrUsernameAndNotDeleted(request.getEmailOrUsername())
                                .orElseThrow(() -> new InvalidCredentialsException());

                // Check if account is locked
                if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                        throw new AccountLockedException(user.getAccountLockedUntil());
                }

                // Verify password
                if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        // Increment failed login attempts
                        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                        // Lock account after 5 failed attempts
                        if (user.getFailedLoginAttempts() >= 5) {
                                user.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
                                userRepository.save(user);
                                log.warn("Account locked due to failed login attempts: {}", user.getEmail());
                                throw new AccountLockedException(user.getAccountLockedUntil());
                        }

                        userRepository.save(user);
                        throw new InvalidCredentialsException();
                }

                // Check if account is active
                if (user.getStatus() == UserStatus.SUSPENDED) {
                        throw new UnauthorizedException("Account is suspended");
                }

                if (user.getStatus() == UserStatus.INACTIVE) {
                        throw new UnauthorizedException("Account is inactive");
                }

                // Auto-activate PENDING_VERIFICATION users on successful login
                // This allows them to use refresh tokens
                if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                        log.info("Auto-activating user on successful login: {}", user.getEmail());
                        user.setStatus(UserStatus.ACTIVE);
                }

                // Reset failed login attempts
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                user.setLastLoginAt(LocalDateTime.now());
                user.setLastLoginIp(request.getIpAddress());
                userRepository.save(user);

                // Prepare extra claims
                java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
                final boolean[] isAdmin = { false }; // Mutable container for lambda

                if (user.getRole() == Role.PHLEBOTOMIST) {
                        phlebotomistProfileRepository.findByUser_UserId(user.getUserId())
                                        .ifPresent(profile -> {
                                                log.info("Phlebotomist login - isAdmin: {}", profile.getIsAdmin());
                                                isAdmin[0] = profile.getIsAdmin();
                                                extraClaims.put("isAdmin", profile.getIsAdmin());
                                        });
                }

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                                user.getUserId(),
                                user.getEmail(),
                                user.getRole().name(),
                                extraClaims);
                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

                // Save refresh token
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                                .user(user)
                                .token(refreshToken)
                                .expiresAt(LocalDateTime.now().plusDays(7))
                                .revoked(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                refreshTokenRepository.save(refreshTokenEntity);

                // Build response
                UserResponse.UserResponseBuilder userResponseBuilder = UserResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .emailVerified(user.isEmailVerified())
                                .phoneVerified(user.isPhoneVerified())
                                .twoFactorEnabled(user.isTwoFactorEnabled())
                                .lastLoginAt(user.getLastLoginAt())
                                .createdAt(user.getCreatedAt());

                if (user.getRole() == Role.PHLEBOTOMIST) {
                        userResponseBuilder.isAdmin(isAdmin[0]);
                }

                UserResponse userResponse = userResponseBuilder.build();

                log.info("Login successful for: {}", user.getEmail());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                                .user(userResponse)
                                .build();
        }

        @Override
        public AuthResponse refreshToken(RefreshTokenRequest request) {
                log.info("Refreshing token: {}", request.getRefreshToken().substring(0, 20) + "...");

                // Find refresh token
                RefreshToken refreshTokenEntity = refreshTokenRepository
                                .findByTokenAndRevokedFalse(request.getRefreshToken())
                                .orElseThrow(() -> {
                                        log.error("Refresh token not found or already revoked");
                                        return new InvalidTokenException("refresh", "token not found or revoked");
                                });

                // Check if token is expired
                if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                        log.error("Refresh token expired at: {}", refreshTokenEntity.getExpiresAt());
                        throw new InvalidTokenException("refresh", "token expired");
                }

                User user = refreshTokenEntity.getUser();

                log.info("Refresh token found for user: {} (ID: {}, Role: {}, Status: {})",
                                user.getEmail(), user.getUserId(), user.getRole(), user.getStatus());

                // Check if user is active
                if (user.isDeleted()) {
                        log.error("User is deleted: {}", user.getEmail());
                        throw new UnauthorizedException("User account is deleted");
                }

                if (user.getStatus() != UserStatus.ACTIVE) {
                        log.error("User account is not active. Status: {}, Email: {}", user.getStatus(),
                                        user.getEmail());
                        throw new UnauthorizedException("User account is not active. Status: " + user.getStatus());
                }

                // Prepare extra claims
                java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();

                if (user.getRole() == Role.PHLEBOTOMIST) {
                        phlebotomistProfileRepository.findByUser_UserId(user.getUserId())
                                        .ifPresent(profile -> {
                                                extraClaims.put("isAdmin", profile.getIsAdmin());
                                        });
                }

                // Generate new tokens
                String newAccessToken = jwtTokenProvider.generateAccessToken(
                                user.getUserId(),
                                user.getEmail(),
                                user.getRole().name(),
                                extraClaims);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

                // Revoke old refresh token
                refreshTokenEntity.setRevoked(true);
                refreshTokenEntity.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(refreshTokenEntity);

                // Save new refresh token
                RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                                .user(user)
                                .token(newRefreshToken)
                                .expiresAt(LocalDateTime.now().plusDays(7))
                                .revoked(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                refreshTokenRepository.save(newRefreshTokenEntity);

                // Build response
                UserResponse userResponse = UserResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .emailVerified(user.isEmailVerified())
                                .phoneVerified(user.isPhoneVerified())
                                .twoFactorEnabled(user.isTwoFactorEnabled())
                                .build();

                log.info("Token refreshed for user: {}", user.getEmail());

                return AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                                .user(userResponse)
                                .build();
        }

        @Override
        public void logout(String refreshToken) {
                log.info("Logout request");

                // Find and revoke refresh token
                refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                                .ifPresent(token -> {
                                        token.setRevoked(true);
                                        token.setRevokedAt(LocalDateTime.now());
                                        refreshTokenRepository.save(token);
                                        log.info("Refresh token revoked for user: {}", token.getUser().getEmail());
                                });
        }

        @Override
        public UserResponse verifyEmail(String token) {
                log.info("Email verification request");

                // Find user by verification token
                User user = userRepository.findByValidEmailVerificationToken(token, LocalDateTime.now())
                                .orElseThrow(() -> new InvalidTokenException("email verification",
                                                "invalid or expired"));

                // Mark email as verified
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null);
                user.setEmailVerificationTokenExpiresAt(null);
                user.setStatus(UserStatus.ACTIVE);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("Email verified for user: {}", user.getEmail());

                return UserResponse.builder()
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .emailVerified(user.isEmailVerified())
                                .build();
        }

        @Override
        public void forgotPassword(ForgotPasswordRequest request) {
                log.info("Forgot password request for: {}", request.getEmail());

                // Find user by email
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UserNotFoundException("email", request.getEmail()));

                // Generate password reset token
                user.setPasswordResetToken(UUID.randomUUID().toString());
                user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                // Send password reset email
                emailService.sendEmail(
                                user.getEmail(),
                                "Reset your password",
                                "Your password reset token is: " + user.getPasswordResetToken());

                log.info("Password reset token sent to: {}", user.getEmail());
        }

        @Override
        public void resetPassword(ResetPasswordRequest request) {
                log.info("Password reset request");

                // Find user by reset token
                User user = userRepository.findByValidPasswordResetToken(request.getToken(), LocalDateTime.now())
                                .orElseThrow(() -> new InvalidTokenException("password reset", "invalid or expired"));

                // Update password
                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiresAt(null);
                user.setPasswordChangedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                // Revoke all refresh tokens for security
                refreshTokenRepository.findByUserAndRevokedFalse(user)
                                .forEach(token -> {
                                        token.setRevoked(true);
                                        token.setRevokedAt(LocalDateTime.now());
                                        refreshTokenRepository.save(token);
                                });

                log.info("Password reset successful for: {}", user.getEmail());
        }

        @Override
        public void changePassword(String userId, ChangePasswordRequest request) {
                log.info("Password change request for user: {}", userId);

                // Find user
                User user = userRepository.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new UserNotFoundException(UUID.fromString(userId)));

                // Verify current password
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                        throw new InvalidCredentialsException("Current password is incorrect");
                }

                // Update password
                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                user.setPasswordChangedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                // Revoke all refresh tokens for security
                refreshTokenRepository.findByUserAndRevokedFalse(user)
                                .forEach(token -> {
                                        token.setRevoked(true);
                                        token.setRevokedAt(LocalDateTime.now());
                                        refreshTokenRepository.save(token);
                                });

                log.info("Password changed successfully for user: {}", user.getEmail());
        }
}
