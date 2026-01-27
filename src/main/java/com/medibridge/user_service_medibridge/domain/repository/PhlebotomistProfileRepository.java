package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.PhlebotomistProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhlebotomistProfileRepository extends JpaRepository<PhlebotomistProfile, UUID> {

    Optional<PhlebotomistProfile> findByUser_UserId(UUID userId);

    Optional<PhlebotomistProfile> findByCertificationNumber(String certificationNumber);

    List<PhlebotomistProfile> findByActiveTrue();

    boolean existsByCertificationNumber(String certificationNumber);

    /**
     * Get phlebotomists with pagination and optional filtering
     */
    @Query("SELECT p FROM PhlebotomistProfile p WHERE p.deleted = false")
    Page<PhlebotomistProfile> findAllActive(Pageable pageable);

    /**
     * Search phlebotomists by name or email
     */
    @Query("""
        SELECT p FROM PhlebotomistProfile p 
        WHERE p.deleted = false 
        AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<PhlebotomistProfile> searchByNameOrEmail(@Param("search") String search, Pageable pageable);

    /**
     * Get phlebotomists filtered by status
     */
    @Query("""
        SELECT p FROM PhlebotomistProfile p 
        WHERE p.deleted = false AND p.active = :active
    """)
    Page<PhlebotomistProfile> findByStatus(@Param("active") boolean active, Pageable pageable);

    /**
     * Search and filter by status
     */
    @Query("""
        SELECT p FROM PhlebotomistProfile p 
        WHERE p.deleted = false 
        AND p.active = :active
        AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<PhlebotomistProfile> searchByNameOrEmailAndStatus(
            @Param("search") String search,
            @Param("active") boolean active,
            Pageable pageable
    );
}


