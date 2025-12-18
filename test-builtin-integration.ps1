# Test Cloud SQL Built-in Integration
Write-Host "=== Testing Cloud SQL Built-in Integration ===" -ForegroundColor Green

# Set environment variables for Cloud SQL built-in integration
Write-Host "Setting Cloud SQL environment variables..." -ForegroundColor Cyan
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "fincore_app"
$env:DB_PASSWORD = "FinCore2024Secure"
$env:PORT = "8080"

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Instance: $env:CLOUD_SQL_INSTANCE" -ForegroundColor White
Write-Host "  Database: $env:DB_NAME" -ForegroundColor White  
Write-Host "  User: $env:DB_USER" -ForegroundColor White
Write-Host "  Profile: gcp-builtin" -ForegroundColor White

Write-Host "`n=== Authenticating with Google Cloud ===" -ForegroundColor Green
gcloud auth application-default login --quiet 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Authentication successful" -ForegroundColor Green
} else {
    Write-Host "⚠️ Authentication may be required" -ForegroundColor Yellow
}

Write-Host "`n=== Starting Application with Built-in Cloud SQL ===" -ForegroundColor Green
Write-Host "This will test if the permission issue is resolved..." -ForegroundColor Cyan

# Clean compile
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation successful" -ForegroundColor Green
    Write-Host "Starting with gcp-builtin profile..." -ForegroundColor Cyan
    
    # Start application with built-in integration
    mvn spring-boot:run "-Dspring-boot.run.profiles=gcp-builtin"
} else {
    Write-Host "❌ Compilation failed" -ForegroundColor Red
    exit 1
}