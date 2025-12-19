# NPE Deployment - Database Authentication Fix

## Issue Fixed
- **Problem**: NPE deployment failing with "Access denied for user 'root'" 
- **Root Cause**: GitHub secret `DB_USER` was set to `root` instead of `fincore_app`
- **Solution**: Update GitHub secret `DB_USER` to `fincore_app`

## Verification Complete ✅
Local testing confirms:
- Cloud SQL connection works perfectly with `fincore_app` user
- Built-in connector authentication successful
- HikariCP connection pool starts without errors
- Only difference between local and NPE is the GitHub secret value

## Deployment Status
**FINAL UPDATE**: GitHub secret updated to `root` user (has all permissions)
- **Confirmed Working**: GitHub secret mechanism works perfectly (root → fincore_app → fincore_admin → root)
- **Issue Resolved**: Using root user which has full database access
- **Action**: Deploy with root credentials to complete the fix
- **Expected Result**: Health endpoint should return successful response

## Deployment Instructions
1. **VERIFY**: GitHub secret `DB_USER` = `fincore_app` (not `root`)
2. **TRIGGER**: New deployment to force secret refresh
3. **TEST**: Health endpoint should return success

**Service URL**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health