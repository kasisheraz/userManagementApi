# UI Not Showing Data - Troubleshooting Guide

## Current Status
- ✅ Service is UP and healthy
- ❌ CORS returning 403 Forbidden (deployment may not be complete)
- ❓ Test data import needs verification

## Issue 1: CORS Still Blocking (403 Forbidden)

### Check Deployment Status
1. **Go to GitHub Actions**: https://github.com/kasisheraz/userManagementApi/actions
2. **Look for latest workflow**: "Build & Deploy to NPE"
3. **Check status**:
   - ✅ Green checkmark = Deployed successfully
   - 🔄 Yellow spinner = Still deploying (wait 5-10 minutes)
   - ❌ Red X = Deployment failed

### If Deployment is Complete
Check the actual deployed revision:
```powershell
# From Cloud Console
# Go to: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api?project=fincore-platform
# Look for latest revision number
```

### If Still Getting 403 After Deployment
The CORS fix might need adjustment. Check browser console for exact error message.

## Issue 2: Verify Test Data Was Actually Imported

### Option 1: Cloud SQL Console (Easiest)
1. Go to [Cloud SQL Instances](https://console.cloud.google.com/sql/instances/fincore-npe-db?project=fincore-platform)
2. Click **"Connect using Cloud Shell"** or **"Open Cloud Shell"**
3. Run these queries:

```sql
-- Connect to database
USE fincore_db;

-- Check record counts
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'roles', COUNT(*) FROM roles
UNION ALL SELECT 'permissions', COUNT(*) FROM permissions
UNION ALL SELECT 'organisation', COUNT(*) FROM organisation
UNION ALL SELECT 'kyc_documents', COUNT(*) FROM kyc_documents
UNION ALL SELECT 'customer_kyc_verification', COUNT(*) FROM customer_kyc_verification
UNION ALL SELECT 'aml_screening_results', COUNT(*) FROM aml_screening_results
UNION ALL SELECT 'questionnaire_questions', COUNT(*) FROM questionnaire_questions
UNION ALL SELECT 'customer_answers', COUNT(*) FROM customer_answers;

-- Expected results:
-- users: 12 (or more if you created users before)
-- roles: 4
-- permissions: 17
-- organisation: 8
-- kyc_documents: 16
-- customer_kyc_verification: 10
-- aml_screening_results: 14
-- questionnaire_questions: 20
-- customer_answers: 48
```

### Option 2: Check Sample Data
```sql
-- Check if new users exist
SELECT User_Identifier, Phone_Number, First_Name, Last_Name, Email
FROM users
WHERE Phone_Number IN ('+447911123456', '+447911123457', '+447911123460', '+447911123461');

-- Should return 4 users:
-- Sarah Williams, John Smith, Olivia Taylor, James Wilson

-- Check if new organizations exist
SELECT Organisation_Identifier, Legal_Name, Organisation_Type_Description, Status_Description
FROM organisation
WHERE Legal_Name LIKE '%Digital Payments%' OR Legal_Name LIKE '%GlobalTransfer%';

-- Should return at least 2 organizations
```

### Option 3: Check Import History
```sql
-- Check when data was last modified
SELECT 
    'users' as table_name, 
    MAX(Created_Datetime) as last_created,
    COUNT(*) as total_count
FROM users
UNION ALL
SELECT 'organisation', MAX(Created_Datetime), COUNT(*) FROM organisation;
```

## Issue 3: UI Not Showing Data (Even After CORS Fixed)

### Possible Causes & Solutions

#### A. UI Caching
**Symptom**: Old empty data still showing  
**Solution**:
1. Hard refresh: `Ctrl + Shift + R` (Windows) or `Cmd + Shift + R` (Mac)
2. Clear browser cache
3. Open in incognito/private window

#### B. UI Not Calling API
**Symptom**: No network requests in browser DevTools  
**Solution**:
1. Open Browser DevTools (F12)
2. Go to **Network** tab
3. Refresh page
4. Check if API calls are being made
5. If no calls, UI may need configuration update

#### C. API Returning Empty Arrays
**Symptom**: Network shows 200 OK but empty data  
**Solution**:
1. Check API response in Network tab
2. If `[]` or empty, data didn't import
3. Re-run import process

#### D. Authorization Issues
**Symptom**: All requests return 401 or 403  
**Solution**:
1. Check if user is logged in
2. Verify JWT token is being sent
3. Check token hasn't expired
4. Try logging out and back in

#### E. Wrong API Endpoint
**Symptom**: 404 Not Found errors  
**Solution**:
1. Verify UI is pointing to: `https://fincore-npe-api-994490239798.europe-west2.run.app`
2. Check endpoint paths match API documentation

#### F. CORS Still Blocking
**Symptom**: Red CORS errors in console  
**Solution**:
1. Wait for deployment to complete
2. Check GitHub Actions status
3. Verify new revision is active

## Step-by-Step UI Debugging

### 1. Open Browser DevTools
Press `F12` or right-click → Inspect

### 2. Check Console Tab
Look for:
- ❌ **CORS errors** → CORS not fixed yet (wait for deployment)
- ❌ **401 Unauthorized** → Login again
- ❌ **404 Not Found** → Wrong endpoint URL
- ❌ **Network Error** → API is down
- ✅ **No errors** → Check Network tab

### 3. Check Network Tab
1. Clear network log
2. Navigate to a page (e.g., Users page)
3. Look for API calls:
   - **GET /api/users** should appear
   - Click on it to see response
   - Check **Preview** or **Response** tab
   - Should show array of user objects

### 4. Check Application Tab
1. Go to **Application** → **Local Storage**
2. Check for JWT token
3. If missing, user needs to login
4. If exists, check if expired

### 5. Test Direct API Call
Open browser console and run:
```javascript
fetch('https://fincore-npe-api-994490239798.europe-west2.run.app/api/users', {
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN_HERE'
  }
})
.then(r => r.json())
.then(d => console.log('Users:', d))
.catch(e => console.error('Error:', e));
```

## Quick Verification Checklist

Use this checklist to diagnose the issue:

- [ ] **Data imported?** Run SQL queries in Cloud SQL Console
- [ ] **CORS deployment complete?** Check GitHub Actions
- [ ] **Service healthy?** Visit `/actuator/health`
- [ ] **UI making API calls?** Check Network tab in DevTools
- [ ] **CORS errors in console?** Check Console tab in DevTools
- [ ] **Authorization header present?** Check request headers in Network tab
- [ ] **API returning data?** Check API response in Network tab
- [ ] **Browser cache cleared?** Try hard refresh or incognito

## Next Steps Based on Findings

### If Data NOT Imported (Queries return 0 or old counts)
1. Check Cloud SQL import status
2. Look for import errors in Cloud SQL logs
3. Try re-importing the SQL file
4. Check for foreign key violations

### If CORS Still Blocking (after 15+ minutes)
1. Verify commit was pushed: `git log -1`
2. Check if CI/CD ran: GitHub Actions page
3. Look for deployment errors in Actions logs
4. May need to manually redeploy or fix CORS config

### If Data Imported BUT UI Shows Empty
1. Verify API returns data (test with Postman)
2. Check UI console for errors
3. Verify UI endpoints match API
4. Check UI authentication flow
5. Test with different user account

### If Everything Works in Postman But Not UI
1. 100% CORS issue
2. Check browser console for CORS errors
3. Wait for CORS deployment
4. Verify CORS headers allow your UI origin

## Contact Points for Each Issue

| Issue | What to Check | Documentation |
|-------|---------------|---------------|
| Data not imported | Cloud SQL Console | [TEST_DATA_GUIDE.md](TEST_DATA_GUIDE.md) |
| CORS errors | GitHub Actions, Browser Console | [UI_INTEGRATION_GUIDE.md](UI_INTEGRATION_GUIDE.md) |
| API errors | Postman, Cloud Run Logs | [POSTMAN_USAGE_GUIDE.md](POSTMAN_USAGE_GUIDE.md) |
| Deployment issues | GitHub Actions, Cloud Run | [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) |

## Expected Timeline

- **Data Import**: 2-3 minutes (via Cloud SQL Console)
- **CORS Deployment**: 5-10 minutes (via GitHub Actions)
- **UI Should Work**: Within 15 minutes of git push

**Current Status**: ~10-15 minutes since CORS fix was pushed

## Immediate Actions

1. **FIRST**: Verify data in database using Cloud SQL Console queries above
2. **SECOND**: Check GitHub Actions for deployment status
3. **THIRD**: Open UI in browser with DevTools (F12) and check Console + Network tabs
4. **Report back with**:
   - Record counts from database
   - GitHub Actions status (running/success/failed)
   - Any errors in browser console
   - Network requests being made (or not)

---

**Created**: March 6, 2026  
**Purpose**: Troubleshoot UI data display issues  
**Status**: Awaiting deployment completion (~10-15 min wait recommended)
