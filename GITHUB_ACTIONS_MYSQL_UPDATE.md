# GitHub Actions - MySQL Configuration Update

**Date**: December 17, 2025  
**Status**: ⚠️ **NEEDS ACTIVATION - Updated for MySQL**

---

## What Changed

The GitHub Actions deployment workflow has been **updated** to deploy with **MySQL Cloud SQL** instead of H2 database.

### Updated Configuration
- ✅ Spring profile changed from `h2` to `gcp`
- ✅ MySQL environment variables added
- ✅ Cloud SQL instance connection configured
- ✅ Memory increased to 1Gi (from 512Mi)
- ✅ Timeout increased to 10 minutes
- ✅ Smoke test password updated to correct admin password

---

## Required GitHub Secrets

You need to add these secrets to GitHub repository settings:

**Go to**: https://github.com/kasisheraz/userManagementApi/settings/secrets/actions

### Existing Secrets (Should Already Exist)
1. **GCP_PROJECT_ID**
   - Value: `project-07a61357-b791-4255-a9e`

2. **GCP_SA_KEY**
   - Value: Contents of your service account key JSON file

3. **GCP_SERVICE_ACCOUNT**
   - Value: `fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com`

### New Secrets Required for MySQL
4. **DB_PASSWORD**
   - Value: `FinCore2024Secure`
   - Description: MySQL database password

5. **CLOUDSQL_INSTANCE**
   - Value: `project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db`
   - Description: Cloud SQL connection string

---

## How to Add GitHub Secrets

### Using GitHub Web UI:

1. Go to your repository: https://github.com/kasisheraz/userManagementApi

2. Click **Settings** → **Secrets and variables** → **Actions**

3. Click **New repository secret** button

4. For each secret:
   - Enter the **Name** (e.g., `DB_PASSWORD`)
   - Enter the **Secret** value
   - Click **Add secret**

### Using GitHub CLI:

```bash
cd C:\Development\git\userManagementApi

# Add DB_PASSWORD secret
gh secret set DB_PASSWORD --body "FinCore2024Secure"

# Add CLOUDSQL_INSTANCE secret
gh secret set CLOUDSQL_INSTANCE --body "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"
```

---

## Testing the Updated Workflow

### Option 1: Commit and Push Changes

```bash
cd C:\Development\git\userManagementApi

# Add the updated workflow file
git add .github/workflows/deploy-npe.yml

# Commit the changes
git commit -m "ci: Update deployment workflow for MySQL Cloud SQL"

# Push to main (this will trigger deployment)
git push origin main
```

### Option 2: Manual Trigger (if workflow supports it)

Go to: https://github.com/kasisheraz/userManagementApi/actions  
Click on "Build & Deploy to NPE" → "Run workflow"

---

## What Happens on Deployment

When you push to `main` branch, GitHub Actions will:

1. ✅ **Build** - Compile Java code with Maven
2. ✅ **Test** - Run all unit tests
3. ✅ **Docker** - Build and push container to GCR
4. ✅ **Deploy** - Deploy to Cloud Run with:
   - Spring profile: `gcp` (MySQL mode)
   - Database: Cloud SQL `fincore-npe-db`
   - Connection: Via Cloud SQL Proxy
   - Memory: 1GB
   - Timeout: 10 minutes
5. ✅ **Health Check** - Verify `/actuator/health` endpoint
6. ✅ **Smoke Test** - Test login with admin credentials

**Total Time**: ~10-15 minutes

---

## Current Manual Deployment vs GitHub Actions

### Manual Deployment (What You Did Today)
```bash
# Build Docker image
gcloud builds submit --tag gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest

# Deploy to Cloud Run
gcloud run deploy fincore-npe-api \
  --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest \
  --region=europe-west2 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=gcp,DB_NAME=my_auth_db,DB_USER=fincore_app,DB_PASSWORD=FinCore2024Secure,CLOUDSQL_INSTANCE=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db" \
  --set-secrets="JWT_SECRET=fincore-npe-jwt-secret:1" \
  --add-cloudsql-instances=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db \
  --memory=1Gi --cpu=1 --timeout=10m --max-instances=2
```

### GitHub Actions Deployment (Automated)
```bash
# Just push your code!
git push origin main

# Everything else happens automatically:
# - Build, test, docker build, push, deploy, verify
```

---

## Verification After Deployment

Once deployed via GitHub Actions, verify it works:

```bash
# Test health endpoint
curl https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health

# Test login
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'
```

Expected response:
```json
{
  "token": "api-key-...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

---

## Troubleshooting

### Deployment Fails?

1. **Check GitHub Actions logs**:
   - Go to: https://github.com/kasisheraz/userManagementApi/actions
   - Click on the failed workflow run
   - Review the logs for each step

2. **Common Issues**:
   - ❌ Missing secrets → Add all 5 required secrets
   - ❌ Wrong secret values → Double-check the values
   - ❌ Service account permissions → Verify GCP IAM roles
   - ❌ Cloud SQL connection → Check instance name format

3. **View Cloud Run logs**:
   ```bash
   gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" --limit=50
   ```

---

## Next Steps

1. ✅ **Add the 2 new secrets** to GitHub (DB_PASSWORD, CLOUDSQL_INSTANCE)

2. ✅ **Commit the updated workflow**:
   ```bash
   git add .github/workflows/deploy-npe.yml GITHUB_ACTIONS_MYSQL_UPDATE.md
   git commit -m "ci: Update deployment workflow for MySQL Cloud SQL"
   git push origin main
   ```

3. ✅ **Watch the deployment**: 
   - Go to: https://github.com/kasisheraz/userManagementApi/actions
   - Monitor the workflow execution

4. ✅ **Verify the deployment**:
   - Test the API endpoints
   - Check Cloud Run service status

---

## Benefits of GitHub Actions

### Before (Manual)
- 😫 Build, test, push, deploy manually
- 😫 Remember all the command flags
- 😫 Wait and watch each step
- 😫 No automated testing before deploy
- 😫 Risk of human error

### After (GitHub Actions)
- 🎉 Push code → Everything automatic
- 🎉 Always tested before deploy
- 🎉 Consistent deployments
- 🎉 Full audit trail in GitHub
- 🎉 Roll back easily via Git

---

## Questions?

- Workflow details: See `.github/workflows/deploy-npe.yml`
- GCP setup: See `GITHUB_ACTIONS_SETUP.md`
- Architecture: See `CICD_ARCHITECTURE.md`
- Quick start: See `GITHUB_ACTIONS_QUICKSTART.md`

**Your deployment is working manually with MySQL. Now automate it with GitHub Actions!** 🚀
