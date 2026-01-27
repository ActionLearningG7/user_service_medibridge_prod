package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.DoctorProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.util.constant.Specialization;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DoctorProfile entity operations.
 * 
 * Security: Contains professional credentials - verification required
 * Compliance: Supports license tracking and verification workflows
 */
@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

        // User relationship queries
        Optional<DoctorProfile> findByUser(User user);

        Optional<DoctorProfile> findByUserUserId(UUID userId);

        Optional<DoctorProfile> findByUserUserIdAndDeletedFalse(UUID userId);

        Optional<DoctorProfile> findByUserEmailAndDeletedFalse(String email);

        @Query("SELECT d FROM DoctorProfile d WHERE d.user.userId = :userId AND d.deleted = false")
        Optional<DoctorProfile> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

        // License queries
        Optional<DoctorProfile> findByMedicalLicenseNumber(String licenseNumber);

        @Query("SELECT d FROM DoctorProfile d WHERE d.medicalLicenseNumber = :licenseNumber AND d.deleted = false")
        Optional<DoctorProfile> findByMedicalLicenseNumberAndNotDeleted(@Param("licenseNumber") String licenseNumber);

        boolean existsByMedicalLicenseNumber(String licenseNumber);

        // Verification queries
        List<DoctorProfile> findByVerificationStatus(VerificationStatus status);

        List<DoctorProfile> findByVerificationStatusAndDeletedFalse(VerificationStatus status);

        @Query("SELECT d FROM DoctorProfile d WHERE d.verificationStatus = :status AND d.deleted = false")
        List<DoctorProfile> findByVerificationStatusAndNotDeleted(@Param("status") VerificationStatus status);

        @Query("SELECT d FROM DoctorProfile d WHERE d.verificationStatus = 'PENDING' AND d.deleted = false ORDER BY d.createdAt ASC")
        List<DoctorProfile> findPendingVerifications();

        // Specialization queries
        List<DoctorProfile> findBySpecialization(Specialization specialization);

        List<DoctorProfile> findBySpecializationAndVerificationStatusAndDeletedFalse(Specialization specialization,
                        VerificationStatus status);

        @Query("SELECT d FROM DoctorProfile d WHERE d.specialization = :specialization AND d.active = true AND d.deleted = false")
        List<DoctorProfile> findActiveBySpecialization(@Param("specialization") Specialization specialization);

        @Query("SELECT d FROM DoctorProfile d WHERE d.specialization = :specialization AND d.verificationStatus = 'VERIFIED' AND d.active = true AND d.deleted = false")
        List<DoctorProfile> findVerifiedBySpecialization(@Param("specialization") Specialization specialization);

        // Department queries
        List<DoctorProfile> findByDepartmentAndDeletedFalse(String department);

        @Query("SELECT d FROM DoctorProfile d WHERE d.department = :department AND d.active = true AND d.deleted = false")
        List<DoctorProfile> findActiveByDepartment(@Param("department") String department);

        // Availability queries
        @Query("SELECT d FROM DoctorProfile d WHERE d.availableForConsultation = true AND d.verificationStatus = :status AND d.deleted = false")
        List<DoctorProfile> findByAvailableForConsultationTrueAndVerificationStatusAndDeletedFalse(
                        @Param("status") VerificationStatus status);

        @Query("SELECT d FROM DoctorProfile d WHERE d.availableForEmergency = true AND d.verificationStatus = :status AND d.deleted = false")
        List<DoctorProfile> findByAvailableForEmergencyTrueAndVerificationStatusAndDeletedFalse(
                        @Param("status") VerificationStatus status);

        @Query("SELECT d FROM DoctorProfile d WHERE d.availableForConsultation = true AND d.active = true AND d.verificationStatus = 'VERIFIED' AND d.deleted = false")
        List<DoctorProfile> findAvailableForConsultation();

        @Query("SELECT d FROM DoctorProfile d WHERE d.availableForEmergency = true AND d.active = true AND d.verificationStatus = 'VERIFIED' AND d.deleted = false")
        List<DoctorProfile> findAvailableForEmergency();

        @Query("SELECT d FROM DoctorProfile d WHERE d.specialization = :specialization AND d.availableForConsultation = true AND d.active = true AND d.verificationStatus = 'VERIFIED' AND d.deleted = false")
        List<DoctorProfile> findAvailableBySpecialization(@Param("specialization") Specialization specialization);

        // License expiry queries
        List<DoctorProfile> findByMedicalLicenseExpiryDateBeforeAndDeletedFalse(LocalDate date);

        @Query("SELECT d FROM DoctorProfile d WHERE d.medicalLicenseExpiryDate <= :date AND d.active = true AND d.deleted = false")
        List<DoctorProfile> findDoctorsWithExpiringLicense(@Param("date") LocalDate date);

        @Query("SELECT d FROM DoctorProfile d WHERE d.medicalLicenseExpiryDate < :date AND d.active = true AND d.deleted = false")
        List<DoctorProfile> findDoctorsWithExpiredLicense(@Param("date") LocalDate date);

        // Active status queries
        List<DoctorProfile> findByActiveAndDeletedFalse(Boolean active);

        List<DoctorProfile> findByDeletedFalse();

        @Query("SELECT d FROM DoctorProfile d WHERE d.active = true AND d.verificationStatus = 'VERIFIED' AND d.deleted = false")
        List<DoctorProfile> findAllActiveAndVerified();

        // Search queries
        @Query("SELECT d FROM DoctorProfile d WHERE " +
                        "(LOWER(d.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(d.medicalLicenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "AND d.deleted = false")
        List<DoctorProfile> searchDoctors(@Param("searchTerm") String searchTerm);

        // Statistics
        @Query("SELECT COUNT(d) FROM DoctorProfile d WHERE d.deleted = false")
        long countAllDoctors();

        @Query("SELECT COUNT(d) FROM DoctorProfile d WHERE d.active = true AND d.deleted = false")
        long countActiveDoctors();

        @Query("SELECT COUNT(d) FROM DoctorProfile d WHERE d.verificationStatus = 'VERIFIED' AND d.deleted = false")
        long countVerifiedDoctors();

        @Query("SELECT COUNT(d) FROM DoctorProfile d WHERE d.verificationStatus = 'PENDING' AND d.deleted = false")
        long countPendingVerifications();

        @Query("SELECT COUNT(d) FROM DoctorProfile d WHERE d.specialization = :specialization AND d.active = true AND d.deleted = false")
        long countBySpecialization(@Param("specialization") Specialization specialization);
}
