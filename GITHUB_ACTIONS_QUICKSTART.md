# GitHub Actions CI/CD Quick Start

## ⚡ 5-Minute Setup

### Step 1: Create GCP Service Account (Run locally or in Cloud Shell)

```bash
PROJECT_ID="project-07a61357-b791-4255-a9e"
SERVICE_ACCOUNT_NAME="github-actions"

# Create service account
gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
  --display-name="GitHub Actions CI/CD" \
  --project=$PROJECT_ID

# Grant necessary roles
for role in roles/run.admin roles/storage.admin roles/cloudsql.client roles/iam.serviceAccountUser roles/editor; do
  gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
    --role=$role --quiet
done

# Create and download key
gcloud iam service-accounts keys create /tmp/github-actions-key.json \
  --iam-account=$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com
```

### Step 2: Add GitHub Secrets

1. Go to: https://github.com/kasisheraz/userManagementApi/settings/secrets/actions

2. Click **New repository secret** and add:

```
Name: GCP_PROJECT_ID
Value: project-07a61357-b791-4255-a9e

Name: GCP_SA_KEY
Value: (Paste entire contents of github-actions-key.json)

Name: GCP_SERVICE_ACCOUNT
Value: fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com
```

### Step 3: Trigger Deployment

Push to main branch:
```bash
git add .
git commit -m "Trigger CI/CD workflow"
git push origin main
```

View deployment: https://github.com/kasisheraz/userManagementApi/actions

---

## 🔄 How It Works

### Automatic Workflow on Push to Main

```
Code Push to main
    ↓
Build Job (Maven)
    ├─ Checkout code
    ├─ Setup JDK 21
    ├─ Build & test Maven project
    └─ Upload artifact
    ↓
Docker Build & Push (GCR)
    ├─ Configure Google Cloud SDK
    ├─ Build Docker image
    └─ Push to gcr.io/project-07a61357-b791-4255-a9e/fincore-api
    ↓
Deploy to Cloud Run
    ├─ Deploy fincore-npe-api service
    ├─ Set environment variables
    ├─ Configure secrets
    ├─ Health check (30 attempts)
    └─ Smoke tests
    ↓
Deployment Complete ✅
Service URL: https://fincore-npe-api-994490239798.europe-west2.run.app
```

### Automatic Testing on Pull Request

```
Pull Request to main
    ↓
Test Job (Maven)
    ├─ Checkout code
    ├─ Setup JDK 21
    ├─ Build & run all tests
    └─ Publish test results
    ↓
Test Report displayed in PR ✅
```

---

## 📊 Workflow Status

Check here after pushing: https://github.com/kasisheraz/userManagementApi/actions

### Successful Workflow Run Output:

```
✅ Build & Test Job
   └─ Maven test suite passed

✅ Build & Push Docker Image Job
   └─ Image pushed: gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest

✅ Deploy to Cloud Run NPE Job
   ├─ Health check passed
   ├─ Smoke tests passed
   │  ├─ GET /actuator/health → {"status":"UP"}
   │  └─ POST /api/auth/login → {"token":"eyJ..."}
   └─ 🚀 Service deployed to: https://fincore-npe-api-994490239798.europe-west2.run.app
```

---

## 🧪 Available API Endpoints

After deployment, test the service:

```bash
SERVICE_URL="https://fincore-npe-api-994490239798.europe-west2.run.app"

# Health check
curl $SERVICE_URL/actuator/health

# Register user
curl -X POST $SERVICE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass123"}'

# Login
TOKEN=$(curl -s -X POST $SERVICE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | jq -r '.token')

# Get users (requires JWT)
curl -X GET $SERVICE_URL/api/users \
  -H "Authorization: Bearer $TOKEN"

# Create user (requires JWT)
curl -X POST $SERVICE_URL/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"user@example.com","password":"pass123"}'
```

---

## 🔐 Production Notes

### Database Options

**Current: H2 (In-Memory)**
- ✅ Fast, no configuration needed
- ❌ Data lost on restart
- ✅ Good for NPE/testing

**Optional: MySQL Cloud SQL**
- ✅ Persistent data
- ✅ Scalable
- ⏳ Connection setup required

To switch to MySQL, modify `.github/workflows/deploy-npe.yml`:

```yaml
# Change this line in the "Deploy to Cloud Run" step:
--set-env-vars="SPRING_PROFILES_ACTIVE=h2" \

# To this:
--set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_HOST=127.0.0.1,DB_PORT=3306,DB_NAME=my_auth_db,DB_USER=fincore_app,CLOUDSQL_INSTANCE=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db" \

# And add this line:
--set-secrets="DB_PASSWORD=fincore-npe-db-password:latest,JWT_SECRET=jwt-secret:latest" \
```

---

## 📝 Commit & Deploy Example

```bash
# Make changes to code
echo "// New feature" >> src/main/java/com/fincore/api/User.java

# Commit
git add src/
git commit -m "feat: Add new user field"

# Push (triggers CI/CD automatically)
git push origin main

# Check deployment status
# https://github.com/kasisheraz/userManagementApi/actions
```

The workflow will:
1. ✅ Build and test your code
2. ✅ Build Docker image
3. ✅ Push to GCR
4. ✅ Deploy to Cloud Run
5. ✅ Run smoke tests
6. ✅ Confirm service is live

**Result**: Your changes are automatically deployed to NPE!

---

## 🛑 Stopping a Deployment

If something goes wrong:

```bash
# Stop current Cloud Run service
gcloud run services delete fincore-npe-api \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e

# Or just update the workflow to not deploy
# Edit .github/workflows/deploy-npe.yml and comment out the deploy job
```

---

## 📊 Monitoring

### View Workflow Logs
https://github.com/kasisheraz/userManagementApi/actions

### View Cloud Run Logs
https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs

### View GCR Images
https://console.cloud.google.com/gcr/images/project-07a61357-b791-4255-a9e/

---

## ❓ Troubleshooting

| Issue | Solution |
|-------|----------|
| "GitHub secrets not set" | Go to Settings → Secrets and add GCP_PROJECT_ID, GCP_SA_KEY, GCP_SERVICE_ACCOUNT |
| "Docker push fails" | Ensure GCP_SA_KEY is set and service account has editor role |
| "Cloud Run deployment times out" | Check Cloud Run logs for startup errors; increase timeout in workflow |
| "Health check fails" | Service may be slow to start; check Cloud Run logs for errors |
| "Smoke tests fail" | Service is running but endpoints not responding; check application logs |

---

## ✨ Next Steps

1. ✅ GCP service account created
2. ✅ GitHub secrets configured
3. ✅ Workflows pushed to repository
4. **→ Push a change to main branch**
5. → Check Actions tab for deployment progress
6. → Verify service at https://fincore-npe-api-994490239798.europe-west2.run.app

**That's it!** Continuous deployment is now active. Every push to main automatically deploys to NPE. 🚀

---

For detailed setup instructions, see: [GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md)
