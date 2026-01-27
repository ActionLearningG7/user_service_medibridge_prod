package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.AdminProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AdminProfile entity operations.
 * 
 * Security: Highest privilege level - strict access control
 * Compliance: Supports permission and compliance queries
 */
@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {

        // User relationship queries
        Optional<AdminProfile> findByUser(User user);

        Optional<AdminProfile> findByUserUserId(UUID userId);

        Optional<AdminProfile> findByUserUserIdAndDeletedFalse(UUID userId);

        Optional<AdminProfile> findByUserEmailAndDeletedFalse(String email);

        @Query("SELECT a FROM AdminProfile a WHERE a.user.userId = :userId AND a.deleted = false")
        Optional<AdminProfile> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

        // Admin level queries
        List<AdminProfile> findByAdminLevel(AdminLevel adminLevel);

        List<AdminProfile> findByAdminLevelAndDeletedFalse(AdminLevel adminLevel);

        @Query("SELECT a FROM AdminProfile a WHERE a.adminLevel = :level AND a.active = true AND a.deleted = false")
        List<AdminProfile> findActiveByAdminLevel(@Param("level") AdminLevel level);

        @Query("SELECT a FROM AdminProfile a WHERE a.deleted = false ORDER BY a.adminLevel ASC")
        List<AdminProfile> findAllOrderedByLevel();

        // Permission queries
        List<AdminProfile> findByCanVerifyDoctorsTrueAndDeletedFalse();

        @Query("SELECT a FROM AdminProfile a WHERE a.canVerifyDoctors = true AND a.active = true AND a.deleted = false")
        List<AdminProfile> findAdminsWhoCanVerifyDoctors();

        @Query("SELECT a FROM AdminProfile a WHERE a.canManageSystemConfig = true AND a.active = true AND a.deleted = false")
        List<AdminProfile> findAdminsWhoCanManageSystem();

        @Query("SELECT a FROM AdminProfile a WHERE a.canDeleteUsers = true AND a.active = true AND a.deleted = false")
        List<AdminProfile> findAdminsWhoCanDeleteUsers();

        // Department queries
        List<AdminProfile> findByDepartmentAndDeletedFalse(String department);

        @Query("SELECT a FROM AdminProfile a WHERE a.department = :department AND a.active = true AND a.deleted = false")
        List<AdminProfile> findActiveByDepartment(@Param("department") String department);

        // Status queries
        List<AdminProfile> findByActiveAndDeletedFalse(Boolean active);

        List<AdminProfile> findByDeletedFalse();

        long countByDeletedFalse();

        List<AdminProfile> findBySuspendedAndDeletedFalse(Boolean suspended);

        @Query("SELECT a FROM AdminProfile a WHERE a.active = true AND a.suspended = false AND a.deleted = false")
        List<AdminProfile> findAllActiveAndNotSuspended();

        // Compliance queries
        @Query("SELECT a FROM AdminProfile a WHERE a.hipaaTrainingCompleted = false AND a.active = true AND a.deleted = false")
        List<AdminProfile> findAdminsWithoutHipaaTraining();

        @Query("SELECT a FROM AdminProfile a WHERE a.dataPrivacyAcknowledged = false AND a.active = true AND a.deleted = false")
        List<AdminProfile> findAdminsWithoutPrivacyAcknowledgement();

        @Query("SELECT a FROM AdminProfile a WHERE " +
                        "(a.hipaaTrainingCompleted = false OR a.dataPrivacyAcknowledged = false OR a.codeOfConductAccepted = false) "
                        +
                        "AND a.active = true AND a.deleted = false")
        List<AdminProfile> findNonCompliantAdmins();

        // Search queries
        @Query("SELECT a FROM AdminProfile a WHERE " +
                        "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(a.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "AND a.deleted = false")
        List<AdminProfile> searchAdmins(@Param("searchTerm") String searchTerm);

        // Statistics
        @Query("SELECT COUNT(a) FROM AdminProfile a WHERE a.deleted = false")
        long countAllAdmins();

        @Query("SELECT COUNT(a) FROM AdminProfile a WHERE a.active = true AND a.deleted = false")
        long countActiveAdmins();

        @Query("SELECT COUNT(a) FROM AdminProfile a WHERE a.suspended = true AND a.deleted = false")
        long countSuspendedAdmins();

        @Query("SELECT COUNT(a) FROM AdminProfile a WHERE a.adminLevel = :level AND a.deleted = false")
        long countByAdminLevel(@Param("level") AdminLevel level);
}
