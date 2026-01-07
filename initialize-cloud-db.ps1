# Initialize Cloud SQL Database for London Region
# Run this after database migration to europe-west2

param(
    [Parameter(Mandatory=$false)]
    [string]$ProjectId = "project-07a61357-b791-4255-a9e",
    
    [Parameter(Mandatory=$false)]
    [string]$Instance = "fincore-npe-db",
    
    [Parameter(Mandatory=$false)]
    [string]$Database = "fincore_db"
)

Write-Host "========================================" -ForegroundColor Blue
Write-Host "Cloud SQL Database Initialization" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

Write-Host "Project: $ProjectId" -ForegroundColor Cyan
Write-Host "Instance: $Instance" -ForegroundColor Cyan
Write-Host "Database: $Database`n" -ForegroundColor Cyan

# Check if gcloud is installed
Write-Host "Checking prerequisites..." -ForegroundColor Yellow
$gcloudPath = Get-Command gcloud -ErrorAction SilentlyContinue
if (-not $gcloudPath) {
    Write-Host "✗ gcloud CLI not found!" -ForegroundColor Red
    Write-Host "Install from: https://cloud.google.com/sdk/docs/install" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ gcloud CLI found`n" -ForegroundColor Green

# Option 1: Set case-insensitive flag
Write-Host "========================================" -ForegroundColor Blue
Write-Host "Option 1: Configure Case-Insensitivity" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

Write-Host "This will set lower_case_table_names=1 flag on the database." -ForegroundColor Yellow
Write-Host "⚠️  WARNING: Instance will restart (downtime ~2-5 minutes)`n" -ForegroundColor Yellow

$setCaseFlag = Read-Host "Set case-insensitive flag? (y/n)"

if ($setCaseFlag -eq 'y') {
    Write-Host "`nSetting database flag..." -ForegroundColor Cyan
    
    gcloud sql instances patch $Instance `
        --database-flags=lower_case_table_names=1 `
        --project=$ProjectId
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Flag set successfully! Instance is restarting..." -ForegroundColor Green
        Write-Host "⏳ Waiting for instance to be ready (this may take 2-5 minutes)...`n" -ForegroundColor Yellow
        
        # Wait for instance to be ready
        $maxWait = 300 # 5 minutes
        $waited = 0
        $ready = $false
        
        while (-not $ready -and $waited -lt $maxWait) {
            Start-Sleep -Seconds 10
            $waited += 10
            
            $status = gcloud sql instances describe $Instance --project=$ProjectId --format="value(state)" 2>$null
            
            if ($status -eq "RUNNABLE") {
                $ready = $true
                Write-Host "✓ Instance is ready!`n" -ForegroundColor Green
            } else {
                Write-Host "Status: $status (waiting ${waited}s / ${maxWait}s)" -ForegroundColor Yellow
            }
        }
        
        if (-not $ready) {
            Write-Host "✗ Instance took too long to restart. Please check Cloud Console." -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✗ Failed to set flag!" -ForegroundColor Red
        exit 1
    }
}

# Option 2: Initialize schema
Write-Host "`n========================================" -ForegroundColor Blue
Write-Host "Option 2: Initialize Database Schema" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

Write-Host "This will execute the schema SQL file to create all tables." -ForegroundColor Yellow
$initSchema = Read-Host "Initialize schema now? (y/n)"

if ($initSchema -eq 'y') {
    if (-not (Test-Path "cloud-sql-schema-complete.sql")) {
        Write-Host "✗ cloud-sql-schema-complete.sql not found!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "`nExecuting schema..." -ForegroundColor Cyan
    Write-Host "Note: You'll need to authenticate via browser`n" -ForegroundColor Yellow
    
    gcloud sql connect $Instance `
        --user=root `
        --project=$ProjectId `
        --database=$Database `
        < cloud-sql-schema-complete.sql
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Schema initialized successfully!`n" -ForegroundColor Green
    } else {
        Write-Host "✗ Schema initialization failed!" -ForegroundColor Red
        Write-Host "Alternative: Execute cloud-sql-schema-complete.sql manually in Cloud SQL Console`n" -ForegroundColor Yellow
    }
}

Write-Host "`n========================================" -ForegroundColor Blue
Write-Host "Next Steps" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

Write-Host "1. Verify tables exist in Cloud SQL Console" -ForegroundColor Cyan
Write-Host "2. Deploy application to Cloud Run" -ForegroundColor Cyan
Write-Host "3. Test endpoints via Postman`n" -ForegroundColor Cyan

Write-Host "✓ Initialization complete!" -ForegroundColor Green
