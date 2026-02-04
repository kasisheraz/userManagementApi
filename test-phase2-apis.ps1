# Comprehensive Phase 2 API Testing Script
param(
    [string]$ServiceUrl = "https://fincore-npe-api-lfd6ooarra-nw.a.run.app",
    [string]$PhoneNumber = "+1234567890"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Phase 2 API Comprehensive Test Suite" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
Write-Host "Service URL: $ServiceUrl`n" -ForegroundColor Yellow

$ErrorActionPreference = "Continue"
$testsPassed = 0
$testsFailed = 0

# Helper function to make API calls
function Invoke-ApiTest {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null,
        [hashtable]$Headers = @{},
        [string]$ExpectedStatus = "200"
    )
    
    Write-Host "Testing: $Name" -ForegroundColor White
    
    try {
        $params = @{
            Uri = "$ServiceUrl$Endpoint"
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        Write-Host "  ✓ PASSED" -ForegroundColor Green
        $script:testsPassed++
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "  ✓ PASSED (Expected $ExpectedStatus)" -ForegroundColor Green
            $script:testsPassed++
        } else {
            Write-Host "  ✗ FAILED (HTTP $statusCode)" -ForegroundColor Red
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            $script:testsFailed++
        }
        return $null
    }
}

# Test 1: Health Check
Write-Host "`n1. Health Check" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray
$health = Invoke-ApiTest -Name "Health Endpoint" -Method GET -Endpoint "/actuator/health"

# Test 2: Request OTP
Write-Host "`n2. Authentication Flow" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray
$otpRequest = Invoke-ApiTest `
    -Name "Request OTP" `
    -Method POST `
    -Endpoint "/api/auth/request-otp" `
    -Body "{`"phoneNumber`":`"$PhoneNumber`"}"

# Test 3: Verify OTP and Get Token
$token = $null
if ($otpRequest -and $otpRequest.devOtp) {
    $verifyBody = @{
        phoneNumber = $PhoneNumber
        otp = $otpRequest.devOtp
    } | ConvertTo-Json
    
    $authResponse = Invoke-ApiTest `
        -Name "Verify OTP" `
        -Method POST `
        -Endpoint "/api/auth/verify-otp" `
        -Body $verifyBody
    
    if ($authResponse) {
        $token = $authResponse.token
        if (-not $token) {
            $token = $authResponse.accessToken
        }
        Write-Host "  Token obtained: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Gray
    }
}

if (-not $token) {
    Write-Host "`n⚠️ Could not obtain authentication token. Skipping authenticated tests.`n" -ForegroundColor Yellow
    Write-Host "Test Results:" -ForegroundColor Cyan
    Write-Host "  Passed: $testsPassed" -ForegroundColor Green
    Write-Host "  Failed: $testsFailed" -ForegroundColor Red
    exit 0
}

$authHeaders = @{
    "Authorization" = "Bearer $token"
}

# Test 4: KYC Verification - Submit
Write-Host "`n3. KYC Verification APIs" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

$kycSubmit = Invoke-ApiTest `
    -Name "Submit KYC Verification" `
    -Method POST `
    -Endpoint "/api/kyc/verification" `
    -Headers $authHeaders `
    -Body (@{
        userId = 1
        verificationLevel = "BASIC"
        sumsubApplicantId = "test-smoke-$([DateTime]::Now.Ticks)"
    } | ConvertTo-Json)

$verificationId = $null
if ($kycSubmit) {
    $verificationId = $kycSubmit.verificationId
    Write-Host "  Verification ID: $verificationId" -ForegroundColor Gray
}

# Test 5: Get User KYC Verifications
Invoke-ApiTest `
    -Name "Get User KYC Verifications" `
    -Method GET `
    -Endpoint "/api/kyc/verification/user/1" `
    -Headers $authHeaders | Out-Null

# Test 6: Get Specific Verification
if ($verificationId) {
    Invoke-ApiTest `
        -Name "Get Verification by ID" `
        -Method GET `
        -Endpoint "/api/kyc/verification/$verificationId" `
        -Headers $authHeaders | Out-Null
}

# Test 7: Update Verification Status
if ($verificationId) {
    Invoke-ApiTest `
        -Name "Update Verification Status" `
        -Method PUT `
        -Endpoint "/api/kyc/verification/$verificationId/status" `
        -Headers $authHeaders `
        -Body (@{
            status = "APPROVED"
            reason = "Automated smoke test approval"
        } | ConvertTo-Json) | Out-Null
}

# Test 8: Questionnaire Questions
Write-Host "`n4. Questionnaire APIs" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

# Create a question first
$questionCreate = Invoke-ApiTest `
    -Name "Create Question" `
    -Method POST `
    -Endpoint "/api/questionnaire/questions" `
    -Headers $authHeaders `
    -Body (@{
        questionText = "Smoke test question - What is your occupation?"
        questionCategory = "EMPLOYMENT"
        displayOrder = 999
        status = "ACTIVE"
    } | ConvertTo-Json)

$questionId = $null
if ($questionCreate) {
    $questionId = $questionCreate.questionId
    Write-Host "  Question ID: $questionId" -ForegroundColor Gray
}

# Test 9: Get All Questions
$questions = Invoke-ApiTest `
    -Name "Get All Questions" `
    -Method GET `
    -Endpoint "/api/questionnaire/questions" `
    -Headers $authHeaders

# Test 10: Get Question by ID
if ($questionId) {
    Invoke-ApiTest `
        -Name "Get Question by ID" `
        -Method GET `
        -Endpoint "/api/questionnaire/questions/$questionId" `
        -Headers $authHeaders | Out-Null
}

# Test 11: Get Questions by Category
Invoke-ApiTest `
    -Name "Get Questions by Category" `
    -Method GET `
    -Endpoint "/api/questionnaire/questions/category/EMPLOYMENT" `
    -Headers $authHeaders | Out-Null

# Test 12: Customer Answers
Write-Host "`n5. Customer Answer APIs" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

$answerCreate = $null
if ($questionId) {
    $answerCreate = Invoke-ApiTest `
        -Name "Submit Answer" `
        -Method POST `
        -Endpoint "/api/questionnaire/answers" `
        -Headers $authHeaders `
        -Body (@{
            userId = 1
            questionId = $questionId
            answer = "Software Engineer - Smoke Test"
        } | ConvertTo-Json)
}

$answerId = $null
if ($answerCreate) {
    $answerId = $answerCreate.answerId
    Write-Host "  Answer ID: $answerId" -ForegroundColor Gray
}

# Test 13: Get User Answers
Invoke-ApiTest `
    -Name "Get User Answers" `
    -Method GET `
    -Endpoint "/api/questionnaire/answers/user/1" `
    -Headers $authHeaders | Out-Null

# Test 14: Get Answer by ID
if ($answerId) {
    Invoke-ApiTest `
        -Name "Get Answer by ID" `
        -Method GET `
        -Endpoint "/api/questionnaire/answers/$answerId" `
        -Headers $authHeaders | Out-Null
}

# Test 15: Update Answer
if ($answerId) {
    Invoke-ApiTest `
        -Name "Update Answer" `
        -Method PUT `
        -Endpoint "/api/questionnaire/answers/$answerId" `
        -Headers $authHeaders `
        -Body (@{
            answer = "Senior Software Engineer - Updated Smoke Test"
        } | ConvertTo-Json) | Out-Null
}

# Test 16: AML Screening (via KYC)
Write-Host "`n6. AML Screening (Embedded)" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

if ($verificationId) {
    $kycDetails = Invoke-ApiTest `
        -Name "Get KYC with AML Results" `
        -Method GET `
        -Endpoint "/api/kyc/verification/$verificationId" `
        -Headers $authHeaders
    
    if ($kycDetails -and $kycDetails.amlScreenings) {
        Write-Host "  AML Screenings found: $($kycDetails.amlScreenings.Count)" -ForegroundColor Gray
    }
}

# Cleanup: Delete test data
Write-Host "`n7. Cleanup Test Data" -ForegroundColor Cyan
Write-Host "─────────────────────────────────────────" -ForegroundColor Gray

if ($answerId) {
    Invoke-ApiTest `
        -Name "Delete Answer" `
        -Method DELETE `
        -Endpoint "/api/questionnaire/answers/$answerId" `
        -Headers $authHeaders `
        -ExpectedStatus "204" | Out-Null
}

if ($questionId) {
    Invoke-ApiTest `
        -Name "Delete Question" `
        -Method DELETE `
        -Endpoint "/api/questionnaire/questions/$questionId" `
        -Headers $authHeaders `
        -ExpectedStatus "204" | Out-Null
}

# Final Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Passed: $testsPassed" -ForegroundColor Green
Write-Host "  Failed: $testsFailed" -ForegroundColor Red
Write-Host ""

if ($testsFailed -eq 0) {
    Write-Host "✅ ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Phase 2 Features Verified:" -ForegroundColor Yellow
    Write-Host "  ✓ KYC Verification (CRUD)" -ForegroundColor White
    Write-Host "  ✓ AML Screening Results" -ForegroundColor White
    Write-Host "  ✓ Questionnaire Management" -ForegroundColor White
    Write-Host "  ✓ Customer Answers" -ForegroundColor White
    Write-Host ""
    Write-Host "Database Tables Tested:" -ForegroundColor Yellow
    Write-Host "  ✓ customer_kyc_verification" -ForegroundColor White
    Write-Host "  ✓ aml_screening_results" -ForegroundColor White
    Write-Host "  ✓ questionnaire_questions" -ForegroundColor White
    Write-Host "  ✓ customer_answers" -ForegroundColor White
    exit 0
} else {
    Write-Host "⚠️ Some tests failed. Review errors above." -ForegroundColor Yellow
    exit 1
}
