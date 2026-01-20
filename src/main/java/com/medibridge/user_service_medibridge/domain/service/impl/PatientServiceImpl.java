package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.request.UpdatePatientProfileRequest;
import com.medibridge.user_service_medibridge.api.dto.response.PatientProfileResponse;
import com.medibridge.user_service_medibridge.domain.entity.PatientProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.PatientProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.PatientService;
import com.medibridge.user_service_medibridge.exception.custom.ProfileNotFoundException;
import com.medibridge.user_service_medibridge.exception.custom.UserNotFoundException;
import com.medibridge.user_service_medibridge.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PatientService
 * 
 * Handles patient profile management and queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(UUID userId) {
        log.info("Fetching patient profile for user: {}", userId);

        PatientProfile profile = patientProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Patient", userId));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfileByEmail(String email) {
        log.info("Fetching patient profile for email: {}", email);
        User user = userRepository.findByEmailOrUsernameAndNotDeleted(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        return getPatientProfile(user.getUserId());
    }

    @Override
    public PatientProfileResponse updatePatientProfileByEmail(String email, UpdatePatientProfileRequest request) {
        log.info("Updating patient profile for email: {}", email);
        User user = userRepository.findByEmailOrUsernameAndNotDeleted(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        return updatePatientProfile(user.getUserId(), request);
    }

    @Override
    public PatientProfileResponse updatePatientProfile(UUID userId, UpdatePatientProfileRequest request) {
        log.info("Updating patient profile for user: {}", userId);

        PatientProfile profile = patientProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Patient", userId));

        // Update profile fields
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            profile.setMiddleName(request.getMiddleName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getBloodGroup() != null) {
            profile.setBloodGroup(request.getBloodGroup());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAlternatePhoneNumber() != null) {
            profile.setAlternatePhoneNumber(request.getAlternatePhoneNumber());
        }
        if (request.getAddressLine1() != null) {
            profile.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            profile.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            profile.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            profile.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getEmergencyContactName() != null) {
            profile.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactRelationship() != null) {
            profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        }
        if (request.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getGovernmentIdType() != null) {
            profile.setGovernmentIdType(request.getGovernmentIdType());
        }
        if (request.getGovernmentIdNumber() != null) {
            profile.setGovernmentIdNumber(request.getGovernmentIdNumber());
        }
        if (request.getInsuranceProvider() != null) {
            profile.setInsuranceProvider(request.getInsuranceProvider());
        }
        if (request.getInsurancePolicyNumber() != null) {
            profile.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        }
        if (request.getInsuranceValidUntil() != null) {
            profile.setInsuranceValidUntil(request.getInsuranceValidUntil());
        }
        if (request.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getAllergies() != null) {
            profile.setAllergies(request.getAllergies());
        }
        if (request.getChronicConditions() != null) {
            profile.setChronicConditions(request.getChronicConditions());
        }
        if (request.getCurrentMedications() != null) {
            profile.setCurrentMedications(request.getCurrentMedications());
        }
        if (request.getConsentForTreatment() != null) {
            profile.setConsentForTreatment(request.getConsentForTreatment());
        }
        if (request.getConsentForDataSharing() != null) {
            profile.setConsentForDataSharing(request.getConsentForDataSharing());
        }
        if (request.getConsentForMarketing() != null) {
            profile.setConsentForMarketing(request.getConsentForMarketing());
        }

        // Check if profile is complete
        profile.setProfileCompleted(isProfileComplete(profile));
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setUpdatedBy(userId.toString());

        profile = patientProfileRepository.save(profile);
        log.info("Patient profile updated for user: {}", userId);

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileResponse> getAllPatients() {
        log.info("Fetching all patient profiles");

        List<PatientProfile> profiles = patientProfileRepository.findByDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileResponse> searchPatients(String searchTerm) {
        log.info("Searching patients with term: {}", searchTerm);

        List<PatientProfile> profiles = patientProfileRepository.searchPatients(searchTerm);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileResponse> getIncompleteProfiles() {
        log.info("Fetching incomplete patient profiles");

        List<PatientProfile> profiles = patientProfileRepository.findByProfileCompletedFalseAndDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileResponse> getPatientsWithoutConsent() {
        log.info("Fetching patients without treatment consent");

        List<PatientProfile> profiles = patientProfileRepository.findByConsentForTreatmentFalseAndDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePatientProfile(UUID userId) {
        log.info("Deleting patient profile for user: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Find profile
        PatientProfile profile = patientProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Patient", userId));

        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityUtils.getCurrentUsername());
        userRepository.save(user);

        // Soft delete profile
        profile.setDeleted(true);
        profile.setDeletedAt(LocalDateTime.now());
        profile.setDeletedBy(SecurityUtils.getCurrentUsername());
        patientProfileRepository.save(profile);

        log.info("Patient profile deleted for user: {}", userId);
    }

    /**
     * Map PatientProfile entity to response DTO
     */
    private PatientProfileResponse mapToResponse(PatientProfile profile) {
        return PatientProfileResponse.builder()
                .patientProfileId(profile.getPatientProfileId())
                .userId(profile.getUser().getUserId())
                .firstName(profile.getFirstName())
                .middleName(profile.getMiddleName())
                .lastName(profile.getLastName())
                .fullName(buildFullName(profile))
                .dateOfBirth(profile.getDateOfBirth())
                .age(calculateAge(profile.getDateOfBirth()))
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .phoneNumber(profile.getPhoneNumber())
                .alternatePhoneNumber(profile.getAlternatePhoneNumber())
                .addressLine1(profile.getAddressLine1())
                .addressLine2(profile.getAddressLine2())
                .city(profile.getCity())
                .state(profile.getState())
                .postalCode(profile.getPostalCode())
                .country(profile.getCountry())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactRelationship(profile.getEmergencyContactRelationship())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .governmentIdType(profile.getGovernmentIdType())
                .governmentIdNumber(profile.getGovernmentIdNumber())
                .insuranceProvider(profile.getInsuranceProvider())
                .insurancePolicyNumber(profile.getInsurancePolicyNumber())
                .hasValidInsurance(hasValidInsurance(profile))
                .insuranceValidUntil(profile.getInsuranceValidUntil())
                .preferredLanguage(profile.getPreferredLanguage())
                .allergies(profile.getAllergies())
                .chronicConditions(profile.getChronicConditions())
                .currentMedications(profile.getCurrentMedications())
                .consentForTreatment(profile.isConsentForTreatment())
                .consentForDataSharing(profile.isConsentForDataSharing())
                .consentForMarketing(profile.isConsentForMarketing())
                .profileCompleted(profile.isProfileCompleted())
                .profileVerified(profile.isProfileVerified())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Build full name from profile
     */
    private String buildFullName(PatientProfile profile) {
        StringBuilder fullName = new StringBuilder();

        if (profile.getFirstName() != null) {
            fullName.append(profile.getFirstName());
        }
        if (profile.getMiddleName() != null && !profile.getMiddleName().isEmpty()) {
            if (fullName.length() > 0)
                fullName.append(" ");
            fullName.append(profile.getMiddleName());
        }
        if (profile.getLastName() != null) {
            if (fullName.length() > 0)
                fullName.append(" ");
            fullName.append(profile.getLastName());
        }

        return fullName.toString();
    }

    /**
     * Calculate age from date of birth
     */
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Check if insurance is valid
     */
    private Boolean hasValidInsurance(PatientProfile profile) {
        if (profile.getInsuranceValidUntil() == null) {
            return false;
        }
        return profile.getInsuranceValidUntil().isAfter(LocalDate.now());
    }

    /**
     * Check if profile is complete
     */
    private boolean isProfileComplete(PatientProfile profile) {
        return profile.getFirstName() != null &&
                profile.getLastName() != null &&
                profile.getDateOfBirth() != null &&
                profile.getGender() != null &&
                profile.getPhoneNumber() != null &&
                profile.getAddressLine1() != null &&
                profile.getCity() != null &&
                profile.getState() != null &&
                profile.getPostalCode() != null &&
                profile.getCountry() != null &&
                profile.getEmergencyContactName() != null &&
                profile.getEmergencyContactPhone() != null &&
                profile.isConsentForTreatment();
    }
}
