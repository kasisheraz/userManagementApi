# Cloud Run Deployment - Manual Step-by-Step Guide

**Status:** ✅ Application built successfully (JAR created)  
**Date:** December 15, 2025  
**Next:** Docker image build and GCP deployment

---

## Quick Start

### Option 1: Automated Deployment (PowerShell)

If Docker is installed, run the automated script:

```powershell
cd c:\Development\git\userManagementApi
.\deploy-to-cloud-run-npe.ps1
```

### Option 2: Manual Deployment (Step-by-Step)

Follow the manual steps below.

---

## Prerequisites Installation

### 1. Verify Maven Build ✅

Your application has been built successfully:

```
JAR Location: c:\Development\git\userManagementApi\target\user-management-api-1.0.0.jar
Size: ~60 MB
```

**Verify:**
```powershell
ls c:\Development\git\userManagementApi\target\user-management-api-*.jar
```

### 2. Install Docker Desktop

**Required for building container images**

1. Download: https://docs.docker.com/desktop/install/windows-install/
2. Install and restart your computer
3. Verify installation:
   ```powershell
   docker --version
   # Should output: Docker version 24.0+ or higher
   ```

### 3. Install/Verify gcloud CLI

**Required for GCP deployment**

```powershell
# Check if installed
gcloud --version

# If not installed, download from:
# https://cloud.google.com/sdk/docs/install-windows
```

### 4. Verify Application Configuration

Your application has been updated for Cloud Run:

- ✅ **pom.xml:** Updated with required dependencies
- ✅ **application-gcp.yml:** Updated with Cloud SQL Auth Proxy configuration
- ✅ **Dockerfile:** Multi-stage build configured

**Configuration Details:**
```yaml
# application-gcp.yml datasource
datasource:
  url: jdbc:mysql://127.0.0.1:3306/my_auth_db
  # Cloud Run will automatically proxy this to Cloud SQL
  # No VPC Connector needed!
```

---

## Manual Deployment Steps

### Step 1: Build Docker Image

```powershell
cd c:\Development\git\userManagementApi

# Build image with two tags
docker build `
  -t gcr.io/fincore-npe-project/fincore-api:latest `
  -t gcr.io/fincore-npe-project/fincore-api:v1.0.0 `
  .

# Verify build
docker images | grep fincore-api
```

**Expected output:**
```
fincore-api       latest       [IMAGE_ID]   2 minutes ago   400MB
fincore-api       v1.0.0       [IMAGE_ID]   2 minutes ago   400MB
```

### Step 2: Setup GCP Authentication

```powershell
# Set your GCP project
gcloud config set project fincore-npe-project

# Verify project
gcloud config get-value project
# Output: fincore-npe-project

# Authenticate Docker with Google Cloud Registry
gcloud auth configure-docker gcr.io --quiet
```

### Step 3: Push Image to Google Container Registry

```powershell
# Push both tags
docker push gcr.io/fincore-npe-project/fincore-api:latest
docker push gcr.io/fincore-npe-project/fincore-api:v1.0.0

# Verify image in registry
gcloud container images describe gcr.io/fincore-npe-project/fincore-api:latest

# List all tags
gcloud container images list-tags gcr.io/fincore-npe-project/fincore-api
```

**Expected: Image appears in GCP Console → Container Registry**

### Step 4: Verify GCP Infrastructure

```powershell
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"

# 1. Check Cloud SQL instance
gcloud sql instances describe fincore-npe-db --region=$REGION
# Should show: state = RUNNABLE

# 2. Check Service Account
gcloud iam service-accounts describe fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com
# Should exist

# 3. Verify secrets
gcloud secrets list
# Should include: db-password, jwt-secret
```

### Step 5: Deploy to Cloud Run

```powershell
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"

gcloud run deploy fincore-npe-api `
  --image=gcr.io/$PROJECT_ID/fincore-api:latest `
  --region=$REGION `
  --platform=managed `
  --allow-unauthenticated `
  --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com `
  --memory=256Mi `
  --cpu=0.5 `
  --max-instances=2 `
  --min-instances=0 `
  --timeout=540 `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080,DB_NAME=my_auth_db,DB_USER=fincore_app" `
  --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest" `
  --set-cloudsql-instances="$PROJECT_ID:$REGION:fincore-npe-db" `
  --project=$PROJECT_ID
```

**This will take 2-3 minutes. Wait for completion.**

### Step 6: Verify Deployment

```powershell
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"

# Get service URL
$SERVICE_URL = gcloud run services describe fincore-npe-api `
  --region=$REGION `
  --format='value(status.url)' `
  --project=$PROJECT_ID

Write-Host "Service URL: $SERVICE_URL"

# Verify service details
gcloud run services describe fincore-npe-api `
  --region=$REGION `
  --project=$PROJECT_ID
```

### Step 7: Test Deployment

```powershell
# Test health endpoint
curl "$SERVICE_URL/actuator/health"

# Expected response:
# {
#   "status":"UP",
#   "components":{...}
# }

# Test API endpoints
curl -X GET "$SERVICE_URL/api/users"
curl -X POST "$SERVICE_URL/api/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin123"}'
```

### Step 8: Monitor Logs

```powershell
# View recent logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" `
  --limit=50 `
  --project=fincore-npe-project

# Stream logs in real-time
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" `
  --follow `
  --project=fincore-npe-project

# View only errors
gcloud logging read "severity=ERROR" `
  --limit=20 `
  --project=fincore-npe-project
```

---

## Troubleshooting

### Docker Build Issues

**Error: "docker: command not found"**
- Install Docker Desktop from: https://docs.docker.com/desktop/install/windows-install/
- Add Docker to PATH or use full path: `"C:\Program Files\Docker\Docker\resources\bin\docker.exe"`

**Error: "failed to solve with frontend dockerfile.v0"**
- Ensure Dockerfile exists in project root
- Check Dockerfile syntax: `docker build --no-cache .`

### GCP Deployment Issues

**Error: "image not found"**
- Verify image was pushed: `gcloud container images list`
- Ensure authentication: `gcloud auth configure-docker gcr.io`

**Error: "Cloud SQL connection refused"**
- Verify Cloud SQL instance is RUNNABLE: `gcloud sql instances describe fincore-npe-db`
- Check service account has `roles/cloudsql.client` role
- View logs for details: `gcloud logging read 'severity=ERROR'`

**Error: "Secret not found"**
- Create secrets if missing:
  ```powershell
  echo "YOUR_DB_PASSWORD" | gcloud secrets create db-password --data-file=- 2>$null
  ```

### Cloud Run Startup Issues

**Service not responding after deployment**
- Wait 1-2 minutes for startup
- Check logs: `gcloud logging read 'resource.type=cloud_run_revision' --follow`
- Verify environment variables: `gcloud run services describe fincore-npe-api --region=europe-west2`

**High memory usage**
- Current: 256Mi (suitable for NPE)
- Increase if needed: `--memory=512Mi`

**Connection timeout**
- Check Cloud SQL private IP: `gcloud sql instances describe fincore-npe-db --region=europe-west2`
- Verify Cloud Run has Cloud SQL client role
- Check connection pool settings in application-gcp.yml

---

## Cost Verification

After deployment, verify costs:

```powershell
# View Cloud Run costs (estimated)
gcloud run services describe fincore-npe-api --region=europe-west2 --format="table(status.latestReadyRevisionName, status.latestUrl)"

# Expected monthly costs:
# - Cloud Run: $0-2 (free tier covers first 2M invocations)
# - Cloud SQL: $6
# - Storage: $0.20
# - Total: ~$8-10/month
```

---

## Next Steps

1. **Monitor for 24 hours** to ensure stable operation
2. **Setup custom domain** (optional):
   ```powershell
   gcloud run services update-traffic fincore-npe-api --region=europe-west2 --to-revisions=LATEST=100
   ```

3. **Enable auto-scaling alerts**:
   ```powershell
   gcloud alpha monitoring policies create `
     --display-name="Cloud Run High Error Rate" `
     --condition-threshold-value=5
   ```

4. **Setup CI/CD** for automated deployments (Cloud Build)

5. **Performance testing**:
   ```powershell
   # Install Apache Bench
   choco install apache-bench
   
   # Run load test
   ab -n 1000 -c 10 "$SERVICE_URL/actuator/health"
   ```

---

## Useful Commands Reference

```powershell
# Get service URL
gcloud run services describe fincore-npe-api --region=europe-west2 --format='value(status.url)'

# Scale service
gcloud run services update fincore-npe-api --max-instances=5 --region=europe-west2

# View metrics
gcloud monitoring metrics-descriptors list --filter="metric.type:run*"

# Connect to Cloud SQL
gcloud sql connect fincore-npe-db --user=root --region=europe-west2

# Delete service (if needed)
gcloud run services delete fincore-npe-api --region=europe-west2

# View all Cloud Run services
gcloud run services list --region=europe-west2

# View revisions
gcloud run revisions list --region=europe-west2
```

---

## Configuration Files Changed

### 1. pom.xml
- Added Google Maven repository
- Dependencies verified for Cloud Run compatibility
- Spring Boot 3.2.0, Java 21

### 2. application-gcp.yml
- Updated datasource URL for Cloud SQL Auth Proxy
- Removed VPC Connector requirement
- Cloud Run will automatically proxy localhost:3306 to Cloud SQL

### 3. Dockerfile
- Multi-stage build (builder → runtime)
- Alpine base image (lightweight)
- Non-root user (security)
- Health check configured
- Environment variables optimized for Cloud Run

---

**Ready for deployment!** 🚀

Choose either:
- **Automated:** Run `.\deploy-to-cloud-run-npe.ps1`
- **Manual:** Follow steps 1-8 above

Both approaches will deploy your API to Cloud Run within 3-5 minutes.
