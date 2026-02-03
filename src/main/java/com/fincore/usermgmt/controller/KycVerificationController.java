package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.KycVerificationRequestDTO;
import com.fincore.usermgmt.dto.KycVerificationResponseDTO;
import com.fincore.usermgmt.dto.KycVerificationUpdateDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.service.KycVerificationService;
import com.fincore.usermgmt.mapper.KycAmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for KYC Verification endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kyc-verification")
@RequiredArgsConstructor
public class KycVerificationController {

    private final KycVerificationService kycService;
    private final KycAmlMapper mapper;

    /**
     * Submit a new KYC verification
     * POST /api/v1/kyc-verification/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<KycVerificationResponseDTO> submitVerification(
            @RequestBody KycVerificationRequestDTO request) {

        log.info("Submitting KYC verification for user: {}", request.getUserId());

        // Note: In real implementation, fetch User from database
        User user = User.builder().id(request.getUserId()).build();

        CustomerKycVerification verification = kycService.submitVerification(
                user,
                VerificationLevel.valueOf(request.getVerificationLevel())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toKycVerificationResponseDTO(verification));
    }

    /**
     * Get KYC verification by ID
     * GET /api/v1/kyc-verification/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<KycVerificationResponseDTO> getVerification(@PathVariable Long id) {
        log.info("Fetching verification: {}", id);

        CustomerKycVerification verification = kycService.getVerificationById(id);
        return ResponseEntity.ok(mapper.toKycVerificationResponseDTO(verification));
    }

    /**
     * Get user's current verification
     * GET /api/v1/kyc-verification/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<KycVerificationResponseDTO> getUserVerification(@PathVariable Long userId) {
        log.info("Fetching verification for user: {}", userId);

        return kycService.getUserVerification(userId)
                .map(v -> ResponseEntity.ok(mapper.toKycVerificationResponseDTO(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update verification status
     * PUT /api/v1/kyc-verification/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<KycVerificationResponseDTO> updateVerificationStatus(
            @PathVariable Long id,
            @RequestBody KycVerificationUpdateDTO request) {

        log.info("Updating verification {} status to: {}", id, request.getStatus());

        User reviewer = User.builder().id(request.getReviewedById()).build();

        CustomerKycVerification updated = kycService.updateVerificationStatus(
                id,
                VerificationStatus.valueOf(request.getStatus()),
                reviewer,
                request.getReviewResult()
        );

        return ResponseEntity.ok(mapper.toKycVerificationResponseDTO(updated));
    }

    /**
     * Get expired verifications
     * GET /api/v1/kyc-verification/expired
     */
    @GetMapping("/expired")
    public ResponseEntity<List<KycVerificationResponseDTO>> getExpiredVerifications() {
        log.info("Fetching expired verifications");

        List<CustomerKycVerification> expired = kycService.findExpiredVerifications();
        List<KycVerificationResponseDTO> response = expired.stream()
                .map(mapper::toKycVerificationResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get verifications by status
     * GET /api/v1/kyc-verification/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KycVerificationResponseDTO>> getVerificationsByStatus(
            @PathVariable String status) {

        log.info("Fetching verifications with status: {}", status);

        List<CustomerKycVerification> verifications = kycService
                .getVerificationsByStatus(com.fincore.usermgmt.entity.enums.VerificationStatus.valueOf(status));

        List<KycVerificationResponseDTO> response = verifications.stream()
                .map(mapper::toKycVerificationResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get verification count by status
     * GET /api/v1/kyc-verification/count/{status}
     */
    @GetMapping("/count/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable String status) {
        log.info("Counting verifications with status: {}", status);

        long count = kycService.countByStatus(
                com.fincore.usermgmt.entity.enums.VerificationStatus.valueOf(status)
        );

        return ResponseEntity.ok(count);
    }

    /**
     * Check if user has approved verification
     * GET /api/v1/kyc-verification/approved/{userId}
     */
    @GetMapping("/approved/{userId}")
    public ResponseEntity<Boolean> hasApprovedVerification(@PathVariable Long userId) {
        log.info("Checking approved verification for user: {}", userId);

        boolean hasApproved = kycService.hasApprovedVerification(userId);
        return ResponseEntity.ok(hasApproved);
    }

    /**
     * Delete verification
     * DELETE /api/v1/kyc-verification/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerification(@PathVariable Long id) {
        log.info("Deleting verification: {}", id);

        kycService.deleteVerification(id);
        return ResponseEntity.noContent().build();
    }
}
