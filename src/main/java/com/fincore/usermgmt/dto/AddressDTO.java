package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Address response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private Long id;
    private String addressType;
    private Integer typeCode;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String stateCode;
    private String city;
    private String country;
    private String statusDescription;
    private LocalDateTime createdDatetime;
}
