# =====================================================
# Comprehensive CRUD Testing Script
# Tests Users and Organizations with new roles
# =====================================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminPhone = "+1234567890",
    [string]$AdminPassword = "password123"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API CRUD Operations Test" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Helper function to make API calls
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Token = $null,
        [object]$Body = $null
    )
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    $params = @{
        Uri = "$BaseUrl$Endpoint"
        Method = $Method
        Headers = $headers
    }
    
    if ($Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }
    
    try {
        $response = Invoke-RestMethod @params
        return $response
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        return $null
    }
}

# =====================================================
# STEP 1: Authenticate as Admin
# =====================================================
Write-Host "`n[STEP 1] Authenticating as Admin..." -ForegroundColor Yellow

$authBody = @{
    phoneNumber = $AdminPhone
    password = $AdminPassword
}

$authResponse = Invoke-ApiRequest -Method POST -Endpoint "/api/auth/login" -Body $authBody

if (-not $authResponse) {
    Write-Host "FAILED: Could not authenticate" -ForegroundColor Red
    exit 1
}

$token = $authResponse.token
$adminUser = $authResponse.user

Write-Host "SUCCESS: Authenticated as $($adminUser.firstName) $($adminUser.lastName)" -ForegroundColor Green
Write-Host "  Role: $($adminUser.role)" -ForegroundColor Gray
Write-Host "  Token: $($token.Substring(0, 20))..." -ForegroundColor Gray

# =====================================================
# STEP 2: Get All Roles
# =====================================================
Write-Host "`n[STEP 2] Fetching available roles..." -ForegroundColor Yellow

$roles = Invoke-ApiRequest -Method GET -Endpoint "/api/roles" -Token $token

if ($roles) {
    Write-Host "SUCCESS: Found $($roles.Count) roles" -ForegroundColor Green
    foreach ($role in $roles) {
        Write-Host "  - $($role.name): $($role.description)" -ForegroundColor Gray
    }
} else {
    Write-Host "WARNING: Could not fetch roles" -ForegroundColor Red
}

# =====================================================
# STEP 3: Create Business User
# =====================================================
Write-Host "`n[STEP 3] Creating a Business User..." -ForegroundColor Yellow

$businessUserData = @{
    firstName = "John"
    middleName = "Michael"
    lastName = "Doe"
    email = "john.doe@test.com"
    phoneNumber = "+447700900$(Get-Random -Minimum 100 -Maximum 999)"
    dateOfBirth = "1990-05-15"
    role = "Business User"
    residentialAddress = @{
        typeCode = 1
        addressLine1 = "123 Main Street"
        addressLine2 = "Apartment 4B"
        city = "London"
        stateCode = "Greater London"
        postalCode = "SW1A 1AA"
        country = "United Kingdom"
    }
    postalAddress = @{
        typeCode = 2
        addressLine1 = "PO Box 456"
        city = "London"
        stateCode = "Greater London"
        postalCode = "EC1A 1BB"
        country = "United Kingdom"
    }
}

$createdBusinessUser = Invoke-ApiRequest -Method POST -Endpoint "/api/users" -Token $token -Body $businessUserData

if ($createdBusinessUser) {
    Write-Host "SUCCESS: Created Business User" -ForegroundColor Green
    Write-Host "  ID: $($createdBusinessUser.id)" -ForegroundColor Gray
    Write-Host "  Name: $($createdBusinessUser.firstName) $($createdBusinessUser.lastName)" -ForegroundColor Gray
    Write-Host "  Role: $($createdBusinessUser.role)" -ForegroundColor Gray
    Write-Host "  Status: $($createdBusinessUser.statusDescription)" -ForegroundColor Gray
    Write-Host "  Phone: $($createdBusinessUser.phoneNumber)" -ForegroundColor Gray
} else {
    Write-Host "FAILED: Could not create Business User" -ForegroundColor Red
}

# =====================================================
# STEP 4: Create Operational User
# =====================================================
Write-Host "`n[STEP 4] Creating an Operational User..." -ForegroundColor Yellow

$operationalUserData = @{
    firstName = "Jane"
    middleName = "Elizabeth"
    lastName = "Smith"
    email = "jane.smith@test.com"
    phoneNumber = "+447700900$(Get-Random -Minimum 100 -Maximum 999)"
    dateOfBirth = "1988-08-20"
    role = "Operational"
    residentialAddress = @{
        typeCode = 1
        addressLine1 = "456 Oak Avenue"
        city = "Manchester"
        stateCode = "Greater Manchester"
        postalCode = "M1 1AA"
        country = "United Kingdom"
    }
}

$createdOperationalUser = Invoke-ApiRequest -Method POST -Endpoint "/api/users" -Token $token -Body $operationalUserData

if ($createdOperationalUser) {
    Write-Host "SUCCESS: Created Operational User" -ForegroundColor Green
    Write-Host "  ID: $($createdOperationalUser.id)" -ForegroundColor Gray
    Write-Host "  Name: $($createdOperationalUser.firstName) $($createdOperationalUser.lastName)" -ForegroundColor Gray
    Write-Host "  Role: $($createdOperationalUser.role)" -ForegroundColor Gray
    Write-Host "  Status: $($createdOperationalUser.statusDescription)" -ForegroundColor Gray
} else {
    Write-Host "FAILED: Could not create Operational User" -ForegroundColor Red
}

# =====================================================
# STEP 5: Get All Users
# =====================================================
Write-Host "`n[STEP 5] Fetching all users..." -ForegroundColor Yellow

$allUsers = Invoke-ApiRequest -Method GET -Endpoint "/api/users" -Token $token

if ($allUsers) {
    Write-Host "SUCCESS: Retrieved users" -ForegroundColor Green
    if ($allUsers.content) {
        Write-Host "  Total Users: $($allUsers.content.Count)" -ForegroundColor Gray
        foreach ($user in $allUsers.content | Select-Object -First 5) {
            Write-Host "  - $($user.firstName) $($user.lastName) ($($user.role)) - $($user.statusDescription)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  Total Users: $($allUsers.Count)" -ForegroundColor Gray
    }
} else {
    Write-Host "FAILED: Could not fetch users" -ForegroundColor Red
}

# =====================================================
# STEP 6: Update User
# =====================================================
if ($createdBusinessUser) {
    Write-Host "`n[STEP 6] Updating Business User..." -ForegroundColor Yellow
    
    $updateData = @{
        firstName = "Jonathan"
        lastName = "Doe-Updated"
        email = $createdBusinessUser.email
        phoneNumber = $createdBusinessUser.phoneNumber
        dateOfBirth = $createdBusinessUser.dateOfBirth
        statusDescription = "Active"
    }
    
    $updatedUser = Invoke-ApiRequest -Method PUT -Endpoint "/api/users/$($createdBusinessUser.id)" -Token $token -Body $updateData
    
    if ($updatedUser) {
        Write-Host "SUCCESS: Updated user" -ForegroundColor Green
        Write-Host "  New Name: $($updatedUser.firstName) $($updatedUser.lastName)" -ForegroundColor Gray
    } else {
        Write-Host "FAILED: Could not update user" -ForegroundColor Red
    }
}

# =====================================================
# STEP 7: Create Organization
# =====================================================
Write-Host "`n[STEP 7] Creating an Organization..." -ForegroundColor Yellow

if ($createdBusinessUser) {
    $orgData = @{
        userIdentifier = $createdBusinessUser.id
        legalName = "Test Financial Services Ltd"
        businessName = "Test Finance"
        organisationTypeDescription = "LTD"
        registrationNumber = "REG$(Get-Random -Minimum 10000 -Maximum 99999)"
        sicCode = "64999"
        businessDescription = "Financial services and money transfer"
        incorporationDate = "2020-01-15"
        countryOfIncorporation = "United Kingdom"
        websiteAddress = "https://testfinance.com"
        registeredAddress = @{
            typeCode = 3
            addressLine1 = "10 Business Park"
            addressLine2 = "Suite 200"
            city = "London"
            stateCode = "Greater London"
            postalCode = "EC2A 1AA"
            country = "United Kingdom"
        }
        businessAddress = @{
            typeCode = 4
            addressLine1 = "20 Commerce Street"
            city = "London"
            stateCode = "Greater London"
            postalCode = "EC2B 2BB"
            country = "United Kingdom"
        }
    }
    
    $createdOrg = Invoke-ApiRequest -Method POST -Endpoint "/api/organisations" -Token $token -Body $orgData
    
    if ($createdOrg) {
        Write-Host "SUCCESS: Created Organization" -ForegroundColor Green
        Write-Host "  ID: $($createdOrg.id)" -ForegroundColor Gray
        Write-Host "  Legal Name: $($createdOrg.legalName)" -ForegroundColor Gray
        Write-Host "  Business Name: $($createdOrg.businessName)" -ForegroundColor Gray
        Write-Host "  Registration: $($createdOrg.registrationNumber)" -ForegroundColor Gray
        Write-Host "  Status: $($createdOrg.statusDescription)" -ForegroundColor Gray
    } else {
        Write-Host "FAILED: Could not create organization" -ForegroundColor Red
    }
}

# =====================================================
# STEP 8: Get All Organizations
# =====================================================
Write-Host "`n[STEP 8] Fetching all organizations..." -ForegroundColor Yellow

$allOrgs = Invoke-ApiRequest -Method GET -Endpoint "/api/organisations" -Token $token

if ($allOrgs) {
    Write-Host "SUCCESS: Retrieved organizations" -ForegroundColor Green
    if ($allOrgs.content) {
        Write-Host "  Total Organizations: $($allOrgs.content.Count)" -ForegroundColor Gray
        foreach ($org in $allOrgs.content | Select-Object -First 5) {
            Write-Host "  - $($org.legalName) ($($org.organisationTypeDescription)) - $($org.statusDescription)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  Total Organizations: $($allOrgs.Count)" -ForegroundColor Gray
    }
} else {
    Write-Host "FAILED: Could not fetch organizations" -ForegroundColor Red
}

# =====================================================
# STEP 9: Update Organization
# =====================================================
if ($createdOrg) {
    Write-Host "`n[STEP 9] Updating Organization..." -ForegroundColor Yellow
    
    $orgUpdateData = @{
        userIdentifier = $createdOrg.userIdentifier
        legalName = "$($createdOrg.legalName) - Updated"
        businessName = $createdOrg.businessName
        organisationTypeDescription = $createdOrg.organisationTypeDescription
        registrationNumber = $createdOrg.registrationNumber
        sicCode = $createdOrg.sicCode
        businessDescription = "Updated: Financial services and money transfer"
        incorporationDate = $createdOrg.incorporationDate
        countryOfIncorporation = $createdOrg.countryOfIncorporation
        websiteAddress = $createdOrg.websiteAddress
        statusDescription = "Active"
    }
    
    $updatedOrg = Invoke-ApiRequest -Method PUT -Endpoint "/api/organisations/$($createdOrg.id)" -Token $token -Body $orgUpdateData
    
    if ($updatedOrg) {
        Write-Host "SUCCESS: Updated organization" -ForegroundColor Green
        Write-Host "  New Legal Name: $($updatedOrg.legalName)" -ForegroundColor Gray
    } else {
        Write-Host "FAILED: Could not update organization" -ForegroundColor Red
    }
}

# =====================================================
# STEP 10: Test Business User Login and Data Filtering
# =====================================================
if ($createdBusinessUser) {
    Write-Host "`n[STEP 10] Testing Business User data filtering..." -ForegroundColor Yellow
    
    # Note: This requires the user to have a password set
    Write-Host "  Business User Phone: $($createdBusinessUser.phoneNumber)" -ForegroundColor Gray
    Write-Host "  Note: You'll need to set a password for this user first" -ForegroundColor Yellow
    Write-Host "  Then test login to verify they only see their own data" -ForegroundColor Yellow
}

# =====================================================
# Summary
# =====================================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ Authentication" -ForegroundColor Green
Write-Host "✓ Role Fetching" -ForegroundColor Green
if ($createdBusinessUser) { Write-Host "✓ Create Business User" -ForegroundColor Green } else { Write-Host "✗ Create Business User" -ForegroundColor Red }
if ($createdOperationalUser) { Write-Host "✓ Create Operational User" -ForegroundColor Green } else { Write-Host "✗ Create Operational User" -ForegroundColor Red }
if ($allUsers) { Write-Host "✓ Get All Users" -ForegroundColor Green } else { Write-Host "✗ Get All Users" -ForegroundColor Red }
if ($updatedUser) { Write-Host "✓ Update User" -ForegroundColor Green } else { Write-Host "✗ Update User" -ForegroundColor Red }
if ($createdOrg) { Write-Host "✓ Create Organization" -ForegroundColor Green } else { Write-Host "✗ Create Organization" -ForegroundColor Red }
if ($allOrgs) { Write-Host "✓ Get All Organizations" -ForegroundColor Green } else { Write-Host "✗ Get All Organizations" -ForegroundColor Red }
if ($updatedOrg) { Write-Host "✓ Update Organization" -ForegroundColor Green } else { Write-Host "✗ Update Organization" -ForegroundColor Red }
Write-Host "========================================" -ForegroundColor Cyan
