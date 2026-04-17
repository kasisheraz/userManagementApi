package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for rejecting an organisation with specific document feedback.
 * Admin can specify which documents are rejected and provide individual reasons.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisationRejectionDTO {

    @NotEmpty(message = "At least one document must be rejected")
    private List<DocumentRejection> documentRejections;

    /**
     * Individual document rejection with reason.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentRejection {
        
        @NotNull(message = "Document ID is required")
        private Long documentId;
        
        @NotNull(message = "Rejection reason is required")
        private String rejectionReason;
    }
}
