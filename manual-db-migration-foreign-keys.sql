-- Manual Database Migration Script
-- Purpose: Add Foreign Key Constraints to Users Table for Address References
-- Environment: NPE (Non-Production Environment)
-- Database: fincore_db
-- Date: 2026-03-29
-- 
-- IMPORTANT: This script should be executed manually on the Cloud SQL database
-- if Flyway migrations are not automatically applied during deployment.
--
-- Prerequisites:
-- 1. Ensure no orphaned records exist (Users with address IDs that don't exist in Address table)
-- 2. Backup the database before applying these changes
-- 3. Verify the Address table exists and has the Address_Identifier primary key
--
-- Steps to Execute:

-- Step 1: Check for orphaned records (optional but recommended)
-- Run this query to verify data integrity:
/*
SELECT 
    u.User_Identifier,
    u.Residential_Address_Identifier,
    u.Postal_Address_Identifier
FROM Users u
WHERE (u.Residential_Address_Identifier IS NOT NULL 
       AND u.Residential_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address))
   OR (u.Postal_Address_Identifier IS NOT NULL 
       AND u.Postal_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address));
*/

-- If orphaned records exist, either:
-- a) Set those fields to NULL:  UPDATE Users SET Residential_Address_Identifier = NULL WHERE ...
-- b) Create missing Address records
-- c) Fix the data as appropriate for your business logic

-- Step 2: Add Foreign Key Constraint for Residential Address
ALTER TABLE Users 
ADD CONSTRAINT fk_add1_id 
FOREIGN KEY (Residential_Address_Identifier) 
REFERENCES Address(Address_Identifier);

-- Step 3: Add Foreign Key Constraint for Postal Address
ALTER TABLE Users 
ADD CONSTRAINT fk_add2_id 
FOREIGN KEY (Postal_Address_Identifier) 
REFERENCES Address(Address_Identifier);

-- Step 4: Verify the constraints were created successfully
-- Run this query to check:
/*
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'fincore_db'
  AND TABLE_NAME = 'Users'
  AND CONSTRAINT_NAME IN ('fk_add1_id', 'fk_add2_id');
*/

-- Expected result: Two rows showing the foreign key constraints

-- Notes:
-- - These constraints ensure referential integrity between Users and Address tables
-- - If you need to rollback, use: 
--   ALTER TABLE Users DROP FOREIGN KEY fk_add1_id;
--   ALTER TABLE Users DROP FOREIGN KEY fk_add2_id;
-- - Future inserts/updates to Users table will validate that address IDs exist in Address table
