# Comprehensive GCP Cloud Run Deployment Plan
## User Management API - fincore-npe-project

**Document Version:** 1.0  
**Date:** December 15, 2025  
**Environment:** NPE (Non-Production)  
**Target GCP Project:** `fincore-npe-project`  
**Target Region:** `europe-west2` (London)  
**Application:** User Management API (Java Spring Boot 3.2.0)

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [Prerequisites & Setup](#prerequisites--setup)
4. [Phase 1: Infrastructure Preparation](#phase-1-infrastructure-preparation)
5. [Phase 2: Application Preparation](#phase-2-application-preparation)
6. [Phase 3: Container Image Building](#phase-3-container-image-building)
7. [Phase 4: Cloud Run Deployment](#phase-4-cloud-run-deployment)
8. [Phase 5: Testing & Validation](#phase-5-testing--validation)
9. [Phase 6: Post-Deployment](#phase-6-post-deployment)
10. [Rollback Procedures](#rollback-procedures)
11. [Monitoring & Troubleshooting](#monitoring--troubleshooting)
12. [Cost Estimation](#cost-estimation)

---

## Executive Summary

This document provides a complete deployment strategy for the **User Management API** to Google Cloud Platform's Cloud Run service in the **fincore-npe-project**. The deployment leverages the existing **fincore_Iasc** Infrastructure as Code (Terraform) modules that have already provisioned:

- ✅ VPC Network with Serverless VPC Connector
- ✅ Cloud SQL MySQL 8.0 Instance (fincore-npe-db)
- ✅ Cloud Storage buckets (state, artifacts, uploads)
- ✅ Service accounts with IAM roles
- ✅ Monitoring, logging, and security infrastructure

**Current Status:** Infrastructure ready. Application deployment pending.

**Deployment Approach:** Multi-phase, automated with validation at each step.

---

## Architecture Overview

### High-Level Deployment Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Applications                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────────┐
        │      Cloud Run Service                   │
        │  fincore-npe-api:latest                 │
        │  - 256Mi Memory, 0.5 CPU                │
        │  - Max 2 instances, auto-scaling        │
        │  - Service Account: fincore-npe-cloudrun│
        └──────────────────┬───────────────────────┘
                           │
                           │ (via Serverless VPC Connector)
                           │
        ┌──────────────────▼───────────────────────┐
        │   VPC Network (fincore-npe-vpc)          │
        │   Subnet: 10.0.0.0/20                   │
        └──────────────────┬───────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────────┐
        │    Cloud SQL MySQL 8.0                  │
        │    Instance: fincore-npe-db            │
        │    Database: my_auth_db                 │
        │    Users: root, fincore_app             │
        │    Private IP only (SSL required)       │
        └──────────────────────────────────────────┘
```

### Key Components

| Component | Details |
|-----------|---------|
| **Service** | Cloud Run (serverless container) |
| **Image Registry** | Artifact Registry (gcr.io) |
| **Database** | Cloud SQL MySQL 8.0 (private VPC) |
| **Networking** | Serverless VPC Connector → VPC → Cloud SQL |
| **Security** | Service accounts, Secret Manager, SSL |
| **Monitoring** | Cloud Logging, Cloud Monitoring, Alerts |
| **Storage** | GCS buckets for artifacts and state |

---

## Prerequisites & Setup

### 1. Required Tools & Versions

Install or verify the following on your local machine (Windows):

```powershell
# 1. Google Cloud SDK (gcloud CLI)
# Download from: https://cloud.google.com/sdk/docs/install-windows
gcloud --version
# Expected: Google Cloud SDK (>= 400)

# 2. Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop
docker --version
# Expected: Docker version 20.10+ 

# 3. Maven
# Download from: https://maven.apache.org/download.cgi
mvn --version
# Expected: Apache Maven 3.8+

# 4. Git
# Download from: https://git-scm.com/download/win
git --version
# Expected: git version 2.40+

# 5. Terraform (for verification only)
# Download from: https://www.terraform.io/downloads
terraform --version
# Expected: Terraform v1.6+

# 6. PowerShell
# Comes pre-installed. Verify version:
$PSVersionTable.PSVersion
# Expected: PowerShell 5.1+
```

### 2. GCP Project Configuration

```powershell
# Set the GCP project
gcloud config set project fincore-npe-project

# List all projects to verify
gcloud projects list

# Verify APIs are enabled
gcloud services list --enabled | grep -E "run|sql|storage|secret|monitoring"

# If APIs not enabled, enable them:
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  compute.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  monitoring.googleapis.com \
  logging.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com
```

### 3. Authentication Setup

```powershell
# Authenticate with GCP
gcloud auth application-default login

# Create a service account for deployments (optional but recommended)
gcloud iam service-accounts create cloud-run-deployer \
  --display-name="Cloud Run Deployment Service Account"

# Grant Cloud Run Admin role
gcloud projects add-iam-policy-binding fincore-npe-project \
  --member="serviceAccount:cloud-run-deployer@fincore-npe-project.iam.gserviceaccount.com" \
  --role="roles/run.admin"

# Create a key for CI/CD pipelines (store securely)
gcloud iam service-accounts keys create deployer-key.json \
  --iam-account=cloud-run-deployer@fincore-npe-project.iam.gserviceaccount.com
```

### 4. Verify Infrastructure

```powershell
# Verify Cloud SQL instance exists
gcloud sql instances describe fincore-npe-db --region=europe-west2

# Verify VPC Connector exists
gcloud compute vpc-access connectors describe npe-connector --region=europe-west2

# Verify storage buckets exist
gcloud storage buckets list

# Verify service accounts exist
gcloud iam service-accounts list
```

---

## Phase 1: Infrastructure Preparation

### Step 1.1: Verify Infrastructure (5 minutes)

The fincore_Iasc repository has already deployed the infrastructure. Verify it's ready:

```powershell
# Option A: Run the PowerShell test script from fincore_Iasc repo
# (if you have it cloned locally)
cd path/to/fincore_Iasc
./test-npe-infrastructure.ps1

# Option B: Manual verification
# 1. Check Cloud SQL
$dbName = gcloud sql instances describe fincore-npe-db \
  --region=europe-west2 \
  --format="value(name)"
Write-Host "Cloud SQL Instance: $dbName"

# 2. Check VPC Connector
$connector = gcloud compute vpc-access connectors describe npe-connector \
  --region=europe-west2 \
  --format="value(name)"
Write-Host "VPC Connector: $connector"

# 3. Check Service Accounts
gcloud iam service-accounts list --filter="email:fincore-npe-*"

# 4. Check Storage
gcloud storage buckets list --project=fincore-npe-project
```

### Step 1.2: Retrieve Infrastructure Details (5 minutes)

Extract key information needed for deployment:

```powershell
# Set variables for later use
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"
$SERVICE_ACCOUNT = "fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"
$VPC_CONNECTOR = "npe-connector"
$DB_INSTANCE = "fincore-npe-db"
$DB_NAME = "my_auth_db"
$IMAGE_REGISTRY = "gcr.io/$PROJECT_ID"

# Get Cloud SQL connection details
$SQL_CONN_NAME = gcloud sql instances describe $DB_INSTANCE \
  --region=$REGION \
  --format="value(connectionName)"
Write-Host "Cloud SQL Connection Name: $SQL_CONN_NAME"

# Get VPC Network name
$VPC_NETWORK = gcloud compute vpc-access connectors describe $VPC_CONNECTOR \
  --region=$REGION \
  --format="value(network)"
Write-Host "VPC Network: $VPC_NETWORK"
```

### Step 1.3: Create/Update Secrets in Secret Manager (10 minutes)

Store sensitive data securely:

```powershell
# Get current database password (from your terraform state or existing deployment)
# Replace 'YOUR_DB_PASSWORD' with the actual password
$DB_PASSWORD = "YOUR_DB_PASSWORD"

# Create secret for database password
echo $DB_PASSWORD | gcloud secrets create db-password \
  --replication-policy="automatic" \
  --data-file=- \
  --project=$PROJECT_ID \
  2>$null  # Ignore if already exists

# Update if it already exists
echo $DB_PASSWORD | gcloud secrets versions add db-password \
  --data-file=- \
  --project=$PROJECT_ID

# Create JWT secret (if not already created)
$JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
echo $JWT_SECRET | gcloud secrets create jwt-secret \
  --replication-policy="automatic" \
  --data-file=- \
  --project=$PROJECT_ID \
  2>$null

# Grant Cloud Run service account access to secrets
gcloud secrets add-iam-policy-binding db-password \
  --member="serviceAccount:$SERVICE_ACCOUNT" \
  --role="roles/secretmanager.secretAccessor" \
  --project=$PROJECT_ID

gcloud secrets add-iam-policy-binding jwt-secret \
  --member="serviceAccount:$SERVICE_ACCOUNT" \
  --role="roles/secretmanager.secretAccessor" \
  --project=$PROJECT_ID
```

---

## Phase 2: Application Preparation

### Step 2.1: Update Application Configuration (10 minutes)

Modify your Spring Boot application configuration for GCP:

#### Update application-gcp.yml

```yaml
spring:
  application:
    name: user-management-api
  datasource:
    # For Cloud SQL with private VPC connector
    url: jdbc:mysql://127.0.0.1:3306/${DB_NAME:my_auth_db}?useSSL=true&allowPublicKeyRetrieval=false&serverTimezone=UTC&autoReconnect=true&zeroDateTimeBehavior=convertToNull
    username: ${DB_USER:fincore_app}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
      connection-timeout: 30000
      idle-timeout: 900000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.MySQLDialect
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql

server:
  port: ${PORT:8080}
  compression:
    enabled: true
    min-response-size: 1024
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      simple:
        enabled: true

logging:
  level:
    root: INFO
    com.fincore: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

jwt:
  secret: ${JWT_SECRET}
  expiration: 900000
```

### Step 2.2: Build & Test Locally (15 minutes)

```powershell
# Navigate to project root
cd c:\Development\git\userManagementApi

# Build the application
mvn clean package -DskipTests -q

# Verify build success
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Build successful"
} else {
    Write-Host "✗ Build failed"
    exit 1
}

# Test with H2 (in-memory database)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# In another terminal, test the health endpoint:
curl http://localhost:8080/actuator/health
```

### Step 2.3: Create Environment Variable Configuration (5 minutes)

Create a `.env.gcp` file for deployment:

```
# .env.gcp
PROJECT_ID=fincore-npe-project
REGION=europe-west2
APP_NAME=fincore-npe-api
IMAGE_NAME=fincore-api
IMAGE_TAG=latest
SERVICE_ACCOUNT=fincore-npe-cloudrun@fincore-npe-project.iam.gserviceaccount.com
VPC_CONNECTOR=npe-connector
DB_NAME=my_auth_db
DB_USER=fincore_app
DB_INSTANCE_CONNECTION_NAME=fincore-npe-project:europe-west2:fincore-npe-db
MEMORY=256Mi
CPU=0.5
MAX_INSTANCES=2
MIN_INSTANCES=0
TIMEOUT=540
PORT=8080
```

---

## Phase 3: Container Image Building

### Step 3.1: Build Docker Image Locally (15 minutes)

```powershell
# Load environment variables
$env:PROJECT_ID = "fincore-npe-project"
$env:REGION = "europe-west2"
$env:IMAGE_NAME = "fincore-api"
$env:IMAGE_TAG = "latest"

# Build image
cd c:\Development\git\userManagementApi

docker build `
  --tag gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME:$env:IMAGE_TAG `
  --tag gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME:v1.0.0 `
  -f Dockerfile .

# Verify image built successfully
docker images | grep fincore-api
```

### Step 3.2: Test Docker Image Locally (10 minutes)

```powershell
# Run container locally (without Cloud SQL connection)
docker run -d `
  --name test-api `
  -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=h2 `
  -e PORT=8080 `
  gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME:$env:IMAGE_TAG

# Wait for startup
Start-Sleep -Seconds 10

# Test health endpoint
curl http://localhost:8080/actuator/health

# View logs
docker logs test-api

# Cleanup
docker stop test-api
docker rm test-api
```

### Step 3.3: Configure Artifact Registry (5 minutes)

```powershell
# Enable Artifact Registry API (if not already enabled)
gcloud services enable artifactregistry.googleapis.com

# Create Docker repository in Artifact Registry (optional, for better security)
gcloud artifacts repositories create fincore-docker \
  --repository-format=docker \
  --location=$env:REGION \
  --project=$env:PROJECT_ID \
  2>$null  # Ignore if already exists

# Configure Docker authentication for Artifact Registry
gcloud auth configure-docker gcr.io --quiet
gcloud auth configure-docker $env:REGION-docker.pkg.dev --quiet
```

### Step 3.4: Push Image to Container Registry (10 minutes)

```powershell
# Authenticate Docker with GCP
gcloud auth configure-docker gcr.io --quiet

# Push to GCR (Google Container Registry)
docker push gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME:$env:IMAGE_TAG
docker push gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME:v1.0.0

# Verify image in registry
gcloud container images list --project=$env:PROJECT_ID

# List image tags
gcloud container images list-tags gcr.io/$env:PROJECT_ID/$env:IMAGE_NAME
```

---

## Phase 4: Cloud Run Deployment

### Step 4.1: Deploy to Cloud Run (15 minutes)

**Option A: Using gcloud CLI**

```powershell
# Set variables
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"
$SERVICE_NAME = "fincore-npe-api"
$IMAGE = "gcr.io/$PROJECT_ID/fincore-api:latest"
$SERVICE_ACCOUNT = "fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"
$VPC_CONNECTOR = "npe-connector"

# Deploy to Cloud Run
gcloud run deploy $SERVICE_NAME `
  --image=$IMAGE `
  --region=$REGION `
  --platform=managed `
  --allow-unauthenticated `
  --service-account=$SERVICE_ACCOUNT `
  --memory=256Mi `
  --cpu=0.5 `
  --max-instances=2 `
  --min-instances=0 `
  --timeout=540 `
  --ingress=internal `
  --vpc-connector=$VPC_CONNECTOR `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080,LOG_LEVEL=INFO" `
  --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest" `
  --set-cloudsql-instances="$PROJECT_ID:$REGION:fincore-npe-db" `
  --project=$PROJECT_ID

# The deployment will take 2-5 minutes
```

**Option B: Using Terraform (Recommended for IaC consistency)**

Add to your Terraform configuration:

```hcl
# terraform/modules/cloud-run/main.tf

resource "google_cloud_run_service" "api" {
  name     = "${var.environment}-api"
  location = var.region

  template {
    spec {
      service_account_name = google_service_account.cloudrun.email
      containers {
        image = "gcr.io/${var.project_id}/fincore-api:latest"
        
        env {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "mysql"
        }
        env {
          name  = "PORT"
          value = "8080"
        }
        
        env_from {
          secret_ref {
            name = "DB_PASSWORD"
            key  = google_secret_manager_secret_version.db_password.version
          }
        }
        
        resources {
          limits = {
            memory = "256Mi"
            cpu    = "0.5"
          }
        }
      }
      
      vpc_access_connector = google_vpc_access_connector.connector.name
    }
    metadata {
      annotations = {
        "run.googleapis.com/cloudsql-instances" = google_sql_database_instance.main.connection_name
        "autoscaling.knative.dev/maxScale"      = "2"
        "autoscaling.knative.dev/minScale"      = "0"
      }
    }
  }

  depends_on = [
    google_sql_database_instance.main,
    google_vpc_access_connector.connector
  ]
}

# Update Terraform with the Cloud Run resource
terraform apply -var-file="environments/npe/terraform.tfvars"
```

### Step 4.2: Verify Deployment (5 minutes)

```powershell
# Get the Cloud Run service URL
$SERVICE_URL = gcloud run services describe $SERVICE_NAME `
  --region=$REGION `
  --format='value(status.url)' `
  --project=$PROJECT_ID

Write-Host "Service URL: $SERVICE_URL"

# Test health endpoint
curl "$SERVICE_URL/actuator/health"

# Check service details
gcloud run services describe $SERVICE_NAME `
  --region=$REGION `
  --project=$PROJECT_ID

# View recent revisions
gcloud run revisions list --region=$REGION --project=$PROJECT_ID
```

### Step 4.3: Configure Service Networking (5 minutes)

If using internal ingress (private service):

```powershell
# Make service internal-only (if required)
gcloud run services update $SERVICE_NAME `
  --ingress=internal `
  --region=$REGION `
  --project=$PROJECT_ID

# Configure Cloud Load Balancer to expose internally (if needed)
# Or use Cloud API Gateway for external access with authentication
```

---

## Phase 5: Testing & Validation

### Step 5.1: Connectivity Testing (10 minutes)

```powershell
# 1. Test Cloud SQL connectivity from Cloud Run
# This is automatic via the VPC connector

# 2. Check Cloud Run logs for connection errors
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE_NAME" `
  --limit=50 `
  --format=json `
  --project=$PROJECT_ID

# 3. View recent errors
gcloud logging read "resource.type=cloud_run_revision AND severity=ERROR" `
  --limit=20 `
  --project=$PROJECT_ID
```

### Step 5.2: API Endpoint Testing (15 minutes)

```powershell
# Get the service URL
$SERVICE_URL = gcloud run services describe $SERVICE_NAME `
  --region=$REGION `
  --format='value(status.url)' `
  --project=$PROJECT_ID

# Test authentication endpoint
Invoke-WebRequest -Uri "$SERVICE_URL/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'

# Test user endpoints
Invoke-WebRequest -Uri "$SERVICE_URL/api/users" -Method GET

# Test health checks
Invoke-WebRequest -Uri "$SERVICE_URL/actuator/health" -Method GET
```

### Step 5.3: Load Testing (20 minutes)

```powershell
# Create a simple load test with ab (Apache Bench) or similar
# Install: choco install apache-bench

# Run load test
ab -n 1000 -c 10 "$SERVICE_URL/actuator/health"

# Alternative: Use Apache JMeter or Postman for more complex testing
# Import the postman_collection.json from your repo
```

### Step 5.4: Database Validation (10 minutes)

```powershell
# Connect to Cloud SQL and verify database
gcloud sql connect fincore-npe-db --user=root --region=$REGION

# Once connected (will prompt for password):
# SQL> USE my_auth_db;
# SQL> SHOW TABLES;
# SQL> SELECT COUNT(*) FROM users;
# SQL> EXIT;
```

---

## Phase 6: Post-Deployment

### Step 6.1: Configure Monitoring (10 minutes)

```powershell
# View Cloud Run metrics
gcloud monitoring metrics-descriptors list --filter="metric.type:run*"

# Create alert for high error rate
gcloud alpha monitoring policies create \
  --display-name="Cloud Run High Error Rate" \
  --condition-display-name="Error rate > 5%" \
  --condition-threshold-value=5 \
  --condition-threshold-duration=300s \
  --condition-threshold-comparison=COMPARISON_GT

# View logs
gcloud logging read "resource.type=cloud_run_revision" \
  --limit=100 \
  --format="table(timestamp, severity, jsonPayload.message)"
```

### Step 6.2: Configure Custom Domain (Optional, 15 minutes)

```powershell
# Map custom domain to Cloud Run service
gcloud run services update-traffic $SERVICE_NAME `
  --region=$REGION `
  --to-revisions=LATEST=100 `
  --project=$PROJECT_ID

# If using Cloud Armor or Load Balancer:
gcloud compute backend-services create fincore-api-backend \
  --global `
  --protocol=HTTPS `
  --health-checks=fincore-api-health-check `
  --port-name=https
```

### Step 6.3: Setup Auto-scaling Policies (5 minutes)

Already configured in Terraform with:
- Min instances: 0
- Max instances: 2
- CPU utilization target: 60%

Monitor in Cloud Console:
```
Cloud Run → Services → fincore-npe-api → Metrics
```

### Step 6.4: Create Cloud Build CI/CD Pipeline (20 minutes, Optional)

For automated builds on Git push:

```yaml
# cloudbuild.yaml
steps:
  # Build Docker image
  - name: 'gcr.io/cloud-builders/docker'
    args:
      - build
      - -t
      - gcr.io/$PROJECT_ID/fincore-api:$SHORT_SHA
      - -t
      - gcr.io/$PROJECT_ID/fincore-api:latest
      - .

  # Push to Container Registry
  - name: 'gcr.io/cloud-builders/docker'
    args:
      - push
      - gcr.io/$PROJECT_ID/fincore-api:$SHORT_SHA

  # Deploy to Cloud Run
  - name: 'gcr.io/cloud-builders/gke-deploy'
    args:
      - run
      - --filename=.
      - --image=gcr.io/$PROJECT_ID/fincore-api:$SHORT_SHA
      - --location=$_REGION
      - --namespace=default

# Configuration
images:
  - gcr.io/$PROJECT_ID/fincore-api:$SHORT_SHA
  - gcr.io/$PROJECT_ID/fincore-api:latest

substitutions:
  _REGION: 'europe-west2'
```

---

## Rollback Procedures

### Rollback via Cloud Run

```powershell
# List all revisions
gcloud run revisions list --region=$REGION --project=$PROJECT_ID

# If current version has issues, route traffic to previous revision
gcloud run services update-traffic $SERVICE_NAME `
  --region=$REGION `
  --to-revisions=<PREVIOUS_REVISION_ID>=100 `
  --project=$PROJECT_ID

# Or route traffic to specific revision percentage (blue-green deployment)
gcloud run services update-traffic $SERVICE_NAME `
  --region=$REGION `
  --to-revisions=<NEW_REVISION>=10,<OLD_REVISION>=90 `
  --project=$PROJECT_ID
```

### Rollback via Terraform

```powershell
# Revert to previous image in Terraform
# Edit terraform/environments/npe/terraform.tfvars:
# Change: container_image = "gcr.io/project/fincore-api:previous-tag"

# Apply changes
terraform plan -var-file="environments/npe/terraform.tfvars"
terraform apply -var-file="environments/npe/terraform.tfvars"
```

### Database Rollback

```powershell
# Cloud SQL automatic backups are retained for 7 days
# To restore from backup:
gcloud sql backups list --instance=fincore-npe-db

# Restore from backup (creates new instance)
gcloud sql backups restore <BACKUP_ID> \
  --backup-instance=fincore-npe-db \
  --region=$REGION
```

---

## Monitoring & Troubleshooting

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| **Cloud Run → Cloud SQL connection fails** | 1. Verify VPC Connector is `Ready` state<br>2. Check service account has `cloudsql.client` role<br>3. Verify Cloud SQL private IP is in same VPC |
| **Database connection timeout** | 1. Check Cloud SQL max connections setting<br>2. Verify connection pool configuration in application.yml<br>3. Check Cloud SQL CPU/memory usage |
| **High memory usage on Cloud Run** | 1. Increase memory limit to 512Mi<br>2. Check for memory leaks in application<br>3. Review Hibernate query N+1 problems |
| **Image pull failures** | 1. Verify image exists: `gcloud container images list`<br>2. Check service account has `storage.objects.get` role<br>3. Verify container registry is accessible |
| **Deployment timeout** | 1. Check startup time: `gcloud logging read 'resource.type=cloud_run_revision'`<br>2. Increase timeout value<br>3. Review application startup logs |

### Monitoring Dashboard Commands

```powershell
# View Cloud Run dashboard
gcloud cloud-run services describe $SERVICE_NAME `
  --region=$REGION `
  --project=$PROJECT_ID

# Export custom metrics
gcloud monitoring metrics-descriptors list --filter="metric.type:custom*"

# Create custom dashboard (via GCP Console)
# Cloud Monitoring → Dashboards → Create Dashboard
# Add widgets for:
# - Cloud Run Request Count
# - Cloud Run Request Latencies
# - Cloud SQL CPU Utilization
# - Cloud SQL Connection Count
# - Cloud Run Memory Usage
```

### Log Analysis Commands

```powershell
# Real-time log streaming
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE_NAME" `
  --follow

# Search for specific errors
gcloud logging read "severity=ERROR AND resource.type=cloud_run_revision" `
  --limit=50

# Export logs to BigQuery for analysis
gcloud logging sinks create fincore-bq-sink \
  bigquery.googleapis.com/projects/$PROJECT_ID/datasets/fincore_logs \
  --log-filter='resource.type=cloud_run_revision'
```

---

## Cost Estimation

### Monthly Costs (NPE Configuration)

| Service | Configuration | Estimated Cost |
|---------|-------------|------------------|
| **Cloud Run** | 256Mi, 0.5 CPU, max 2 instances, 1M requests/month | $0-5 (free tier covers first 2M invocations) |
| **Cloud SQL** | db-f1-micro, MySQL 8.0, 10GB storage | ~$6 |
| **Cloud Storage** | 3 buckets, standard class, 10GB total | ~$0.20 |
| **Networking** | VPC Connector (npe-connector) | ~$0.10/hour = ~$73/month |
| **Monitoring** | Basic Cloud Logging (7-day retention) | ~$0.50 |
| **Total Monthly** | | **~$80-85/month** |

### Cost Optimization Tips

1. **Reduce VPC Connector hours**: Only use when Cloud Run needs to access Cloud SQL
2. **Increase min-instances to 0**: Allows cold starts, reducing always-on costs
3. **Use Cloud Run 2nd generation**: More cost-effective for variable workloads
4. **Archive old logs**: Move logs >7 days to Cloud Storage
5. **Use Cloud Storage Lifecycle**: Auto-delete old backups after 30 days

---

## Checklist

### Pre-Deployment Checklist
- [ ] GCP project created (fincore-npe-project)
- [ ] Required APIs enabled
- [ ] Terraform infrastructure deployed (fincore_Iasc)
- [ ] Cloud SQL instance running and accessible
- [ ] VPC Connector in `Ready` state
- [ ] Service accounts created with proper IAM roles
- [ ] Secrets created in Secret Manager
- [ ] Local testing completed with H2 database
- [ ] Docker image builds successfully
- [ ] Docker authentication configured

### Deployment Checklist
- [ ] Image pushed to gcr.io
- [ ] Cloud Run service deployed
- [ ] Environment variables configured
- [ ] Secrets mounted correctly
- [ ] VPC Connector attached
- [ ] Cloud SQL instance specified
- [ ] Service account assigned
- [ ] Health checks passing

### Post-Deployment Checklist
- [ ] Health endpoint responds
- [ ] API endpoints responding
- [ ] Database connectivity verified
- [ ] Logs flowing to Cloud Logging
- [ ] Monitoring dashboards created
- [ ] Alerts configured
- [ ] Runbook documentation updated
- [ ] Backup and recovery tested

---

## Next Steps

1. **Execute Phase 1-3** following this plan
2. **Deploy to Cloud Run** (Phase 4)
3. **Run comprehensive tests** (Phase 5)
4. **Monitor for 24-48 hours** before considering production-ready
5. **Document any issues** and add to troubleshooting guide
6. **Plan for Phase 2: Frontend deployment** using same IaC approach
7. **Implement CI/CD pipeline** with Cloud Build for future deployments

---

## Additional Resources

- **Fincore IASC Repository**: https://github.com/kasisheraz/fincore_Iasc
- **GCP Cloud Run Documentation**: https://cloud.google.com/run/docs
- **Cloud SQL Best Practices**: https://cloud.google.com/sql/docs/mysql/best-practices
- **Terraform GCP Provider**: https://registry.terraform.io/providers/hashicorp/google/latest/docs
- **Spring Boot on Cloud Run**: https://cloud.google.com/run/docs/quickstarts/build-and-deploy/java

---

**Document Prepared By:** GitHub Copilot  
**Last Updated:** December 15, 2025  
**Status:** Ready for Deployment
