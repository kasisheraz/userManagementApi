package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerKycVerification entity
 * Provides database operations for KYC verification records
 */
@Repository
public interface CustomerKycVerificationRepository extends JpaRepository<CustomerKycVerification, Long> {

    /**
     * Find verification by user ID
     */
    Optional<CustomerKycVerification> findByUser_Id(Long userId);

    /**
     * Find verification by Sumsub applicant ID
     */
    Optional<CustomerKycVerification> findBySumsubApplicantId(String sumsubApplicantId);

    /**
     * Find verifications by status
     */
    List<CustomerKycVerification> findByStatus(VerificationStatus status);

    /**
     * Find verifications by status with pagination
     */
    Page<CustomerKycVerification> findByStatus(VerificationStatus status, Pageable pageable);

    /**
     * Find expired verifications
     */
    @Query("SELECT v FROM CustomerKycVerification v WHERE v.expiresAt IS NOT NULL AND v.expiresAt < CURRENT_TIMESTAMP AND v.status != 'EXPIRED'")
    List<CustomerKycVerification> findExpiredVerifications();

    /**
     * Find verification by user ID and status
     */
    @Query("SELECT v FROM CustomerKycVerification v WHERE v.user.id = :userId AND v.status = :status")
    Optional<CustomerKycVerification> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") VerificationStatus status);

    /**
     * Count verifications by status
     */
    @Query("SELECT COUNT(v) FROM CustomerKycVerification v WHERE v.status = :status")
    long countByStatus(@Param("status") VerificationStatus status);

    /**
     * Find latest verification for a user
     */
    @Query(value = "SELECT * FROM customer_kyc_verification WHERE user_id = :userId ORDER BY created_datetime DESC LIMIT 1", nativeQuery = true)
    Optional<CustomerKycVerification> findLatestByUserId(@Param("userId") Long userId);
}
