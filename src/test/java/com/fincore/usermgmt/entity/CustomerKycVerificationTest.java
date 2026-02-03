package com.fincore.usermgmt.entity;

import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerKycVerification entity
 */
class CustomerKycVerificationTest {

    private CustomerKycVerification verification;
    private User testUser;
    private User reviewer;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        reviewer = User.builder()
                .id(2L)
                .build();

        verification = CustomerKycVerification.builder()
                .user(testUser)
                .verificationLevel(VerificationLevel.FULL)
                .status(VerificationStatus.PENDING)
                .riskLevel(RiskLevel.LOW)
                .sumsubApplicantId("sumsub-123")
                .lastModifiedBy(reviewer)
                .build();
    }

    @Test
    void testVerificationCreation() {
        assertNotNull(verification);
        assertEquals(testUser.getId(), verification.getUser().getId());
        assertEquals(VerificationLevel.FULL, verification.getVerificationLevel());
        assertEquals(VerificationStatus.PENDING, verification.getStatus());
        assertEquals(RiskLevel.LOW, verification.getRiskLevel());
    }

    @Test
    void testVerificationStatus() {
        verification.setStatus(VerificationStatus.APPROVED);
        assertEquals(VerificationStatus.APPROVED, verification.getStatus());

        verification.setStatus(VerificationStatus.REJECTED);
        assertEquals(VerificationStatus.REJECTED, verification.getStatus());
    }

    @Test
    void testVerificationLevel() {
        verification.setVerificationLevel(VerificationLevel.BASIC);
        assertEquals(VerificationLevel.BASIC, verification.getVerificationLevel());

        verification.setVerificationLevel(VerificationLevel.AML);
        assertEquals(VerificationLevel.AML, verification.getVerificationLevel());
    }

    @Test
    void testRiskLevel() {
        verification.setRiskLevel(RiskLevel.MEDIUM);
        assertEquals(RiskLevel.MEDIUM, verification.getRiskLevel());

        verification.setRiskLevel(RiskLevel.HIGH);
        assertEquals(RiskLevel.HIGH, verification.getRiskLevel());
    }

    @Test
    void testSumsubApplicantId() {
        verification.setSumsubApplicantId("new-sumsub-456");
        assertEquals("new-sumsub-456", verification.getSumsubApplicantId());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        verification.setSubmittedAt(now);
        verification.setApprovedAt(now.plusMinutes(10));
        verification.setExpiresAt(now.plusDays(365));

        assertEquals(now, verification.getSubmittedAt());
        assertEquals(now.plusMinutes(10), verification.getApprovedAt());
        assertEquals(now.plusDays(365), verification.getExpiresAt());
    }

    @Test
    void testReviewerRelationship() {
        assertNotNull(verification.getLastModifiedBy());
        assertEquals(reviewer.getId(), verification.getLastModifiedBy().getId());
    }

    @Test
    void testBuilderPattern() {
        CustomerKycVerification builtVerification = CustomerKycVerification.builder()
                .user(testUser)
                .verificationLevel(VerificationLevel.BASIC)
                .status(VerificationStatus.PENDING)
                .riskLevel(RiskLevel.MEDIUM)
                .sumsubApplicantId("sumsub-789")
                .build();

        assertEquals(testUser.getId(), builtVerification.getUser().getId());
        assertEquals(VerificationLevel.BASIC, builtVerification.getVerificationLevel());
        assertEquals(VerificationStatus.PENDING, builtVerification.getStatus());
    }

    @Test
    void testReviewResult() {
        String reviewResult = "{\"status\": \"approved\", \"score\": 95}";
        verification.setReviewResult(reviewResult);
        assertEquals(reviewResult, verification.getReviewResult());
    }

    @Test
    void testUserRelationship() {
        assertNotNull(verification.getUser());
        assertEquals(testUser.getId(), verification.getUser().getId());
    }

    @Test
    void testNullableTimestamps() {
        CustomerKycVerification minimalVerification = CustomerKycVerification.builder()
                .user(testUser)
                .status(VerificationStatus.PENDING)
                .build();

        assertNull(minimalVerification.getSubmittedAt());
        assertNull(minimalVerification.getApprovedAt());
        assertNull(minimalVerification.getRejectedAt());
        assertNull(minimalVerification.getExpiresAt());
    }

    @Test
    void testAllStatusTransitions() {
        for (VerificationStatus status : VerificationStatus.values()) {
            verification.setStatus(status);
            assertEquals(status, verification.getStatus());
        }
    }

    @Test
    void testAmlScreeningsCollection() {
        assertNull(verification.getAmlScreenings());

        verification.setAmlScreenings(new java.util.HashSet<>());
        assertNotNull(verification.getAmlScreenings());
        assertTrue(verification.getAmlScreenings().isEmpty());
    }
}
