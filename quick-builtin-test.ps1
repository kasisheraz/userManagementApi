# Quick Test for Cloud SQL Built-in Integration
Write-Host "=== Quick Cloud SQL Built-in Test ===" -ForegroundColor Green

# Set environment variables
$env:CLOUD_SQL_INSTANCE = "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "fincore_app"
$env:DB_PASSWORD = "FinCore2024Secure"
$env:PORT = "8080"

Write-Host "Environment configured for built-in integration" -ForegroundColor Cyan
Write-Host "Starting application with timeout..." -ForegroundColor Cyan

# Start application with timeout
$job = Start-Job -ScriptBlock {
    param($workingDir)
    Set-Location $workingDir
    mvn spring-boot:run "-Dspring-boot.run.profiles=gcp-builtin"
} -ArgumentList (Get-Location)

# Wait for 60 seconds to see startup logs
Write-Host "Waiting for startup (60 seconds)..." -ForegroundColor Yellow
$output = Receive-Job -Job $job -Wait -Timeout 60

if ($job.State -eq "Running") {
    Write-Host "✅ Application appears to be starting..." -ForegroundColor Green
    Write-Host "Output preview:" -ForegroundColor Cyan
    $output | Select-Object -Last 10
    
    # Stop the job
    Stop-Job $job -ErrorAction SilentlyContinue
    Remove-Job $job -ErrorAction SilentlyContinue
} else {
    Write-Host "❌ Application startup failed or completed quickly" -ForegroundColor Red
    Write-Host "Full output:" -ForegroundColor Yellow
    $output
}

Write-Host "`nTest complete. Check output above for connection status." -ForegroundColor Green