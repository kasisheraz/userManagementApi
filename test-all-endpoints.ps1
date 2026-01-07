# FinCore API - Comprehensive Endpoint Testing Script
# Tests all endpoints in the correct order with dependency management

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
)

$ErrorActionPreference = "Continue"
$testResults = @()
$authToken = ""
$userId = ""
$organisationId = ""
$addressId = ""
$kycDocId = ""

# Helper function to make API calls
function Invoke-ApiTest {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [bool]$RequiresAuth = $false
    )
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "TEST: $Name" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Method: $Method $Endpoint" -ForegroundColor Yellow
    
    try {
        $requestHeaders = @{}
        if ($Headers) {
            foreach ($key in $Headers.Keys) {
                $requestHeaders[$key] = $Headers[$key]
            }
        }
        $requestHeaders["Content-Type"] = "application/json"
        
        if ($RequiresAuth -and $authToken) {
            $requestHeaders["Authorization"] = "Bearer $authToken"
        }
        
        $params = @{
            Uri = "$BaseUrl$Endpoint"
            Method = $Method
            Headers = $requestHeaders
            TimeoutSec = 30
        }
        
        if ($Body) {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
            Write-Host "Request Body: $($params["Body"])" -ForegroundColor Gray
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        
        Write-Host "✓ SUCCESS" -ForegroundColor Green
        Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Green
        
        $script:testResults += [PSCustomObject]@{
            Test = $Name
            Status = "PASS"
            StatusCode = "200"
            Response = ($response | ConvertTo-Json -Compress)
        }
        
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $errorBody = ""
        
        if ($_.ErrorDetails) {
            $errorBody = $_.ErrorDetails.Message
        }
        
        Write-Host "✗ FAILED" -ForegroundColor Red
        Write-Host "Status: $statusCode" -ForegroundColor Red
        Write-Host "Error: $errorBody" -ForegroundColor Red
        
        $script:testResults += [PSCustomObject]@{
            Test = $Name
            Status = "FAIL"
            StatusCode = $statusCode
            Response = $errorBody
        }
        
        return $null
    }
}

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Blue
Write-Host "║   FinCore API - Comprehensive Endpoint Test Suite        ║" -ForegroundColor Blue
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Blue
Write-Host "`nBase URL: $BaseUrl`n" -ForegroundColor Cyan

# ============================================
# 1. HEALTH CHECKS
# ============================================
Write-Host "`n▶ Phase 1: Health Checks" -ForegroundColor Magenta

Invoke-ApiTest -Name "Health Check" -Method "GET" -Endpoint "/actuator/health"
Invoke-ApiTest -Name "Info Check" -Method "GET" -Endpoint "/actuator/info"

# ============================================
# 2. AUTHENTICATION
# ============================================
Write-Host "`n▶ Phase 2: Authentication Flow" -ForegroundColor Magenta

# Request OTP
$otpResponse = Invoke-ApiTest -Name "Request OTP (Admin)" -Method "POST" -Endpoint "/api/auth/request-otp" -Body @{
    phoneNumber = "+1234567890"
}

if ($otpResponse -and $otpResponse.devOtp) {
    Write-Host "`nℹ️  OTP Code: $($otpResponse.devOtp)" -ForegroundColor Yellow
    
    Start-Sleep -Seconds 2
    
    # Verify OTP
    $authResponse = Invoke-ApiTest -Name "Verify OTP (Admin)" -Method "POST" -Endpoint "/api/auth/verify-otp" -Body @{
        phoneNumber = "+1234567890"
        otpCode = $otpResponse.devOtp
    }
    
    if ($authResponse -and $authResponse.token) {
        $script:authToken = $authResponse.token
        $script:userId = $authResponse.user.id
        Write-Host "`n✓ Authenticated! Token saved." -ForegroundColor Green
        Write-Host "User ID: $userId" -ForegroundColor Cyan
    }
}

# ============================================
# 3. USER MANAGEMENT
# ============================================
Write-Host "`n▶ Phase 3: User Management" -ForegroundColor Magenta

Invoke-ApiTest -Name "Get All Users" -Method "GET" -Endpoint "/api/users" -RequiresAuth $true
Invoke-ApiTest -Name "Get User by ID" -Method "GET" -Endpoint "/api/users/$userId" -RequiresAuth $true
Invoke-ApiTest -Name "Get Current User" -Method "GET" -Endpoint "/api/users/me" -RequiresAuth $true

# Create new user
$newUser = Invoke-ApiTest -Name "Create New User" -Method "POST" -Endpoint "/api/users" -RequiresAuth $true -Body @{
    phoneNumber = "+447123456789"
    email = "test.user@fincore.com"
    firstName = "Test"
    lastName = "User"
    roleId = 3
    status = "ACTIVE"
}

# ============================================
# 4. ROLE & PERMISSION MANAGEMENT
# ============================================
Write-Host "`n▶ Phase 4: Roles & Permissions" -ForegroundColor Magenta

Invoke-ApiTest -Name "Get All Roles" -Method "GET" -Endpoint "/api/users/roles" -RequiresAuth $true
Invoke-ApiTest -Name "Get All Permissions" -Method "GET" -Endpoint "/api/users/permissions" -RequiresAuth $true

# ============================================
# 5. ADDRESS MANAGEMENT
# ============================================
Write-Host "`n▶ Phase 5: Address Management" -ForegroundColor Magenta

# Create address
$newAddress = Invoke-ApiTest -Name "Create Address" -Method "POST" -Endpoint "/api/addresses" -RequiresAuth $true -Body @{
    typeCode = 1
    addressLine1 = "456 Test Street"
    addressLine2 = "Suite 100"
    postalCode = "SW1A 1AA"
    stateCode = "London"
    countryCode = "GB"
    cityName = "London"
}

if ($newAddress -and $newAddress.id) {
    $script:addressId = $newAddress.id
    Write-Host "Created Address ID: $addressId" -ForegroundColor Cyan
    
    Invoke-ApiTest -Name "Get All Addresses" -Method "GET" -Endpoint "/api/addresses" -RequiresAuth $true
    Invoke-ApiTest -Name "Get Address by ID" -Method "GET" -Endpoint "/api/addresses/$addressId" -RequiresAuth $true
    
    # Update address
    Invoke-ApiTest -Name "Update Address" -Method "PUT" -Endpoint "/api/addresses/$addressId" -RequiresAuth $true -Body @{
        typeCode = 1
        addressLine1 = "456 Test Street Updated"
        addressLine2 = "Suite 200"
        postalCode = "SW1A 1AA"
        stateCode = "London"
        countryCode = "GB"
        cityName = "London"
    }
}

# ============================================
# 6. ORGANISATION MANAGEMENT
# ============================================
Write-Host "`n▶ Phase 6: Organisation Management" -ForegroundColor Magenta

# Create organisation
$newOrg = Invoke-ApiTest -Name "Create Organisation" -Method "POST" -Endpoint "/api/organisations" -RequiresAuth $true -Body @{
    legalName = "Test Organisation Ltd"
    businessName = "Test Business"
    registrationNumber = "REG123456"
    organisationType = "LIMITED_COMPANY"
    businessDescription = "Test business for API testing"
    incorporationDate = "2024-01-01"
    countryOfIncorporation = "United Kingdom"
    hmrcMlrNumber = "XMLR123456"
    status = "PENDING"
    registeredAddress = @{
        typeCode = 1
        addressLine1 = "789 Business Park"
        postalCode = "EC2A 1AA"
        cityName = "London"
        countryCode = "GB"
    }
}

if ($newOrg -and $newOrg.id) {
    $script:organisationId = $newOrg.id
    Write-Host "Created Organisation ID: $organisationId" -ForegroundColor Cyan
    
    Invoke-ApiTest -Name "Get All Organisations" -Method "GET" -Endpoint "/api/organisations" -RequiresAuth $true
    Invoke-ApiTest -Name "Get Organisation by ID" -Method "GET" -Endpoint "/api/organisations/$organisationId" -RequiresAuth $true
    
    # Update organisation
    Invoke-ApiTest -Name "Update Organisation" -Method "PUT" -Endpoint "/api/organisations/$organisationId" -RequiresAuth $true -Body @{
        legalName = "Test Organisation Ltd - Updated"
        businessName = "Test Business Updated"
        registrationNumber = "REG123456"
        organisationType = "LIMITED_COMPANY"
        businessDescription = "Updated description"
        status = "ACTIVE"
    }
    
    Invoke-ApiTest -Name "Get Organisations by User" -Method "GET" -Endpoint "/api/organisations/user/$userId" -RequiresAuth $true
}

# ============================================
# 7. KYC DOCUMENT MANAGEMENT
# ============================================
Write-Host "`n▶ Phase 7: KYC Document Management" -ForegroundColor Magenta

if ($organisationId) {
    # Create KYC document
    $newKyc = Invoke-ApiTest -Name "Create KYC Document" -Method "POST" -Endpoint "/api/kyc-documents" -RequiresAuth $true -Body @{
        organisationId = $organisationId
        documentType = "CERTIFICATE_OF_INCORPORATION"
        fileName = "incorporation-cert.pdf"
        fileUrl = "https://example.com/docs/cert.pdf"
        status = "PENDING"
    }
    
    if ($newKyc -and $newKyc.id) {
        $script:kycDocId = $newKyc.id
        Write-Host "Created KYC Document ID: $kycDocId" -ForegroundColor Cyan
        
        Invoke-ApiTest -Name "Get KYC Documents by Organisation" -Method "GET" -Endpoint "/api/kyc-documents/organisation/$organisationId" -RequiresAuth $true
        Invoke-ApiTest -Name "Get KYC Document by ID" -Method "GET" -Endpoint "/api/kyc-documents/$kycDocId" -RequiresAuth $true
        
        # Update KYC document
        Invoke-ApiTest -Name "Update KYC Document" -Method "PUT" -Endpoint "/api/kyc-documents/$kycDocId" -RequiresAuth $true -Body @{
            organisationId = $organisationId
            documentType = "CERTIFICATE_OF_INCORPORATION"
            fileName = "incorporation-cert-updated.pdf"
            fileUrl = "https://example.com/docs/cert-v2.pdf"
            status = "VERIFIED"
        }
        
        # Verify KYC document
        Invoke-ApiTest -Name "Verify KYC Document" -Method "POST" -Endpoint "/api/kyc-documents/$kycDocId/verify" -RequiresAuth $true -Body @{
            status = "VERIFIED"
            reasonDescription = "Document verified successfully"
        }
    }
}

# ============================================
# SUMMARY
# ============================================
Write-Host "`n`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Blue
Write-Host "║                    TEST SUMMARY                           ║" -ForegroundColor Blue
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Blue

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$totalCount = $testResults.Count
$successRate = if ($totalCount -gt 0) { [math]::Round(($passCount/$totalCount)*100, 2) } else { 0 }

Write-Host "`nTotal Tests: $totalCount" -ForegroundColor Cyan
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Success Rate: $successRate%" -ForegroundColor Yellow
Write-Host ""

# Display results table
$testResults | Format-Table -Property Test, Status, StatusCode -AutoSize

# Save results to file
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportFile = "test-results-$timestamp.json"
$testResults | ConvertTo-Json -Depth 10 | Out-File $reportFile
Write-Host "`n📄 Detailed results saved to: $reportFile" -ForegroundColor Cyan

if ($failCount -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! 🎉" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Some tests failed. Review the errors above." -ForegroundColor Yellow
}
