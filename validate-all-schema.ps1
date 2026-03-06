# Comprehensive Schema Validation
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Comprehensive Schema Validation" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$entities = @(
    "User.java",
    "Role.java",
    "Permission.java",
    "Address.java",
    "Organisation.java",
    "OtpToken.java",
    "KycDocument.java",
    "CustomerKycVerification.java",
    "AmlScreeningResult.java",
    "QuestionnaireQuestion.java",
    "CustomerAnswer.java"
)

Write-Host "Checking entity definitions..." -ForegroundColor Yellow
Write-Host ""

foreach ($entity in $entities) {
    $path = "src\main\java\com\fincore\usermgmt\entity\$entity"
    if (Test-Path $path) {
        Write-Host "✓ Found: $entity" -ForegroundColor Green
        
        # Extract table name
        $content = Get-Content $path -Raw
        if ($content -match '@Table\(name\s*=\s*"([^"]+)"') {
            $tableName = $matches[1]
            Write-Host "  Table: $tableName" -ForegroundColor Gray
            
            # Extract columns with VARCHAR/length constraints
            $columns = [regex]::Matches($content, '@Column\([^)]+name\s*=\s*"([^"]+)"[^)]*(?:length\s*=\s*(\d+))?[^)]*\).*?private\s+(\w+)')
            foreach ($col in $columns) {
                $colName = $col.Groups[1].Value
                $length = $col.Groups[2].Value
                $type = $col.Groups[3].Value
                if ($length) {
                    Write-Host "    - $colName : $type ($length)" -ForegroundColor Cyan
                }
            }
            
            # Check for enums
            $enums = [regex]::Matches($content, '@Column\([^)]+name\s*=\s*"([^"]+)"[^)]*length\s*=\s*(\d+)[^)]*\).*?@Enumerated')
            foreach ($enum in $enums) {
                Write-Host "    - $($enum.Groups[1].Value) : ENUM ($($enum.Groups[2].Value))" -ForegroundColor Yellow
            }
        }
        Write-Host ""
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Now checking SQL file for mismatches..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Key columns to verify in SQL:" -ForegroundColor Yellow
Write-Host "1. aml_screening_results.screening_type = VARCHAR(20)" -ForegroundColor White
Write-Host "2. aml_screening_results.screened_at = DATETIME" -ForegroundColor White
Write-Host "3. customer_kyc_verification.status = VARCHAR(50)" -ForegroundColor White
Write-Host "4. questionnaire_questions.question_category = VARCHAR(50)" -ForegroundColor White
Write-Host "5. questionnaire_questions.status = VARCHAR(20)" -ForegroundColor White
Write-Host ""

Write-Host "Checking SQL schema..." -ForegroundColor Cyan
$sqlContent = Get-Content "complete-entity-schema.sql" -Raw

$checks = @{
    "screening_type VARCHAR\(20\)" = "aml_screening_results.screening_type"
    "screened_at DATETIME" = "aml_screening_results.screened_at"
    "question_category VARCHAR\(50\)" = "questionnaire_questions.question_category"
}

foreach ($pattern in $checks.Keys) {
    if ($sqlContent -match $pattern) {
        Write-Host "✓ $($checks[$pattern]) = CORRECT" -ForegroundColor Green
    } else {
        Write-Host "✗ $($checks[$pattern]) = WRONG" -ForegroundColor Red
    }
}
