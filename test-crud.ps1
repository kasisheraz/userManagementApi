# Comprehensive API CRUD Testing Script
$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host ""
Write-Host "========================================"
Write-Host "  COMPREHENSIVE API CRUD TEST"
Write-Host "========================================"
Write-Host ""

# Step 1: Authentication
Write-Host "1. AUTHENTICATION FLOW"
$otpRequest = @{
    phoneNumber = "+1234567890"
} | ConvertTo-Json

try {
    $otpResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/request-otp" -Method POST -ContentType "application/json" -Body $otpRequest
    $otp = if ($otpResponse.devOtp) { $otpResponse.devOtp } else { $otpResponse.otp }
    Write-Host "   OK OTP received: $otp"
    
    $verifyRequest = @{
        phoneNumber = "+1234567890"
        otp = $otp
    } | ConvertTo-Json
    
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/verify-otp" -Method POST -ContentType "application/json" -Body $verifyRequest
    $token = $authResponse.token
    Write-Host "   OK JWT Token obtained"
    Write-Host ""
} catch {
    Write-Host "   FAILED Authentication: $_"
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$testResults = @()

# Test Phase 1 APIs
Write-Host "========================================"
Write-Host "  PHASE 1 APIs"
Write-Host "========================================"
Write-Host ""

# Test Users
Write-Host "2. TESTING: Users"
$result = @{
    Endpoint = "Users"
    Read = $false
    Create = $false
    Update = $false
    Delete = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/users" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

try {
    $userCreate = @{
        phoneNumber = "+44-7700-900999"
        firstName = "Test"
        lastName = "User"
        email = "testuser@example.com"
    } | ConvertTo-Json
    
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/users" -Method POST -Headers $headers -Body $userCreate
    $userId = $createResponse.userId
    Write-Host "   OK CREATE: Created user ID $userId"
    $result.Create = $true
    
    # Update
    $userUpdate = @{
        phoneNumber = "+44-7700-900999"
        firstName = "Updated"
        lastName = "User"
        email = "updated@example.com"
    } | ConvertTo-Json
    
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method PUT -Headers $headers -Body $userUpdate
    Write-Host "   OK UPDATE: Updated user ID $userId"
    $result.Update = $true
    
    # Delete
    Invoke-RestMethod -Uri "$baseUrl/api/users/$userId" -Method DELETE -Headers $headers
    Write-Host "   OK DELETE: Deleted user ID $userId"
    $result.Delete = $true
} catch {
    Write-Host "   FAILED CREATE/UPDATE/DELETE: $_"
    $result.Errors += "Create/Update/Delete: $_"
}

$testResults += $result

# Test Addresses
Write-Host ""
Write-Host "3. TESTING: Addresses"
$result = @{
    Endpoint = "Addresses"
    Read = $false
    Create = $false
    Update = $false
    Delete = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/addresses" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

try {
    $addressCreate = @{
        userId = 1
        addressLine1 = "123 Test St"
        city = "London"
        state = "London"
        country = "UK"
        postalCode = "SW1A 1AA"
        addressType = "HOME"
    } | ConvertTo-Json
    
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/addresses" -Method POST -Headers $headers -Body $addressCreate
    $addressId = $createResponse.addressId
    Write-Host "   OK CREATE: Created address ID $addressId"
    $result.Create = $true
    
    # Update
    $addressUpdate = @{
        userId = 1
        addressLine1 = "456 Updated St"
        city = "London"
        state = "London"
        country = "UK"
        postalCode = "SW1A 2BB"
        addressType = "HOME"
    } | ConvertTo-Json
    
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/addresses/$addressId" -Method PUT -Headers $headers -Body $addressUpdate
    Write-Host "   OK UPDATE: Updated address ID $addressId"
    $result.Update = $true
    
    # Delete
    Invoke-RestMethod -Uri "$baseUrl/api/addresses/$addressId" -Method DELETE -Headers $headers
    Write-Host "   OK DELETE: Deleted address ID $addressId"
    $result.Delete = $true
} catch {
    Write-Host "   FAILED CREATE/UPDATE/DELETE: $_"
    $result.Errors += "Create/Update/Delete: $_"
}

$testResults += $result

# Test KYC Documents
Write-Host ""
Write-Host "4. TESTING: KYC Documents"
$result = @{
    Endpoint = "KYC Documents"
    Read = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/kyc-documents" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

$testResults += $result

# Test Phase 2 APIs
Write-Host ""
Write-Host "========================================"
Write-Host "  PHASE 2 APIs"
Write-Host "========================================"
Write-Host ""

# Test Organizations
Write-Host "5. TESTING: Organizations"
$result = @{
    Endpoint = "Organizations"
    Read = $false
    Create = $false
    Update = $false
    Delete = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/organizations" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

try {
    $orgCreate = @{
        name = "Test Organization"
        registrationNumber = "TEST123456"
        country = "UK"
        organisationType = "CORPORATION"
    } | ConvertTo-Json
    
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/organizations" -Method POST -Headers $headers -Body $orgCreate
    $orgId = $createResponse.organisationId
    Write-Host "   OK CREATE: Created organization ID $orgId"
    $result.Create = $true
    
    # Update
    $orgUpdate = @{
        name = "Updated Organization"
        registrationNumber = "TEST123456"
        country = "UK"
        organisationType = "CORPORATION"
    } | ConvertTo-Json
    
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/organizations/$orgId" -Method PUT -Headers $headers -Body $orgUpdate
    Write-Host "   OK UPDATE: Updated organization ID $orgId"
    $result.Update = $true
    
    # Delete
    Invoke-RestMethod -Uri "$baseUrl/api/organizations/$orgId" -Method DELETE -Headers $headers
    Write-Host "   OK DELETE: Deleted organization ID $orgId"
    $result.Delete = $true
} catch {
    Write-Host "   FAILED CREATE/UPDATE/DELETE: $_"
    $result.Errors += "Create/Update/Delete: $_"
}

$testResults += $result

# Test Questionnaires - REPORTED ISSUE
Write-Host ""
Write-Host "6. TESTING: Questionnaires (REPORTED ISSUE: Name is null)"
$result = @{
    Endpoint = "Questionnaires"
    Read = $false
    Create = $false
    Update = $false
    Delete = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/questionnaires" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

try {
    $questionnaireCreate = @{
        name = "Test Questionnaire"
        description = "Testing questionnaire creation"
        version = "1.0"
        isActive = $true
    } | ConvertTo-Json
    
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/api/questionnaires" -Method POST -Headers $headers -Body $questionnaireCreate
    $questionnaireId = $createResponse.questionnaireId
    Write-Host "   OK CREATE: Created questionnaire ID $questionnaireId"
    $result.Create = $true
    
    # Update
    $questionnaireUpdate = @{
        name = "Updated Questionnaire"
        description = "Updated description"
        version = "1.0"
        isActive = $true
    } | ConvertTo-Json
    
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/questionnaires/$questionnaireId" -Method PUT -Headers $headers -Body $questionnaireUpdate
    Write-Host "   OK UPDATE: Updated questionnaire ID $questionnaireId"
    $result.Update = $true
    
    # Delete
    Invoke-RestMethod -Uri "$baseUrl/api/questionnaires/$questionnaireId" -Method DELETE -Headers $headers
    Write-Host "   OK DELETE: Deleted questionnaire ID $questionnaireId"
    $result.Delete = $true
} catch {
    Write-Host "   FAILED CREATE/UPDATE/DELETE: $_"
    $result.Errors += "Create/Update/Delete: $_"
}

$testResults += $result

# Test Questions
Write-Host ""
Write-Host "7. TESTING: Questions"
$result = @{
    Endpoint = "Questions"
    Read = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/questions" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

$testResults += $result

# Test KYC Verifications - REPORTED ISSUE
Write-Host ""
Write-Host "8. TESTING: KYC Verifications (REPORTED ISSUE: 500 error)"
$result = @{
    Endpoint = "KYC Verifications"
    Read = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/kyc-verifications" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

$testResults += $result

# Test Customer Answers
Write-Host ""
Write-Host "9. TESTING: Customer Answers"
$result = @{
    Endpoint = "Customer Answers"
    Read = $false
    Errors = @()
}

try {
    $getResponse = Invoke-RestMethod -Uri "$baseUrl/api/customer-answers" -Method GET -Headers $headers
    Write-Host "   OK READ: Got $($getResponse.Count) records"
    $result.Read = $true
} catch {
    Write-Host "   FAILED READ: $_"
    $result.Errors += "Read: $_"
}

$testResults += $result

# Summary
Write-Host ""
Write-Host "========================================"
Write-Host "  TEST SUMMARY"
Write-Host "========================================"
Write-Host ""

$totalTests = $testResults.Count
$readSuccess = ($testResults | Where-Object { $_.Read }).Count
$createSuccess = ($testResults | Where-Object { $_.Create }).Count
$updateSuccess = ($testResults | Where-Object { $_.Update }).Count
$deleteSuccess = ($testResults | Where-Object { $_.Delete }).Count

Write-Host "Endpoints Tested: $totalTests"
Write-Host "READ Operations: $readSuccess/$totalTests"
Write-Host "CREATE Operations: $createSuccess"
Write-Host "UPDATE Operations: $updateSuccess"
Write-Host "DELETE Operations: $deleteSuccess"
Write-Host ""

Write-Host "ISSUES FOUND:"
$issuesFound = $false
foreach ($result in $testResults) {
    if ($result.Errors.Count -gt 0) {
        $issuesFound = $true
        Write-Host ""
        Write-Host "$($result.Endpoint):"
        foreach ($error in $result.Errors) {
            Write-Host "  - $error"
        }
    }
}

if (-not $issuesFound) {
    Write-Host "  No issues found!"
}

Write-Host ""
