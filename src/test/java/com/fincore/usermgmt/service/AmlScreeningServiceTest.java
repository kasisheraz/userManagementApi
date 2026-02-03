package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import com.fincore.usermgmt.entity.enums.ScreeningType;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.repository.AmlScreeningResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AmlScreeningService
 */
@ExtendWith(MockitoExtension.class)
public class AmlScreeningServiceTest {

    @Mock
    private AmlScreeningResultRepository amlRepository;

    @Mock
    private KycVerificationService kycService;

    @InjectMocks
    private AmlScreeningService amlService;

    private User testUser;
    private CustomerKycVerification testVerification;
    private AmlScreeningResult testScreening;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testVerification = CustomerKycVerification.builder()
                .verificationId(1L)
                .user(testUser)
                .build();

        testScreening = AmlScreeningResult.builder()
                .screeningId(1L)
                .verification(testVerification)
                .user(testUser)
                .screeningType(ScreeningType.SANCTIONS)
                .matchFound(false)
                .riskScore(25)
                .build();
    }

    /**
     * Test create screening with valid data
     */
    @Test
    void testCreateScreening_Success() {
        when(amlRepository.save(any(AmlScreeningResult.class)))
                .thenReturn(testScreening);

        AmlScreeningResult result = amlService.createScreening(
                testVerification,
                testUser,
                ScreeningType.SANCTIONS,
                false,
                25,
                "No matches found"
        );

        assertNotNull(result);
        assertEquals(ScreeningType.SANCTIONS, result.getScreeningType());
        assertEquals(25, result.getRiskScore());
        verify(amlRepository, times(1)).save(any());
    }

    /**
     * Test create screening with invalid risk score - too high
     */
    @Test
    void testCreateScreening_InvalidRiskScore() {
        assertThrows(IllegalArgumentException.class, () -> amlService.createScreening(
                testVerification,
                testUser,
                ScreeningType.SANCTIONS,
                false,
                150, // Invalid: > 100
                "Invalid score"
        ));
    }

    /**
     * Test get screening by ID
     */
    @Test
    void testGetScreeningById_Success() {
        when(amlRepository.findById(1L))
                .thenReturn(Optional.of(testScreening));

        AmlScreeningResult result = amlService.getScreeningById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getScreeningId());
        verify(amlRepository, times(1)).findById(1L);
    }

    /**
     * Test get screenings by verification
     */
    @Test
    void testGetScreeningsByVerification() {
        List<AmlScreeningResult> screenings = Arrays.asList(testScreening);

        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(screenings);

        List<AmlScreeningResult> result = amlService.getScreeningsByVerification(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(amlRepository, times(1)).findByVerificationIdOrderByScreenedAtDesc(1L);
    }

    /**
     * Test get screenings by user
     */
    @Test
    void testGetScreeningsByUser() {
        List<AmlScreeningResult> screenings = Arrays.asList(testScreening);

        when(amlRepository.findByUserIdOrderByScreenedAtDesc(1L))
                .thenReturn(screenings);

        List<AmlScreeningResult> result = amlService.getScreeningsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test get high risk screenings - with matches
     */
    @Test
    void testGetHighRiskScreenings_WithMatches() {
        AmlScreeningResult highRiskScreening = AmlScreeningResult.builder()
                .screeningId(2L)
                .matchFound(true)
                .riskScore(85)
                .build();

        List<AmlScreeningResult> highRiskList = Arrays.asList(highRiskScreening);

        when(amlRepository.findByMatchFoundTrueOrderByScreenedAtDesc())
                .thenReturn(highRiskList);

        List<AmlScreeningResult> result = amlService.getHighRiskScreenings();

        assertNotNull(result);
        assertTrue(result.stream().anyMatch(s -> Boolean.TRUE.equals(s.getMatchFound())));
    }

    /**
     * Test get screenings by type
     */
    @Test
    void testGetScreeningsByType() {
        List<AmlScreeningResult> screenings = Arrays.asList(testScreening);

        when(amlRepository.findByScreeningTypeOrderByScreenedAtDesc(ScreeningType.SANCTIONS))
                .thenReturn(screenings);

        List<AmlScreeningResult> result = amlService.getScreeningsByType(ScreeningType.SANCTIONS);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ScreeningType.SANCTIONS, result.get(0).getScreeningType());
    }

    /**
     * Test assess overall risk - HIGH (due to match found)
     */
    @Test
    void testAssessOverallRisk_High_DueToMatch() {
        AmlScreeningResult matchFound = AmlScreeningResult.builder()
                .screeningId(1L)
                .matchFound(true)
                .riskScore(50)
                .build();

        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(Arrays.asList(matchFound));

        RiskLevel risk = amlService.assessOverallRisk(1L);

        assertEquals(RiskLevel.HIGH, risk);
    }

    /**
     * Test assess overall risk - HIGH (due to score > 70)
     */
    @Test
    void testAssessOverallRisk_High_DueToScore() {
        AmlScreeningResult highScore = AmlScreeningResult.builder()
                .screeningId(1L)
                .matchFound(false)
                .riskScore(85)
                .build();

        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(Arrays.asList(highScore));

        RiskLevel risk = amlService.assessOverallRisk(1L);

        assertEquals(RiskLevel.HIGH, risk);
    }

    /**
     * Test assess overall risk - MEDIUM
     */
    @Test
    void testAssessOverallRisk_Medium() {
        AmlScreeningResult mediumScore = AmlScreeningResult.builder()
                .screeningId(1L)
                .matchFound(false)
                .riskScore(55)
                .build();

        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(Arrays.asList(mediumScore));

        RiskLevel risk = amlService.assessOverallRisk(1L);

        assertEquals(RiskLevel.MEDIUM, risk);
    }

    /**
     * Test assess overall risk - LOW
     */
    @Test
    void testAssessOverallRisk_Low() {
        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(Arrays.asList(testScreening)); // riskScore = 25

        RiskLevel risk = amlService.assessOverallRisk(1L);

        assertEquals(RiskLevel.LOW, risk);
    }

    /**
     * Test update screening
     */
    @Test
    void testUpdateScreening() {
        AmlScreeningResult updated = testScreening;
        updated.setMatchFound(true);
        updated.setRiskScore(90);

        when(amlRepository.findById(1L))
                .thenReturn(Optional.of(testScreening));
        when(amlRepository.save(any()))
                .thenReturn(updated);

        AmlScreeningResult result = amlService.updateScreening(1L, true, 90, "Match found");

        assertTrue(result.getMatchFound());
        assertEquals(90, result.getRiskScore());
        verify(amlRepository, times(1)).save(any());
    }

    /**
     * Test trigger sanctions screening
     */
    @Test
    void testTriggerSanctionsScreening() {
        AmlScreeningResult sanctionsScreening = AmlScreeningResult.builder()
                .screeningId(1L)
                .screeningType(ScreeningType.SANCTIONS)
                .build();

        when(amlRepository.save(any()))
                .thenReturn(sanctionsScreening);

        AmlScreeningResult result = amlService.triggerSanctionsScreening(testVerification, testUser, false, 30);

        assertEquals(ScreeningType.SANCTIONS, result.getScreeningType());
        verify(amlRepository, times(1)).save(any());
    }

    /**
     * Test trigger PEP screening
     */
    @Test
    void testTriggerPepScreening() {
        AmlScreeningResult pepScreening = AmlScreeningResult.builder()
                .screeningId(1L)
                .screeningType(ScreeningType.PEP)
                .build();

        when(amlRepository.save(any()))
                .thenReturn(pepScreening);

        AmlScreeningResult result = amlService.triggerPepScreening(testVerification, testUser, false, 20);

        assertEquals(ScreeningType.PEP, result.getScreeningType());
    }

    /**
     * Test trigger adverse media screening
     */
    @Test
    void testTriggerAdverseMediaScreening() {
        AmlScreeningResult adverseMediaScreening = AmlScreeningResult.builder()
                .screeningId(1L)
                .screeningType(ScreeningType.ADVERSE_MEDIA)
                .build();

        when(amlRepository.save(any()))
                .thenReturn(adverseMediaScreening);

        AmlScreeningResult result = amlService.triggerAdverseMediaScreening(testVerification, testUser, false, 15);

        assertEquals(ScreeningType.ADVERSE_MEDIA, result.getScreeningType());
    }

    /**
     * Test count matches by type
     */
    @Test
    void testCountMatchesByType() {
        when(amlRepository.countByScreeningTypeAndMatchFoundTrue(ScreeningType.SANCTIONS))
                .thenReturn(5L);

        long count = amlService.countMatchesByType(ScreeningType.SANCTIONS);

        assertEquals(5L, count);
        verify(amlRepository, times(1)).countByScreeningTypeAndMatchFoundTrue(ScreeningType.SANCTIONS);
    }

    /**
     * Test delete screening
     */
    @Test
    void testDeleteScreening() {
        doNothing().when(amlRepository).deleteById(1L);

        amlService.deleteScreening(1L);

        verify(amlRepository, times(1)).deleteById(1L);
    }

    /**
     * Test get latest screening
     */
    @Test
    void testGetLatestScreening() {
        when(amlRepository.findFirstByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(Optional.of(testScreening));

        Optional<AmlScreeningResult> result = amlService.getLatestScreening(1L);

        assertTrue(result.isPresent());
        assertEquals(testScreening.getScreeningId(), result.get().getScreeningId());
    }
}
