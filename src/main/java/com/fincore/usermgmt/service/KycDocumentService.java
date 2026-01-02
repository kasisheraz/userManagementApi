package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.KycDocumentCreateDTO;
import com.fincore.usermgmt.dto.KycDocumentDTO;
import com.fincore.usermgmt.dto.KycDocumentUpdateDTO;
import com.fincore.usermgmt.dto.PagedResponse;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.KycDocumentMapper;
import com.fincore.usermgmt.repository.KycDocumentRepository;
import com.fincore.usermgmt.repository.OrganisationRepository;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for KYC Document management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KycDocumentService {

    private final KycDocumentRepository kycDocumentRepository;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final KycDocumentMapper kycDocumentMapper;

    /**
     * Upload a new KYC document.
     */
    @Transactional
    public KycDocumentDTO createDocument(KycDocumentCreateDTO createDTO) {
        log.info("Creating new KYC document for organisation: {}", createDTO.getOrganisationId());

        Organisation organisation = organisationRepository.findById(createDTO.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + createDTO.getOrganisationId()));

        KycDocument document = kycDocumentMapper.toKycDocument(createDTO);
        document.setOrganisation(organisation);

        KycDocument saved = kycDocumentRepository.save(document);
        log.info("Created KYC document with ID: {}", saved.getId());

        return kycDocumentMapper.toKycDocumentDTO(saved);
    }

    /**
     * Get document by ID.
     */
    @Transactional(readOnly = true)
    public Optional<KycDocumentDTO> getDocumentById(Long id) {
        log.debug("Fetching KYC document by ID: {}", id);
        return kycDocumentRepository.findById(id)
                .map(kycDocumentMapper::toKycDocumentDTO);
    }

    /**
     * Get all documents for an organisation.
     */
    @Transactional(readOnly = true)
    public List<KycDocumentDTO> getDocumentsByOrganisation(Long organisationId) {
        log.debug("Fetching KYC documents for organisation: {}", organisationId);
        return kycDocumentRepository.findByOrganisationId(organisationId).stream()
                .map(kycDocumentMapper::toKycDocumentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for an organisation with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<KycDocumentDTO> getDocumentsByOrganisationPaged(Long organisationId, int page, int size) {
        log.debug("Fetching KYC documents for organisation: {} - page: {}, size: {}", organisationId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDatetime").descending());
        Page<KycDocument> documentPage = kycDocumentRepository.findByOrganisationId(organisationId, pageable);

        return buildPagedResponse(documentPage);
    }

    /**
     * Get documents by status.
     */
    @Transactional(readOnly = true)
    public List<KycDocumentDTO> getDocumentsByStatus(String statusStr) {
        log.debug("Fetching KYC documents by status: {}", statusStr);
        DocumentStatus status = DocumentStatus.valueOf(statusStr.toUpperCase());
        return kycDocumentRepository.findByStatus(status).stream()
                .map(kycDocumentMapper::toKycDocumentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents pending verification.
     */
    @Transactional(readOnly = true)
    public List<KycDocumentDTO> getPendingDocuments() {
        log.debug("Fetching pending KYC documents");
        return kycDocumentRepository.findPendingVerification().stream()
                .map(kycDocumentMapper::toKycDocumentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by organisation and type.
     */
    @Transactional(readOnly = true)
    public List<KycDocumentDTO> getDocumentsByOrganisationAndType(Long organisationId, String documentTypeStr) {
        log.debug("Fetching KYC documents for organisation: {} with type: {}", organisationId, documentTypeStr);
        DocumentType documentType = DocumentType.valueOf(documentTypeStr.toUpperCase());
        return kycDocumentRepository.findByOrganisationIdAndDocumentType(organisationId, documentType).stream()
                .map(kycDocumentMapper::toKycDocumentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update a document.
     */
    @Transactional
    public KycDocumentDTO updateDocument(Long id, KycDocumentUpdateDTO updateDTO) {
        log.info("Updating KYC document ID: {}", id);

        KycDocument document = kycDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC Document not found with ID: " + id));

        kycDocumentMapper.updateKycDocumentFromDto(updateDTO, document);

        KycDocument saved = kycDocumentRepository.save(document);
        log.info("Updated KYC document ID: {}", saved.getId());

        return kycDocumentMapper.toKycDocumentDTO(saved);
    }

    /**
     * Verify a document.
     */
    @Transactional
    public KycDocumentDTO verifyDocument(Long id, Long verifierId, String status, String reason) {
        log.info("Verifying KYC document ID: {} by user: {}", id, verifierId);

        KycDocument document = kycDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC Document not found with ID: " + id));

        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new RuntimeException("Verifier user not found with ID: " + verifierId));

        DocumentStatus documentStatus = DocumentStatus.valueOf(status.toUpperCase());
        document.setStatus(documentStatus);
        document.setReasonDescription(reason);
        document.setVerifiedBy(verifier);

        KycDocument saved = kycDocumentRepository.save(document);
        log.info("Verified KYC document ID: {} with status: {}", saved.getId(), documentStatus);

        return kycDocumentMapper.toKycDocumentDTO(saved);
    }

    /**
     * Delete a document.
     */
    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting KYC document ID: {}", id);

        if (!kycDocumentRepository.existsById(id)) {
            throw new RuntimeException("KYC Document not found with ID: " + id);
        }

        kycDocumentRepository.deleteById(id);
        log.info("Deleted KYC document ID: {}", id);
    }

    /**
     * Count verified documents for an organisation.
     */
    @Transactional(readOnly = true)
    public long countVerifiedDocuments(Long organisationId) {
        return kycDocumentRepository.countVerifiedDocumentsByOrganisation(organisationId);
    }

    /**
     * Build paginated response from Page object.
     */
    private PagedResponse<KycDocumentDTO> buildPagedResponse(Page<KycDocument> page) {
        List<KycDocumentDTO> content = page.getContent().stream()
                .map(kycDocumentMapper::toKycDocumentDTO)
                .collect(Collectors.toList());

        return PagedResponse.<KycDocumentDTO>builder()
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
