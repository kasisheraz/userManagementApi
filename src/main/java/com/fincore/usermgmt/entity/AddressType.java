package com.fincore.usermgmt.entity;

/**
 * Enum representing different types of addresses.
 * Maps to Type_Code column in Address table.
 */
public enum AddressType {
    RESIDENTIAL(1, "Residential Address"),
    BUSINESS(2, "Business Address"),
    REGISTERED(3, "Registered Office Address"),
    CORRESPONDENCE(4, "Correspondence Address"),
    POSTAL(5, "Postal Address");

    private final int code;
    private final String description;

    AddressType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AddressType fromCode(int code) {
        for (AddressType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown address type code: " + code);
    }
}
