package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entity representing a physical address.
 * Can be used for various address types (residential, business, registered, etc.)
 */
@Entity
@Table(name = "Address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Address_Identifier")
    private Long id;

    @Column(name = "Type_Code", nullable = false)
    private Integer typeCode;

    @Column(name = "Address_Line1", nullable = false, length = 100)
    private String addressLine1;

    @Column(name = "Address_Line2", length = 100)
    private String addressLine2;

    @Column(name = "Postal_Code", length = 20)
    private String postalCode;

    @Column(name = "State_Code", length = 20)
    private String stateCode;

    @Column(name = "City", length = 50)
    private String city;

    @Column(name = "Country", nullable = false, length = 50)
    private String country;

    @Column(name = "Status_Description", length = 20)
    private String statusDescription;

    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "Created_By")
    private Long createdBy;

    /**
     * Get the address type as enum.
     */
    @Transient
    public AddressType getAddressType() {
        return typeCode != null ? AddressType.fromCode(typeCode) : null;
    }

    /**
     * Set the address type using enum.
     */
    public void setAddressType(AddressType addressType) {
        this.typeCode = addressType != null ? addressType.getCode() : null;
    }

    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
        if (statusDescription == null) {
            statusDescription = "ACTIVE";
        }
    }
}
