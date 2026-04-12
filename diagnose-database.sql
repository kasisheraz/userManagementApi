-- =====================================================
-- Database Diagnostic Script
-- Run this to check if everything is set up correctly
-- =====================================================

-- Check database version
SELECT 'Database Connection Test' as Check_Type, NOW() as Current_Time;

-- =====================================================
-- CHECK 1: Roles Table
-- =====================================================
SELECT '=== ROLES CHECK ===' as Section;
SELECT 
    'Roles' as Table_Name,
    COUNT(*) as Row_Count
FROM Roles;

SELECT 
    Role_Identifier,
    Role_Name,
    Role_Description,
    Created_Datetime
FROM Roles
ORDER BY Role_Identifier;

-- =====================================================
-- CHECK 2: Permissions Table
-- =====================================================
SELECT '=== PERMISSIONS CHECK ===' as Section;
SELECT 
    'Permissions' as Table_Name,
    COUNT(*) as Row_Count
FROM Permissions;

SELECT 
    Permission_Identifier,
    Permission_Name,
    Resource,
    Action
FROM Permissions
LIMIT 10;

-- =====================================================
-- CHECK 3: Role Permissions Mapping
-- =====================================================
SELECT '=== ROLE-PERMISSIONS CHECK ===' as Section;
SELECT 
    r.Role_Name,
    COUNT(rp.Permission_Identifier) as Permission_Count
FROM Roles r
LEFT JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
GROUP BY r.Role_Name
ORDER BY r.Role_Name;

-- Detailed view
SELECT 
    r.Role_Name,
    p.Permission_Name,
    p.Resource,
    p.Action
FROM Roles r
JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
JOIN Permissions p ON rp.Permission_Identifier = p.Permission_Identifier
ORDER BY r.Role_Name, p.Permission_Name
LIMIT 20;

-- =====================================================
-- CHECK 4: Users Table
-- =====================================================
SELECT '=== USERS CHECK ===' as Section;
SELECT 
    'Users' as Table_Name,
    COUNT(*) as Row_Count
FROM Users;

SELECT 
    User_Identifier,
    Phone_Number,
    Email,
    First_Name,
    Last_Name,
    r.Role_Name,
    Status_Description,
    Created_Datetime
FROM Users u
LEFT JOIN Roles r ON u.Role_Identifier = r.Role_Identifier
ORDER BY User_Identifier;

-- =====================================================
-- CHECK 5: Check for Admin User
-- =====================================================
SELECT '=== ADMIN USER CHECK ===' as Section;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'FOUND'
        ELSE 'MISSING'
    END as Admin_User_Status,
    COUNT(*) as Admin_Count
FROM Users u
JOIN Roles r ON u.Role_Identifier = r.Role_Identifier
WHERE r.Role_Name IN ('Admin', 'ADMIN', 'SYSTEM_ADMINISTRATOR');

-- =====================================================
-- CHECK 6: Specific Admin User Login Details
-- =====================================================
SELECT '=== DEFAULT ADMIN LOGIN CHECK ===' as Section;
SELECT 
    u.User_Identifier,
    u.Phone_Number,
    u.Email,
    u.First_Name,
    u.Last_Name,
    r.Role_Name,
    u.Status_Description,
    CASE 
        WHEN u.Password IS NULL OR u.Password = '' THEN 'NO PASSWORD SET'
        ELSE 'Password EXISTS'
    END as Password_Status
FROM Users u
JOIN Roles r ON u.Role_Identifier = r.Role_Identifier  
WHERE u.Phone_Number = '+1234567890'
   OR u.Email = 'admin@fincore.com';

-- =====================================================
-- CHECK 7: Organizations
-- =====================================================
SELECT '=== ORGANIZATIONS CHECK ===' as Section;
SELECT 
    'Organisation' as Table_Name,
    COUNT(*) as Row_Count
FROM Organisation;

SELECT 
    Organisation_Identifier,
    Legal_Name,
    Business_Name,
    Organisation_Type_Description,
    Registration_Number,
    Status_Description
FROM Organisation
LIMIT 5;

-- =====================================================
-- CHECK 8: Addresses
-- =====================================================
SELECT '=== ADDRESSES CHECK ===' as Section;
SELECT 
    'Address' as Table_Name,
    COUNT(*) as Row_Count
FROM Address;

-- =====================================================
-- SUMMARY
-- =====================================================
SELECT '=== DIAGNOSTIC SUMMARY ===' as Section;

SELECT 
    'Total Roles' as Metric,
    COUNT(*) as Value
FROM Roles
UNION ALL
SELECT 
    'Total Permissions',
    COUNT(*)
FROM Permissions
UNION ALL
SELECT 
    'Total Users',
    COUNT(*)
FROM Users
UNION ALL
SELECT 
    'Total Organizations',
    COUNT(*)
FROM Organisation
UNION ALL
SELECT 
    'Admin Users',
    COUNT(*)
FROM Users u
JOIN Roles r ON u.Role_Identifier = r.Role_Identifier
WHERE r.Role_Name IN ('Admin', 'ADMIN', 'SYSTEM_ADMINISTRATOR');

-- =====================================================
-- CHECK FOR MISSING ELEMENTS
-- =====================================================
SELECT '=== ISSUES FOUND ===' as Section;

SELECT 
    'Missing Roles' as Issue_Type,
    CASE 
        WHEN COUNT(*) = 0 THEN 'YES - NO ROLES FOUND'
        WHEN COUNT(*) < 4 THEN CONCAT('PARTIAL - Only ', COUNT(*), ' roles found, expected 4')
        ELSE 'NO - All roles present'
    END as Issue_Status
FROM Roles
UNION ALL
SELECT 
    'Missing Admin User',
    CASE 
        WHEN COUNT(*) = 0 THEN 'YES - NO ADMIN USER'
        ELSE 'NO - Admin exists'
    END
FROM Users u
JOIN Roles r ON u.Role_Identifier = r.Role_Identifier
WHERE (r.Role_Name IN ('Admin', 'ADMIN', 'SYSTEM_ADMINISTRATOR'))
  AND (u.Phone_Number = '+1234567890' OR u.Email = 'admin@fincore.com');
