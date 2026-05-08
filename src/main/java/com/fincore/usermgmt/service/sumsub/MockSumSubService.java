package com.fincore.usermgmt.service.sumsub;

import com.fincore.usermgmt.dto.sumsub.SumSubApplicantResponse;
import com.fincore.usermgmt.dto.sumsub.SumSubVerificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of SumSub service for development/testing
 * Simulates SumSub behavior without external dependencies
 * Active in: npe, local profiles
 */
@Service
@Profile({"npe", "local", "test"})
@Slf4j
public class MockSumSubService implements SumSubService {

    // In-memory storage for mock applicants
    private final Map<String, MockApplicant> applicants = new ConcurrentHashMap<>();

    @Override
    public SumSubApplicantResponse createApplicant(
            Long userId,
            String firstName,
            String lastName,
            String email,
            String phone) {
        
        log.info("🎭 MOCK: Creating SumSub applicant for user {}", userId);

        // Generate mock applicant ID
        String applicantId = "MOCK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Store in memory
        MockApplicant mockApplicant = new MockApplicant();
        mockApplicant.applicantId = applicantId;
        mockApplicant.userId = userId;
        mockApplicant.firstName = firstName;
        mockApplicant.lastName = lastName;
        mockApplicant.email = email;
        mockApplicant.phone = phone;
        mockApplicant.reviewStatus = "init";
        mockApplicant.createdAt = LocalDateTime.now();
        
        applicants.put(applicantId, mockApplicant);

        log.info("✅ MOCK: Created applicant {} for user {}", applicantId, userId);

        return SumSubApplicantResponse.builder()
                .applicantId(applicantId)
                .externalUserId(userId.toString())
                .type("individual")
                .reviewStatus("init")
                .createdAt(LocalDateTime.now())
                .mock(true)
                .build();
    }

    @Override
    public String generateAccessToken(String applicantId, String levelName) {
        log.info("🎭 MOCK: Generating access token for applicant {}", applicantId);

        // Generate mock access token
        String token = "MOCK_TOKEN_" + applicantId + "_" + System.currentTimeMillis();
        
        log.info("✅ MOCK: Generated access token for applicant {}", applicantId);
        
        return token;
    }

    @Override
    public SumSubVerificationStatus getVerificationStatus(String applicantId) {
        log.info("🎭 MOCK: Getting verification status for applicant {}", applicantId);

        MockApplicant applicant = applicants.get(applicantId);
        
        if (applicant == null) {
            log.warn("⚠️ MOCK: Applicant {} not found", applicantId);
            return SumSubVerificationStatus.builder()
                    .applicantId(applicantId)
                    .reviewStatus("init")
                    .reviewResult(null)
                    .updatedAt(LocalDateTime.now())
                    .mock(true)
                    .build();
        }

        return SumSubVerificationStatus.builder()
                .applicantId(applicantId)
                .reviewStatus(applicant.reviewStatus)
                .reviewResult(applicant.reviewResult)
                .rejectLabels(applicant.rejectLabels)
                .updatedAt(applicant.updatedAt)
                .mock(true)
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        log.info("🎭 MOCK: Verifying webhook signature");
        // Mock always accepts webhooks (for testing)
        return true;
    }

    @Override
    public void simulateVerificationComplete(String applicantId, boolean approved) {
        log.info("🎭 MOCK: Simulating verification complete for {} - approved: {}", 
                applicantId, approved);

        MockApplicant applicant = applicants.get(applicantId);
        
        if (applicant == null) {
            log.warn("⚠️ MOCK: Cannot simulate - applicant {} not found", applicantId);
            return;
        }

        applicant.reviewStatus = "completed";
        applicant.reviewResult = approved ? "GREEN" : "RED";
        applicant.rejectLabels = approved ? null : new String[]{"DOCUMENT_MISMATCH"};
        applicant.updatedAt = LocalDateTime.now();

        log.info("✅ MOCK: Verification completed for {} - result: {}", 
                applicantId, applicant.reviewResult);
    }

    /**
     * Internal mock applicant data structure
     */
    private static class MockApplicant {
        String applicantId;
        Long userId;
        String firstName;
        String lastName;
        String email;
        String phone;
        String reviewStatus;
        String reviewResult;
        String[] rejectLabels;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }
}
