# Health Check Script for Local Built-in Cloud SQL Testing
# This script tests the API endpoints after the application starts

Write-Host "=== Health Check for Local Cloud SQL Built-in Integration ===" -ForegroundColor Green

$BASE_URL = "http://localhost:8080"

Write-Host "Waiting for application to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "=== Testing Application Health ===" -ForegroundColor Cyan

# Test health endpoint
try {
    $healthResponse = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method Get
    Write-Host "✓ Health Check: " -NoNewline -ForegroundColor Green
    Write-Host $healthResponse.status -ForegroundColor White
} catch {
    Write-Host "✗ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test basic API endpoints
Write-Host ""
Write-Host "=== Testing API Endpoints ===" -ForegroundColor Cyan

# Test users endpoint
try {
    $usersResponse = Invoke-RestMethod -Uri "$BASE_URL/api/users" -Method Get
    Write-Host "✓ Users endpoint: Returned $($usersResponse.Count) users" -ForegroundColor Green
} catch {
    Write-Host "✗ Users endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test database connection specifically
Write-Host ""
Write-Host "=== Testing Database Connection ===" -ForegroundColor Cyan

try {
    # This will trigger a database query
    $response = Invoke-WebRequest -Uri "$BASE_URL/api/users" -Method Get
    if ($response.StatusCode -eq 200) {
        Write-Host "✓ Database connection successful - API responded with status 200" -ForegroundColor Green
    } else {
        Write-Host "⚠ Unexpected status code: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    if ($_.Exception.Message -like "*timeout*" -or $_.Exception.Message -like "*connection*") {
        Write-Host "✗ Database connection issue: $($_.Exception.Message)" -ForegroundColor Red
    } else {
        Write-Host "⚠ Other issue: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== Connection Test Complete ===" -ForegroundColor Green
Write-Host "Check the application logs for detailed connection information"