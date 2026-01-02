package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.KycDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for KYC Document management endpoints.
 */
@RestController
@RequestMapping("/api/kyc-documents")
@RequiredArgsConstructor
@Slf4j
public class KycDocumentController {

    private final KycDocumentService kycDocumentService;

    /**
     * Upload a new KYC document.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<KycDocumentDTO> createDocument(
            @Valid @RequestBody KycDocumentCreateDTO createDTO) {
        log.info("REST request to create KYC document for organisation: {}", createDTO.getOrganisationId());
        KycDocumentDTO created = kycDocumentService.createDocument(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get document by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KycDocumentDTO> getDocumentById(@PathVariable Long id) {
        log.info("REST request to get KYC document by ID: {}", id);
        return kycDocumentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all documents for an organisation.
     */
    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByOrganisation(@PathVariable Long organisationId) {
        log.info("REST request to get KYC documents for organisation: {}", organisationId);
        List<KycDocumentDTO> documents = kycDocumentService.getDocumentsByOrganisation(organisationId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents for an organisation with pagination.
     */
    @GetMapping("/organisation/{organisationId}/paged")
    public ResponseEntity<PagedResponse<KycDocumentDTO>> getDocumentsByOrganisationPaged(
            @PathVariable Long organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get KYC documents for organisation: {} - page: {}, size: {}", organisationId, page, size);
        PagedResponse<KycDocumentDTO> response = kycDocumentService.getDocumentsByOrganisationPaged(organisationId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get documents by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByStatus(@PathVariable String status) {
        log.info("REST request to get KYC documents by status: {}", status);
        try {
            List<KycDocumentDTO> documents = kycDocumentService.getDocumentsByStatus(status);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get pending documents for verification.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<KycDocumentDTO>> getPendingDocuments() {
        log.info("REST request to get pending KYC documents");
        List<KycDocumentDTO> documents = kycDocumentService.getPendingDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by organisation and type.
     */
    @GetMapping("/organisation/{organisationId}/type/{documentType}")
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByOrganisationAndType(
            @PathVariable Long organisationId,
            @PathVariable String documentType) {
        log.info("REST request to get KYC documents for organisation: {} with type: {}", organisationId, documentType);
        try {
            List<KycDocumentDTO> documents = kycDocumentService.getDocumentsByOrganisationAndType(organisationId, documentType);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update a document.
     */
    @PutMapping("/{id}")
    public ResponseEntity<KycDocumentDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody KycDocumentUpdateDTO updateDTO) {
        log.info("REST request to update KYC document ID: {}", id);
        try {
            KycDocumentDTO updated = kycDocumentService.updateDocument(id, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Verify a document.
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<KycDocumentDTO> verifyDocument(
            @PathVariable Long id,
            @RequestParam Long verifierId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        log.info("REST request to verify KYC document ID: {} by user: {}", id, verifierId);
        try {
            KycDocumentDTO verified = kycDocumentService.verifyDocument(id, verifierId, status, reason);
            return ResponseEntity.ok(verified);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Delete a document.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("REST request to delete KYC document ID: {}", id);
        try {
            kycDocumentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Get count of verified documents for an organisation.
     */
    @GetMapping("/organisation/{organisationId}/verified/count")
    public ResponseEntity<Long> countVerifiedDocuments(@PathVariable Long organisationId) {
        log.info("REST request to count verified documents for organisation: {}", organisationId);
        long count = kycDocumentService.countVerifiedDocuments(organisationId);
        return ResponseEntity.ok(count);
    }
}
