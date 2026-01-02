package com.fincore.usermgmt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing Organisation.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisationUpdateDTO {

    @Size(max = 20, message = "Registration number must not exceed 20 characters")
    private String registrationNumber;

    @Size(max = 20, message = "SIC code must not exceed 20 characters")
    private String sicCode;

    @Size(max = 100, message = "Legal name must not exceed 100 characters")
    private String legalName;

    @Size(max = 100, message = "Business name must not exceed 100 characters")
    private String businessName;

    private String organisationType;

    @Size(max = 255, message = "Business description must not exceed 255 characters")
    private String businessDescription;

    private LocalDate incorporationDate;

    @Size(max = 100, message = "Country of incorporation must not exceed 100 characters")
    private String countryOfIncorporation;

    @Size(max = 50, message = "Type of business code must not exceed 50 characters")
    private String typeOfBusinessCode;

    // Regulatory Information
    @Size(max = 50, message = "HMRC MLR number must not exceed 50 characters")
    private String hmrcMlrNumber;

    private LocalDate hmrcExpiryDate;

    @Size(max = 20, message = "FCA number must not exceed 20 characters")
    private String fcaNumber;

    @Size(max = 20, message = "ICO number must not exceed 20 characters")
    private String icoNumber;

    // Business Structure
    @Size(max = 10, message = "Number of branches must not exceed 10 characters")
    private String numberOfBranches;

    @Size(max = 10, message = "Number of agents must not exceed 10 characters")
    private String numberOfAgents;

    @Size(max = 100, message = "MLRO details must not exceed 100 characters")
    private String mlroDetails;

    @Size(max = 100, message = "Compliance consultant details must not exceed 100 characters")
    private String complianceConsultantDetails;

    @Size(max = 100, message = "Accountant details must not exceed 100 characters")
    private String accountantDetails;

    @Size(max = 100, message = "Technology service provider details must not exceed 100 characters")
    private String technologyServiceProviderDetails;

    @Size(max = 50, message = "Payout partner name must not exceed 50 characters")
    private String payoutPartnerName;

    // Registration Details
    @Size(max = 100, message = "Registration information must not exceed 100 characters")
    private String registrationInformation;

    @Size(max = 20, message = "Company number must not exceed 20 characters")
    private String companyNumber;

    @Size(max = 50, message = "SIC codes must not exceed 50 characters")
    private String sicCodes;

    @Size(max = 50, message = "Business license number must not exceed 50 characters")
    private String businessLicenseNumber;

    @Size(max = 100, message = "Website address must not exceed 100 characters")
    private String websiteAddress;

    // Remittance Information
    @Size(max = 50, message = "Primary remittance destination country must not exceed 50 characters")
    private String primaryRemittanceDestinationCountry;

    @Size(max = 50, message = "Secondary remittance destination country must not exceed 50 characters")
    private String secondaryRemittanceDestinationCountry;

    // Transaction Volume Information
    @Size(max = 50, message = "Monthly turnover range must not exceed 50 characters")
    private String monthlyTurnoverRange;

    @Size(max = 20, message = "Number of incoming transactions must not exceed 20 characters")
    private String numberOfIncomingTransactions;

    @Size(max = 20, message = "Number of outgoing transactions must not exceed 20 characters")
    private String numberOfOutgoingTransactions;

    @Size(max = 50, message = "Value of incoming transactions must not exceed 50 characters")
    private String valueOfIncomingTransactions;

    @Size(max = 50, message = "Value of outgoing transactions must not exceed 50 characters")
    private String valueOfOutgoingTransactions;

    @Size(max = 50, message = "Max value of incoming payments must not exceed 50 characters")
    private String maxValueOfIncomingPayments;

    @Size(max = 50, message = "Max value of outgoing payments must not exceed 50 characters")
    private String maxValueOfOutgoingPayments;

    @Size(max = 255, message = "Product description must not exceed 255 characters")
    private String productDescription;

    // Addresses (if provided, will replace existing)
    @Valid
    private AddressCreateDTO registeredAddress;

    @Valid
    private AddressCreateDTO businessAddress;

    @Valid
    private AddressCreateDTO correspondenceAddress;

    // Status update (only for admin operations)
    private String status;

    @Size(max = 255, message = "Reason description must not exceed 255 characters")
    private String reasonDescription;
}
