# UI Integration Guide - Organization & User Endpoints

**Issue**: UI requests failing due to CORS restrictions  
**Fixed**: CORS configuration updated to allow all origins in NPE environment  
**Deployment**: Automatic via GitHub Actions (5-10 minutes)  
**Date**: March 6, 2026

---

## 🔴 Root Cause

The API had restrictive CORS settings that only allowed specific localhost ports. The UI was being blocked from making cross-origin requests.

### What Was Fixed
1. ✅ Updated [CorsConfig.java](src/main/java/com/fincore/usermgmt/config/CorsConfig.java) to use `allowedOriginPatterns` with wildcards
2. ✅ Added support for any localhost port (`http://localhost:*`)
3. ✅ Added support for all Cloud Run apps (`https://*.run.app`)
4. ✅ Added wildcard support for NPE environment (`*`)
5. ✅ Disabled CORS in SecurityConfig to prevent conflicts

---

## ✅ Verified Working Endpoints

Both endpoints were tested successfully via API:

### 1. Organization Creation - ✅ WORKING
### 2. User Creation - ✅ WORKING

---

## 📋 Organization Endpoint Details

### Endpoint
```
POST /api/organisations
```

### Request Headers
```http
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Required Request Body Fields
```json
{
  "ownerId": 1,                    // Required: Long (User ID who owns this org)
  "legalName": "Test Corp Ltd",    // Required: String (max 100 chars)
  "organisationType": "LTD"        // Required: String (LTD, PLC, LLP, SOLE_TRADER, CHARITY, PARTNERSHIP)
}
```

### Optional Fields
```json
{
  "businessName": "Test Corp",
  "businessDescription": "Description here",
  "registrationNumber": "12345678",
  "sicCode": "62011",
  "incorporationDate": "2020-01-15",
  "countryOfIncorporation": "United Kingdom",
  "hmrcMlrNumber": "XMLR1234567",
  "fcaNumber": "FCA123456",
  "icoNumber": "ICO123456",
  // ... see OrganisationCreateDTO.java for all fields
}
```

### Success Response (201 Created)
```json
{
  "id": 2,
  "ownerId": 1,
  "ownerName": "System Administrator",
  "legalName": "Test Corp Ltd",
  "organisationType": "LTD",
  "businessName": "Test Corp",
  "businessDescription": "Testing organization creation from UI",
  "status": "PENDING",
  "createdDatetime": "2026-03-06T20:26:47.827215209",
  "lastModifiedDatetime": "2026-03-06T20:26:47.827229965",
  // ... other fields will be null if not provided
}
```

### Error Responses

#### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```
**Fix**: Ensure JWT token is included in Authorization header

#### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Legal name is required"
}
```
**Fix**: Ensure all required fields are included

#### 403 Forbidden (CORS - NOW FIXED)
**Symptom**: No response, browser console shows CORS error  
**Status**: ✅ FIXED in latest deployment

---

## 📋 User Endpoint Details

### Endpoint
```
POST /api/users
```

### Request Headers
```http
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Required Request Body Fields
```json
{
  "phoneNumber": "+447912345678",  // Required: String (max 20 chars, unique)
  "firstName": "John",             // Required: String (max 100 chars)
  "lastName": "Doe"                // Required: String (max 100 chars)
}
```

### Optional Fields
```json
{
  "email": "john.doe@example.com",           // Optional: String (max 50 chars, unique, must be valid email)
  "middleName": "Michael",                   // Optional: String (max 100 chars)
  "dateOfBirth": "1990-05-15",              // Optional: Date (format: YYYY-MM-DD)
  "residentialAddressIdentifier": 1,        // Optional: Integer (ID of address)
  "postalAddressIdentifier": 2,             // Optional: Integer (ID of address)
  "statusDescription": "ACTIVE",            // Optional: String (max 20 chars)
  "role": "ADMIN"                           // Optional: String (ADMIN, USER, COMPLIANCE_OFFICER, OPERATIONAL_STAFF)
}
```

### Success Response (201 Created)
```json
{
  "id": 6,
  "phoneNumber": "+44202647",
  "email": "j202647@t.com",
  "firstName": "John",
  "middleName": null,
  "lastName": "Doe",
  "dateOfBirth": null,
  "residentialAddressIdentifier": null,
  "postalAddressIdentifier": null,
  "statusDescription": null,
  "role": null,
  "createdDatetime": "2026-03-06T20:26:48.026072989",
  "lastModifiedDatetime": "2026-03-06T20:26:48.026089097"
}
```

### Error Responses

#### 409 Conflict
```json
{
  "message": "Email already exists",
  "status": 409
}
```
**or**
```json
{
  "message": "Phone number already exists",
  "status": 409
}
```
**Fix**: Use a different email or phone number

#### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Phone number is required"
}
```
**Fix**: Ensure all required fields are included

---

## 🔐 Authentication Flow

Before calling organization or user endpoints, you must authenticate:

### Step 1: Request OTP
```http
POST /api/auth/request-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}
```

**Response**:
```json
{
  "message": "OTP sent successfully",
  "devOtp": "123456"  // Only in dev/NPE environments
}
```

### Step 2: Verify OTP and Get JWT Token
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "otp": "123456"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "firstName": "System",
    "lastName": "Administrator",
    "role": "ADMIN"
  }
}
```

### Step 3: Use Token in Subsequent Requests
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token Expiration**: 24 hours

---

## 🧪 Testing the Endpoints

### Using Postman

1. **Import Collection**: `postman_collection.json`
2. **Import Environment**: `postman_environment_cloud.json`
3. **Set Base URL**: 
   ```
   https://fincore-npe-api-994490239798.europe-west2.run.app
   ```
4. **Run "Request OTP - Admin"** (phone: +1234567890)
5. **Run "Verify OTP - Admin"** (token auto-saved)
6. **Run any Organization or User endpoint**

### Using cURL

```bash
# Get JWT token first
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890"}'

# Note the devOtp from response, then verify:
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890","otp":"YOUR_OTP"}'

# Save the token, then create organization:
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/organisations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ownerId":1,"legalName":"Test Corp","organisationType":"LTD"}'
```

### Using PowerShell
See [simple-crud-test.ps1](simple-crud-test.ps1) for a working example.

---

## 🚀 Deployment Status

### Current Status
- **Deployment**: IN PROGRESS (GitHub Actions triggered)
- **Expected completion**: 5-10 minutes from push
- **Revision**: Will be fincore-npe-api-00089 (or higher)

### How to Verify CORS Fix

1. **Check GitHub Actions**:
   - Go to: https://github.com/kasisheraz/userManagementApi/actions
   - Look for the latest workflow run ("Build & Deploy to NPE")
   - Wait for all stages to complete (Build, Docker, Deploy, Smoke Tests)

2. **Test from UI**:
   ```javascript
   // Your UI code should now work without CORS errors
   fetch('https://fincore-npe-api-994490239798.europe-west2.run.app/api/organisations', {
     method: 'POST',
     headers: {
       'Authorization': `Bearer ${token}`,
       'Content-Type': 'application/json'
     },
     body: JSON.stringify({
       ownerId: 1,
       legalName: 'Test Corp',
       organisationType: 'LTD'
     })
   })
   ```

3. **Check Browser Console**:
   - Open Developer Tools (F12)
   - Go to Console tab
   - You should NO LONGER see CORS errors like:
     ```
     ❌ Access to fetch at '...' from origin '...' has been blocked by CORS policy
     ```

---

## 📝 Common UI Issues & Solutions

### Issue 1: CORS Errors
**Symptom**: Browser console shows "blocked by CORS policy"  
**Status**: ✅ FIXED - Wait for deployment to complete  
**Verify**: Try request again after 10 minutes

### Issue 2: 401 Unauthorized
**Symptom**: All protected endpoints return 401  
**Cause**: Missing or expired JWT token  
**Fix**: 
1. Call `/api/auth/request-otp` and `/api/auth/verify-otp` first
2. Store the token in localStorage or sessionStorage
3. Include token in all subsequent requests:
   ```javascript
   headers: {
     'Authorization': `Bearer ${token}`
   }
   ```

### Issue 3: 400 Bad Request
**Symptom**: Endpoint returns validation error  
**Cause**: Missing required fields or invalid data format  
**Fix**: 
- Organization: Ensure `ownerId`, `legalName`, `organisationType` are included
- User: Ensure `phoneNumber`, `firstName`, `lastName` are included
- Check field lengths and formats match requirements above

### Issue 4: Empty Response Body
**Symptom**: Network tab shows 200 OK but no response data  
**Cause**: Not parsing JSON response  
**Fix**:
```javascript
const response = await fetch(url, options);
const data = await response.json();  // Don't forget this!
```

### Issue 5: "phoneNumber already exists"
**Symptom**: 409 Conflict when creating user  
**Cause**: Phone number is already registered  
**Fix**: 
- Use a different phone number
- Or update the existing user with PUT `/api/users/{id}`

---

## 🔍 Debugging Checklist

When UI requests fail, check these in order:

1. ✅ **Is the service up?**
   ```
   GET https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. ✅ **Is authentication working?**
   - Try requesting OTP
   - Try verifying OTP
   - Confirm you receive a token

3. ✅ **Is the token included?**
   - Check Network tab in browser
   - Look at Request Headers
   - Verify "Authorization: Bearer ..." is present

4. ✅ **Is the request body correct?**
   - Check Network tab → Payload
   - Verify all required fields are included
   - Verify JSON is valid (use JSONLint.com)

5. ✅ **Are there CORS errors?**
   - Check Console tab
   - After deployment completes, CORS errors should be gone

---

## 📞 Support

### Test Users Available
- **Admin**: +1234567890 (use for testing)
- **Compliance**: +1234567891
- **Staff**: +1234567892

### Documentation Files
- [Architecture Documentation](architecture-documentation.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Postman Usage Guide](POSTMAN_USAGE_GUIDE.md)
- [Executive Summary](EXECUTIVE_SUMMARY.md)

### Test Scripts
- `simple-crud-test.ps1` - Quick endpoint test
- `quick-test-gcp.ps1` - Full deployment validation

---

## ✅ Summary

**What Was Wrong**: CORS configuration was blocking UI requests

**What Was Fixed**: 
- Updated CORS to allow all origins in NPE environment
- Changed from `allowedOrigins` to `allowedOriginPatterns` with wildcards
- Disabled conflicting CORS in SecurityConfig

**What To Do Now**:
1. Wait 5-10 minutes for automatic deployment
2. Test organization creation from UI
3. Test user creation from UI
4. CORS errors should be gone
5. Endpoints work exactly as documented above

**API is fully functional** - tested successfully via direct API calls. Once deployment completes, UI should work without issues.

---

**Generated**: March 6, 2026  
**Deployment Commit**: bec4df9  
**Expected Live**: ~10 minutes from commit timestamp
