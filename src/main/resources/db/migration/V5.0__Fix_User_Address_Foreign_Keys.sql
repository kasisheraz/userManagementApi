-- Migration V5.0: Fix User Address Foreign Key Constraints
-- Purpose: Ensure proper data type alignment and add foreign key constraints
-- Date: 2026-03-30
-- Note: Aligns Users address identifier columns to BIGINT to match Address table

-- Step 1: Modify column data types to match Address table (BIGINT)
-- The Address.Address_Identifier is BIGINT, so foreign key columns must also be BIGINT
-- This ensures data type consistency and allows for larger address ID ranges
ALTER TABLE Users 
    MODIFY COLUMN Residential_Address_Identifier BIGINT,
    MODIFY COLUMN Postal_Address_Identifier BIGINT;

-- Step 2: Add foreign key constraints for address references
-- These constraints ensure referential integrity between Users and Address tables
-- Note: Using conditional logic to avoid errors if constraints already exist

-- Add foreign key for Residential_Address_Identifier
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Users'
    AND CONSTRAINT_NAME = 'fk_add1_id'
);

SET @add_fk1 = IF(@fk_exists = 0,
    'ALTER TABLE Users ADD CONSTRAINT fk_add1_id 
     FOREIGN KEY (Residential_Address_Identifier) 
     REFERENCES Address(Address_Identifier) ON DELETE SET NULL',
    'SELECT "FK constraint fk_add1_id already exists" AS info'
);

PREPARE stmt FROM @add_fk1;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for Postal_Address_Identifier
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'Users'
    AND CONSTRAINT_NAME = 'fk_add2_id'
);

SET @add_fk2 = IF(@fk_exists = 0,
    'ALTER TABLE Users ADD CONSTRAINT fk_add2_id 
     FOREIGN KEY (Postal_Address_Identifier) 
     REFERENCES Address(Address_Identifier) ON DELETE SET NULL',
    'SELECT "FK constraint fk_add2_id already exists" AS info'
);

PREPARE stmt FROM @add_fk2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verification query (commented out - uncomment to verify)
-- SELECT 
--     TABLE_NAME,
--     COLUMN_NAME,
--     CONSTRAINT_NAME,
--     REFERENCED_TABLE_NAME,
--     REFERENCED_COLUMN_NAME
-- FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = DATABASE()
-- AND TABLE_NAME = 'Users'
-- AND CONSTRAINT_NAME IN ('fk_add1_id', 'fk_add2_id');
