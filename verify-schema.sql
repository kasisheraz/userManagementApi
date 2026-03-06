-- Run this SQL to verify schema is correct
-- Copy and paste into Cloud SQL Query editor or run via mysql client

USE fincore_db;

-- Check all critical columns
SELECT 
    'aml_screening_results.screening_type' AS check_name,
    COLUMN_TYPE AS actual_value,
    'varchar(20)' AS expected_value,
    CASE WHEN COLUMN_TYPE = 'varchar(20)' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'aml_screening_results' 
  AND COLUMN_NAME = 'screening_type'

UNION ALL

SELECT 
    'aml_screening_results.screened_at' AS check_name,
    COLUMN_TYPE AS actual_value,
    'datetime' AS expected_value,
    CASE WHEN COLUMN_TYPE = 'datetime' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'aml_screening_results' 
  AND COLUMN_NAME = 'screened_at'

UNION ALL

SELECT 
    'questionnaire_questions.question_id' AS check_name,
    COLUMN_TYPE AS actual_value,
    'int' AS expected_value,
    CASE WHEN COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'questionnaire_questions' 
  AND COLUMN_NAME = 'question_id'

UNION ALL

SELECT 
    'questionnaire_questions.question_category' AS check_name,
    COLUMN_TYPE AS actual_value,
    'varchar(50)' AS expected_value,
    CASE WHEN COLUMN_TYPE = 'varchar(50)' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'questionnaire_questions' 
  AND COLUMN_NAME = 'question_category'

UNION ALL

SELECT 
    'customer_answers.answer' AS check_name,
    COLUMN_TYPE AS actual_value,
    'varchar(500)' AS expected_value,
    CASE WHEN COLUMN_TYPE = 'varchar(500)' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'customer_answers' 
  AND COLUMN_NAME = 'answer'

UNION ALL

SELECT 
    'customer_answers.question_id' AS check_name,
    COLUMN_TYPE AS actual_value,
    'int' AS expected_value,
    CASE WHEN COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'customer_answers' 
  AND COLUMN_NAME = 'question_id'

UNION ALL

SELECT 
    'customer_kyc_verification.status' AS check_name,
    COLUMN_TYPE AS actual_value,
    'varchar(50)' AS expected_value,
    CASE WHEN COLUMN_TYPE = 'varchar(50)' THEN '✓ PASS' ELSE '✗ FAIL' END AS status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND TABLE_NAME = 'customer_kyc_verification' 
  AND COLUMN_NAME = 'status';

-- Check for old incorrect columns that should NOT exist
SELECT 
    '❌ OLD COLUMNS CHECK' AS check_name,
    CONCAT(TABLE_NAME, '.', COLUMN_NAME) AS column_found,
    'SHOULD BE REMOVED' AS issue
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND (
      (TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screening_date') OR
      (TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'answer_text')
  );

-- If the above query returns no rows, that's GOOD! It means old columns are gone.
