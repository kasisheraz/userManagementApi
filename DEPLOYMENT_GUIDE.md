# 🚀 GCP Deployment Guide - FinCore User Management API

This guide walks you through deploying the User Management API to Google Cloud Platform with the updated database name `fincore_db`.

## 📋 Prerequisites

Before deploying, ensure you have:

- ✅ Google Cloud Platform account with billing enabled
- ✅ [Google Cloud SDK (gcloud CLI)](https://cloud.google.com/sdk/docs/install) installed
- ✅ GitHub account with repository access
- ✅ Java 21 and Maven installed locally (for building)
- ✅ Docker installed (optional, for local testing)

## 🎯 Deployment Options

You have two deployment options:

1. **Automated CI/CD via GitHub Actions** (Recommended) - Deploy on every push to main
2. **Manual Deployment via gcloud CLI** - One-time or on-demand deployment

---

## Option 1: Automated Deployment via GitHub Actions (Recommended)

### Step 1: Initial GCP Infrastructure Setup

Run the infrastructure setup script to create all required GCP resources:

```bash
# Make the script executable (if on Linux/Mac)
chmod +x setup-gcp-infrastructure.sh

# Run the setup script with your project ID
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID europe-west2
```

**What this script creates:**
- ✅ Cloud SQL MySQL 8.0 instance with `fincore_db` database
- ✅ Service account with appropriate IAM roles
- ✅ Enables required GCP APIs (Cloud Run, Cloud SQL, Container Registry)
- ✅ Configures database user and password
- ✅ Generates `gcp-config.env` with your configuration

**Follow the prompts:**
1. Confirm project details
2. Wait for Cloud SQL instance creation (~5-10 minutes)
3. Enter and confirm database password when prompted
4. Save the generated `gcp-config.env` file securely

### Step 2: Configure GitHub Secrets

Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add the following secrets:

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `GCP_PROJECT_ID` | Your GCP project ID | `project-07a61357-b791-4255-a9e` |
| `GCP_SA_KEY` | Service account JSON key | `{...full JSON content...}` |
| `DB_USER` | Database username | `fincore_app` |
| `SECRET_NAME` | Secret Manager secret name for DB password | `fincore-npe-app-password` |
| `CLOUDSQL_INSTANCE` | Full Cloud SQL instance name | `project-id:region:instance-name` |
| `GCP_SERVICE_ACCOUNT` | Service account email | `fincore-npe-cloudrun@project-id.iam.gserviceaccount.com` |

**To get the Service Account JSON key:**
```bash
# Create and download service account key
gcloud iam service-accounts keys create sa-key.json \
  --iam-account=user-management-api-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com

# Copy the entire content of sa-key.json to GCP_SA_KEY secret
cat sa-key.json
```

### Step 3: Deploy via GitHub

Once secrets are configured, deployment is automatic:

```bash
# Commit and push your changes
git add .
git commit -m "Deploy with fincore_db database"
git push origin main
```

**The GitHub Actions workflow will:**
1. ✅ Build the application with Maven
2. ✅ Create Docker image
3. ✅ Push to Google Container Registry
4. ✅ Deploy to Cloud Run
5. ✅ Run health checks

**Monitor the deployment:**
- Go to GitHub → **Actions** tab
- Click on the running workflow
- View real-time logs

### Step 4: Initialize Database Schema

After first deployment, run the SQL schema to create tables:

```bash
# Connect to Cloud SQL instance
gcloud sql connect YOUR_INSTANCE_NAME --user=fincore_app --database=fincore_db

# Once connected, run the schema
mysql> source cloud-sql-schema.sql;
mysql> exit;
```

Or use Cloud SQL Proxy:

```bash
# Start Cloud SQL Proxy
./cloud-sql-proxy YOUR_PROJECT_ID:europe-west2:YOUR_INSTANCE_NAME

# In another terminal, connect with MySQL client
mysql -h 127.0.0.1 -u fincore_app -p fincore_db < cloud-sql-schema.sql
```

### Step 5: Verify Deployment

```bash
# Get the Cloud Run service URL
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)'

# Test health endpoint
curl https://YOUR_SERVICE_URL/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## Option 2: Manual Deployment via gcloud CLI

### Step 1: Build the Application

```bash
# Build the JAR file
mvn clean package -DskipTests

# Verify the JAR was created
ls -lh target/user-management-api-1.0.0.jar
```

### Step 2: Build and Push Docker Image

```bash
# Authenticate Docker with GCR
gcloud auth configure-docker gcr.io

# Build Docker image
docker build -t gcr.io/YOUR_PROJECT_ID/fincore-api:latest .

# Push to Google Container Registry
docker push gcr.io/YOUR_PROJECT_ID/fincore-api:latest
```

### Step 3: Deploy to Cloud Run

```bash
# Source your configuration
source gcp-config.env

# Deploy to Cloud Run
gcloud run deploy fincore-npe-api \
  --image=gcr.io/$GCP_PROJECT_ID/fincore-api:latest \
  --region=europe-west2 \
  --platform=managed \
  --allow-unauthenticated \
  --service-account=fincore-npe-cloudrun@$GCP_PROJECT_ID.iam.gserviceaccount.com \
  --memory=1Gi \
  --cpu=1 \
  --timeout=900 \
  --max-instances=3 \
  --min-instances=0 \
  --add-cloudsql-instances=$CLOUDSQL_INSTANCE \
  --set-env-vars="SPRING_PROFILES_ACTIVE=npe,DB_NAME=fincore_db,DB_USER=$DB_USER,DB_PASSWORD=$DB_PASSWORD,CLOUD_SQL_INSTANCE=$CLOUDSQL_INSTANCE" \
  --port=8080
```

### Step 4: Update Cloud Run with Database

After deployment, verify the database connection:

```bash
# Get service logs
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --limit=50
```

---

## 🔧 Post-Deployment Configuration

### 1. Configure Custom Domain (Optional)

```bash
# Map custom domain to Cloud Run service
gcloud run domain-mappings create \
  --service=fincore-npe-api \
  --domain=api.yourcompany.com \
  --region=europe-west2
```

### 2. Set Up Cloud SQL Backups

```bash
# Enable automated backups
gcloud sql instances patch YOUR_INSTANCE_NAME \
  --backup-start-time=03:00 \
  --enable-bin-log
```

### 3. Configure Monitoring & Alerts

```bash
# Enable Cloud Monitoring
gcloud services enable monitoring.googleapis.com

# View metrics in Cloud Console
echo "View metrics at: https://console.cloud.google.com/monitoring"
```

### 4. Test the API

```bash
# Get the service URL
SERVICE_URL=$(gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)')

# Test login endpoint
curl -X POST ${SERVICE_URL}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }'

# Expected response with JWT token
```

---

## 🛠️ Troubleshooting

### Issue: Cloud SQL Connection Failed

**Check 1: Verify Cloud SQL instance is running**
```bash
gcloud sql instances describe YOUR_INSTANCE_NAME \
  --format='value(state)'
```

**Check 2: Verify service account permissions**
```bash
gcloud projects get-iam-policy YOUR_PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:*cloudrun*"
```

**Check 3: View Cloud Run logs**
```bash
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --limit=100
```

### Issue: Database Schema Not Initialized

```bash
# Connect to Cloud SQL and verify tables exist
gcloud sql connect YOUR_INSTANCE_NAME --user=fincore_app --database=fincore_db

mysql> SHOW TABLES;
mysql> SELECT COUNT(*) FROM users;
```

### Issue: Build Failures

```bash
# Clean Maven cache and rebuild
mvn clean install -U

# Check Java version
java -version  # Should be Java 21

# Verify Maven version
mvn -version   # Should be 3.9+
```

### Issue: Container Image Push Failed

```bash
# Re-authenticate with GCR
gcloud auth configure-docker gcr.io

# Check project permissions
gcloud projects get-iam-policy YOUR_PROJECT_ID
```

---

## 📊 Monitoring Your Deployment

### View Application Logs

```bash
# Real-time logs
gcloud run services logs tail fincore-npe-api --region=europe-west2

# Recent logs
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --limit=100
```

### Check Service Status

```bash
# Service details
gcloud run services describe fincore-npe-api --region=europe-west2

# Service metrics
gcloud run services list --region=europe-west2
```

### Database Monitoring

```bash
# Cloud SQL instance operations
gcloud sql operations list --instance=YOUR_INSTANCE_NAME

# Database connections
gcloud sql instances describe YOUR_INSTANCE_NAME \
  --format='value(settings.ipConfiguration.authorizedNetworks)'
```

---

## 🔒 Security Checklist

- ✅ Database password is strong and stored in GitHub Secrets
- ✅ Service account has minimal required permissions
- ✅ JWT secret is securely configured
- ✅ Cloud SQL instance has authorized networks configured
- ✅ HTTPS is enforced (Cloud Run does this automatically)
- ✅ Cloud SQL automatic backups are enabled
- ✅ Sensitive data is not committed to Git

---

## 🚀 Continuous Deployment

Your deployment is now automated! Any push to the `main` branch will:

1. Trigger GitHub Actions workflow
2. Build and test the application
3. Create Docker image
4. Push to GCR
5. Deploy to Cloud Run
6. Run health checks

**To deploy a new version:**
```bash
git add .
git commit -m "Your changes"
git push origin main
```

---

## 📚 Additional Resources

- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Cloud SQL for MySQL](https://cloud.google.com/sql/docs/mysql)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Spring Boot on Cloud Run](https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service)

---

## 📧 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Cloud Run logs
3. Check GitHub Actions workflow logs
4. Open a GitHub issue

---

**Deployment Status:** ✅ Deployed & Running  
**Database Name:** `fincore_db`  
**Service URL:** `https://fincore-npe-api-994490239798.europe-west2.run.app`  
**Last Updated:** December 31, 2025
