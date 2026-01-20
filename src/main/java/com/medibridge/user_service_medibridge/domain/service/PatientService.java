package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.UpdatePatientProfileRequest;
import com.medibridge.user_service_medibridge.api.dto.response.PatientProfileResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for patient operations.
 * 
 * Security: Patient can only access their own data
 * ADMIN can access all patient data
 */
public interface PatientService {

    /**
     * Get patient profile by user ID
     */
    PatientProfileResponse getPatientProfile(UUID userId);

    /**
     * Get patient profile by email
     */
    PatientProfileResponse getPatientProfileByEmail(String email);

    /**
     * Update patient profile
     */
    PatientProfileResponse updatePatientProfile(UUID userId, UpdatePatientProfileRequest request);

    /**
     * Update patient profile by email
     */
    PatientProfileResponse updatePatientProfileByEmail(String email, UpdatePatientProfileRequest request);

    /**
     * Get all patients (ADMIN only)
     */
    List<PatientProfileResponse> getAllPatients();

    /**
     * Search patients by name or phone (ADMIN only)
     */
    List<PatientProfileResponse> searchPatients(String searchTerm);

    /**
     * Get patients with incomplete profiles (ADMIN only)
     */
    List<PatientProfileResponse> getIncompleteProfiles();

    /**
     * Get patients without treatment consent (ADMIN only)
     */
    List<PatientProfileResponse> getPatientsWithoutConsent();

    /**
     * Delete patient profile (soft delete)
     */
    void deletePatientProfile(UUID userId);
}
