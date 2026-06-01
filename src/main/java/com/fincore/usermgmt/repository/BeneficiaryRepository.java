package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Beneficiary;
import com.fincore.usermgmt.entity.BeneficiaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Beneficiary entity.
 * Provides CRUD operations and custom queries for beneficiary management.
 * 
 * <p>Key Features:</p>
 * <ul>
 *   <li>User-scoped queries (users see only their beneficiaries)</li>
 *   <li>Status-based filtering</li>
 *   <li>Search by name, business name, country</li>
 *   <li>Count validation for 20-beneficiary limit</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    /**
     * Find all beneficiaries for a specific user.
     * Business rule: Users can only see their own beneficiaries.
     * 
     * @param userId The user identifier
     * @return List of beneficiaries owned by the user
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.owner.id = :userId ORDER BY b.createdDatetime DESC")
    List<Beneficiary> findByUserId(@Param("userId") Long userId);

    /**
     * Find all beneficiaries for a user with a specific status.
     * 
     * @param userId The user identifier
     * @param status The beneficiary status
     * @return List of beneficiaries matching criteria
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.owner.id = :userId AND b.status = :status ORDER BY b.createdDatetime DESC")
    List<Beneficiary> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BeneficiaryStatus status);

    /**
     * Count total beneficiaries for a user.
     * Business rule: Users can create maximum 20 beneficiaries.
     * 
     * @param userId The user identifier
     * @return Total count of user's beneficiaries
     */
    @Query("SELECT COUNT(b) FROM Beneficiary b WHERE b.owner.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Count active beneficiaries for a user.
     * 
     * @param userId The user identifier
     * @return Count of active beneficiaries
     */
    @Query("SELECT COUNT(b) FROM Beneficiary b WHERE b.owner.id = :userId AND b.status = 'ACTIVE'")
    long countActiveByUserId(@Param("userId") Long userId);

    /**
     * Check if a user has reached the 20-beneficiary limit.
     * 
     * @param userId The user identifier
     * @return true if user has 20 or more beneficiaries
     */
    default boolean hasReachedLimit(Long userId) {
        return countByUserId(userId) >= 20;
    }

    /**
     * Find a beneficiary by ID and verify ownership.
     * Security: Ensures users can only access their own beneficiaries.
     * 
     * @param beneficiaryId The beneficiary identifier
     * @param userId The user identifier
     * @return Optional containing beneficiary if found and owned by user
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.id = :beneficiaryId AND b.owner.id = :userId")
    Optional<Beneficiary> findByIdAndUserId(@Param("beneficiaryId") Long beneficiaryId, @Param("userId") Long userId);

    /**
     * Search beneficiaries by name (beneficiary name or business name).
     * Case-insensitive partial match.
     * 
     * @param userId The user identifier
     * @param searchTerm The search term
     * @return List of matching beneficiaries
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.owner.id = :userId AND " +
           "(LOWER(b.beneficiaryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.businessName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.nickName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY b.createdDatetime DESC")
    List<Beneficiary> searchByName(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Filter beneficiaries by country.
     * 
     * @param userId The user identifier
     * @param country The country code/name
     * @return List of beneficiaries in specified country
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.owner.id = :userId AND b.country = :country ORDER BY b.createdDatetime DESC")
    List<Beneficiary> findByUserIdAndCountry(@Param("userId") Long userId, @Param("country") String country);

    /**
     * Find all Counter Over Counter (C2C) beneficiaries for a user.
     * 
     * @param userId The user identifier
     * @return List of C2C beneficiaries
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.owner.id = :userId AND b.isCounterOverCounter = true ORDER BY b.createdDatetime DESC")
    List<Beneficiary> findC2CBeneficiaries(@Param("userId") Long userId);

    // ========================================
    // Admin-Only Queries (All Users)
    // ========================================

    /**
     * Find all beneficiaries with a specific status (admin view).
     * Used by SYSTEM_ADMINISTRATOR and COMPLIANCE_OFFICER roles.
     * 
     * @param status The beneficiary status
     * @return List of all beneficiaries with specified status
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.status = :status ORDER BY b.createdDatetime DESC")
    List<Beneficiary> findByStatus(@Param("status") BeneficiaryStatus status);

    /**
     * Find all beneficiaries pending approval (admin queue).
     * 
     * @return List of beneficiaries awaiting admin review
     */
    @Query("SELECT b FROM Beneficiary b WHERE b.status = 'UNDER_REVIEW' ORDER BY b.createdDatetime ASC")
    List<Beneficiary> findPendingApprovals();

    /**
     * Get statistics: count beneficiaries by status (admin dashboard).
     * 
     * @return List of [status, count] pairs
     */
    @Query("SELECT b.status, COUNT(b) FROM Beneficiary b GROUP BY b.status")
    List<Object[]> getStatusStatistics();

    /**
     * Search all beneficiaries by name (admin view).
     * 
     * @param searchTerm The search term
     * @return List of matching beneficiaries across all users
     */
    @Query("SELECT b FROM Beneficiary b WHERE " +
           "LOWER(b.beneficiaryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.businessName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.nickName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY b.createdDatetime DESC")
    List<Beneficiary> adminSearchByName(@Param("searchTerm") String searchTerm);
}
