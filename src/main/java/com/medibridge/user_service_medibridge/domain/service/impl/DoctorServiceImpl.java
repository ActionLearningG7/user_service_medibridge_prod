package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.request.CreateDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.request.VerifyDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.response.DoctorProfileResponse;
import com.medibridge.user_service_medibridge.domain.entity.DoctorProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.DoctorProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.DoctorService;
import com.medibridge.user_service_medibridge.exception.custom.DuplicateEmailException;
import com.medibridge.user_service_medibridge.exception.custom.ProfileNotFoundException;
import com.medibridge.user_service_medibridge.exception.custom.UserNotFoundException;
import com.medibridge.user_service_medibridge.service.notification.CredentialNotificationService;
import com.medibridge.user_service_medibridge.util.CredentialGenerator;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.Specialization;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import com.medibridge.user_service_medibridge.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DoctorService
 * 
 * Handles doctor profile management, verification, and queries
 * Implements secure doctor onboarding with credential generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialGenerator credentialGenerator;
    private final CredentialNotificationService credentialNotificationService;

    @Value("${app.temporary.password.validity.hours:24}")
    private int temporaryPasswordValidityHours;

    @Override
    public DoctorProfileResponse createDoctor(CreateDoctorRequest request, String createdBy) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("DOCTOR ONBOARDING INITIATED");
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
                .role(Role.DOCTOR)
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
        // STEP 5: PROFILE CREATION - Create doctor profile
        // ═══════════════════════════════════════════════════════════════
        log.info("Creating doctor profile...");
        DoctorProfile doctorProfile = DoctorProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .medicalLicenseNumber(request.getMedicalLicenseNumber())
                .medicalLicenseIssuingAuthority(request.getMedicalLicenseIssuingAuthority())
                .medicalLicenseIssueDate(request.getMedicalLicenseIssueDate())
                .medicalLicenseExpiryDate(request.getMedicalLicenseExpiryDate())
                .medicalRegistrationNumber(request.getMedicalRegistrationNumber())
                .medicalCouncil(request.getMedicalCouncil())
                .medicalDegree(request.getMedicalDegree())
                .medicalSchool(request.getMedicalSchool())
                .graduationYear(request.getGraduationYear())
                .additionalQualifications(request.getAdditionalQualifications())
                .specialization(request.getSpecialization())
                .subSpecialization(request.getSubSpecialization())
                .yearsOfExperience(request.getYearsOfExperience())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .employmentType(request.getEmploymentType())
                .joiningDate(request.getJoiningDate())
                .employeeId(request.getEmployeeId())
                .consultationFee(request.getConsultationFee())
                .availableForConsultation(false)
                .availableForEmergency(false)
                .verificationStatus(VerificationStatus.PENDING)
                .active(false)
                .profileCompleted(true)
                .deleted(false)
                .createdAt(now)
                .createdBy(createdBy)
                .build();

        doctorProfile = doctorProfileRepository.save(doctorProfile);
        log.info("✓ Doctor profile created: {}", doctorProfile.getDoctorProfileId());

        // ═══════════════════════════════════════════════════════════════
        // STEP 6: NOTIFICATION - Send credentials via secure email
        // ═══════════════════════════════════════════════════════════════
        log.info("Sending onboarding credentials via secure email...");
        String fullName = buildFullName(doctorProfile);

        try {
            credentialNotificationService.sendDoctorCredentials(
                    request.getEmail(),
                    generatedUsername,
                    temporaryPassword,
                    fullName,
                    passwordExpiresAt);
            log.info("✓ Credentials sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("✗ Failed to send credentials email", e);
            // Don't fail the entire onboarding - admin can resend
            log.warn("Doctor created but email notification failed. Manual intervention required.");
        }

        // ═══════════════════════════════════════════════════════════════
        // STEP 7: AUDIT LOG - Record successful onboarding
        // ═══════════════════════════════════════════════════════════════
        log.info("═══════════════════════════════════════════════════════════");
        log.info("DOCTOR ONBOARDING COMPLETED SUCCESSFULLY");
        log.info("User ID: {}", user.getUserId());
        log.info("Doctor Profile ID: {}", doctorProfile.getDoctorProfileId());
        log.info("Username: {}", generatedUsername);
        log.info("Email: {}", request.getEmail());
        log.info("Password Expires: {}", passwordExpiresAt);
        log.info("Created By: {}", createdBy);
        log.info("═══════════════════════════════════════════════════════════");

        return mapToResponse(doctorProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(UUID userId) {
        log.info("Fetching doctor profile for user: {}", userId);

        DoctorProfile profile = doctorProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Doctor", userId));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfileByEmail(String email) {
        log.info("Fetching doctor profile by email: {}", email);

        DoctorProfile profile = doctorProfileRepository.findByUserEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for email: " + email));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getAllDoctors() {
        log.info("Fetching all doctor profiles");

        List<DoctorProfile> profiles = doctorProfileRepository.findByDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getVerifiedDoctors() {
        log.info("Fetching verified doctor profiles");

        List<DoctorProfile> profiles = doctorProfileRepository
                .findByVerificationStatusAndDeletedFalse(VerificationStatus.VERIFIED);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getDoctorsBySpecialization(Specialization specialization) {
        log.info("Fetching doctors by specialization: {}", specialization);

        List<DoctorProfile> profiles = doctorProfileRepository.findBySpecializationAndVerificationStatusAndDeletedFalse(
                specialization, VerificationStatus.VERIFIED);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getDoctorsByDepartment(String department) {
        log.info("Fetching doctors by department: {}", department);

        List<DoctorProfile> profiles = doctorProfileRepository.findByDepartmentAndDeletedFalse(department);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getAvailableDoctors() {
        log.info("Fetching available doctors");

        List<DoctorProfile> profiles = doctorProfileRepository
                .findByAvailableForConsultationTrueAndVerificationStatusAndDeletedFalse(
                        VerificationStatus.VERIFIED);

        System.out.println(profiles.toString());

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getEmergencyDoctors() {
        log.info("Fetching emergency doctors");

        List<DoctorProfile> profiles = doctorProfileRepository
                .findByAvailableForEmergencyTrueAndVerificationStatusAndDeletedFalse(
                        VerificationStatus.VERIFIED);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getPendingVerifications() {
        log.info("Fetching pending doctor verifications");

        List<DoctorProfile> profiles = doctorProfileRepository
                .findByVerificationStatusAndDeletedFalse(VerificationStatus.PENDING);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorProfileResponse verifyDoctor(UUID doctorId, VerifyDoctorRequest request, String verifiedBy) {
        log.info("Verifying doctor: {}", doctorId);

        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ProfileNotFoundException("Doctor", doctorId));

        // Update verification status
        profile.setVerificationStatus(request.getVerificationStatus());
        profile.setVerificationNotes(request.getVerificationNotes());
        profile.setRejectionReason(request.getRejectionReason());
        profile.setVerifiedAt(LocalDateTime.now());
        profile.setVerifiedBy(verifiedBy);
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setUpdatedBy(verifiedBy);

        // If verified, activate the doctor
        if (request.getVerificationStatus() == VerificationStatus.VERIFIED) {
            profile.setActive(true);
            profile.setAvailableForConsultation(true);

            // Update user status
            User user = profile.getUser();
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);

            log.info("Doctor verified and activated: {}", doctorId);
        } else if (request.getVerificationStatus() == VerificationStatus.REJECTED) {
            profile.setActive(false);
            log.info("Doctor verification rejected: {}", doctorId);
        }

        profile = doctorProfileRepository.save(profile);

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> searchDoctors(String searchTerm) {
        log.info("Searching doctors with term: {}", searchTerm);

        List<DoctorProfile> profiles = doctorProfileRepository.searchDoctors(searchTerm);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getDoctorsWithExpiringLicenses(int daysThreshold) {
        log.info("Fetching doctors with licenses expiring in {} days", daysThreshold);

        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        List<DoctorProfile> profiles = doctorProfileRepository
                .findByMedicalLicenseExpiryDateBeforeAndDeletedFalse(thresholdDate);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDoctorProfile(UUID userId) {
        log.info("Deleting doctor profile for user: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Find profile
        DoctorProfile profile = doctorProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Doctor", userId));

        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityUtils.getCurrentUsername());
        userRepository.save(user);

        // Soft delete profile
        profile.setDeleted(true);
        profile.setDeletedAt(LocalDateTime.now());
        profile.setDeletedBy(SecurityUtils.getCurrentUsername());
        doctorProfileRepository.save(profile);

        log.info("Doctor profile deleted for user: {}", userId);
    }

    /**
     * Map DoctorProfile entity to response DTO
     */
    private DoctorProfileResponse mapToResponse(DoctorProfile profile) {
        return DoctorProfileResponse.builder()
                .doctorProfileId(profile.getDoctorProfileId())
                .userId(profile.getUser().getUserId())
                .firstName(profile.getFirstName())
                .middleName(profile.getMiddleName())
                .lastName(profile.getLastName())
                .fullName(buildFullName(profile))
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .phoneNumber(profile.getPhoneNumber())
                .medicalLicenseNumber(profile.getMedicalLicenseNumber())
                .medicalLicenseIssuingAuthority(profile.getMedicalLicenseIssuingAuthority())
                .medicalLicenseExpiryDate(profile.getMedicalLicenseExpiryDate())
                .licenseValid(isLicenseValid(profile))
                .medicalRegistrationNumber(profile.getMedicalRegistrationNumber())
                .medicalCouncil(profile.getMedicalCouncil())
                .medicalDegree(profile.getMedicalDegree())
                .medicalSchool(profile.getMedicalSchool())
                .graduationYear(profile.getGraduationYear())
                .additionalQualifications(profile.getAdditionalQualifications())
                .specialization(profile.getSpecialization())
                .subSpecialization(profile.getSubSpecialization())
                .yearsOfExperience(profile.getYearsOfExperience())
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .employmentType(profile.getEmploymentType())
                .consultationFee(profile.getConsultationFee())
                .availableForConsultation(profile.getAvailableForConsultation())
                .availableForEmergency(profile.getAvailableForEmergency())
                .verificationStatus(profile.getVerificationStatus())
                .verifiedAt(profile.getVerifiedAt())
                .verifiedBy(profile.getVerifiedBy())
                .verificationNotes(profile.getVerificationNotes())
                .rejectionReason(profile.getRejectionReason())
                .active(profile.getActive())
                .profileCompleted(profile.getProfileCompleted())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Build full name from profile
     */
    private String buildFullName(DoctorProfile profile) {
        StringBuilder fullName = new StringBuilder("Dr. ");

        if (profile.getFirstName() != null) {
            fullName.append(profile.getFirstName());
        }
        if (profile.getMiddleName() != null && !profile.getMiddleName().isEmpty()) {
            if (fullName.length() > 4)
                fullName.append(" ");
            fullName.append(profile.getMiddleName());
        }
        if (profile.getLastName() != null) {
            if (fullName.length() > 4)
                fullName.append(" ");
            fullName.append(profile.getLastName());
        }

        return fullName.toString();
    }

    /**
     * Check if medical license is valid
     */
    private Boolean isLicenseValid(DoctorProfile profile) {
        if (profile.getMedicalLicenseExpiryDate() == null) {
            return false;
        }
        return profile.getMedicalLicenseExpiryDate().isAfter(LocalDate.now());
    }
}
