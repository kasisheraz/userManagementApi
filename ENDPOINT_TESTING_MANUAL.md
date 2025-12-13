# Manual Endpoint Testing Guide

## Issue Encountered

The application successfully compiles, builds, and starts (confirmed by logs showing "Tomcat started on port 8080" and "Started UserManagementApplication in 13-16 seconds"). However, when running the application as a background process in VS Code terminals, the Java process terminates prematurely before HTTP requests can be made to test the endpoints.

**Root Cause**: The VS Code terminal background process isolation/cleanup mechanism appears to be terminating the Java process shortly after startup, even when the application has successfully initialized.

**Solution**: Manual testing with the application terminal kept open and visible.

---

## Step 1: Start the Application

Keep one terminal window dedicated to running the application, **DO NOT** close or minimize it during testing.

### Option A: Use VS Code Terminal (Recommended)

1. Open a NEW PowerShell terminal in VS Code
2. Navigate to the project directory:
   ```powershell
   cd C:\Development\git\userManagementApi
   ```
3. Start the application and **KEEP THIS WINDOW OPEN**:
   ```powershell
   java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
   ```
4. Wait until you see:
   ```
   Tomcat started on port 8080 (http) with context path ''
   Started UserManagementApplication in X.XXX seconds
   ```
5. **Leave this terminal window open** - do NOT minimize or close it

### Option B: Use External Terminal

```cmd
# From Windows Command Prompt
cd C:\Development\git\userManagementApi
java -jar target\user-management-api-1.0.0.jar --spring.profiles.active=h2
```

---

## Step 2: Open a NEW Terminal for Testing

Once the application is running and you see the startup completion message, **open a SEPARATE new terminal** for testing:

1. Open a new PowerShell terminal
2. Navigate to project directory (optional, for reference):
   ```powershell
   cd C:\Development\git\userManagementApi
   ```

---

## Step 3: Test Endpoints

Using the new terminal, run the following tests. The application should still be running in the first terminal.

### Test 1: Health Check

```powershell
# Using PowerShell Invoke-WebRequest
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing | Select-Object -ExpandProperty Content

# OR using curl (if available)
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

**Status Code:** `200 OK`

---

### Test 2: Login (Get JWT Token)

```powershell
# Using PowerShell
$loginBody = @{
    username = "admin"
    password = "Admin@123456"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody `
    -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json

# OR using curl
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{`"username`":`"admin`",`"password`":`"Admin@123456`"}"
```

**Expected Response:**
```json
{
  "token": "api-key-...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

**Status Code:** `200 OK`

**Note:** Save the `token` value for subsequent authenticated requests:
```powershell
$token = "<paste the token from response here>"
```

---

### Test 3: Get All Users (Authenticated)

```powershell
# First, get the token (from Test 2 above)
$loginBody = @{
    email = "admin@fincore.com"
    password = "admin123"
} | ConvertTo-Json

$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody `
    -UseBasicParsing

$loginJson = $login.Content | ConvertFrom-Json
$token = $loginJson.token

# Now get users with the token
$headers = @{ "Authorization" = "Bearer $token" }

Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Headers $headers `
    -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected Response:**
```json
{
  "message": "Users retrieved successfully",
  "users": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@fincore.com",
      "fullName": "System Administrator",
      "phoneNumber": "+1234567890",
      "status": "ACTIVE",
      "createdAt": "2025-12-13T19:43:26.000Z"
    },
    {
      "id": 2,
      "username": "compliance",
      "email": "compliance@fincore.com",
      "fullName": "Compliance Officer",
      "phoneNumber": "+1234567891",
      "status": "ACTIVE",
      "createdAt": "2025-12-13T19:43:26.000Z"
    },
    {
      "id": 3,
      "username": "staff",
      "email": "staff@fincore.com",
      "fullName": "Operational Staff",
      "phoneNumber": "+1234567892",
      "status": "ACTIVE",
      "createdAt": "2025-12-13T19:43:26.000Z"
    }
  ]
}
```

**Status Code:** `200 OK`

---

### Test 4: Create a New User

```powershell
# Get token first (see Test 2)
$loginBody = @{
    email = "admin@fincore.com"
    password = "admin123"
} | ConvertTo-Json

$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody `
    -UseBasicParsing

$loginJson = $login.Content | ConvertFrom-Json
$token = $loginJson.token

# Create new user
$newUserBody = @{
    username = "testuser"
    email = "testuser@fincore.com"
    password = "TestPass123!"
    fullName = "Test User"
    phoneNumber = "+1234567893"
    employeeId = "EMP004"
    department = "Testing"
    jobTitle = "QA Engineer"
} | ConvertTo-Json

$headers = @{ "Authorization" = "Bearer $token" }

Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
    -Method POST `
    -ContentType "application/json" `
    -Body $newUserBody `
    -Headers $headers `
    -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected Response:**
```json
{
  "message": "User created successfully",
  "user": {
    "id": 4,
    "username": "testuser",
    "email": "testuser@fincore.com",
    "fullName": "Test User",
    "phoneNumber": "+1234567893",
    "employeeId": "EMP004",
    "department": "Testing",
    "jobTitle": "QA Engineer",
    "status": "ACTIVE",
    "createdAt": "2025-12-13T19:50:00.000Z"
  }
}
```

**Status Code:** `201 Created`

---

### Test 5: Update a User

```powershell
# Get token
$loginBody = @{
    email = "admin@fincore.com"
    password = "admin123"
} | ConvertTo-Json

$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody `
    -UseBasicParsing

$loginJson = $login.Content | ConvertFrom-Json
$token = $loginJson.token

# Update user (ID 4 from Test 4, or use any existing user ID)
$updateBody = @{
    fullName = "Updated Test User"
    phoneNumber = "+9876543210"
    department = "QA"
    jobTitle = "Senior QA Engineer"
} | ConvertTo-Json

$headers = @{ "Authorization" = "Bearer $token" }

Invoke-WebRequest -Uri "http://localhost:8080/api/users/4" `
    -Method PUT `
    -ContentType "application/json" `
    -Body $updateBody `
    -Headers $headers `
    -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected Response:**
```json
{
  "message": "User updated successfully",
  "user": {
    "id": 4,
    "username": "testuser",
    "email": "testuser@fincore.com",
    "fullName": "Updated Test User",
    "phoneNumber": "+9876543210",
    "department": "QA",
    "jobTitle": "Senior QA Engineer",
    "status": "ACTIVE",
    "updatedAt": "2025-12-13T19:51:00.000Z"
  }
}
```

**Status Code:** `200 OK`

---

### Test 6: Test Authentication Failure

```powershell
# Attempt login with wrong password
$loginBody = @{
    username = "admin"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing | Select-Object -ExpandProperty Content
} catch {
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Response.StatusDescription)"
}
```

**Expected Response:**
```json
{
  "timestamp": "2025-12-13T19:52:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

**Status Code:** `401 Unauthorized`

---

### Test 7: Test Missing Authentication Token

```powershell
# Try to access protected endpoint without token
try {
    Invoke-WebRequest -Uri "http://localhost:8080/api/users" `
        -UseBasicParsing | Select-Object -ExpandProperty Content
} catch {
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    Write-Host "Response: $($_.Exception.Response.StatusDescription)"
}
```

**Expected Response:**
```json
{
  "timestamp": "2025-12-13T19:53:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. No authorization token provided."
}
```

**Status Code:** `403 Forbidden`

---

## Test Checklist

After running all tests, verify:

| Test | Endpoint | Method | Expected Status | Actual Status | ✅/❌ |
|------|----------|--------|-----------------|---------------|-------|
| Health | `/actuator/health` | GET | 200 | | |
| Login | `/api/auth/login` | POST | 200 | | |
| Get Users | `/api/users` | GET | 200 | | |
| Create User | `/api/users` | POST | 201 | | |
| Update User | `/api/users/{id}` | PUT | 200 | | |
| Auth Failure | `/api/auth/login` | POST | 401 | | |
| No Token | `/api/users` | GET | 403 | | |

---

## Automated Testing Script (For Reference)

If the application stays running when started from an external command prompt (not VS Code background process), you can use the `test-endpoints.bat` script:

```batch
.\test-endpoints.bat
```

This script provides automated testing for basic endpoints.

---

## Next Steps After Validation

Once all endpoint tests pass:

1. ✅ Confirm database initialization works (H2 in-memory)
2. ✅ Confirm authentication/JWT tokens issue correctly
3. ✅ Confirm CRUD operations work as expected
4. ✅ Confirm error handling (401, 403, validation errors)

**Then proceed to Cloud Run deployment:**

1. Install Docker (optional, but recommended for testing container image locally)
2. Configure `gcp-config.env` with your GCP project details
3. Run `setup-gcp-infrastructure.sh` to create Cloud SQL resources
4. Run `deploy-to-cloud-run.sh` to deploy to Google Cloud Run
5. Test cloud endpoints using the same test suite against the Cloud Run URL

---

## Troubleshooting

### "Unable to connect to the remote server"

**Problem**: Application is not listening on localhost:8080

**Solutions:**
1. Check the application terminal - ensure it shows "Tomcat started on port 8080"
2. Verify no other application is using port 8080: `netstat -ano | findstr "8080"`
3. Restart the application: stop (Ctrl+C) and restart with the command from Step 1
4. Check Windows Firewall is not blocking Java (unlikely, but possible)

### "Invalid email or password"

**Problem**: Login fails with 401 Unauthorized

**Solutions:**
1. Verify credentials: Default user is `admin@fincore.com` / `admin123`
2. Check spelling and case sensitivity
3. Check password was not changed (it loads from `data.sql`)
4. If password is BCrypt hashed and changed, verify the hash is correct

### "Access denied. No authorization token provided."

**Problem**: HTTP 403 when accessing `/api/users` without Bearer token

**This is expected behavior** - the endpoint requires authentication. Add the Authorization header with the JWT token obtained from login endpoint.

---

## Test Data Reference

Default users loaded from `src/main/resources/data.sql`:

| Username | Email | Password | Role | Status |
|----------|-------|----------|------|--------|
| admin | admin@fincore.com | Admin@123456 | ADMIN | ACTIVE |
| compliance | compliance@fincore.com | Compliance@123 | COMPLIANCE_OFFICER | ACTIVE |
| staff | staff@fincore.com | Staff@123456 | OPERATIONAL_STAFF | ACTIVE |

---

## Application Configuration (H2 Profile)

- **Database**: H2 in-memory (`jdbc:h2:mem:fincore_db`)
- **Port**: 8080
- **H2 Console**: http://localhost:8080/h2-console
- **Health Endpoint**: http://localhost:8080/actuator/health
- **Database Schema**: Auto-created (DDL: create-drop)
- **Test Data**: Loaded from `data.sql`

---

Generated: December 13, 2025
