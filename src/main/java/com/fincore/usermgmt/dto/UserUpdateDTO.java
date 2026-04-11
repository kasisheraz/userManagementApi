package com.fincore.usermgmt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Data Transfer Object for updating an existing user")
public class UserUpdateDTO {
    @Size(max = 20)
    @Schema(description = "User's phone number (unique)", example = "+1234567890")
    private String phoneNumber;

    @Email
    @Size(max = 50)
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Size(max = 100)
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "User's middle name", example = "Michael")
    private String middleName;

    @Size(max = 100)
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "User's residential address")
    private AddressCreateDTO residentialAddress;
    
    @Schema(description = "User's postal address")
    private AddressCreateDTO postalAddress;
    
    @Size(max = 20)
    @Schema(description = "User status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "PENDING", "SUSPENDED"})
    private String statusDescription;
    
    @Schema(description = "User role name", example = "OPERATIONAL_STAFF")
    private String role;
}
