package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.CreateDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.request.VerifyDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.response.DoctorProfileResponse;
import com.medibridge.user_service_medibridge.util.constant.Specialization;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for doctor operations.
 * 
 * Security: Only ADMIN can create doctors
 * Public can view verified doctors
 */
public interface DoctorService {

    /**
     * Create new doctor (ADMIN only)
     */
    DoctorProfileResponse createDoctor(CreateDoctorRequest request, String createdBy);

    /**
     * Get doctor profile by user ID
     */
    DoctorProfileResponse getDoctorProfile(UUID userId);

    /**
     * Get doctor profile by email
     */
    DoctorProfileResponse getDoctorProfileByEmail(String email);

    /**
     * Get all doctors (ADMIN only - includes unverified)
     */
    List<DoctorProfileResponse> getAllDoctors();

    /**
     * Get all verified and active doctors (PUBLIC)
     */
    List<DoctorProfileResponse> getVerifiedDoctors();

    /**
     * Get doctors by specialization (PUBLIC - verified only)
     */
    List<DoctorProfileResponse> getDoctorsBySpecialization(Specialization specialization);

    /**
     * Get doctors by department (ADMIN)
     */
    List<DoctorProfileResponse> getDoctorsByDepartment(String department);

    /**
     * Get doctors available for consultation (PUBLIC)
     */
    List<DoctorProfileResponse> getAvailableDoctors();

    /**
     * Get doctors available for emergency (PUBLIC)
     */
    List<DoctorProfileResponse> getEmergencyDoctors();

    /**
     * Get pending verifications (ADMIN only)
     */
    List<DoctorProfileResponse> getPendingVerifications();

    /**
     * Verify doctor credentials (ADMIN only)
     */
    DoctorProfileResponse verifyDoctor(UUID doctorId, VerifyDoctorRequest request, String verifiedBy);

    /**
     * Search doctors by name or license (ADMIN)
     */
    List<DoctorProfileResponse> searchDoctors(String searchTerm);

    /**
     * Get doctors with expiring licenses (ADMIN)
     */
    List<DoctorProfileResponse> getDoctorsWithExpiringLicenses(int daysThreshold);

    /**
     * Delete doctor profile (soft delete - ADMIN only)
     */
    void deleteDoctorProfile(UUID userId);
}
