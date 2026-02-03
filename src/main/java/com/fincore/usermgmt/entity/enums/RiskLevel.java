package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing risk levels in KYC and AML screening
 */
public enum RiskLevel {
    LOW("Low risk - minimal concerns"),
    MEDIUM("Medium risk - requires attention"),
    HIGH("High risk - requires rejection or enhanced due diligence");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
