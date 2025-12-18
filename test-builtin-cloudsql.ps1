# Test Built-in Cloud SQL Connector Locally (PowerShell)
# This script tests the new Cloud SQL Java connector approach

Write-Host "🔧 Testing Built-in Cloud SQL Connector Approach" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Set environment variables for testing
$env:SPRING_PROFILES_ACTIVE = "gcp-builtin"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "fincore_app"
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"

# Check if gcloud is authenticated
Write-Host "1. Checking GCP authentication..." -ForegroundColor Yellow
try {
    $null = gcloud auth print-access-token 2>$null
    Write-Host "✅ GCP authentication verified" -ForegroundColor Green
} catch {
    Write-Host "❌ Not authenticated with GCP. Please run:" -ForegroundColor Red
    Write-Host "   gcloud auth login" -ForegroundColor White
    Write-Host "   gcloud auth application-default login" -ForegroundColor White
    exit 1
}

# Verify Cloud SQL instance exists
Write-Host "2. Verifying Cloud SQL instance..." -ForegroundColor Yellow
try {
    $null = gcloud sql instances describe fincore-npe-db --quiet 2>$null
    Write-Host "✅ Cloud SQL instance verified" -ForegroundColor Green
} catch {
    Write-Host "❌ Cloud SQL instance 'fincore-npe-db' not found" -ForegroundColor Red
    exit 1
}

# Get database password from environment or prompt
if (-not $env:DB_PASSWORD) {
    Write-Host "3. Database password required..." -ForegroundColor Yellow
    $securePassword = Read-Host "Enter database password for user '$($env:DB_USER)'" -AsSecureString
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword))
}

# Build the application
Write-Host "4. Building application..." -ForegroundColor Yellow
$buildResult = mvn clean compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build successful" -ForegroundColor Green

# Test connection with built-in connector
Write-Host "5. Testing built-in Cloud SQL connector..." -ForegroundColor Yellow
Write-Host "   Profile: gcp-builtin" -ForegroundColor White
Write-Host "   Instance: $($env:CLOUD_SQL_INSTANCE)" -ForegroundColor White
Write-Host "   Database: $($env:DB_NAME)" -ForegroundColor White
Write-Host "   User: $($env:DB_USER)" -ForegroundColor White

# Start the application in test mode
Write-Host "   Starting application..." -ForegroundColor White
$job = Start-Job -ScriptBlock {
    mvn spring-boot:run "-Dspring-boot.run.profiles=gcp-builtin" -q
}

# Wait for application to start
Start-Sleep -Seconds 15

# Test health endpoint
Write-Host "6. Testing health endpoint..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 10
    if ($healthResponse.Content -like "*UP*") {
        Write-Host "✅ Health check passed - Built-in connector working!" -ForegroundColor Green
        
        # Test database connection indirectly via API
        Write-Host "7. Testing database connection via API..." -ForegroundColor Yellow
        $loginData = @{
            username = "admin"
            password = "Admin@123456"
        } | ConvertTo-Json
        
        try {
            $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
                -Method POST `
                -ContentType "application/json" `
                -Body $loginData `
                -UseBasicParsing `
                -TimeoutSec 10
            
            if ($loginResponse.Content -like "*token*") {
                Write-Host "✅ Database connection verified - Login successful!" -ForegroundColor Green
            } else {
                Write-Host "❌ Database connection test failed" -ForegroundColor Red
                Write-Host "Response: $($loginResponse.Content)" -ForegroundColor White
            }
        } catch {
            Write-Host "❌ Database connection test failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Health check failed" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Clean up
Write-Host "8. Cleaning up..." -ForegroundColor Yellow
Stop-Job -Job $job -Force 2>$null
Remove-Job -Job $job -Force 2>$null

Write-Host ""
Write-Host "🎯 Test Summary:" -ForegroundColor Cyan
Write-Host "   Built-in Cloud SQL Java connector tested" -ForegroundColor White
Write-Host "   Connection string: jdbc:mysql://google/$($env:DB_NAME)" -ForegroundColor White
Write-Host "   Socket factory: com.google.cloud.sql.mysql.SocketFactory" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "   1. If tests pass, update GitHub Actions to use gcp-builtin profile" -ForegroundColor White
Write-Host "   2. Remove public IP dependency from Cloud SQL instance" -ForegroundColor White
Write-Host "   3. Deploy to Cloud Run with built-in connector" -ForegroundColor White