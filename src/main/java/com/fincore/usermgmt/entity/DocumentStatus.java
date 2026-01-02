package com.fincore.usermgmt.entity;

/**
 * Enum representing the verification status of a KYC document.
 * Maps to Status_Description column in KYC_Documents table.
 */
public enum DocumentStatus {
    PENDING("Document pending verification"),
    UNDER_REVIEW("Document under review"),
    VERIFIED("Document verified successfully"),
    REJECTED("Document rejected"),
    EXPIRED("Document has expired"),
    REQUIRES_UPDATE("Document requires update");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
