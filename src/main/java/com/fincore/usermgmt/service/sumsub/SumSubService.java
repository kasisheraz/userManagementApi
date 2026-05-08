package com.fincore.usermgmt.service.sumsub;

import com.fincore.usermgmt.dto.sumsub.SumSubApplicantResponse;
import com.fincore.usermgmt.dto.sumsub.SumSubVerificationStatus;

/**
 * Interface for SumSub integration service
 * Allows switching between mock and real implementations
 */
public interface SumSubService {

    /**
     * Create a new applicant in SumSub
     * @param userId Internal user ID
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email
     * @param phone User's phone number
     * @return SumSub applicant response with applicantId
     */
    SumSubApplicantResponse createApplicant(
            Long userId,
            String firstName,
            String lastName,
            String email,
            String phone
    );

    /**
     * Generate access token for SDK initialization
     * @param applicantId SumSub applicant ID
     * @param levelName Verification level name (e.g., "basic-kyc-level")
     * @return Access token for frontend SDK
     */
    String generateAccessToken(String applicantId, String levelName);

    /**
     * Get verification status for an applicant
     * @param applicantId SumSub applicant ID
     * @return Current verification status
     */
    SumSubVerificationStatus getVerificationStatus(String applicantId);

    /**
     * Verify webhook signature
     * @param payload Webhook payload
     * @param signature Webhook signature from header
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature);

    /**
     * Simulate verification completion (mock only)
     * Used for testing without real SumSub
     * @param applicantId SumSub applicant ID
     * @param approved Whether to approve or reject
     */
    void simulateVerificationComplete(String applicantId, boolean approved);
}
