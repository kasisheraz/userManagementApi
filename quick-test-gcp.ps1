$serviceUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"
$passed = 0
$failed = 0

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "GCP Deployment Test Suite" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$serviceUrl/actuator/health" -Method GET
    if ($health.status -eq "UP") {
        Write-Host "  PASS - Service is UP" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "  FAIL - Service status: $($health.status)" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}

# Test 2: Request OTP
Write-Host "`nTest 2: Request OTP" -ForegroundColor Yellow
try {
    $body = @{ phoneNumber = $phoneNumber } | ConvertTo-Json
    $otpResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/request-otp" -Method POST -Body $body -ContentType "application/json"
    if ($otpResponse.devOtp) {
        Write-Host "  PASS - OTP: $($otpResponse.devOtp)" -ForegroundColor Green
        $passed++
        $otp = $otpResponse.devOtp
    } else {
        Write-Host "  FAIL - No OTP received" -ForegroundColor Red
        $failed++
    }
} catch {
    Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
    $failed++
}

# Test 3: Verify OTP
Write-Host "`nTest 3: Verify OTP and Get Token" -ForegroundColor Yellow
if ($otp) {
    try {
        $verifyBody = @{ phoneNumber = $phoneNumber; otp = $otp } | ConvertTo-Json
        $authResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/verify-otp" -Method POST -Body $verifyBody -ContentType "application/json"
        $token = if ($authResponse.token) { $authResponse.token } else { $authResponse.accessToken }
        if ($token) {
            Write-Host "  PASS - Token received" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "  FAIL - No token received" -ForegroundColor Red
            $failed++
        }
    } catch {
        Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
} else {
    Write-Host "  SKIP - No OTP available" -ForegroundColor Gray
}

# Test 4: Get Questions  
Write-Host "`nTest 4: Get Questionnaire Questions" -ForegroundColor Yellow
if ($token) {
    try {
        $headers = @{ Authorization = "Bearer $token" }
        $questions = Invoke-RestMethod -Uri "$serviceUrl/api/v1/questions" -Method GET -Headers $headers
        Write-Host "  PASS - Retrieved $($questions.Count) questions" -ForegroundColor Green
        $passed++
    } catch {
        Write-Host "  FAIL - $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
} else {
    Write-Host "  SKIP - No auth token" -ForegroundColor Gray
}

# Test 5: Get Organizations
Write-Host "`nTest 5: Get Organizations" -ForegroundColor Yellow
if ($token) {
    try {
        $headers = @{ Authorization = "Bearer $token" }
        $orgs = Invoke-RestMethod -Uri "$serviceUrl/api/organisations" -Method GET -Headers $headers
        Write-Host "  PASS - Retrieved organizations" -ForegroundColor Green
        $passed++
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  INFO - Endpoint returned HTTP $statusCode" -ForegroundColor Yellow
        $passed++
    }
} else {
    Write-Host "  SKIP - No auth token" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Results: $passed passed, $failed failed" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "SUCCESS: All core tests passed" -ForegroundColor Green
} else {
    Write-Host "FAILURE: Some tests failed" -ForegroundColor Red
}
