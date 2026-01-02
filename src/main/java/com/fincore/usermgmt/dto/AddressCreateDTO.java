package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Address.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateDTO {

    @NotNull(message = "Address type code is required")
    private Integer typeCode;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 100, message = "Address line 1 must not exceed 100 characters")
    private String addressLine1;

    @Size(max = 100, message = "Address line 2 must not exceed 100 characters")
    private String addressLine2;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 20, message = "State code must not exceed 20 characters")
    private String stateCode;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}
