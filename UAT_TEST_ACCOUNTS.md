# UAT Test Accounts

## 🎯 Admin Account (VERIFIED WORKING)

**Account Details:**
- **Phone Number:** +447700900000
- **Email:** admin.uat@fincore.com
- **Name:** UAT Admin User
- **Role:** Admin
- **Status:** ACTIVE
- **User ID:** 5

**✅ Successfully Tested:** 2026-05-01 22:04 UTC
- OTP generation: Working
- OTP verification: Working
- JWT token generation: Working
- User authentication: Working

## 📱 How to Login

### Step 1: Request OTP
```bash
POST https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/request-otp
Content-Type: application/json

{
  "phoneNumber": "+447700900000"
}
```

**Response:**
```json
{
  "message": "OTP sent to +********0000. Please verify to complete authentication.",
  "phoneNumber": "+447700900000",
  "expiresIn": 300
}
```

### Step 2: Get OTP from Backend Logs
Since SMS is not configured, retrieve the OTP from backend logs:

```powershell
gcloud run services logs read fincore-uat-api --region=europe-west2 --limit=5 --format="value(textPayload)" | Select-String "Generated OTP"
```

Look for a line like:
```
Generated OTP for +447700900000: 185302 (SMS disabled...)
```

### Step 3: Verify OTP
```bash
POST https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+447700900000",
  "otp": "185302"
}
```

**Success Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 5,
    "phoneNumber": "+447700900000",
    "email": "admin.uat@fincore.com",
    "firstName": "UAT",
    "middleName": "Admin",
    "lastName": "User",
    "dateOfBirth": "1990-01-01",
    "statusDescription": "ACTIVE",
    "role": "Admin",
    "createdDatetime": "2026-05-01T21:58:43",
    "lastModifiedDatetime": "2026-05-01T21:58:43"
  }
}
```

## 🔐 Using the Access Token

After successful login, use the access token in subsequent API requests:

```bash
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

Token expires in 900 seconds (15 minutes).

## 👥 Additional Test Accounts

The following accounts are also available for testing different roles:

### Compliance Officer
- **Phone Number:** +447700900001
- **Email:** compliance.uat@fincore.com
- **Name:** UAT Compliance Officer
- **Role:** Compliance_Officer
- **Status:** ACTIVE

### Operational Staff
- **Phone Number:** +447700900002
- **Email:** operations.uat@fincore.com
- **Name:** UAT Operations User
- **Role:** Operational_Staff
- **Status:** ACTIVE

### Business User
- **Phone Number:** +447700900003
- **Email:** business.uat@fincore.com
- **Name:** UAT Business User
- **Role:** Business_User
- **Status:** ACTIVE

## 🔧 PowerShell Quick Login Script

Save this as `uat-login.ps1` for quick testing:

```powershell
# Request OTP
$requestBody = @{ phoneNumber = '+447700900000' } | ConvertTo-Json
$response = Invoke-WebRequest -Uri "https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/request-otp" `
    -Method POST -Body $requestBody -ContentType "application/json" -UseBasicParsing

Write-Host "✅ OTP Requested" -ForegroundColor Green
Write-Host "Getting OTP from logs..." -ForegroundColor Yellow

# Get OTP from logs
$otpLog = gcloud run services logs read fincore-uat-api --region=europe-west2 --limit=5 --format="value(textPayload)" | Select-String "Generated OTP for \+447700900000: (\d{6})"
$otp = $otpLog.Matches[0].Groups[1].Value

Write-Host "📱 OTP: $otp" -ForegroundColor Cyan

# Wait a moment
Start-Sleep -Seconds 1

# Verify OTP
$verifyBody = @{ phoneNumber = '+447700900000'; otp = $otp } | ConvertTo-Json
$authResponse = Invoke-WebRequest -Uri "https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/verify-otp" `
    -Method POST -Body $verifyBody -ContentType "application/json" -UseBasicParsing

$auth = $authResponse.Content | ConvertFrom-Json

Write-Host "✅ Login Successful!" -ForegroundColor Green
Write-Host "👤 User: $($auth.user.firstName) $($auth.user.lastName)" -ForegroundColor Cyan
Write-Host "📧 Email: $($auth.user.email)" -ForegroundColor Cyan
Write-Host "🎭 Role: $($auth.user.role)" -ForegroundColor Cyan
Write-Host "🔑 Token: $($auth.accessToken.Substring(0, 50))..." -ForegroundColor Yellow
Write-Host "⏰ Expires in: $($auth.expiresIn) seconds" -ForegroundColor Yellow

# Save token to environment variable for subsequent requests
$env:UAT_TOKEN = $auth.accessToken
Write-Host "`n💾 Token saved to `$env:UAT_TOKEN" -ForegroundColor Green
```

## 📚 Testing Workflows

### 1. **Admin Workflow** - User Management
Use the Admin account (+447700900000) to:
- Create new users
- Update user roles
- Manage permissions
- View all users

### 2. **Compliance Workflow** - KYC & AML
Use the Compliance Officer account (+447700900001) to:
- Review KYC documents
- Approve/reject applications
- Run AML checks
- Generate compliance reports

### 3. **Operations Workflow** - Daily Tasks
Use the Operational Staff account (+447700900002) to:
- Process applications
- Update user information
- Handle routine operations

### 4. **Business Workflow** - Limited Access
Use the Business User account (+447700900003) to:
- View reports
- Access read-only data
- Basic user operations

## ⚠️ Important Notes

1. **OTP Expiration:** OTPs expire after 300 seconds (5 minutes)
2. **Token Expiration:** Access tokens expire after 900 seconds (15 minutes)
3. **SMS Not Configured:** Must retrieve OTP from backend logs
4. **Case Sensitivity:** MySQL tables on Cloud SQL are case-sensitive
5. **Database:** All tables use lowercase names (users, roles, permissions, otp_tokens)

## 🐛 Troubleshooting

### "User not found" Error
- ✅ **Fixed:** Tables were created with uppercase names but Hibernate expects lowercase
- **Solution:** Created lowercase tables and populated with test data

### "Table doesn't exist" Error
- ✅ **Fixed:** MySQL on Linux is case-sensitive for table names
- **Solution:** Ensured all tables (users, roles, permissions, otp_tokens) exist in lowercase

### "Invalid or expired OTP" Error
- OTPs expire after 5 minutes
- Request a new OTP and verify it immediately
- Ensure you're using the most recent OTP from logs

### Token Expired
- Tokens expire after 15 minutes
- Request a new OTP and login again

## 📊 Database Table Status

| Table Name | Status | Records | Notes |
|------------|--------|---------|-------|
| users | ✅ Working | 4 | Lowercase, populated with test accounts |
| roles | ✅ Working | 4 | Lowercase, all roles configured |
| permissions | ✅ Working | 9 | Lowercase, all permissions set |
| otp_tokens | ✅ Working | Dynamic | Lowercase, OTPs stored here |
| Role_Permissions | ✅ Working | Multiple | Mixed case (as defined in entity) |

## 🔗 Useful Commands

### Check if user exists:
```sql
SELECT * FROM users WHERE Phone_Number = '+447700900000';
```

### Check user roles:
```sql
SELECT u.Phone_Number, u.Email, r.Role_Name 
FROM users u 
LEFT JOIN roles r ON u.Role_Identifier = r.Role_Identifier;
```

### Check recent OTPs:
```sql
SELECT * FROM otp_tokens WHERE Phone_Number = '+447700900000' ORDER BY Created_At DESC LIMIT 5;
```

### View backend logs:
```powershell
gcloud run services logs read fincore-uat-api --region=europe-west2 --limit=50
```

---

**Last Updated:** 2026-05-01  
**Environment:** UAT  
**Backend URL:** https://fincore-uat-api-994490239798.europe-west2.run.app  
**Database:** fincore_db (Cloud SQL - MySQL 8.0)
