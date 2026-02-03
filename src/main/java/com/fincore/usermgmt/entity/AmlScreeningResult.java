package com.fincore.usermgmt.entity;

import com.fincore.usermgmt.entity.enums.ScreeningType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing AML (Anti-Money Laundering) screening results
 * Stores screening results, match information, and risk scores
 */
@Entity
@Table(name = "aml_screening_results", indexes = {
    @Index(name = "idx_verification_id", columnList = "verification_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_screening_type", columnList = "screening_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmlScreeningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long screeningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private CustomerKycVerification verification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "screening_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ScreeningType screeningType;

    @Column(name = "match_found", nullable = false)
    private Boolean matchFound;

    @Column(name = "match_details", columnDefinition = "JSON")
    private String matchDetails;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "screened_at", nullable = false)
    private LocalDateTime screenedAt;

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

    @PrePersist
    protected void onCreate() {
        if (screenedAt == null) {
            screenedAt = LocalDateTime.now();
        }
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
