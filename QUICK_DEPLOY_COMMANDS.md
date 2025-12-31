# 🚀 Quick GCP Deployment Commands

## Configuration Variables
Replace these values with your actual GCP configuration:

```bash
PROJECT_ID="your-project-id"
REGION="europe-west2"
SERVICE_NAME="fincore-npe-api"
IMAGE_NAME="fincore-api"
DB_INSTANCE="fincore-npe-db"
DB_NAME="fincore_db"
DB_USER="fincore_app"
CLOUDSQL_INSTANCE="project-id:region:instance-name"
SERVICE_ACCOUNT="fincore-npe-cloudrun@project-id.iam.gserviceaccount.com"
```

---

## 🎯 Quick Deploy (All-in-One)

### Windows PowerShell
```powershell
# Run the deployment script
.\deploy-to-gcp.ps1 -ProjectId "your-project-id" -Region "europe-west2"
```

### Linux/Mac Bash
```bash
# Make executable
chmod +x setup-gcp-infrastructure.sh

# Setup infrastructure
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID europe-west2

# Build and deploy
mvn clean package -DskipTests
docker build -t gcr.io/$PROJECT_ID/fincore-api:latest .
docker push gcr.io/$PROJECT_ID/fincore-api:latest
gcloud run deploy fincore-npe-api --image=gcr.io/$PROJECT_ID/fincore-api:latest [options...]
```

---

## 📋 Step-by-Step Commands

### 1. Set GCP Project
```bash
gcloud config set project YOUR_PROJECT_ID
gcloud config get-value project
```

### 2. Enable Required APIs
```bash
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

### 3. Build Application
```bash
# Build with Maven
mvn clean package -DskipTests

# Verify build
ls -lh target/user-management-api-1.0.0.jar
```

### 4. Build & Push Docker Image
```bash
# Configure Docker for GCR
gcloud auth configure-docker gcr.io

# Build image
docker build -t gcr.io/$PROJECT_ID/fincore-api:latest .

# Push to GCR
docker push gcr.io/$PROJECT_ID/fincore-api:latest

# List images in GCR
gcloud container images list --repository=gcr.io/$PROJECT_ID
```

### 5. Deploy to Cloud Run
```bash
# Using Secret Manager for DB_PASSWORD (recommended)
gcloud run deploy fincore-npe-api \
  --image=gcr.io/$PROJECT_ID/fincore-api:latest \
  --region=europe-west2 \
  --platform=managed \
  --allow-unauthenticated \
  --service-account=$SERVICE_ACCOUNT \
  --memory=1Gi \
  --cpu=1 \
  --timeout=900 \
  --max-instances=3 \
  --min-instances=0 \
  --add-cloudsql-instances=$CLOUDSQL_INSTANCE \
  --set-env-vars="SPRING_PROFILES_ACTIVE=npe,DB_NAME=fincore_db,DB_USER=$DB_USER,CLOUD_SQL_INSTANCE=$CLOUDSQL_INSTANCE" \
  --update-secrets="DB_PASSWORD=fincore-npe-app-password:latest" \
  --port=8080
```

### 6. Initialize Database
```bash
# Connect to Cloud SQL
gcloud sql connect $DB_INSTANCE --user=$DB_USER --database=$DB_NAME

# Or use Cloud SQL Proxy
./cloud-sql-proxy $CLOUDSQL_INSTANCE
mysql -h 127.0.0.1 -u $DB_USER -p $DB_NAME < cloud-sql-schema.sql
```

---

## 🔍 Monitoring & Debugging

### View Service Details
```bash
# Get service URL
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)'

# List all services
gcloud run services list --region=europe-west2

# Get service configuration
gcloud run services describe fincore-npe-api --region=europe-west2
```

### View Logs
```bash
# Tail logs in real-time
gcloud run services logs tail fincore-npe-api --region=europe-west2

# Read recent logs
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --limit=100

# Filter logs by severity
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --log-filter="severity>=ERROR"
```

### Test Endpoints
```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)')

# Test health endpoint
curl $SERVICE_URL/actuator/health

# Test OTP Authentication (Non-Production has devOtp in response)
# Step 1: Request OTP
curl -X POST $SERVICE_URL/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890"}'

# Response includes devOtp in non-production environments:
# {"message":"OTP sent successfully","expiresIn":300,"devOtp":"123456"}

# Step 2: Verify OTP and get JWT token
curl -X POST $SERVICE_URL/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890","otp":"123456"}'
```

### Database Operations
```bash
# List Cloud SQL instances
gcloud sql instances list

# Describe instance
gcloud sql instances describe $DB_INSTANCE

# List databases
gcloud sql databases list --instance=$DB_INSTANCE

# Connect to database
gcloud sql connect $DB_INSTANCE --user=$DB_USER --database=$DB_NAME

# Create backup
gcloud sql backups create --instance=$DB_INSTANCE

# List backups
gcloud sql backups list --instance=$DB_INSTANCE
```

---

## 🔧 Update & Redeploy

### Update Code and Redeploy
```bash
# 1. Pull latest code
git pull origin main

# 2. Rebuild
mvn clean package -DskipTests

# 3. Rebuild Docker image
docker build -t gcr.io/$PROJECT_ID/fincore-api:latest .

# 4. Push to GCR
docker push gcr.io/$PROJECT_ID/fincore-api:latest

# 5. Cloud Run will auto-deploy latest image or force update
gcloud run services update fincore-npe-api \
  --image=gcr.io/$PROJECT_ID/fincore-api:latest \
  --region=europe-west2
```

### Update Environment Variables Only
```bash
gcloud run services update fincore-npe-api \
  --region=europe-west2 \
  --update-env-vars="DB_NAME=fincore_db,SPRING_PROFILES_ACTIVE=npe"
```

### Update Service Configuration
```bash
# Update memory and CPU
gcloud run services update fincore-npe-api \
  --region=europe-west2 \
  --memory=2Gi \
  --cpu=2

# Update scaling
gcloud run services update fincore-npe-api \
  --region=europe-west2 \
  --min-instances=1 \
  --max-instances=5
```

---

## 🚨 Troubleshooting Commands

### Check Service Status
```bash
# Service health
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.conditions)'

# Recent revisions
gcloud run revisions list \
  --service=fincore-npe-api \
  --region=europe-west2
```

### Delete and Redeploy
```bash
# Delete service (if needed)
gcloud run services delete fincore-npe-api --region=europe-west2

# Redeploy from scratch
gcloud run deploy fincore-npe-api [full deployment command...]
```

### Check IAM Permissions
```bash
# List service account permissions
gcloud projects get-iam-policy $PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:$SERVICE_ACCOUNT"

# Add Cloud SQL client role (if missing)
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$SERVICE_ACCOUNT" \
  --role="roles/cloudsql.client"
```

### Clear Docker Cache
```bash
# Remove all local images
docker system prune -a

# Remove specific image
docker rmi gcr.io/$PROJECT_ID/fincore-api:latest

# Rebuild from scratch
docker build --no-cache -t gcr.io/$PROJECT_ID/fincore-api:latest .
```

---

## 📊 Performance & Metrics

### View Service Metrics
```bash
# Open in browser
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)'

# View in Cloud Console
echo "https://console.cloud.google.com/run/detail/$REGION/fincore-npe-api/metrics?project=$PROJECT_ID"
```

### View Database Metrics
```bash
# Open in Cloud Console
echo "https://console.cloud.google.com/sql/instances/$DB_INSTANCE/metrics?project=$PROJECT_ID"
```

---

## 🔒 Security Commands

### Manage Secrets
```bash
# Create secret in Secret Manager
echo -n "my-secret-value" | gcloud secrets create jwt-secret \
  --data-file=- \
  --replication-policy="automatic"

# Update secret
echo -n "new-secret-value" | gcloud secrets versions add jwt-secret \
  --data-file=-

# Grant access to service account
gcloud secrets add-iam-policy-binding jwt-secret \
  --member="serviceAccount:$SERVICE_ACCOUNT" \
  --role="roles/secretmanager.secretAccessor"

# Use secret in Cloud Run
gcloud run services update fincore-npe-api \
  --region=europe-west2 \
  --update-secrets=JWT_SECRET=jwt-secret:latest
```

### Rotate Database Password
```bash
# Update database user password
gcloud sql users set-password $DB_USER \
  --instance=$DB_INSTANCE \
  --password="NEW_SECURE_PASSWORD"

# Update Cloud Run environment variable
gcloud run services update fincore-npe-api \
  --region=europe-west2 \
  --update-env-vars="DB_PASSWORD=NEW_SECURE_PASSWORD"
```

---

## 📱 GitHub Actions (Automated Deployment)

### Trigger Manual Deployment
```bash
# Push to main branch triggers auto-deployment
git add .
git commit -m "Deploy to GCP"
git push origin main

# View workflow status
gh run list --workflow=deploy-npe.yml
gh run view --log
```

### Required GitHub Secrets
```bash
# Set via GitHub UI or CLI
gh secret set GCP_PROJECT_ID -b "your-project-id"
gh secret set DB_USER -b "fincore_app"
gh secret set DB_PASSWORD -b "your-password"
gh secret set CLOUDSQL_INSTANCE -b "project:region:instance"
gh secret set GCP_SERVICE_ACCOUNT -b "service-account@project.iam.gserviceaccount.com"

# Set GCP service account key from file
gh secret set GCP_SA_KEY < sa-key.json
```

---

## 📞 Quick Help

### Get Current Configuration
```bash
# Current project
gcloud config get-value project

# Current region
gcloud config get-value run/region

# List all services
gcloud run services list
```

### Common Issues
```bash
# Issue: "Permission denied"
# Fix: Check IAM roles
gcloud projects get-iam-policy $PROJECT_ID

# Issue: "Image not found"
# Fix: List images and verify
gcloud container images list --repository=gcr.io/$PROJECT_ID

# Issue: "Database connection failed"
# Fix: Check Cloud SQL instance status
gcloud sql instances describe $DB_INSTANCE --format='value(state)'

# Issue: "Service not responding"
# Fix: Check logs
gcloud run services logs read fincore-npe-api --region=europe-west2 --limit=50
```

---

**Last Updated:** December 30, 2025  
**Database Name:** fincore_db  
**Project Status:** ✅ Ready for Deployment
