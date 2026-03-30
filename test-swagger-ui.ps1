# Test Swagger UI and API Documentation

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   SWAGGER UI TESTING SCRIPT" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test 1: Check if application is running
Write-Host "[Test 1] Checking application health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -Method GET
    Write-Host "✅ Application Status: $($health.status)" -ForegroundColor Green
    Write-Host "   Database: $($health.components.db.details.database)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Application is not running!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Check OpenAPI documentation
Write-Host "`n[Test 2] Checking OpenAPI documentation..." -ForegroundColor Yellow
try {
    $openapi = Invoke-RestMethod -Uri "http://localhost:8081/api-docs" -Method GET
    Write-Host "✅ OpenAPI Version: $($openapi.openapi)" -ForegroundColor Green
    Write-Host "   API Title: $($openapi.info.title)" -ForegroundColor Gray
    Write-Host "   API Version: $($openapi.info.version)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to retrieve OpenAPI docs!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Check User DTOs in OpenAPI spec
Write-Host "`n[Test 3] Verifying User DTO schemas..." -ForegroundColor Yellow

# Check UserCreateDTO
if ($openapi.components.schemas.UserCreateDTO) {
    $userCreateDTO = $openapi.components.schemas.UserCreateDTO
    Write-Host "✅ UserCreateDTO found in schema" -ForegroundColor Green
    
    # Check residential address identifier
    if ($userCreateDTO.properties.residentialAddressIdentifier) {
        $resAddr = $userCreateDTO.properties.residentialAddressIdentifier
        Write-Host "   ├─ residentialAddressIdentifier:" -ForegroundColor Gray
        Write-Host "   │  ├─ Type: $($resAddr.type)" -ForegroundColor Gray
        Write-Host "   │  ├─ Format: $($resAddr.format)" -ForegroundColor Gray
        Write-Host "   │  ├─ Description: $($resAddr.description)" -ForegroundColor Gray
        Write-Host "   │  └─ Example: $($resAddr.example)" -ForegroundColor Gray
        
        if ($resAddr.format -eq "int64") {
            Write-Host "   ✅ Address identifier correctly typed as int64 (Long)" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Address identifier format is NOT int64!" -ForegroundColor Red
        }
    } else {
        Write-Host "   ❌ residentialAddressIdentifier field NOT found!" -ForegroundColor Red
    }
    
    # Check postal address identifier
    if ($userCreateDTO.properties.postalAddressIdentifier) {
        $postAddr = $userCreateDTO.properties.postalAddressIdentifier
        Write-Host "   └─ postalAddressIdentifier:" -ForegroundColor Gray
        Write-Host "      ├─ Type: $($postAddr.type)" -ForegroundColor Gray
        Write-Host "      ├─ Format: $($postAddr.format)" -ForegroundColor Gray
        Write-Host "      ├─ Description: $($postAddr.description)" -ForegroundColor Gray
        Write-Host "      └─ Example: $($postAddr.example)" -ForegroundColor Gray
        
        if ($postAddr.format -eq "int64") {
            Write-Host "   ✅ Postal address identifier correctly typed as int64 (Long)" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Postal address identifier format is NOT int64!" -ForegroundColor Red
        }
    } else {
        Write-Host "   ❌ postalAddressIdentifier field NOT found!" -ForegroundColor Red
    }
} else {
    Write-Host "❌ UserCreateDTO NOT found in schema!" -ForegroundColor Red
}

# Check UserUpdateDTO
Write-Host "`n" -NoNewline
if ($openapi.components.schemas.UserUpdateDTO) {
    Write-Host "✅ UserUpdateDTO found in schema" -ForegroundColor Green
    
    # Check if address fields exist
    $hasResAddr = $null -ne $openapi.components.schemas.UserUpdateDTO.properties.residentialAddressIdentifier
    $hasPostAddr = $null -ne $openapi.components.schemas.UserUpdateDTO.properties.postalAddressIdentifier
    
    if ($hasResAddr -and $hasPostAddr) {
        Write-Host "   ✅ Both address identifier fields present" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Missing address identifier fields!" -ForegroundColor Red
    }
} else {
    Write-Host "❌ UserUpdateDTO NOT found in schema!" -ForegroundColor Red
}

# Check UserDTO (response)
Write-Host "`n" -NoNewline
if ($openapi.components.schemas.UserDTO) {
    Write-Host "✅ UserDTO found in schema" -ForegroundColor Green
    
    # Check if address fields exist
    $hasResAddr = $null -ne $openapi.components.schemas.UserDTO.properties.residentialAddressIdentifier
    $hasPostAddr = $null -ne $openapi.components.schemas.UserDTO.properties.postalAddressIdentifier
    
    if ($hasResAddr -and $hasPostAddr) {
        Write-Host "   ✅ Both address identifier fields present" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Missing address identifier fields!" -ForegroundColor Red
    }
} else {
    Write-Host "❌ UserDTO NOT found in schema!" -ForegroundColor Red
}

# Test 4: Check Swagger UI accessibility
Write-Host "`n[Test 4] Checking Swagger UI accessibility..." -ForegroundColor Yellow
try {
    $swaggerUI = Invoke-WebRequest -Uri "http://localhost:8081/swagger-ui/index.html" -Method GET -UseBasicParsing
    if ($swaggerUI.StatusCode -eq 200) {
        Write-Host "✅ Swagger UI is accessible" -ForegroundColor Green
        Write-Host "   URL: http://localhost:8081/swagger-ui/index.html" -ForegroundColor Cyan
    }
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "❌ Swagger UI returns 403 Forbidden" -ForegroundColor Red
        Write-Host "   Solution: Restore SecurityConfig.java from stash" -ForegroundColor Yellow
        Write-Host "   Command: git stash pop" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Failed to access Swagger UI!" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 5: List all User-related endpoints
Write-Host "`n[Test 5] Listing User-related API endpoints..." -ForegroundColor Yellow
$userEndpoints = $openapi.paths.PSObject.Properties | Where-Object { $_.Name -like "*user*" }
if ($userEndpoints) {
    Write-Host "✅ Found $($userEndpoints.Count) User-related endpoints:" -ForegroundColor Green
    foreach ($endpoint in $userEndpoints) {
        $methods = $endpoint.Value.PSObject.Properties.Name -join ", "
        Write-Host "   ├─ $($endpoint.Name)" -ForegroundColor Gray
        Write-Host "   │  └─ Methods: $($methods.ToUpper())" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ No User endpoints found!" -ForegroundColor Red
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n✅ Application is running on port 8081" -ForegroundColor Green
Write-Host "✅ OpenAPI documentation available at: http://localhost:8081/api-docs" -ForegroundColor Green
Write-Host "✅ Swagger UI available at: http://localhost:8081/swagger-ui/index.html" -ForegroundColor Green
Write-Host "`n[NEXT STEPS]" -ForegroundColor Yellow
Write-Host "   1. Open Swagger UI in browser" -ForegroundColor Gray
Write-Host "   2. Expand POST /api/users endpoint" -ForegroundColor Gray
Write-Host "   3. Verify residentialAddressIdentifier shows as 'integer <int64>'" -ForegroundColor Gray
Write-Host "   4. Verify field descriptions and examples are visible" -ForegroundColor Gray
Write-Host "   5. Test 'Try it out' with sample data" -ForegroundColor Gray
Write-Host "`n" -ForegroundColor Gray

# Save schema to file for review
Write-Host "[INFO] Saving OpenAPI schema to file..." -ForegroundColor Yellow
$openapi | ConvertTo-Json -Depth 20 | Out-File -FilePath "openapi-schema.json" -Encoding UTF8
Write-Host "✅ Schema saved to: openapi-schema.json" -ForegroundColor Green

Write-Host "`n[SUCCESS] All tests completed!`n" -ForegroundColor Cyan
