package com.fincore.usermgmt.dto.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SumSub verification status response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubVerificationStatus {

    /**
     * SumSub applicant ID
     */
    private String applicantId;

    /**
     * Review status: init, pending, completed, onHold
     */
    private String reviewStatus;

    /**
     * Review result: GREEN, RED, YELLOW
     */
    private String reviewResult;

    /**
     * Reject labels if any
     */
    private String[] rejectLabels;

    /**
     * Last updated timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Whether this is a mock response
     */
    private boolean mock;

    /**
     * Check if verification is approved
     */
    public boolean isApproved() {
        return "completed".equalsIgnoreCase(reviewStatus) 
                && "GREEN".equalsIgnoreCase(reviewResult);
    }

    /**
     * Check if verification is rejected
     */
    public boolean isRejected() {
        return "completed".equalsIgnoreCase(reviewStatus) 
                && "RED".equalsIgnoreCase(reviewResult);
    }

    /**
     * Check if verification is pending
     */
    public boolean isPending() {
        return "pending".equalsIgnoreCase(reviewStatus);
    }
}
