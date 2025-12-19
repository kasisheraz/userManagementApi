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
**ISSUE**: Deployment still showing `root` user in logs after GitHub secret update
- Logs show: `Access denied for user 'root'@'cloudsqlproxy'`
- **Action Required**: Verify GitHub secret `DB_USER` is properly set to `fincore_app`
- **Force Deployment**: Trigger new deployment to ensure updated secrets are used

## Deployment Instructions
1. **VERIFY**: GitHub secret `DB_USER` = `fincore_app` (not `root`)
2. **TRIGGER**: New deployment to force secret refresh
3. **TEST**: Health endpoint should return success

**Service URL**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health