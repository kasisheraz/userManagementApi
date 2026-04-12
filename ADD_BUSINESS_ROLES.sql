-- =====================================================
-- Add Business Roles - Direct SQL Script
-- =====================================================
-- Run this script directly in your MySQL database
-- =====================================================

-- Check current roles
SELECT 'Current Roles Before Insert:' as Info;
SELECT Role_Identifier, Role_Name, Role_Description FROM Roles;

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

-- Verify roles were added
SELECT 'Roles After Insert:' as Info;
SELECT Role_Identifier, Role_Name, Role_Description FROM Roles 
WHERE Role_Name IN ('Admin', 'Compliance', 'Operational', 'Business User');

-- Show all roles
SELECT 'All Roles in Database:' as Info;
SELECT Role_Identifier, Role_Name, Role_Description FROM Roles ORDER BY Role_Identifier;

-- Grant permissions to Admin role (all permissions)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r
CROSS JOIN Permissions p
WHERE r.Role_Name = 'Admin';

-- Grant permissions to Compliance role (read-only)
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

-- Final verification
SELECT 'SUCCESS - Roles Created!' as Status;
SELECT 'Total Roles:' as Info, COUNT(*) as Count FROM Roles;
SELECT 'New Roles Added:' as Info, Role_Name FROM Roles WHERE Role_Name IN ('Admin', 'Compliance', 'Operational', 'Business User');
