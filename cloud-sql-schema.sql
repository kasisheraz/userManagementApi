-- User Management API - Cloud SQL MySQL Schema
-- This script creates the complete database schema for the cloud environment
-- Execute this script on Cloud SQL MySQL 8.0 instance

USE fincore_db;

-- Drop tables in correct order (foreign keys first)
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