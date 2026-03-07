# Test JWT Authentication Debug
$ErrorActionPreference = "Stop"

$API_BASE = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "Waiting 4 minutes for deployment..." -ForegroundColor Yellow
Start-Sleep -Seconds 240

Write-Host "`n=== Step 1: Get JWT Token ===" -ForegroundColor Cyan
$authBody = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
$authResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/request-otp" -Method Post -Body $authBody -ContentType "application/json"
Write-Host "[OK] OTP: $($authResponse.devOtp)" -ForegroundColor Green

$verifyBody = @{ phoneNumber = "+1234567890"; otp = $authResponse.devOtp } | ConvertTo-Json
$tokenResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json"
$token = $tokenResponse.token
Write-Host "[OK] Token received: $($token.Substring(0, 20))..." -ForegroundColor Green

Write-Host "`n=== Step 2: Test Auth Debug Endpoint ===" -ForegroundColor Cyan
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$authTestResponse = Invoke-RestMethod -Uri "$API_BASE/api/system/auth-test" -Method Get -Headers $headers
Write-Host "`nAuthentication Status:" -ForegroundColor Yellow
Write-Host "  Has Auth Header: $($authTestResponse.hasAuthHeader)" -ForegroundColor $(if ($authTestResponse.hasAuthHeader) { "Green" } else { "Red" })
Write-Host "  Is Authenticated: $($authTestResponse.isAuthenticated)" -ForegroundColor $(if ($authTestResponse.isAuthenticated) { "Green" } else { "Red" })
Write-Host "  Principal: $($authTestResponse.principal)" -ForegroundColor White
Write-Host "  Authorities: $($authTestResponse.authorities)" -ForegroundColor White

Write-Host "`n=== Step 3: Test Protected Endpoint ===" -ForegroundColor Cyan
try {
    $users = Invoke-RestMethod -Uri "$API_BASE/api/users" -Method Get -Headers $headers
    Write-Host "[SUCCESS] Got $($users.Count) users" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "[FAILED] HTTP $statusCode" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== DIAGNOSIS ===" -ForegroundColor Cyan
if ($authTestResponse.isAuthenticated -eq $true) {
    Write-Host "✅ JWT Filter IS working - authentication set" -ForegroundColor Green
    Write-Host "✅ Problem is elsewhere (not JWT authentication)" -ForegroundColor Green
} else {
    Write-Host "❌ JWT Filter NOT working - authentication not set" -ForegroundColor Red
    Write-Host "❌ Filter might not be running or has errors" -ForegroundColor Red
}
