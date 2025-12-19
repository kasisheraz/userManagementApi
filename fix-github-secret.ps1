# GitHub Secret Update Guide for Windows
# Run this after updating the GitHub secret DB_USER from "root" to "fincore_app"

Write-Host "🔧 GITHUB SECRET FIX VERIFICATION" -ForegroundColor Yellow
Write-Host ""
Write-Host "✅ COMPLETED: Local database connection test with fincore_app user" -ForegroundColor Green
Write-Host ""
Write-Host "📝 REQUIRED ACTION:" -ForegroundColor Cyan
Write-Host "1. Go to: https://github.com/YOUR_USERNAME/userManagementApi/settings/secrets/actions" -ForegroundColor White
Write-Host "2. Click on: DB_USER" -ForegroundColor White
Write-Host "3. Update value from: root" -ForegroundColor Red
Write-Host "4. Change to: fincore_app" -ForegroundColor Green
Write-Host "5. Click: Update secret" -ForegroundColor White
Write-Host ""
Write-Host "🚀 TRIGGER DEPLOYMENT:" -ForegroundColor Yellow
Write-Host "Option 1: Push any commit to trigger GitHub Actions" -ForegroundColor White
Write-Host "Option 2: Go to Actions tab and manually trigger 'Deploy NPE' workflow" -ForegroundColor White
Write-Host ""
Write-Host "🔍 VERIFICATION:" -ForegroundColor Cyan
Write-Host "After deployment, test:" -ForegroundColor White
Write-Host "Invoke-WebRequest -Uri 'https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health'" -ForegroundColor White
Write-Host ""
Write-Host "Expected result: {`"status`":`"UP`",`"components`":{...}}" -ForegroundColor Green
Write-Host ""
Write-Host "If still failing, check logs with:" -ForegroundColor Yellow
Write-Host "gcloud run services logs read fincore-npe-api --region=europe-west2" -ForegroundColor White