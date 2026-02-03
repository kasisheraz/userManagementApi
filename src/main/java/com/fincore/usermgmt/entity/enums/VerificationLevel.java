package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing the levels of KYC verification
 */
public enum VerificationLevel {
    BASIC("Basic identity verification"),
    FULL("Full KYC verification including document and biometric checks"),
    AML("AML screening and enhanced due diligence");

    private final String description;

    VerificationLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
