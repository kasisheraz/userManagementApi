# GitHub Actions Troubleshooting & Manual Deployment Guide

## Issue: GitHub Actions Not Triggering

### Current Status:
- ✅ Workflow file fixed (VPC connector configuration corrected)
- ✅ Code changes committed and pushed to `main` branch
- ❌ GitHub Actions workflow NOT running automatically
- ❌ No new builds in Cloud Build since December 31, 2025

### Latest Commits Need Deployment:
1. **97d1594** - Add GET all endpoints for KYC Documents, KYC Verifications, Customer Answers
2. **9eabb4b** - Fix GitHub Actions deployment (VPC connector fix)
3. **eaf3f78** - Trigger workflow test

---

## Quick Fix: Manual Deployment

### Option 1: Deploy Using gcloud CLI (Recommended)

```powershell
# Set variables
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$SERVICE_NAME = "fincore-npe-api"
$REGION = "europe-west2"
$IMAGE = "gcr.io/$PROJECT_ID/fincore-api:latest"

# Deploy
gcloud run deploy $SERVICE_NAME `
    --image=$IMAGE `
    --region=$REGION `
    --platform=managed `
    --allow-unauthenticated `
    --memory=1Gi `
    --cpu=1 `
    --timeout=900 `
    --max-instances=3 `
    --min-instances=0 `
    --vpc-connector=npe-connector `
    --vpc-egress=private-ranges-only `
    --port=8080 `
    --project=$PROJECT_ID
```

**Note:** This uses the existing image from December 31st. It won't have your latest code changes.

### Option 2: Build & Deploy from Source (Gets Latest Code)

```powershell
# This will build from your local code and deploy
gcloud run deploy fincore-npe-api `
    --source=. `
    --region=europe-west2 `
    --platform=managed `
    --allow-unauthenticated `
    --memory=1Gi `
    --cpu=1 `
    --timeout=900 `
    --max-instances=3 `
    --min-instances=0 `
    --vpc-connector=npe-connector `
    --vpc-egress=private-ranges-only `
    --set-env-vars="SPRING_PROFILES_ACTIVE=npe" `
    --port=8080
```

**This is the best option** - it will:
- Build a Docker image from your current code
- Push it to Artifact Registry
- Deploy to Cloud Run
- Takes 3-5 minutes

---

## GitHub Actions Troubleshooting Steps

### 1. Check if Actions are Enabled

Visit: https://github.com/kasisheraz/userManagementApi/settings/actions

Ensure:
- [ ] "Allow all actions and reusable workflows" is selected
- [ ] Actions are not disabled for this repository

### 2. Check Workflow Runs

Visit: https://github.com/kasisheraz/userManagementApi/actions

Look for:
- Recent workflow runs (should see them for commits 97d1594, 9eabb4b, eaf3f78)
- If no runs appear, workflow is not triggering
- If runs appear but failed, check error logs

### 3. Check Repository Permissions

GitHub Actions needs these permissions:
1. **Repository Secrets** - Go to Settings → Secrets → Actions
   - Should have: `GCP_SA_KEY`, `GCP_PROJECT_ID`, `GCP_SERVICE_ACCOUNT`, `DB_USER`, `NEW_DB_PASSWORD`

2. **Workflow Permissions** - Settings → Actions → General → Workflow permissions
   - Should be: "Read and write permissions"

### 4. Check Workflow File

The workflow should trigger on push to `main`:

```yaml
on:
  push:
    branches: [ main, public-ip-connection ]
```

### 5. Common Issues

**Issue: Workflow file has syntax errors**
- Run: `yamllint .github/workflows/deploy-npe.yml`
- Or use: https://www.yamllint.com/

**Issue: Branch protection rules preventing workflow**
- Check: Settings → Branches → main → Branch protection rules
- Ensure workflows are allowed to run

**Issue: GitHub Actions disabled for organization**
- Organization admins may have disabled Actions
- Contact organization owner

---

## Verification After Deployment

### 1. Check Deployed Version

```powershell
$info = Invoke-RestMethod -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/system/info"
Write-Host "Build: $($info.build)"
Write-Host "Time: $($info.timestamp)"
```

Expected build: `97d1594` or newer

### 2. Run Full API Tests

```powershell
.\test-all-phase2-apis.ps1
```

Expected result: **12/12 APIs passing**

Including the 3 newly fixed APIs:
- ✅ GET /api/kyc-documents
- ✅ GET /api/kyc-verifications  
- ✅ GET /api/customer-answers

---

## What Changed in Latest Code

### Commit 97d1594: Critical API Fixes

**Problem:** 3 APIs returned 500 errors because they lacked simple GET endpoints

**Fixed:**
1. **KycDocumentController** - Added `GET /api/kyc-documents` (list all)
2. **KycVerificationController** - Added `GET /api/kyc-verifications` (list all)
3. **CustomerAnswerController** - Added `GET /api/customer-answers` (list all)
4. **KycVerificationService** - Added `getAllVerifications()` method
5. **CustomerAnswerService** - Added `getAllAnswers()` method

**Before:** UI got 500 errors when trying to load these pages
**After:** All pages load successfully (may show empty lists if no data)

### Commit 9eabb4b: GitHub Actions Fix

**Problem:** Deployment failed with VPC connector error
```
ERROR: Cannot remove VPC connector with VPC egress set to "all-traffic"
```

**Fixed:**
- Removed `--clear-vpc-connector` flag
- Added `--vpc-connector=npe-connector` 
- Set `--vpc-egress=private-ranges-only`

Cloud Run needs VPC connector to access Cloud SQL on private IP.

---

## Contact

If GitHub Actions still doesn't work after checking all the above:

1. Create a GitHub Issue in the repository
2. Contact GitHub Support
3. Consider setting up manual deployments as a temporary workaround

Manual deployments work perfectly - you just need to remember to run them after each code change.

---

## Quick Reference

### Deploy Latest Code Now:
```powershell
.\quick-deploy.ps1
```

### Test All APIs:
```powershell
.\test-all-phase2-apis.ps1
```

### Check Deployment Status:
```powershell
$info = Invoke-RestMethod -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/system/info"
$info | ConvertTo-Json
```
