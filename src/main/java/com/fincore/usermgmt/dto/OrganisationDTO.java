package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Organisation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisationDTO {
    private Long id;
    private Long ownerId;
    private String ownerName;
    
    // Basic Information
    private String registrationNumber;
    private String sicCode;
    private String legalName;
    private String businessName;
    private String organisationType;
    private String businessDescription;
    private LocalDate incorporationDate;
    private String countryOfIncorporation;
    private String typeOfBusinessCode;
    
    // Regulatory Information
    private String hmrcMlrNumber;
    private LocalDate hmrcExpiryDate;
    private String fcaNumber;
    private String icoNumber;
    
    // Business Structure
    private String numberOfBranches;
    private String numberOfAgents;
    private String mlroDetails;
    private String complianceConsultantDetails;
    private String accountantDetails;
    private String technologyServiceProviderDetails;
    private String payoutPartnerName;
    
    // Registration Details
    private String registrationInformation;
    private String companyNumber;
    private String sicCodes;
    private String businessLicenseNumber;
    private String websiteAddress;
    
    // Remittance Information
    private String primaryRemittanceDestinationCountry;
    private String secondaryRemittanceDestinationCountry;
    
    // Transaction Volume Information
    private String monthlyTurnoverRange;
    private String numberOfIncomingTransactions;
    private String numberOfOutgoingTransactions;
    private String valueOfIncomingTransactions;
    private String valueOfOutgoingTransactions;
    private String maxValueOfIncomingPayments;
    private String maxValueOfOutgoingPayments;
    private String productDescription;
    
    // Addresses
    private AddressDTO registeredAddress;
    private AddressDTO businessAddress;
    private AddressDTO correspondenceAddress;
    
    // Status and Audit
    private String status;
    private String reasonDescription;
    private String legacyIdentifier;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastModifiedDatetime;
}
