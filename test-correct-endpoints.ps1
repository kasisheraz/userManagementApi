# Correct API Endpoint Testing
# Tests all endpoints with their ACTUAL paths

$ErrorActionPreference = "Continue"
$API_BASE = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  COMPREHENSIVE API ENDPOINT TEST  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Authenticate
Write-Host "Step 1: Authentication..." -ForegroundColor Cyan
$authBody = @{ phoneNumber = "+1234567890" } | ConvertTo-Json
$authResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/request-otp" -Method Post -Body $authBody -ContentType "application/json"
Write-Host "[OK] OTP: $($authResponse.devOtp)" -ForegroundColor Green

$verifyBody = @{ phoneNumber = "+1234567890"; otp = $authResponse.devOtp } | ConvertTo-Json
$tokenResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json"
$token = $tokenResponse.token
Write-Host "[OK] Authenticated" -ForegroundColor Green
Write-Host ""

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Test Results
$results = @()

# Helper function to test endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [hashtable]$Headers
    )
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -Headers $Headers -ErrorAction Stop
        $count = if ($response -is [array]) { $response.Count } elseif ($response.content -is [array]) { $response.content.Count} elseif ($response.data -is [array]) { $response.data.Count } else { 1 }
        Write-Host "[OK] $Name - $count records" -ForegroundColor Green
        return @{ Name = $Name; Status = "PASS"; Count = $count; Error = $null }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "[X] $Name - HTTP $statusCode" -ForegroundColor Red
        return @{ Name = $Name; Status = "FAIL"; Count = 0; Error = "HTTP $statusCode" }
    }
}

Write-Host "Step 2: Testing ALL Endpoints..." -ForegroundColor Cyan
Write-Host ""

# Core Endpoints
Write-Host "Core Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "Users" "$API_BASE/api/users" $headers
$results += Test-Endpoint "Addresses" "$API_BASE/api/addresses" $headers
$results += Test-Endpoint "Roles" "$API_BASE/api/roles" $headers
Write-Host ""

# Organization Endpoints  
Write-Host "Organization Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "Organizations (GET /api/organisations)" "$API_BASE/api/organisations" $headers
$results += Test-Endpoint "Organizations by Owner (GET /api/organisations/owner/1)" "$API_BASE/api/organisations/owner/1" $headers
Write-Host ""

# Question/Questionnaire Endpoints
Write-Host "Questionnaire Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "Questions (GET /api/v1/questions)" "$API_BASE/api/v1/questions" $headers
$results += Test-Endpoint "Questions by Category (GET /api/v1/questions/category/BUSINESS_PURPOSE)" "$API_BASE/api/v1/questions/category/BUSINESS_PURPOSE" $headers
$results += Test-Endpoint "Active Questions (GET /api/v1/questions/active)" "$API_BASE/api/v1/questions/active" $headers
Write-Host ""

# Customer Answer Endpoints
Write-Host "Customer Answer Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "Answers for User 1 (GET /api/v1/answers/user/1)" "$API_BASE/api/v1/answers/user/1" $headers
$results += Test-Endpoint "Answers for Question 1 (GET /api/v1/answers/question/1)" "$API_BASE/api/v1/answers/question/1" $headers
Write-Host ""

# KYC Verification Endpoints
Write-Host "KYC Verification Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "KYC for User 1 (GET /api/v1/kyc-verification/user/1)" "$API_BASE/api/v1/kyc-verification/user/1" $headers
$results += Test-Endpoint "KYC by Status APPROVED (GET /api/v1/kyc-verification/status/APPROVED)" "$API_BASE/api/v1/kyc-verification/status/APPROVED" $headers
$results += Test-Endpoint "Expired KYC (GET /api/v1/kyc-verification/expired)" "$API_BASE/api/v1/kyc-verification/expired" $headers
Write-Host ""

# KYC Documents
Write-Host "KYC Document Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "KYC Documents (GET /api/kyc-documents)" "$API_BASE/api/kyc-documents" $headers
Write-Host ""

# AML Screening
Write-Host "AML Screening Endpoints:" -ForegroundColor Yellow
$results += Test-Endpoint "AML for User 1 (GET /api/v1/aml-screening/user/1)" "$API_BASE/api/v1/aml-screening/user/1" $headers
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$passCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
$totalCount = $results.Count

Write-Host "Total: $totalCount tests" -ForegroundColor White
Write-Host "Passed: $passCount tests" -ForegroundColor Green
Write-Host "Failed: $failCount tests" -ForegroundColor Red
Write-Host ""

if ($failCount -gt 0) {
    Write-Host "Failed Endpoints:" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Error)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "Passed Endpoints:" -ForegroundColor Green
$results | Where-Object { $_.Status -eq "PASS" } | ForEach-Object {
    Write-Host "  - $($_.Name): $($_.Count) records" -ForegroundColor Green
}
Write-Host ""

# Data Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DATA SUMMARY  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$passedResults = $results | Where-Object { $_.Status -eq "PASS" }
$totalRecords = ($passedResults | Measure-Object -Property Count -Sum).Sum

Write-Host "Total records across all endpoints: $totalRecords" -ForegroundColor Yellow
Write-Host ""

Write-Host "Recommendations:" -ForegroundColor Cyan
Write-Host "1. Check failed endpoints - may need database data or may not exist" -ForegroundColor White
Write-Host "2. For 404 errors: Endpoint path might be wrong or not implemented" -ForegroundColor White
Write-Host "3. For 500 errors: Check backend logs for stack traces" -ForegroundColor White
Write-Host "4. For empty results: Import test data using insert-test-data.sql" -ForegroundColor White
Write-Host ""
