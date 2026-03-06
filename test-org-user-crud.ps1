$serviceUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     ORGANIZATION and USER ENDPOINT TESTING            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Step 1: Authenticate
Write-Host "Step 1: Authenticating..." -ForegroundColor Yellow
try {
    $otpBody = @{ phoneNumber = $phoneNumber } | ConvertTo-Json
    $otpResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/request-otp" -Method POST -Body $otpBody -ContentType "application/json"
    $otp = $otpResponse.devOtp
    Write-Host "  ✓ OTP: $otp" -ForegroundColor Green
    
    $verifyBody = @{ phoneNumber = $phoneNumber; otp = $otp } | ConvertTo-Json
    $authResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/verify-otp" -Method POST -Body $verifyBody -ContentType "application/json"
    $token = if ($authResponse.token) { $authResponse.token } else { $authResponse.accessToken }
    Write-Host "  ✓ Token obtained" -ForegroundColor Green
    $headers = @{ 
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
} catch {
    Write-Host "  ✗ Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Test Organization Endpoints
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "ORGANIZATION CRUD OPERATIONS" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

# Test 1: GET All Organizations
Write-Host "[1] GET All Organizations: GET /api/organisations" -ForegroundColor White
try {
    $orgs = Invoke-RestMethod -Uri "$serviceUrl/api/organisations" -Method GET -Headers $headers
    Write-Host "  ✓ SUCCESS - Retrieved $($orgs.Count) organizations" -ForegroundColor Green
    Write-Host "  Response Type: $($orgs.GetType().Name)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message
    Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
    Write-Host "  Error: $errorBody" -ForegroundColor Red
}

# Test 2: CREATE Organization
Write-Host "`n[2] CREATE Organization: POST /api/organisations" -ForegroundColor White
$orgCreateBody = @{
    legalName = "Test Organization $(Get-Date -Format 'yyyyMMddHHmmss')"
    type = "LTD"
    status = "PENDING"
} | ConvertTo-Json

Write-Host "  Request Body:" -ForegroundColor Gray
Write-Host "  $orgCreateBody" -ForegroundColor Gray

try {
    $newOrg = Invoke-RestMethod -Uri "$serviceUrl/api/organisations" -Method POST -Body $orgCreateBody -Headers $headers
    $orgId = $newOrg.id
    if (-not $orgId) { $orgId = $newOrg.organisationId }
    if (-not $orgId) { $orgId = $newOrg.Organization_Identifier }
    
    Write-Host "  ✓ SUCCESS - Created organization" -ForegroundColor Green
    Write-Host "  Organization ID: $orgId" -ForegroundColor Gray
    Write-Host "  Response:" -ForegroundColor Gray
    $newOrg | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message
    Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
    Write-Host "  Error Details: $errorBody" -ForegroundColor Red
    Write-Host "  Full Exception: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: GET Organization by ID
if ($orgId) {
    Write-Host "`n[3] GET Organization by ID: GET /api/organisations/$orgId" -ForegroundColor White
    try {
        $org = Invoke-RestMethod -Uri "$serviceUrl/api/organisations/$orgId" -Method GET -Headers $headers
        Write-Host "  ✓ SUCCESS - Retrieved organization" -ForegroundColor Green
        Write-Host "  Name: $($org.legalName)" -ForegroundColor Gray
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }

    # Test 4: UPDATE Organization
    Write-Host "`n[4] UPDATE Organization: PUT /api/organisations/$orgId" -ForegroundColor White
    $orgUpdateBody = @{
        legalName = "Updated Test Organization"
        type = "LTD"
        status = "ACTIVE"
    } | ConvertTo-Json
    
    try {
        $updatedOrg = Invoke-RestMethod -Uri "$serviceUrl/api/organisations/$orgId" -Method PUT -Body $orgUpdateBody -Headers $headers
        Write-Host "  ✓ SUCCESS - Updated organization" -ForegroundColor Green
        Write-Host "  New Status: $($updatedOrg.status)" -ForegroundColor Gray
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }

    # Test 5: DELETE Organization
    Write-Host "`n[5] DELETE Organization: DELETE /api/organisations/$orgId" -ForegroundColor White
    try {
        Invoke-RestMethod -Uri "$serviceUrl/api/organisations/$orgId" -Method DELETE -Headers $headers
        Write-Host "  ✓ SUCCESS - Deleted organization" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }
}

# Step 3: Test User Endpoints
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "USER CRUD OPERATIONS" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

# Test 1: GET All Users
Write-Host "[1] GET All Users: GET /api/users" -ForegroundColor White
try {
    $users = Invoke-RestMethod -Uri "$serviceUrl/api/users" -Method GET -Headers $headers
    Write-Host "  ✓ SUCCESS - Retrieved $($users.Count) users" -ForegroundColor Green
    Write-Host "  Response Type: $($users.GetType().Name)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message
    Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
    Write-Host "  Error: $errorBody" -ForegroundColor Red
}

# Test 2: CREATE User
Write-Host "`n[2] CREATE User: POST /api/users" -ForegroundColor White
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$userCreateBody = @{
    phoneNumber = "+44123456$timestamp"
    firstName = "Test"
    lastName = "User"
    email = "test.user.$timestamp@example.com"
    roleId = 1
} | ConvertTo-Json

Write-Host "  Request Body:" -ForegroundColor Gray
Write-Host "  $userCreateBody" -ForegroundColor Gray

try {
    $newUser = Invoke-RestMethod -Uri "$serviceUrl/api/users" -Method POST -Body $userCreateBody -Headers $headers
    $userId = $newUser.userId
    if (-not $userId) { $userId = $newUser.id }
    if (-not $userId) { $userId = $newUser.User_Identifier }
    
    Write-Host "  ✓ SUCCESS - Created user" -ForegroundColor Green
    Write-Host "  User ID: $userId" -ForegroundColor Gray
    Write-Host "  Response:" -ForegroundColor Gray
    $newUser | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message
    Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
    Write-Host "  Error Details: $errorBody" -ForegroundColor Red
    Write-Host "  Full Exception: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: GET User by ID
if ($userId) {
    Write-Host "`n[3] GET User by ID: GET /api/users/$userId" -ForegroundColor White
    try {
        $user = Invoke-RestMethod -Uri "$serviceUrl/api/users/$userId" -Method GET -Headers $headers
        Write-Host "  ✓ SUCCESS - Retrieved user" -ForegroundColor Green
        Write-Host "  Name: $($user.firstName) $($user.lastName)" -ForegroundColor Gray
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }

    # Test 4: UPDATE User
    Write-Host "`n[4] UPDATE User: PUT /api/users/$userId" -ForegroundColor White
    $userUpdateBody = @{
        phoneNumber = "+44123456$timestamp"
        firstName = "Updated"
        lastName = "TestUser"
        email = "updated.test.$timestamp@example.com"
        roleId = 1
    } | ConvertTo-Json
    
    try {
        $updatedUser = Invoke-RestMethod -Uri "$serviceUrl/api/users/$userId" -Method PUT -Body $userUpdateBody -Headers $headers
        Write-Host "  ✓ SUCCESS - Updated user" -ForegroundColor Green
        Write-Host "  New Name: $($updatedUser.firstName) $($updatedUser.lastName)" -ForegroundColor Gray
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }

    # Test 5: DELETE User
    Write-Host "`n[5] DELETE User: DELETE /api/users/$userId" -ForegroundColor White
    try {
        Invoke-RestMethod -Uri "$serviceUrl/api/users/$userId" -Method DELETE -Headers $headers
        Write-Host "  ✓ SUCCESS - Deleted user" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-Host "  ✗ FAILED - HTTP $statusCode" -ForegroundColor Red
        Write-Host "  Error: $errorBody" -ForegroundColor Red
    }
}

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                  TESTING COMPLETE                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan
