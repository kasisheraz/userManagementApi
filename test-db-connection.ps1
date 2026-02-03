# Test Cloud SQL Connection from Local
# This script will test if the database is reachable with public IP

Write-Host "Testing Cloud SQL Connection..." -ForegroundColor Yellow
Write-Host ""

# Set environment variables (you need to fill in the values)
$env:DB_NAME = "fincore_db"
$env:DB_USER = "fincore_app"
$env:DB_PASSWORD = Read-Host "Enter DB_PASSWORD" -AsSecureString
$env:DB_PASSWORD = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($env:DB_PASSWORD))
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$env:SPRING_PROFILES_ACTIVE = "npe"

Write-Host "Building application..." -ForegroundColor Cyan
mvn clean package -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting application to test DB connection..." -ForegroundColor Cyan
Write-Host "Watch for connection errors in the logs below:" -ForegroundColor Yellow
Write-Host ""

# Run the application - it should fail fast if DB connection fails
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=npe

Write-Host ""
Write-Host "If you see 'Started UserManagementApplication', the DB connection works!" -ForegroundColor Green
