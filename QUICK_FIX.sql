-- QUICK FIX SCRIPT
-- Run this in your database to fix both issues immediately
-- This adds the missing roles and cleans up duplicate empty company numbers

-- =====================================================
-- FIX 1: Add Missing User Roles
-- =====================================================

-- Check if roles already exist
SELECT 'Current Roles:' as Info, Role_Identifier, Role_Name FROM Roles ORDER BY Role_Identifier;

-- Add USER role if missing
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('USER', 'Standard user with basic access');

-- Add MANAGER role if missing
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('MANAGER', 'Manager with operational oversight');

-- Add SUPER_ADMIN role if missing  
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('SUPER_ADMIN', 'Super administrator with elevated privileges');

-- Add permissions for USER role (read-only)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'USER' 
  AND p.Permission_Name IN ('USER_READ', 'CUSTOMER_READ');

-- Add permissions for MANAGER role
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'MANAGER' 
  AND p.Permission_Name IN ('USER_READ', 'CUSTOMER_READ', 'CUSTOMER_WRITE', 'ORG_READ');

-- Add permissions for SUPER_ADMIN role (all permissions)
INSERT IGNORE INTO Role_Permissions (Role_Identifier, Permission_Identifier)
SELECT r.Role_Identifier, p.Permission_Identifier
FROM Roles r, Permissions p
WHERE r.Role_Name = 'SUPER_ADMIN';

-- Verify roles were added
SELECT 'After Fix - All Roles:' as Info, Role_Identifier, Role_Name, Role_Description 
FROM Roles 
ORDER BY Role_Identifier;

-- =====================================================
-- FIX 2: Clean Up Empty Company Numbers
-- =====================================================

-- Show organizations with empty or null company numbers
SELECT 'Organizations with empty company numbers:' as Info,
       Organisation_Identifier, 
       Legal_Name, 
       COALESCE(Company_Number, 'NULL') as Company_Number,
       COALESCE(Registration_Number, 'NULL') as Registration_Number
FROM Organisation
WHERE Company_Number IS NULL OR Company_Number = '';

-- Set empty company numbers to NULL to avoid duplicate validation issues
UPDATE Organisation 
SET Company_Number = NULL 
WHERE Company_Number = '';

-- Set empty registration numbers to NULL
UPDATE Organisation 
SET Registration_Number = NULL 
WHERE Registration_Number = '';

-- Verify cleanup
SELECT 'After Cleanup:' as Info,
       Organisation_Identifier, 
       Legal_Name, 
       COALESCE(Company_Number, 'NULL') as Company_Number,
       COALESCE(Registration_Number, 'NULL') as Registration_Number
FROM Organisation;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check final role count
SELECT 'Total Roles Count:' as Info, COUNT(*) as Total FROM Roles;

-- Check if we can create users with USER role now
SELECT 'USER Role Details:' as Info, * 
FROM Roles 
WHERE Role_Name = 'USER';

-- Check permissions for USER role
SELECT 'USER Role Permissions:' as Info, r.Role_Name, p.Permission_Name, p.Description
FROM Roles r
JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
JOIN Permissions p ON rp.Permission_Identifier = p.Permission_Identifier
WHERE r.Role_Name = 'USER';

SELECT '✅ FIXES APPLIED SUCCESSFULLY!' as Status;
