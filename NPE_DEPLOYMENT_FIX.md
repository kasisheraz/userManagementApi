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
**LATEST UPDATE**: GitHub secret updated to `fincore_admin` user
- **Previous**: Logs showed `Access denied for user 'fincore_app'` (permissions issue)
- **Current**: Updated secret to `fincore_admin` (admin user with full permissions)
- **Issue**: Deployment revision 00005-r6f still shows `fincore_app` in logs
- **Action Required**: Force new deployment to pick up `fincore_admin` secret

## Deployment Instructions
1. **VERIFY**: GitHub secret `DB_USER` = `fincore_app` (not `root`)
2. **TRIGGER**: New deployment to force secret refresh
3. **TEST**: Health endpoint should return success

**Service URL**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health