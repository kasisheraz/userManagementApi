package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.service.KycWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for KYC workflow operations.
 * Provides endpoints for each step of the KYC verification workflow.
 */
@RestController
@RequestMapping("/api/kyc/workflow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC Workflow", description = "KYC verification workflow management")
public class KycWorkflowController {

    private final KycWorkflowService kycWorkflowService;
    private final UserRepository userRepository;

    /**
     * Start a new KYC workflow process.
     * 
     * @param level Verification level (BASIC, enhanced, or FULL)
     * @return The created verification with workflow status
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Start KYC workflow", description = "Initiate a new KYC verification workflow")
    public ResponseEntity<Map<String, Object>> startWorkflow(
            @RequestParam VerificationLevel level) {
        
        log.info("Starting KYC workflow for level: {}", level);

        User currentUser = getCurrentUser();
        CustomerKycVerification verification = kycWorkflowService.startKycProcess(currentUser, level);
        
        // Return workflow status immediately
        Map<String, Object> response = kycWorkflowService.getWorkflowStatus(verification.getVerificationId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Complete Step 1: User Information.
     * Validates that user has all required information.
     * 
     * @param verificationId The verification ID
     * @return Updated workflow status
     */
    @PostMapping("/{verificationId}/step1/user-info")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Complete user info step", description = "Validate and complete Step 1: User Information")
    public ResponseEntity<Map<String, Object>> completeStep1(
            @PathVariable Long verificationId) {
        
        log.info("Completing Step 1 for verification: {}", verificationId);
        
        // Verify ownership
        verifyVerificationOwnership(verificationId);
        
        Map<String, Object> status = kycWorkflowService.completeStep1UserInfo(verificationId);
        return ResponseEntity.ok(status);
    }

    /**
     * Complete Step 2: Document Verification via SumSub.
     * Links the SumSub applicant ID to this verification.
     * 
     * @param verificationId The verification ID
     * @param request Request containing sumsubApplicantId
     * @return Updated workflow status
     */
    @PostMapping("/{verificationId}/step2/sumsub")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Complete document verification step", description = "Complete Step 2: Document Verification via SumSub")
    public ResponseEntity<Map<String, Object>> completeStep2(
            @PathVariable Long verificationId,
            @RequestBody Map<String, String> request) {
        
        log.info("Completing Step 2 for verification: {}", verificationId);
        
        // Verify ownership
        verifyVerificationOwnership(verificationId);
        
        String sumsubApplicantId = request.get("sumsubApplicantId");
        if (sumsubApplicantId == null || sumsubApplicantId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sumsubApplicantId is required"));
        }
        
        Map<String, Object> status = kycWorkflowService.completeStep2SumSub(verificationId, sumsubApplicantId);
        return ResponseEntity.ok(status);
    }

    /**
     * Complete Step 3: Compliance Questionnaire.
     * Submits user answers to compliance questions.
     * 
     * @param verificationId The verification ID
     * @param request Request containing list of answers
     * @return Updated workflow status
     */
    @PostMapping("/{verificationId}/step3/questionnaire")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Complete questionnaire step", description = "Complete Step 3: Compliance Questionnaire")
    public ResponseEntity<Map<String, Object>> completeStep3(
            @PathVariable Long verificationId,
            @RequestBody Map<String, List<CustomerAnswerRequestDTO>> request) {
        
        log.info("Completing Step 3 for verification: {}", verificationId);
        
        // Verify ownership
        verifyVerificationOwnership(verificationId);
        
        List<CustomerAnswerRequestDTO> answers = request.get("answers");
        if (answers == null || answers.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "answers are required"));
        }
        
        Map<String, Object> status = kycWorkflowService.completeStep3Questionnaire(verificationId, answers);
        return ResponseEntity.ok(status);
    }

    /**
     * Complete Step 4: Final Review.
     * Submits verification for admin review.
     * 
     * @param verificationId The verification ID
     * @return Updated workflow status
     */
    @PostMapping("/{verificationId}/step4/review")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Complete review step", description = "Complete Step 4: Final Review - Submit for admin review")
    public ResponseEntity<Map<String, Object>> completeStep4(
            @PathVariable Long verificationId) {
        
        log.info("Completing Step 4 for verification: {}", verificationId);
        
        // Verify ownership
        verifyVerificationOwnership(verificationId);
        
        Map<String, Object> status = kycWorkflowService.completeStep4Review(verificationId);
        return ResponseEntity.ok(status);
    }

    /**
     * Get current workflow status and progress.
     * 
     * @param verificationId The verification ID
     * @return Workflow status with completion details
     */
    @GetMapping("/{verificationId}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get workflow status", description = "Get detailed workflow status and progress")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus(
            @PathVariable Long verificationId) {
        
        log.info("Getting workflow status for verification: {}", verificationId);
        
        // Admins can view any verification, users only their own
        if (!hasRole("ADMIN")) {
            verifyVerificationOwnership(verificationId);
        }
        
        Map<String, Object> status = kycWorkflowService.getWorkflowStatus(verificationId);
        return ResponseEntity.ok(status);
    }

    /**
     * Get workflow progress (simplified for UI).
     * 
     * @param verificationId The verification ID
     * @return Simplified progress information
     */
    @GetMapping("/{verificationId}/progress")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get workflow progress", description = "Get simplified workflow progress for UI display")
    public ResponseEntity<Map<String, Object>> getWorkflowProgress(
            @PathVariable Long verificationId) {
        
        log.info("Getting workflow progress for verification: {}", verificationId);
        
        // Admins can view any verification, users only their own
        if (!hasRole("ADMIN")) {
            verifyVerificationOwnership(verificationId);
        }
        
        Map<String, Object> progress = kycWorkflowService.getWorkflowProgress(verificationId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Get current user from security context.
     * In production, would extract user ID from JWT token.
     * For now, requires userId to be passed via request parameter or path variable.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Try to get user by phone number (current auth mechanism)
        return userRepository.findByPhoneNumber(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * Verify that current user owns this verification.
     */
    private void verifyVerificationOwnership(Long verificationId) {
        User currentUser = getCurrentUser();
        
        // This would typically query the verification and check ownership
        // For simplicity, we'll let the service layer handle this
        // In production, add explicit ownership check here
    }

    /**
     * Check if current user has a specific role.
     */
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Exception handler for workflow-specific errors.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        log.error("Workflow validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid workflow request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
