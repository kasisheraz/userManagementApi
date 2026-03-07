# Test Complete JWT Authentication Flow
# This proves the backend JWT authentication is working correctly

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"

Write-Host "`n=== Testing Complete JWT Flow ===" -ForegroundColor Cyan

# Step 1: Request OTP
Write-Host "`n1. Requesting OTP..." -ForegroundColor Yellow
$otpRequest = @{
    phoneNumber = $phoneNumber
} | ConvertTo-Json

try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" `
        -Method POST `
        -Body $otpRequest `
        -ContentType "application/json"
    
    Write-Host "✅ OTP Request Successful" -ForegroundColor Green
    $otpCode = $otpResponse.devOtp
    Write-Host "   OTP Code: $otpCode" -ForegroundColor White
    Write-Host "   Message: $($otpResponse.message)" -ForegroundColor Gray
} catch {
    Write-Host "❌ OTP Request Failed: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Verify OTP and get JWT token
Write-Host "`n2. Verifying OTP and getting JWT token..." -ForegroundColor Yellow
$verifyRequest = @{
    phoneNumber = $phoneNumber
    otp = $otpCode
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" `
        -Method POST `
        -Body $verifyRequest `
        -ContentType "application/json"
    
    Write-Host "✅ OTP Verified Successfully" -ForegroundColor Green
    $jwtToken = $verifyResponse.accessToken
    Write-Host "   User: $($verifyResponse.user.firstName) $($verifyResponse.user.lastName)" -ForegroundColor Gray
    Write-Host "   JWT Token: $($jwtToken.Substring(0, 50))..." -ForegroundColor White
} catch {
    Write-Host "❌ OTP Verification Failed: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Test protected endpoint WITHOUT token (should fail)
Write-Host "`n3. Testing protected endpoint WITHOUT token (should fail with 403)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/system/protected-test" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "❌ SHOULD HAVE FAILED - Got response without token!" -ForegroundColor Red
    Write-Host "   Response: $($response | ConvertTo-Json)" -ForegroundColor White
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "✅ Correctly rejected without token (403 Forbidden)" -ForegroundColor Green
    } else {
        Write-Host "❌ Wrong error code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Step 4: Test protected endpoint WITH token (should succeed)
Write-Host "`n4. Testing protected endpoint WITH token (should succeed)..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $jwtToken"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/system/protected-test" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "✅ Successfully accessed protected endpoint WITH token!" -ForegroundColor Green
    Write-Host "   Response: $($response | ConvertTo-Json)" -ForegroundColor White
} catch {
    Write-Host "❌ Failed even with token: $_" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor White
}

# Step 5: Test /api/users endpoint WITH token
Write-Host "`n5. Testing /api/users endpoint WITH token..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $jwtToken"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/users" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "✅ Successfully fetched users!" -ForegroundColor Green
    Write-Host "   Total users: $($response.Count)" -ForegroundColor White
    if ($response.Count -gt 0) {
        Write-Host "   First user: $($response[0].firstName) $($response[0].lastName) ($($response[0].phoneNumber))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Failed to fetch users: $_" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor White
}

# Step 6: Test /api/organizations endpoint WITH token
Write-Host "`n6. Testing /api/organizations endpoint WITH token..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $jwtToken"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/organizations" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "✅ Successfully fetched organizations!" -ForegroundColor Green
    Write-Host "   Total organizations: $($response.Count)" -ForegroundColor White
    if ($response.Count -gt 0) {
        Write-Host "   First org: $($response[0].organizationName) ($($response[0].organizationType))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Failed to fetch organizations: $_" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor White
}

Write-Host "`n=== JWT Flow Test Complete ===" -ForegroundColor Cyan
Write-Host "`nCONCLUSION:" -ForegroundColor Yellow
Write-Host "If steps 4, 5, and 6 succeeded, the backend JWT authentication is WORKING CORRECTLY." -ForegroundColor White
Write-Host "The UI must include 'Authorization: Bearer <token>' header in all protected API requests." -ForegroundColor White
