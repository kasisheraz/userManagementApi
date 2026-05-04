# UAT Login Guide - Using Browser DevTools

This guide shows you how to login to the UAT environment using the test account and browser DevTools to view the OTP code.

## 🌐 UAT Environment URLs

- **Frontend**: https://fincore-webui-uat-994490239798.europe-west2.run.app
- **Backend API**: https://fincore-uat-api-994490239798.europe-west2.run.app

## 👤 Test Account

- **Phone Number**: `+447700900000`
- **Email**: admin@fincore.test
- **Role**: Admin
- **OTP**: Retrieved from API response (see below)

---

## 📋 Step-by-Step Login Process

### Step 1: Open UAT Application

1. Open your browser (Chrome recommended)
2. Navigate to: https://fincore-webui-uat-994490239798.europe-west2.run.app/login
3. You should see the FinCore login page

### Step 2: Open Browser DevTools

**Keyboard Shortcuts:**
- **Chrome/Edge**: Press `F12` or `Ctrl+Shift+I` (Windows) / `Cmd+Option+I` (Mac)
- **Firefox**: Press `F12` or `Ctrl+Shift+K` (Windows) / `Cmd+Option+K` (Mac)

**Or via Menu:**
- Chrome: `⋮` (three dots) → More Tools → Developer Tools
- Firefox: `☰` (hamburger) → More Tools → Web Developer Tools
- Edge: `...` (three dots) → More Tools → Developer Tools

### Step 3: Navigate to Network Tab

1. Click on the **Network** tab in DevTools (top row)
2. ✅ Make sure recording is enabled (red dot or "Record" button should be active)
3. Optional: Clear existing logs (trash icon) for a clean view

![DevTools Network Tab](https://i.imgur.com/example-network.png)

### Step 4: Request OTP

1. In the login page, enter phone number: `+447700900000`
2. Click **"Request OTP"** button
3. Wait for the request to complete

### Step 5: Find the API Request in DevTools

1. In the Network tab, look for a request named: **`request-otp`**
2. Click on this request to open the details panel
3. You should see:
   - **Status**: `200 OK` (green)
   - **Method**: `POST`
   - **URL**: `https://fincore-uat-api-.../api/auth/request-otp`

### Step 6: View the Response with OTP

1. In the details panel (right side), click the **"Response"** tab
2. You'll see JSON response like:
```json
{
    "message": "OTP sent to +********0000. Please verify to complete authentication.",
    "phoneNumber": "+447700900000",
    "expiresIn": 300,
    "devOtp": "123456"  ← THIS IS YOUR OTP CODE!
}
```

3. **Copy the `devOtp` value** (e.g., `123456`)

> 💡 **Why is devOtp shown?**  
> In UAT and NPE (non-production) environments, the API returns the OTP in the response for easy testing. This makes it convenient for developers and testers to login without needing SMS access. In production, this field is **never** returned for security.

### Step 7: Verify OTP and Login

1. Go back to the login page
2. Enter the OTP code you copied from DevTools
3. Click **"Verify OTP"** button
4. ✅ You should be redirected to the Dashboard

---

## 🖼️ Visual Guide with Screenshots

### Before Requesting OTP
```
┌─────────────────────────────────────────┐
│  FinCore - Login                        │
│                                          │
│  Phone Number: [+447700900000      ]   │
│                                          │
│  [  Request OTP  ]                      │
└─────────────────────────────────────────┘
```

### DevTools Network Tab (After Request OTP)
```
┌─────────────────────────────────────────────────────────────┐
│ DevTools - Network                                          │
├─────────────────────────────────────────────────────────────┤
│ Name            | Status | Type | Size | Time               │
├─────────────────────────────────────────────────────────────┤
│ request-otp     |  200   | xhr  | 245B | 450ms ← Click this│
│ ...             |  ...   | ...  | ...  | ...                │
└─────────────────────────────────────────────────────────────┘

┌─── Response ──────────────────────────┐
│ {                                     │
│   "message": "OTP sent to...",        │
│   "phoneNumber": "+447700900000",     │
│   "expiresIn": 300,                   │
│   "devOtp": "123456"  ← Copy this     │
│ }                                     │
└───────────────────────────────────────┘
```

### After Entering OTP
```
┌─────────────────────────────────────────┐
│  FinCore - Login                        │
│                                          │
│  Phone Number: +447700900000            │
│  ✓ OTP Sent                             │
│                                          │
│  OTP Code: [123456]                     │
│                                          │
│  [  Verify OTP  ]                       │
└─────────────────────────────────────────┘
```

---

## 🔧 Alternative Methods

### Method 2: Using Browser Console

1. Open DevTools → **Console** tab
2. Request OTP through the UI
3. The OTP might appear in console logs (if logging is enabled)

### Method 3: Using Postman

See [POSTMAN_UAT_GUIDE.md](./POSTMAN_UAT_GUIDE.md) for API testing with Postman.

### Method 4: Using cURL (Command Line)

```bash
# Request OTP
curl -X POST https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+447700900000"}'

# Response includes devOtp
{
  "message": "OTP sent to +********0000...",
  "phoneNumber": "+447700900000",
  "expiresIn": 300,
  "devOtp": "123456"
}

# Verify OTP
curl -X POST https://fincore-uat-api-994490239798.europe-west2.run.app/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+447700900000","otp":"123456"}'
```

---

## ❓ Troubleshooting

### ❌ Problem: "User not found" error

**Cause**: The test user might not exist in the database.

**Solutions**:
1. Check you're using the correct phone number: `+447700900000`
2. Verify you're on the UAT environment (URL should have `-uat-` in it)
3. Check that the frontend is calling the UAT API (not NPE):
   - In Network tab: Request URL should have `fincore-uat-api` (not `npe`)

### ❌ Problem: devOtp not in response

**Cause**: Backend not configured as non-production environment.

**Check**:
1. Backend environment variable: `SPRING_PROFILES_ACTIVE=uat`
2. API health endpoint: https://fincore-uat-api-.../actuator/health
3. Contact admin if issue persists

### ❌ Problem: OTP expired

**Cause**: OTPs expire after 5 minutes (300 seconds).

**Solution**:
1. Request a new OTP
2. Complete verification within 5 minutes

### ❌ Problem: Can't see Network tab requests

**Solutions**:
1. Make sure DevTools is open **before** clicking "Request OTP"
2. Check "Preserve log" checkbox in Network tab (survives page reloads)
3. Clear browser cache and try again (Ctrl+Shift+Del)

### ❌ Problem: Frontend calling wrong API

**Symptoms**: Network shows requests to `fincore-npe-api` instead of `fincore-uat-api`

**Solution**: Frontend deployment issue - contact admin. The build should use `.env.uat` configuration.

---

## 🔐 Security Notes

### Why devOtp Works This Way

- **UAT/NPE**: Returns OTP in response for testing convenience
- **Production**: OTP only sent via SMS, never in response
- **Profile Detection**: Backend checks `SPRING_PROFILES_ACTIVE` to determine environment

### Test Account Security

The test account (`+447700900000`) is:
- ✅ Safe for testing in UAT/NPE
- ✅ Not a real phone number
- ✅ Admin role for full feature access
- ❌ Should NOT exist in production database

---

## 📚 Related Documentation

- [POSTMAN_UAT_GUIDE.md](./POSTMAN_UAT_GUIDE.md) - API testing with Postman
- [UAT_NPE_PARITY_FIX.md](./UAT_NPE_PARITY_FIX.md) - Why devOtp was missing initially
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Comprehensive testing guide

---

## 💡 Pro Tips

### 1. Keep DevTools Open
Keep DevTools open when testing - you'll catch API errors immediately.

### 2. Use Network Filter
In Network tab, filter by "Fetch/XHR" to see only API requests.

### 3. Copy as cURL
Right-click any request → "Copy" → "Copy as cURL" to replay requests.

### 4. Response Preview
Use the "Preview" tab (next to "Response") for formatted JSON view.

### 5. Persistent Logs
Enable "Preserve log" to keep requests across page navigations.

---

## 🎯 Quick Reference Card

### Essential Info
| Item | Value |
|------|-------|
| **UAT URL** | https://fincore-webui-uat-994490239798.europe-west2.run.app |
| **Test Phone** | +447700900000 |
| **Open DevTools** | F12 or Ctrl+Shift+I |
| **Network Tab** | Find "request-otp" request |
| **Get OTP** | Response → devOtp field |
| **OTP Validity** | 5 minutes (300 seconds) |

### Quick Login Flow
```
1. Open UAT URL
2. Press F12 → Network tab
3. Enter: +447700900000
4. Click: Request OTP
5. DevTools → Find: request-otp
6. Response → Copy: devOtp value
7. Enter OTP → Click: Verify OTP
8. ✅ Logged in!
```

---

**Last Updated**: May 4, 2026  
**Environment**: UAT (User Acceptance Testing)  
**Support**: Contact DevOps team if issues persist
