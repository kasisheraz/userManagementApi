package com.fincore.usermgmt.dto.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response from SumSub when creating an applicant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubApplicantResponse {

    /**
     * SumSub applicant ID (unique identifier)
     */
    private String applicantId;

    /**
     * External user ID (our internal user ID)
     */
    private String externalUserId;

    /**
     * Applicant type (e.g., "individual")
     */
    private String type;

    /**
     * Review status (init, pending, completed, etc.)
     */
    private String reviewStatus;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Whether this is a mock response (for testing)
     */
    private boolean mock;
}
