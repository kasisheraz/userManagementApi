# Wait for Cloud SQL Operations to Complete, Then Import
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Auto-Wait and Import Schema" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$INSTANCE = "fincore-npe-db"
$PROJECT = "project-07a61357-b791-4255-a9e"
$DATABASE = "fincore_db"
$BUCKET = "fincore-npe-db-imports"
$SCHEMA_FILE = "complete-entity-schema.sql"

# Check if schema file exists
if (-not (Test-Path $SCHEMA_FILE)) {
    Write-Host "❌ Error: $SCHEMA_FILE not found!" -ForegroundColor Red
    exit 1
}

Write-Host "Schema file: $SCHEMA_FILE" -ForegroundColor Green
Write-Host "Instance: $INSTANCE" -ForegroundColor Yellow
Write-Host "Database: $DATABASE" -ForegroundColor Yellow
Write-Host ""

# Function to check for running operations
function Get-RunningOperations {
    $ops = cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"status=RUNNING OR status=PENDING`" --format=`"value(name)`" 2>nul"
    return $ops
}

# Wait for any running operations to complete
Write-Host "Checking for blocking operations..." -ForegroundColor Cyan
$maxWaitMinutes = 10
$startTime = Get-Date

while ($true) {
    $runningOps = Get-RunningOperations
    
    if (-not $runningOps) {
        Write-Host "✅ No blocking operations" -ForegroundColor Green
        break
    }
    
    $elapsed = [math]::Round(((Get-Date) - $startTime).TotalMinutes, 1)
    
    if ($elapsed -ge $maxWaitMinutes) {
        Write-Host ""
        Write-Host "⚠️ Still waiting after $maxWaitMinutes minutes" -ForegroundColor Yellow
        Write-Host "Operations in progress:" -ForegroundColor Yellow
        cmd /c "gcloud sql operations list --instance=$INSTANCE --project=$PROJECT --filter=`"status=RUNNING OR status=PENDING`" --format=`"table(operationType,status,startTime)`""
        Write-Host ""
        $continue = Read-Host "Continue waiting? (Y/N)"
        if ($continue -ne "Y" -and $continue -ne "y") {
            Write-Host "Aborted by user" -ForegroundColor Red
            exit 1
        }
        $startTime = Get-Date
    }
    
    Write-Host "Waiting for operations to complete... ($elapsed minutes elapsed)" -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Uploading Schema to Cloud Storage" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$gcsPath = "gs://$BUCKET/schema-$timestamp.sql"

Write-Host "Uploading to: $gcsPath" -ForegroundColor Yellow
cmd /c "gsutil cp $SCHEMA_FILE $gcsPath"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to upload to Cloud Storage" -ForegroundColor Red
    Write-Host ""
    Write-Host "Trying to create bucket..." -ForegroundColor Yellow
    cmd /c "gsutil mb -p $PROJECT -l europe-west2 gs://$BUCKET"
    
    Write-Host "Retrying upload..." -ForegroundColor Yellow
    cmd /c "gsutil cp $SCHEMA_FILE $gcsPath"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Upload failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ Upload successful" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Importing Schema to Cloud SQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Starting import..." -ForegroundColor Yellow
cmd /c "gcloud sql import sql $INSTANCE $gcsPath --database=$DATABASE --project=$PROJECT"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ SUCCESS! Schema Imported" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. GitHub Actions will automatically redeploy" -ForegroundColor White
    Write-Host "2. Monitor deployment:" -ForegroundColor White
    Write-Host "   https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Application should start successfully now! 🚀" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ Import Failed" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the error message above for details." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "- SQL syntax errors" -ForegroundColor Gray
    Write-Host "- Database permissions" -ForegroundColor Gray
    Write-Host "- Connection issues" -ForegroundColor Gray
    exit 1
}
