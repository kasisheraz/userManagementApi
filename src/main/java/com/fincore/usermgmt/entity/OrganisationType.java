package com.fincore.usermgmt.entity;

/**
 * Enum representing different types of organisations.
 * Maps to Organisation_Type_Description column in Organisation table.
 */
public enum OrganisationType {
    SOLE_TRADER("Sole Trader"),
    PARTNERSHIP("Partnership"),
    LLP("Limited Liability Partnership"),
    LTD("Private Limited Company"),
    PLC("Public Limited Company"),
    CHARITY("Charity/Non-Profit"),
    TRUST("Trust"),
    OTHER("Other");

    private final String description;

    OrganisationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
