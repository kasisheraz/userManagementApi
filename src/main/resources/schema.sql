-- User Management API Database Schema
-- Compatible with both MySQL 8.0 (Cloud SQL) and H2 (Local Development)

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
    Permission_Identifier INT,
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