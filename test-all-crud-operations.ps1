# Comprehensive API CRUD Testing Script
# Tests all endpoints with Create, Read, Update, Delete operations

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  COMPREHENSIVE API CRUD TEST" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Authentication
Write-Host "1. AUTHENTICATION FLOW" -ForegroundColor White
Write-Host "   Requesting OTP..." -ForegroundColor Gray

$otpRequest = @{
    phoneNumber = "+44-7700-900123"
} | ConvertTo-Json

try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" -Method POST -ContentType "application/json" -Body $otpRequest
    $otp = $otpResponse.otp
    Write-Host "   ✓ OTP received: $otp" -ForegroundColor Green
    
    # Verify OTP
    $verifyRequest = @{
        phoneNumber = "+44-7700-900123"
        otp = $otp
    } | ConvertTo-Json
    
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" -Method POST -ContentType "application/json" -Body $verifyRequest
    $token = $authResponse.token
    Write-Host "   ✓ JWT Token obtained`n" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Authentication failed: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$testResults = @()

# Helper function to test endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Endpoint,
        [hashtable]$CreateData = $null,
        [hashtable]$UpdateData = $null,
        [string]$IdField = "id"
    )
    
    Write-Host "`n2. TESTING: $Name" -ForegroundColor White
    $result = @{
        Endpoint = $Name
        Read = $false
        Create = $false
        Update = $false
        Delete = $false
        Errors = @()
    }
    
    # READ (GET ALL)
    try {
        Write-Host "   [READ] GET $Endpoint" -ForegroundColor Gray
        $getResponse = Invoke-RestMethod -Uri "$baseUrl$Endpoint" -Method GET -Headers $headers
        $count = if ($getResponse -is [array]) { $getResponse.Count } else { 1 }
        Write-Host "   ✓ Read: Got $count records" -ForegroundColor Green
        $result.Read = $true
    } catch {
        Write-Host "   ✗ Read failed: $($_.Exception.Message)" -ForegroundColor Red
        $result.Errors += "Read: $($_.Exception.Message)"
    }
    
    # CREATE (POST)
    if ($CreateData) {
        try {
            Write-Host "   [CREATE] POST $Endpoint" -ForegroundColor Gray
            $createBody = $CreateData | ConvertTo-Json
            $createResponse = Invoke-RestMethod -Uri "$baseUrl$Endpoint" -Method POST -Headers $headers -Body $createBody
            $createdId = $createResponse.$IdField
            Write-Host "   ✓ Create: Created record with $IdField=$createdId" -ForegroundColor Green
            $result.Create = $true
            
            # UPDATE (PUT)
            if ($UpdateData -and $createdId) {
                try {
                    Write-Host "   [UPDATE] PUT $Endpoint/$createdId" -ForegroundColor Gray
                    $updateBody = $UpdateData | ConvertTo-Json
                    $updateResponse = Invoke-RestMethod -Uri "$baseUrl$Endpoint/$createdId" -Method PUT -Headers $headers -Body $updateBody
                    Write-Host "   ✓ Update: Updated record $createdId" -ForegroundColor Green
                    $result.Update = $true
                } catch {
                    Write-Host "   ✗ Update failed: $($_.Exception.Message)" -ForegroundColor Red
                    $result.Errors += "Update: $($_.Exception.Message)"
                }
            }
            
            # DELETE
            if ($createdId) {
                try {
                    Write-Host "   [DELETE] DELETE $Endpoint/$createdId" -ForegroundColor Gray
                    Invoke-RestMethod -Uri "$baseUrl$Endpoint/$createdId" -Method DELETE -Headers $headers
                    Write-Host "   ✓ Delete: Deleted record $createdId" -ForegroundColor Green
                    $result.Delete = $true
                } catch {
                    Write-Host "   ✗ Delete failed: $($_.Exception.Message)" -ForegroundColor Red
                    $result.Errors += "Delete: $($_.Exception.Message)"
                }
            }
        } catch {
            Write-Host "   ✗ Create failed: $($_.Exception.Message)" -ForegroundColor Red
            $result.Errors += "Create: $($_.Exception.Message)"
        }
    } else {
        Write-Host "   ⊘ Create/Update/Delete: No test data provided" -ForegroundColor Yellow
    }
    
    return $result
}

# Test Phase 1 APIs
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PHASE 1 APIs" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Users
$userCreate = @{
    phoneNumber = "+44-7700-900999"
    firstName = "Test"
    lastName = "User"
    email = "testuser@example.com"
}
$userUpdate = @{
    phoneNumber = "+44-7700-900999"
    firstName = "Updated"
    lastName = "User"
    email = "updated@example.com"
}
$testResults += Test-Endpoint -Name "Users" -Endpoint "/api/users" -CreateData $userCreate -UpdateData $userUpdate -IdField "userId"

# Addresses
$addressCreate = @{
    userId = 1
    addressLine1 = "123 Test St"
    city = "London"
    state = "London"
    country = "UK"
    postalCode = "SW1A 1AA"
    addressType = "HOME"
}
$addressUpdate = @{
    userId = 1
    addressLine1 = "456 Updated St"
    city = "London"
    state = "London"
    country = "UK"
    postalCode = "SW1A 2BB"
    addressType = "HOME"
}
$testResults += Test-Endpoint -Name "Addresses" -Endpoint "/api/addresses" -CreateData $addressCreate -UpdateData $addressUpdate -IdField "addressId"

# KYC Documents
$testResults += Test-Endpoint -Name "KYC Documents" -Endpoint "/api/kyc-documents"

# Test Phase 2 APIs
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PHASE 2 APIs" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Organizations
$orgCreate = @{
    name = "Test Organization"
    registrationNumber = "TEST123456"
    country = "UK"
    organisationType = "CORPORATION"
}
$orgUpdate = @{
    name = "Updated Organization"
    registrationNumber = "TEST123456"
    country = "UK"
    organisationType = "CORPORATION"
}
$testResults += Test-Endpoint -Name "Organizations" -Endpoint "/api/organizations" -CreateData $orgCreate -UpdateData $orgUpdate -IdField "organisationId"

# Questionnaires - TEST THE REPORTED ISSUE
Write-Host "`n   TESTING REPORTED ISSUE: 'Name is null' error" -ForegroundColor Yellow
$questionnaireCreate = @{
    name = "Test Questionnaire"
    description = "Testing questionnaire creation"
    version = "1.0"
    isActive = $true
}
$questionnaireUpdate = @{
    name = "Updated Questionnaire"
    description = "Updated description"
    version = "1.0"
    isActive = $true
}
$testResults += Test-Endpoint -Name "Questionnaires" -Endpoint "/api/questionnaires" -CreateData $questionnaireCreate -UpdateData $questionnaireUpdate -IdField "questionnaireId"

# Questions
$testResults += Test-Endpoint -Name "Questions" -Endpoint "/api/questions"

# KYC Verifications - TEST THE REPORTED ISSUE
Write-Host "`n   TESTING REPORTED ISSUE: 500 error" -ForegroundColor Yellow
$testResults += Test-Endpoint -Name "KYC Verifications" -Endpoint "/api/kyc-verifications"

# Customer Answers
$testResults += Test-Endpoint -Name "Customer Answers" -Endpoint "/api/customer-answers"

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$totalTests = $testResults.Count
$readSuccess = ($testResults | Where-Object { $_.Read }).Count
$createSuccess = ($testResults | Where-Object { $_.Create }).Count
$updateSuccess = ($testResults | Where-Object { $_.Update }).Count
$deleteSuccess = ($testResults | Where-Object { $_.Delete }).Count

Write-Host "`nEndpoints Tested: $totalTests" -ForegroundColor White
Write-Host "READ Operations: $readSuccess/$totalTests" -ForegroundColor $(if($readSuccess -eq $totalTests){'Green'}else{'Yellow'})
Write-Host "CREATE Operations: $createSuccess" -ForegroundColor $(if($createSuccess -gt 0){'Green'}else{'Yellow'})
Write-Host "UPDATE Operations: $updateSuccess" -ForegroundColor $(if($updateSuccess -gt 0){'Green'}else{'Yellow'})
Write-Host "DELETE Operations: $deleteSuccess" -ForegroundColor $(if($deleteSuccess -gt 0){'Green'}else{'Yellow'})

Write-Host "`n⚠️ ISSUES FOUND:" -ForegroundColor Yellow
$issuesFound = $false
foreach ($result in $testResults) {
    if ($result.Errors.Count -gt 0) {
        $issuesFound = $true
        Write-Host "`n$($result.Endpoint):" -ForegroundColor Red
        foreach ($error in $result.Errors) {
            Write-Host "  - $error" -ForegroundColor Red
        }
    }
}

if (-not $issuesFound) {
    Write-Host "  ✓ No issues found!" -ForegroundColor Green
}

Write-Host "`n"
