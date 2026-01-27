package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.request.CreatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.request.UpdatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.response.PhlebotomistProfileResponse;
import com.medibridge.user_service_medibridge.domain.entity.PhlebotomistProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.PhlebotomistProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.PhlebotomistService;
import com.medibridge.user_service_medibridge.exception.custom.DuplicateEmailException;
import com.medibridge.user_service_medibridge.exception.custom.ProfileNotFoundException;
import com.medibridge.user_service_medibridge.exception.custom.UserNotFoundException;
import com.medibridge.user_service_medibridge.service.notification.CredentialNotificationService;
import com.medibridge.user_service_medibridge.util.CredentialGenerator;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
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
 * Implementation of PhlebotomistService
 *
 * Handles phlebotomist profile management, verification, and queries
 * Implements secure phlebotomist onboarding with credential generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhlebotomistServiceImpl implements PhlebotomistService {

    private final PhlebotomistProfileRepository phlebotomistProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialGenerator credentialGenerator;
    private final CredentialNotificationService credentialNotificationService;

    @Value("${app.temporary.password.validity.hours:24}")
    private int temporaryPasswordValidityHours;

    @Override
    public PhlebotomistProfileResponse createPhlebotomist(CreatePhlebotomistRequest request, String createdBy) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("PHLEBOTOMIST ONBOARDING INITIATED");
        log.info("Email: {}", request.getEmail());
        log.info("Name: {} {}", request.getFirstName(), request.getLastName());
        log.info("Created by: {}", createdBy);
        log.info("═══════════════════════════════════════════════════════════");

        // ═══════════════════════════════════════════════════════════════
        // STEP 1: VALIDATION - Check for duplicates
        // ═══════════════════════════════════════════════════════════════
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            log.error("ONBOARDING FAILED: Email already exists: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        if (phlebotomistProfileRepository.existsByCertificationNumber(request.getCertificationNumber())) {
            log.error("ONBOARDING FAILED: Certification number already exists: {}", request.getCertificationNumber());
            throw new IllegalArgumentException("Certification number already exists");
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
        // SECURITY: Password itself is NEVER logged

        // Calculate expiration time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passwordExpiresAt = now.plusHours(temporaryPasswordValidityHours);

        // ═══════════════════════════════════════════════════════════════
        // STEP 4: USER CREATION - Create user with temporary credentials
        // ═══════════════════════════════════════════════════════════════
        log.info("Creating user account with temporary credentials...");
        User user = User.builder()
                .username(generatedUsername)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .role(Role.PHLEBOTOMIST)
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
        // STEP 5: PROFILE CREATION - Create phlebotomist profile
        // ═══════════════════════════════════════════════════════════════
        log.info("Creating phlebotomist profile...");
        PhlebotomistProfile phlebotomistProfile = PhlebotomistProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .certificationNumber(request.getCertificationNumber())
                .certificationIssuingBody(request.getCertificationIssuingBody())
                .certificationExpiryDate(request.getCertificationExpiryDate())
                .shiftType(request.getShiftType())
                .assignedZone(request.getAssignedZone())
                .joiningDate(request.getJoiningDate())
                .employeeId(request.getEmployeeId())
                .verificationStatus(VerificationStatus.PENDING)
                .active(false)
                .isAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : false)
                .deleted(false)
                .createdAt(now)
                .createdBy(createdBy)
                .build();

        phlebotomistProfile = phlebotomistProfileRepository.save(phlebotomistProfile);
        log.info("✓ Phlebotomist profile created: {}", phlebotomistProfile.getPhlebotomistProfileId());

        // ═══════════════════════════════════════════════════════════════
        // STEP 6: NOTIFICATION - Send credentials via secure email
        // ═══════════════════════════════════════════════════════════════
        log.info("Sending onboarding credentials via secure email...");
        String fullName = request.getFirstName() + " " + request.getLastName();

        try {
            credentialNotificationService.sendPhlebotomistCredentials(
                    request.getEmail(),
                    generatedUsername,
                    temporaryPassword,
                    fullName,
                    passwordExpiresAt);
            log.info("✓ Credentials sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("✗ Failed to send credentials email", e);
            // Don't fail the entire onboarding - admin can resend
            log.warn("Phlebotomist created but email notification failed. Manual intervention required.");
        }

        // ═══════════════════════════════════════════════════════════════
        // STEP 7: AUDIT LOG - Record successful onboarding
        // ═══════════════════════════════════════════════════════════════
        log.info("═══════════════════════════════════════════════════════════");
        log.info("PHLEBOTOMIST ONBOARDING COMPLETED SUCCESSFULLY");
        log.info("User ID: {}", user.getUserId());
        log.info("Profile ID: {}", phlebotomistProfile.getPhlebotomistProfileId());
        log.info("Username: {}", generatedUsername);
        log.info("Email: {}", request.getEmail());
        log.info("═══════════════════════════════════════════════════════════");

        return mapToResponse(phlebotomistProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public PhlebotomistProfileResponse getPhlebotomistProfileByEmail(String email) {
        User user = userRepository.findByEmailOrUsernameAndNotDeleted(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist profile not found"));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PhlebotomistProfileResponse getPhlebotomistProfile(UUID userId) {
        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist profile not found for user: " + userId));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhlebotomistProfileResponse> getAllActivePhlebotomists() {
        return phlebotomistProfileRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhlebotomistProfileResponse> getAllPhlebotomists() {
        return phlebotomistProfileRepository.findAll()
                .stream()
                .filter(profile -> !profile.getDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhlebotomistProfile(UUID userId, String deletedBy) {
        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist profile not found for user: " + userId));

        profile.softDelete(deletedBy);
        phlebotomistProfileRepository.save(profile);

        // Also soft delete the user
        User user = profile.getUser();
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Phlebotomist profile deleted: {} by: {}", userId, deletedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhlebotomistProfileResponse> getAllPhlebotomistsPageable(Pageable pageable, String search, String status) {
        Page<PhlebotomistProfile> page;

        // Determine active status filter
        boolean filterByStatus = status != null && !status.isEmpty();
        boolean isActive = filterByStatus && "ACTIVE".equalsIgnoreCase(status);

        // Apply search and status filters
        if (search != null && !search.isEmpty()) {
            if (filterByStatus) {
                // Search with status filter
                page = phlebotomistProfileRepository.searchByNameOrEmailAndStatus(search, isActive, pageable);
            } else {
                // Search only - return all (both active and inactive)
                page = phlebotomistProfileRepository.searchByNameOrEmail(search, pageable);
            }
        } else {
            if (filterByStatus) {
                // Status filter only
                page = phlebotomistProfileRepository.findByStatus(isActive, pageable);
            } else {
                // No filters - get ALL phlebotomists (active and inactive)
                page = phlebotomistProfileRepository.findAll(pageable);
            }
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PhlebotomistProfileResponse updatePhlebotomist(UUID userId, UpdatePhlebotomistRequest request, String updatedBy) {
        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist not found"));

        // Update fields if provided
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getShiftType() != null) {
            profile.setShiftType(request.getShiftType());
        }
        if (request.getAssignedZone() != null) {
            profile.setAssignedZone(request.getAssignedZone());
        }
        if (request.getEmployeeId() != null) {
            profile.setEmployeeId(request.getEmployeeId());
        }
        if (request.getCertificationNumber() != null) {
            profile.setCertificationNumber(request.getCertificationNumber());
        }
        if (request.getCertificationExpiryDate() != null) {
            profile.setCertificationExpiryDate(request.getCertificationExpiryDate());
        }
        if (request.getSpecialization() != null) {
            profile.setSpecialization(request.getSpecialization());
        }
        if (request.getIsAdmin() != null) {
            profile.setIsAdmin(request.getIsAdmin());
            log.info("Phlebotomist admin status updated to: {} for user: {}", request.getIsAdmin(), userId);
        }

        // Update user email if provided
        if (request.getEmail() != null && !request.getEmail().equals(profile.getUser().getEmail())) {
            if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                throw new DuplicateEmailException(request.getEmail());
            }
            profile.getUser().setEmail(request.getEmail());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        PhlebotomistProfile updated = phlebotomistProfileRepository.save(profile);

        log.info("Phlebotomist updated: {} by: {}", userId, updatedBy);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public PhlebotomistProfileResponse updatePhlebotomistStatus(UUID userId, String status, String updatedBy) {
        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist not found"));

        boolean isActive = "ACTIVE".equals(status);
        profile.setActive(isActive);

        // Also update user status
        User user = profile.getUser();
        if (isActive) {
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.INACTIVE);
        }

        profile.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        PhlebotomistProfile updated = phlebotomistProfileRepository.save(profile);

        log.info("Phlebotomist status updated to {}: {} by: {}", status, userId, updatedBy);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void resetPhlebotomistCredentials(UUID userId, String resetBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Get phlebotomist profile for full name
        PhlebotomistProfile profile = phlebotomistProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Phlebotomist profile not found"));

        // Generate new temporary password
        String temporaryPassword = credentialGenerator.generateTemporaryPassword();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passwordExpiresAt = now.plusHours(temporaryPasswordValidityHours);

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setPasswordIsTemporary(true);
        user.setPasswordMustChange(true);
        user.setTemporaryPasswordExpiresAt(passwordExpiresAt);
        user.setCredentialGeneratedAt(now);
        user.setCredentialGeneratedBy(resetBy);
        user.setUpdatedAt(now);

        userRepository.save(user);

        // Send new credentials via email
        String fullName = profile.getFirstName() + " " + profile.getLastName();
        credentialNotificationService.sendPhlebotomistCredentials(
                user.getEmail(),
                user.getUsername(),
                temporaryPassword,
                fullName,
                passwordExpiresAt
        );

        log.info("Phlebotomist credentials reset: {} by: {}", userId, resetBy);
    }

    /**
     * Map PhlebotomistProfile entity to response DTO
     */
    private PhlebotomistProfileResponse mapToResponse(PhlebotomistProfile profile) {
        String fullName = profile.getFirstName() + " " + profile.getLastName();
        String status = profile.getActive() ? "ACTIVE" : "INACTIVE";

        return PhlebotomistProfileResponse.builder()
                .phlebotomistProfileId(profile.getPhlebotomistProfileId())
                .userId(profile.getUser().getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(fullName)
                .email(profile.getUser().getEmail())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .phoneNumber(profile.getPhoneNumber())
                .certificationNumber(profile.getCertificationNumber())
                .certificationIssuingBody(profile.getCertificationIssuingBody())
                .certificationExpiryDate(profile.getCertificationExpiryDate())
                .shiftType(profile.getShiftType())
                .assignedZone(profile.getAssignedZone())
                .employeeId(profile.getEmployeeId())
                .joiningDate(profile.getJoiningDate())
                .specialization(profile.getSpecialization())
                .verificationStatus(profile.getVerificationStatus())
                .active(profile.getActive())
                .status(status)
                .isAdmin(profile.getIsAdmin())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
