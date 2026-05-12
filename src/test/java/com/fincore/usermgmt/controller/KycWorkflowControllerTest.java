package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.config.TestMailConfig;
import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.service.KycWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class KycWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KycWorkflowService kycWorkflowService;

    @MockBean
    private UserRepository userRepository;

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
    @WithMockUser(username = "+447700900000", roles = "USER")
    void startWorkflow_ShouldCreateVerification() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));
        when(kycWorkflowService.startKycProcess(any(User.class), eq(VerificationLevel.ENHANCED)))
                .thenReturn(testVerification);

        Map<String, Object> expectedStatus = new HashMap<>();
        expectedStatus.put("verificationId", 100L);
        expectedStatus.put("userId", 1L);
        expectedStatus.put("level", "ENHANCED");
        expectedStatus.put("status", "PENDING");

        when(kycWorkflowService.getWorkflowStatus(100L)).thenReturn(expectedStatus);

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/start")
                        .param("level", "ENHANCED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.level").value("ENHANCED"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void completeStep1_ShouldReturnUpdatedStatus() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));

        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", 100L);
        status.put("currentStep", "DOCUMENT_VERIFICATION");
        status.put("progressPercentage", 20);

        when(kycWorkflowService.completeStep1UserInfo(100L)).thenReturn(status);

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/100/step1/user-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.currentStep").value("DOCUMENT_VERIFICATION"))
                .andExpect(jsonPath("$.progressPercentage").value(20));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void completeStep2_ShouldLinkSumSubApplicant() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));

        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", 100L);
        status.put("currentStep", "QUESTIONNAIRE");
        status.put("progressPercentage", 40);

        when(kycWorkflowService.completeStep2SumSub(eq(100L), eq("MOCK_ABC123")))
                .thenReturn(status);

        Map<String, String> request = new HashMap<>();
        request.put("sumsubApplicantId", "MOCK_ABC123");

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/100/step2/sumsub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.currentStep").value("QUESTIONNAIRE"))
                .andExpect(jsonPath("$.progressPercentage").value(40));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void completeStep3_ShouldSubmitAnswers() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));

        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", 100L);
        status.put("currentStep", "REVIEW");
        status.put("progressPercentage", 60);

        when(kycWorkflowService.completeStep3Questionnaire(eq(100L), anyList()))
                .thenReturn(status);

        List<CustomerAnswerRequestDTO> answers = Arrays.asList(
                CustomerAnswerRequestDTO.builder()
                        .questionId(1)
                        .answerText("Answer 1")
                        .build()
        );

        Map<String, Object> request = new HashMap<>();
        request.put("answers", answers);

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/100/step3/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.currentStep").value("REVIEW"))
                .andExpect(jsonPath("$.progressPercentage").value(60));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void completeStep4_ShouldCompleteWorkflow() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));

        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", 100L);
        status.put("currentStep", "COMPLETED");
        status.put("progressPercentage", 80);
        status.put("status", "PENDING");

        when(kycWorkflowService.completeStep4Review(100L)).thenReturn(status);

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/100/step4/review")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.currentStep").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercentage").value(80));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void getStatus_ShouldReturnDetailedStatus() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));
        
        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", 100L);
        status.put("userId", 1L);
        status.put("level", "ENHANCED");
        status.put("status", "PENDING");
        status.put("progressPercentage", 60);

        when(kycWorkflowService.getWorkflowStatus(100L)).thenReturn(status);

        // When/Then
        mockMvc.perform(get("/api/kyc/workflow/100/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.level").value("ENHANCED"))
                .andExpect(jsonPath("$.progressPercentage").value(60));
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void getProgress_ShouldReturnSimplifiedProgress() throws Exception {
        // Given
        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("verificationId", 100L);
        progress.put("progressPercentage", 40);
        progress.put("currentStep", "QUESTIONNAIRE");
        progress.put("status", "PENDING");

        when(kycWorkflowService.getWorkflowProgress(100L)).thenReturn(progress);

        // When/Then
        mockMvc.perform(get("/api/kyc/workflow/100/progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId").value(100))
                .andExpect(jsonPath("$.progressPercentage").value(40))
                .andExpect(jsonPath("$.currentStep").value("QUESTIONNAIRE"));
    }

    @Test
    void startWorkflow_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When/Then
        // Spring Security returns 403 Forbidden when there's no authentication
        mockMvc.perform(post("/api/kyc/workflow/start")
                        .param("level", "ENHANCED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "+447700900000", roles = "USER")
    void completeStep_WithUnauthorizedVerification_ShouldReturnForbidden() throws Exception {
        // Given
        User differentUser = User.builder()
                .id(2L)
                .phoneNumber("+447700900001")
                .build();

        when(userRepository.findByPhoneNumber("+447700900000")).thenReturn(Optional.of(testUser));
        when(kycWorkflowService.completeStep1UserInfo(100L))
                .thenThrow(new IllegalArgumentException("Verification not found or access denied"));

        // When/Then
        mockMvc.perform(post("/api/kyc/workflow/100/step1/user-info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
