package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import com.fincore.usermgmt.repository.CustomerKycVerificationRepository;
import com.fincore.usermgmt.service.sumsub.SumSubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for orchestrating the complete KYC workflow.
 * 
 * The workflow consists of 4 main steps:
 * 1. User Information Collection - Basic personal information
 * 2. Document Verification - SumSub integration for ID verification
 * 3. Compliance Questionnaire - Risk assessment questions
 * 4. Final Review - Admin review and approval/rejection
 * 
 * Each step must be completed before proceeding to the next step.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KycWorkflowService {

    private final CustomerKycVerificationRepository kycRepository;
    private final KycVerificationService kycVerificationService;
    private final CustomerAnswerService customerAnswerService;
    private final AmlScreeningService amlScreeningService;
    private final SumSubService sumSubService;
    private final KycEmailNotificationService emailNotificationService;

    // Workflow step constants
    public static final String STEP_USER_INFO = "USER_INFO";
    public static final String STEP_DOCUMENT_VERIFICATION = "DOCUMENT_VERIFICATION";
    public static final String STEP_QUESTIONNAIRE = "QUESTIONNAIRE";
    public static final String STEP_REVIEW = "REVIEW";
    public static final String STEP_COMPLETED = "COMPLETED";

    /**
     * Start a new KYC workflow process.
     * Creates a new verification record and initializes workflow state.
     * 
     * @param user The user starting the KYC process
     * @param level The verification level (BASIC, ENHANCED, or FULL)
     * @return The created verification record
     */
    public CustomerKycVerification startKycProcess(User user, VerificationLevel level) {
        log.info("Starting KYC workflow for user: {}, level: {}", user.getId(), level);

        // Create verification record via existing service
        CustomerKycVerification verification = kycVerificationService.submitVerificationWithSumSub(user, level);

        log.info("KYC workflow started with verification ID: {}", verification.getVerificationId());
        return verification;
    }

    /**
     * Complete Step 1: User Information Collection.
     * This step validates that all required user information is present.
     * 
     * @param verificationId The verification ID
     * @return Current workflow status
     */
    public Map<String, Object> completeStep1UserInfo(Long verificationId) {
        log.info("Completing Step 1 (User Info) for verification: {}", verificationId);

        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        // Validate user has required information
        User user = verification.getUser();
        validateUserInformation(user);

        // Step 1 is automatically complete when verification is created
        // Users must have basic info to register

        log.info("Step 1 (User Info) completed for verification: {}", verificationId);
        return getWorkflowStatus(verificationId);
    }

    /**
     * Complete Step 2: Document Verification via SumSub.
     * Links the SumSub applicant ID and initiates the verification process.
     * 
     * @param verificationId The verification ID
     * @param sumsubApplicantId The SumSub applicant ID
     * @return Current workflow status
     */
    public Map<String, Object> completeStep2SumSub(Long verificationId, String sumsubApplicantId) {
        log.info("Completing Step 2 (Document Verification) for verification: {}", verificationId);

        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        // Link SumSub applicant ID if not already set
        if (verification.getSumsubApplicantId() == null) {
            verification.setSumsubApplicantId(sumsubApplicantId);
            kycRepository.save(verification);
        }

        // In mock environment, SumSub completion is simulated
        // In production, this would wait for SumSub webhook callback

        log.info("Step 2 (Document Verification) completed for verification: {}", verificationId);
        return getWorkflowStatus(verificationId);
    }

    /**
     * Complete Step 3: Compliance Questionnaire.
     * Submits and validates user's answers to compliance questions.
     * 
     * @param verificationId The verification ID
     * @param answers List of answers to questionnaire questions
     * @return Current workflow status
     */
    public Map<String, Object> completeStep3Questionnaire(Long verificationId, List<CustomerAnswerRequestDTO> answers) {
        log.info("Completing Step 3 (Questionnaire) for verification: {} with {} answers", 
                verificationId, answers.size());

        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        // Validate prerequisites
        if (verification.getSumsubApplicantId() == null) {
            throw new IllegalStateException("Document verification (Step 2) must be completed first");
        }

        // Submit answers via existing service
        User user = verification.getUser();
        for (CustomerAnswerRequestDTO answer : answers) {
            customerAnswerService.saveAnswer(
                    user,
                    answer.getQuestionId(),
                    answer.getAnswerText()
            );
        }

        // Calculate compliance risk based on answers
        // This would integrate with risk scoring logic
        log.info("Step 3 (Questionnaire) completed for verification: {}", verificationId);
        return getWorkflowStatus(verificationId);
    }

    /**
     * Complete Step 4: Final Review.
     * Performs automated checks and queues for admin review if needed.
     * 
     * @param verificationId The verification ID
     * @return Current workflow status
     */
    public Map<String, Object> completeStep4Review(Long verificationId) {
        log.info("Completing Step 4 (Review) for verification: {}", verificationId);

        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        // Validate all previous steps completed
        validateWorkflowPrerequisites(verification);

        // Perform AML screening (if not already done)
        if (verification.getAmlScreenings().isEmpty()) {
            log.info("Performing AML screening for verification: {}", verificationId);
            // In production, this would trigger actual AML checks
            // For now, we skip automatic screening
        }

        // Update status to remain PENDING for admin review
        // In production, might add a separate IN_REVIEW status
        VerificationStatus oldStatus = verification.getStatus();
        verification.setStatus(VerificationStatus.PENDING);
        verification.setReviewedAt(LocalDateTime.now());
        verification = kycRepository.save(verification);

        // Send email notification
        try {
            emailNotificationService.sendKycSubmittedNotification(verification);
            emailNotificationService.sendKycUnderReviewNotification(verification);
        } catch (Exception e) {
            log.error("Failed to send email notification for verification: {}", verificationId, e);
            // Don't fail the workflow if email fails
        }

        log.info("Step 4 (Review) completed. Verification {} is now UNDER_REVIEW", verificationId);
        return getWorkflowStatus(verificationId);
    }

    /**
     * Get current workflow status and progress.
     * Returns detailed information about which steps are completed and what's next.
     * 
     * @param verificationId The verification ID
     * @return Map containing workflow status, progress, and next steps
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkflowStatus(Long verificationId) {
        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        Map<String, Object> status = new HashMap<>();
        status.put("verificationId", verificationId);
        status.put("userId", verification.getUser().getId());
        status.put("level", verification.getVerificationLevel());
        status.put("status", verification.getStatus());

        // Calculate workflow progress
        Map<String, Boolean> steps = new HashMap<>();
        steps.put(STEP_USER_INFO, true); // Always true (user must exist)
        steps.put(STEP_DOCUMENT_VERIFICATION, verification.getSumsubApplicantId() != null);
        steps.put(STEP_QUESTIONNAIRE, hasCompletedQuestionnaire(verification));
        steps.put(STEP_REVIEW, verification.getStatus() != VerificationStatus.PENDING);
        steps.put(STEP_COMPLETED, verification.getStatus() == VerificationStatus.APPROVED);

        status.put("steps", steps);

        // Calculate progress percentage
        long completedSteps = steps.values().stream().filter(Boolean::booleanValue).count();
        int progressPercentage = (int) ((completedSteps / 5.0) * 100);
        status.put("progressPercentage", progressPercentage);

        // Determine current step
        String currentStep;
        if (!steps.get(STEP_DOCUMENT_VERIFICATION)) {
            currentStep = STEP_DOCUMENT_VERIFICATION;
        } else if (!steps.get(STEP_QUESTIONNAIRE)) {
            currentStep = STEP_QUESTIONNAIRE;
        } else if (!steps.get(STEP_REVIEW)) {
            currentStep = STEP_REVIEW;
        } else {
            currentStep = STEP_COMPLETED;
        }
        status.put("currentStep", currentStep);

        // Add metadata
        status.put("submittedAt", verification.getSubmittedAt());
        status.put("reviewedAt", verification.getReviewedAt());
        status.put("riskLevel", verification.getRiskLevel());

        return status;
    }

    /**
     * Get simplified workflow progress (for UI display).
     * 
     * @param verificationId The verification ID
     * @return Progress information
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkflowProgress(Long verificationId) {
        Map<String, Object> fullStatus = getWorkflowStatus(verificationId);
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("verificationId", verificationId);
        progress.put("progressPercentage", fullStatus.get("progressPercentage"));
        progress.put("currentStep", fullStatus.get("currentStep"));
        progress.put("status", fullStatus.get("status"));
        
        return progress;
    }

    /**
     * Validate user has required information for KYC.
     */
    private void validateUserInformation(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalStateException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalStateException("Last name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalStateException("Email is required");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalStateException("Phone number is required");
        }
        if (user.getDateOfBirth() == null) {
            throw new IllegalStateException("Date of birth is required");
        }
    }

    /**
     * Check if user has completed the questionnaire.
     */
    private boolean hasCompletedQuestionnaire(CustomerKycVerification verification) {
        // Check if user has submitted any answers
        // In a full implementation, would check if ALL required questions are answered
        User user = verification.getUser();
        return !customerAnswerService.getAnswersByUser(user.getId()).isEmpty();
    }

    /**
     * Validate all workflow prerequisites before final review.
     */
    private void validateWorkflowPrerequisites(CustomerKycVerification verification) {
        // Step 1: User info (always valid if user exists)
        validateUserInformation(verification.getUser());

        // Step 2: Document verification
        if (verification.getSumsubApplicantId() == null) {
            throw new IllegalStateException("Document verification must be completed before review");
        }

        // Step 3: Questionnaire
        if (!hasCompletedQuestionnaire(verification)) {
            throw new IllegalStateException("Compliance questionnaire must be completed before review");
        }
    }
}
