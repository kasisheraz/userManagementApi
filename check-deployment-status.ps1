# Check Cloud Run Deployment Status
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CHECKING DEPLOYMENT STATUS  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$PROJECT = "fincore-platform"
$SERVICE = "fincore-npe-api"
$REGION = "europe-west2"

Write-Host "Fetching current Cloud Run revision..." -ForegroundColor Cyan
$revisionInfo = gcloud run revisions list --service=$SERVICE --region=$REGION --project=$PROJECT --format="table(SERVICE,REVISION,ACTIVE,DEPLOYED)" --limit=5

Write-Host $revisionInfo
Write-Host ""

Write-Host "Checking GitHub Actions workflows..." -ForegroundColor Cyan
Write-Host "Go to: https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor Yellow
Write-Host ""

Write-Host "Testing CORS headers..." -ForegroundColor Cyan
$testUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app/api/users"

try {
    $response = Invoke-WebRequest -Uri $testUrl -Method Options -Headers @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "GET"
        "Access-Control-Request-Headers" = "Authorization,Content-Type"
    } -UseBasicParsing
    
    Write-Host "[OK] CORS preflight successful" -ForegroundColor Green
    Write-Host "Response Headers:" -ForegroundColor Yellow
    $response.Headers | Format-Table -AutoSize
} catch {
    Write-Host "[X] CORS preflight failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "This suggests CORS deployment is not complete yet" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Checking service health..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health"
    Write-Host "[OK] Service is UP" -ForegroundColor Green
    Write-Host $health | ConvertTo-Json
} catch {
    Write-Host "[X] Service health check failed" -ForegroundColor Red
}

Write-Host ""
