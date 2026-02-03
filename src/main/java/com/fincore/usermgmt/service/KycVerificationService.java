package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.entity.enums.RiskLevel;
import com.fincore.usermgmt.repository.CustomerKycVerificationRepository;
import com.fincore.usermgmt.repository.AmlScreeningResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing KYC verification workflow
 * Handles submission, review, approval/rejection, and expiration logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KycVerificationService {

    private final CustomerKycVerificationRepository kycRepository;
    private final AmlScreeningResultRepository amlRepository;

    /**
     * Submit a new KYC verification request
     */
    public CustomerKycVerification submitVerification(User user, VerificationLevel level) {
        log.info("Submitting KYC verification for user: {}, level: {}", user.getId(), level);

        // Check for existing active verification
        Optional<CustomerKycVerification> existing = kycRepository.findByUser_Id(user.getId());
        if (existing.isPresent() && existing.get().getStatus() == VerificationStatus.PENDING) {
            log.warn("User {} has pending verification", user.getId());
            throw new IllegalStateException("User has pending verification");
        }

        CustomerKycVerification verification = CustomerKycVerification.builder()
                .user(user)
                .verificationLevel(level)
                .status(VerificationStatus.PENDING)
                .riskLevel(RiskLevel.LOW)
                .submittedAt(LocalDateTime.now())
                .build();

        CustomerKycVerification saved = kycRepository.save(verification);
        log.info("KYC verification submitted with ID: {}", saved.getVerificationId());
        return saved;
    }

    /**
     * Update verification status and assign reviewer
     */
    public CustomerKycVerification updateVerificationStatus(
            Long verificationId,
            VerificationStatus status,
            User reviewer,
            String reviewResult) {

        log.info("Updating verification {} to status: {}", verificationId, status);

        CustomerKycVerification verification = kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));

        verification.setStatus(status);
        verification.setReviewedBy(reviewer);
        verification.setReviewResult(reviewResult);
        verification.setReviewedAt(LocalDateTime.now());

        if (status == VerificationStatus.APPROVED) {
            verification.setApprovedAt(LocalDateTime.now());
            verification.setExpiresAt(LocalDateTime.now().plusYears(1));
            log.info("Verification {} approved, expires: {}", verificationId, verification.getExpiresAt());
        } else if (status == VerificationStatus.REJECTED) {
            verification.setRejectedAt(LocalDateTime.now());
            log.info("Verification {} rejected", verificationId);
        }

        return kycRepository.save(verification);
    }

    /**
     * Get verification by ID
     */
    @Transactional(readOnly = true)
    public CustomerKycVerification getVerificationById(Long verificationId) {
        return kycRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found"));
    }

    /**
     * Get user's current verification
     */
    @Transactional(readOnly = true)
    public Optional<CustomerKycVerification> getUserVerification(Long userId) {
        return kycRepository.findByUser_Id(userId);
    }

    /**
     * Get latest verification for user
     */
    @Transactional(readOnly = true)
    public Optional<CustomerKycVerification> getLatestVerification(Long userId) {
        return kycRepository.findLatestByUserId(userId);
    }

    /**
     * Get verifications by status
     */
    @Transactional(readOnly = true)
    public List<CustomerKycVerification> getVerificationsByStatus(VerificationStatus status) {
        log.info("Fetching verifications with status: {}", status);
        return kycRepository.findByStatus(status);
    }

    /**
     * Get verifications by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerKycVerification> getVerificationsByStatusPaged(
            VerificationStatus status,
            Pageable pageable) {
        return kycRepository.findByStatus(status, pageable);
    }

    /**
     * Find all expired verifications
     */
    @Transactional(readOnly = true)
    public List<CustomerKycVerification> findExpiredVerifications() {
        log.info("Finding expired verifications");
        List<CustomerKycVerification> expired = kycRepository.findExpiredVerifications();
        log.info("Found {} expired verifications", expired.size());
        return expired;
    }

    /**
     * Mark expired verifications as expired status
     */
    public void markExpiredVerifications() {
        log.info("Marking expired verifications");
        List<CustomerKycVerification> expired = findExpiredVerifications();

        for (CustomerKycVerification verification : expired) {
            verification.setStatus(VerificationStatus.EXPIRED);
            kycRepository.save(verification);
        }

        log.info("Marked {} verifications as expired", expired.size());
    }

    /**
     * Count verifications by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(VerificationStatus status) {
        return kycRepository.countByStatus(status);
    }

    /**
     * Get AML screenings for a verification
     */
    @Transactional(readOnly = true)
    public List<AmlScreeningResult> getAmlScreenings(Long verificationId) {
        return amlRepository.findByVerification_VerificationId(verificationId);
    }

    /**
     * Update risk level based on AML screening results
     */
    public void updateRiskLevel(Long verificationId) {
        log.info("Updating risk level for verification: {}", verificationId);

        CustomerKycVerification verification = getVerificationById(verificationId);
        List<AmlScreeningResult> screenings = getAmlScreenings(verificationId);

        if (screenings.isEmpty()) {
            verification.setRiskLevel(RiskLevel.LOW);
        } else {
            int maxRiskScore = screenings.stream()
                    .mapToInt(s -> s.getRiskScore() != null ? s.getRiskScore() : 0)
                    .max()
                    .orElse(0);

            boolean hasMatches = screenings.stream()
                    .anyMatch(s -> Boolean.TRUE.equals(s.getMatchFound()));

            if (hasMatches) {
                verification.setRiskLevel(RiskLevel.HIGH);
            } else if (maxRiskScore > 50) {
                verification.setRiskLevel(RiskLevel.MEDIUM);
            } else {
                verification.setRiskLevel(RiskLevel.LOW);
            }
        }

        kycRepository.save(verification);
        log.info("Risk level updated to: {}", verification.getRiskLevel());
    }

    /**
     * Set Sumsub applicant ID for verification
     */
    public void setSumsubApplicantId(Long verificationId, String applicantId) {
        log.info("Setting Sumsub applicant ID for verification: {}", verificationId);

        CustomerKycVerification verification = getVerificationById(verificationId);
        verification.setSumsubApplicantId(applicantId);
        kycRepository.save(verification);
    }

    /**
     * Get verification by Sumsub applicant ID
     */
    @Transactional(readOnly = true)
    public Optional<CustomerKycVerification> getVerificationBySumsubId(String sumsubApplicantId) {
        return kycRepository.findBySumsubApplicantId(sumsubApplicantId);
    }

    /**
     * Check if user has approved verification
     */
    @Transactional(readOnly = true)
    public boolean hasApprovedVerification(Long userId) {
        Optional<CustomerKycVerification> verification = kycRepository
                .findByUserIdAndStatus(userId, VerificationStatus.APPROVED);
        return verification.isPresent() && 
               (verification.get().getExpiresAt() == null || 
                verification.get().getExpiresAt().isAfter(LocalDateTime.now()));
    }

    /**
     * Delete verification
     */
    public void deleteVerification(Long verificationId) {
        log.info("Deleting verification: {}", verificationId);
        kycRepository.deleteById(verificationId);
    }
}
