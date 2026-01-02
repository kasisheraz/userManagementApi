package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.DocumentStatus;
import com.fincore.usermgmt.entity.DocumentType;
import com.fincore.usermgmt.entity.KycDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for KycDocument entity operations.
 */
@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    /**
     * Find documents by organisation.
     * @param organisationId the organisation ID
     * @return list of documents for the organisation
     */
    @Query("SELECT d FROM KycDocument d WHERE d.organisation.id = :organisationId")
    List<KycDocument> findByOrganisationId(@Param("organisationId") Long organisationId);

    /**
     * Find documents by organisation with pagination.
     * @param organisationId the organisation ID
     * @param pageable pagination information
     * @return page of documents
     */
    @Query("SELECT d FROM KycDocument d WHERE d.organisation.id = :organisationId")
    Page<KycDocument> findByOrganisationId(@Param("organisationId") Long organisationId, Pageable pageable);

    /**
     * Find documents by status.
     * @param status the document status
     * @return list of documents with the specified status
     */
    List<KycDocument> findByStatus(DocumentStatus status);

    /**
     * Find documents by type.
     * @param documentType the document type
     * @return list of documents of the specified type
     */
    List<KycDocument> findByDocumentType(DocumentType documentType);

    /**
     * Find documents by organisation and type.
     * @param organisationId the organisation ID
     * @param documentType the document type
     * @return list of matching documents
     */
    @Query("SELECT d FROM KycDocument d WHERE d.organisation.id = :organisationId AND d.documentType = :documentType")
    List<KycDocument> findByOrganisationIdAndDocumentType(
            @Param("organisationId") Long organisationId,
            @Param("documentType") DocumentType documentType);

    /**
     * Find documents by organisation and status.
     * @param organisationId the organisation ID
     * @param status the document status
     * @return list of matching documents
     */
    @Query("SELECT d FROM KycDocument d WHERE d.organisation.id = :organisationId AND d.status = :status")
    List<KycDocument> findByOrganisationIdAndStatus(
            @Param("organisationId") Long organisationId,
            @Param("status") DocumentStatus status);

    /**
     * Find document by Sumsub document identifier.
     * @param sumsubDocumentIdentifier the external system reference
     * @return optional document
     */
    Optional<KycDocument> findBySumsubDocumentIdentifier(String sumsubDocumentIdentifier);

    /**
     * Find documents pending verification.
     * @return list of pending documents
     */
    @Query("SELECT d FROM KycDocument d WHERE d.status = 'PENDING' OR d.status = 'UNDER_REVIEW'")
    List<KycDocument> findPendingVerification();

    /**
     * Count documents by organisation and status.
     * @param organisationId the organisation ID
     * @param status the document status
     * @return count of matching documents
     */
    @Query("SELECT COUNT(d) FROM KycDocument d WHERE d.organisation.id = :organisationId AND d.status = :status")
    long countByOrganisationIdAndStatus(
            @Param("organisationId") Long organisationId,
            @Param("status") DocumentStatus status);

    /**
     * Check if organisation has all required verified documents.
     * @param organisationId the organisation ID
     * @return count of verified documents
     */
    @Query("SELECT COUNT(d) FROM KycDocument d WHERE d.organisation.id = :organisationId AND d.status = 'VERIFIED'")
    long countVerifiedDocumentsByOrganisation(@Param("organisationId") Long organisationId);
}
