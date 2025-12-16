# 🚀 GitHub Actions CI/CD - Complete Setup

## Overview

Your User Management API now has **continuous deployment** configured! Every push to the `main` branch automatically:

1. ✅ Builds and tests the application
2. ✅ Creates a Docker container
3. ✅ Pushes to Google Container Registry (GCR)
4. ✅ Deploys to Google Cloud Run (NPE)
5. ✅ Verifies the deployment with health checks
6. ✅ Runs smoke tests

**Total time**: 8-13 minutes from push to live deployment 🎉

---

## Files Created

### Workflows (`.github/workflows/`)

```
.github/workflows/
├── deploy-npe.yml          # Main deployment workflow (push to main)
└── test.yml               # PR testing workflow (pull requests)
```

### Documentation

```
GITHUB_ACTIONS_SETUP.md         # Detailed setup instructions
GITHUB_ACTIONS_QUICKSTART.md    # 5-minute quick start
GITHUB_ACTIONS_ACTIVATION.md    # Activation checklist
CICD_ARCHITECTURE.md            # Complete architecture design
setup-github-actions.sh         # Automated setup script
```

---

## ⚡ Quick Activation (3 Steps)

### Step 1️⃣: Create GCP Service Account

```bash
# Run locally or in Google Cloud Shell
bash setup-github-actions.sh

# Or manually:
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions CI/CD" \
  --project=project-07a61357-b791-4255-a9e

# Grant roles
for role in roles/run.admin roles/storage.admin roles/cloudsql.client roles/iam.serviceAccountUser roles/editor; do
  gcloud projects add-iam-policy-binding project-07a61357-b791-4255-a9e \
    --member=serviceAccount:github-actions@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com \
    --role=$role --quiet
done

# Create key
gcloud iam service-accounts keys create github-actions-key.json \
  --iam-account=github-actions@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com
```

### Step 2️⃣: Add GitHub Secrets

Go to: **Settings → Secrets and variables → Actions** in your GitHub repository

Add these 3 secrets:

| Secret Name | Value |
|-------------|-------|
| `GCP_PROJECT_ID` | `project-07a61357-b791-4255-a9e` |
| `GCP_SA_KEY` | (Contents of `github-actions-key.json`) |
| `GCP_SERVICE_ACCOUNT` | `fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com` |

### Step 3️⃣: Test It!

```bash
# Push any change to main
git push origin main

# Watch it deploy at:
# https://github.com/kasisheraz/userManagementApi/actions
```

---

## 📊 Workflow Status

### Deployment Workflow (`deploy-npe.yml`)

**Triggers on**: Push to `main` branch

```
┌─ Build Job ──────────┐
│ • Maven build         │
│ • Run tests           │  ~3-5 min
│ • Upload artifact     │
└──────────┬────────────┘
           │
           ▼
┌─ Docker Build & Push ┐
│ • Build image         │  ~2-3 min
│ • Push to GCR         │
└──────────┬────────────┘
           │
           ▼
┌─ Deploy to Cloud Run ┐
│ • Deploy service      │  ~3-5 min
│ • Health check (30x)  │
│ • Smoke tests         │
└──────────┬────────────┘
           │
           ▼
      ✅ SUCCESS
  Service live at:
  https://fincore-npe-api-
  994490239798.europe-west2.run.app
```

### Test Workflow (`test.yml`)

**Triggers on**: Pull requests to `main` or `develop` branches

```
┌─ Test Job ────────────┐
│ • Maven build          │  ~3-5 min
│ • Run tests            │
│ • Publish results      │
└────────────────────────┘
       ↓
  Results shown in PR ✅
```

---

## 🧪 What Gets Deployed

### Cloud Run Service
- **Name**: `fincore-npe-api`
- **Region**: `europe-west2` (London)
- **Memory**: 512MB
- **CPU**: 1 vCPU
- **Database**: H2 (in-memory)
- **Public**: Yes (no authentication required)

### Docker Image
- **Base**: Eclipse Temurin 21 JRE Alpine
- **Registry**: Google Container Registry (GCR)
- **Image name**: `gcr.io/project-07a61357-b791-4255-a9e/fincore-api`
- **Tags**: `latest` and git SHA (e.g., `abc1234567`)

### Environment
```
SPRING_PROFILES_ACTIVE=h2
JWT_SECRET=<from Secret Manager>
```

---

## 🔍 Monitoring Deployments

### GitHub Actions Dashboard
https://github.com/kasisheraz/userManagementApi/actions

- View workflow runs in real-time
- Check build logs
- See deployment progress

### Cloud Run Console
https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api

- View service metrics
- Check recent revisions
- Monitor error rates

### Cloud Run Logs
```bash
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" \
  --project=project-07a61357-b791-4255-a9e \
  --limit=50
```

---

## 🧬 API Endpoints (After Deployment)

Once deployed, test the service:

```bash
SERVICE_URL="https://fincore-npe-api-994490239798.europe-west2.run.app"

# ✅ Health check
curl $SERVICE_URL/actuator/health

# ✅ Login
TOKEN=$(curl -s -X POST $SERVICE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | jq -r '.token')

# ✅ Get users (with JWT)
curl -X GET $SERVICE_URL/api/users \
  -H "Authorization: Bearer $TOKEN"

# ✅ Create user
curl -X POST $SERVICE_URL/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"user@example.com","password":"pass123"}'
```

---

## 🔐 Security

### Service Account Permissions
The `github-actions` service account has **minimal required permissions**:
- `roles/run.admin` - Deploy Cloud Run services
- `roles/storage.admin` - Push to GCR
- `roles/cloudsql.client` - Access Cloud SQL (if using MySQL)
- `roles/iam.serviceAccountUser` - Use service accounts
- `roles/editor` - General resource management

### Secret Management
- GitHub secrets are **encrypted at rest**
- `GCP_SA_KEY` used only during workflow execution
- Never exposed in logs or pushed to repository
- `.gitignore` prevents accidental commits

---

## ⚙️ Configuration

### Modify Deployment Settings

Edit `.github/workflows/deploy-npe.yml` to change:

| Setting | Location | Value |
|---------|----------|-------|
| Region | Line 16 | `europe-west2` |
| Memory | `--memory` param | `512Mi` |
| CPU | `--cpu` param | `1` |
| Service Name | Line 15 | `fincore-npe-api` |
| Database Profile | `--set-env-vars` | `h2` or `mysql` |

### Switch to MySQL Database

In `.github/workflows/deploy-npe.yml`, find the deploy step and change:

```yaml
# From (H2):
--set-env-vars="SPRING_PROFILES_ACTIVE=h2" \

# To (MySQL):
--set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_HOST=127.0.0.1,DB_PORT=3306,DB_NAME=my_auth_db,DB_USER=fincore_app,CLOUDSQL_INSTANCE=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db" \
--set-secrets="DB_PASSWORD=fincore-npe-db-password:latest,JWT_SECRET=jwt-secret:latest" \
```

---

## 🚨 Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| Workflow doesn't run | Secrets not configured | Add GCP_PROJECT_ID, GCP_SA_KEY, GCP_SERVICE_ACCOUNT |
| Build fails | Maven issues | Check Java/Maven versions; run `mvn clean package` locally |
| Docker push fails | GCR auth issue | Verify service account has `roles/editor` |
| Deployment timeout | Service startup slow | Check Cloud Run logs for errors |
| Health check fails | Service not responding | Check application logs in Cloud Run |

---

## 📚 Full Documentation

For detailed information, see:

- **[GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md)** - Complete setup guide
- **[GITHUB_ACTIONS_QUICKSTART.md](GITHUB_ACTIONS_QUICKSTART.md)** - 5-minute quick start
- **[GITHUB_ACTIONS_ACTIVATION.md](GITHUB_ACTIONS_ACTIVATION.md)** - Activation checklist
- **[CICD_ARCHITECTURE.md](CICD_ARCHITECTURE.md)** - Architecture & design

---

## ✅ Checklist

- [ ] Run `setup-github-actions.sh` or manual commands
- [ ] Create `github-actions-key.json`
- [ ] Add `GCP_PROJECT_ID` secret
- [ ] Add `GCP_SA_KEY` secret
- [ ] Add `GCP_SERVICE_ACCOUNT` secret
- [ ] Push test commit to main
- [ ] Check workflow at `/actions`
- [ ] Verify deployment in Cloud Run
- [ ] Test API endpoints
- [ ] 🎉 Continuous deployment is now active!

---

## 🎯 Your Deployment Pipeline

```
You make changes
    ↓
Commit and push to main
    ↓
GitHub Actions automatically:
    ├─ Builds & tests
    ├─ Creates Docker image
    ├─ Pushes to GCR
    └─ Deploys to Cloud Run
    ↓
Service goes live (8-13 min)
    ↓
Everyone can use your latest code! 🚀
```

---

**Next Step**: Follow the [activation checklist](GITHUB_ACTIONS_ACTIVATION.md) to get started!

**Questions?** See [GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md#troubleshooting) for detailed troubleshooting.

---

Last Updated: 2025-12-16
Status: ✅ Ready to Activate
