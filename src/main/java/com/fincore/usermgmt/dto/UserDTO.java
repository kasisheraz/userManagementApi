package com.fincore.usermgmt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object representing a user in responses")
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
    
    @Schema(description = "User's first name", example = "John")
    private String firstName;
    
    @Schema(description = "User's middle name", example = "Michael")
    private String middleName;
    
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "User's date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "User's residential address")
    private AddressDTO residentialAddress;
    
    @Schema(description = "User's postal address")
    private AddressDTO postalAddress;
    
    @Schema(description = "Current status of the user", example = "ACTIVE")
    private String statusDescription;
    
    @Schema(description = "User's role name", example = "OPERATIONAL_STAFF")
    private String role;
    
    @Schema(description = "Timestamp when user was created", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdDatetime;
    
    @Schema(description = "Timestamp when user was last modified", example = "2024-01-20T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastModifiedDatetime;
}
