# Check Cloud Run Logs for JWT Authentication Issues
$ErrorActionPreference = "Continue"

$API = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JWT AUTHENTICATION LOG ANALYSIS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Trigger a test request
Write-Host "Step 1: Triggering test request..." -ForegroundColor Yellow
try {
    $authBody = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
    $authResponse = Invoke-RestMethod -Uri "$API/api/auth/request-otp" -Method Post -Body $authBody -ContentType "application/json"
    $tokenResponse = Invoke-RestMethod -Uri "$API/api/auth/verify-otp" -Method Post -Body (@{ phoneNumber = "+1234567890"; otp = $authResponse.devOtp } | ConvertTo-Json) -ContentType "application/json"
    $token = $tokenResponse.token
    
    Write-Host "[OK] Got JWT token: $($token.Substring(0, 30))..." -ForegroundColor Green
    
    Write-Host "`nMaking protected request to trigger JWT filter..." -ForegroundColor Yellow
    try {
        $result = Invoke-RestMethod -Uri "$API/api/users" -Method Get -Headers @{"Authorization" = "Bearer $token"}
        Write-Host "[SUCCESS] Request worked! Got response." -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "[EXPECTED] Got HTTP $statusCode (this will help us see logs)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Failed to get token: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Fetching Cloud Run logs..." -ForegroundColor Yellow
Write-Host "Looking for JWT filter execution logs..." -ForegroundColor Gray
Write-Host ""

# Step 2: Fetch logs
try {
    $logs = gcloud run logs read fincore-npe-api --region=europe-west2 --limit=200 --format=json 2>&1 | ConvertFrom-Json
    
    if ($logs) {
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "  RELEVANT LOG ENTRIES" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        
        # Filter for JWT-related logs
        $jwtLogs = $logs | Where-Object { 
            $_.textPayload -like "*JWT*" -or 
            $_.textPayload -like "*Authentication*" -or
            $_.textPayload -like "*401*" -or
            $_.textPayload -like "*403*" -or
            $_.textPayload -like "*Bearer*" -or
            $_.jsonPayload.message -like "*JWT*"
        } | Select-Object -First 50
        
        if ($jwtLogs) {
            $jwtLogs | ForEach-Object {
                $timestamp = $_.timestamp
                $message = if ($_.textPayload) { $_.textPayload } else { $_.jsonPayload.message }
                
                if ($message -like "*JWT Filter running*") {
                    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Green
                    Write-Host "✅ $message" -ForegroundColor Green
                } elseif ($message -like "*Authentication set*") {
                    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Green
                    Write-Host "✅ $message" -ForegroundColor Green
                } elseif ($message -like "*valid: true*") {
                    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Green
                    Write-Host "✅ $message" -ForegroundColor Green
                } elseif ($message -like "*error*" -or $message -like "*failed*") {
                    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Red
                    Write-Host "❌ $message" -ForegroundColor Red
                } else {
                    Write-Host "[$timestamp] " -NoNewline -ForegroundColor Yellow
                    Write-Host "$message" -ForegroundColor White
                }
            }
            
            Write-Host ""
            Write-Host "========================================" -ForegroundColor Cyan
            Write-Host "  DIAGNOSIS" -ForegroundColor Cyan
            Write-Host "========================================" -ForegroundColor Cyan
            Write-Host ""
            
            $filterRunning = $jwtLogs | Where-Object { $_.textPayload -like "*JWT Filter running*" -or $_.jsonPayload.message -like "*JWT Filter running*" }
            $tokenValid = $jwtLogs | Where-Object { $_.textPayload -like "*JWT token valid: true*" -or $_.jsonPayload.message -like "*JWT token valid: true*" }
            $authSet = $jwtLogs | Where-Object { $_.textPayload -like "*Authentication set*" -or $_.jsonPayload.message -like "*Authentication set*" }
            
            if ($filterRunning) {
                Write-Host "✅ JWT Filter IS executing" -ForegroundColor Green
            } else {
                Write-Host "❌ JWT Filter NOT found in logs (might not be running)" -ForegroundColor Red
            }
            
            if ($tokenValid) {
                Write-Host "✅ JWT token validation succeeds" -ForegroundColor Green
            } else {
                Write-Host "❌ JWT token validation failing or not logged" -ForegroundColor Red
            }
            
            if ($authSet) {
                Write-Host "✅ Authentication is being set in SecurityContext" -ForegroundColor Green
            } else {
                Write-Host "❌ Authentication NOT being set (this is the problem!)" -ForegroundColor Red
            }
            
        } else {
            Write-Host "No JWT-related logs found." -ForegroundColor Yellow
            Write-Host ""
            Write-Host "This means either:" -ForegroundColor Yellow
            Write-Host "  1. The JWT filter is not running at all" -ForegroundColor White
            Write-Host "  2. Logs haven't been indexed yet (wait 30 seconds and try again)" -ForegroundColor White
            Write-Host "  3. The deployment with logging (079abf9) hasn't been deployed yet" -ForegroundColor White
        }
        
    } else {
        Write-Host "No logs returned. Trying alternative method..." -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "[ERROR] Could not fetch logs: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Try manually with:" -ForegroundColor Yellow
    Write-Host "  gcloud run logs read fincore-npe-api --region=europe-west2 --limit=100" -ForegroundColor White
    Write-Host ""
    Write-Host "Or view in console:" -ForegroundColor Yellow
    Write-Host "  https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ADDITIONAL DEBUGGING" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "View all recent logs:" -ForegroundColor Yellow
Write-Host "  gcloud run logs read fincore-npe-api --region=europe-west2 --limit=200" -ForegroundColor White
Write-Host ""
Write-Host "View logs in real-time:" -ForegroundColor Yellow
Write-Host "  gcloud run logs tail fincore-npe-api --region=europe-west2" -ForegroundColor White
Write-Host ""
Write-Host "View in GCP Console:" -ForegroundColor Yellow
Write-Host "  https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs" -ForegroundColor White
Write-Host ""
