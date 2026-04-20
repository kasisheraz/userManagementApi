# Script to create a test organization using existing JWT token

$API_URL = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$USER_ID = 20  # Updated to match JWT token

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Create Test Organization for Review" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Get your JWT token from browser:" -ForegroundColor Yellow
Write-Host "  1. Open browser DevTools (F12)" -ForegroundColor Gray
Write-Host "  2. Go to Application tab -> Local Storage" -ForegroundColor Gray
Write-Host "  3. Find token or authToken key" -ForegroundColor Gray
Write-Host "  4. Copy the token value" -ForegroundColor Gray
Write-Host ""
Write-Host "Enter your JWT token:"
$token = Read-Host

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host "ERROR: Token cannot be empty!" -ForegroundColor Red
    exit 1
}

# Step 1: Create organization
Write-Host ""
Write-Host "Step 1: Creating test organization..." -ForegroundColor Yellow

$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$orgName = "Demo Corp $randomNum - Admin Approval Test"

$createOrgBody = @{
    ownerId = $USER_ID
    legalName = $orgName
    businessName = "Demo Business $randomNum"
    organisationType = "PRIVATE"
    registrationNumber = "REG-TEST-$randomNum"
    companyNumber = "CN-$randomNum"
    countryOfIncorporation = "United Kingdom"
    businessDescription = "Test organization created to demonstrate admin approval workflow"
    registeredAddress = @{
        typeCode = 1
        addressLine1 = "123 Demo Street"
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
    Write-Host "SUCCESS: Organization created!" -ForegroundColor Green
    Write-Host "   Organization ID: $orgId" -ForegroundColor Gray
    Write-Host "   Name: $orgName" -ForegroundColor Gray
    Write-Host "   Current Status: $($createResponse.status)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "ERROR: Failed to create organization" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# Step 2: Set status to UNDER_REVIEW
Write-Host "Step 2: Setting status to UNDER_REVIEW..." -ForegroundColor Yellow

try {
    $statusResponse = Invoke-RestMethod `
        -Uri "$API_URL/api/organizations/$orgId/status?status=UNDER_REVIEW" `
        -Method PATCH `
        -Headers @{ Authorization = "Bearer $token" } `
        -ContentType "application/json"
    
    Write-Host "SUCCESS: Status updated!" -ForegroundColor Green
    Write-Host "   Organization ID: $orgId" -ForegroundColor Gray
    Write-Host "   Name: $($statusResponse.legalName)" -ForegroundColor Gray
    Write-Host "   New Status: $($statusResponse.status)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "ERROR: Failed to update status" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "NOTE: Organization was created (ID: $orgId) but status could not be changed" -ForegroundColor Yellow
    Write-Host "   You can manually submit it for review through the UI" -ForegroundColor Yellow
    exit 0
}

# Summary
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SUCCESS!" -ForegroundColor Green  
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Test organization created and ready for approval:" -ForegroundColor White
Write-Host "  * Organization ID: $orgId" -ForegroundColor White
Write-Host "  * Name: $orgName" -ForegroundColor White
Write-Host "  * Status: UNDER_REVIEW" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps - Test Admin Approval:" -ForegroundColor Yellow
Write-Host "  1. Refresh your Organizations page" -ForegroundColor White
Write-Host "  2. Click the Filter icon (funnel)" -ForegroundColor White
Write-Host "  3. Set Status = UNDER_REVIEW" -ForegroundColor White
Write-Host "  4. Click Apply" -ForegroundColor White
Write-Host "  5. You will see Approve and Reject buttons!" -ForegroundColor Green
Write-Host "  6. Click the green checkmark to approve the organization" -ForegroundColor White
Write-Host ""
