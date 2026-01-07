-- ============================================
-- COMPLETE ENTITY-BASED SQL SCHEMA
-- Generated from JPA entities in src/main/java/com/fincore/usermgmt/entity/
-- MySQL 8.0 compatible with lower_case_table_names=1
-- For Cloud SQL Import: Database 'fincore_db' must be selected in import dialog
-- ============================================

-- Ensure we're using the correct database
USE fincore_db;

-- Drop tables in correct order (handle foreign key constraints)
DROP TABLE IF EXISTS kyc_documents;
DROP TABLE IF EXISTS otp_tokens;
DROP TABLE IF EXISTS organisation;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS permissions;

-- ============================================
-- CORE TABLES
-- ============================================

-- Permissions Table
CREATE TABLE permissions (
    Permission_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Permission_Name VARCHAR(100) NOT NULL UNIQUE,
    Description TEXT,
    Resource VARCHAR(50),
    Action VARCHAR(50),
    Created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_permission_name (Permission_Name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Roles Table
CREATE TABLE roles (
    Role_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Role_Name VARCHAR(30),
    Role_Description VARCHAR(100),
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_name (Role_Name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Role_Permissions Join Table
CREATE TABLE role_permissions (
    Role_Identifier BIGINT NOT NULL,
    Permission_Identifier BIGINT NOT NULL,
    PRIMARY KEY (Role_Identifier, Permission_Identifier),
    CONSTRAINT fk_rp_role FOREIGN KEY (Role_Identifier) REFERENCES roles(Role_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (Permission_Identifier) REFERENCES permissions(Permission_Identifier) ON DELETE CASCADE,
    INDEX idx_role_id (Role_Identifier),
    INDEX idx_permission_id (Permission_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users Table
CREATE TABLE users (
    User_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Phone_Number VARCHAR(20) UNIQUE,
    Email VARCHAR(50),
    Role_Identifier BIGINT,
    First_Name VARCHAR(100),
    Middle_Name VARCHAR(100),
    Last_Name VARCHAR(100),
    Date_Of_Birth DATE,
    Residential_Address_Identifier INT,
    Postal_Address_Identifier INT,
    Status_Description VARCHAR(20),
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (Role_Identifier) REFERENCES roles(Role_Identifier),
    INDEX idx_phone_number (Phone_Number),
    INDEX idx_email (Email),
    INDEX idx_role_id (Role_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Address Table
CREATE TABLE address (
    Address_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Type_Code INT NOT NULL,
    Address_Line1 VARCHAR(100) NOT NULL,
    Address_Line2 VARCHAR(100),
    Postal_Code VARCHAR(20),
    State_Code VARCHAR(20),
    City VARCHAR(50),
    Country VARCHAR(50) NOT NULL,
    Status_Description VARCHAR(20),
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    Created_By BIGINT,
    INDEX idx_type_code (Type_Code),
    INDEX idx_country (Country),
    INDEX idx_postal_code (Postal_Code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Organisation Table
CREATE TABLE organisation (
    Organisation_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    User_Identifier BIGINT NOT NULL,
    Registration_Number VARCHAR(20),
    SIC_Code VARCHAR(20),
    Legal_Name VARCHAR(100) NOT NULL,
    Business_Name VARCHAR(100),
    Organisation_Type_Description VARCHAR(20) NOT NULL,
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
    
    -- Address References (Foreign Keys)
    Registered_Address_Identifier BIGINT,
    Business_Address_Identifier BIGINT,
    Correspondence_Address_Identifier BIGINT,
    
    -- Status and Audit
    Status_Description VARCHAR(20),
    Reason_Description VARCHAR(255),
    Legacy_Identifier VARCHAR(20),
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    Created_By BIGINT,
    Last_Modified_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Last_Modified_By BIGINT,
    
    CONSTRAINT fk_org_owner FOREIGN KEY (User_Identifier) REFERENCES users(User_Identifier),
    CONSTRAINT fk_org_registered_address FOREIGN KEY (Registered_Address_Identifier) REFERENCES address(Address_Identifier) ON DELETE SET NULL,
    CONSTRAINT fk_org_business_address FOREIGN KEY (Business_Address_Identifier) REFERENCES address(Address_Identifier) ON DELETE SET NULL,
    CONSTRAINT fk_org_correspondence_address FOREIGN KEY (Correspondence_Address_Identifier) REFERENCES address(Address_Identifier) ON DELETE SET NULL,
    
    INDEX idx_user_id (User_Identifier),
    INDEX idx_legal_name (Legal_Name),
    INDEX idx_company_number (Company_Number),
    INDEX idx_org_type (Organisation_Type_Description),
    INDEX idx_status (Status_Description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- KYC_Documents Table
CREATE TABLE kyc_documents (
    Document_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Verification_Identifier INT,
    Reference_Identifier BIGINT NOT NULL,
    Document_Type_Description VARCHAR(50) NOT NULL,
    Sumsub_Document_Identifier VARCHAR(100),
    File_Name VARCHAR(255),
    File_URL TEXT,
    Status_Description VARCHAR(20),
    Reason_Description TEXT,
    Document_Verified_By BIGINT,
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    Created_By BIGINT,
    Last_Modified_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Last_Modified_By BIGINT,
    
    CONSTRAINT fk_kyc_organisation FOREIGN KEY (Reference_Identifier) REFERENCES organisation(Organisation_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_kyc_verifier FOREIGN KEY (Document_Verified_By) REFERENCES users(User_Identifier) ON DELETE SET NULL,
    
    INDEX idx_reference_id (Reference_Identifier),
    INDEX idx_doc_type (Document_Type_Description),
    INDEX idx_status (Status_Description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OTP_Tokens Table
CREATE TABLE otp_tokens (
    Token_Id BIGINT AUTO_INCREMENT PRIMARY KEY,
    Phone_Number VARCHAR(20) NOT NULL,
    Otp_Code VARCHAR(6) NOT NULL,
    Expires_At DATETIME NOT NULL,
    Verified BOOLEAN NOT NULL DEFAULT FALSE,
    Created_At DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_phone_number (Phone_Number),
    INDEX idx_expires_at (Expires_At)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SEED DATA
-- ============================================

-- Insert default permissions
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_READ', 'Read user information', 'users', 'read');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('USER_WRITE', 'Create and update users', 'users', 'write');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_READ', 'Read customer information', 'customers', 'read');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('CUSTOMER_WRITE', 'Create and update customers', 'customers', 'write');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('ORG_READ', 'Read organisation information', 'organisations', 'read');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('ORG_WRITE', 'Create and update organisations', 'organisations', 'write');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_READ', 'Read KYC documents', 'kyc', 'read');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_WRITE', 'Upload and update KYC documents', 'kyc', 'write');
INSERT INTO permissions (Permission_Name, Description, Resource, Action) VALUES ('KYC_VERIFY', 'Verify KYC documents', 'kyc', 'verify');

-- Insert default roles
INSERT INTO roles (Role_Name, Role_Description) VALUES ('SYSTEM_ADMINISTRATOR', 'Full system access');
INSERT INTO roles (Role_Name, Role_Description) VALUES ('ADMIN', 'Administrator with user management capabilities');
INSERT INTO roles (Role_Name, Role_Description) VALUES ('COMPLIANCE_OFFICER', 'Compliance and AML access');
INSERT INTO roles (Role_Name, Role_Description) VALUES ('OPERATIONAL_STAFF', 'Operational access');

-- Link permissions to roles
-- SYSTEM_ADMINISTRATOR gets all permissions (1-9)
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 1);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 2);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 3);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 4);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 5);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 6);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 7);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 8);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (1, 9);

-- ADMIN gets read permissions
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (2, 1);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (2, 3);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (2, 5);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (2, 7);

-- COMPLIANCE_OFFICER gets read and verify permissions
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 1);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 3);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 5);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 6);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 7);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 8);
INSERT INTO role_permissions (Role_Identifier, Permission_Identifier) VALUES (3, 9);

-- Insert default admin user
INSERT INTO users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567890', 'admin@fincore.com', 1, 'System', NULL, 'Administrator', '1990-01-01', 'ACTIVE');

-- Insert compliance officer user
INSERT INTO users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567891', 'compliance@fincore.com', 3, 'Compliance', NULL, 'Officer', '1985-05-15', 'ACTIVE');

-- Insert operational staff user
INSERT INTO users (Phone_Number, Email, Role_Identifier, First_Name, Middle_Name, Last_Name, Date_Of_Birth, Status_Description) 
VALUES ('+1234567892', 'staff@fincore.com', 4, 'Operational', NULL, 'Staff', '1992-03-20', 'ACTIVE');

-- ============================================
-- SAMPLE ORGANISATION DATA
-- ============================================

-- Insert sample addresses (Type_Code: 1=Residential, 2=Business, 3=Registered, 4=Postal, 5=Correspondence)
INSERT INTO address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (3, '10 Downing Street', 'Westminster', 'SW1A 2AA', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

INSERT INTO address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (2, '1 Canada Square', 'Canary Wharf', 'E14 5AB', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

INSERT INTO address (Type_Code, Address_Line1, Address_Line2, Postal_Code, State_Code, City, Country, Status_Description, Created_By) 
VALUES (4, 'PO Box 1234', NULL, 'EC1A 1AA', 'Greater London', 'London', 'United Kingdom', 'ACTIVE', 1);

-- Insert sample organisation
INSERT INTO organisation (
    User_Identifier, Registration_Number, SIC_Code, Legal_Name, Business_Name, 
    Organisation_Type_Description, Business_Description, Incorporation_Date, Country_Of_Incorporation,
    HMRC_MLR_Number, FCA_Number, Number_Of_Branches, Number_Of_Agents,
    Company_Number, Website_Address, Primary_Remittance_Destination_Country, 
    Monthly_Turnover_Range, Number_Of_Incoming_Transactions, Number_Of_Outgoing_Transactions,
    Registered_Address_Identifier, Business_Address_Identifier, Correspondence_Address_Identifier,
    Status_Description, Created_By
) VALUES (
    1, 'REG12345678', '64999', 'Fincore Money Services Ltd', 'Fincore Remittance',
    'LTD', 'International money transfer and remittance services', '2018-06-15', 'United Kingdom',
    'XMLR00123456', 'FRN123456', '5', '20',
    'CN12345678', 'https://fincore-remittance.com', 'India',
    '1000000-5000000', '5000', '4500',
    1, 2, 3,
    'ACTIVE', 1
);

-- Insert sample KYC documents for the organisation
INSERT INTO kyc_documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'CERTIFICATE_OF_INCORPORATION', 'fincore_certificate_of_incorporation.pdf', 'https://storage.fincore.com/docs/cert_incorporation.pdf', 'VERIFIED', 1);

INSERT INTO kyc_documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'HMRC_REGISTRATION', 'fincore_hmrc_registration.pdf', 'https://storage.fincore.com/docs/hmrc_reg.pdf', 'VERIFIED', 1);

INSERT INTO kyc_documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'FCA_AUTHORISATION', 'fincore_fca_authorisation.pdf', 'https://storage.fincore.com/docs/fca_auth.pdf', 'PENDING', 1);

INSERT INTO kyc_documents (Reference_Identifier, Document_Type_Description, File_Name, File_URL, Status_Description, Created_By)
VALUES (1, 'PROOF_OF_ADDRESS', 'fincore_utility_bill.pdf', 'https://storage.fincore.com/docs/utility_bill.pdf', 'VERIFIED', 1);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Use these queries to verify the schema was created correctly:
-- 
-- SELECT COUNT(*) FROM permissions;  -- Should return 9
-- SELECT COUNT(*) FROM roles;  -- Should return 4
-- SELECT COUNT(*) FROM role_permissions;  -- Should return 20
-- SELECT COUNT(*) FROM users;  -- Should return 3
-- SELECT COUNT(*) FROM address;  -- Should return 3
-- SELECT COUNT(*) FROM organisation;  -- Should return 1
-- SELECT COUNT(*) FROM kyc_documents;  -- Should return 4
-- 
-- SHOW TABLES;
-- DESCRIBE users;
-- DESCRIBE organisation;
-- DESCRIBE kyc_documents;
-- ============================================
