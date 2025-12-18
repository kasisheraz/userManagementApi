Write-Host "=== Testing Cloud SQL Built-in Integration ===" -ForegroundColor Green

# Set environment
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "fincore_app" 
$env:DB_PASSWORD = "FinCore2024Secure"
$env:PORT = "8080"

Write-Host "Starting application for 30 seconds to check connection..." -ForegroundColor Cyan

# Start in background and capture output
Start-Process powershell -ArgumentList "-Command", "mvn spring-boot:run -Dspring-boot.run.profiles=gcp-builtin" -WindowStyle Hidden -PassThru | Out-Null

# Wait 30 seconds
Start-Sleep 30

# Check if application is running
$javaProcess = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcess) {
    Write-Host "✅ Java application is running" -ForegroundColor Green
    Write-Host "PID: $($javaProcess.Id)" -ForegroundColor Cyan
    
    # Test the health endpoint
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/health" -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✅ Health check successful: $($response.StatusCode)" -ForegroundColor Green
        Write-Host $response.Content
    } catch {
        Write-Host "⚠️ Health check failed (app may still be starting): $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    # Stop the application
    Write-Host "`nStopping application..." -ForegroundColor Yellow
    $javaProcess | Stop-Process -Force
    Write-Host "✅ Application stopped" -ForegroundColor Green
} else {
    Write-Host "❌ Application not found running" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green