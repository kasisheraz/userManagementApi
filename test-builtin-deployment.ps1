#!/usr/bin/env pwsh
# Quick test for built-in Cloud SQL connector deployment

Write-Host "🧪 Testing Built-in Cloud SQL Connector Deployment" -ForegroundColor Green
Write-Host "=" * 60 -ForegroundColor Gray
Write-Host ""

# Check if deployment is running
Write-Host "📡 Checking Cloud Run service status..." -ForegroundColor Cyan
$serviceStatus = gcloud run services describe user-management-api-npe `
    --region=europe-west2 `
    --project=project-07a61357-b791-4255-a9e `
    --format='value(status.conditions[0].status)' 2>$null

if ($serviceStatus -eq "True") {
    Write-Host "✅ Service is running" -ForegroundColor Green
    
    # Get service URL
    $serviceUrl = gcloud run services describe user-management-api-npe `
        --region=europe-west2 `
        --project=project-07a61357-b791-4255-a9e `
        --format='value(status.url)'
    
    Write-Host "🌐 Service URL: $serviceUrl" -ForegroundColor Yellow
    
    # Test health endpoint
    Write-Host ""
    Write-Host "🏥 Testing health endpoint..." -ForegroundColor Cyan
    try {
        $healthResponse = Invoke-WebRequest -Uri "$serviceUrl/actuator/health" -TimeoutSec 30
        Write-Host "✅ Health check: $($healthResponse.StatusCode)" -ForegroundColor Green
        Write-Host "📝 Response: $($healthResponse.Content)" -ForegroundColor White
    }
    catch {
        Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Test users endpoint
    Write-Host ""
    Write-Host "👥 Testing users endpoint..." -ForegroundColor Cyan
    try {
        $usersResponse = Invoke-WebRequest -Uri "$serviceUrl/users" -TimeoutSec 30
        Write-Host "✅ Users endpoint: $($usersResponse.StatusCode)" -ForegroundColor Green
        $users = $usersResponse.Content | ConvertFrom-Json
        Write-Host "📊 User count: $($users.Count)" -ForegroundColor White
    }
    catch {
        Write-Host "❌ Users endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   This might be expected if database connection is still being established" -ForegroundColor Gray
    }
    
} else {
    Write-Host "❌ Service is not ready. Status: $serviceStatus" -ForegroundColor Red
    Write-Host "🔍 Check deployment logs with:" -ForegroundColor Yellow
    Write-Host "   gcloud run services get-iam-policy user-management-api-npe --region=europe-west2" -ForegroundColor Gray
}

Write-Host ""
Write-Host "🏁 Built-in connector test complete!" -ForegroundColor Green