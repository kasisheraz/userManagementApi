# Check Cloud SQL Operations Status
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Checking Cloud SQL Operations" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$INSTANCE = "fincore-npe-db"
$PROJECT = "project-07a61357-b791-4255-a9e"

Write-Host "Checking for running operations..." -ForegroundColor Yellow
Write-Host ""

# List recent operations
cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --limit=10 --format=`"table(operationType,status,startTime,endTime,error.code)`""

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Looking for PENDING or RUNNING operations:" -ForegroundColor Yellow

$operations = cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"status=RUNNING OR status=PENDING`" --format=`"value(name,operationType,status)`""

if ($operations) {
    Write-Host ""
    Write-Host "Found blocking operations:" -ForegroundColor Red
    Write-Host $operations
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "1. Wait for operations to complete (recommended)" -ForegroundColor Cyan
    Write-Host "2. Check operation details" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To wait and retry automatically, run:" -ForegroundColor Green
    Write-Host "  .\wait-and-import.ps1" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "✅ No blocking operations found!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You should be able to import now." -ForegroundColor Cyan
    Write-Host "If import still fails, try:" -ForegroundColor Yellow
    Write-Host "1. Refresh the GCP Console page" -ForegroundColor Cyan
    Write-Host "2. Wait 1-2 minutes and try again" -ForegroundColor Cyan
    Write-Host "3. Check instance status is RUNNABLE" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Instance status:" -ForegroundColor Yellow
cmd /c "gcloud sql instances describe $INSTANCE --project=$PROJECT --format=`"value(state)`""

Write-Host ""
