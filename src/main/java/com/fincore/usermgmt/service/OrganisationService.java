package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.mapper.OrganisationMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import com.fincore.usermgmt.repository.KycDocumentRepository;
import com.fincore.usermgmt.repository.OrganisationRepository;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Organisation management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final KycDocumentService kycDocumentService;
    private final KycDocumentRepository kycDocumentRepository;
    private final OrganisationMapper organisationMapper;
    private final AddressMapper addressMapper;
    private final SecurityUtil securityUtil;

    /**
     * Create a new organisation.
     */
    @Transactional
    public OrganisationDTO createOrganisation(OrganisationCreateDTO createDTO) {
        log.info("Creating new organisation: {}", createDTO.getLegalName());

        // Validate owner exists
        User owner = userRepository.findById(createDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner user not found with ID: " + createDTO.getOwnerId()));

        // Check for duplicate registration number (only if provided and not empty)
        if (createDTO.getRegistrationNumber() != null && 
            !createDTO.getRegistrationNumber().trim().isEmpty() &&
            organisationRepository.existsByRegistrationNumber(createDTO.getRegistrationNumber())) {
            throw new RuntimeException("Organisation with registration number already exists: " + createDTO.getRegistrationNumber());
        }

        // Check for duplicate company number (only if provided and not empty)
        if (createDTO.getCompanyNumber() != null && 
            !createDTO.getCompanyNumber().trim().isEmpty() &&
            organisationRepository.existsByCompanyNumber(createDTO.getCompanyNumber())) {
            throw new RuntimeException("Organisation with company number already exists: " + createDTO.getCompanyNumber());
        }

        Organisation organisation = organisationMapper.toOrganisation(createDTO);
        organisation.setOwner(owner);

        // Handle addresses
        if (createDTO.getRegisteredAddress() != null) {
            Address registeredAddress = addressMapper.toAddress(createDTO.getRegisteredAddress());
            registeredAddress.setAddressType(AddressType.REGISTERED);
            organisation.setRegisteredAddress(registeredAddress);
        }

        if (createDTO.getBusinessAddress() != null) {
            Address businessAddress = addressMapper.toAddress(createDTO.getBusinessAddress());
            businessAddress.setAddressType(AddressType.BUSINESS);
            organisation.setBusinessAddress(businessAddress);
        }

        if (createDTO.getCorrespondenceAddress() != null) {
            Address correspondenceAddress = addressMapper.toAddress(createDTO.getCorrespondenceAddress());
            correspondenceAddress.setAddressType(AddressType.CORRESPONDENCE);
            organisation.setCorrespondenceAddress(correspondenceAddress);
        }

        Organisation saved = organisationRepository.save(organisation);
        log.info("Created organisation with ID: {}", saved.getId());
        
        // Handle KYC documents if provided
        if (createDTO.getKycDocuments() != null && !createDTO.getKycDocuments().isEmpty()) {
            log.info("Creating {} KYC documents for organisation {}", 
                createDTO.getKycDocuments().size(), saved.getId());
            
            for (KycDocumentCreateDTO kycDocDTO : createDTO.getKycDocuments()) {
                // Set the organisation ID for each document
                kycDocDTO.setOrganisationId(saved.getId());
                try {
                    kycDocumentService.createDocument(kycDocDTO);
                } catch (Exception e) {
                    log.error("Failed to create KYC document: {}", e.getMessage());
                    // Continue with other documents even if one fails
                }
            }
        }
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Get organisation by ID.
     */
    @Transactional(readOnly = true)
    public Optional<OrganisationDTO> getOrganisationById(Long id) {
        log.debug("Fetching organisation by ID: {}", id);
        return organisationRepository.findById(id)
                .map(organisationMapper::toOrganisationDTO);
    }

    /**
     * Get all organisations with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrganisationDTO> getAllOrganisations(int page, int size, String sortBy, String sortDirection) {
        log.debug("Fetching all organisations - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Business Users can only see their own organisations
        if (securityUtil.isBusinessUser()) {
            return securityUtil.getCurrentUser()
                    .map(user -> {
                        List<Organisation> userOrgs = organisationRepository.findByOwnerId(user.getId());
                        // Calculate pagination manually
                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), userOrgs.size());
                        List<Organisation> pageContent = userOrgs.subList(start, Math.min(end, userOrgs.size()));
                        Page<Organisation> organisationPage = new PageImpl<>(pageContent, pageable, userOrgs.size());
                        return buildPagedResponse(organisationPage);
                    })
                    .orElse(new PagedResponse<>(List.of(), 0, 0, 0L, 0, true, true));
        }
        
        // All other roles can see all organisations
        Page<Organisation> organisationPage = organisationRepository.findAll(pageable);
        
        return buildPagedResponse(organisationPage);
    }

    /**
     * Search organisations with filters.
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrganisationDTO> searchOrganisations(OrganisationSearchDTO searchDTO) {
        log.debug("Searching organisations with criteria: {}", searchDTO);
        
        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("DESC") 
                ? Sort.by(searchDTO.getSortBy()).descending() 
                : Sort.by(searchDTO.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        OrganisationStatus status = null;
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            try {
                status = OrganisationStatus.valueOf(searchDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", searchDTO.getStatus());
            }
        }

        OrganisationType type = null;
        if (searchDTO.getOrganisationType() != null && !searchDTO.getOrganisationType().isEmpty()) {
            try {
                type = OrganisationType.valueOf(searchDTO.getOrganisationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid organisation type filter: {}", searchDTO.getOrganisationType());
            }
        }

        // Make variables effectively final for lambda
        final OrganisationStatus finalStatus = status;
        final OrganisationType finalType = type;

        // Business Users can only search their own organisations
        if (securityUtil.isBusinessUser()) {
            return securityUtil.getCurrentUser()
                    .map(user -> {
                        List<Organisation> userOrgs = organisationRepository.findByOwnerId(user.getId());
                        // Apply filters manually
                        List<Organisation> filtered = userOrgs.stream()
                                .filter(org -> searchDTO.getSearchTerm() == null || searchDTO.getSearchTerm().isEmpty() ||
                                        org.getLegalName().toLowerCase().contains(searchDTO.getSearchTerm().toLowerCase()) ||
                                        (org.getCompanyNumber() != null && org.getCompanyNumber().toLowerCase().contains(searchDTO.getSearchTerm().toLowerCase())))
                                .filter(org -> finalStatus == null || org.getStatus() == finalStatus)
                                .filter(org -> finalType == null || org.getOrganisationType() == finalType)
                                .collect(Collectors.toList());
                        
                        // Calculate pagination manually
                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), filtered.size());
                        List<Organisation> pageContent = filtered.subList(start, Math.min(end, filtered.size()));
                        Page<Organisation> organisationPage = new PageImpl<>(pageContent, pageable, filtered.size());
                        return buildPagedResponse(organisationPage);
                    })
                    .orElse(new PagedResponse<>(List.of(), 0, 0, 0L, 0, true, true));
        }

        // All other roles can search all organisations
        Page<Organisation> organisationPage = organisationRepository.searchOrganisations(
                searchDTO.getSearchTerm(), status, type, pageable);
        
        return buildPagedResponse(organisationPage);
    }

    /**
     * Get organisations by owner user ID.
     */
    @Transactional(readOnly = true)
    public List<OrganisationDTO> getOrganisationsByOwner(Long ownerId) {
        log.debug("Fetching organisations for owner: {}", ownerId);
        return organisationRepository.findByOwnerId(ownerId).stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get organisations by status.
     */
    @Transactional(readOnly = true)
    public List<OrganisationDTO> getOrganisationsByStatus(String statusStr) {
        log.debug("Fetching organisations by status: {}", statusStr);
        OrganisationStatus status = OrganisationStatus.valueOf(statusStr.toUpperCase());
        return organisationRepository.findByStatus(status).stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an organisation.
     */
    @Transactional
    public OrganisationDTO updateOrganisation(Long id, OrganisationUpdateDTO updateDTO) {
        log.info("Updating organisation ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        // Check for duplicate registration number (if changed)
        if (updateDTO.getRegistrationNumber() != null && 
            !updateDTO.getRegistrationNumber().equals(organisation.getRegistrationNumber()) &&
            organisationRepository.existsByRegistrationNumber(updateDTO.getRegistrationNumber())) {
            throw new RuntimeException("Organisation with registration number already exists: " + updateDTO.getRegistrationNumber());
        }

        organisationMapper.updateOrganisationFromDto(updateDTO, organisation);

        // Handle address updates
        if (updateDTO.getRegisteredAddress() != null) {
            if (organisation.getRegisteredAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getRegisteredAddress(), organisation.getRegisteredAddress());
            } else {
                Address registeredAddress = addressMapper.toAddress(updateDTO.getRegisteredAddress());
                registeredAddress.setAddressType(AddressType.REGISTERED);
                organisation.setRegisteredAddress(registeredAddress);
            }
        }

        if (updateDTO.getBusinessAddress() != null) {
            if (organisation.getBusinessAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getBusinessAddress(), organisation.getBusinessAddress());
            } else {
                Address businessAddress = addressMapper.toAddress(updateDTO.getBusinessAddress());
                businessAddress.setAddressType(AddressType.BUSINESS);
                organisation.setBusinessAddress(businessAddress);
            }
        }

        if (updateDTO.getCorrespondenceAddress() != null) {
            if (organisation.getCorrespondenceAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getCorrespondenceAddress(), organisation.getCorrespondenceAddress());
            } else {
                Address correspondenceAddress = addressMapper.toAddress(updateDTO.getCorrespondenceAddress());
                correspondenceAddress.setAddressType(AddressType.CORRESPONDENCE);
                organisation.setCorrespondenceAddress(correspondenceAddress);
            }
        }

        Organisation saved = organisationRepository.save(organisation);
        log.info("Updated organisation ID: {}", saved.getId());
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Update organisation status.
     */
    @Transactional
    public OrganisationDTO updateOrganisationStatus(Long id, String statusStr, String reason) {
        log.info("Updating organisation status - ID: {}, Status: {}", id, statusStr);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        OrganisationStatus newStatus = OrganisationStatus.valueOf(statusStr.toUpperCase());
        organisation.setStatus(newStatus);
        organisation.setReasonDescription(reason);

        Organisation saved = organisationRepository.save(organisation);
        log.info("Updated organisation status to: {}", newStatus);
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Delete an organisation.
     */
    @Transactional
    public void deleteOrganisation(Long id) {
        log.info("Deleting organisation ID: {}", id);
        
        if (!organisationRepository.existsById(id)) {
            throw new RuntimeException("Organisation not found with ID: " + id);
        }
        
        organisationRepository.deleteById(id);
        log.info("Deleted organisation ID: {}", id);
    }

    /**
     * Check if organisation exists by registration number.
     */
    @Transactional(readOnly = true)
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return organisationRepository.existsByRegistrationNumber(registrationNumber);
    }

    /**
     * Submit organisation for admin review.
     * Changes status from PENDING to UNDER_REVIEW.
     * Also updates all KYC documents to UNDER_REVIEW status.
     */
    @Transactional
    public OrganisationDTO submitForReview(Long id) {
        log.info("Submitting organisation for review - ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        if (organisation.getStatus() != OrganisationStatus.PENDING && 
            organisation.getStatus() != OrganisationStatus.REQUIRES_RESUBMISSION) {
            throw new RuntimeException("Only organisations with status PENDING or REQUIRES_RESUBMISSION can be submitted for review");
        }

        // Update all KYC documents to UNDER_REVIEW
        List<KycDocument> kycDocuments = kycDocumentRepository.findByOrganisationId(id);
        for (KycDocument doc : kycDocuments) {
            doc.setStatus(DocumentStatus.UNDER_REVIEW);
        }

        organisation.setStatus(OrganisationStatus.UNDER_REVIEW);
        organisation.setReasonDescription(null); // Clear any previous rejection reason

        Organisation saved = organisationRepository.save(organisation);
        log.info("Organisation submitted for review - ID: {}, {} documents updated", saved.getId(), kycDocuments.size());
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Approve an organisation (Admin only).
     * Verifies all KYC documents and clears rejection reasons.
     */
    @Transactional
    public OrganisationDTO approveOrganisation(Long id) {
        log.info("Approving organisation - ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        if (organisation.getStatus() != OrganisationStatus.UNDER_REVIEW) {
            throw new RuntimeException("Only organisations with status UNDER_REVIEW can be approved");
        }

        // Verify all KYC documents and clear rejection reasons
        List<KycDocument> kycDocuments = kycDocumentRepository.findByOrganisationId(id);
        for (KycDocument doc : kycDocuments) {
            doc.setStatus(DocumentStatus.VERIFIED);
            doc.setReasonDescription(null); // Clear any rejection feedback
        }

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setReasonDescription(null); // Clear organisation-level summary

        Organisation saved = organisationRepository.save(organisation);
        log.info("Organisation approved - ID: {}, {} documents verified", saved.getId(), kycDocuments.size());
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Reject an organisation (Admin only) with per-document feedback.
     * Rejects specific documents with detailed feedback and verifies the rest.
     */
    @Transactional
    public OrganisationDTO rejectOrganisation(Long id, OrganisationRejectionDTO rejectionDTO) {
        log.info("Rejecting organisation - ID: {}, {} documents to reject", id, rejectionDTO.getDocumentRejections().size());
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        if (organisation.getStatus() != OrganisationStatus.UNDER_REVIEW) {
            throw new RuntimeException("Only organisations with status UNDER_REVIEW can be rejected");
        }

        // Get all KYC documents
        List<KycDocument> kycDocuments = kycDocumentRepository.findByOrganisationId(id);
        
        if (kycDocuments.isEmpty()) {
            throw new RuntimeException("Organisation has no KYC documents to reject");
        }

        // Process document rejections
        List<Long> rejectedDocIds = rejectionDTO.getDocumentRejections().stream()
                .map(OrganisationRejectionDTO.DocumentRejection::getDocumentId)
                .collect(Collectors.toList());

        int rejectedCount = 0;
        int verifiedCount = 0;

        for (KycDocument doc : kycDocuments) {
            Optional<OrganisationRejectionDTO.DocumentRejection> rejection = rejectionDTO.getDocumentRejections().stream()
                    .filter(r -> r.getDocumentId().equals(doc.getId()))
                    .findFirst();

            if (rejection.isPresent()) {
                // Reject this document with specific reason
                doc.setStatus(DocumentStatus.REJECTED);
                doc.setReasonDescription(rejection.get().getRejectionReason());
                rejectedCount++;
                log.debug("Rejecting document ID: {} - Reason: {}", doc.getId(), rejection.get().getRejectionReason());
            } else {
                // Approve documents that weren't rejected
                doc.setStatus(DocumentStatus.VERIFIED);
                doc.setReasonDescription(null);
                verifiedCount++;
                log.debug("Verifying document ID: {}", doc.getId());
            }
        }

        // Auto-generate organisation-level summary
        String summary = String.format("%d of %d documents rejected", rejectedCount, kycDocuments.size());
        organisation.setStatus(OrganisationStatus.REQUIRES_RESUBMISSION);
        organisation.setReasonDescription(summary);

        Organisation saved = organisationRepository.save(organisation);
        log.info("Organisation rejected - ID: {}, Summary: {}, Rejected: {}, Verified: {}", 
                 saved.getId(), summary, rejectedCount, verifiedCount);
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Build paginated response from Page object.
     */
    private PagedResponse<OrganisationDTO> buildPagedResponse(Page<Organisation> page) {
        List<OrganisationDTO> content = page.getContent().stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());

        return PagedResponse.<OrganisationDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
