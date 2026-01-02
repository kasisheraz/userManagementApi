package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an organisation/business entity.
 * Supports full organisation onboarding with regulatory compliance information.
 */
@Entity
@Table(name = "Organisation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Organisation_Identifier")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_Identifier", nullable = false)
    private User owner;

    @Column(name = "Registration_Number", length = 20)
    private String registrationNumber;

    @Column(name = "SIC_Code", length = 20)
    private String sicCode;

    @Column(name = "Legal_Name", nullable = false, length = 100)
    private String legalName;

    @Column(name = "Business_Name", length = 100)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "Organisation_Type_Description", nullable = false, length = 20)
    private OrganisationType organisationType;

    @Column(name = "Business_Description", length = 255)
    private String businessDescription;

    @Column(name = "Incorporation_Date")
    private LocalDate incorporationDate;

    @Column(name = "Country_Of_Incorporation", length = 100)
    private String countryOfIncorporation;

    @Column(name = "Type_Of_Business_Code", length = 50)
    private String typeOfBusinessCode;

    // Regulatory Information
    @Column(name = "HMRC_MLR_Number", length = 50)
    private String hmrcMlrNumber;

    @Column(name = "HMRC_Expiry_Date")
    private LocalDate hmrcExpiryDate;

    @Column(name = "FCA_Number", length = 20)
    private String fcaNumber;

    @Column(name = "ICO_Number", length = 20)
    private String icoNumber;

    // Business Structure
    @Column(name = "Number_Of_Branches", length = 10)
    private String numberOfBranches;

    @Column(name = "Number_Of_Agents", length = 10)
    private String numberOfAgents;

    @Column(name = "MLRO_Details", length = 100)
    private String mlroDetails;

    @Column(name = "Compliance_Consultant_Details", length = 100)
    private String complianceConsultantDetails;

    @Column(name = "Accountant_Details", length = 100)
    private String accountantDetails;

    @Column(name = "Technology_Service_Provider_Details", length = 100)
    private String technologyServiceProviderDetails;

    @Column(name = "Payout_Partner_Name", length = 50)
    private String payoutPartnerName;

    // Registration Details
    @Column(name = "Registration_Information", length = 100)
    private String registrationInformation;

    @Column(name = "Company_Number", length = 20)
    private String companyNumber;

    @Column(name = "SIC_Codes", length = 50)
    private String sicCodes;

    @Column(name = "Business_License_Number", length = 50)
    private String businessLicenseNumber;

    @Column(name = "Website_Address", length = 100)
    private String websiteAddress;

    // Remittance Information
    @Column(name = "Primary_Remittance_Destination_Country", length = 50)
    private String primaryRemittanceDestinationCountry;

    @Column(name = "Secondary_Remittance_Destination_Country", length = 50)
    private String secondaryRemittanceDestinationCountry;

    // Transaction Volume Information
    @Column(name = "Monthly_Turnover_Range", length = 50)
    private String monthlyTurnoverRange;

    @Column(name = "Number_Of_Incoming_Transactions", length = 20)
    private String numberOfIncomingTransactions;

    @Column(name = "Number_Of_Outgoing_Transactions", length = 20)
    private String numberOfOutgoingTransactions;

    @Column(name = "Value_Of_Incoming_Transactions", length = 50)
    private String valueOfIncomingTransactions;

    @Column(name = "Value_Of_Outgoing_Transactions", length = 50)
    private String valueOfOutgoingTransactions;

    @Column(name = "Max_Value_Of_Incoming_Payments", length = 50)
    private String maxValueOfIncomingPayments;

    @Column(name = "Max_Value_Of_Outgoing_Payments", length = 50)
    private String maxValueOfOutgoingPayments;

    @Column(name = "Product_Description", length = 255)
    private String productDescription;

    // Address References
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "Registered_Address_Identifier")
    private Address registeredAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "Business_Address_Identifier")
    private Address businessAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "Correspondence_Address_Identifier")
    private Address correspondenceAddress;

    // Status and Audit
    @Enumerated(EnumType.STRING)
    @Column(name = "Status_Description", length = 20)
    private OrganisationStatus status;

    @Column(name = "Reason_Description", length = 255)
    private String reasonDescription;

    @Column(name = "Legacy_Identifier", length = 20)
    private String legacyIdentifier;

    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "Created_By")
    private Long createdBy;

    @Column(name = "Last_Modified_Datetime")
    private LocalDateTime lastModifiedDatetime;

    @Column(name = "Last_Modified_By")
    private Long lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
        lastModifiedDatetime = LocalDateTime.now();
        if (status == null) {
            status = OrganisationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }
}
