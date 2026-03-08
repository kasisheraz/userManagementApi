# Quick Deploy Script - Deploys from source (easier than Docker)
# GitHub Actions is not triggering, so use this to deploy manually

Write-Host "`n🚀 Quick Deploy to Cloud Run NPE`n" -ForegroundColor Cyan

# Get current commit
$commit = (git rev-parse --short HEAD)
Write-Host "Deploying commit: $commit`n" -ForegroundColor White

Write-Host "⚠️  This will take 3-5 minutes...`n" -ForegroundColor Yellow

# Deploy from source (Cloud Build will handle Maven + Docker)
gcloud run deploy fincore-npe-api `
    --source=. `
    --region=europe-west2 `
    --platform=managed `
    --allow-unauthenticated `
    --port=8080 `
    --memory=1Gi `
    --cpu=1 `
    --min-instances=1 `
    --max-instances=10 `
    --set-env-vars="SPRING_PROFILES_ACTIVE=npe,BUILD_NUMBER=$commit" `
    --vpc-connector=npe-connector `
    --vpc-egress=all-traffic `
    --timeout=300 `
    --quiet

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Deployment initiated!`n" -ForegroundColor Green
    Write-Host "Waiting 60 seconds for service to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 60
    
    Write-Host "`nVerifying deployment..." -ForegroundColor Cyan
    $info = Invoke-RestMethod -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/system/info"
    Write-Host "Build: $($info.build)" -ForegroundColor White
    Write-Host "Time: $($info.timestamp)`n" -ForegroundColor Gray
    
    if ($info.build -eq $commit) {
        Write-Host "🎉 SUCCESS! Build $commit is live!`n" -ForegroundColor Green
        Write-Host "Now run: .\test-all-phase2-apis.ps1`n" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️  Build mismatch - expected $commit, got $($info.build)`n" -ForegroundColor Yellow  
        Write-Host "Wait another minute and check again.`n" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n❌ Deployment failed!`n" -ForegroundColor Red
}
