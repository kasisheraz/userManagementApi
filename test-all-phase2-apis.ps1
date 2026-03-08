# Test All Phase 2 APIs
# Comprehensive test of all 6 fixed endpoints

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "  TESTING ALL PHASE 2 APIs - FULL JWT FLOW" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

# ============================================
# STEP 1: Get JWT Token
# ============================================
Write-Host "    Authentication Flow    `n" -ForegroundColor Yellow

Write-Host "[1] Requesting OTP..." -ForegroundColor White
$otpRequest = @{ phoneNumber = $phoneNumber } | ConvertTo-Json
try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" `
        -Method POST -Body $otpRequest -ContentType "application/json"
    $otpCode = $otpResponse.devOtp
    Write-Host "   OK  OTP: $otpCode`n" -ForegroundColor Green
} catch {
    Write-Host "   FAIL Failed: $_`n" -ForegroundColor Red
    exit 1
}

Write-Host "[2] Verifying OTP and getting JWT..." -ForegroundColor White
$verifyRequest = @{ phoneNumber = $phoneNumber; otp = $otpCode } | ConvertTo-Json
try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" `
        -Method POST -Body $verifyRequest -ContentType "application/json"
    $jwtToken = $verifyResponse.accessToken
    Write-Host "   OK  JWT Token: $($jwtToken.Substring(0, 40))...`n" -ForegroundColor Green
} catch {
    Write-Host "   FAIL Failed: $_`n" -ForegroundColor Red
    exit 1
}

$headers = @{ "Authorization" = "Bearer $jwtToken" }

# ============================================
# STEP 2: Test All Working APIs
# ============================================
Write-Host "`n    Testing Working Phase 1 APIs    `n" -ForegroundColor Yellow

$workingApis = @(
    @{ name = "Users"; endpoint = "/api/users" },
    @{ name = "Addresses"; endpoint = "/api/addresses" },
    @{ name = "KYC Documents"; endpoint = "/api/kyc-documents" }
)

foreach ($api in $workingApis) {
    Write-Host "[*] Testing $($api.name)..." -ForegroundColor White
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl$($api.endpoint)" `
            -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "  OK  SUCCESS - Got $($response.Count) records`n" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  FAIL FAILED - Status: $statusCode`n" -ForegroundColor Red
    }
}

# ============================================
# STEP 3: Test Previously Failing APIs
# ============================================
Write-Host "    Testing FIXED Phase 2 APIs    `n" -ForegroundColor Yellow

$fixedApis = @(
    @{ name = "Organizations"; endpoint = "/api/organizations"; oldPath = "/api/organisations" },
    @{ name = "Questionnaires"; endpoint = "/api/questionnaires"; oldPath = "/api/v1/questions" },
    @{ name = "Questions"; endpoint = "/api/questions"; oldPath = "NEW" },
    @{ name = "KYC Verifications"; endpoint = "/api/kyc-verifications"; oldPath = "/api/v1/kyc-verification" },
    @{ name = "Customer Answers"; endpoint = "/api/customer-answers"; oldPath = "/api/v1/answers" }
)

$successCount = 0
$failCount = 0

foreach ($api in $fixedApis) {
    Write-Host "[*] Testing $($api.name)..." -ForegroundColor White
    Write-Host "   Path: $($api.endpoint) (was: $($api.oldPath))" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl$($api.endpoint)" `
            -Method GET -Headers $headers -ErrorAction Stop
        
        Write-Host "   OK  SUCCESS - Got $($response.Count) records" -ForegroundColor Green
        $successCount++
        
        # Show first record details
        if ($response.Count -gt 0) {
            $first = $response[0]
            $keys = $first.PSObject.Properties.Name | Select-Object -First 3
            $preview = ($keys | ForEach-Object { "$_=$($first.$_)" }) -join ", "
            Write-Host "   Data Sample: $preview`n" -ForegroundColor Gray
        } else {
            Write-Host "   WARN No data found (table might be empty)`n" -ForegroundColor Yellow
        }
    } catch {
        $failCount++
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorMsg = $_.Exception.Message
        
        if ($statusCode -eq 404) {
            Write-Host "   FAIL FAILED - 404 Not Found (endpoint does not exist)" -ForegroundColor Red
        } elseif ($statusCode -eq 500) {
            Write-Host "   FAIL FAILED - 500 Internal Server Error" -ForegroundColor Red
            try {
                $errorBody = $_.ErrorDetails.Message | ConvertFrom-Json
                Write-Host "   Error: $($errorBody.message)" -ForegroundColor Red
            } catch {
                Write-Host "   Error: $errorMsg" -ForegroundColor Red
            }
        } elseif ($statusCode -eq 403) {
            Write-Host "   FAIL FAILED - 403 Forbidden (JWT auth issue)" -ForegroundColor Red
        } else {
            Write-Host "   FAIL FAILED - Status: $statusCode" -ForegroundColor Red
            Write-Host "   Error: $errorMsg" -ForegroundColor Red
        }
        Write-Host ""
    }
}

# ============================================
# SUMMARY
# ============================================
Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "                    TEST SUMMARY" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "Phase 2 APIs Fixed:" -ForegroundColor Yellow
Write-Host "  Success: $successCount / $($fixedApis.Count)" -ForegroundColor $(if ($successCount -eq $fixedApis.Count) { "Green" } else { "Yellow" })
Write-Host "  Failed:  $failCount / $($fixedApis.Count)`n" -ForegroundColor $(if ($failCount -eq 0) { "Gray" } else { "Red" })

if ($successCount -eq $fixedApis.Count) {
    Write-Host "SUCCESS! ALL APIS WORKING! Backend is fully operational!`n" -ForegroundColor Green
} elseif ($successCount -gt 0) {
    Write-Host "PARTIAL: Some APIs still having issues - check errors above`n" -ForegroundColor Yellow
} else {
    Write-Host "FAILED: All APIs still failing - deployment may not be complete yet`n" -ForegroundColor Red
}

Write-Host "============================================================`n" -ForegroundColor Cyan
