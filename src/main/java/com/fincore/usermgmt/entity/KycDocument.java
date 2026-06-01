package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entity representing a KYC (Know Your Customer) document.
 * Used for organisation and beneficiary verification and compliance.
 * 
 * <p>A document can belong to EITHER an Organisation OR a Beneficiary, not both.
 * One of {organisation, beneficiary} must be populated.</p>
 * 
 * @since 2.2.0 - Extended to support Beneficiary documents
 */
@Entity
@Table(name = "kyc_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Document_Identifier")
    private Long id;

    @Column(name = "Verification_Identifier")
    private Integer verificationIdentifier;

    // Organisation reference (for organisation documents)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Reference_Identifier")
    private Organisation organisation;

    // Beneficiary reference (for beneficiary documents) - Added in v2.2.0
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Beneficiary_Identifier")
    private Beneficiary beneficiary;

    @Enumerated(EnumType.STRING)
    @Column(name = "Document_Type_Description", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "Sumsub_Document_Identifier", length = 100)
    private String sumsubDocumentIdentifier;

    @Column(name = "File_Name", length = 255)
    private String fileName;

    @Column(name = "File_URL", columnDefinition = "TEXT")
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status_Description", length = 20)
    private DocumentStatus status;

    @Column(name = "Reason_Description", columnDefinition = "TEXT")
    private String reasonDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Document_Verified_By")
    private User verifiedBy;

    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "Created_By")
    private Long createdBy;

    @Column(name = "Last_Modified_Datetime")
    private LocalDateTime lastModifiedDatetime;

    @Column(name = "Last_Modified_By")
    private Long lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
        lastModifiedDatetime = LocalDateTime.now();
        if (status == null) {
            status = DocumentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }

    /**
     * Check if this document belongs to an organisation.
     * @return true if organisation reference is populated
     */
    public boolean isOrganisationDocument() {
        return organisation != null;
    }

    /**
     * Check if this document belongs to a beneficiary.
     * @return true if beneficiary reference is populated
     */
    public boolean isBeneficiaryDocument() {
        return beneficiary != null;
    }

    /**
     * Get the reference identifier (organisation or beneficiary ID).
     * @return organisation ID if org document, beneficiary ID if beneficiary document
     */
    public Long getReferenceId() {
        if (organisation != null) {
            return organisation.getId();
        } else if (beneficiary != null) {
            return beneficiary.getId();
        }
        return null;
    }

    /**
     * Get the reference type (ORGANISATION or BENEFICIARY).
     * @return reference type as string
     */
    public String getReferenceType() {
        if (organisation != null) {
            return "ORGANISATION";
        } else if (beneficiary != null) {
            return "BENEFICIARY";
        }
        return "UNKNOWN";
    }
}

