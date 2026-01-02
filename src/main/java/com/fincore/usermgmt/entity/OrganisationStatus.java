package com.fincore.usermgmt.entity;

/**
 * Enum representing the status of an organisation.
 * Maps to Status_Description column in Organisation table.
 */
public enum OrganisationStatus {
    PENDING("Organisation registration pending review"),
    ACTIVE("Organisation is active and operational"),
    SUSPENDED("Organisation temporarily suspended"),
    REJECTED("Organisation registration rejected"),
    CLOSED("Organisation permanently closed");

    private final String description;

    OrganisationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
