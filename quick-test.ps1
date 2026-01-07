# FinCore API - Quick Test Script
param([string]$BaseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app")

$token = ""
$userId = 1

function Test-Endpoint {
    param($Name, $Method, $Url, $Body = $null, $UseAuth = $false)
    
    Write-Host "`n>> Testing: $Name" -ForegroundColor Cyan
    try {
        $headers = @{"Content-Type"="application/json"}
        if ($UseAuth -and $token) { $headers["Authorization"] = "Bearer $token" }
        
        $params = @{Uri="$BaseUrl$Url"; Method=$Method; Headers=$headers}
        if ($Body) { $params["Body"] = ($Body | ConvertTo-Json) }
        
        $result = Invoke-RestMethod @params
        Write-Host "   PASS" -ForegroundColor Green
        return $result
    }
    catch {
        Write-Host "   FAIL: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host "========================================" -ForegroundColor Blue
Write-Host "FinCore API Test Suite" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue

# Test 1: Health Check
Test-Endpoint "Health Check" "GET" "/actuator/health"

# Test 2: Request OTP
$otp = Test-Endpoint "Request OTP" "POST" "/api/auth/request-otp" @{phoneNumber="+1234567890"}
if ($otp.devOtp) {
    Write-Host "   OTP: $($otp.devOtp)" -ForegroundColor Yellow
    
    # Test 3: Verify OTP
    $auth = Test-Endpoint "Verify OTP" "POST" "/api/auth/verify-otp" @{phoneNumber="+1234567890"; otpCode=$otp.devOtp}
    if ($auth.token) {
        $script:token = $auth.token
        $script:userId = $auth.user.id
        Write-Host "   Authenticated!" -ForegroundColor Green
    }
}

# Test 4: Get Users
Test-Endpoint "Get All Users" "GET" "/api/users" -UseAuth $true

# Test 5: Get User by ID
Test-Endpoint "Get User by ID" "GET" "/api/users/$userId" -UseAuth $true

# Test 6: Get Roles
Test-Endpoint "Get All Roles" "GET" "/api/users/roles" -UseAuth $true

# Test 7: Create Address
$addr = Test-Endpoint "Create Address" "POST" "/api/addresses" @{
    typeCode=1; addressLine1="Test St"; postalCode="EC1A 1AA"; cityName="London"; countryCode="GB"
} -UseAuth $true

if ($addr.id) {
    # Test 8: Get Address
    Test-Endpoint "Get Address by ID" "GET" "/api/addresses/$($addr.id)" -UseAuth $true
}

# Test 9: Get All Organisations
Test-Endpoint "Get All Organisations" "GET" "/api/organisations" -UseAuth $true

# Test 10: Create Organisation
$org = Test-Endpoint "Create Organisation" "POST" "/api/organisations" @{
    legalName="Test Corp Ltd"
    businessName="Test Business"
    registrationNumber="TEST123"
    organisationType="LIMITED_COMPANY"
    status="PENDING"
    registeredAddress=@{typeCode=1; addressLine1="Business St"; postalCode="EC2A 1AA"; cityName="London"; countryCode="GB"}
} -UseAuth $true

if ($org.id) {
    # Test 11: Get Organisation
    Test-Endpoint "Get Organisation by ID" "GET" "/api/organisations/$($org.id)" -UseAuth $true
    
    # Test 12: Create KYC Document
    $kyc = Test-Endpoint "Create KYC Document" "POST" "/api/kyc-documents" @{
        organisationId=$org.id
        documentType="CERTIFICATE_OF_INCORPORATION"
        fileName="cert.pdf"
        fileUrl="https://example.com/cert.pdf"
        status="PENDING"
    } -UseAuth $true
    
    if ($kyc.id) {
        # Test 13: Get KYC Documents
        Test-Endpoint "Get KYC Documents" "GET" "/api/kyc-documents/organisation/$($org.id)" -UseAuth $true
    }
}

Write-Host "`n========================================" -ForegroundColor Blue
Write-Host "Testing Complete!" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue
