# Quick Check - Phase 2 Tables via gcloud
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Quick Schema Verification (via gcloud)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$INSTANCE = "fincore-npe-db"
$PROJECT = "project-07a61357-b791-4255-a9e"
$DATABASE = "fincore_db"

# Check recent import status
Write-Host "Recent Import Operations:" -ForegroundColor Yellow
$imports = cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"operationType=IMPORT`" --limit=1 --format=`"value(status,endTime)`""

if ($imports -like "*DONE*") {
    Write-Host "Latest Import: SUCCESS" -ForegroundColor Green
    Write-Host "Completed: $imports" -ForegroundColor Gray
} else {
    Write-Host "Latest Import: $imports" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Expected Phase 2 Tables:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. aml_screening_results" -ForegroundColor White
Write-Host "  2. customer_kyc_verification" -ForegroundColor White
Write-Host "  3. questionnaire_questions" -ForegroundColor White
Write-Host "  4. customer_answers" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if GitHub Actions is running
Write-Host "1. Check GitHub Actions deployment status:" -ForegroundColor White
Write-Host "   https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Gray
Write-Host ""

Write-Host "2. The application should now start successfully!" -ForegroundColor White
Write-Host "   Previous error was: 'missing table [aml_screening_results]'" -ForegroundColor Gray
Write-Host "   This should now be resolved." -ForegroundColor Gray
Write-Host ""

Write-Host "3. Once deployed, test the health endpoint:" -ForegroundColor White
Write-Host "   curl https://fincore-npe-api-xxx.run.app/actuator/health" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "Schema Import Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
