package com.fincore.usermgmt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for creating a new address.
 */
@Data
@Schema(description = "Data Transfer Object for creating a new address")
public class CreateAddressDTO {
    @NotNull
    @Schema(description = "Type of address (1=Residential, 2=Business, 3=Registered, 4=Correspondence, 5=Postal)", 
            example = "1", required = true)
    private Integer typeCode;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Address line 1", example = "123 Main Street", required = true)
    private String addressLine1;

    @Size(max = 100)
    @Schema(description = "Address line 2 (optional)", example = "Apt 4B")
    private String addressLine2;

    @Size(max = 50)
    @Schema(description = "City", example = "London")
    private String city;

    @Size(max = 20)
    @Schema(description = "State or province code", example = "CA")
    private String stateCode;

    @Size(max = 20)
    @Schema(description = "Postal code", example = "SW1A 1AA")
    private String postalCode;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Country", example = "United Kingdom", required = true)
    private String country;
}
