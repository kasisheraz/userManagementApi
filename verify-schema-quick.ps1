# Verify Database Schema using gcloud
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Database Schema Verification" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$instance = "fincore-npe-db"
$database = "fincore_db"

Write-Host "Checking Phase 2 tables schema...`n" -ForegroundColor Yellow

# Check aml_screening_results.screening_type
Write-Host "1. Checking aml_screening_results.screening_type..." -ForegroundColor White
$query1 = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='aml_screening_results' AND COLUMN_NAME='screening_type';"
$result1 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query1 2>&1 | Select-String -Pattern "varchar"
if ($result1 -match "varchar\(20\)") {
    Write-Host "   ✓ screening_type = varchar(20)" -ForegroundColor Green
} else {
    Write-Host "   ✗ screening_type = $result1 (Expected: varchar(20))" -ForegroundColor Red
}

# Check aml_screening_results.screened_at
Write-Host "2. Checking aml_screening_results.screened_at..." -ForegroundColor White
$query2 = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='aml_screening_results' AND COLUMN_NAME='screened_at';"
$result2 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query2 2>&1 | Select-String -Pattern "screened_at"
if ($result2 -match "screened_at") {
    Write-Host "   ✓ screened_at column exists" -ForegroundColor Green
} else {
    Write-Host "   ✗ screened_at column NOT FOUND" -ForegroundColor Red
}

# Check questionnaire_questions.question_id type
Write-Host "3. Checking questionnaire_questions.question_id..." -ForegroundColor White
$query3 = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='questionnaire_questions' AND COLUMN_NAME='question_id';"
$result3 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query3 2>&1 | Select-String -Pattern "int"
if ($result3 -match "^int\(" -and $result3 -notmatch "bigint") {
    Write-Host "   ✓ question_id = int" -ForegroundColor Green
} else {
    Write-Host "   ✗ question_id = $result3 (Expected: int, not bigint)" -ForegroundColor Red
}

# Check questionnaire_questions.question_category
Write-Host "4. Checking questionnaire_questions.question_category..." -ForegroundColor White
$query4 = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='questionnaire_questions' AND COLUMN_NAME='question_category';"
$result4 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query4 2>&1 | Select-String -Pattern "varchar"
if ($result4 -match "varchar\(50\)") {
    Write-Host "   ✓ question_category = varchar(50)" -ForegroundColor Green
} else {
    Write-Host "   ✗ question_category = $result4 (Expected: varchar(50))" -ForegroundColor Red
}

# Check customer_answers.answer (not answer_text)
Write-Host "5. Checking customer_answers.answer..." -ForegroundColor White
$query5 = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='customer_answers' AND COLUMN_NAME='answer';"
$result5 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query5 2>&1 | Select-String -Pattern "varchar"
if ($result5 -match "varchar\(500\)") {
    Write-Host "   ✓ answer = varchar(500)" -ForegroundColor Green
} else {
    Write-Host "   ✗ answer = $result5 (Expected: varchar(500))" -ForegroundColor Red
}

# Check customer_answers.question_id type
Write-Host "6. Checking customer_answers.question_id..." -ForegroundColor White
$query6 = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='fincore_db' AND TABLE_NAME='customer_answers' AND COLUMN_NAME='question_id';"
$result6 = gcloud sql connect $instance --user=fincore_user --database=$database --quiet --format=value --query=$query6 2>&1 | Select-String -Pattern "int"
if ($result6 -match "^int\(" -and $result6 -notmatch "bigint") {
    Write-Host "   ✓ question_id = int" -ForegroundColor Green
} else {
    Write-Host "   ✗ question_id = $result6 (Expected: int, not bigint)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Verification Complete!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan
