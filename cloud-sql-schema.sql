-- User Management API - Cloud SQL MySQL Schema
-- This script creates the complete database schema for the cloud environment
-- Execute this script on Cloud SQL MySQL 8.0 instance

USE my_auth_db;

-- Drop tables if they exist (for clean recreation)
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;

-- Create permissions table
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    module VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255),
    employee_id VARCHAR(255),
    department VARCHAR(255),
    job_title VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    role_id BIGINT,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role_permissions junction table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_employee_id ON users(employee_id);

-- Create indexes for role_permissions
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Insert default data
-- Insert default permissions
INSERT INTO permissions (name, description, module) VALUES 
    ('USER_READ', 'Read user information', 'USER_MANAGEMENT'),
    ('USER_WRITE', 'Create and update users', 'USER_MANAGEMENT'),
    ('CUSTOMER_READ', 'Read customer information', 'CUSTOMER_MANAGEMENT'),
    ('CUSTOMER_WRITE', 'Create and update customers', 'CUSTOMER_MANAGEMENT');

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('SYSTEM_ADMINISTRATOR', 'Full system access'),
    ('ADMIN', 'Administrator with user management capabilities'),
    ('COMPLIANCE_OFFICER', 'Compliance and AML access'),
    ('OPERATIONAL_STAFF', 'Operational access');

-- Link permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES 
    (1, 1), (1, 2), (1, 3), (1, 4),  -- SYSTEM_ADMINISTRATOR gets all permissions
    (2, 1), (2, 3),                   -- ADMIN gets read permissions
    (3, 1), (3, 3),                   -- COMPLIANCE_OFFICER gets read permissions  
    (4, 1);                           -- OPERATIONAL_STAFF gets user read only

-- Insert default users (with bcrypt hashed passwords)
INSERT INTO users (username, password, full_name, email, phone_number, employee_id, department, job_title, status, role_id, failed_login_attempts, created_at, updated_at) VALUES 
    ('admin', '$2a$10$nZhPCqOb6NsCCXmFPjscGeaH83R4HOBY0o8FMJvfxAkyfF22PDC12', 'System Administrator', 'admin@fincore.com', '+1234567890', 'EMP001', 'IT', 'System Admin', 'ACTIVE', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('compliance', '$2a$10$1My2JRg0A4A0PGwzalpd2efAY3Evz87X3CQSQ9SrvExqQSj6.E7O.', 'Compliance Officer', 'compliance@fincore.com', '+1234567891', 'EMP002', 'Compliance', 'Compliance Officer', 'ACTIVE', 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('staff', '$2a$10$OHPlw5KVcthAjlsaDwzRmueQMvzxEVfnPt2KcT52cmYciyW6KijdC', 'Operational Staff', 'staff@fincore.com', '+1234567892', 'EMP003', 'Operations', 'Staff Member', 'ACTIVE', 4, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Verify the setup
SELECT 'Setup Complete' as status;
SELECT COUNT(*) as permission_count FROM permissions;
SELECT COUNT(*) as role_count FROM roles; 
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as role_permission_count FROM role_permissions;