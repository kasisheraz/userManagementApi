package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing types of AML screening checks
 */
public enum ScreeningType {
    SANCTIONS("Sanctions list screening"),
    PEP("Politically Exposed Person screening"),
    ADVERSE_MEDIA("Adverse media screening");

    private final String description;

    ScreeningType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
