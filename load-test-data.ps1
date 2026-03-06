# Insert Test Data via GCS Import
# Uploads SQL file to GCS and imports into Cloud SQL
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSERT TEST DATA via GCS Import  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$PROJECT = "fincore-platform"
$INSTANCE = "fincore-npe-db"
$DATABASE = "fincore_db"
$BUCKET = "fincore-npe-db-backups"
$SQL_FILE = "insert-test-data.sql"
$GCS_PATH = "gs://$BUCKET/test-data/$SQL_FILE"

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Project: $PROJECT" -ForegroundColor White
Write-Host "  Instance: $INSTANCE" -ForegroundColor White
Write-Host "  Database: $DATABASE" -ForegroundColor White
Write-Host "  SQL File: $SQL_FILE" -ForegroundColor White
Write-Host "  GCS Path: $GCS_PATH" -ForegroundColor White
Write-Host ""

# Check if SQL file exists
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "[X] SQL file not found: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] SQL file found" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Uploading SQL file to GCS..." -ForegroundColor Cyan
$uploadResult = cmd /c "gsutil cp $SQL_FILE $GCS_PATH 2>&1"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Upload failed" -ForegroundColor Red
    Write-Host $uploadResult
    exit 1
}

Write-Host "[OK] Uploaded to GCS" -ForegroundColor Green
Write-Host ""

Write-Host "Step 2: Checking for running operations..." -ForegroundColor Cyan
$ops = cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"status=RUNNING OR status=PENDING`" --format=`"value(name)`" 2>nul"

if ($ops) {
    Write-Host "[!] Warning: There are running operations on the instance" -ForegroundColor Yellow
    Write-Host "Waiting for operations to complete..." -ForegroundColor Yellow
    
    $maxWait = 180  
    $waited = 0
    
    while ($ops -and $waited -lt $maxWait) {
        Start-Sleep -Seconds 5
        $waited += 5
        $ops = cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"status=RUNNING OR status=PENDING`" --format=`"value(name)`" 2>nul"
        Write-Host "  Waited $waited seconds..." -ForegroundColor Gray
    }
    
    if ($ops) {
        Write-Host "[X] Timeout waiting for operations to complete" -ForegroundColor Red
        Write-Host "Please try again later or cancel the running operations" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "[OK] No running operations" -ForegroundColor Green
Write-Host ""

Write-Host "Step 3: Importing SQL file into Cloud SQL..." -ForegroundColor Cyan
Write-Host "This may take 1-2 minutes..." -ForegroundColor Yellow
Write-Host ""

$importResult = cmd /c "gcloud sql import sql $INSTANCE $GCS_PATH --database=$DATABASE --project=$PROJECT 2>&1"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Import failed" -ForegroundColor Red
    Write-Host $importResult
    
    if ($importResult -like "*Duplicate entry*" -or $importResult -like "*already exists*") {
        Write-Host "" -ForegroundColor Yellow
        Write-Host "[!] Warning: Some data may already exist in the database" -ForegroundColor Yellow
        Write-Host "This might mean test data was partially inserted before" -ForegroundColor Yellow
        Write-Host "" -ForegroundColor Yellow
        Write-Host "Options:" -ForegroundColor Cyan
        Write-Host "1. Clear existing data first (not recommended for production)" -ForegroundColor White
        Write-Host "2. Modify the SQL file to use INSERT IGNORE" -ForegroundColor White
        Write-Host "3. Accept that some data already exists" -ForegroundColor White
    }
    
    exit 1
}

Write-Host "[OK] Import completed successfully" -ForegroundColor Green
Write-Host $importResult
Write-Host ""

Write-Host "Step 4: Cleaning up GCS file..." -ForegroundColor Cyan
$cleanupResult = cmd /c "gsutil rm $GCS_PATH 2>&1"

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] GCS file removed" -ForegroundColor Green
} else {
    Write-Host "[!] Warning: Could not remove GCS file (not critical)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  TEST DATA INSERTION COMPLETE!  " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "[OK] SQL file uploaded to GCS" -ForegroundColor Green
Write-Host "[OK] Data imported into Cloud SQL" -ForegroundColor Green
Write-Host "[OK] Database now contains test data for UI" -ForegroundColor Green
Write-Host ""
Write-Host "Test Data Includes:" -ForegroundColor Yellow
Write-Host "  - 17 Permissions (CRUD operations)" -ForegroundColor White
Write-Host "  - 4 Roles (ADMIN, COMPLIANCE_OFFICER, OPERATIONAL_STAFF, USER)" -ForegroundColor White
Write-Host "  - 12 Users (various roles)" -ForegroundColor White
Write-Host "  - 15 Addresses" -ForegroundColor White
Write-Host "  - 8 Organizations (LTD, PLC, CHARITY, SOLE_TRADER, LLP)" -ForegroundColor White
Write-Host "  - 16 KYC Documents" -ForegroundColor White
Write-Host "  - 10 KYC Verifications" -ForegroundColor White
Write-Host "  - 14 AML Screening Results" -ForegroundColor White
Write-Host "  - 20 Questionnaire Questions" -ForegroundColor White
Write-Host "  - 48 Customer Answers" -ForegroundColor White
Write-Host ""
Write-Host "Test Users for Login:" -ForegroundColor Yellow
Write-Host "  Admin:       +1234567890         (existing)" -ForegroundColor White
Write-Host "  Compliance:  +447911123456       (Sarah Williams)" -ForegroundColor White
Write-Host "  Compliance:  +447911123457       (John Smith)" -ForegroundColor White
Write-Host "  Staff:       +447911123458       (Emma Brown)" -ForegroundColor White
Write-Host "  User:        +447911123460       (Olivia Taylor - org owner)" -ForegroundColor White
Write-Host "  User:        +447911123461       (James Wilson - org owner)" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Start your UI application" -ForegroundColor White
Write-Host "2. Login with any test phone number" -ForegroundColor White
Write-Host "3. All pages should now display data" -ForegroundColor White
Write-Host "4. To verify data, run: .\quick-test-gcp.ps1" -ForegroundColor Cyan
Write-Host ""
