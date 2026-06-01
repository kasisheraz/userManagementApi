package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.dto.BeneficiaryRequestDTO;
import com.fincore.usermgmt.dto.BeneficiaryResponseDTO;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import com.fincore.usermgmt.repository.BeneficiaryRepository;
import com.fincore.usermgmt.repository.KycDocumentRepository;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Beneficiary management operations.
 * 
 * <p>Business Rules Enforced:</p>
 * <ul>
 *   <li>Max 20 beneficiaries per user</li>
 *   <li>C2C validation: If Is_Counter_Over_Counter = true, Collector_Contact_Number required</li>
 *   <li>Status workflow: PENDING → UNDER_REVIEW → ACTIVE/REJECTED</li>
 *   <li>Only PENDING beneficiaries can be edited or deleted</li>
 *   <li>Document validation before submission</li>
 *   <li>Role-based access control</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final AddressMapper addressMapper;
    // TODO: Add EmailService for notifications when available

    private static final int MAX_BENEFICIARIES_PER_USER = 20;

    /**
     * Create a new beneficiary for a user.
     */
    @Transactional
    public BeneficiaryResponseDTO createBeneficiary(Long userId, BeneficiaryRequestDTO request) {
        log.info("Creating new beneficiary for user {}: {}", userId, request.getBeneficiaryName());

        // Validate user exists
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Business Rule 1: Check 20-beneficiary limit
        long count = beneficiaryRepository.countByUserId(userId);
        if (count >= MAX_BENEFICIARIES_PER_USER) {
            throw new IllegalArgumentException(
                    String.format("Maximum beneficiary limit reached. You can create up to %d beneficiaries.", 
                    MAX_BENEFICIARIES_PER_USER)
            );
        }

        // Business Rule 2: C2C Validation
        validateC2CRequirements(request);

        // Validate address exists
        Address address = addressRepository.findById(request.getRegisteredAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + request.getRegisteredAddressId()));

        // Create beneficiary entity
        Beneficiary beneficiary = Beneficiary.builder()
                .beneficiaryName(request.getBeneficiaryName())
                .nickName(request.getNickName())
                .businessName(request.getBusinessName())
                .country(request.getCountry())
                .owner(owner)
                .registeredAddress(address)
                .isCounterOverCounter(request.getIsCounterOverCounter())
                .collectorContactNumber(request.getCollectorContactNumber())
                .status(BeneficiaryStatus.PENDING)
                .createdBy(userId)
                .lastModifiedBy(userId)
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary created successfully: ID={}, Name={}", saved.getId(), saved.getBeneficiaryName());

        return mapToResponseDTO(saved);
    }

    /**
     * Update an existing beneficiary.
     * Only allowed if beneficiary status is PENDING.
     */
    @Transactional
    public BeneficiaryResponseDTO updateBeneficiary(Long userId, Long beneficiaryId, BeneficiaryRequestDTO request) {
        log.info("Updating beneficiary {} for user {}", beneficiaryId, userId);

        // Find and validate ownership
        Beneficiary beneficiary = findBeneficiaryAndValidateOwnership(beneficiaryId, userId);

        // Business Rule: Can only edit PENDING beneficiaries
        if (!beneficiary.canBeEdited()) {
            throw new IllegalStateException(
                    "Cannot edit beneficiary. Only beneficiaries with PENDING status can be edited. Current status: " + 
                    beneficiary.getStatus()
            );
        }

        // Business Rule: C2C Validation
        validateC2CRequirements(request);

        // Validate address if changed
        if (!beneficiary.getRegisteredAddress().getId().equals(request.getRegisteredAddressId())) {
            Address address = addressRepository.findById(request.getRegisteredAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + request.getRegisteredAddressId()));
            beneficiary.setRegisteredAddress(address);
        }

        // Update fields
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setNickName(request.getNickName());
        beneficiary.setBusinessName(request.getBusinessName());
        beneficiary.setCountry(request.getCountry());
        beneficiary.setIsCounterOverCounter(request.getIsCounterOverCounter());
        beneficiary.setCollectorContactNumber(request.getCollectorContactNumber());
        beneficiary.setLastModifiedBy(userId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary updated successfully: ID={}", updated.getId());

        return mapToResponseDTO(updated);
    }

    /**
     * Get a single beneficiary by ID (user-scoped).
     */
    @Transactional(readOnly = true)
    public BeneficiaryResponseDTO getBeneficiaryById(Long userId, Long beneficiaryId) {
        log.debug("Fetching beneficiary {} for user {}", beneficiaryId, userId);
        Beneficiary beneficiary = findBeneficiaryAndValidateOwnership(beneficiaryId, userId);
        return mapToResponseDTO(beneficiary);
    }

    /**
     * Get all beneficiaries for a user.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getAllBeneficiariesForUser(Long userId) {
        log.debug("Fetching all beneficiaries for user {}", userId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserId(userId);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get beneficiaries filtered by status.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getBeneficiariesByStatus(Long userId, BeneficiaryStatus status) {
        log.debug("Fetching beneficiaries with status {} for user {}", status, userId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserIdAndStatus(userId, status);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search beneficiaries by name.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> searchBeneficiaries(Long userId, String searchTerm) {
        log.debug("Searching beneficiaries for user {} with term: {}", userId, searchTerm);
        List<Beneficiary> beneficiaries = beneficiaryRepository.searchByName(userId, searchTerm);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filter beneficiaries by country.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getBeneficiariesByCountry(Long userId, String country) {
        log.debug("Fetching beneficiaries in country {} for user {}", country, userId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUserIdAndCountry(userId, country);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all C2C beneficiaries for a user.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getC2CBeneficiaries(Long userId) {
        log.debug("Fetching C2C beneficiaries for user {}", userId);
        List<Beneficiary> beneficiaries = beneficiaryRepository.findC2CBeneficiaries(userId);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Submit a beneficiary for admin review.
     * Changes status from PENDING to UNDER_REVIEW.
     */
    @Transactional
    public BeneficiaryResponseDTO submitForReview(Long userId, Long beneficiaryId) {
        log.info("Submitting beneficiary {} for review by user {}", beneficiaryId, userId);

        Beneficiary beneficiary = findBeneficiaryAndValidateOwnership(beneficiaryId, userId);

        // Validate status
        if (!beneficiary.canBeSubmitted()) {
            throw new IllegalStateException(
                    "Cannot submit beneficiary for review. Current status: " + beneficiary.getStatus()
            );
        }

        // Business Rule: Validate required documents are uploaded
        validateRequiredDocuments(beneficiary);

        // Change status to UNDER_REVIEW
        beneficiary.setStatus(BeneficiaryStatus.UNDER_REVIEW);
        beneficiary.setLastModifiedBy(userId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} submitted for review successfully", beneficiaryId);

        return mapToResponseDTO(updated);
    }

    /**
     * Delete a beneficiary (only if PENDING).
     */
    @Transactional
    public void deleteBeneficiary(Long userId, Long beneficiaryId) {
        log.info("Deleting beneficiary {} for user {}", beneficiaryId, userId);

        Beneficiary beneficiary = findBeneficiaryAndValidateOwnership(beneficiaryId, userId);

        // Business Rule: Can only delete PENDING beneficiaries
        if (!beneficiary.canBeEdited()) {
            throw new IllegalStateException(
                    "Cannot delete beneficiary. Only beneficiaries with PENDING status can be deleted. Current status: " + 
                    beneficiary.getStatus()
            );
        }

        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiary {} deleted successfully", beneficiaryId);
    }

    /**
     * Get count of beneficiaries for a user.
     */
    @Transactional(readOnly = true)
    public long getBeneficiaryCount(Long userId) {
        return beneficiaryRepository.countByUserId(userId);
    }

    // ========================================
    // Admin Operations
    // ========================================

    /**
     * Get all beneficiaries (admin view).
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getAllBeneficiaries() {
        log.debug("Fetching all beneficiaries (admin view)");
        List<Beneficiary> beneficiaries = beneficiaryRepository.findAll();
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all beneficiaries pending approval (admin queue).
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> getPendingApprovals() {
        log.debug("Fetching pending approval queue");
        List<Beneficiary> beneficiaries = beneficiaryRepository.findPendingApprovals();
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Approve a beneficiary (admin action).
     */
    @Transactional
    public BeneficiaryResponseDTO approveBeneficiary(Long adminUserId, Long beneficiaryId) {
        log.info("Admin {} approving beneficiary {}", adminUserId, beneficiaryId);

        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found with ID: " + beneficiaryId));

        // Validate can be approved
        if (!beneficiary.canBeApproved()) {
            throw new IllegalStateException(
                    "Cannot approve beneficiary. Current status: " + beneficiary.getStatus()
            );
        }

        // Change status to ACTIVE
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        beneficiary.setReasonDescription(null); // Clear any previous rejection reason
        beneficiary.setLastModifiedBy(adminUserId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} approved successfully", beneficiaryId);

        // TODO: Send approval email notification to beneficiary owner
        // emailService.sendBeneficiaryApprovalEmail(beneficiary.getOwner(), beneficiary);

        return mapToResponseDTO(updated);
    }

    /**
     * Reject a beneficiary (admin action).
     */
    @Transactional
    public BeneficiaryResponseDTO rejectBeneficiary(Long adminUserId, Long beneficiaryId, String reason) {
        log.info("Admin {} rejecting beneficiary {} with reason: {}", adminUserId, beneficiaryId, reason);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found with ID: " + beneficiaryId));

        // Validate can be rejected
        if (!beneficiary.canBeRejected()) {
            throw new IllegalStateException(
                    "Cannot reject beneficiary. Current status: " + beneficiary.getStatus()
            );
        }

        // Change status to REJECTED
        beneficiary.setStatus(BeneficiaryStatus.REJECTED);
        beneficiary.setReasonDescription(reason);
        beneficiary.setLastModifiedBy(adminUserId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} rejected successfully", beneficiaryId);

        // TODO: Send rejection email notification to beneficiary owner
        // emailService.sendBeneficiaryRejectionEmail(beneficiary.getOwner(), beneficiary, reason);

        return mapToResponseDTO(updated);
    }

    /**
     * Suspend an active beneficiary (admin action).
     */
    @Transactional
    public BeneficiaryResponseDTO suspendBeneficiary(Long adminUserId, Long beneficiaryId, String reason) {
        log.info("Admin {} suspending beneficiary {} with reason: {}", adminUserId, beneficiaryId, reason);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Suspension reason is required");
        }

        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found with ID: " + beneficiaryId));

        // Validate is ACTIVE
        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot suspend beneficiary. Only ACTIVE beneficiaries can be suspended. Current status: " + 
                    beneficiary.getStatus()
            );
        }

        // Change status to SUSPENDED
        beneficiary.setStatus(BeneficiaryStatus.SUSPENDED);
        beneficiary.setReasonDescription(reason);
        beneficiary.setLastModifiedBy(adminUserId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} suspended successfully", beneficiaryId);

        // TODO: Send suspension email notification to beneficiary owner
        // emailService.sendBeneficiarySuspensionEmail(beneficiary.getOwner(), beneficiary, reason);

        return mapToResponseDTO(updated);
    }

    /**
     * Reactivate a suspended beneficiary (admin action).
     */
    @Transactional
    public BeneficiaryResponseDTO reactivateBeneficiary(Long adminUserId, Long beneficiaryId) {
        log.info("Admin {} reactivating beneficiary {}", adminUserId, beneficiaryId);

        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found with ID: " + beneficiaryId));

        // Validate is SUSPENDED
        if (beneficiary.getStatus() != BeneficiaryStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Cannot reactivate beneficiary. Only SUSPENDED beneficiaries can be reactivated. Current status: " + 
                    beneficiary.getStatus()
            );
        }

        // Change status back to ACTIVE
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        beneficiary.setReasonDescription("Reactivated by admin");
        beneficiary.setLastModifiedBy(adminUserId);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} reactivated successfully", beneficiaryId);

        // TODO: Send reactivation email notification to beneficiary owner
        // emailService.sendBeneficiaryReactivationEmail(beneficiary.getOwner(), beneficiary);

        return mapToResponseDTO(updated);
    }

    /**
     * Search all beneficiaries by name (admin view).
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponseDTO> adminSearchBeneficiaries(String searchTerm) {
        log.debug("Admin searching beneficiaries with term: {}", searchTerm);
        List<Beneficiary> beneficiaries = beneficiaryRepository.adminSearchByName(searchTerm);
        return beneficiaries.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get beneficiary statistics by status (admin dashboard).
     */
    @Transactional(readOnly = true)
    public Map<BeneficiaryStatus, Long> getStatusStatistics() {
        log.debug("Fetching beneficiary status statistics");
        List<Object[]> results = beneficiaryRepository.getStatusStatistics();
        
        Map<BeneficiaryStatus, Long> stats = new EnumMap<>(BeneficiaryStatus.class);
        for (Object[] result : results) {
            BeneficiaryStatus status = (BeneficiaryStatus) result[0];
            Long count = (Long) result[1];
            stats.put(status, count);
        }
        
        return stats;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Find beneficiary and validate ownership.
     */
    private Beneficiary findBeneficiaryAndValidateOwnership(Long beneficiaryId, Long userId) {
        return beneficiaryRepository.findByIdAndUserId(beneficiaryId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Beneficiary not found with ID: " + beneficiaryId + " or you do not have permission to access it"
                ));
    }

    /**
     * Validate C2C requirements.
     * Business Rule: If Is_Counter_Over_Counter = true, Collector_Contact_Number is required.
     */
    private void validateC2CRequirements(BeneficiaryRequestDTO request) {
        if (Boolean.TRUE.equals(request.getIsCounterOverCounter())) {
            if (request.getCollectorContactNumber() == null || request.getCollectorContactNumber().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Collector Contact Number is required when Counter Over Counter is enabled"
                );
            }
        }
    }

    /**
     * Validate required documents before submission.
     * Business Rule: Documents 1-3 are always required, Document 4 (Collector ID) is required if C2C = true.
     */
    private void validateRequiredDocuments(Beneficiary beneficiary) {
        List<KycDocument> documents = kycDocumentRepository.findByBeneficiaryId(beneficiary.getId());
        
        // Check required document types
        boolean hasClientAuthLetter = documents.stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.CLIENT_AUTHORISATION_LETTER);
        boolean hasBeneficiaryKYC = documents.stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.BENEFICIARY_COMPANY_KYC);
        boolean hasBeneficiaryAgreement = documents.stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.BENEFICIARY_AGREEMENT);
        
        if (!hasClientAuthLetter || !hasBeneficiaryKYC || !hasBeneficiaryAgreement) {
            throw new IllegalStateException(
                    "Required documents are missing. Please upload: Client Authorisation Letter, " +
                    "Beneficiary Company KYC, and Beneficiary Agreement before submitting."
            );
        }
        
        // If C2C, check for Collector Identification
        if (beneficiary.isC2C()) {
            boolean hasCollectorID = documents.stream()
                    .anyMatch(doc -> doc.getDocumentType() == DocumentType.COLLECTOR_IDENTIFICATION);
            if (!hasCollectorID) {
                throw new IllegalStateException(
                        "Collector Identification document is required for Counter Over Counter beneficiaries."
                );
            }
        }
    }

    /**
     * Map Beneficiary entity to Response DTO.
     */
    private BeneficiaryResponseDTO mapToResponseDTO(Beneficiary beneficiary) {
        AddressDTO addressDTO = null;
        if (beneficiary.getRegisteredAddress() != null) {
            addressDTO = addressMapper.toDto(beneficiary.getRegisteredAddress());
        }

        // Get creator/modifier names
        String createdByName = beneficiary.getCreatedBy() != null 
                ? userRepository.findById(beneficiary.getCreatedBy())
                        .map(User::getFullName)
                        .orElse("Unknown")
                : "System";

        String lastModifiedByName = beneficiary.getLastModifiedBy() != null 
                ? userRepository.findById(beneficiary.getLastModifiedBy())
                        .map(User::getFullName)
                        .orElse("Unknown")
                : "System";

        return BeneficiaryResponseDTO.builder()
                .id(beneficiary.getId())
                .ownerId(beneficiary.getOwner().getId())
                .ownerName(beneficiary.getOwner().getFullName())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .nickName(beneficiary.getNickName())
                .businessName(beneficiary.getBusinessName())
                .country(beneficiary.getCountry())
                .registeredAddress(addressDTO)
                .isCounterOverCounter(beneficiary.getIsCounterOverCounter())
                .collectorContactNumber(beneficiary.getCollectorContactNumber())
                .status(beneficiary.getStatus().name())
                .reasonDescription(beneficiary.getReasonDescription())
                .createdDatetime(beneficiary.getCreatedDatetime())
                .createdByName(createdByName)
                .lastModifiedDatetime(beneficiary.getLastModifiedDatetime())
                .lastModifiedByName(lastModifiedByName)
                .canBeEdited(beneficiary.canBeEdited())
                .canBeSubmitted(beneficiary.canBeSubmitted())
                .isActive(beneficiary.isActive())
                .build();
    }
}
