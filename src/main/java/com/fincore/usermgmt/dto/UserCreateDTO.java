package com.fincore.usermgmt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Data Transfer Object for creating a new user")
public class UserCreateDTO {
    @NotBlank
    @Size(max = 20)
    @Schema(description = "User's phone number (unique)", example = "+1234567890", required = true)
    private String phoneNumber;

    @Email
    @Size(max = 50)
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "User's first name", example = "John", required = true)
    private String firstName;

    @Size(max = 100)
    @Schema(description = "User's middle name", example = "Michael")
    private String middleName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "User's last name", example = "Doe", required = true)
    private String lastName;

    @Schema(description = "User's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "ID of the user's residential address (FK to Address table)", example = "1", type = "integer", format = "int64")
    private Long residentialAddressIdentifier;
    
    @Schema(description = "ID of the user's postal address (FK to Address table)", example = "2", type = "integer", format = "int64")
    private Long postalAddressIdentifier;
    
    @Size(max = 20)
    @Schema(description = "User status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING", "SUSPENDED"})
    private String statusDescription;
    
    @Schema(description = "User role name", example = "OPERATIONAL_STAFF")
    private String role;
}
