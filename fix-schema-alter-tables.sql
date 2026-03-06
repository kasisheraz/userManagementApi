-- CRITICAL FIX: Alter existing tables to match entity definitions
-- This modifies the existing tables instead of trying to drop/recreate them
-- Run this in GCP Console Cloud SQL Query Editor

USE fincore_db;

-- Fix aml_screening_results table
ALTER TABLE aml_screening_results 
    MODIFY COLUMN screening_type VARCHAR(20) NOT NULL;

-- Check if screened_at exists, if not rename screening_date
-- First check if screening_date exists
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fincore_db' 
    AND TABLE_NAME = 'aml_screening_results' 
    AND COLUMN_NAME = 'screening_date');

-- If screening_date exists, rename it to screened_at
SET @query = IF(@col_exists > 0,
    'ALTER TABLE aml_screening_results CHANGE COLUMN screening_date screened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Column screening_date does not exist, skipping rename" AS message');
    
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Fix questionnaire_questions table
ALTER TABLE questionnaire_questions 
    MODIFY COLUMN question_id INT AUTO_INCREMENT PRIMARY KEY,
    MODIFY COLUMN question_category VARCHAR(50);

-- Fix customer_answers table - first need to drop foreign key
-- Get the foreign key name
SELECT CONSTRAINT_NAME INTO @fk_name
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'fincore_db'
  AND TABLE_NAME = 'customer_answers'
  AND COLUMN_NAME = 'question_id'
  AND REFERENCED_TABLE_NAME = 'questionnaire_questions'
LIMIT 1;

-- Drop the foreign key
SET @drop_fk = CONCAT('ALTER TABLE customer_answers DROP FOREIGN KEY ', @fk_name);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Modify the column types
ALTER TABLE customer_answers 
    MODIFY COLUMN question_id INT NOT NULL;

-- Check if answer_text exists and rename to answer
SET @col_exists_answer = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'fincore_db' 
    AND TABLE_NAME = 'customer_answers' 
    AND COLUMN_NAME = 'answer_text');

SET @query_answer = IF(@col_exists_answer > 0,
    'ALTER TABLE customer_answers CHANGE COLUMN answer_text answer VARCHAR(500) NOT NULL',
    'ALTER TABLE customer_answers MODIFY COLUMN answer VARCHAR(500) NOT NULL');
    
PREPARE stmt FROM @query_answer;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Re-add the foreign key with correct type
ALTER TABLE customer_answers 
    ADD CONSTRAINT fk_answer_question 
    FOREIGN KEY (question_id) REFERENCES questionnaire_questions(question_id) ON DELETE CASCADE;

-- Verify the changes
SELECT 'VERIFICATION RESULTS' AS status;

SELECT 
    CONCAT(TABLE_NAME, '.', COLUMN_NAME) as column_name,
    COLUMN_TYPE,
    CASE 
        WHEN TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screening_type' AND COLUMN_TYPE = 'varchar(20)' THEN '✓ PASS'
        WHEN TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screened_at' AND COLUMN_TYPE = 'datetime' THEN '✓ PASS'
        WHEN TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME = 'question_id' AND COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN '✓ PASS'
        WHEN TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME = 'question_category' AND COLUMN_TYPE = 'varchar(50)' THEN '✓ PASS'
        WHEN TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'answer' AND COLUMN_TYPE = 'varchar(500)' THEN '✓ PASS'
        WHEN TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'question_id' AND COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN '✓ PASS'
        ELSE '✗ FAIL'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND (
      (TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME IN ('screening_type', 'screened_at')) OR
      (TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME IN ('question_id', 'question_category')) OR
      (TABLE_NAME = 'customer_answers' AND COLUMN_NAME IN ('answer', 'question_id'))
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
