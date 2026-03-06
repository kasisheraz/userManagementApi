Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Quick Schema Verification" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$queries = @"
SELECT 
    CONCAT(TABLE_NAME, '.', COLUMN_NAME) as column_name,
    COLUMN_TYPE,
    CASE 
        WHEN TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screening_type' AND COLUMN_TYPE = 'varchar(20)' THEN 'PASS'
        WHEN TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screened_at' AND COLUMN_TYPE = 'datetime' THEN 'PASS'
        WHEN TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME = 'question_id' AND COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN 'PASS'
        WHEN TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME = 'question_category' AND COLUMN_TYPE = 'varchar(50)' THEN 'PASS'
        WHEN TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'answer' AND COLUMN_TYPE = 'varchar(500)' THEN 'PASS'
        WHEN TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'question_id' AND COLUMN_TYPE LIKE 'int%' AND COLUMN_TYPE NOT LIKE 'bigint%' THEN 'PASS'
        WHEN TABLE_NAME = 'customer_kyc_verification' AND COLUMN_NAME = 'status' AND COLUMN_TYPE = 'varchar(50)' THEN 'PASS'
        ELSE 'FAIL'
    END as status
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
  AND (
      (TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME IN ('screening_type', 'screened_at')) OR
      (TABLE_NAME = 'questionnaire_questions' AND COLUMN_NAME IN ('question_id', 'question_category')) OR
      (TABLE_NAME = 'customer_answers' AND COLUMN_NAME IN ('answer', 'question_id')) OR
      (TABLE_NAME = 'customer_kyc_verification' AND COLUMN_NAME = 'status')
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
"@

Write-Host "Executing verification query..." -ForegroundColor Yellow

# Save query to temp file
$queries | Out-File -FilePath "temp-verify.sql" -Encoding UTF8

# Execute via gcloud
$result = gcloud sql connect fincore-npe-db --user=fincore_user --database=fincore_db --quiet < temp-verify.sql 2>&1

Write-Host $result
Write-Host ""

# Cleanup
Remove-Item "temp-verify.sql" -ErrorAction SilentlyContinue

Write-Host "========================================`n" -ForegroundColor Cyan
