# Trigger Manual Redeploy to Cloud Run
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Triggering Application Redeploy" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Option 1: Make a dummy commit to trigger GitHub Actions" -ForegroundColor Yellow
Write-Host "Option 2: Wait for GitHub Actions workflow to complete and re-run it" -ForegroundColor Yellow
Write-Host "Option 3: Manually redeploy latest image from Cloud Run console" -ForegroundColor Yellow
Write-Host ""

$choice = Read-Host "Select option (1, 2, or 3)"

if ($choice -eq "1") {
    Write-Host ""
    Write-Host "Creating dummy commit to trigger deployment..." -ForegroundColor Cyan
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "Schema updated: $timestamp" | Out-File -Append -FilePath ".deployment-trigger"
    
    git add .deployment-trigger
    git commit -m "chore: trigger redeploy after schema fix

- Fixed column name: screening_date -> screened_at
- Schema re-imported at $timestamp"
    
    Write-Host ""
    Write-Host "Pushing to trigger GitHub Actions..." -ForegroundColor Cyan
    git push origin main
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Push successful!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Monitor deployment:" -ForegroundColor Cyan
        Write-Host "https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Gray
    }
}
elseif ($choice -eq "2") {
    Write-Host ""
    Write-Host "To re-run the failed workflow:" -ForegroundColor Cyan
    Write-Host "1. Go to: https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor White
    Write-Host "2. Click on the latest failed workflow" -ForegroundColor White
    Write-Host "3. Click 'Re-run jobs' button" -ForegroundColor White
    Write-Host "4. Select 'Re-run all jobs'" -ForegroundColor White
}
elseif ($choice -eq "3") {
    Write-Host ""
    Write-Host "To manually redeploy from Cloud Run:" -ForegroundColor Cyan
    Write-Host "1. Go to: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api" -ForegroundColor White
    Write-Host "2. Click 'EDIT & DEPLOY NEW REVISION'" -ForegroundColor White
    Write-Host "3. Don't change anything, just click 'DEPLOY'" -ForegroundColor White
    Write-Host "   (This will redeploy the same image with new DB connection)" -ForegroundColor White
}
else {
    Write-Host "Invalid option!" -ForegroundColor Red
}

Write-Host ""
