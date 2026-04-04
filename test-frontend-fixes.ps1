# Test Frontend Bug Fixes
$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host ""
Write-Host "========================================"
Write-Host "  TESTING FRONTEND BUG FIXES"
Write-Host "========================================"
Write-Host ""

# Authentication
Write-Host "1. Getting JWT Token..."
$otpRequest = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" -Method POST -ContentType "application/json" -Body $otpRequest
    $otp = if ($otpResponse.devOtp) { $otpResponse.devOtp } else { $otpResponse.otp }
    
    $verifyRequest = @{ phoneNumber = "+1234567890"; otp = $otp } | ConvertTo-Json
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" -Method POST -ContentType "application/json" -Body $verifyRequest
    $token = $authResponse.token
    Write-Host "   OK Token obtained"
} catch {
    Write-Host "   FAILED Authentication: $_"
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host ""
Write-Host "========================================"
Write-Host "  BUG FIX #1: Questionnaires CREATE"
Write-Host "========================================"
Write-Host ""

# Test 1: Questionnaires CREATE
$questionnaireData = @{
    name = "KYC Onboarding Questionnaire"
    description = "Questions for new customer onboarding"
    version = "1.0"
    isActive = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/questionnaires" -Method POST -Headers $headers -Body $questionnaireData
    Write-Host "OK SUCCESS: Questionnaire created!"
    Write-Host "   Question ID: $($response.questionId)"
    Write-Host "   Question Text: $($response.questionText)"
} catch {
    Write-Host "FAILED: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody"
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "  BUG FIX #2: KYC Verifications CREATE"
Write-Host "========================================"
Write-Host ""

# Test 2: KYC Verifications CREATE
$kycData = @{
    userId = 1
    verificationLevel = "STANDARD"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/kyc-verifications" -Method POST -Headers $headers -Body $kycData
    Write-Host "OK SUCCESS: KYC Verification created!"
    Write-Host "   Verification ID: $($response.verificationId)"
    Write-Host "   User ID: $($response.userId)"
    Write-Host "   Level: $($response.verificationLevel)"
    Write-Host "   Status: $($response.verificationStatus)"
} catch {
    Write-Host "FAILED: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody"
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "  TEST COMPLETE"
Write-Host "========================================"
Write-Host ""
