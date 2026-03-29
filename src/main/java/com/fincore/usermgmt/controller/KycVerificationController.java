package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.dto.KycVerificationRequestDTO;
import com.fincore.usermgmt.dto.KycVerificationResponseDTO;
import com.fincore.usermgmt.dto.KycVerificationUpdateDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationLevel;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import com.fincore.usermgmt.service.KycVerificationService;
import com.fincore.usermgmt.mapper.KycAmlMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/kyc-verifications")
@RequiredArgsConstructor
@Tag(name = "KYC Verification Management", description = "APIs for managing customer KYC verification processes, including submission, review, and status tracking")
@SecurityRequirement(name = "bearerAuth")
public class KycVerificationController {

    private final KycVerificationService kycService;
    private final KycAmlMapper mapper;

    /**
     * Get all KYC verifications (for admin/overview purposes).
     * GET /api/kyc-verifications
     */
    @GetMapping
    @Operation(
        summary = "Get all KYC verifications",
        description = "Retrieves all KYC verifications in the system for administrative and overview purposes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of verifications",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycVerificationResponseDTO>> getAllVerifications() {
        log.info("Fetching all KYC verifications");
        List<CustomerKycVerification> verifications = kycService.getAllVerifications();
        List<KycVerificationResponseDTO> response = verifications.stream()
                .map(mapper::toKycVerificationResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Submit a new KYC verification
     * POST /api/kyc-verifications (primary endpoint)
     */
    @PostMapping
    @Operation(
        summary = "Create a new KYC verification",
        description = "Submits a new KYC verification request for a user with the specified verification level (BASIC, STANDARD, ENHANCED)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Verification created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data - userId or verificationLevel missing",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycVerificationResponseDTO> createVerification(
            @Parameter(description = "KYC verification request data", required = true)
            @RequestBody KycVerificationRequestDTO request) {

        log.info("Creating KYC verification for user: {}", request.getUserId());

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getVerificationLevel() == null) {
            throw new IllegalArgumentException("verificationLevel is required");
        }

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
     * Submit a new KYC verification (legacy endpoint)
     * POST /api/kyc-verifications/submit
     */
    @PostMapping("/submit")
    @Operation(
        summary = "Submit a new KYC verification (legacy)",
        description = "Legacy endpoint for submitting a new KYC verification request. Use POST /api/kyc-verifications instead"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Verification submitted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycVerificationResponseDTO> submitVerification(
            @Parameter(description = "KYC verification request data", required = true)
            @RequestBody KycVerificationRequestDTO request) {

        log.info("Submitting KYC verification for user: {} (legacy endpoint)", request.getUserId());
        return createVerification(request);
    }

    /**
     * Get KYC verification by ID
     * GET /api/v1/kyc-verification/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get KYC verification by ID",
        description = "Retrieves a specific KYC verification by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved verification",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Verification not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycVerificationResponseDTO> getVerification(
            @Parameter(description = "Verification ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Fetching verification: {}", id);

        CustomerKycVerification verification = kycService.getVerificationById(id);
        return ResponseEntity.ok(mapper.toKycVerificationResponseDTO(verification));
    }

    /**
     * Get user's current verification
     * GET /api/v1/kyc-verification/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user's KYC verification",
        description = "Retrieves the current KYC verification record for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved verification",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "No verification found for this user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycVerificationResponseDTO> getUserVerification(
            @Parameter(description = "User ID", required = true, example = "123")
            @PathVariable Long userId) {
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
    @Operation(
        summary = "Update KYC verification status",
        description = "Updates the status of a KYC verification (e.g., PENDING, APPROVED, REJECTED, EXPIRED) with review results and reviewer information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Verification not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<KycVerificationResponseDTO> updateVerificationStatus(
            @Parameter(description = "Verification ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Verification status update data", required = true)
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
    @Operation(
        summary = "Get expired KYC verifications",
        description = "Retrieves all KYC verifications that have expired and require renewal"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved expired verifications",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(
        summary = "Get KYC verifications by status",
        description = "Retrieves all KYC verifications with a specific status (PENDING, APPROVED, REJECTED, EXPIRED, IN_REVIEW)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved verifications",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = KycVerificationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<KycVerificationResponseDTO>> getVerificationsByStatus(
            @Parameter(description = "Verification status", required = true, example = "PENDING")
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
    @Operation(
        summary = "Count KYC verifications by status",
        description = "Returns the total number of KYC verifications with a specific status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved count",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Verification status", required = true, example = "APPROVED")
            @PathVariable String status) {
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
    @Operation(
        summary = "Check if user has approved verification",
        description = "Checks whether a user has an approved KYC verification on file"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked verification status",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Boolean> hasApprovedVerification(
            @Parameter(description = "User ID", required = true, example = "123")
            @PathVariable Long userId) {
        log.info("Checking approved verification for user: {}", userId);

        boolean hasApproved = kycService.hasApprovedVerification(userId);
        return ResponseEntity.ok(hasApproved);
    }

    /**
     * Delete verification
     * DELETE /api/v1/kyc-verification/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a KYC verification",
        description = "Permanently deletes a KYC verification from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Verification deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Verification not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteVerification(
            @Parameter(description = "Verification ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Deleting verification: {}", id);

        kycService.deleteVerification(id);
        return ResponseEntity.noContent().build();
    }
}
