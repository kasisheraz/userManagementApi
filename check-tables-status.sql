-- Run this in Cloud SQL Console to check what tables exist
USE fincore_db;

-- Show all tables in the database
SHOW TABLES;

-- Show the exact table names and their case
SELECT TABLE_NAME, TABLE_SCHEMA 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fincore_db'
ORDER BY TABLE_NAME;

-- Check lower_case_table_names setting
SHOW VARIABLES LIKE 'lower_case_table_names';
