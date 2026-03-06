# Quick Check - Which Phase 2 Tables Are Missing
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Checking Cloud SQL Database Tables" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$DB_HOST = "34.89.96.239"  # Cloud SQL Public IP
$DB_NAME = "fincore_db"
$DB_USER = "fincore_app"

Write-Host "Connecting to: $DB_HOST" -ForegroundColor Yellow
Write-Host "Database: $DB_NAME" -ForegroundColor Yellow
Write-Host ""

$DB_PASSWORD_SECURE = Read-Host "Database Password" -AsSecureString
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD_SECURE)
)

Write-Host ""
Write-Host "Checking for Phase 2 tables..." -ForegroundColor Yellow

# Phase 2 tables that should exist
$phase2Tables = @(
    "aml_screening_results",
    "customer_kyc_verification",
    "questionnaire_questions",
    "customer_answers"
)

$checkSQL = @"
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = '$DB_NAME'
AND TABLE_NAME IN ('aml_screening_results', 'customer_kyc_verification', 'questionnaire_questions', 'customer_answers');
"@

# Save to temp file
$checkSQL | Out-File -FilePath "check-tables.sql" -Encoding ASCII

# Run check
$result = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -e `"$checkSQL`""

Write-Host ""
Write-Host "Existing Phase 2 tables:" -ForegroundColor Cyan
Write-Host $result
Write-Host ""

# Check what's missing
Write-Host "Expected Phase 2 tables:" -ForegroundColor Yellow
foreach ($table in $phase2Tables) {
    if ($result -like "*$table*") {
        Write-Host "  ✅ $table" -ForegroundColor Green
    }
    else {
        Write-Host "  ❌ $table (MISSING)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "To fix: Run deploy-phase2-schema.ps1" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Clean up
Remove-Item "check-tables.sql" -ErrorAction SilentlyContinue
