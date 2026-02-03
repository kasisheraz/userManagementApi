package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing the status of KYC verification
 */
public enum VerificationStatus {
    PENDING("Verification is pending review"),
    APPROVED("Verification is approved"),
    REJECTED("Verification is rejected"),
    EXPIRED("Verification has expired");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
