package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.entity.enums.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for exposing all application enums.
 * Provides dynamic dropdown options for the UI.
 */
@Slf4j
@RestController
@RequestMapping("/api/enums")
@Tag(name = "Enums", description = "Endpoints for retrieving enum values for dropdowns")
@SecurityRequirement(name = "bearerAuth")
public class EnumsController {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumOption {
        private String value;
        private String label;
        private String description;
    }

    /**
     * Get all enums in a single call
     */
    @GetMapping
    @Operation(summary = "Get all enums", description = "Retrieves all enum values available in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all enums"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public Map<String, List<EnumOption>> getAllEnums() {
        log.info("Fetching all enum values");
        
        return Map.ofEntries(
            Map.entry("userStatus", getUserStatusOptions()),
            Map.entry("organizationStatus", getOrganizationStatusOptions()),
            Map.entry("organizationType", getOrganizationTypeOptions()),
            Map.entry("documentType", getDocumentTypeOptions()),
            Map.entry("documentStatus", getDocumentStatusOptions()),
            Map.entry("addressType", getAddressTypeOptions()),
            Map.entry("verificationStatus", getVerificationStatusOptions()),
            Map.entry("verificationLevel", getVerificationLevelOptions()),
            Map.entry("screeningType", getScreeningTypeOptions()),
            Map.entry("riskLevel", getRiskLevelOptions()),
            Map.entry("questionCategory", getQuestionCategoryOptions())
        );
    }

    /**
     * Get user status options
     */
    @GetMapping("/user-status")
    @Operation(summary = "Get user status options")
    public List<EnumOption> getUserStatusOptions() {
        return Arrays.stream(UserStatus.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), null))
            .collect(Collectors.toList());
    }

    /**
     * Get organization status options
     */
    @GetMapping("/organization-status")
    @Operation(summary = "Get organization status options")
    public List<EnumOption> getOrganizationStatusOptions() {
        return Arrays.stream(OrganisationStatus.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), null))
            .collect(Collectors.toList());
    }

    /**
     * Get organization type options
     */
    @GetMapping("/organization-type")
    @Operation(summary = "Get organization type options")
    public List<EnumOption> getOrganizationTypeOptions() {
        return Arrays.stream(OrganisationType.values())
            .map(e -> new EnumOption(e.name(), e.getDescription(), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get document type options
     */
    @GetMapping("/document-type")
    @Operation(summary = "Get document type options")
    public List<EnumOption> getDocumentTypeOptions() {
        return Arrays.stream(DocumentType.values())
            .map(e -> new EnumOption(e.name(), e.getDescription(), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get document status options
     */
    @GetMapping("/document-status")
    @Operation(summary = "Get document status options")
    public List<EnumOption> getDocumentStatusOptions() {
        return Arrays.stream(DocumentStatus.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), null))
            .collect(Collectors.toList());
    }

    /**
     * Get address type options
     */
    @GetMapping("/address-type")
    @Operation(summary = "Get address type options")
    public List<EnumOption> getAddressTypeOptions() {
        return Arrays.stream(AddressType.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), null))
            .collect(Collectors.toList());
    }

    /**
     * Get verification status options
     */
    @GetMapping("/verification-status")
    @Operation(summary = "Get verification status options")
    public List<EnumOption> getVerificationStatusOptions() {
        return Arrays.stream(VerificationStatus.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get verification level options
     */
    @GetMapping("/verification-level")
    @Operation(summary = "Get verification level options")
    public List<EnumOption> getVerificationLevelOptions() {
        return Arrays.stream(VerificationLevel.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get screening type options
     */
    @GetMapping("/screening-type")
    @Operation(summary = "Get screening type options")
    public List<EnumOption> getScreeningTypeOptions() {
        return Arrays.stream(ScreeningType.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get risk level options
     */
    @GetMapping("/risk-level")
    @Operation(summary = "Get risk level options")
    public List<EnumOption> getRiskLevelOptions() {
        return Arrays.stream(RiskLevel.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), e.getDescription()))
            .collect(Collectors.toList());
    }

    /**
     * Get question category options
     */
    @GetMapping("/question-category")
    @Operation(summary = "Get question category options")
    public List<EnumOption> getQuestionCategoryOptions() {
        return Arrays.stream(QuestionCategory.values())
            .map(e -> new EnumOption(e.name(), formatEnumName(e.name()), null))
            .collect(Collectors.toList());
    }

    /**
     * Format enum name for display
     * Converts UPPERCASE_WITH_UNDERSCORES to Title Case
     */
    private String formatEnumName(String enumName) {
        return Arrays.stream(enumName.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
