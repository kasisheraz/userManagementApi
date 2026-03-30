# Data Model Analysis and Implementation Plan

**Date:** 2024-03-30  
**Purpose:** Compare current implementation with PDF specifications (03-User-Rep.pdf and 02-Org.pdf)  
**Status:** Analysis in Progress

---

## 1. CURRENT USER DATA MODEL

### User Entity (User.java)
**Current Fields:**
- `id` (User_Identifier) - BIGINT, Primary Key
- `phoneNumber` (Phone_Number) - VARCHAR(20), UNIQUE
- `email` (Email) - VARCHAR(50)
- `role` (Role_Identifier) - FK to Roles table
- `firstName` (First_Name) - VARCHAR(100)
- `middleName` (Middle_Name) - VARCHAR(100)
- `lastName` (Last_Name) - VARCHAR(100)
- `dateOfBirth` (Date_Of_Birth) - DATE
- `residentialAddressIdentifier` (Residential_Address_Identifier) - INT, FK to Address
- `postalAddressIdentifier` (Postal_Address_Identifier) - INT, FK to Address
- `statusDescription` (Status_Description) - VARCHAR(20)
- `createdDatetime` (Created_Datetime) - TIMESTAMP
- `lastModifiedDatetime` (Last_Modified_Datetime) - TIMESTAMP

**Database Schema (V1.0__Initial_Schema.sql):**
```sql
CREATE TABLE IF NOT EXISTS Users (
    User_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    Phone_Number VARCHAR(20) UNIQUE,
    Email VARCHAR(50),
    Role_Identifier INT,
    First_Name VARCHAR(100),
    Middle_Name VARCHAR(100),
    Last_Name VARCHAR(100),
    Date_Of_Birth DATE,
    Residential_Address_Identifier INT,
    Postal_Address_Identifier INT,
    Status_Description VARCHAR(20),
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_id FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier)
);
```

### User DTOs
**UserCreateDTO Fields:**
- phoneNumber
- email
- firstName
- middleName
- lastName
- dateOfBirth
- residentialAddressIdentifier
- postalAddressIdentifier
- statusDescription
- role

---

## 2. CURRENT ORGANISATION DATA MODEL

### Organisation Entity (Organisation.java)
**Current Fields:**
- `id` (Organisation_Identifier) - BIGINT, Primary Key
- `owner` (User_Identifier) - FK to User
- `registrationNumber` (Registration_Number) - VARCHAR(20)
- `sicCode` (SIC_Code) - VARCHAR(20)
- `legalName` (Legal_Name) - VARCHAR(100), NOT NULL
- `businessName` (Business_Name) - VARCHAR(100)
- `organisationType` (Organisation_Type_Description) - ENUM (VARCHAR 20)
- `businessDescription` (Business_Description) - VARCHAR(255)
- `incorporationDate` (Incorporation_Date) - DATE
- `countryOfIncorporation` (Country_Of_Incorporation) - VARCHAR(100)
- `typeOfBusinessCode` (Type_Of_Business_Code) - VARCHAR(50)

**Regulatory Information:**
- `hmrcMlrNumber` (HMRC_MLR_Number) - VARCHAR(50)
- `hmrcExpiryDate` (HMRC_Expiry_Date) - DATE
- `fcaNumber` (FCA_Number) - VARCHAR(20)
- `icoNumber` (ICO_Number) - VARCHAR(20)

**Business Structure:**
- `numberOfBranches` (Number_Of_Branches) - VARCHAR(10)
- `numberOfAgents` (Number_Of_Agents) - VARCHAR(10)
- `mlroDetails` (MLRO_Details) - VARCHAR(100)
- `complianceConsultantDetails` (Compliance_Consultant_Details) - VARCHAR(100)
- `accountantDetails` (Accountant_Details) - VARCHAR(100)
- `technologyServiceProviderDetails` (Technology_Service_Provider_Details) - VARCHAR(100)
- `payoutPartnerName` (Payout_Partner_Name) - VARCHAR(50)

**Registration Details:**
- `registrationInformation` (Registration_Information) - VARCHAR(100)
- `companyNumber` (Company_Number) - VARCHAR(20)
- `sicCodes` (SIC_Codes) - VARCHAR(50)
- `businessLicenseNumber` (Business_License_Number) - VARCHAR(50)
- `websiteAddress` (Website_Address) - VARCHAR(100)

**Remittance Information:**
- `primaryRemittanceDestinationCountry` (Primary_Remittance_Destination_Country) - VARCHAR(50)
- `secondaryRemittanceDestinationCountry` (Secondary_Remittance_Destination_Country) - VARCHAR(50)

**Transaction Volume Information:**
- `monthlyTurnoverRange` (Monthly_Turnover_Range) - VARCHAR(50)
- `numberOfIncomingTransactions` (Number_Of_Incoming_Transactions) - VARCHAR(20)
- `numberOfOutgoingTransactions` (Number_Of_Outgoing_Transactions) - VARCHAR(20)
- `valueOfIncomingTransactions` (Value_Of_Incoming_Transactions) - VARCHAR(50)
- `valueOfOutgoingTransactions` (Value_Of_Outgoing_Transactions) - VARCHAR(50)
- `maxValueOfIncomingPayments` (Max_Value_Of_Incoming_Payments) - VARCHAR(50)
- `maxValueOfOutgoingPayments` (Max_Value_Of_Outgoing_Payments) - VARCHAR(50)
- `productDescription` (Product_Description) - VARCHAR(255)

**Addresses:**
- `registeredAddress` (Registered_Address_Identifier) - FK to Address
- `businessAddress` (Business_Address_Identifier) - FK to Address
- `correspondenceAddress` (Correspondence_Address_Identifier) - FK to Address

**Status and Audit:**
- `status` (Status_Description) - ENUM (VARCHAR 20) - PENDING, ACTIVE, SUSPENDED, REJECTED, CLOSED
- `reasonDescription` (Reason_Description) - VARCHAR(255)
- `legacyIdentifier` (Legacy_Identifier) - VARCHAR(20)
- `createdDatetime` (Created_Datetime) - TIMESTAMP
- `createdBy` (Created_By) - BIGINT
- `lastModifiedDatetime` (Last_Modified_Datetime) - TIMESTAMP
- `lastModifiedBy` (Last_Modified_By) - BIGINT

---

## 3. MISSING FIELDS ANALYSIS

### User Entity - Missing Fields (from 03-User-Rep.pdf)
**⚠️ REQUIRES USER INPUT: Please provide the complete field list from 03-User-Rep.pdf**

**Expected fields that might be missing:**
- [ ] Gender / Title
- [ ] Nationality
- [ ] ID Number (Passport/National ID)
- [ ] Tax ID / NI Number
- [ ] Occupation
- [ ] Source of Funds
- [ ] Employment Status
- [ ] Employer Details
- [ ] Annual Income Range
- [ ] PEP (Politically Exposed Person) Status
- [ ] Sanctions Check Status
- [ ] KYC Status
- [ ] KYC Verification Date
- [ ] Preferred Language
- [ ] Marketing Consent
- [ ] Terms Acceptance Date
- [ ] Last Login
- [ ] Failed Login Attempts
- [ ] Account Locked Status
- [ ] Profile Photo URL
- [ ] Additional Contact Numbers
- [ ] Emergency Contact Details
- [ ] Referred By
- [ ] Customer Segment
- [ ] Risk Rating
- [ ] Additional fields from PDF?

### Organisation Entity - Missing Fields (from 02-Org.pdf)
**⚠️ REQUIRES USER INPUT: Please provide the complete field list from 02-Org.pdf**

**Expected fields that might be missing:**
- [ ] VAT Number
- [ ] Tax Identification Number
- [ ] Primary Contact Person Details
- [ ] Secondary Contact Details
- [ ] Bank Account Details
- [ ] Expected Transaction Volume (monthly)
- [ ] Trading Name (if different from business name)
- [ ] Parent Company Details
- [ ] Subsidiary Information
- [ ] Director/Officer Details
- [ ] Shareholder Information (% ownership)
- [ ] Beneficial Owner Details
- [ ] Business Email
- [ ] Business Phone
- [ ] Business Fax
- [ ] Registered Office Email
- [ ] Company Secretary Details
- [ ] Auditor Details
- [ ] Insurance Provider Details
- [ ] Professional Indemnity Insurance Details
- [ ] Expected Annual Turnover
- [ ] Employee Count
- [ ] Years in Business
- [ ] Credit Rating
- [ ] Industry Classification
- [ ] Business Risk Rating
- [ ] Compliance Status
- [ ] Last Audit Date
- [ ] Next Review Date
- [ ] Additional fields from PDF?

---

## 4. IMPLEMENTATION PLAN (TEMPLATE)

### Phase 1: Analysis and Documentation
- [ ] **User to provide complete field lists from both PDFs**
- [ ] Document all missing fields
- [ ] Identify field types, constraints, and validation rules
- [ ] Map PDF fields to database column names
- [ ] Identify relationships and foreign keys
- [ ] Document backward compatibility requirements

### Phase 2: Database Migration
- [ ] Create V5.0__Add_Missing_User_Fields.sql
  - ALTER TABLE Users ADD COLUMN statements
  - New indexes for searchable fields
  - Data migration scripts for defaults
- [ ] Create V6.0__Add_Missing_Organisation_Fields.sql
  - ALTER TABLE Organisation ADD COLUMN statements
  - New indexes
  - Data migration scripts
- [ ] Test migrations on local H2 database
- [ ] Test migrations on MySQL development instance

### Phase 3: Entity Updates
- [ ] Update User.java entity
  - Add missing fields with JPA annotations
  - Add validation annotations (@NotNull, @Size, @Pattern, etc.)
  - Update @PrePersist and @PreUpdate methods
  - Add new enums if needed
- [ ] Update Organisation.java entity
  - Add missing fields with JPA annotations
  - Add validation annotations
  - Update lifecycle methods
  - Add new enums if needed

### Phase 4: DTO Updates
- [ ] Update UserCreateDTO.java
  - Add fields for user creation
  - Add validation annotations
  - Add Swagger documentation
- [ ] Update UserUpdateDTO.java
  - Add fields for updates
  - Mark optional fields appropriately
- [ ] Update UserDTO.java (response)
  - Add all fields for responses
  - Exclude sensitive fields if needed
- [ ] Repeat for OrganisationCreateDTO, OrganisationUpdateDTO, OrganisationDTO

### Phase 5: Mapper Updates
- [ ] Update UserMapper.java
  - Add mappings for all new fields
  - Handle null values
  - Add conversion logic for enums
  - Map nested objects
- [ ] Update OrganisationMapper.java
  - Add mappings for all new fields
  - Handle nested addresses
  - Handle audit fields

### Phase 6: Repository Updates
- [ ] Review UserRepository.java
  - Add custom queries if needed for new fields
  - Add search methods
- [ ] Review OrganisationRepository.java
  - Add custom queries
  - Add filtering methods

### Phase 7: Service Layer Updates
- [ ] Update UserService.java
  - Add business logic for new fields
  - Add validation logic
  - Update CRUD methods
- [ ] Update OrganisationService.java
  - Add business logic
  - Add validation
  - Handle complex relationships

### Phase 8: Controller Updates
- [ ] Update UserController.java
  - Update Swagger annotations
  - Add new endpoints if needed
  - Update request/response examples
- [ ] Update OrganisationController.java
  - Update Swagger annotations
  - Add endpoints
  - Update documentation

### Phase 9: Postman Collection Updates
- [ ] Update postman_collection.json
  - Add new fields to all User requests
  - Add examples with new fields
  - Update test assertions
- [ ] Update phase2-postman-collection.json
  - Add new fields to all Organisation requests
  - Update examples
  - Update tests
- [ ] Create new test scenarios for new fields

### Phase 10: Security Configuration
- [ ] Restore Swagger UI fix from stash
  - Apply: `git stash pop`
  - Review SecurityConfig.java changes
  - Ensure Swagger paths are whitelisted

### Phase 11: Testing
- [ ] Unit Tests
  - Test all mappers with new fields
  - Test validation annotations
  - Test service layer logic
- [ ] Integration Tests
  - Test full CRUD operations with new fields
  - Test database constraints
  - Test relationships
- [ ] API Tests
  - Test all endpoints with Postman
  - Verify response formats
  - Test validation errors
- [ ] Migration Tests
  - Test forward migration (V5.0, V6.0)
  - Test rollback if needed
  - Verify existing data integrity

### Phase 12: Documentation
- [ ] Update API documentation (Swagger)
- [ ] Update README.md
- [ ] Update RUN_INSTRUCTIONS.md
- [ ] Create MIGRATION_GUIDE.md for deployment
- [ ] Update POSTMAN_USAGE_GUIDE.md

### Phase 13: Deployment
- [ ] Test on local H2
- [ ] Test on local MySQL
- [ ] Deploy to NPE environment
- [ ] Run smoke tests
- [ ] Monitor logs
- [ ] Verify Postman tests pass

---

## 5. REQUIRED USER INPUT

**⚠️ CRITICAL: Cannot proceed without the following information:**

### From 03-User-Rep.pdf:
Please provide the complete list of User fields with:
1. Field name (as shown in PDF)
2. Data type (String, Integer, Date, Boolean, etc.)
3. Max length (if applicable)
4. Required/Optional
5. Validation rules
6. Default value (if any)
7. Description/Purpose

**Example format:**
```
Field: National_Insurance_Number
Type: String
Length: 20
Required: Yes
Validation: UK NI Number format (XX 12 34 56 X)
Default: None
Description: User's National Insurance Number for tax purposes
```

### From 02-Org.pdf:
Please provide the complete list of Organisation fields with the same details as above.

---

## 6. RISK ASSESSMENT

### High Risk Areas:
- **Database Migration**: Production data must not be lost
- **Backward Compatibility**: Existing APIs must continue to work
- **Data Validation**: New required fields may break existing flows
- **Performance**: Additional fields may impact query performance

### Mitigation Strategies:
- Make all new fields NULLABLE initially
- Add defaults where appropriate
- Use Flyway for reversible migrations
- Comprehensive testing before production deployment
- Feature flags for gradual rollout if needed

---

## 7. EXISTING MIGRATION SCRIPTS

**Current Flyway Versions:**
- V1.0__Initial_Schema.sql - Core tables (Users, Roles, Permissions, OTP, Organisation, Address, KYC_Documents)
- V2.0__Initial_Data.sql - Seed data for roles and permissions
- V3.0__Create_KYC_AML_Verification_Tables.sql - KYC verification tables
- V4.0__Add_Users_Address_Foreign_Keys.sql - Address foreign key constraints

**Next Versions to Create:**
- V5.0__Add_Missing_User_Fields.sql (pending PDF analysis)
- V6.0__Add_Missing_Organisation_Fields.sql (pending PDF analysis)

---

## 8. NOTES

- Java 17 is configured for this session only (not permanent)
- Swagger UI fix is in git stash: "Swagger UI fix and documentation updates - 2026-03-30 21:22"
- 544/588 tests currently passing (92.5%)
- Application last ran successfully on port 8081
- Working tree is clean, on branch main

---

## 9. NEXT IMMEDIATE ACTIONS

1. **USER ACTION REQUIRED**: Share field specifications from PDFs
2. Create detailed gap analysis once PDF content is provided
3. Design database migration scripts
4. Begin implementation once design is approved

