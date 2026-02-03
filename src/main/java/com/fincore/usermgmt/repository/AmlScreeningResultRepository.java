package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.enums.ScreeningType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AmlScreeningResult entity
 * Provides database operations for AML screening records
 */
@Repository
public interface AmlScreeningResultRepository extends JpaRepository<AmlScreeningResult, Long> {

    /**
     * Find screenings by verification ID
     */
    List<AmlScreeningResult> findByVerification_VerificationId(Long verificationId);

    /**
     * Find screening by verification ID with pagination
     */
    Page<AmlScreeningResult> findByVerification_VerificationId(Long verificationId, Pageable pageable);

    /**
     * Find screenings by user ID
     */
    List<AmlScreeningResult> findByUser_Id(Long userId);

    /**
     * Find screenings by screening type
     */
    List<AmlScreeningResult> findByScreeningType(ScreeningType screeningType);

    /**
     * Find screenings by screening type with pagination
     */
    Page<AmlScreeningResult> findByScreeningType(ScreeningType screeningType, Pageable pageable);

    /**
     * Find high risk screenings (matches found)
     */
    @Query("SELECT s FROM AmlScreeningResult s WHERE s.matchFound = true ORDER BY s.riskScore DESC")
    List<AmlScreeningResult> findHighRiskScreenings();

    /**
     * Find high risk screenings with pagination
     */
    @Query("SELECT s FROM AmlScreeningResult s WHERE s.matchFound = true ORDER BY s.riskScore DESC")
    Page<AmlScreeningResult> findHighRiskScreenings(Pageable pageable);

    /**
     * Find latest screening for a verification
     */
    @Query(value = "SELECT * FROM aml_screening_results WHERE verification_id = :verificationId ORDER BY screened_at DESC LIMIT 1", nativeQuery = true)
    Optional<AmlScreeningResult> findLatestByVerificationId(@Param("verificationId") Long verificationId);

    /**
     * Count screenings by screening type
     */
    @Query("SELECT COUNT(s) FROM AmlScreeningResult s WHERE s.screeningType = :screeningType AND s.matchFound = true")
    long countMatchesByScreeningType(@Param("screeningType") ScreeningType screeningType);
}
