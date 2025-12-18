# Monitor Built-in Integration Deployment
Write-Host "=== Monitoring Built-in Cloud SQL Integration Deployment ===" -ForegroundColor Green

$serviceUrl = "https://fincore-npe-api-lfd6ooarra-nw.a.run.app"
Write-Host "Current service URL: $serviceUrl" -ForegroundColor Cyan

Write-Host "`n=== Checking Service Health ===" -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$serviceUrl/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "✅ Current deployment is healthy:" -ForegroundColor Green
    Write-Host ($healthResponse | ConvertTo-Json -Depth 2) -ForegroundColor White
} catch {
    Write-Host "⚠️ Health check failed (may be updating): $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n=== Deployment Status ===" -ForegroundColor Yellow
Write-Host "🚀 Built-in integration deployment triggered" -ForegroundColor Green
Write-Host "📦 Changes include:" -ForegroundColor Cyan
Write-Host "   - Socket Factory instead of Cloud SQL Proxy" -ForegroundColor White
Write-Host "   - gcp-builtin profile configuration" -ForegroundColor White  
Write-Host "   - Database permission fix step" -ForegroundColor White
Write-Host "   - CLOUD_SQL_INSTANCE environment variable" -ForegroundColor White

Write-Host "`n⏳ Monitor deployment at:" -ForegroundColor Cyan
Write-Host "GitHub Actions: https://github.com/kasisheraz/userManagementApi/actions" -ForegroundColor White
Write-Host "Cloud Run Logs: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs?project=project-07a61357-b791-4255-a9e" -ForegroundColor White

Write-Host "`n=== Expected Timeline ===" -ForegroundColor Cyan  
Write-Host "Build: ~3-5 minutes" -ForegroundColor White
Write-Host "Docker Push: ~2-3 minutes" -ForegroundColor White
Write-Host "Cloud Run Deploy: ~3-5 minutes" -ForegroundColor White
Write-Host "Total: ~10-15 minutes" -ForegroundColor White

Write-Host "`n🔍 Watch for these key indicators:" -ForegroundColor Green
Write-Host "✅ 'UserMgmtPool - Start completed' (Hikari connection pool)" -ForegroundColor Yellow
Write-Host "✅ 'Database connection is healthy'" -ForegroundColor Yellow  
Write-Host "✅ 'Started UserManagementApplication' (successful startup)" -ForegroundColor Yellow
Write-Host "❌ 'Access denied for user fincore_app' (permission issue)" -ForegroundColor Red