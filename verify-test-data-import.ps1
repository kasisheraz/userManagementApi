# Verify Test Data Import
# Checks if all test data was successfully imported into Cloud SQL
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFY TEST DATA IMPORT  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$API_BASE = "https://fincore-npe-api-994490239798.europe-west2.run.app"

Write-Host "Step 1: Authenticating..." -ForegroundColor Cyan
$authBody = @{
    phoneNumber = "+1234567890"
} | ConvertTo-Json

$authResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/request-otp" -Method Post -Body $authBody -ContentType "application/json"
Write-Host "OTP requested. Dev OTP: $($authResponse.devOtp)" -ForegroundColor Yellow

$verifyBody = @{
    phoneNumber = "+1234567890"
    otp = $authResponse.devOtp
} | ConvertTo-Json

$tokenResponse = Invoke-RestMethod -Uri "$API_BASE/api/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json"
$token = $tokenResponse.token
Write-Host "[OK] Authenticated successfully" -ForegroundColor Green
Write-Host ""

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Step 2: Checking data counts..." -ForegroundColor Cyan
Write-Host ""

# Check Users
try {
    $users = Invoke-RestMethod -Uri "$API_BASE/api/users" -Method Get -Headers $headers
    $userCount = if ($users -is [array]) { $users.Count } else { 1 }
    Write-Host "[OK] Users: $userCount records" -ForegroundColor Green
    
    if ($userCount -ge 12) {
        Write-Host "  Expected: 12+ users - PASS" -ForegroundColor Green
    } else {
        Write-Host "  Expected: 12+ users, Found: $userCount - FAIL" -ForegroundColor Red
    }
} catch {
    Write-Host "[X] Users: Failed to fetch - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Check Organizations
try {
    $orgs = Invoke-RestMethod -Uri "$API_BASE/api/organisations" -Method Get -Headers $headers
    $orgCount = if ($orgs -is [array]) { $orgs.Count } else { 1 }
    Write-Host "[OK] Organizations: $orgCount records" -ForegroundColor Green
    
    if ($orgCount -ge 8) {
        Write-Host "  Expected: 8+ organizations - PASS" -ForegroundColor Green
    } else {
        Write-Host "  Expected: 8+ organizations, Found: $orgCount - FAIL" -ForegroundColor Red
    }
    
    # Show sample organizations
    Write-Host ""
    Write-Host "Sample Organizations:" -ForegroundColor Yellow
    if ($orgs -is [array]) {
        $orgs | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - ID: $($_.id), Name: $($_.legalName), Type: $($_.organisationType), Status: $($_.status)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "[X] Organizations: Failed to fetch - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Check Questionnaire Questions
try {
    $questions = Invoke-RestMethod -Uri "$API_BASE/api/v1/questions" -Method Get -Headers $headers
    $questionCount = if ($questions -is [array]) { $questions.Count } else { 1 }
    Write-Host "[OK] Questionnaire Questions: $questionCount records" -ForegroundColor Green
    
    if ($questionCount -ge 20) {
        Write-Host "  Expected: 20+ questions - PASS" -ForegroundColor Green
    } else {
        Write-Host "  Expected: 20+ questions, Found: $questionCount - FAIL" -ForegroundColor Red
    }
} catch {
    Write-Host "[X] Questions: Failed to fetch - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Check Roles
try {
    $roles = Invoke-RestMethod -Uri "$API_BASE/api/roles" -Method Get -Headers $headers
    $roleCount = if ($roles -is [array]) { $roles.Count } else { 1 }
    Write-Host "[OK] Roles: $roleCount records" -ForegroundColor Green
    
    if ($roleCount -ge 4) {
        Write-Host "  Expected: 4+ roles - PASS" -ForegroundColor Green
    } else {
        Write-Host "  Expected: 4+ roles, Found: $roleCount - FAIL" -ForegroundColor Red
    }
} catch {
    Write-Host "[X] Roles: Failed to fetch - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 3: Detailed User Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Show all users with details
try {
    $allUsers = Invoke-RestMethod -Uri "$API_BASE/api/users" -Method Get -Headers $headers
    
    Write-Host "Total Users: $($allUsers.Count)" -ForegroundColor Yellow
    Write-Host ""
    
    $allUsers | ForEach-Object {
        Write-Host "User ID: $($_.id)" -ForegroundColor Cyan
        Write-Host "  Name: $($_.firstName) $($_.lastName)" -ForegroundColor White
        Write-Host "  Phone: $($_.phoneNumber)" -ForegroundColor White
        Write-Host "  Email: $($_.email)" -ForegroundColor White
        Write-Host "  Role: $($_.role)" -ForegroundColor White
        Write-Host "  Status: $($_.statusDescription)" -ForegroundColor White
        Write-Host ""
    }
} catch {
    Write-Host "[X] Could not fetch detailed user list" -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Step 4: Test New User Phone Numbers" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$testPhones = @("+447911123456", "+447911123457", "+447911123460", "+447911123461")

foreach ($phone in $testPhones) {
    Write-Host "Testing: $phone" -ForegroundColor Yellow
    try {
        $otpBody = @{
            phoneNumber = $phone
        } | ConvertTo-Json
        
        $otpResp = Invoke-RestMethod -Uri "$API_BASE/api/auth/request-otp" -Method Post -Body $otpBody -ContentType "application/json"
        Write-Host "  [OK] OTP available (Dev OTP: $($otpResp.devOtp))" -ForegroundColor Green
    } catch {
        Write-Host "  [X] Failed - $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "  VERIFICATION COMPLETE  " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "- If all checks show 'PASS', test data was imported successfully" -ForegroundColor White
Write-Host "- If counts are low, some data may not have imported" -ForegroundColor White
Write-Host "- Test phone numbers should all return OTP if users exist" -ForegroundColor White
Write-Host ""
Write-Host "Next: Check UI CORS and API connectivity" -ForegroundColor Yellow
Write-Host ""
