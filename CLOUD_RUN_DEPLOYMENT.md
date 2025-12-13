# Cloud Run Deployment Guide

## Overview
This guide provides step-by-step instructions to deploy the User Management API to Google Cloud Run with Cloud SQL.

## Prerequisites

### Required Tools
- **gcloud CLI**: [Install Guide](https://cloud.google.com/sdk/docs/install)
- **Docker**: [Install Guide](https://docs.docker.com/get-docker/)
- **Maven**: [Install Guide](https://maven.apache.org/install.html)
- **Git**: For version control
- **curl**: For testing endpoints

### GCP Account Setup
1. Create a GCP project
2. Enable the following APIs:
   - Cloud Run API
   - Cloud SQL Admin API
   - Container Registry API
   - Cloud Build API
   - Cloud Logging API

```bash
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable cloudbuild.googleapis.com
gcloud services enable logging.googleapis.com
```

## Step-by-Step Deployment

### Step 1: Set Up GCP Project Environment Variables

```bash
# Linux/macOS
export GCP_PROJECT_ID="your-gcp-project-id"
export CLOUD_RUN_SERVICE_NAME="user-management-api"
export CLOUD_RUN_REGION="us-central1"  # or your preferred region
export DB_INSTANCE="user-management-db"
export DB_USER="root"
export DB_PASSWORD="your-secure-password"
export DB_NAME="my_auth_db"
export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

# Windows (Command Prompt)
set GCP_PROJECT_ID=your-gcp-project-id
set CLOUD_RUN_SERVICE_NAME=user-management-api
set CLOUD_RUN_REGION=us-central1
set DB_INSTANCE=user-management-db
set DB_USER=root
set DB_PASSWORD=your-secure-password
set DB_NAME=my_auth_db
set JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Windows (PowerShell)
$env:GCP_PROJECT_ID = "your-gcp-project-id"
$env:CLOUD_RUN_SERVICE_NAME = "user-management-api"
$env:CLOUD_RUN_REGION = "us-central1"
$env:DB_INSTANCE = "user-management-db"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your-secure-password"
$env:DB_NAME = "my_auth_db"
$env:JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

### Step 2: Authenticate with GCP

```bash
gcloud auth login
gcloud config set project $GCP_PROJECT_ID
gcloud auth configure-docker gcr.io
```

### Step 3: Create Cloud SQL Instance

#### Using gcloud CLI

```bash
gcloud sql instances create $DB_INSTANCE \
    --database-version=MYSQL_8_0 \
    --tier=db-f1-micro \
    --region=$CLOUD_RUN_REGION \
    --availability-type=regional \
    --enable-bin-log \
    --backup-start-time=03:00
```

#### Create Database

```bash
gcloud sql databases create $DB_NAME \
    --instance=$DB_INSTANCE
```

#### Create Database User

```bash
gcloud sql users create $DB_USER \
    --instance=$DB_INSTANCE \
    --password=$DB_PASSWORD
```

### Step 4: Create Service Account

```bash
# Create service account
gcloud iam service-accounts create user-management-api-sa \
    --display-name="User Management API Service Account"

# Grant Cloud SQL Client role
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
    --member="serviceAccount:user-management-api-sa@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/cloudsql.client"

# Grant Cloud Run service access
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
    --member="serviceAccount:user-management-api-sa@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/run.invoker"

# Grant Cloud Logging write
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
    --member="serviceAccount:user-management-api-sa@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/logging.logWriter"
```

### Step 5: Build and Push Docker Image

#### Option A: Using Deployment Script (Recommended)

**Linux/macOS:**
```bash
chmod +x deploy-to-cloud-run.sh
./deploy-to-cloud-run.sh
```

**Windows (PowerShell):**
```powershell
.\deploy-to-cloud-run.bat
```

#### Option B: Manual Steps

```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t gcr.io/$GCP_PROJECT_ID/user-management-api:latest .

# Push to Google Container Registry
docker push gcr.io/$GCP_PROJECT_ID/user-management-api:latest
```

### Step 6: Deploy to Cloud Run

```bash
gcloud run deploy user-management-api \
    --image gcr.io/$GCP_PROJECT_ID/user-management-api:latest \
    --platform managed \
    --region $CLOUD_RUN_REGION \
    --allow-unauthenticated \
    --set-env-vars="SPRING_PROFILES_ACTIVE=gcp,DB_USER=$DB_USER,DB_NAME=$DB_NAME,JWT_SECRET=$JWT_SECRET,LOG_LEVEL=INFO" \
    --add-cloudsql-instances="$GCP_PROJECT_ID:$CLOUD_RUN_REGION:$DB_INSTANCE" \
    --service-account="user-management-api-sa@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
    --memory 512Mi \
    --cpu 1 \
    --max-instances 10 \
    --min-instances 1 \
    --timeout 3600 \
    --port 8080
```

### Step 7: Get Service URL and Test

```bash
# Get the service URL
SERVICE_URL=$(gcloud run services describe user-management-api \
    --platform managed \
    --region $CLOUD_RUN_REGION \
    --format='value(status.url)')

echo "Service URL: $SERVICE_URL"

# Test health endpoint
curl -X GET "$SERVICE_URL/actuator/health"

# Test login endpoint
curl -X POST "$SERVICE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@fincore.com","password":"admin123"}'
```

## Troubleshooting

### Build Issues
- **Maven compilation errors**: Run `mvn clean compile` locally first
- **Docker build errors**: Check Docker daemon is running with `docker ps`
- **GCR authentication**: Run `gcloud auth configure-docker gcr.io`

### Deployment Issues
- **Cloud SQL connection timeout**: Ensure Cloud SQL instance is running and accessible
- **Service account permissions**: Verify service account has Cloud SQL Client role
- **Timeout errors**: Check application startup logs with:
  ```bash
  gcloud run logs read user-management-api --region $CLOUD_RUN_REGION --limit 50
  ```

### Runtime Issues
- **Database connection errors**: Verify database credentials and instance name
- **Health check failures**: Check application logs for startup errors
- **Port binding errors**: Ensure application listens on port 8080

### View Logs

```bash
# Stream real-time logs
gcloud run logs read user-management-api \
    --region $CLOUD_RUN_REGION \
    --follow

# View specific revision logs
gcloud run logs read user-management-api \
    --region $CLOUD_RUN_REGION \
    --limit 100
```

### Monitor Performance

```bash
# View service metrics
gcloud monitoring metrics-descriptors list --filter="service:user-management-api"

# Check service details
gcloud run services describe user-management-api \
    --region $CLOUD_RUN_REGION
```

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile to use | `gcp` |
| `DB_USER` | Database username | `root` |
| `DB_NAME` | Database name | `my_auth_db` |
| `DB_PASSWORD` | Database password | (set via GCP secrets) |
| `JWT_SECRET` | JWT signing secret | (base64 encoded) |
| `LOG_LEVEL` | Application log level | `INFO`, `DEBUG` |
| `PORT` | Server port (Cloud Run sets this) | `8080` |

## Database Configuration

The application uses a connection pool configured for Cloud Run:
- **Max connections**: 5
- **Min idle**: 1
- **Connection timeout**: 30 seconds
- **Idle timeout**: 15 minutes
- **Max lifetime**: 30 minutes

This configuration is optimized for the serverless nature of Cloud Run.

## Security Considerations

1. **Database Password**: Store in Google Secret Manager
   ```bash
   echo -n "$DB_PASSWORD" | gcloud secrets create db-password --data-file=-
   ```

2. **Service Account**: Use least privilege principle
   - Only grant necessary roles
   - Use separate service accounts per environment

3. **API Security**:
   - Use HTTPS (Cloud Run provides this automatically)
   - Implement rate limiting
   - Validate all inputs
   - Use strong JWT secrets

4. **Cloud SQL Security**:
   - Use Cloud SQL Auth proxy (already configured)
   - Enable SSL for connections
   - Use VPC connectors for additional isolation

## Scaling and Performance

### Adjust Instance Settings
```bash
gcloud run services update user-management-api \
    --region $CLOUD_RUN_REGION \
    --memory 1Gi \
    --cpu 2 \
    --max-instances 50 \
    --min-instances 2
```

### Monitor Costs
- Cloud Run: Pay only for request execution time
- Cloud SQL: Varies by instance type and usage
- Container Registry: Storage fees for images

## Rollback Procedures

```bash
# View service revisions
gcloud run revisions list --service=user-management-api --region=$CLOUD_RUN_REGION

# Deploy previous version
gcloud run deploy user-management-api \
    --image gcr.io/$GCP_PROJECT_ID/user-management-api:previous-tag \
    --region $CLOUD_RUN_REGION
```

## Continuous Deployment

For CI/CD integration, see `.github/workflows/` or your preferred CI/CD platform:
- GitHub Actions
- GitLab CI/CD
- Jenkins
- Cloud Build

## Support and Resources

- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Cloud SQL Documentation](https://cloud.google.com/sql/docs)
- [Spring Boot on Google Cloud](https://spring.io/guides/gs/cloud-gcp/)
- [GCP Pricing Calculator](https://cloud.google.com/products/calculator)
