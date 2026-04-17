package com.fincore.usermgmt.entity;

/**
 * Enum representing the status of an organisation.
 * Maps to Status_Description column in Organisation table.
 */
public enum OrganisationStatus {
    PENDING("Organisation draft - editable by user"),
    UNDER_REVIEW("Organisation submitted for admin review - read-only"),
    REQUIRES_RESUBMISSION("Organisation rejected - requires changes"),
    ACTIVE("Organisation approved and operational"),
    SUSPENDED("Organisation temporarily suspended"),
    REJECTED("Organisation registration permanently rejected"),
    CLOSED("Organisation permanently closed");

    private final String description;

    OrganisationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
