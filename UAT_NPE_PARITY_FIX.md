# UAT/NPE Parity Fix - devOtp Response

## Issue
UAT environment was not returning `devOtp` in the `/api/auth/request-otp` response, while NPE environment was. This caused inconsistent testing experience between environments.

## Root Cause
The backend code checks the `SPRING_PROFILES_ACTIVE` environment variable to determine if it should include `devOtp` in the response:

**Original Code** (`AuthenticationService.java`):
```java
private boolean isNonProductionEnvironment() {
    return activeProfile != null && 
           (activeProfile.contains("npe") || 
            activeProfile.contains("local") || 
            activeProfile.contains("test") ||
            activeProfile.contains("h2") ||
            activeProfile.contains("dev"));
}
```

**Deployment Profiles**:
- NPE: `SPRING_PROFILES_ACTIVE=npe` ✅ Returns `devOtp`
- UAT: `SPRING_PROFILES_ACTIVE=uat` ❌ Does NOT return `devOtp`

## Solution
Added `"uat"` to the list of non-production environments:

**Updated Code** (`AuthenticationService.java`):
```java
private boolean isNonProductionEnvironment() {
    return activeProfile != null && 
           (activeProfile.contains("npe") || 
            activeProfile.contains("uat") ||   // ← ADDED THIS LINE
            activeProfile.contains("local") || 
            activeProfile.contains("test") ||
            activeProfile.contains("h2") ||
            activeProfile.contains("dev"));
}
```

## Changes Made

### 1. Backend Code
- **File**: `src/main/java/com/fincore/usermgmt/service/AuthenticationService.java`
- **Change**: Added `activeProfile.contains("uat")` to `isNonProductionEnvironment()` method
- **Impact**: UAT environment now returns `devOtp` in OTP request responses

### 2. Backend Deployment
- **Build**: Cloud Build ID `f0a1d886-0c58-4cc0-8631-0e1b76cc3024`
- **Image**: `gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest`
- **Deployed**: Cloud Run service `fincore-uat-api` revision `fincore-uat-api-00009-924`
- **Environment Variables**: Restored all DB connection variables (DB_NAME, DB_USER, DB_PASSWORD, DB_HOST, DB_PORT)

### 3. Postman Collection
- **File**: `postman_collection_uat.json`
- **Changes**:
  - Updated variable names to match NPE collection: `jwt_token`, `phone_number`, `otp_code` (instead of `access_token`, `test_phone`, `current_otp`)
  - Updated test scripts to automatically save `devOtp` to `otp_code` variable
  - Updated authentication header to use `{{jwt_token}}`

### 4. Postman Environment
- **File**: `postman_environment_uat.json`
- **Changes**:
  - Renamed environment ID to `fincore-uat-env`
  - Updated display name to "FinCore UAT Environment"
  - Changed variable names to match NPE: `jwt_token`, `phone_number`, `otp_code`
  - Set default test phone to `+447700900000`

### 5. Documentation
- **File**: `POSTMAN_UAT_GUIDE.md`
- **Changes**:
  - Removed "Step 2: Get OTP from Logs" - no longer needed
  - Updated instructions to show `devOtp` is returned automatically
  - Simplified authentication flow (3 steps instead of 4)
  - Updated troubleshooting section
  - Emphasized automatic OTP and token saving

## Testing Results

### Before Fix
```json
{
  "message": "OTP sent to +********0000...",
  "phoneNumber": "+447700900000",
  "expiresIn": 300
}
```
❌ No `devOtp` field - had to check Cloud Run logs

### After Fix
```json
{
  "message": "OTP sent to +********0000...",
  "phoneNumber": "+447700900000",
  "expiresIn": 300,
  "devOtp": "685617"
}
```
✅ `devOtp` field included - same behavior as NPE

## Usage in Postman

### Step 1: Request OTP
```
POST {{base_url}}/api/auth/request-otp
Body: {"phoneNumber": "{{phone_number}}"}
```
**Response**: Includes `devOtp` field  
**Auto-saved**: `devOtp` → `otp_code` environment variable

### Step 2: Verify OTP
```
POST {{base_url}}/api/auth/verify-otp
Body: {"phoneNumber": "{{phone_number}}", "otp": "{{otp_code}}"}
```
**Response**: Includes `accessToken`  
**Auto-saved**: `accessToken` → `jwt_token` environment variable

### Step 3: Make Authenticated Requests
```
Authorization: Bearer {{jwt_token}}
```
All authenticated endpoints automatically use the saved token.

## Benefits
1. **Consistency**: UAT and NPE environments now behave identically
2. **Faster Testing**: No need to check Cloud Run logs for OTP codes
3. **Automation**: Postman scripts automatically save OTP and JWT tokens
4. **Better UX**: Same workflow across all non-production environments
5. **Frontend Testing**: Browser developer tools now show `devOtp` in UAT (matching NPE)

## Environment Comparison

| Feature | NPE | UAT (Before) | UAT (After) |
|---------|-----|--------------|-------------|
| Profile | npe | uat | uat |
| Returns `devOtp` | ✅ Yes | ❌ No | ✅ Yes |
| Auto-save in Postman | ✅ Yes | ❌ No | ✅ Yes |
| Visible in Browser DevTools | ✅ Yes | ❌ No | ✅ Yes |
| Need to check logs | ❌ No | ✅ Yes | ❌ No |

## Deployment Details

**Build Command**:
```bash
gcloud builds submit --tag=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest
```

**Deploy Command**:
```bash
gcloud run deploy fincore-uat-api \
  --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest \
  --region=europe-west2 \
  --set-env-vars="DB_NAME=fincore_db,DB_USER=fincore_app,DB_PASSWORD=<password>,DB_HOST=35.189.81.151,DB_PORT=3306,SPRING_PROFILES_ACTIVE=uat,GCS_ENABLED=true,GCS_BUCKET_NAME=fincore-uat-kyc-documents,GCP_PROJECT_ID=project-07a61357-b791-4255-a9e"
```

## Verification

**Test API Endpoint**:
```powershell
$body = '{"phoneNumber":"+447700900000"}'
Invoke-WebRequest -Uri 'https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/request-otp' -Method Post -Body $body -ContentType 'application/json'
```

**Expected Response**:
```json
{
  "message": "OTP sent to +********0000. Please verify to complete authentication.",
  "phoneNumber": "+447700900000",
  "expiresIn": 300,
  "devOtp": "685617"
}
```

## Related Files
- `src/main/java/com/fincore/usermgmt/service/AuthenticationService.java` - Backend logic
- `postman_collection_uat.json` - UAT Postman collection
- `postman_environment_uat.json` - UAT Postman environment
- `POSTMAN_UAT_GUIDE.md` - Updated user guide
- `.github/workflows/deploy-npe.yml` - NPE deployment reference

## Status
✅ **RESOLVED** - UAT environment now returns `devOtp` in OTP responses, matching NPE behavior.

---

*Fixed: May 2, 2026*  
*Backend Revision: fincore-uat-api-00009-924*  
*Build ID: f0a1d886-0c58-4cc0-8631-0e1b76cc3024*
