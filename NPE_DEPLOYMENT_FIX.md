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

## Deployment Instructions
1. Update GitHub secret: `DB_USER` = `fincore_app`
2. Trigger this deployment by pushing this commit
3. Verify health endpoint returns success

**Service URL**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health