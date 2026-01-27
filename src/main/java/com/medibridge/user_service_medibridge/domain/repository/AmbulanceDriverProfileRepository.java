package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.AmbulanceDriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AmbulanceDriverProfile entity
 * Provides database access methods for driver profiles
 */
@Repository
public interface AmbulanceDriverProfileRepository extends JpaRepository<AmbulanceDriverProfile, UUID> {

    /**
     * Find driver profile by user ID
     */
    Optional<AmbulanceDriverProfile> findByUserId(UUID userId);

    /**
     * Find driver profile by license number
     */
    Optional<AmbulanceDriverProfile> findByLicenseNumber(String licenseNumber);

    /**
     * Find driver profile by ambulance ID
     */
    Optional<AmbulanceDriverProfile> findByAmbulanceId(UUID ambulanceId);

    /**
     * Find all active drivers in an organization
     */
    @Query("SELECT p FROM AmbulanceDriverProfile p WHERE p.organizationId = :orgId AND p.isActive = true")
    List<AmbulanceDriverProfile> findAllActiveByOrganization(@Param("orgId") UUID orgId);

    /**
     * Find all on-duty drivers in an organization
     */
    @Query("SELECT p FROM AmbulanceDriverProfile p WHERE p.organizationId = :orgId AND p.onDutyStatus IN ('ON_DUTY', 'RESPONDING')")
    List<AmbulanceDriverProfile> findAllOnDutyByOrganization(@Param("orgId") UUID orgId);

    /**
     * Find all available drivers (on duty and not busy)
     */
    @Query("SELECT p FROM AmbulanceDriverProfile p WHERE p.organizationId = :orgId AND p.onDutyStatus = 'ON_DUTY' AND p.isActive = true")
    List<AmbulanceDriverProfile> findAllAvailableByOrganization(@Param("orgId") UUID orgId);

    /**
     * Find drivers in a service area
     */
    @Query("SELECT p FROM AmbulanceDriverProfile p WHERE p.organizationId = :orgId AND p.serviceArea = :serviceArea AND p.isActive = true")
    List<AmbulanceDriverProfile> findByServiceArea(@Param("orgId") UUID orgId, @Param("serviceArea") String serviceArea);

    /**
     * Count active drivers in organization
     */
    Long countByOrganizationIdAndIsActive(UUID orgId, Boolean isActive);
}
