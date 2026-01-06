-- User Management API Database Schema
-- Compatible with both MySQL 8.0 (Cloud SQL) and H2 (Local Development)
-- Phase 2: Organisation Onboarding Support Added

-- Drop tables in correct order (foreign keys first)
DROP TABLE IF EXISTS KYC_Documents;
DROP TABLE IF EXISTS Organisation;
DROP TABLE IF EXISTS Otp_Tokens;
DROP TABLE IF EXISTS Role_Permissions;
DROP TABLE IF EXISTS User_Roles;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS Permissions;

-- Create Permissions table
CREATE TABLE Permissions (
    Permission_Identifier BIGINT PRIMARY KEY AUTO_INCREMENT,
    Permission_Name VARCHAR(100) UNIQUE NOT NULL,
    Description TEXT,
    Resource VARCHAR(50),
    Action VARCHAR(50),
    Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Roles table
CREATE TABLE Roles (
    Role_Identifier INT AUTO_INCREMENT PRIMARY KEY,
    Role_Name VARCHAR(30),
    Role_Description VARCHAR(100),
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Users table
CREATE TABLE Users (
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

-- Create Role_Permissions junction table
CREATE TABLE Role_Permissions (
    Role_Identifier INT,
    Permission_Identifier BIGINT,
    Granted_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (Role_Identifier, Permission_Identifier),
    CONSTRAINT fk_role_permission_id FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier),
    CONSTRAINT fk_permission_id FOREIGN KEY (Permission_Identifier) REFERENCES Permissions(Permission_Identifier)
);

-- Create OTP Tokens table for multi-factor authentication
CREATE TABLE Otp_Tokens (
    Token_Id BIGINT PRIMARY KEY AUTO_INCREMENT,
    Phone_Number VARCHAR(20) NOT NULL,
    Otp_Code VARCHAR(6) NOT NULL,
    Expires_At TIMESTAMP NOT NULL,
    Verified BOOLEAN NOT NULL DEFAULT FALSE,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_phone ON Users(Phone_Number);
CREATE INDEX idx_users_email ON Users(Email);
CREATE INDEX idx_users_role_id ON Users(Role_Identifier);
CREATE INDEX idx_users_status ON Users(Status_Description);
CREATE INDEX idx_otp_phone_code ON Otp_Tokens(Phone_Number, Otp_Code);
CREATE INDEX idx_otp_expires ON Otp_Tokens(Expires_At);

-- ============================================
-- Phase 2: Organisation Onboarding Tables
-- ============================================

-- Create Address table (generic address for various purposes)
CREATE TABLE address (
    Address_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    Type_Code SMALLINT NOT NULL COMMENT '1=Residential, 2=Business, 3=Registered, 4=Correspondence, 5=Postal',
    Address_Line1 VARCHAR(100) NOT NULL,
    Address_Line2 VARCHAR(100),
    Postal_Code VARCHAR(20),
    State_Code VARCHAR(20),
    City VARCHAR(50),
    Country VARCHAR(50) NOT NULL,
    Status_Description VARCHAR(20) DEFAULT 'ACTIVE',
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_By INT
);

-- Create Organisation table
CREATE TABLE Organisation (
    Organisation_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    User_Identifier INT NOT NULL COMMENT 'Owner/Primary contact user',
    Registration_Number VARCHAR(20),
    SIC_Code VARCHAR(20),
    Legal_Name VARCHAR(100) NOT NULL,
    Business_Name VARCHAR(100),
    Organisation_Type_Description VARCHAR(20) NOT NULL COMMENT 'SOLE_TRADER, PARTNERSHIP, LTD, PLC, etc.',
    Business_Description VARCHAR(255),
    Incorporation_Date DATE,
    Country_Of_Incorporation VARCHAR(100),
    Type_Of_Business_Code VARCHAR(50),
    
    -- Regulatory Information
    HMRC_MLR_Number VARCHAR(50),
    HMRC_Expiry_Date DATE,
    FCA_Number VARCHAR(20),
    ICO_Number VARCHAR(20),
    
    -- Business Structure
    Number_Of_Branches VARCHAR(10),
    Number_Of_Agents VARCHAR(10),
    MLRO_Details VARCHAR(100),
    Compliance_Consultant_Details VARCHAR(100),
    Accountant_Details VARCHAR(100),
    Technology_Service_Provider_Details VARCHAR(100),
    Payout_Partner_Name VARCHAR(50),
    
    -- Registration Details
    Registration_Information VARCHAR(100),
    Company_Number VARCHAR(20),
    SIC_Codes VARCHAR(50),
    Business_License_Number VARCHAR(50),
    Website_Address VARCHAR(100),
    
    -- Remittance Information
    Primary_Remittance_Destination_Country VARCHAR(50),
    Secondary_Remittance_Destination_Country VARCHAR(50),
    
    -- Transaction Volume Information
    Monthly_Turnover_Range VARCHAR(50),
    Number_Of_Incoming_Transactions VARCHAR(20),
    Number_Of_Outgoing_Transactions VARCHAR(20),
    Value_Of_Incoming_Transactions VARCHAR(50),
    Value_Of_Outgoing_Transactions VARCHAR(50),
    Max_Value_Of_Incoming_Payments VARCHAR(50),
    Max_Value_Of_Outgoing_Payments VARCHAR(50),
    Product_Description VARCHAR(255),
    
    -- Address References
    Registered_Address_Identifier INT,
    Business_Address_Identifier INT,
    Correspondence_Address_Identifier INT,
    
    -- Status and Audit
    Status_Description VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, ACTIVE, SUSPENDED, REJECTED, CLOSED',
    Reason_Description VARCHAR(255),
    Legacy_Identifier VARCHAR(20),
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_By INT,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Last_Modified_By INT,
    
    -- Foreign Keys
    CONSTRAINT fk_org_user FOREIGN KEY (User_Identifier) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_org_registered_addr FOREIGN KEY (Registered_Address_Identifier) REFERENCES Address(Address_Identifier),
    CONSTRAINT fk_org_business_addr FOREIGN KEY (Business_Address_Identifier) REFERENCES Address(Address_Identifier),
    CONSTRAINT fk_org_correspondence_addr FOREIGN KEY (Correspondence_Address_Identifier) REFERENCES Address(Address_Identifier)
);

-- Create KYC_Documents table (for organisation document verification)
CREATE TABLE KYC_Documents (
    Document_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    Verification_Identifier INT COMMENT 'Optional verification batch reference',
    Reference_Identifier INT NOT NULL COMMENT 'Organisation_Identifier reference',
    Document_Type_Description VARCHAR(50) NOT NULL COMMENT 'CERTIFICATE_OF_INCORPORATION, PROOF_OF_ADDRESS, ID_DOCUMENT, etc.',
    Sumsub_Document_Identifier VARCHAR(100) COMMENT 'External verification system reference',
    File_Name VARCHAR(255),
    File_URL TEXT,
    Status_Description VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, VERIFIED, REJECTED, EXPIRED',
    Reason_Description TEXT,
    Document_Verified_By INT,
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_By INT,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Last_Modified_By INT,
    
    -- Foreign Keys
    CONSTRAINT fk_kyc_org FOREIGN KEY (Reference_Identifier) REFERENCES Organisation(Organisation_Identifier),
    CONSTRAINT fk_kyc_verified_by FOREIGN KEY (Document_Verified_By) REFERENCES Users(User_Identifier)
);

-- Additional indexes for Organisation tables
CREATE INDEX idx_org_user ON Organisation(User_Identifier);
CREATE INDEX idx_org_status ON Organisation(Status_Description);
CREATE INDEX idx_org_legal_name ON Organisation(Legal_Name);
CREATE INDEX idx_org_reg_number ON Organisation(Registration_Number);
CREATE INDEX idx_address_type ON Address(Type_Code);
CREATE INDEX idx_address_country ON Address(Country);
CREATE INDEX idx_kyc_reference ON KYC_Documents(Reference_Identifier);
CREATE INDEX idx_kyc_status ON KYC_Documents(Status_Description);