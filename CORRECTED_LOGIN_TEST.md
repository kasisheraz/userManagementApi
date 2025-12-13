# ✅ CORRECTED Login Test Commands

## The Issue

The **LoginRequest DTO requires `username`** (not `email`). The error you saw was:

```
Field error in object 'loginRequest' on field 'username': rejected value [null]; 
codes [NotBlank.loginRequest.username,NotBlank.username,NotBlank.java.lang.String,NotBlank]
```

This means the `username` field was missing and null.

---

## ✅ CORRECTED Test Commands

### Default Test Credentials

| Username | Password | Email |
|----------|----------|-------|
| admin | Admin@123456 | admin@fincore.com |
| compliance | Compliance@123 | compliance@fincore.com |
| staff | Staff@123456 | staff@fincore.com |

---

## Quick Tests (Copy & Paste)

### 1. Health Check
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing | Select-Object -ExpandProperty Content
```

### 2. Login (CORRECTED - use username, not email!)
```powershell
$body = @{username="admin";password="Admin@123456"} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected Response:**
```json
{
  "token": "api-key-1-...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

### 3. Get Users (with token from login)
```powershell
$loginBody = @{username="admin";password="Admin@123456"} | ConvertTo-Json
$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody -UseBasicParsing
$token = ($login.Content | ConvertFrom-Json).token
$headers = @{"Authorization" = "Bearer $token"}
Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content
```

### 4. Test Wrong Password (should get 401)
```powershell
$body = @{username="admin";password="wrongpassword"} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -ErrorAction Stop 2>&1 | Select-Object -ExpandProperty Content
```

---

## What Changed

| Field | WRONG ❌ | CORRECT ✅ |
|-------|---------|----------|
| Email field | `email` | (NOT used for login) |
| Username field | Missing ❌ | `username` ✅ |
| Email value | "admin@fincore.com" | "admin" ✅ |
| Password value | "admin123" | "Admin@123456" ✅ |

---

## Updated Documentation

The following files have been corrected:
- ✅ [QUICK_TEST.md](QUICK_TEST.md) - Corrected login command
- ✅ [ENDPOINT_TESTING_MANUAL.md](ENDPOINT_TESTING_MANUAL.md) - Updated all login examples
- ✅ [CORRECTED_LOGIN_TEST.md](CORRECTED_LOGIN_TEST.md) - This file

---

## Try Now!

1. **Terminal 1**: Keep application running
   ```powershell
   java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
   ```

2. **Terminal 2**: Run the corrected login test
   ```powershell
   $body = @{username="admin";password="Admin@123456"} | ConvertTo-Json
   Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing | Select-Object -ExpandProperty Content
   ```

You should now get a 200 OK response with a JWT token! ✅

