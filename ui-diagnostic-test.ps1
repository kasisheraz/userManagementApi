# Organization and User CRUD Test Script
$serviceUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"

Write-Host "`n╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ORGANIZATION and USER ENDPOINT DIAGNOSTIC TEST   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Step 1: Authenticate
Write-Host "[AUTH] Authenticating..." -ForegroundColor Yellow
$otpBody = @{ phoneNumber = $phoneNumber } | ConvertTo-Json
$otpResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/request-otp" -Method POST -Body $otpBody -ContentType "application/json"
$otp = $otpResponse.devOtp

$verifyBody = @{ phoneNumber = $phoneNumber; otp = $otp } | ConvertTo-Json
$authResponse = Invoke-RestMethod -Uri "$serviceUrl/api/auth/verify-otp" -Method POST -Body $verifyBody -ContentType "application/json"
$token = if ($authResponse.token) { $authResponse.token } else { $authResponse.accessToken }

if (-not $token) {
    Write-Host "Authentication failed!" -ForegroundColor Red
    exit 1
}

$headers = @{ 
    "Authorization" = "Bearer $token"
}

Write-Host "✓ Authenticated successfully`n" -ForegroundColor Green

# ==================== ORGANIZATION TESTS ====================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "ORGANIZATION CRUD TESTING" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

# Test 1: Create Organization (CORRECT PAYLOAD)
Write-Host "[ORG-1] Creating Organization..." -ForegroundColor Yellow
$orgPayload = @{
    ownerId = 1
    legalName = "Test Corp Ltd $(Get-Date -Format 'yyyyMMdd-HHmmss')"
    organisationType = "LTD"
    businessName = "Test Corp"
    businessDescription = "Testing organization creation from UI"
} | ConvertTo-Json

Write-Host "Payload:" -ForegroundColor Gray
Write-Host $orgPayload -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$serviceUrl/api/organisations" `
        -Method POST `
        -Headers $headers `
        -Body $orgPayload `
        -ContentType "application/json"
    
    $orgData = $response.Content | ConvertFrom-Json
    $orgId = $orgData.organizationIdentifier
    if (-not $orgId) { $orgId = $orgData.id }
    if (-not $orgId) { $orgId = $orgData.Organisation_Identifier }
    
    Write-Host "✓ SUCCESS - HTTP $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  Organization ID: $orgId" -ForegroundColor Green
    Write-Host "  Response:" -ForegroundColor Gray
    Write-Host $response.Content -ForegroundColor Gray
} catch {
    Write-Host "✗ FAILED - HTTP $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    $errorBody = $reader.ReadToEnd()
    Write-Host "  Error Response:" -ForegroundColor Red
    Write-Host $errorBody -ForegroundColor Red
}

# Test 2: Get All Organizations
Write-Host "`n[ORG-2] Getting All Organizations..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$serviceUrl/api/organisations" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✓ SUCCESS - HTTP $($response.StatusCode)" -ForegroundColor Green
    $orgList = $response.Content | ConvertFrom-Json
    if ($orgList.content) {
        Write-Host "  Total Organizations: $($orgList.totalElements)" -ForegroundColor Green
    } else {
        Write-Host "  Organizations Count: $($orgList.Count)" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ FAILED - HTTP $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    $errorBody = $reader.ReadToEnd()
    Write-Host "  Error: $errorBody" -ForegroundColor Red
}

# ==================== USER TESTS ====================
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "USER CRUD TESTING" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

# Test 1: Create User (CORRECT PAYLOAD)
Write-Host "[USER-1] Creating User..." -ForegroundColor Yellow
$timestamp = Get-Date -Format 'HHmmss'
$userPayload = @{
    phoneNumber = "+4479$timestamp"
    firstName = "John"
    lastName = "Doe"
    email = "john.doe.$timestamp@example.com"
    role = "ADMIN"
} | ConvertTo-Json

Write-Host "Payload:" -ForegroundColor Gray
Write-Host $userPayload -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$serviceUrl/api/users" `
        -Method POST `
        -Headers $headers `
        -Body $userPayload `
        -ContentType "application/json"
    
    $userData = $response.Content | ConvertFrom-Json
    $userId = $userData.userId
    if (-not $userId) { $userId = $userData.id }
    if (-not $userId) { $userId = $userData.User_Identifier }
    
    Write-Host "✓ SUCCESS - HTTP $($response.StatusCode)" -ForegroundColor Green
    Write-Host "  User ID: $userId" -ForegroundColor Green
    Write-Host "  Response:" -ForegroundColor Gray
    Write-Host $response.Content -ForegroundColor Gray
} catch {
    Write-Host "✗ FAILED - HTTP $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    $errorBody = $reader.ReadToEnd()
    Write-Host "  Error Response:" -ForegroundColor Red
    Write-Host $errorBody -ForegroundColor Red
}

# Test 2: Get All Users
Write-Host "`n[USER-2] Getting All Users..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$serviceUrl/api/users" `
        -Method GET `
        -Headers $headers
    
    Write-Host "✓ SUCCESS - HTTP $($response.StatusCode)" -ForegroundColor Green
    $userList = $response.Content | ConvertFrom-Json
    Write-Host "  Total Users: $($userList.Count)" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED - HTTP $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    $errorBody = $reader.ReadToEnd()
    Write-Host "  Error: $errorBody" -ForegroundColor Red
}

Write-Host "`n╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              DIAGNOSTIC TEST COMPLETE              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Key Findings for UI Team:" -ForegroundColor Yellow
Write-Host "=========================" -ForegroundColor Yellow
Write-Host "Organization Endpoint:" -ForegroundColor White
Write-Host "  - URL: POST /api/organisations" -ForegroundColor Gray
Write-Host "  - Required Fields: ownerId (Long), legalName (String), organisationType (String)" -ForegroundColor Gray
Write-Host "  - Optional Fields: businessName, businessDescription, etc." -ForegroundColor Gray
Write-Host "" -ForegroundColor White
Write-Host "User Endpoint:" -ForegroundColor White
Write-Host "  - URL: POST /api/users" -ForegroundColor Gray
Write-Host "  - Required Fields: phoneNumber (String), firstName (String), lastName (String)" -ForegroundColor Gray
Write-Host "  - Optional Fields: email, role (String)" -ForegroundColor Gray
Write-Host "" -ForegroundColor White
