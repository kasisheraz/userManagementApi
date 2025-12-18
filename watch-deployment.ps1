# Monitor for Built-in Integration Deployment
Write-Host "=== Monitoring for New Built-in Integration Deployment ===" -ForegroundColor Green

$serviceUrl = "https://fincore-npe-api-lfd6ooarra-nw.a.run.app"
$maxAttempts = 20
$attempt = 0

Write-Host "Checking for new deployment every 30 seconds..." -ForegroundColor Cyan
Write-Host "Looking for built-in integration indicators in logs..." -ForegroundColor Yellow

do {
    $attempt++
    Write-Host "`n--- Attempt $attempt/$maxAttempts ---" -ForegroundColor Cyan
    
    # Check current revision
    $revisionInfo = gcloud run services describe fincore-npe-api --region=europe-west2 --project=project-07a61357-b791-4255-a9e --format="value(status.latestReadyRevisionName,metadata.annotations.'serving.knative.dev/lastModifier')" 2>$null
    Write-Host "Current revision: $revisionInfo" -ForegroundColor White
    
    # Test health endpoint
    try {
        $health = Invoke-RestMethod -Uri "$serviceUrl/actuator/health" -TimeoutSec 5 2>$null
        Write-Host "Health status: $($health.status)" -ForegroundColor Green
        
        # Try to get revision info to see if it's updated
        $headers = Invoke-WebRequest -Uri $serviceUrl/actuator/health -Method HEAD -TimeoutSec 5 2>$null
        $serverHeader = $headers.Headers['Server']
        Write-Host "Response headers: $serverHeader" -ForegroundColor Gray
        
    } catch {
        Write-Host "Service not responding (may be updating): $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "This could indicate deployment in progress!" -ForegroundColor Cyan
    }
    
    if ($attempt -lt $maxAttempts) {
        Write-Host "Waiting 30 seconds..." -ForegroundColor Gray
        Start-Sleep 30
    }
    
} while ($attempt -lt $maxAttempts)

Write-Host "`n=== Final Status ===" -ForegroundColor Green
Write-Host "Monitor GitHub Actions: https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Cyan
Write-Host "Monitor Cloud Run: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs?project=project-07a61357-b791-4255-a9e" -ForegroundColor Cyan