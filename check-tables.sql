-- Check existing tables
SELECT TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'fincore_db' 
ORDER BY TABLE_NAME;
