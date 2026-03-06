-- Direct query to check actual database schema
-- Run this in GCP Console Cloud SQL Query Editor

USE fincore_db;

-- Show the actual CREATE TABLE statement for aml_screening_results
SHOW CREATE TABLE aml_screening_results;

-- Show column definitions
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'fincore_db'
  AND TABLE_NAME = 'aml_screening_results'
ORDER BY ORDINAL_POSITION;
