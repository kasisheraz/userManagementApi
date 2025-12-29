package com.fincore.usermgmt.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String phoneNumber;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Integer residentialAddressIdentifier;
    private Integer postalAddressIdentifier;
    private String statusDescription;
    private String role;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastModifiedDatetime;
}
