package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.KycDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "KYC Document Management", description = "APIs for managing KYC documents for organisations including upload, verification, and status tracking")
@SecurityRequirement(name = "bearerAuth")
public class KycDocumentController {

    private final KycDocumentService kycDocumentService;

    /**
     * Upload a new KYC document.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Upload a new KYC document",
        description = "Uploads a new KYC document for an organisation with document type, file path, and metadata"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycDocumentDTO> createDocument(
            @Parameter(description = "KYC document creation data", required = true)
            @Valid @RequestBody KycDocumentCreateDTO createDTO) {
        log.info("REST request to create KYC document for organisation: {}", createDTO.getOrganisationId());
        KycDocumentDTO created = kycDocumentService.createDocument(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all KYC documents (for admin/overview purposes).
     * GET /api/kyc-documents
     */
    @GetMapping
    @Operation(
        summary = "Get all KYC documents",
        description = "Retrieves all KYC documents in the system (returns pending documents by default)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getAllDocuments() {
        log.info("REST request to get all KYC documents");
        List<KycDocumentDTO> documents = kycDocumentService.getPendingDocuments(); // Returns pending docs as default
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get KYC document by ID",
        description = "Retrieves a specific KYC document by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved document",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycDocumentDTO> getDocumentById(
        @Parameter(description = "Document ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("REST request to get KYC document by ID: {}", id);
        return kycDocumentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all documents for an organisation.
     */
    @GetMapping("/organisation/{organisationId}")
    @Operation(
        summary = "Get KYC documents by organisation",
        description = "Retrieves all KYC documents for a specific organisation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByOrganisation(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long organisationId) {
        log.info("REST request to get KYC documents for organisation: {}", organisationId);
        List<KycDocumentDTO> documents = kycDocumentService.getDocumentsByOrganisation(organisationId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents for an organisation with pagination.
     */
    @GetMapping("/organisation/{organisationId}/paged")
    @Operation(
        summary = "Get KYC documents by organisation (paginated)",
        description = "Retrieves KYC documents for a specific organisation with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<KycDocumentDTO>> getDocumentsByOrganisationPaged(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long organisationId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get KYC documents for organisation: {} - page: {}, size: {}", organisationId, page, size);
        PagedResponse<KycDocumentDTO> response = kycDocumentService.getDocumentsByOrganisationPaged(organisationId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get documents by status.
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get KYC documents by status",
        description = "Retrieves all KYC documents with a specific verification status (PENDING, APPROVED, REJECTED)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByStatus(
            @Parameter(description = "Document status", required = true, example = "PENDING")
            @PathVariable String status) {
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
    @Operation(
        summary = "Get pending KYC documents",
        description = "Retrieves all KYC documents that are pending verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pending documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getPendingDocuments() {
        log.info("REST request to get pending KYC documents");
        List<KycDocumentDTO> documents = kycDocumentService.getPendingDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by organisation and type.
     */
    @GetMapping("/organisation/{organisationId}/type/{documentType}")
    @Operation(
        summary = "Get KYC documents by organisation and type",
        description = "Retrieves KYC documents for a specific organisation filtered by document type (e.g., PASSPORT, DRIVERS_LICENSE)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid document type",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getDocumentsByOrganisationAndType(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long organisationId,
            @Parameter(description = "Document type", required = true, example = "PASSPORT")
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
    @Operation(
        summary = "Update a KYC document",
        description = "Updates an existing KYC document's metadata and information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycDocumentDTO> updateDocument(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Document update data", required = true)
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
    @Operation(
        summary = "Verify a KYC document",
        description = "Performs verification on a KYC document, updating its status to APPROVED or REJECTED with an optional reason"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document verified successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "404", description = "Document not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycDocumentDTO> verifyDocument(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Verifier user ID", required = true, example = "2")
            @RequestParam Long verifierId,
            @Parameter(description = "Verification status (APPROVED or REJECTED)", required = true, example = "APPROVED")
            @RequestParam String status,
            @Parameter(description = "Reason for rejection (optional)", example = "Document expired")
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
    @Operation(
        summary = "Delete a KYC document",
        description = "Permanently deletes a KYC document from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(
        summary = "Count verified documents for organisation",
        description = "Returns the total number of verified (approved) KYC documents for a specific organisation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved count",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Long> countVerifiedDocuments(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long organisationId) {
        log.info("REST request to count verified documents for organisation: {}", organisationId);
        long count = kycDocumentService.countVerifiedDocuments(organisationId);
        return ResponseEntity.ok(count);
    }
}
