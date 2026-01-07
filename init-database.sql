-- Initialize Database for User Management API
-- Run this script in your MySQL client or command line:
-- mysql -u root -p < init-database.sql

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS fincore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE fincore_db;

-- Show current tables (for verification)
SHOW TABLES;

-- If Address table doesn't exist, you need to run the schema.sql file
-- Check if Address table exists
SELECT COUNT(*) as table_exists
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fincore_db' 
AND TABLE_NAME = 'Address';

-- Instructions:
-- If the table doesn't exist, run: mysql -u root -p fincore_db < src/main/resources/schema.sql
-- Then run: mysql -u root -p fincore_db < src/main/resources/data.sql
