-- User Management API - Cloud SQL MySQL Schema
-- This script creates the complete database schema for the cloud environment
-- Execute this script on Cloud SQL MySQL 8.0 instance

USE fincore_db;

-- Drop tables in correct order (foreign keys first)
DROP TABLE IF EXISTS KYC_Documents;
DROP TABLE IF EXISTS Organisation;
DROP TABLE IF EXISTS Address;
DROP TABLE IF EXISTS Otp_Tokens;
DROP TABLE IF EXISTS Role_Permissions;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS User_Roles;
DROP TABLE IF EXISTS Permissions;

-- Create Permissions table
CREATE TABLE Permissions (
    Permission_Identifier BIGINT PRIMARY KEY AUTO_INCREMENT,
    Permission_Name VARCHAR(100) UNIQUE NOT NULL,
    Description TEXT,
    Resource VARCHAR(50),
    Action VARCHAR(50),
    Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Roles table
CREATE TABLE Roles (
    Role_Identifier INT AUTO_INCREMENT PRIMARY KEY,
    Role_Name VARCHAR(30),
    Role_Description VARCHAR(100),
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    CONSTRAINT fk_role_id FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Role_Permissions junction table
CREATE TABLE Role_Permissions (
    Role_Identifier INT,
    Permission_Identifier INT,
    Granted_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (Role_Identifier, Permission_Identifier),
    CONSTRAINT fk_role_permission_id FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_permission_id FOREIGN KEY (Permission_Identifier) REFERENCES Permissions(Permission_Identifier) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_users_phone ON Users(Phone_Number);
CREATE INDEX idx_users_email ON Users(Email);
CREATE INDEX idx_users_role_id ON Users(Role_Identifier);
CREATE INDEX idx_users_status ON Users(Status_Description);

-- Create indexes for role_permissions
CREATE INDEX idx_role_permissions_role_id ON Role_Permissions(Role_Identifier);
CREATE INDEX idx_role_permissions_permission_id ON Role_Permissions(Permission_Identifier);

-- Create Otp_Tokens table for authentication
CREATE TABLE Otp_Tokens (
    Token_Id BIGINT PRIMARY KEY AUTO_INCREMENT,
    Phone_Number VARCHAR(20) NOT NULL,
    Otp_Code VARCHAR(6) NOT NULL,
    Expires_At TIMESTAMP NOT NULL,
    Verified BOOLEAN NOT NULL DEFAULT FALSE,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for OTP lookup and cleanup
CREATE INDEX idx_otp_phone_code ON Otp_Tokens(Phone_Number, Otp_Code);
CREATE INDEX idx_otp_expires ON Otp_Tokens(Expires_At);

-- ============================================
-- Phase 2: Organisation Onboarding Tables
-- ============================================

-- Create Address table (generic address for various purposes)
CREATE TABLE Address (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Additional indexes for Organisation tables
CREATE INDEX idx_org_user ON Organisation(User_Identifier);
CREATE INDEX idx_org_status ON Organisation(Status_Description);
CREATE INDEX idx_org_legal_name ON Organisation(Legal_Name);
CREATE INDEX idx_org_reg_number ON Organisation(Registration_Number);
CREATE INDEX idx_address_type ON Address(Type_Code);
CREATE INDEX idx_address_country ON Address(Country);
CREATE INDEX idx_kyc_reference ON KYC_Documents(Reference_Identifier);
CREATE INDEX idx_kyc_status ON KYC_Documents(Status_Description);

-- Insert default data
-- Insert default permissions
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES 
    ('USER_READ', 'Read user information', 'users', 'read'),
    ('USER_WRITE', 'Create and update users', 'users', 'write'),
    ('CUSTOMER_READ', 'Read customer information', 'customers', 'read'),
    ('CUSTOMER_WRITE', 'Create and update customers', 'customers', 'write');

-- Insert default roles
INSERT INTO Roles (Role_Name, Role_Description) VALUES 
    ('SYSTEM_ADMINISTRATOR', 'Full system access'),
    ('ADMIN', 'Administrator with user management capabilities'),
    ('COMPLIANCE_OFFICER', 'Compliance and AML access'),
    ('OPERATIONAL_STAFF', 'Operational access');

-- Link permissions to roles
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier) VALUES 
    (1, 1), (1, 2), (1, 3), (1, 4),  -- SYSTEM_ADMINISTRATOR gets all permissions
    (2, 1), (2, 3),                   -- ADMIN gets read permissions
    (3, 1), (3, 3),                   -- COMPLIANCE_OFFICER gets read permissions  
    (4, 1);                           -- OPERATIONAL_STAFF gets user read only

-- Insert default users
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) VALUES 
    ('+1234567890', 'admin@fincore.com', 1, 'System', NULL, 'Administrator', '1990-01-01', 'ACTIVE'),
    ('+1234567891', 'compliance@fincore.com', 3, 'Compliance', NULL, 'Officer', '1985-05-15', 'ACTIVE'),
    ('+1234567892', 'staff@fincore.com', 4, 'Operational', NULL, 'Staff', '1992-03-20', 'ACTIVE');

-- Verify the setup
SELECT 'Setup Complete' as status;
SELECT COUNT(*) as permission_count FROM Permissions;
SELECT COUNT(*) as role_count FROM Roles; 
SELECT COUNT(*) as user_count FROM Users;
SELECT COUNT(*) as role_permission_count FROM Role_Permissions;