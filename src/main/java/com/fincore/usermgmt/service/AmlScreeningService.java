package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.ScreeningType;
import com.fincore.usermgmt.entity.enums.RiskLevel;
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
 * Service for managing AML screening results
 * Handles screening execution, result processing, and risk assessment
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AmlScreeningService {

    private final AmlScreeningResultRepository screeningRepository;
    private final KycVerificationService kycService;

    /**
     * Create and save AML screening result
     */
    public AmlScreeningResult createScreening(
            CustomerKycVerification verification,
            User user,
            ScreeningType screeningType,
            Boolean matchFound,
            Integer riskScore,
            String matchDetails) {

        log.info("Creating AML screening for verification: {}, type: {}", 
                verification.getVerificationId(), screeningType);

        if (riskScore != null && (riskScore < 0 || riskScore > 100)) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }

        AmlScreeningResult screening = AmlScreeningResult.builder()
                .verification(verification)
                .user(user)
                .screeningType(screeningType)
                .matchFound(matchFound != null ? matchFound : false)
                .riskScore(riskScore != null ? riskScore : 0)
                .matchDetails(matchDetails)
                .screenedAt(LocalDateTime.now())
                .build();

        AmlScreeningResult saved = screeningRepository.save(screening);
        log.info("AML screening created with ID: {}", saved.getScreeningId());

        // Update verification risk level
        kycService.updateRiskLevel(verification.getVerificationId());

        return saved;
    }

    /**
     * Get screening result by ID
     */
    @Transactional(readOnly = true)
    public AmlScreeningResult getScreeningById(Long screeningId) {
        return screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));
    }

    /**
     * Get all screenings for a verification
     */
    @Transactional(readOnly = true)
    public List<AmlScreeningResult> getScreeningsByVerification(Long verificationId) {
        log.info("Fetching screenings for verification: {}", verificationId);
        return screeningRepository.findByVerification_VerificationId(verificationId);
    }

    /**
     * Get screenings for verification with pagination
     */
    @Transactional(readOnly = true)
    public Page<AmlScreeningResult> getScreeningsByVerificationPaged(
            Long verificationId,
            Pageable pageable) {
        return screeningRepository.findByVerification_VerificationId(verificationId, pageable);
    }

    /**
     * Get all screenings for a user
     */
    @Transactional(readOnly = true)
    public List<AmlScreeningResult> getScreeningsByUser(Long userId) {
        log.info("Fetching screenings for user: {}", userId);
        return screeningRepository.findByUser_Id(userId);
    }

    /**
     * Get screenings by type
     */
    @Transactional(readOnly = true)
    public List<AmlScreeningResult> getScreeningsByType(ScreeningType screeningType) {
        log.info("Fetching screenings by type: {}", screeningType);
        return screeningRepository.findByScreeningType(screeningType);
    }

    /**
     * Get screenings by type with pagination
     */
    @Transactional(readOnly = true)
    public Page<AmlScreeningResult> getScreeningsByTypePaged(
            ScreeningType screeningType,
            Pageable pageable) {
        return screeningRepository.findByScreeningType(screeningType, pageable);
    }

    /**
     * Get all high-risk screenings (matches found)
     */
    @Transactional(readOnly = true)
    public List<AmlScreeningResult> getHighRiskScreenings() {
        log.info("Fetching high-risk screenings");
        return screeningRepository.findHighRiskScreenings();
    }

    /**
     * Get high-risk screenings with pagination
     */
    @Transactional(readOnly = true)
    public Page<AmlScreeningResult> getHighRiskScreeningsPaged(Pageable pageable) {
        return screeningRepository.findHighRiskScreenings(pageable);
    }

    /**
     * Get latest screening for a verification
     */
    @Transactional(readOnly = true)
    public Optional<AmlScreeningResult> getLatestScreening(Long verificationId) {
        return screeningRepository.findLatestByVerificationId(verificationId);
    }

    /**
     * Count matches by screening type
     */
    @Transactional(readOnly = true)
    public long countMatchesByType(ScreeningType screeningType) {
        return screeningRepository.countMatchesByScreeningType(screeningType);
    }

    /**
     * Update screening result
     */
    public AmlScreeningResult updateScreening(
            Long screeningId,
            Boolean matchFound,
            Integer riskScore,
            String matchDetails) {

        log.info("Updating screening: {}", screeningId);

        if (riskScore != null && (riskScore < 0 || riskScore > 100)) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }

        AmlScreeningResult screening = getScreeningById(screeningId);

        if (matchFound != null) {
            screening.setMatchFound(matchFound);
        }
        if (riskScore != null) {
            screening.setRiskScore(riskScore);
        }
        if (matchDetails != null) {
            screening.setMatchDetails(matchDetails);
        }

        AmlScreeningResult updated = screeningRepository.save(screening);

        // Update verification risk level
        if (screening.getVerification() != null) {
            kycService.updateRiskLevel(screening.getVerification().getVerificationId());
        }

        return updated;
    }

    /**
     * Assess overall risk based on all screenings
     */
    @Transactional(readOnly = true)
    public RiskLevel assessOverallRisk(Long verificationId) {
        log.info("Assessing overall risk for verification: {}", verificationId);

        List<AmlScreeningResult> screenings = getScreeningsByVerification(verificationId);

        if (screenings.isEmpty()) {
            return RiskLevel.LOW;
        }

        boolean hasMatches = screenings.stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getMatchFound()));

        if (hasMatches) {
            return RiskLevel.HIGH;
        }

        int maxRiskScore = screenings.stream()
                .mapToInt(s -> s.getRiskScore() != null ? s.getRiskScore() : 0)
                .max()
                .orElse(0);

        if (maxRiskScore > 70) {
            return RiskLevel.HIGH;
        } else if (maxRiskScore > 40) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * Trigger sanctions screening
     */
    public AmlScreeningResult triggerSanctionsScreening(
            CustomerKycVerification verification,
            User user) {

        log.info("Triggering sanctions screening for user: {}", user.getId());
        // In real implementation, would call external sanctions screening API
        return createScreening(verification, user, ScreeningType.SANCTIONS, false, 0, null);
    }

    /**
     * Trigger PEP screening
     */
    public AmlScreeningResult triggerPepScreening(
            CustomerKycVerification verification,
            User user) {

        log.info("Triggering PEP screening for user: {}", user.getId());
        // In real implementation, would call external PEP screening API
        return createScreening(verification, user, ScreeningType.PEP, false, 0, null);
    }

    /**
     * Trigger adverse media screening
     */
    public AmlScreeningResult triggerAdverseMediaScreening(
            CustomerKycVerification verification,
            User user) {

        log.info("Triggering adverse media screening for user: {}", user.getId());
        // In real implementation, would call external adverse media screening API
        return createScreening(verification, user, ScreeningType.ADVERSE_MEDIA, false, 0, null);
    }

    /**
     * Delete screening result
     */
    public void deleteScreening(Long screeningId) {
        log.info("Deleting screening: {}", screeningId);
        screeningRepository.deleteById(screeningId);
    }
}
