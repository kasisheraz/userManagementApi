-- Quick Diagnostic Check
-- Run this in Cloud SQL Console to see what's in the database

USE fincore_db;

-- Check MySQL case sensitivity setting
SHOW VARIABLES LIKE 'lower_case_table_names';

-- Show all tables
SHOW TABLES;

-- Count records in each table (if they exist)
SELECT 'permissions' AS table_name, COUNT(*) AS count FROM permissions
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'address', COUNT(*) FROM address
UNION ALL
SELECT 'organisation', COUNT(*) FROM organisation;
