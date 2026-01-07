-- Quick Database Initialization for London Region
-- Run this in Cloud SQL Console SQL Editor if auto-initialization fails
-- Navigate to: https://console.cloud.google.com/sql/instances/fincore-npe-db/edit?project=project-07a61357-b791-4255-a9e

USE fincore_db;

-- Drop existing tables if they exist (in correct order)
DROP TABLE IF EXISTS KYC_Documents;
DROP TABLE IF EXISTS Organisation;
DROP TABLE IF EXISTS Address;
DROP TABLE IF EXISTS Otp_Tokens;
DROP TABLE IF EXISTS Role_Permissions;
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
    Status_Description VARCHAR(20) DEFAULT 'ACTIVE',
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Role_Permissions junction table
CREATE TABLE Role_Permissions (
    Role_Identifier INT,
    Permission_Identifier BIGINT,
    PRIMARY KEY (Role_Identifier, Permission_Identifier),
    FOREIGN KEY (Role_Identifier) REFERENCES Roles(Role_Identifier),
    FOREIGN KEY (Permission_Identifier) REFERENCES Permissions(Permission_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create OTP Tokens table
CREATE TABLE Otp_Tokens (
    Token_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    User_Identifier INT,
    Otp_Code VARCHAR(6),
    Expiration_Time TIMESTAMP,
    Is_Used BOOLEAN DEFAULT FALSE,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (User_Identifier) REFERENCES Users(User_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Address table
CREATE TABLE Address (
    Address_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    House_Name VARCHAR(100),
    Street_Name VARCHAR(100),
    Locality VARCHAR(100),
    City VARCHAR(50),
    County VARCHAR(50),
    Country VARCHAR(50),
    Postal_Code VARCHAR(20),
    Address_Type VARCHAR(20),
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Organisation table
CREATE TABLE Organisation (
    Organisation_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    Organisation_Name VARCHAR(200) NOT NULL,
    Registered_Name VARCHAR(200),
    Registration_Number VARCHAR(50) UNIQUE,
    Registration_Date DATE,
    Tax_Identification_Number VARCHAR(50),
    Industry_Sector VARCHAR(100),
    Business_Type VARCHAR(50),
    Website_URL VARCHAR(200),
    Contact_Email VARCHAR(100),
    Contact_Phone VARCHAR(20),
    Registered_Address_Identifier INT,
    Business_Address_Identifier INT,
    Correspondence_Address_Identifier INT,
    Number_Of_Employees INT,
    Annual_Revenue DECIMAL(15,2),
    Currency_Code VARCHAR(3) DEFAULT 'GBP',
    Status_Description VARCHAR(20) DEFAULT 'ACTIVE',
    Onboarding_Status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION',
    Onboarding_Completion_Date DATE,
    Risk_Rating VARCHAR(20),
    Notes TEXT,
    Created_By_User_Identifier INT,
    Last_Modified_By_User_Identifier INT,
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (Registered_Address_Identifier) REFERENCES Address(Address_Identifier),
    FOREIGN KEY (Business_Address_Identifier) REFERENCES Address(Address_Identifier),
    FOREIGN KEY (Correspondence_Address_Identifier) REFERENCES Address(Address_Identifier),
    FOREIGN KEY (Created_By_User_Identifier) REFERENCES Users(User_Identifier),
    FOREIGN KEY (Last_Modified_By_User_Identifier) REFERENCES Users(User_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create KYC Documents table
CREATE TABLE KYC_Documents (
    Document_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    Organisation_Identifier INT NOT NULL,
    Document_Type VARCHAR(100) NOT NULL,
    Document_Number VARCHAR(100),
    Document_Name VARCHAR(200),
    Issue_Date DATE,
    Expiry_Date DATE,
    Issuing_Authority VARCHAR(200),
    Issuing_Country VARCHAR(50),
    Document_File_Path VARCHAR(500),
    Document_File_Type VARCHAR(50),
    File_Size_Bytes BIGINT,
    Upload_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Verification_Status VARCHAR(50) DEFAULT 'PENDING',
    Verification_Date TIMESTAMP NULL,
    Verified_By_User_Identifier INT,
    Verification_Notes TEXT,
    Is_Primary_Document BOOLEAN DEFAULT FALSE,
    Is_Active BOOLEAN DEFAULT TRUE,
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (Organisation_Identifier) REFERENCES Organisation(Organisation_Identifier),
    FOREIGN KEY (Verified_By_User_Identifier) REFERENCES Users(User_Identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key constraints to Users table (after Address table exists)
ALTER TABLE Users 
    ADD FOREIGN KEY (Residential_Address_Identifier) REFERENCES Address(Address_Identifier),
    ADD FOREIGN KEY (Postal_Address_Identifier) REFERENCES Address(Address_Identifier);

-- Insert Seed Data
-- Permissions
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) VALUES
('USER_READ', 'Read user information', 'USER', 'READ'),
('USER_CREATE', 'Create new users', 'USER', 'CREATE'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE'),
('ROLE_READ', 'Read role information', 'ROLE', 'READ'),
('ROLE_CREATE', 'Create new roles', 'ROLE', 'CREATE'),
('ROLE_UPDATE', 'Update role information', 'ROLE', 'UPDATE'),
('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
('ORG_READ', 'Read organisation information', 'ORGANISATION', 'READ'),
('ORG_CREATE', 'Create new organisations', 'ORGANISATION', 'CREATE'),
('ORG_UPDATE', 'Update organisation information', 'ORGANISATION', 'UPDATE'),
('ORG_DELETE', 'Delete organisations', 'ORGANISATION', 'DELETE'),
('ADDRESS_READ', 'Read address information', 'ADDRESS', 'READ'),
('ADDRESS_CREATE', 'Create new addresses', 'ADDRESS', 'CREATE'),
('ADDRESS_UPDATE', 'Update address information', 'ADDRESS', 'UPDATE'),
('ADDRESS_DELETE', 'Delete addresses', 'ADDRESS', 'DELETE'),
('KYC_READ', 'Read KYC documents', 'KYC', 'READ'),
('KYC_CREATE', 'Upload KYC documents', 'KYC', 'CREATE'),
('KYC_UPDATE', 'Update KYC documents', 'KYC', 'UPDATE'),
('KYC_DELETE', 'Delete KYC documents', 'KYC', 'DELETE'),
('KYC_VERIFY', 'Verify KYC documents', 'KYC', 'VERIFY');

-- Roles
INSERT INTO Roles (Role_Name, Role_Description) VALUES
('SYSTEM_ADMIN', 'System Administrator with full access'),
('COMPLIANCE_OFFICER', 'Compliance officer with KYC verification rights'),
('CUSTOMER_SERVICE', 'Customer service staff with limited access');

-- Role Permissions
INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT 1, Permission_Identifier FROM Permissions; -- Admin gets all permissions

INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT 2, Permission_Identifier FROM Permissions WHERE Permission_Name IN 
('ORG_READ', 'KYC_READ', 'KYC_VERIFY', 'USER_READ'); -- Compliance Officer

INSERT INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT 3, Permission_Identifier FROM Permissions WHERE Permission_Name IN 
('ORG_READ', 'USER_READ'); -- Customer Service

-- Default Admin User
INSERT INTO Users (Phone_Number, Email, Role_Identifier, First_Name, Last_Name, Status_Description)
VALUES ('+1234567890', 'admin@fincore.com', 1, 'System', 'Administrator', 'ACTIVE');

-- Sample Address
INSERT INTO Address (House_Name, Street_Name, City, Country, Postal_Code, Address_Type)
VALUES ('Tech Hub', '123 Innovation Street', 'London', 'United Kingdom', 'EC2A 4BX', 'BUSINESS');

-- Sample Organisation
INSERT INTO Organisation (Organisation_Name, Registration_Number, Contact_Email, Contact_Phone, Business_Address_Identifier, Status_Description, Created_By_User_Identifier)
VALUES ('Sample Corp Ltd', 'SC123456', 'contact@samplecorp.com', '+442012345678', 1, 'ACTIVE', 1);

-- Verify tables created
SELECT 'Permissions' AS TableName, COUNT(*) AS RecordCount FROM Permissions
UNION ALL
SELECT 'Roles', COUNT(*) FROM Roles
UNION ALL
SELECT 'Users', COUNT(*) FROM Users
UNION ALL
SELECT 'Role_Permissions', COUNT(*) FROM Role_Permissions
UNION ALL
SELECT 'Address', COUNT(*) FROM Address
UNION ALL
SELECT 'Organisation', COUNT(*) FROM Organisation
UNION ALL
SELECT 'KYC_Documents', COUNT(*) FROM KYC_Documents
UNION ALL
SELECT 'Otp_Tokens', COUNT(*) FROM Otp_Tokens;
