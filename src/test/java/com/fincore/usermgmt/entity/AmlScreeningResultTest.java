package com.fincore.usermgmt.entity;

import com.fincore.usermgmt.entity.enums.ScreeningType;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AmlScreeningResult entity
 */
class AmlScreeningResultTest {

    private AmlScreeningResult screeningResult;
    private User testUser;
    private CustomerKycVerification verification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        verification = CustomerKycVerification.builder()
                .verificationId(1L)
                .user(testUser)
                .build();

        screeningResult = AmlScreeningResult.builder()
                .user(testUser)
                .verification(verification)
                .screeningType(ScreeningType.SANCTIONS)
                .matchFound(false)
                .riskScore(0)
                .build();
    }

    @Test
    void testScreeningResultCreation() {
        assertNotNull(screeningResult);
        assertEquals(testUser.getId(), screeningResult.getUser().getId());
        assertEquals(verification.getVerificationId(), screeningResult.getVerification().getVerificationId());
        assertEquals(ScreeningType.SANCTIONS, screeningResult.getScreeningType());
        assertFalse(screeningResult.getMatchFound());
    }

    @Test
    void testScreeningType() {
        screeningResult.setScreeningType(ScreeningType.PEP);
        assertEquals(ScreeningType.PEP, screeningResult.getScreeningType());

        screeningResult.setScreeningType(ScreeningType.ADVERSE_MEDIA);
        assertEquals(ScreeningType.ADVERSE_MEDIA, screeningResult.getScreeningType());
    }

    @Test
    void testMatchFound() {
        screeningResult.setMatchFound(true);
        assertTrue(screeningResult.getMatchFound());

        screeningResult.setMatchFound(false);
        assertFalse(screeningResult.getMatchFound());
    }

    @Test
    void testRiskScore() {
        screeningResult.setRiskScore(75);
        assertEquals(75, screeningResult.getRiskScore());

        screeningResult.setRiskScore(0);
        assertEquals(0, screeningResult.getRiskScore());

        screeningResult.setRiskScore(100);
        assertEquals(100, screeningResult.getRiskScore());
    }

    @Test
    void testMatchDetails() {
        String matchDetails = "{\"matches\": [{\"name\": \"John Doe\", \"confidence\": 0.95}]}";
        screeningResult.setMatchDetails(matchDetails);
        assertEquals(matchDetails, screeningResult.getMatchDetails());
    }

    @Test
    void testScreenedAt() {
        LocalDateTime now = LocalDateTime.now();
        screeningResult.setScreenedAt(now);
        assertEquals(now, screeningResult.getScreenedAt());
    }

    @Test
    void testUserRelationship() {
        assertNotNull(screeningResult.getUser());
        assertEquals(testUser.getId(), screeningResult.getUser().getId());
    }

    @Test
    void testVerificationRelationship() {
        assertNotNull(screeningResult.getVerification());
        assertEquals(verification.getVerificationId(), screeningResult.getVerification().getVerificationId());
    }

    @Test
    void testBuilderPattern() {
        AmlScreeningResult builtResult = AmlScreeningResult.builder()
                .user(testUser)
                .verification(verification)
                .screeningType(ScreeningType.PEP)
                .matchFound(true)
                .riskScore(85)
                .build();

        assertEquals(ScreeningType.PEP, builtResult.getScreeningType());
        assertTrue(builtResult.getMatchFound());
        assertEquals(85, builtResult.getRiskScore());
    }

    @Test
    void testAllScreeningTypes() {
        for (ScreeningType type : ScreeningType.values()) {
            screeningResult.setScreeningType(type);
            assertEquals(type, screeningResult.getScreeningType());
        }
    }

    @Test
    void testHighRiskScoring() {
        screeningResult.setMatchFound(true);
        screeningResult.setRiskScore(95);

        assertTrue(screeningResult.getMatchFound());
        assertTrue(screeningResult.getRiskScore() > 80);
    }

    @Test
    void testLowRiskScoring() {
        screeningResult.setMatchFound(false);
        screeningResult.setRiskScore(0);

        assertFalse(screeningResult.getMatchFound());
        assertEquals(0, screeningResult.getRiskScore());
    }

    @Test
    void testAuditFields() {
        User auditor = User.builder().id(2L).build();
        screeningResult.setCreatedBy(auditor);
        screeningResult.setLastModifiedBy(auditor);

        assertNotNull(screeningResult.getCreatedBy());
        assertNotNull(screeningResult.getLastModifiedBy());
    }
}
