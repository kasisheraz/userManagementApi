# Test Frontend Bug Fixes
# Tests the two CREATE operations that were failing

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TESTING FRONTEND BUG FIXES" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Authentication
Write-Host "1. Getting JWT Token..." -ForegroundColor White
$otpRequest = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" -Method POST -ContentType "application/json" -Body $otpRequest
    $otp = if ($otpResponse.devOtp) { $otpResponse.devOtp } else { $otpResponse.otp }
    
    $verifyRequest = @{ phoneNumber = "+1234567890"; otp = $otp } | ConvertTo-Json
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" -Method POST -ContentType "application/json" -Body $verifyRequest
    $token = $authResponse.token
    Write-Host "   ✓ Token obtained" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Authentication failed: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BUG FIX #1: Questionnaires CREATE" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Testing: POST /api/questionnaires with {name, description}" -ForegroundColor White
Write-Host ""

# Test 1: Questionnaires CREATE with {name, description}
$questionnaireData = @{
    name = "KYC Onboarding Questionnaire"
    description = "Questions for new customer onboarding"
    version = "1.0"
    isActive = $true
} | ConvertTo-Json

try {
    Write-Host "Request Body:" -ForegroundColor Gray
    Write-Host $questionnaireData -ForegroundColor Gray
    Write-Host ""
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/questionnaires" -Method POST -Headers $headers -Body $questionnaireData
    Write-Host "✓ SUCCESS: Questionnaire created!" -ForegroundColor Green
    Write-Host "   Question ID: $($response.questionId)" -ForegroundColor Green
    Write-Host "   Question Text: $($response.questionText)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BUG FIX #2: KYC Verifications CREATE" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Testing: POST /api/kyc-verifications (was /submit only)" -ForegroundColor White
Write-Host ""

# Test 2: KYC Verifications CREATE
$kycData = @{
    userId = 1
    verificationLevel = "STANDARD"
} | ConvertTo-Json

try {
    Write-Host "Request Body:" -ForegroundColor Gray
    Write-Host $kycData -ForegroundColor Gray
    Write-Host ""
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/kyc-verifications" -Method POST -Headers $headers -Body $kycData
    Write-Host "✓ SUCCESS: KYC Verification created!" -ForegroundColor Green
    Write-Host "   Verification ID: $($response.verificationId)" -ForegroundColor Green
    Write-Host "   User ID: $($response.userId)" -ForegroundColor Green
    Write-Host "   Level: $($response.verificationLevel)" -ForegroundColor Green
    Write-Host "   Status: $($response.verificationStatus)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST COMPLETE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Both frontend bugs should now be fixed!" -ForegroundColor Green
Write-Host ""
