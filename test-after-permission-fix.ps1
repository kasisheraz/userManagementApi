# Test Built-in Integration After Permission Fix
Write-Host "=== Testing Built-in Integration After DB Permission Fix ===" -ForegroundColor Green

Write-Host "`nChecking current deployment status..." -ForegroundColor Cyan
$latestRevision = gcloud run services describe fincore-npe-api --region=europe-west2 --project=project-07a61357-b791-4255-a9e --format="value(status.latestCreatedRevisionName)" 2>$null

if ($latestRevision) {
    Write-Host "Latest revision: $latestRevision" -ForegroundColor Yellow
    
    Write-Host "`nChecking revision status..." -ForegroundColor Cyan
    $revisionStatus = gcloud run revisions describe $latestRevision --region=europe-west2 --project=project-07a61357-b791-4255-a9e --format="value(status.conditions[].status)" 2>$null
    
    if ($revisionStatus -like "*True*") {
        Write-Host "✅ Revision is healthy!" -ForegroundColor Green
        
        Write-Host "`nTesting service endpoint..." -ForegroundColor Cyan
        try {
            $response = Invoke-RestMethod -Uri "https://fincore-npe-api-lfd6ooarra-nw.a.run.app/actuator/health" -TimeoutSec 10
            Write-Host "✅ Health check successful!" -ForegroundColor Green
            Write-Host "Status: $($response.status)" -ForegroundColor White
        } catch {
            Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⏳ Revision still starting up..." -ForegroundColor Yellow
        Write-Host "Wait a few more minutes and try again." -ForegroundColor Gray
    }
} else {
    Write-Host "❌ Could not get revision information" -ForegroundColor Red
}

Write-Host "`n=== What to expect after permission fix ===" -ForegroundColor Cyan
Write-Host "✅ Startup probe should succeed" -ForegroundColor Green
Write-Host "✅ Built-in Cloud SQL connector should connect" -ForegroundColor Green  
Write-Host "✅ Application should become healthy within 2-3 minutes" -ForegroundColor Green
Write-Host "❌ If still failing, check Cloud Run logs for other issues" -ForegroundColor Yellow