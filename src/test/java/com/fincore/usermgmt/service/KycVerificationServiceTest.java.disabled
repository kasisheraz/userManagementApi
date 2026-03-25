package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.repository.CustomerKycVerificationRepository;
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
 * Unit tests for KycVerificationService
 */
@ExtendWith(MockitoExtension.class)
public class KycVerificationServiceTest {

    @Mock
    private CustomerKycVerificationRepository kycRepository;

    @Mock
    private AmlScreeningResultRepository amlRepository;

    @InjectMocks
    private KycVerificationService kycService;

    private User testUser;
    private CustomerKycVerification testVerification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testVerification = CustomerKycVerification.builder()
                .verificationId(1L)
                .user(testUser)
                .verificationLevel(VerificationLevel.STANDARD)
                .status(VerificationStatus.PENDING)
                .riskLevel(RiskLevel.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Test successful verification submission
     */
    @Test
    void testSubmitVerification_Success() {
        when(kycRepository.save(any(CustomerKycVerification.class)))
                .thenReturn(testVerification);

        CustomerKycVerification result = kycService.submitVerification(testUser, VerificationLevel.STANDARD);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(VerificationLevel.STANDARD, result.getVerificationLevel());
        assertEquals(VerificationStatus.PENDING, result.getStatus());
        verify(kycRepository, times(1)).save(any());
    }

    /**
     * Test get verification by ID
     */
    @Test
    void testGetVerificationById_Success() {
        when(kycRepository.findById(1L))
                .thenReturn(Optional.of(testVerification));

        CustomerKycVerification result = kycService.getVerificationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getVerificationId());
        verify(kycRepository, times(1)).findById(1L);
    }

    /**
     * Test get verification by ID not found
     */
    @Test
    void testGetVerificationById_NotFound() {
        when(kycRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> kycService.getVerificationById(999L));
    }

    /**
     * Test get user verification
     */
    @Test
    void testGetUserVerification_Success() {
        when(kycRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testVerification));

        Optional<CustomerKycVerification> result = kycService.getUserVerification(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getVerificationId());
    }

    /**
     * Test get user verification - no verification exists
     */
    @Test
    void testGetUserVerification_Empty() {
        when(kycRepository.findByUserIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Arrays.asList());

        Optional<CustomerKycVerification> result = kycService.getUserVerification(999L);

        assertFalse(result.isPresent());
    }

    /**
     * Test get latest verification
     */
    @Test
    void testGetLatestVerification_Success() {
        when(kycRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(testVerification));

        Optional<CustomerKycVerification> result = kycService.getLatestVerification(1L);

        assertTrue(result.isPresent());
        assertEquals(testVerification.getVerificationId(), result.get().getVerificationId());
    }

    /**
     * Test update verification status to APPROVED
     */
    @Test
    void testUpdateVerificationStatus_ToApproved() {
        User reviewer = User.builder().id(2L).build();
        CustomerKycVerification approvedVerification = testVerification;
        approvedVerification.setStatus(VerificationStatus.APPROVED);
        approvedVerification.setReviewedBy(reviewer);

        when(kycRepository.findById(1L))
                .thenReturn(Optional.of(testVerification));
        when(kycRepository.save(any()))
                .thenReturn(approvedVerification);

        CustomerKycVerification result = kycService.updateVerificationStatus(
                1L,
                VerificationStatus.APPROVED,
                reviewer,
                "Approved after review"
        );

        assertEquals(VerificationStatus.APPROVED, result.getStatus());
        verify(kycRepository, times(1)).save(any());
    }

    /**
     * Test find expired verifications
     */
    @Test
    void testFindExpiredVerifications() {
        List<CustomerKycVerification> expiredList = Arrays.asList(
                testVerification,
                CustomerKycVerification.builder()
                        .verificationId(2L)
                        .status(VerificationStatus.PENDING)
                        .build()
        );

        when(kycRepository.findByStatusNotIn(anyList()))
                .thenReturn(expiredList);

        List<CustomerKycVerification> result = kycService.findExpiredVerifications();

        assertNotNull(result);
        verify(kycRepository, times(1)).findByStatusNotIn(anyList());
    }

    /**
     * Test count by status
     */
    @Test
    void testCountByStatus() {
        when(kycRepository.countByStatus(VerificationStatus.APPROVED))
                .thenReturn(5L);

        long count = kycService.countByStatus(VerificationStatus.APPROVED);

        assertEquals(5L, count);
        verify(kycRepository, times(1)).countByStatus(VerificationStatus.APPROVED);
    }

    /**
     * Test get AML screenings for verification
     */
    @Test
    void testGetAmlScreenings() {
        List<AmlScreeningResult> screenings = Arrays.asList(
                AmlScreeningResult.builder()
                        .screeningId(1L)
                        .matchFound(true)
                        .riskScore(85)
                        .build()
        );

        when(amlRepository.findByVerificationIdOrderByScreenedAtDesc(1L))
                .thenReturn(screenings);

        List<AmlScreeningResult> result = kycService.getAmlScreenings(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getMatchFound());
    }

    /**
     * Test set Sumsub applicant ID
     */
    @Test
    void testSetSumsubApplicantId() {
        when(kycRepository.findById(1L))
                .thenReturn(Optional.of(testVerification));
        testVerification.setSumsubApplicantId("sumsub-123");
        when(kycRepository.save(any()))
                .thenReturn(testVerification);

        CustomerKycVerification result = kycService.setSumsubApplicantId(1L, "sumsub-123");

        assertEquals("sumsub-123", result.getSumsubApplicantId());
        verify(kycRepository, times(1)).save(any());
    }

    /**
     * Test get verification by Sumsub ID
     */
    @Test
    void testGetVerificationBySumsubId() {
        testVerification.setSumsubApplicantId("sumsub-123");
        when(kycRepository.findBySumsubApplicantId("sumsub-123"))
                .thenReturn(Optional.of(testVerification));

        Optional<CustomerKycVerification> result = kycService.getVerificationBySumsubId("sumsub-123");

        assertTrue(result.isPresent());
        assertEquals("sumsub-123", result.get().getSumsubApplicantId());
    }

    /**
     * Test has approved verification - with active approval
     */
    @Test
    void testHasApprovedVerification_True() {
        testVerification.setStatus(VerificationStatus.APPROVED);
        testVerification.setApprovedAt(LocalDateTime.now());

        when(kycRepository.findFirstByUserIdAndStatusOrderByApprovedAtDesc(1L, VerificationStatus.APPROVED))
                .thenReturn(Optional.of(testVerification));

        boolean result = kycService.hasApprovedVerification(1L);

        assertTrue(result);
    }

    /**
     * Test has approved verification - no approval
     */
    @Test
    void testHasApprovedVerification_False() {
        when(kycRepository.findFirstByUserIdAndStatusOrderByApprovedAtDesc(anyLong(), any()))
                .thenReturn(Optional.empty());

        boolean result = kycService.hasApprovedVerification(1L);

        assertFalse(result);
    }

    /**
     * Test delete verification
     */
    @Test
    void testDeleteVerification() {
        doNothing().when(kycRepository).deleteById(1L);

        kycService.deleteVerification(1L);

        verify(kycRepository, times(1)).deleteById(1L);
    }

    /**
     * Test get verifications by status
     */
    @Test
    void testGetVerificationsByStatus() {
        List<CustomerKycVerification> verifications = Arrays.asList(testVerification);

        when(kycRepository.findByStatus(VerificationStatus.PENDING))
                .thenReturn(verifications);

        List<CustomerKycVerification> result = kycService.getVerificationsByStatus(VerificationStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(kycRepository, times(1)).findByStatus(VerificationStatus.PENDING);
    }

    /**
     * Test multiple verification levels
     */
    @Test
    void testSubmitVerification_EnhancedLevel() {
        CustomerKycVerification enhancedVerification = CustomerKycVerification.builder()
                .verificationId(2L)
                .user(testUser)
                .verificationLevel(VerificationLevel.ENHANCED)
                .status(VerificationStatus.PENDING)
                .build();

        when(kycRepository.save(any()))
                .thenReturn(enhancedVerification);

        CustomerKycVerification result = kycService.submitVerification(testUser, VerificationLevel.ENHANCED);

        assertEquals(VerificationLevel.ENHANCED, result.getVerificationLevel());
    }
}
