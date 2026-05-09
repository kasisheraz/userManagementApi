package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.repository.CustomerKycVerificationRepository;
import com.fincore.usermgmt.service.sumsub.SumSubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycWorkflowServiceTest {

    @Mock
    private CustomerKycVerificationRepository kycRepository;

    @Mock
    private KycVerificationService kycVerificationService;

    @Mock
    private CustomerAnswerService customerAnswerService;

    @Mock
    private AmlScreeningService amlScreeningService;

    @Mock
    private SumSubService sumSubService;

    @InjectMocks
    private KycWorkflowService kycWorkflowService;

    private User testUser;
    private CustomerKycVerification testVerification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+447700900000")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        testVerification = CustomerKycVerification.builder()
                .verificationId(100L)
                .user(testUser)
                .verificationLevel(VerificationLevel.ENHANCED)
                .status(VerificationStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void startKycProcess_ShouldCreateVerification() {
        // Given
        when(kycVerificationService.submitVerificationWithSumSub(testUser, VerificationLevel.ENHANCED))
                .thenReturn(testVerification);

        // When
        CustomerKycVerification result = kycWorkflowService.startKycProcess(testUser, VerificationLevel.ENHANCED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getVerificationId()).isEqualTo(100L);
        assertThat(result.getVerificationLevel()).isEqualTo(VerificationLevel.ENHANCED);
        assertThat(result.getStatus()).isEqualTo(VerificationStatus.PENDING);
        verify(kycVerificationService).submitVerificationWithSumSub(testUser, VerificationLevel.ENHANCED);
    }

    @Test
    void completeStep1UserInfo_ShouldValidateAndReturnStatus() {
        // Given
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));

        // When
        Map<String, Object> status = kycWorkflowService.completeStep1UserInfo(100L);

        // Then
        assertThat(status).isNotNull();
        assertThat(status).containsKey("verificationId");
        assertThat(status).containsKey("steps");
        assertThat(status).containsKey("progressPercentage");
        assertThat(status.get("verificationId")).isEqualTo(100L);
    }

    @Test
    void completeStep1UserInfo_WithInvalidUser_ShouldThrowException() {
        // Given
        User invalidUser = User.builder()
                .id(1L)
                .firstName("") // Empty first name
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+447700900000")
                .build();

        CustomerKycVerification verification = CustomerKycVerification.builder()
                .verificationId(100L)
                .user(invalidUser)
                .verificationLevel(VerificationLevel.ENHANCED)
                .status(VerificationStatus.PENDING)
                .build();

        when(kycRepository.findById(100L)).thenReturn(Optional.of(verification));

        // When/Then
        assertThatThrownBy(() -> kycWorkflowService.completeStep1UserInfo(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void completeStep2SumSub_ShouldLinkApplicantId() {
        // Given
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));
        when(kycRepository.save(any(CustomerKycVerification.class))).thenReturn(testVerification);

        // When
        Map<String, Object> status = kycWorkflowService.completeStep2SumSub(100L, "MOCK_ABC123");

        // Then
        assertThat(status).isNotNull();
        assertThat(status).containsKey("verificationId");
        verify(kycRepository).save(any(CustomerKycVerification.class));
    }

    @Test
    void completeStep3Questionnaire_ShouldSubmitAnswers() {
        // Given
        testVerification.setSumsubApplicantId("MOCK_ABC123");
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));

        List<CustomerAnswerRequestDTO> answers = Arrays.asList(
                CustomerAnswerRequestDTO.builder()
                        .questionId(1)
                        .answerText("Answer 1")
                        .build(),
                CustomerAnswerRequestDTO.builder()
                        .questionId(2)
                        .answerText("Answer 2")
                        .build()
        );

        // When
        Map<String, Object> status = kycWorkflowService.completeStep3Questionnaire(100L, answers);

        // Then
        assertThat(status).isNotNull();
        verify(customerAnswerService, times(2)).saveAnswer(any(), any(), any());
    }

    @Test
    void completeStep3Questionnaire_WithoutDocumentVerification_ShouldThrowException() {
        // Given
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));

        List<CustomerAnswerRequestDTO> answers = Arrays.asList(
                CustomerAnswerRequestDTO.builder()
                        .questionId(1)
                        .answerText("Answer 1")
                        .build()
        );

        // When/Then
        assertThatThrownBy(() -> kycWorkflowService.completeStep3Questionnaire(100L, answers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Document verification (Step 2) must be completed first");
    }

    @Test
    void completeStep4Review_ShouldUpdateStatusToPending() {
        // Given
        testVerification.setSumsubApplicantId("MOCK_ABC123");
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));
        when(kycRepository.save(any(CustomerKycVerification.class))).thenReturn(testVerification);
        when(customerAnswerService.getAnswersByUser(1L)).thenReturn(Arrays.asList(
                // Mock answer
        ));

        // When
        Map<String, Object> status = kycWorkflowService.completeStep4Review(100L);

        // Then
        assertThat(status).isNotNull();
        verify(kycRepository).save(argThat(v -> 
                v.getStatus() == VerificationStatus.PENDING && v.getReviewedAt() != null
        ));
    }

    @Test
    void getWorkflowStatus_ShouldReturnDetailedStatus() {
        // Given
        testVerification.setSumsubApplicantId("MOCK_ABC123");
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));
        when(customerAnswerService.getAnswersByUser(1L)).thenReturn(Arrays.asList(
                // Mock answer
        ));

        // When
        Map<String, Object> status = kycWorkflowService.getWorkflowStatus(100L);

        // Then
        assertThat(status).isNotNull();
        assertThat(status).containsKey("verificationId");
        assertThat(status).containsKey("userId");
        assertThat(status).containsKey("level");
        assertThat(status).containsKey("status");
        assertThat(status).containsKey("steps");
        assertThat(status).containsKey("progressPercentage");
        assertThat(status).containsKey("currentStep");

        @SuppressWarnings("unchecked")
        Map<String, Boolean> steps = (Map<String, Boolean>) status.get("steps");
        assertThat(steps).containsKeys(
                KycWorkflowService.STEP_USER_INFO,
                KycWorkflowService.STEP_DOCUMENT_VERIFICATION,
                KycWorkflowService.STEP_QUESTIONNAIRE,
                KycWorkflowService.STEP_REVIEW,
                KycWorkflowService.STEP_COMPLETED
        );

        assertThat(steps.get(KycWorkflowService.STEP_USER_INFO)).isTrue();
        assertThat(steps.get(KycWorkflowService.STEP_DOCUMENT_VERIFICATION)).isTrue();
    }

    @Test
    void getWorkflowStatus_WithVerificationNotFound_ShouldThrowException() {
        // Given
        when(kycRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> kycWorkflowService.getWorkflowStatus(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verification not found");
    }

    @Test
    void getWorkflowProgress_ShouldReturnSimplifiedStatus() {
        // Given
        testVerification.setSumsubApplicantId("MOCK_ABC123");
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));
        when(customerAnswerService.getAnswersByUser(1L)).thenReturn(Arrays.asList());

        // When
        Map<String, Object> progress = kycWorkflowService.getWorkflowProgress(100L);

        // Then
        assertThat(progress).isNotNull();
        assertThat(progress).containsKeys("verificationId", "progressPercentage", "currentStep", "status");
        assertThat(progress.get("verificationId")).isEqualTo(100L);
        assertThat(progress.get("progressPercentage")).isInstanceOf(Integer.class);
    }

    @Test
    void calculateProgressPercentage_ShouldCalculateCorrectly() {
        // Given
        testVerification.setSumsubApplicantId("MOCK_ABC123");
        when(kycRepository.findById(100L)).thenReturn(Optional.of(testVerification));
        when(customerAnswerService.getAnswersByUser(1L)).thenReturn(Arrays.asList(
                // Mock answer
        ));

        // When
        Map<String, Object> status = kycWorkflowService.getWorkflowStatus(100L);

        // Then
        Integer progress = (Integer) status.get("progressPercentage");
        assertThat(progress).isBetween(0, 100);
        
        // With user info + document verification + questionnaire complete = 60%
        assertThat(progress).isEqualTo(60);
    }
}
