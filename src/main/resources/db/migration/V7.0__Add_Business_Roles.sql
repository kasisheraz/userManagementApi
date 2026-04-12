-- =====================================================
-- V7.0 Migration: Add Business Roles
-- =====================================================
-- Description: Add Admin, Compliance, Operational, and Business User roles
-- Date: April 12, 2026
-- =====================================================

-- Add Admin role
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('Admin', 'Administrator with full system access');

-- Add Compliance role  
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('Compliance', 'Compliance officer with audit and review capabilities');

-- Add Operational role
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('Operational', 'Operational staff with standard operational access');

-- Add Business User role
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('Business User', 'Business user with access to own data only');

-- Grant permissions to Admin role (all permissions)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r
CROSS JOIN Permissions p
WHERE r.Role_Name = 'Admin';

-- Grant permissions to Compliance role (read-only + compliance specific)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'Compliance' 
  AND p.Permission_Name IN (
    'USER_READ', 
    'CUSTOMER_READ', 
    'ORG_READ', 
    'DOCUMENT_READ',
    'QUESTIONNAIRE_READ'
  );

-- Grant permissions to Operational role (read + write operations)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'Operational' 
  AND p.Permission_Name IN (
    'USER_READ', 
    'USER_WRITE',
    'CUSTOMER_READ', 
    'CUSTOMER_WRITE',
    'ORG_READ',
    'ORG_WRITE',
    'DOCUMENT_READ',
    'DOCUMENT_WRITE'
  );

-- Grant permissions to Business User role (limited to own data)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'Business User' 
  AND p.Permission_Name IN (
    'USER_READ',
    'ORG_READ'
  );

-- Verification: Display all roles
SELECT 'Roles after migration:' as Info, Role_Identifier, Role_Name, Role_Description 
FROM Roles 
ORDER BY Role_Identifier;

-- Verification: Display role counts
SELECT 'Total Roles Count:' as Info, COUNT(*) as Total 
FROM Roles;
