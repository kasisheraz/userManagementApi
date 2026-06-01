package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.BeneficiaryRejectionDTO;
import com.fincore.usermgmt.dto.BeneficiaryRequestDTO;
import com.fincore.usermgmt.dto.BeneficiaryResponseDTO;
import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.entity.BeneficiaryStatus;
import com.fincore.usermgmt.service.BeneficiaryService;
import com.fincore.usermgmt.util.SecurityUtil;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Beneficiary management endpoints.
 * 
 * <p>Provides APIs for:</p>
 * <ul>
 *   <li>Creating and managing beneficiaries (Business Users)</li>
 *   <li>Searching and filtering beneficiaries</li>
 *   <li>Submitting beneficiaries for approval</li>
 *   <li>Admin approval/rejection workflow</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2.2.0
 */
@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Beneficiary Management", description = "APIs for managing beneficiaries including creation, updates, approval workflow, and searching")
@SecurityRequirement(name = "bearerAuth")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final SecurityUtil securityUtil;

    // ========================================
    // Business User Endpoints (Own Beneficiaries)
    // ========================================

    /**
     * Create a new beneficiary.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new beneficiary",
        description = "Creates a new beneficiary for the current user. Maximum 20 beneficiaries per user. " +
                      "If Counter Over Counter is enabled, Collector Contact Number is required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Beneficiary created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or 20-beneficiary limit reached",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> createBeneficiary(
            @Parameter(description = "Beneficiary creation data", required = true)
            @Valid @RequestBody BeneficiaryRequestDTO request) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to create beneficiary by user {}: {}", userId, request.getBeneficiaryName());
        
        BeneficiaryResponseDTO created = beneficiaryService.createBeneficiary(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing beneficiary.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing beneficiary",
        description = "Updates beneficiary details. Only allowed if beneficiary status is PENDING."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Cannot update beneficiary - status is not PENDING",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found or access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> updateBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Beneficiary update data", required = true)
            @Valid @RequestBody BeneficiaryRequestDTO request) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to update beneficiary {} by user {}", id, userId);
        
        BeneficiaryResponseDTO updated = beneficiaryService.updateBeneficiary(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get a single beneficiary by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get beneficiary by ID",
        description = "Retrieves a specific beneficiary. Users can only access their own beneficiaries."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved beneficiary",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found or access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> getBeneficiaryById(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to get beneficiary {} by user {}", id, userId);
        
        BeneficiaryResponseDTO beneficiary = beneficiaryService.getBeneficiaryById(userId, id);
        return ResponseEntity.ok(beneficiary);
    }

    /**
     * Get all beneficiaries for the current user.
     */
    @GetMapping
    @Operation(
        summary = "Get all beneficiaries",
        description = "Retrieves all beneficiaries for the current user. Supports optional filtering by status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved beneficiaries",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> getAllBeneficiaries(
            @Parameter(description = "Filter by status (optional)", example = "ACTIVE")
            @RequestParam(required = false) BeneficiaryStatus status) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to get all beneficiaries by user {} with status filter: {}", userId, status);
        
        List<BeneficiaryResponseDTO> beneficiaries;
        if (status != null) {
            beneficiaries = beneficiaryService.getBeneficiariesByStatus(userId, status);
        } else {
            beneficiaries = beneficiaryService.getAllBeneficiariesForUser(userId);
        }
        
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Search beneficiaries by name.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search beneficiaries",
        description = "Searches beneficiaries by name (beneficiary name, business name, or nick name). Case-insensitive partial match."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> searchBeneficiaries(
            @Parameter(description = "Search term", required = true, example = "HSBC")
            @RequestParam String query) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to search beneficiaries by user {} with query: {}", userId, query);
        
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.searchBeneficiaries(userId, query);
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Filter beneficiaries by country.
     */
    @GetMapping("/by-country/{country}")
    @Operation(
        summary = "Filter beneficiaries by country",
        description = "Retrieves all beneficiaries in a specific country for the current user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved beneficiaries",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> getBeneficiariesByCountry(
            @Parameter(description = "Country code or name", required = true, example = "United Kingdom")
            @PathVariable String country) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to get beneficiaries in country {} by user {}", country, userId);
        
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.getBeneficiariesByCountry(userId, country);
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Get all C2C beneficiaries.
     */
    @GetMapping("/c2c")
    @Operation(
        summary = "Get Counter Over Counter beneficiaries",
        description = "Retrieves all beneficiaries with C2C collection enabled for the current user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved C2C beneficiaries",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> getC2CBeneficiaries() {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to get C2C beneficiaries by user {}", userId);
        
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.getC2CBeneficiaries(userId);
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Submit beneficiary for review.
     */
    @PostMapping("/{id}/submit")
    @Operation(
        summary = "Submit beneficiary for review",
        description = "Submits a beneficiary for admin review. Changes status from PENDING to UNDER_REVIEW. " +
                      "All required documents must be uploaded before submission."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary submitted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Required documents missing or invalid status",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found or access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> submitForReview(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to submit beneficiary {} for review by user {}", id, userId);
        
        BeneficiaryResponseDTO submitted = beneficiaryService.submitForReview(userId, id);
        return ResponseEntity.ok(submitted);
    }

    /**
     * Delete a beneficiary.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete a beneficiary",
        description = "Deletes a beneficiary. Only allowed if beneficiary status is PENDING."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Beneficiary deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Cannot delete beneficiary - status is not PENDING",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found or access denied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to delete beneficiary {} by user {}", id, userId);
        
        beneficiaryService.deleteBeneficiary(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get beneficiary count for current user.
     */
    @GetMapping("/count")
    @Operation(
        summary = "Get beneficiary count",
        description = "Returns the count of beneficiaries for the current user. Used to display '15 out of 20 beneficiaries' message."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved count")
    })
    public ResponseEntity<Map<String, Object>> getBeneficiaryCount() {
        
        Long userId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request to get beneficiary count by user {}", userId);
        
        long count = beneficiaryService.getBeneficiaryCount(userId);
        long remaining = Math.max(0, 20 - count);
        
        Map<String, Object> response = Map.of(
            "count", count,
            "limit", 20,
            "remaining", remaining,
            "canCreateMore", remaining > 0
        );
        
        return ResponseEntity.ok(response);
    }

    // ========================================
    // Admin Endpoints (All Beneficiaries)
    // ========================================

    /**
     * Get all beneficiaries (admin view).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Get all beneficiaries (Admin)",
        description = "Retrieves all beneficiaries across all users. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all beneficiaries",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> getAllBeneficiariesAdmin() {
        log.info("REST request to get all beneficiaries (admin view)");
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.getAllBeneficiaries();
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Get pending approvals (admin queue).
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Get pending approvals (Admin)",
        description = "Retrieves all beneficiaries awaiting admin review (UNDER_REVIEW status). Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pending approvals",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> getPendingApprovals() {
        log.info("REST request to get pending approval queue");
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.getPendingApprovals();
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Approve a beneficiary (admin action).
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Approve a beneficiary (Admin)",
        description = "Approves a beneficiary, changing status from UNDER_REVIEW to ACTIVE. Sends email notification to owner. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary approved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status - cannot approve",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> approveBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Long adminUserId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request by admin {} to approve beneficiary {}", adminUserId, id);
        
        BeneficiaryResponseDTO approved = beneficiaryService.approveBeneficiary(adminUserId, id);
        return ResponseEntity.ok(approved);
    }

    /**
     * Reject a beneficiary (admin action).
     */
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Reject a beneficiary (Admin)",
        description = "Rejects a beneficiary, changing status from UNDER_REVIEW to REJECTED. Records rejection reason. Sends email notification to owner. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary rejected successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status or missing reason",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> rejectBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Rejection details", required = true)
            @Valid @RequestBody BeneficiaryRejectionDTO rejectionDTO) {
        
        Long adminUserId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request by admin {} to reject beneficiary {} with reason: {}", 
                 adminUserId, id, rejectionDTO.getReason());
        
        BeneficiaryResponseDTO rejected = beneficiaryService.rejectBeneficiary(
                adminUserId, id, rejectionDTO.getReason());
        return ResponseEntity.ok(rejected);
    }

    /**
     * Suspend an active beneficiary (admin action).
     */
    @PostMapping("/admin/{id}/suspend")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Suspend a beneficiary (Admin)",
        description = "Suspends an active beneficiary, changing status from ACTIVE to SUSPENDED. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary suspended successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status or missing reason",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> suspendBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Suspension details", required = true)
            @Valid @RequestBody BeneficiaryRejectionDTO suspensionDTO) {
        
        Long adminUserId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request by admin {} to suspend beneficiary {} with reason: {}", 
                 adminUserId, id, suspensionDTO.getReason());
        
        BeneficiaryResponseDTO suspended = beneficiaryService.suspendBeneficiary(
                adminUserId, id, suspensionDTO.getReason());
        return ResponseEntity.ok(suspended);
    }

    /**
     * Reactivate a suspended beneficiary (admin action).
     */
    @PostMapping("/admin/{id}/reactivate")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Reactivate a beneficiary (Admin)",
        description = "Reactivates a suspended beneficiary, changing status from SUSPENDED to ACTIVE. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiary reactivated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status - cannot reactivate",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Beneficiary not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BeneficiaryResponseDTO> reactivateBeneficiary(
            @Parameter(description = "Beneficiary ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Long adminUserId = securityUtil.getCurrentUser().orElseThrow(() -> new RuntimeException("User not authenticated")).getId();
        log.info("REST request by admin {} to reactivate beneficiary {}", adminUserId, id);
        
        BeneficiaryResponseDTO reactivated = beneficiaryService.reactivateBeneficiary(adminUserId, id);
        return ResponseEntity.ok(reactivated);
    }

    /**
     * Search all beneficiaries (admin view).
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Search all beneficiaries (Admin)",
        description = "Searches beneficiaries across all users by name. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BeneficiaryResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BeneficiaryResponseDTO>> adminSearchBeneficiaries(
            @Parameter(description = "Search term", required = true, example = "HSBC")
            @RequestParam String query) {
        log.info("REST request to admin search beneficiaries with query: {}", query);
        List<BeneficiaryResponseDTO> beneficiaries = beneficiaryService.adminSearchBeneficiaries(query);
        return ResponseEntity.ok(beneficiaries);
    }

    /**
     * Get beneficiary statistics (admin dashboard).
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'COMPLIANCE_OFFICER')")
    @Operation(
        summary = "Get beneficiary statistics (Admin)",
        description = "Retrieves count of beneficiaries by status for admin dashboard. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<BeneficiaryStatus, Long>> getStatistics() {
        log.info("REST request to get beneficiary statistics");
        Map<BeneficiaryStatus, Long> stats = beneficiaryService.getStatusStatistics();
        return ResponseEntity.ok(stats);
    }
}
