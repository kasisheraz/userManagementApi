package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entity representing a KYC (Know Your Customer) document.
 * Used for organisation verification and compliance.
 */
@Entity
@Table(name = "KYC_Documents")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Reference_Identifier", nullable = false)
    private Organisation organisation;

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
}
