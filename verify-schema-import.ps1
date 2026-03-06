# Verify Phase 2 Schema Import Success
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verifying Phase 2 Schema Import" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$INSTANCE = "fincore-npe-db"
$PROJECT = "project-07a61357-b791-4255-a9e"
$DATABASE = "fincore_db"
$DB_HOST = "34.89.96.239"
$DB_USER = "fincore_app"

Write-Host "Cloud SQL Instance: $INSTANCE" -ForegroundColor Yellow
Write-Host "Database: $DATABASE" -ForegroundColor Yellow
Write-Host ""

# Check import operation status
Write-Host "Checking recent import operations..." -ForegroundColor Cyan
cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"operationType=IMPORT`" --limit=3 --format=`"table(startTime,endTime,status,operationType,error.code)`""

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verifying Tables in Database" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$DB_PASSWORD_SECURE = Read-Host "Database Password for $DB_USER" -AsSecureString
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD_SECURE)
)

Write-Host ""
Write-Host "Connecting to database..." -ForegroundColor Yellow
Write-Host ""

# Create verification SQL script
$verifySQL = @"
-- Check all tables
SELECT 'All Tables:' AS '';
SELECT TABLE_NAME, TABLE_ROWS, CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '$DATABASE'
ORDER BY TABLE_NAME;

-- Specifically check Phase 2 tables
SELECT '\nPhase 2 Tables Check:' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) = 4 THEN 'âœ… All 4 Phase 2 tables exist'
        ELSE CONCAT('â�Œï¸ Only ', COUNT(*), ' of 4 Phase 2 tables exist')
    END AS Status
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '$DATABASE'
AND TABLE_NAME IN ('aml_screening_results', 'customer_kyc_verification', 'questionnaire_questions', 'customer_answers');

-- List Phase 2 tables with details
SELECT '\nPhase 2 Tables Details:' AS '';
SELECT 
    TABLE_NAME as 'Table',
    TABLE_ROWS as 'Rows',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024, 2) as 'Size_KB',
    CREATE_TIME as 'Created'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '$DATABASE'
AND TABLE_NAME IN ('aml_screening_results', 'customer_kyc_verification', 'questionnaire_questions', 'customer_answers')
ORDER BY TABLE_NAME;

-- Check table structures
SELECT '\naml_screening_results columns:' AS '';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '$DATABASE' AND TABLE_NAME = 'aml_screening_results'
ORDER BY ORDINAL_POSITION
LIMIT 5;

SELECT '\ncustomer_kyc_verification columns:' AS '';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '$DATABASE' AND TABLE_NAME = 'customer_kyc_verification'
ORDER BY ORDINAL_POSITION
LIMIT 5;

SELECT '\nquestionnaire_questions columns:' AS '';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '$DATABASE' AND TABLE_NAME = 'questionnaire_questions'
ORDER BY ORDINAL_POSITION
LIMIT 5;

SELECT '\ncustomer_answers columns:' AS '';
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '$DATABASE' AND TABLE_NAME = 'customer_answers'
ORDER BY ORDINAL_POSITION
LIMIT 5;
"@

# Save to temp file
$verifySQL | Out-File -FilePath "verify-schema.sql" -Encoding ASCII

# Run verification
Write-Host "Running verification queries..." -ForegroundColor Cyan
Write-Host ""

$result = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DATABASE < verify-schema.sql 2>&1"

Write-Host $result
Write-Host ""

# Clean up
Remove-Item "verify-schema.sql" -ErrorAction SilentlyContinue

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verification Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($result -like "*aml_screening_results*" -and $result -like "*customer_kyc_verification*" -and $result -like "*questionnaire_questions*" -and $result -like "*customer_answers*") {
    Write-Host "SUCCESS! All Phase 2 tables are present" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Redeploy the application (GitHub Actions should auto-deploy)" -ForegroundColor White
    Write-Host "2. Monitor deployment: https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Gray
    Write-Host "3. Application should start successfully now!" -ForegroundColor Green
} else {
    Write-Host "WARNING: Some Phase 2 tables may be missing" -ForegroundColor Yellow
    Write-Host "Review the output above for details" -ForegroundColor Yellow
}

Write-Host ""
