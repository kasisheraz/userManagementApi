package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Organisation;
import com.fincore.usermgmt.entity.OrganisationStatus;
import com.fincore.usermgmt.entity.OrganisationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Organisation entity operations.
 */
@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    /**
     * Find organisation by registration number.
     * @param registrationNumber the company registration number
     * @return optional organisation
     */
    Optional<Organisation> findByRegistrationNumber(String registrationNumber);

    /**
     * Find organisation by company number.
     * @param companyNumber the company number
     * @return optional organisation
     */
    Optional<Organisation> findByCompanyNumber(String companyNumber);

    /**
     * Find organisation by legal name.
     * @param legalName the legal name of the organisation
     * @return optional organisation
     */
    Optional<Organisation> findByLegalName(String legalName);

    /**
     * Find organisations by owner (user).
     * @param ownerId the owner user ID
     * @return list of organisations owned by the user
     */
    @Query("SELECT o FROM Organisation o WHERE o.owner.id = :ownerId")
    List<Organisation> findByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * Find organisations by status.
     * @param status the organisation status
     * @return list of organisations with the specified status
     */
    List<Organisation> findByStatus(OrganisationStatus status);

    /**
     * Find organisations by type.
     * @param organisationType the organisation type
     * @return list of organisations of the specified type
     */
    List<Organisation> findByOrganisationType(OrganisationType organisationType);

    /**
     * Find organisations by country of incorporation.
     * @param countryOfIncorporation the country
     * @return list of organisations incorporated in the country
     */
    List<Organisation> findByCountryOfIncorporation(String countryOfIncorporation);

    /**
     * Search organisations by legal name containing the search term.
     * @param searchTerm the search term
     * @return list of matching organisations
     */
    List<Organisation> findByLegalNameContainingIgnoreCase(String searchTerm);

    /**
     * Check if an organisation exists with the given registration number.
     * @param registrationNumber the registration number
     * @return true if exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Check if an organisation exists with the given company number.
     * @param companyNumber the company number
     * @return true if exists
     */
    boolean existsByCompanyNumber(String companyNumber);

    /**
     * Find all organisations with pagination.
     * @param pageable pagination information
     * @return page of organisations
     */
    Page<Organisation> findAll(Pageable pageable);

    /**
     * Find organisations by status with pagination.
     * @param status the organisation status
     * @param pageable pagination information
     * @return page of organisations
     */
    Page<Organisation> findByStatus(OrganisationStatus status, Pageable pageable);

    /**
     * Search organisations by multiple criteria.
     * @param searchTerm search term for name
     * @param status optional status filter
     * @param organisationType optional type filter
     * @param pageable pagination information
     * @return page of matching organisations
     */
    @Query("SELECT o FROM Organisation o WHERE " +
           "(:searchTerm IS NULL OR LOWER(o.legalName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(o.businessName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:organisationType IS NULL OR o.organisationType = :organisationType)")
    Page<Organisation> searchOrganisations(
            @Param("searchTerm") String searchTerm,
            @Param("status") OrganisationStatus status,
            @Param("organisationType") OrganisationType organisationType,
            Pageable pageable);
}
