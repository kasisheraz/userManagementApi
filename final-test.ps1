# Final Comprehensive API Test
# Run this after deployment completes

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$passCount = 0
$failCount = 0

function Test-API {
    param($Name, $Method, $Uri, $Body = $null, $Token = $null)
    
    Write-Host "`n>> $Name" -ForegroundColor Cyan
    try {
        $headers = @{"Content-Type"="application/json"}
        if ($Token) { $headers["Authorization"] = "Bearer $Token" }
        
        $params = @{Uri=$Uri; Method=$Method; Headers=$headers}
        if ($Body) { $params["Body"] = $Body }
        
        $result = Invoke-RestMethod @params
        Write-Host "   ✓ PASS" -ForegroundColor Green
        $script:passCount++
        return $result
    } catch {
        Write-Host "   ✗ FAIL: $($_.ErrorDetails.Message)" -ForegroundColor Red
        $script:failCount++
        return $null
    }
}

Write-Host "========================================" -ForegroundColor Blue
Write-Host "FinCore API - Final Test Suite" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue

# Health Check
Test-API "Health Check" "GET" "$baseUrl/actuator/health"

# Authentication
$otp = Test-API "Request OTP" "POST" "$baseUrl/api/auth/request-otp" '{"phoneNumber":"+1234567890"}'
if ($otp.devOtp) {
    Write-Host "   OTP: $($otp.devOtp)" -ForegroundColor Yellow
    $auth = Test-API "Verify OTP" "POST" "$baseUrl/api/auth/verify-otp" "{`"phoneNumber`":`"+1234567890`",`"otpCode`":`"$($otp.devOtp)`"}"
    $token = $auth.token
    
    # User Management
    Test-API "Get All Users" "GET" "$baseUrl/api/users" -Token $token
    Test-API "Get User by ID" "GET" "$baseUrl/api/users/1" -Token $token
    Test-API "Get All Roles" "GET" "$baseUrl/api/users/roles" -Token $token
    
    # Address Management
    $addr = Test-API "Create Address" "POST" "$baseUrl/api/addresses" '{"typeCode":1,"addressLine1":"Test St","postalCode":"EC1A","cityName":"London","countryCode":"GB"}' -Token $token
    if ($addr.id) {
        Test-API "Get Address" "GET" "$baseUrl/api/addresses/$($addr.id)" -Token $token
    }
    
    # Organisation Management
    $org = Test-API "Create Organisation" "POST" "$baseUrl/api/organisations" '{"legalName":"Test Corp","businessName":"Test","registrationNumber":"T123","organisationType":"LIMITED_COMPANY","status":"PENDING","registeredAddress":{"typeCode":1,"addressLine1":"Biz St","postalCode":"EC2A","cityName":"London","countryCode":"GB"}}' -Token $token
    if ($org.id) {
        Test-API "Get Organisation" "GET" "$baseUrl/api/organisations/$($org.id)" -Token $token
        Test-API "Get All Organisations" "GET" "$baseUrl/api/organisations" -Token $token
    }
}

Write-Host "`n========================================" -ForegroundColor Blue
Write-Host "RESULTS: $passCount Passed, $failCount Failed" -ForegroundColor $(if($failCount -eq 0){"Green"}else{"Yellow"})
Write-Host "========================================`n" -ForegroundColor Blue
