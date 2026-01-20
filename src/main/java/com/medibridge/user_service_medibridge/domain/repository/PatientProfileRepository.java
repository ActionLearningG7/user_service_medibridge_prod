package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.PatientProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.util.constant.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PatientProfile entity operations.
 * 
 * Security: Contains PII/PHI - use with caution
 * Compliance: Supports consent and verification queries
 */
@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {

    // User relationship queries
    Optional<PatientProfile> findByUser(User user);

    Optional<PatientProfile> findByUserUserId(UUID userId);

    Optional<PatientProfile> findByUserUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT p FROM PatientProfile p WHERE p.user.userId = :userId AND p.deleted = false")
    Optional<PatientProfile> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    // Contact queries
    Optional<PatientProfile> findByPhoneNumber(String phoneNumber);

    List<PatientProfile> findByPhoneNumberAndDeletedFalse(String phoneNumber);

    // Government ID queries (encrypted in production)
    @Query("SELECT p FROM PatientProfile p WHERE p.governmentIdNumber = :idNumber AND p.deleted = false")
    Optional<PatientProfile> findByGovernmentIdNumber(@Param("idNumber") String idNumber);

    // Profile status queries
    List<PatientProfile> findByProfileCompletedAndDeletedFalse(Boolean profileCompleted);

    List<PatientProfile> findByProfileCompletedFalseAndDeletedFalse();

    List<PatientProfile> findByProfileVerifiedAndDeletedFalse(Boolean profileVerified);

    List<PatientProfile> findByDeletedFalse();

    @Query("SELECT p FROM PatientProfile p WHERE p.profileCompleted = false AND p.deleted = false")
    List<PatientProfile> findIncompleteProfiles();

    // Consent queries
    @Query("SELECT p FROM PatientProfile p WHERE p.consentForTreatment = true AND p.deleted = false")
    List<PatientProfile> findPatientsWithTreatmentConsent();

    List<PatientProfile> findByConsentForTreatmentFalseAndDeletedFalse();

    @Query("SELECT p FROM PatientProfile p WHERE p.consentForDataSharing = true AND p.deleted = false")
    List<PatientProfile> findPatientsWithDataSharingConsent();

    // Insurance queries
    @Query("SELECT p FROM PatientProfile p WHERE p.insuranceValidUntil >= :date AND p.deleted = false")
    List<PatientProfile> findPatientsWithValidInsurance(@Param("date") LocalDate date);

    @Query("SELECT p FROM PatientProfile p WHERE p.insuranceValidUntil < :date AND p.insuranceValidUntil IS NOT NULL AND p.deleted = false")
    List<PatientProfile> findPatientsWithExpiredInsurance(@Param("date") LocalDate date);

    // Demographics queries
    List<PatientProfile> findByBloodGroupAndDeletedFalse(BloodGroup bloodGroup);

    @Query("SELECT p FROM PatientProfile p WHERE p.city = :city AND p.deleted = false")
    List<PatientProfile> findByCity(@Param("city") String city);

    @Query("SELECT p FROM PatientProfile p WHERE p.state = :state AND p.deleted = false")
    List<PatientProfile> findByState(@Param("state") String state);

    // Search queries
    @Query("SELECT p FROM PatientProfile p WHERE " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND p.deleted = false")
    List<PatientProfile> searchPatients(@Param("searchTerm") String searchTerm);

    // Statistics
    @Query("SELECT COUNT(p) FROM PatientProfile p WHERE p.deleted = false")
    long countActivePatients();

    @Query("SELECT COUNT(p) FROM PatientProfile p WHERE p.profileCompleted = true AND p.deleted = false")
    long countCompletedProfiles();

    @Query("SELECT COUNT(p) FROM PatientProfile p WHERE p.consentForTreatment = true AND p.deleted = false")
    long countPatientsWithConsent();
}
