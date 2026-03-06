# Comprehensive Database Schema Verification
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Database Schema Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbHost = "34.89.96.239"
$dbUser = "fincore_user"
$dbPassword = "FinCore2024!Secure"
$dbName = "fincore_db"

Write-Host "Connecting to Cloud SQL: $dbHost" -ForegroundColor Yellow
Write-Host "Database: $dbName" -ForegroundColor Yellow
Write-Host ""

# Check if tables exist
Write-Host "Step 1: Checking if Phase 2 tables exist..." -ForegroundColor Cyan
$checkTablesQuery = @"
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'fincore_db' 
AND TABLE_NAME IN ('aml_screening_results', 'customer_kyc_verification', 'questionnaire_questions', 'customer_answers')
ORDER BY TABLE_NAME;
"@

$tables = mysql -h $dbHost -u $dbUser -p"$dbPassword" $dbName -e $checkTablesQuery 2>&1

if ($tables -match "aml_screening_results") {
    Write-Host "✓ aml_screening_results exists" -ForegroundColor Green
} else {
    Write-Host "✗ aml_screening_results MISSING" -ForegroundColor Red
}

if ($tables -match "customer_kyc_verification") {
    Write-Host "✓ customer_kyc_verification exists" -ForegroundColor Green
} else {
    Write-Host "✗ customer_kyc_verification MISSING" -ForegroundColor Red
}

if ($tables -match "questionnaire_questions") {
    Write-Host "✓ questionnaire_questions exists" -ForegroundColor Green
} else {
    Write-Host "✗ questionnaire_questions MISSING" -ForegroundColor Red
}

if ($tables -match "customer_answers") {
    Write-Host "✓ customer_answers exists" -ForegroundColor Green
} else {
    Write-Host "✗ customer_answers MISSING" -ForegroundColor Red
}

Write-Host ""
Write-Host "Step 2: Verifying column definitions..." -ForegroundColor Cyan
Write-Host ""

# Check critical columns
$criticalChecks = @(
    @{
        Table = "aml_screening_results"
        Column = "screening_type"
        ExpectedType = "varchar(20)"
        Description = "screening_type must be VARCHAR(20)"
    },
    @{
        Table = "aml_screening_results"
        Column = "screened_at"
        ExpectedType = "datetime"
        Description = "screened_at must be DATETIME"
    },
    @{
        Table = "questionnaire_questions"
        Column = "question_id"
        ExpectedType = "int"
        Description = "question_id must be INT (not BIGINT)"
    },
    @{
        Table = "questionnaire_questions"
        Column = "question_category"
        ExpectedType = "varchar(50)"
        Description = "question_category must be VARCHAR(50)"
    },
    @{
        Table = "customer_answers"
        Column = "question_id"
        ExpectedType = "int"
        Description = "question_id foreign key must be INT"
    },
    @{
        Table = "customer_answers"
        Column = "answer"
        ExpectedType = "varchar(500)"
        Description = "answer must be VARCHAR(500) (not answer_text TEXT)"
    },
    @{
        Table = "customer_kyc_verification"
        Column = "status"
        ExpectedType = "varchar(50)"
        Description = "status must be VARCHAR(50)"
    }
)

foreach ($check in $criticalChecks) {
    $query = @"
SELECT COLUMN_NAME, DATA_TYPE, 
       CASE 
           WHEN CHARACTER_MAXIMUM_LENGTH IS NOT NULL THEN CONCAT(DATA_TYPE, '(', CHARACTER_MAXIMUM_LENGTH, ')')
           ELSE DATA_TYPE
       END AS FULL_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
AND TABLE_NAME = '$($check.Table)' 
AND COLUMN_NAME = '$($check.Column)';
"@
    
    $result = mysql -h $dbHost -u $dbUser -p"$dbPassword" $dbName -e $query 2>&1 | Select-String -Pattern "^(varchar|int|bigint|datetime|text)"
    
    if ($result) {
        $actualType = ($result -replace '\s+', ' ').Trim().ToLower()
        $expectedType = $check.ExpectedType.ToLower()
        
        if ($actualType -match $expectedType) {
            Write-Host "✓ $($check.Table).$($check.Column) = $actualType" -ForegroundColor Green
        } else {
            Write-Host "✗ $($check.Table).$($check.Column) = $actualType (Expected: $expectedType)" -ForegroundColor Red
            Write-Host "  Issue: $($check.Description)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ $($check.Table).$($check.Column) - Column NOT FOUND" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Step 3: Checking for old incorrect columns..." -ForegroundColor Cyan
Write-Host ""

# Check if old wrong columns still exist
$oldColumnsQuery = @"
SELECT TABLE_NAME, COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fincore_db' 
AND (
    (TABLE_NAME = 'aml_screening_results' AND COLUMN_NAME = 'screening_date') OR
    (TABLE_NAME = 'customer_answers' AND COLUMN_NAME = 'answer_text')
);
"@

$oldColumns = mysql -h $dbHost -u $dbUser -p"$dbPassword" $dbName -e $oldColumnsQuery 2>&1

if ($oldColumns -match "screening_date") {
    Write-Host "✗ OLD COLUMN FOUND: aml_screening_results.screening_date (should be screened_at)" -ForegroundColor Red
} else {
    Write-Host "✓ screening_date column removed (good!)" -ForegroundColor Green
}

if ($oldColumns -match "answer_text") {
    Write-Host "✗ OLD COLUMN FOUND: customer_answers.answer_text (should be answer)" -ForegroundColor Red
} else {
    Write-Host "✓ answer_text column removed (good!)" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verification Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If all checks passed (✓), you can redeploy the application." -ForegroundColor Yellow
Write-Host "If any checks failed (✗), the deployment will still fail." -ForegroundColor Yellow
