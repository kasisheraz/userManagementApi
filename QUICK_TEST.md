# 🚀 Quick Start - Endpoint Testing

## Start Application (Terminal 1 - Keep Open!)

```powershell
cd C:\Development\git\userManagementApi
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
```

**Wait for**: ✅ `Started UserManagementApplication in X.XXX seconds`

---

## Test in New Terminal (Terminal 2)

### 1️⃣ Health Check

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected**: `{"status":"UP"}`

### 2️⃣ Login

```powershell
$body = @{username="admin";password="Admin@123456"} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected**: JWT token + user info (Status: 200)

### 3️⃣ Get All Users (use token from #2)

```powershell
$loginBody = @{username="admin";password="Admin@123456"} | ConvertTo-Json
$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody -UseBasicParsing
$loginJson = $login.Content | ConvertFrom-Json
$token = $loginJson.token

$headers = @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected**: List of 3 users

### 4️⃣ Create New User

```powershell
$body = @{
    username="newuser"
    email="newuser@fincore.com"
    password="NewPass123!"
    fullName="New User"
    phoneNumber="+1234567890"
    department="Testing"
} | ConvertTo-Json

$headers = @{ "Authorization" = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method POST -ContentType "application/json" -Body $body -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected**: New user created (Status: 201)

### 5️⃣ Test Error Handling

```powershell
# Missing token - should get 403
Invoke-WebRequest -Uri "http://localhost:8080/api/users" -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected**: 403 Forbidden

---

## Full Test Suite

For all 7 endpoint tests with detailed instructions:
📖 **[ENDPOINT_TESTING_MANUAL.md](ENDPOINT_TESTING_MANUAL.md)**

---

## Test Checklist

- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 

---

## Test Data

| Username   | Password       | Email                 | Role               |
| ---------- | -------------- | --------------------- | ------------------ |
| admin      | Admin@123456   | admin@fincore.com     | ADMIN              |
| compliance | Compliance@123 | compliance@fincore.com| COMPLIANCE_OFFICER |
| staff      | Staff@123456   | staff@fincore.com     | OPERATIONAL_STAFF  |

---

## After Testing ✅

1. **Cloud Deployment**: Read [CLOUD_RUN_DEPLOYMENT.md](CLOUD_RUN_DEPLOYMENT.md)
2. **Docker Testing** (optional): `docker build -t user-management-api:1.0.0 .`

---

**Status**: Application built ✅  | Tests ready ✅  | Deploy ready ✅
