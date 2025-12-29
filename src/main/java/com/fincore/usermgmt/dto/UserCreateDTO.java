package com.fincore.usermgmt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateDTO {
    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    private LocalDate dateOfBirth;
    private Integer residentialAddressIdentifier;
    private Integer postalAddressIdentifier;
    
    @Size(max = 20)
    private String statusDescription;
    
    private String role;
}
