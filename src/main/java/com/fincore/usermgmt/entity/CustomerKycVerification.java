package com.fincore.usermgmt.entity;

import com.fincore.usermgmt.entity.enums.RiskLevel;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing KYC (Know Your Customer) verification records
 * Tracks the verification status, level, risk assessment, and audit trail
 */
@Entity
@Table(name = "customer_kyc_verification", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_verification_level", columnList = "verification_level"),
    @Index(name = "idx_sumsub_applicant_id", columnList = "sumsub_applicant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerKycVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sumsub_applicant_id", unique = true, length = 100)
    private String sumsubApplicantId;

    @Column(name = "verification_level", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private VerificationLevel verificationLevel;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "review_result", columnDefinition = "JSON")
    private String reviewResult;

    @Column(name = "risk_level", length = 20)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_datetime", nullable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @UpdateTimestamp
    @Column(name = "last_modified_datetime")
    private LocalDateTime lastModifiedDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AmlScreeningResult> amlScreenings = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdDatetime == null) {
            createdDatetime = LocalDateTime.now();
        }
        if (lastModifiedDatetime == null) {
            lastModifiedDatetime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }
}
