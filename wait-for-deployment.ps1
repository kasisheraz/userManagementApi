# Wait for deployment and test endpoints
$ErrorActionPreference = "Stop"

$API_BASE = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  WAITING FOR DEPLOYMENT  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Commit 7861ffb deployed: Fix 403 errors" -ForegroundColor Yellow
Write-Host " - Added Phase 2 API endpoints to SecurityConfig" -ForegroundColor White
Write-Host " - Enhanced JWT authentication logging" -ForegroundColor White
Write-Host " - Enabled DEBUG logging for security package" -ForegroundColor White
Write-Host ""

Write-Host "Monitoring deployment progress..." -ForegroundColor Cyan
Write-Host "Checking every 15 seconds (typical deployment: 3-5 minutes)" -ForegroundColor Gray
Write-Host ""

$startTime = Get-Date
$maxWaitMinutes = 10
$checkCount = 0

while ($true) {
    $checkCount++
    $elapsed = ((Get-Date) - $startTime).TotalMinutes
    
    if ($elapsed -gt $maxWaitMinutes) {
        Write-Host "[TIMEOUT] Deployment taking longer than $maxWaitMinutes minutes" -ForegroundColor Red
        Write-Host "Check manually: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/revisions" -ForegroundColor Yellow
        break
    }
    
    Write-Host "[$checkCount] Checking status... (${elapsed:N1} min elapsed)" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri "$API_BASE/actuator/health" -TimeoutSec 10 -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "[SUCCESS] Service is UP and responding!" -ForegroundColor Green
            Write-Host ""
            
            # Now test authentication
            Write-Host "Testing authentication..." -ForegroundColor Cyan
            try {
                $authBody = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
                $authResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/request-otp" -Method Post -Body $authBody -ContentType "application/json" -ErrorAction Stop
                
                Write-Host "[OK] OTP: $($authResponse.devOtp)" -ForegroundColor Green
                
                $verifyBody = @{ phoneNumber = "+1234567890"; otp = $authResponse.devOtp } | ConvertTo-Json
                $tokenResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json" -ErrorAction Stop
                $token = $tokenResponse.token
                
                Write-Host "[OK] JWT Token received" -ForegroundColor Green
                Write-Host ""
                
                # Test a few key endpoints
                $headers = @{
                    "Authorization" = "Bearer $token"
                    "Content-Type" = "application/json"
                }
                
                Write-Host "Testing endpoints with JWT token..." -ForegroundColor Cyan
                
                # Test Phase 1 endpoint
                try {
                    $users = Invoke-RestMethod -Uri "$API_BASE/api/users" -Method Get -Headers $headers -ErrorAction Stop
                    Write-Host "[OK] /api/users - SUCCESS" -ForegroundColor Green
                } catch {
                    $statusCode = $_.Exception.Response.StatusCode.value__
                    Write-Host "[X] /api/users - HTTP $statusCode" -ForegroundColor Red
                }
                
                # Test Phase 2 endpoints
                try {
                    $orgs = Invoke-RestMethod -Uri "$API_BASE/api/organisations" -Method Get -Headers $headers -ErrorAction Stop
                    Write-Host "[OK] /api/organisations - SUCCESS" -ForegroundColor Green
                } catch {
                    $statusCode = $_.Exception.Response.StatusCode.value__
                    Write-Host "[X] /api/organisations - HTTP $statusCode" -ForegroundColor Red
                }
                
                try {
                    $questions = Invoke-RestMethod -Uri "$API_BASE/api/v1/questions" -Method Get -Headers $headers -ErrorAction Stop
                    Write-Host "[OK] /api/v1/questions - SUCCESS" -ForegroundColor Green
                } catch {
                    $statusCode = $_.Exception.Response.StatusCode.value__
                    Write-Host "[X] /api/v1/questions - HTTP $statusCode" -ForegroundColor Red
                }
                
                try {
                    $kyc = Invoke-RestMethod -Uri "$API_BASE/api/v1/kyc-verification/user/1" -Method Get -Headers $headers -ErrorAction Stop
                    Write-Host "[OK] /api/v1/kyc-verification/user/1 - SUCCESS" -ForegroundColor Green
                } catch {
                    $statusCode = $_.Exception.Response.StatusCode.value__
                    Write-Host "[X] /api/v1/kyc-verification/user/1 - HTTP $statusCode" -ForegroundColor Red
                }
                
                Write-Host ""
                Write-Host "========================================" -ForegroundColor Cyan
                Write-Host "Deployment complete! Run .\test-correct-endpoints.ps1 for full test" -ForegroundColor Yellow
                Write-Host "========================================" -ForegroundColor Cyan
                
            } catch {
                Write-Host "[ERROR] Authentication test failed: $($_.Exception.Message)" -ForegroundColor Red
            }
            
            break
        }
    } catch {
        Write-Host "  ... not yet ready (Connection error or updating)" -ForegroundColor DarkGray
    }
    
    Start-Sleep -Seconds 15
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Run: .\test-correct-endpoints.ps1" -ForegroundColor White
Write-Host "2. If still 403 errors, check logs: gcloud run logs read fincore-npe-api --region=europe-west2 --limit=50" -ForegroundColor White
Write-Host ""
