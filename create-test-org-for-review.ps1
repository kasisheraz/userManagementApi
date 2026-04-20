# Script to create a test organization and submit it for admin review

$API_URL = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$USER_ID = 20

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Create Test Organization for Review" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Get JWT token
Write-Host "Step 1: Login to get JWT token" -ForegroundColor Yellow
Write-Host "Enter your phone number:"
$phoneNumber = Read-Host

Write-Host "Enter your password:"
$password = Read-Host -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

# Login request
$loginBody = @{
    phoneNumber = $phoneNumber
    password = $passwordPlain
} | ConvertTo-Json

Write-Host "Logging in..." -ForegroundColor Gray

try {
    $loginResponse = Invoke-RestMethod `
        -Uri "$API_URL/api/auth/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json"
    
    $token = $loginResponse.token
    $userRole = $loginResponse.user.role
    
    Write-Host "✅ Login successful!" -ForegroundColor Green
    Write-Host "   User ID: $USER_ID" -ForegroundColor Gray
    Write-Host "   Role: $userRole" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# Step 2: Create organization
Write-Host "Step 2: Creating test organization..." -ForegroundColor Yellow

$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$orgName = "Test Corp $randomNum"

$createOrgBody = @{
    ownerId = $USER_ID
    legalName = $orgName
    businessName = "Test Business $randomNum"
    organisationType = "PRIVATE"
    registrationNumber = "REG-$randomNum"
    companyNumber = "CN-$randomNum"
    countryOfIncorporation = "United Kingdom"
    businessDescription = "Test organization for approval workflow demonstration"
    registeredAddress = @{
        typeCode = 1
        addressLine1 = "123 Test Street"
        city = "London"
        postalCode = "SW1A 1AA"
        country = "United Kingdom"
    }
} | ConvertTo-Json -Depth 3

try {
    $createResponse = Invoke-RestMethod `
        -Uri "$API_URL/api/organizations" `
        -Method POST `
        -Body $createOrgBody `
        -ContentType "application/json" `
        -Headers @{ Authorization = "Bearer $token" }
    
    $orgId = $createResponse.id
    Write-Host "✅ Organization created successfully!" -ForegroundColor Green
    Write-Host "   Organization ID: $orgId" -ForegroundColor Gray
    Write-Host "   Name: $orgName" -ForegroundColor Gray
    Write-Host "   Status: $($createResponse.status)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Failed to create organization: $_" -ForegroundColor Red
    Write-Host $_.Exception.Response.StatusCode.value__ -ForegroundColor Red
    exit 1
}

# Step 3: Submit for review
Write-Host "Step 3: Submitting organization for admin review..." -ForegroundColor Yellow

try {
    $submitResponse = Invoke-RestMethod `
        -Uri "$API_URL/api/organizations/$orgId/submit" `
        -Method PUT `
        -Headers @{ Authorization = "Bearer $token" } `
        -ContentType "application/json"
    
    Write-Host "✅ Organization submitted for review!" -ForegroundColor Green
    Write-Host "   Organization ID: $orgId" -ForegroundColor Gray
    Write-Host "   Name: $($submitResponse.legalName)" -ForegroundColor Gray
    Write-Host "   Status: $($submitResponse.status)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "❌ Failed to submit for review: $_" -ForegroundColor Red
    exit 1
}

# Summary
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ Success!" -ForegroundColor Green  
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Test organization created and submitted:" -ForegroundColor White
Write-Host "  • ID: $orgId" -ForegroundColor White
Write-Host "  • Name: $orgName" -ForegroundColor White
Write-Host "  • Status: UNDER_REVIEW" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Login to the UI as ADMIN" -ForegroundColor White
Write-Host "  2. Navigate to Organizations page" -ForegroundColor White
Write-Host "  3. Filter by Status = 'UNDER_REVIEW'" -ForegroundColor White
Write-Host "  4. You'll see ✅ Approve and ❌ Reject buttons!" -ForegroundColor White
Write-Host ""
