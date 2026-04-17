package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.KycDocumentService;
import com.fincore.usermgmt.service.OrganisationService;
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
 * REST Controller for Organisation management endpoints.
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organisation Management", description = "APIs for managing organisations including creation, updates, status changes, and searching")
@SecurityRequirement(name = "bearerAuth")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final KycDocumentService kycDocumentService;

    /**
     * Create a new organisation.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new organisation",
        description = "Creates a new organisation with business details including legal name, registration number, and owner information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Organisation created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or duplicate registration number",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> createOrganisation(
            @Parameter(description = "Organisation creation data", required = true)
            @Valid @RequestBody OrganisationCreateDTO createDTO) {
        log.info("REST request to create organisation: {}", createDTO.getLegalName());
        OrganisationDTO created = organisationService.createOrganisation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get organisation by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get organisation by ID",
        description = "Retrieves a specific organisation by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved organisation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> getOrganisationById(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("REST request to get organisation by ID: {}", id);
        return organisationService.getOrganisationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all organisations with pagination.
     */
    @GetMapping
    @Operation(
        summary = "Get all organisations (paginated)",
        description = "Retrieves all organisations with pagination and sorting support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved organisations",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<OrganisationDTO>> getAllOrganisations(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "legalName")
            @RequestParam(defaultValue = "legalName") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all organisations - page: {}, size: {}", page, size);
        PagedResponse<OrganisationDTO> response = organisationService.getAllOrganisations(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Search organisations with filters.
     */
    @PostMapping("/search")
    @Operation(
        summary = "Search organisations",
        description = "Searches organisations using various filters such as name, status, owner, and date ranges with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<OrganisationDTO>> searchOrganisations(
            @Parameter(description = "Organisation search criteria", required = true)
            @RequestBody OrganisationSearchDTO searchDTO) {
        log.info("REST request to search organisations");
        PagedResponse<OrganisationDTO> response = organisationService.searchOrganisations(searchDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organisations by owner.
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(
        summary = "Get organisations by owner",
        description = "Retrieves all organisations owned by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved organisations",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<OrganisationDTO>> getOrganisationsByOwner(
            @Parameter(description = "Owner user ID", required = true, example = "123")
            @PathVariable Long ownerId) {
        log.info("REST request to get organisations for owner: {}", ownerId);
        List<OrganisationDTO> organisations = organisationService.getOrganisationsByOwner(ownerId);
        return ResponseEntity.ok(organisations);
    }

    /**
     * Get organisations by status.
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get organisations by status",
        description = "Retrieves all organisations with a specific status (PENDING, ACTIVE, SUSPENDED, REJECTED)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved organisations",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<OrganisationDTO>> getOrganisationsByStatus(
            @Parameter(description = "Organisation status", required = true, example = "ACTIVE")
            @PathVariable String status) {
        log.info("REST request to get organisations by status: {}", status);
        try {
            List<OrganisationDTO> organisations = organisationService.getOrganisationsByStatus(status);
            return ResponseEntity.ok(organisations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an organisation.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an organisation",
        description = "Updates an existing organisation's information including legal name, contact details, and business information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> updateOrganisation(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Organisation update data", required = true)
            @Valid @RequestBody OrganisationUpdateDTO updateDTO) {
        log.info("REST request to update organisation ID: {}", id);
        try {
            OrganisationDTO updated = organisationService.updateOrganisation(id, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Update organisation status.
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update organisation status",
        description = "Updates the status of an organisation (PENDING, ACTIVE, SUSPENDED, REJECTED) with an optional reason"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> updateOrganisationStatus(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status", required = true, example = "ACTIVE")
            @RequestParam String status,
            @Parameter(description = "Reason for status change", example = "Verification completed")
            @RequestParam(required = false) String reason) {
        log.info("REST request to update organisation status - ID: {}, Status: {}", id, status);
        try {
            OrganisationDTO updated = organisationService.updateOrganisationStatus(id, status, reason);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Delete an organisation.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an organisation",
        description = "Permanently deletes an organisation from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Organisation deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteOrganisation(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("REST request to delete organisation ID: {}", id);
        try {
            organisationService.deleteOrganisation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Check if registration number exists.
     */
    @GetMapping("/exists/registration/{registrationNumber}")
    @Operation(
        summary = "Check if registration number exists",
        description = "Checks whether an organisation with the given registration number already exists in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked registration number",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Boolean> checkRegistrationNumberExists(
            @Parameter(description = "Registration number to check", required = true, example = "REG123456")
            @PathVariable String registrationNumber) {
        log.info("REST request to check registration number: {}", registrationNumber);
        boolean exists = organisationService.existsByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(exists);
    }

    /**
     * Submit organization for admin review.
     */
    @PutMapping("/{id}/submit")
    @Operation(
        summary = "Submit organisation for review",
        description = "Submits an organisation for admin review by changing status from PENDING to UNDER_REVIEW"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation submitted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> submitForReview(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("REST request to submit organisation for review - ID: {}", id);
        try {
            OrganisationDTO submitted = organisationService.submitForReview(id);
            return ResponseEntity.ok(submitted);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Approve organization (Admin only).
     */
    @PutMapping("/{id}/approve")
    @Operation(
        summary = "Approve organisation",
        description = "Approves an organisation by changing status from UNDER_REVIEW to ACTIVE (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation approved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> approveOrganisation(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("REST request to approve organisation - ID: {}", id);
        try {
            OrganisationDTO approved = organisationService.approveOrganisation(id);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Reject organization (Admin only).
     */
    @PutMapping("/{id}/reject")
    @Operation(
        summary = "Reject organisation",
        description = "Rejects specific documents with individual feedback and changes org status from UNDER_REVIEW to REQUIRES_RESUBMISSION (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation rejected successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganisationDTO> rejectOrganisation(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Document rejections with reasons", required = true)
            @Valid @RequestBody OrganisationRejectionDTO rejectionDTO) {
        log.info("REST request to reject organisation - ID: {}", id);
        try {
            OrganisationDTO rejected = organisationService.rejectOrganisation(id, rejectionDTO);
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Get KYC documents for an organisation.
     */
    @GetMapping("/{id}/kyc-documents")
    @Operation(
        summary = "Get KYC documents for organisation",
        description = "Retrieves all KYC documents for a specific organisation including their status and rejection feedback"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved KYC documents",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycDocumentDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organisation not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycDocumentDTO>> getKycDocuments(
            @Parameter(description = "Organisation ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("REST request to get KYC documents for organisation - ID: {}", id);
        List<KycDocumentDTO> documents = kycDocumentService.getDocumentsByOrganisation(id);
        return ResponseEntity.ok(documents);
    }
}
