# Test Cloud SQL Built-in Integration After Permission Fix
Write-Host "=== Testing Built-in Integration After Permission Fix ===" -ForegroundColor Green

# Set environment variables for Cloud SQL
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "fincore_app"
$env:DB_PASSWORD = "FinCore2024Secure"
$env:PORT = "8080"

Write-Host "Environment Configuration:" -ForegroundColor Cyan
Write-Host "  Cloud SQL Instance: $env:CLOUD_SQL_INSTANCE" -ForegroundColor White
Write-Host "  Database: $env:DB_NAME" -ForegroundColor White
Write-Host "  User: $env:DB_USER" -ForegroundColor White

Write-Host "`n=== Starting Application with Built-in Integration ===" -ForegroundColor Green
Write-Host "This test will verify if the permission fix resolved the issue..." -ForegroundColor Cyan

# Start the application
mvn spring-boot:run "-Dspring-boot.run.profiles=gcp-builtin"