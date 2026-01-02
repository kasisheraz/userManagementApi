package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Organisation search criteria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisationSearchDTO {
    private String searchTerm;
    private String status;
    private String organisationType;
    private String countryOfIncorporation;
    private int page = 0;
    private int size = 20;
    private String sortBy = "legalName";
    private String sortDirection = "ASC";
}
