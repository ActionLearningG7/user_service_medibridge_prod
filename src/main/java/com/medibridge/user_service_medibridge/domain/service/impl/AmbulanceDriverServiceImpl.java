package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.request.CreateAmbulanceDriverRequest;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverOnboardingResponse;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverProfileResponse;
import com.medibridge.user_service_medibridge.domain.entity.AmbulanceDriverProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.AmbulanceDriverProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.AmbulanceDriverService;
import com.medibridge.user_service_medibridge.exception.custom.DuplicateEmailException;
import com.medibridge.user_service_medibridge.exception.custom.ProfileNotFoundException;
import com.medibridge.user_service_medibridge.exception.custom.UserNotFoundException;
import com.medibridge.user_service_medibridge.service.notification.CredentialNotificationService;
import com.medibridge.user_service_medibridge.util.CredentialGenerator;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AmbulanceDriverService
 *
 * Handles ambulance driver profile management, verification, and queries
 * Implements secure driver onboarding with credential generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AmbulanceDriverServiceImpl implements AmbulanceDriverService {

    private final AmbulanceDriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialGenerator credentialGenerator;
    private final CredentialNotificationService credentialNotificationService;

    @Value("${app.temporary.password.validity.hours:24}")
    private int temporaryPasswordValidityHours;

    @Override
    @Transactional
    public AmbulanceDriverOnboardingResponse createAmbulanceDriver(
            CreateAmbulanceDriverRequest request,
            UUID organizationId,
            String createdBy
    ) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("AMBULANCE DRIVER ONBOARDING INITIATED");
        log.info("Email: {}", request.getEmail());
        log.info("Name: {} {}", request.getFirstName(), request.getLastName());
        log.info("License: {}", request.getLicenseNumber());
        log.info("Created by: {}", createdBy);
        log.info("═══════════════════════════════════════════════════════════");

        // ═══════════════════════════════════════════════════════════════
        // STEP 1: VALIDATION - Check for duplicates
        // ═══════════════════════════════════════════════════════════════
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            log.error("ONBOARDING FAILED: Email already exists: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        if (userRepository.existsByPhoneNumberAndDeletedFalse(request.getPhoneNumber())) {
            log.error("ONBOARDING FAILED: Phone number already exists: {}", request.getPhoneNumber());
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            log.error("ONBOARDING FAILED: License number already exists: {}", request.getLicenseNumber());
            throw new IllegalArgumentException("License number already exists");
        }

        // ═══════════════════════════════════════════════════════════════
        // STEP 2: CREDENTIAL GENERATION - Generate unique username
        // ═══════════════════════════════════════════════════════════════
        log.info("Generating unique username...");
        Set<String> existingUsernames = new HashSet<>(userRepository.findAllUsernames());
        String generatedUsername = credentialGenerator.generateUsername(
                request.getFirstName(),
                request.getLastName(),
                existingUsernames);
        log.info("Username generated: {}", generatedUsername);

        // ═══════════════════════════════════════════════════════════════
        // STEP 3: CREDENTIAL GENERATION - Generate secure temporary password
        // ═══════════════════════════════════════════════════════════════
        log.info("Generating cryptographically secure temporary password...");
        String temporaryPassword = credentialGenerator.generateTemporaryPassword();
        log.info("Temporary password generated (length: {}, strength: validated)",
                temporaryPassword.length());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passwordExpiresAt = now.plusHours(temporaryPasswordValidityHours);

        // ═══════════════════════════════════════════════════════════════
        // STEP 4: USER CREATION - Create user with temporary credentials
        // ═══════════════════════════════════════════════════════════════
        log.info("Creating user account with temporary credentials...");
        User user = User.builder()
                .username(generatedUsername)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .role(Role.AMBULANCE_DRIVER)
                .status(UserStatus.PENDING_VERIFICATION)

                // Temporary password management
                .passwordIsTemporary(true)
                .passwordMustChange(true)
                .temporaryPasswordExpiresAt(passwordExpiresAt)
                .credentialGeneratedAt(now)
                .credentialGeneratedBy(createdBy)

                // Security defaults
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)

                // Audit fields
                .deleted(false)
                .createdAt(now)
                .createdBy(createdBy)
                .build();

        user = userRepository.save(user);
        log.info("✓ User account created: {}", user.getUserId());

        // ═══════════════════════════════════════════════════════════════
        // STEP 5: PROFILE CREATION - Create ambulance driver profile
        // ═══════════════════════════════════════════════════════════════
        log.info("Creating ambulance driver profile...");
        AmbulanceDriverProfile driverProfile = AmbulanceDriverProfile.builder()
                .user(user)
                .organizationId(organizationId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeId(request.getEmployeeId())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry().atStartOfDay())
                .vehicleRegistrationNumber(request.getVehicleRegistrationNumber())
                .serviceArea(request.getServiceArea())
                .homeBaseLatitude(request.getHomeBaseLatitude())
                .homeBaseLongitude(request.getHomeBaseLongitude())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .certificationNumber(request.getCertificationNumber())
                .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0)
                .isActive(true)
                .onDutyStatus("OFF_DUTY")
                .totalShiftsCompleted(0)
                .totalIncidentsHandled(0)
                .averageRating(5.0)
                .createdAt(now)
                .createdBy(UUID.fromString(createdBy))
                .build();

        driverProfile = driverProfileRepository.save(driverProfile);
        log.info("✓ Ambulance driver profile created: {}", driverProfile.getProfileId());

        // ═══════════════════════════════════════════════════════════════
        // STEP 6: NOTIFICATION - Send credentials via secure email
        // ═══════════════════════════════════════════════════════════════
        log.info("Sending onboarding credentials via secure email...");
        String fullName = request.getFirstName() + " " + request.getLastName();

        try {
            credentialNotificationService.sendAmbulanceDriverCredentials(
                    request.getEmail(),
                    generatedUsername,
                    temporaryPassword,
                    fullName,
                    passwordExpiresAt);
            log.info("✓ Credentials sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("✗ Failed to send credentials email", e);
            log.warn("Ambulance driver created but email notification failed. Manual intervention required.");
        }

        // ═══════════════════════════════════════════════════════════════
        // STEP 7: AUDIT LOG - Record successful onboarding
        // ═══════════════════════════════════════════════════════════════
        log.info("═══════════════════════════════════════════════════════════");
        log.info("AMBULANCE DRIVER ONBOARDING COMPLETED SUCCESSFULLY");
        log.info("User ID: {}", user.getUserId());
        log.info("Profile ID: {}", driverProfile.getProfileId());
        log.info("Username: {}", generatedUsername);
        log.info("Email: {}", request.getEmail());
        log.info("═══════════════════════════════════════════════════════════");

        return mapToOnboardingResponse(user, driverProfile, generatedUsername, temporaryPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public AmbulanceDriverProfileResponse getAmbulanceDriverByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Ambulance driver profile not found for user: " + email));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AmbulanceDriverProfileResponse getAmbulanceDriver(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Ambulance driver profile not found for user: " + userId));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbulanceDriverProfileResponse> getAllActiveDrivers() {
        return driverProfileRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbulanceDriverProfileResponse> getAllDrivers() {
        return driverProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmbulanceDriverProfileResponse> getAllDriversPageable(
            Pageable pageable,
            String search,
            String status,
            String onDutyStatus
    ) {
        Page<AmbulanceDriverProfile> page;

        if (search != null && !search.isEmpty()) {
            // Search by name or license
            page = driverProfileRepository.searchDrivers(search, pageable);
        } else if (status != null && onDutyStatus != null) {
            Boolean isActive = "ACTIVE".equalsIgnoreCase(status);
            page = driverProfileRepository.findByIsActiveAndOnDutyStatus(isActive, onDutyStatus, pageable);
        } else if (status != null) {
            Boolean isActive = "ACTIVE".equalsIgnoreCase(status);
            page = driverProfileRepository.findByIsActive(isActive, pageable);
        } else if (onDutyStatus != null) {
            page = driverProfileRepository.findByOnDutyStatus(onDutyStatus, pageable);
        } else {
            page = driverProfileRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AmbulanceDriverProfileResponse updateDriver(
            UUID userId,
            CreateAmbulanceDriverRequest request,
            String updatedBy
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        // Update profile fields
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setLicenseExpiry(request.getLicenseExpiry().atStartOfDay());
        profile.setVehicleRegistrationNumber(request.getVehicleRegistrationNumber());
        profile.setServiceArea(request.getServiceArea());
        profile.setHomeBaseLatitude(request.getHomeBaseLatitude());
        profile.setHomeBaseLongitude(request.getHomeBaseLongitude());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        profile.setCertificationNumber(request.getCertificationNumber());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setUpdatedBy(UUID.fromString(updatedBy));
        profile.setUpdatedAt(LocalDateTime.now());

        profile = driverProfileRepository.save(profile);
        log.info("Driver profile updated: {}", userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public AmbulanceDriverProfileResponse updateDriverStatus(UUID userId, String status, String updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        Boolean isActive = "ACTIVE".equalsIgnoreCase(status);
        profile.setIsActive(isActive);
        profile.setUpdatedBy(UUID.fromString(updatedBy));
        profile.setUpdatedAt(LocalDateTime.now());

        profile = driverProfileRepository.save(profile);
        log.info("Driver status updated to: {} for user: {}", status, userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public AmbulanceDriverProfileResponse updateOnDutyStatus(UUID userId, String onDutyStatus, String updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        profile.setOnDutyStatus(onDutyStatus);
        profile.setUpdatedBy(UUID.fromString(updatedBy));
        profile.setUpdatedAt(LocalDateTime.now());

        profile = driverProfileRepository.save(profile);
        log.info("Driver on-duty status updated to: {} for user: {}", onDutyStatus, userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public AmbulanceDriverProfileResponse pauseDriver(UUID userId, String reason, String updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        profile.setOnDutyStatus("OFF_DUTY");
        profile.setIsActive(false);
        profile.setUpdatedBy(UUID.fromString(updatedBy));
        profile.setUpdatedAt(LocalDateTime.now());

        profile = driverProfileRepository.save(profile);
        log.info("Driver paused (reason: {}) for user: {}", reason, userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public AmbulanceDriverProfileResponse resumeDriver(UUID userId, String updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        profile.setIsActive(true);
        profile.setUpdatedBy(UUID.fromString(updatedBy));
        profile.setUpdatedAt(LocalDateTime.now());

        profile = driverProfileRepository.save(profile);
        log.info("Driver resumed for user: {}", userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public AmbulanceDriverOnboardingResponse resetDriverCredentials(UUID userId, String resetBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        AmbulanceDriverProfile profile = driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Driver profile not found"));

        // Generate new temporary password
        String newTemporaryPassword = credentialGenerator.generateTemporaryPassword();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passwordExpiresAt = now.plusHours(temporaryPasswordValidityHours);

        user.setPasswordHash(passwordEncoder.encode(newTemporaryPassword));
        user.setPasswordIsTemporary(true);
        user.setPasswordMustChange(true);
        user.setTemporaryPasswordExpiresAt(passwordExpiresAt);
        user.setCredentialGeneratedAt(now);
        user.setCredentialGeneratedBy(resetBy);

        user = userRepository.save(user);

        // Send notification
        try {
            credentialNotificationService.sendAmbulanceDriverCredentials(
                    user.getEmail(),
                    user.getUsername(),
                    newTemporaryPassword,
                    profile.getUser().getUsername(),
                    passwordExpiresAt);
            log.info("✓ New credentials sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("✗ Failed to send credentials email", e);
        }

        log.info("Driver credentials reset for user: {}", userId);

        return mapToOnboardingResponse(user, profile, user.getUsername(), newTemporaryPassword);
    }

    @Override
    @Transactional
    public void deleteDriver(UUID userId, String deletedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.softDelete(deletedBy);
        userRepository.save(user);

        log.info("Driver profile deleted (soft delete) for user: {}", userId);
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPING UTILITIES
    // ═══════════════════════════════════════════════════════════════

    private AmbulanceDriverProfileResponse mapToResponse(AmbulanceDriverProfile profile) {
        return AmbulanceDriverProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .organizationId(profile.getOrganizationId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getUser().getEmail())
                .phoneNumber(profile.getUser().getPhoneNumber())
                .licenseNumber(profile.getLicenseNumber())
                .licenseExpiry(profile.getLicenseExpiry())
                .vehicleRegistrationNumber(profile.getVehicleRegistrationNumber())
                .ambulanceId(profile.getAmbulanceId())
                .serviceArea(profile.getServiceArea())
                .homeBaseLatitude(profile.getHomeBaseLatitude())
                .homeBaseLongitude(profile.getHomeBaseLongitude())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .isActive(profile.getIsActive())
                .onDutyStatus(profile.getOnDutyStatus())
                .employeeId(profile.getEmployeeId())
                .yearsOfExperience(profile.getYearsOfExperience())
                .certificationNumber(profile.getCertificationNumber())
                .totalShiftsCompleted(profile.getTotalShiftsCompleted())
                .totalIncidentsHandled(profile.getTotalIncidentsHandled())
                .averageRating(profile.getAverageRating())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private AmbulanceDriverOnboardingResponse mapToOnboardingResponse(
            User user,
            AmbulanceDriverProfile profile,
            String username,
            String generatedPassword
    ) {
        String fullName = profile.getFirstName() + " " + profile.getLastName();
        return AmbulanceDriverOnboardingResponse.builder()
                .userId(user.getUserId())
                .profileId(profile.getProfileId())
                .username(username)
                .email(user.getEmail())
                .fullName(fullName)
                .generatedPassword(generatedPassword)
                .passwordIsTemporary(user.getPasswordIsTemporary())
                .passwordMustChange(user.getPasswordMustChange())
                .temporaryPasswordExpiresAt(user.getTemporaryPasswordExpiresAt())
                .licenseNumber(profile.getLicenseNumber())
                .vehicleRegistrationNumber(profile.getVehicleRegistrationNumber())
                .employeeId(profile.getEmployeeId())
                .credentialsGenerated(true)
                .credentialGeneratedAt(user.getCredentialGeneratedAt())
                .build();
    }
}
