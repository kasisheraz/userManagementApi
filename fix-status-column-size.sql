-- Fix Status_Description column size to accommodate all enum values
-- REQUIRES_RESUBMISSION is 23 characters, current column is VARCHAR(20)

USE fincore;

-- Update organisation table
ALTER TABLE organisation 
MODIFY COLUMN Status_Description VARCHAR(30);

-- Update kyc_document table  
ALTER TABLE kyc_document
MODIFY COLUMN Status_Description VARCHAR(30);

-- Verify changes
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore' 
  AND COLUMN_NAME = 'Status_Description';
