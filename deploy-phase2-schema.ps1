# Deploy Phase 2 Database Schema to Cloud SQL
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploy Phase 2 Schema to Cloud SQL NPE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$DB_NAME = "fincore_db"
$DB_USER = "fincore_app"

Write-Host "Cloud SQL Instance: $CLOUD_SQL_INSTANCE" -ForegroundColor Yellow
Write-Host "Database: $DB_NAME" -ForegroundColor Yellow
Write-Host "User: $DB_USER" -ForegroundColor Yellow
Write-Host ""

# Get DB password
$DB_PASSWORD_SECURE = Read-Host "Database Password for $DB_USER" -AsSecureString
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD_SECURE)
)

Write-Host ""
Write-Host "Option 1: Deploy via Cloud SQL Proxy (RECOMMENDED)" -ForegroundColor Cyan
Write-Host "Option 2: Deploy via gcloud sql import" -ForegroundColor Cyan
Write-Host ""

$choice = Read-Host "Select option (1 or 2)"

if ($choice -eq "1") {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Deploying via Cloud SQL Proxy..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    # Check if cloud-sql-proxy is running
    $proxyProcess = Get-Process -Name "cloud-sql-proxy" -ErrorAction SilentlyContinue
    
    if (-not $proxyProcess) {
        Write-Host "Starting Cloud SQL Proxy..." -ForegroundColor Yellow
        Start-Process -FilePath "cloud-sql-proxy" -ArgumentList "--port=3307 $CLOUD_SQL_INSTANCE" -WindowStyle Hidden
        Write-Host "Waiting for proxy to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
    else {
        Write-Host "Cloud SQL Proxy is already running" -ForegroundColor Green
    }
    
    # Connect and run schema
    Write-Host ""
    Write-Host "Executing schema SQL..." -ForegroundColor Yellow
    
    $mysqlCmd = "mysql -h 127.0.0.1 -P 3307 -u $DB_USER -p$DB_PASSWORD $DB_NAME < complete-entity-schema.sql"
    cmd /c $mysqlCmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Schema deployed successfully!" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Schema deployment failed!" -ForegroundColor Red
        exit 1
    }
}
elseif ($choice -eq "2") {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Deploying via gcloud sql import..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    # Upload to Cloud Storage bucket
    $bucketName = "fincore-npe-db-imports"
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $gcsPath = "gs://$bucketName/schema-$timestamp.sql"
    
    Write-Host "Uploading schema to Cloud Storage..." -ForegroundColor Yellow
    cmd /c "gsutil cp complete-entity-schema.sql $gcsPath"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Schema uploaded to $gcsPath" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Upload failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
    Write-Host "Importing schema to Cloud SQL..." -ForegroundColor Yellow
    cmd /c "gcloud sql import sql $CLOUD_SQL_INSTANCE $gcsPath --database=$DB_NAME --project=project-07a61357-b791-4255-a9e"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Schema imported successfully!" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Import failed!" -ForegroundColor Red
        exit 1
    }
}
else {
    Write-Host "Invalid option!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ Database Schema Deployed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next step: Redeploy the application" -ForegroundColor Cyan
Write-Host "The application should now start successfully." -ForegroundColor Cyan
