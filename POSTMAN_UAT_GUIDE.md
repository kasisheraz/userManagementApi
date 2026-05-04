# Postman UAT Testing Guide

Complete guide for testing the FinCore UAT API using Postman.

## Quick Start

### 1. Import Files into Postman

1. Open Postman
2. Click **Import** button (top left)
3. Import both files:
   - `postman_collection_uat.json` - API requests collection
   - `postman_environment_uat.json` - UAT environment variables
4. Select **FinCore UAT** environment from dropdown (top right)

### 2. Test Authentication Flow

#### Step 1: Request OTP
1. Open **Authentication** → **1. Request OTP**
2. Click **Send**
3. **Expected Response**:
   ```json
   {
     "message": "OTP sent to +********0000...",
     "phoneNumber": "+447700900000",
     "expiresIn": 300,
     "devOtp": "685617"
   }
   ```
4. **✅ The OTP is automatically saved to the environment variable `otp_code`**

> **Note**: UAT environment now returns `devOtp` directly in the response, just like NPE. No need to check logs!

#### Step 2: Verify OTP
1. Open **Authentication** → **2. Verify OTP**
2. **The OTP is already filled in automatically from Step 1!**
3. Click **Send**
4. **Expected Response**:
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer",
     "expiresIn": 3600,
     "user": {
       "id": 1,
       "phoneNumber": "+447700900000",
       "email": "admin@fincore.test",
       "firstName": "Admin",
       "lastName": "User",
       "statusDescription": "ACTIVE",
       "role": "Admin"
     }
   }
   ```

✅ **The access token is automatically saved to the environment variable `access_token`**

#### Step 4: Test Authenticated Request
1. Open **Authentication** → **3. Get Current User**
2. Click **Send** (token is automatically included)
3. **Expected Response**: Your user details

---

## Available Endpoints

### 🔐 Authentication

| Request | Method | Auth Required | Description |
|---------|--------|---------------|-------------|
| Request OTP | POST | ❌ No | Request OTP code for phone number |
| Verify OTP | POST | ❌ No | Verify OTP and get JWT token |
| Get Current User | GET | ✅ Yes | Get authenticated user details |

### 👥 Users

| RequesJWT token is automatically saved to the environment variable `jwt_token`**

#### Step 3: Test Authenticated Request  l users |
| Get User by ID | GET | ✅ Yes | Get specific user details |
| Create User | POST | ✅ Yes | Create new user |

### 🏥 Health Check

| Request | Method | Auth Required | Description |
|---------|--------|---------------|-------------|
| API Health | GET | ❌ No | Check API status |

---

## Environment Variables

The UAT environment includes these variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | UAT API base URL | https://fincore-uat-api-994490239798.europe-west2.run.app |
| `test_phone` | Test account phone | +447700900000 |
| `current_otp` | Latest OTP code | 350098 |
| `access_token` | JWT access token | eyJhbGc... |

### Updating Variables

1. Click **Environments** (left sidebar)
2. Select **FinCore UAT**
3. Update values as needed
4. Click **Save**

---

## Common Issues & Solutions

### Issue: "User not found"
**Problem**: Test user doesn't exist in database

**Solution**: Run this command to create test user:
```powershell
cd "c:\Development\git\userManagementApi"
gcloud storage cp fix-test-user.sql gs://fincore-uat-terraform-state/fix-test-user.sql
gcloud sql import sql fincore-uat-db gs://fincore-uat-terraform-state/fix-test-user.sql --database=fincore_db --quiet
```

### Issue: "Invalid or expired OTP"
**Problem**: OTP expired (5 minute timeout) or wrong code

**Solution**:
1. Request a new OTP using **1. Request OTP**
2. The new OTP will automatically be saved to `otp_code` environment variable
3. Verify immediately using **2. Verify OTP**

### Issue: "401 Unauthorized" on authenticated requests
**Problem**: JWT token expired or invalid

**Solution**:
1. Go through authentication flow again (Request OTP → Verify OTP)
2. New token will automatically be saved to `jwt_token`
3. Retry the request
## Test Credentials

### Admin Account
- **Phone**: +447700900000
- **Email**: admin@fincore.test
- **Role**: Admin
- **Status**: ACTIVE

### Getting OTP

**Option 1: Cloud Logs (Recommended)**
```powershell
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-uat-api AND textPayload:OTP" --limit=5 --format="value(textPayload)" --freshness=5m | Select-Object -First 1
```

**Option 2: Cloud Console**
1. Go to [Cloud Run Logs](https://console.cloud.google.com/run/detail/europe-west2/fincore-uat-api/logs)
2. Filter by "OTP"
3. Copy the 6-digit code from recent log entry
- **OTP**: Automatically returned in `devOtp` field when requesting OTP

### Authentication Flow
1. **Request OTP**: POST `/api/auth/request-otp` → Returns `devOtp` in response
2. **System Auto-Saves**: Postman saves `devOtp` to `otp_code` variable
3. **Verify OTP**: POST `/api/auth/verify-otp` → Returns JWT token
4. **System Auto-Saves**: Postman saves JWT to `jwt_token` variable
5. **Make Requests**: All authenticated requests use `jwt_token` automaticall
### Automated Testing with Newman

Run the collection via command line:

```bash
npm install -g newman
newman run postman_collection_uat.json -e postman_environment_uat.json
```

### Pre-request Scripts

The collection includes automatic test scripts that:
- ✅ Validate response status codes
- ✅ Check response structure
- ✅ Save access tokens automatically
- ✅ Save OTP codes (if available in response)
- ✅ Log important data to console

View test results in the **Test Results** tab after sending requests.

---

## Request Headers

All requests automatically include:
```
Content-Type: application/json
```

Authenticated requests automatically include:
```
Authorization: Bearer {{access_token}}
```

---

## Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 404 | Not Found | Resource not found |
| 500 | Server Error | Internal server error |

---

## Tips & Best Practices

1. **Always check test results** - Each request has automated tests that validate the response
2. **Use the Console tab** - Important data like OTP and tokens are logged there
3. **Save your work** - Export the collection after adding custom requests
4. **Monitor logs** - Keep Cloud Run logs open while testing for debugging
5. **Token expiry** - JWT tokens expire after 1 hour, re-authenticate when needed
6. **Variable usage** - Use `{{variable_name}}` syntax to reference environment variables

---

## Troubleshooting Commands

### Check API Health
```bash
curl https://fincore-uat-api-994490239798.europe-west2.run.app/actuator/health
```

### Test OTP Request
```powershell
$headers = @{ 'Content-Type' = 'application/json' }
$body = @{ phoneNumber = '+447700900000' } | ConvertTo-Json
Invoke-WebRequest -Uri 'https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/request-otp' -Method Post -Headers $headers -Body $body -UseBasicParsing
```

### View Recent API Errors
```powershell
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-uat-api AND severity>=ERROR" --limit=10 --freshness=1h
```

---

## Support

For issues or questions:
1. Check Cloud Run logs for errors
2. Verify test user exists in database
3. Ensure OTP hasn't expired
4. Try re-authenticating with a fresh OTP

## UAT Environment Details

- **API URL**: https://fincore-uat-api-994490239798.europe-west2.run.app
- **Frontend URL**: https://fincore-webui-uat-994490239798.europe-west2.run.app
- **Database**: fincore_db @ 35.189.81.151:3306
- **Region**: europe-west2 (London)
- **Environment**: UAT (User Acceptance Testing)
