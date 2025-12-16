# GCP Cloud Run Deployment Plan - Cost-Optimized NPE Configuration

**Revised:** December 15, 2025  
**Environment:** NPE (Non-Production/Development)  
**Target Monthly Cost:** $8-12 (vs. previous $80-85)

---

## Cost Optimization Strategy

### Problem: High VPC Connector Cost
- **VPC Connector (hourly rate)**: $73/month (always running)
- This is unnecessary for a development environment

### Solution: Use Cloud SQL Auth Proxy

Cloud Run has **built-in Cloud SQL Auth Proxy** support that:
- ✅ Costs nothing (included in Cloud Run)
- ✅ Doesn't require VPC Connector
- ✅ Uses service account authentication
- ✅ Handles SSL/TLS encryption
- ✅ Supports private Cloud SQL instances

---

## Optimized Cost Breakdown

| Service | Configuration | Cost |
|---------|-------------|------|
| **Cloud Run** | 256Mi, 0.5 CPU, max 2 instances, min 0 | ~$0-2 (free tier covers most of NPE usage) |
| **Cloud SQL** | db-f1-micro, 10GB storage | ~$6 |
| **Cloud Storage** | 3 buckets, standard class, 10GB | ~$0.20 |
| **Networking** | ❌ NO VPC Connector | $0 |
| **Monitoring** | Cloud Logging (7-day retention) | ~$0.50 |
| **Secrets Manager** | Secret storage | ~$0.50 |
| **Total Monthly** | | **~$8-12/month** |

**Savings: $65-70/month** 🎉

---

## Implementation: Removing VPC Connector

### Step 1: Update Deployment Configuration

**Remove from gcloud command:**

```powershell
# OLD (with VPC Connector - $73/month):
gcloud run deploy $SERVICE_NAME `
  --vpc-connector=$VPC_CONNECTOR `
  ...

# NEW (without VPC Connector - FREE):
gcloud run deploy $SERVICE_NAME `
  --set-cloudsql-instances="$PROJECT_ID:$REGION:fincore-npe-db" `
  ...
```

### Step 2: Update application-gcp.yml

Cloud SQL Auth Proxy automatically handles the connection via service account:

```yaml
spring:
  application:
    name: user-management-api
  datasource:
    # Cloud SQL Auth Proxy: use socket factory
    url: jdbc:mysql:///my_auth_db?cloudSqlInstance=PROJECT_ID:REGION:fincore-npe-db&user=${DB_USER}&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false
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

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    root: INFO
```

### Step 3: Add Cloud SQL Connector Dependency

Update `pom.xml`:

```xml
<dependency>
    <groupId>com.google.cloud.sql</groupId>
    <artifactId>cloud-sql-connector-mysql-socket-factory</artifactId>
    <version>1.15.1</version>
</dependency>
```

### Step 4: New Deployment Command

```powershell
# Set variables
$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"
$SERVICE_NAME = "fincore-npe-api"
$IMAGE = "gcr.io/$PROJECT_ID/fincore-api:latest"
$SERVICE_ACCOUNT = "fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"

# Deploy WITHOUT VPC Connector
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
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080" `
  --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest" `
  --set-cloudsql-instances="$PROJECT_ID:$REGION:fincore-npe-db" `
  --project=$PROJECT_ID
```

**Key changes:**
- ❌ Removed: `--vpc-connector=$VPC_CONNECTOR`
- ✅ Added: `--set-cloudsql-instances` (Cloud SQL Auth Proxy)

### Step 5: Verify IAM Role

Ensure service account has Cloud SQL client role:

```powershell
gcloud projects add-iam-policy-binding $PROJECT_ID `
  --member="serviceAccount:$SERVICE_ACCOUNT" `
  --role="roles/cloudsql.client"
```

---

## Additional Cost Savings (Optional)

### Option 1: Turn Off Cloud SQL When Not in Use

```powershell
# Stop Cloud SQL instance (saves $6/month when stopped)
gcloud sql instances patch fincore-npe-db --backup=false

# Pause (stop charging hourly)
gcloud sql instances describe fincore-npe-db --region=$REGION

# Note: Stopping doesn't work directly, but deleting daily backups saves ~$1
gcloud sql backups list --instance=fincore-npe-db
```

### Option 2: Use Cheaper Storage

```powershell
# Store Terraform state in cheaper location
gsutil lifecycle set lifecycle.json gs://fincore-npe-terraform-state/

# lifecycle.json:
# {
#   "lifecycle": {
#     "rule": [{
#       "action": {"type": "Delete"},
#       "condition": {"age": 30, "matchesStorageClass": ["STANDARD"]}
#     }]
#   }
# }
```

### Option 3: Development-Only Database (Optional)

For pure development, consider using H2 in-memory database:

```yaml
# application-dev.yml (for local development)
spring:
  profiles:
    active: h2
  datasource:
    url: jdbc:h2:mem:fincore_db
    driver-class-name: org.h2.Driver
```

---

## Updated Terraform for Cost-Optimized NPE

Update your fincore_Iasc Terraform to exclude VPC Connector:

```hcl
# terraform/environments/npe/terraform.tfvars

project_id = "fincore-npe-project"
region = "europe-west2"
environment = "npe"

# Compute - minimal sizing
cloud_run_memory = 256
cloud_run_cpu = "0.5"
cloud_run_max_instances = 2
cloud_run_min_instances = 0  # Scale to zero when idle

# Database - minimal tier
cloud_sql_tier = "db-f1-micro"
cloud_sql_backup_enabled = false  # Optional: disable backups

# Networking
enable_vpc_connector = false  # DISABLE VPC CONNECTOR
enable_load_balancer = false
enable_dns = false

# Storage - minimal
storage_retention_days = 7
```

### Update Cloud Run Terraform Module

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
      
      # REMOVED: vpc_access_connector = google_vpc_access_connector.connector.name
      # Using Cloud SQL Auth Proxy instead (built-in to Cloud Run)
    }
    metadata {
      annotations = {
        "run.googleapis.com/cloudsql-instances" = google_sql_database_instance.main.connection_name
        "autoscaling.knative.dev/maxScale"      = "2"
        "autoscaling.knative.dev/minScale"      = "0"  # Scale to zero
      }
    }
  }

  depends_on = [
    google_sql_database_instance.main,
    # REMOVED: google_vpc_access_connector.connector
  ]
}
```

---

## Updated Deployment Plan (Cost-Optimized)

### Phase 1: Infrastructure Verification ✅
- Skip VPC Connector (already deployed but unused)

### Phase 2: Application Updates (15 min)
```powershell
# 1. Update pom.xml with Cloud SQL Connector dependency
# 2. Update application-gcp.yml with new datasource URL
# 3. Rebuild application
mvn clean package -DskipTests -q
```

### Phase 3: Docker Build & Push (30 min)
```powershell
docker build -t gcr.io/$PROJECT_ID/fincore-api:latest .
docker push gcr.io/$PROJECT_ID/fincore-api:latest
```

### Phase 4: Deploy Without VPC Connector (15 min)
```powershell
gcloud run deploy fincore-npe-api `
  --image=gcr.io/$PROJECT_ID/fincore-api:latest `
  --region=europe-west2 `
  --platform=managed `
  --allow-unauthenticated `
  --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com `
  --memory=256Mi `
  --cpu=0.5 `
  --max-instances=2 `
  --min-instances=0 `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080" `
  --set-secrets="DB_PASSWORD=db-password:latest" `
  --set-cloudsql-instances="$PROJECT_ID:europe-west2:fincore-npe-db"
```

### Phase 5: Testing (20 min)
- Same as before

---

## Monitoring Costs

```powershell
# View GCP billing for fincore-npe-project
gcloud billing accounts list
gcloud billing budgets list --billing-account=BILLING_ACCOUNT_ID

# Set budget alert for $20/month to prevent surprises
gcloud billing budgets create \
  --display-name="NPE Budget" \
  --billing-account=BILLING_ACCOUNT_ID \
  --budget-amount=20 \
  --threshold-rule=percent=80 \
  --threshold-rule=percent=100
```

---

## Cost Comparison Table

| Metric | Original Plan | Optimized Plan | Savings |
|--------|---|---|---|
| VPC Connector | $73 | $0 | **$73/month** |
| Cloud Run | $2 | $2 | - |
| Cloud SQL | $6 | $6 | - |
| Storage | $0.20 | $0.20 | - |
| Monitoring | $0.50 | $0.50 | - |
| **Total** | **$81.70** | **$8.70** | **90% reduction** |

---

## Implementation Checklist

- [ ] Update pom.xml with Cloud SQL Connector dependency
- [ ] Update application-gcp.yml with new datasource URL
- [ ] Rebuild and test application locally
- [ ] Update Terraform terraform.tfvars (set enable_vpc_connector=false)
- [ ] Push new image to gcr.io
- [ ] Deploy to Cloud Run with new configuration
- [ ] Test Cloud SQL connectivity
- [ ] Verify no errors in Cloud Run logs
- [ ] Monitor Cloud Logging for first 30 minutes
- [ ] Confirm billing dashboard shows reduced costs

---

## Quick Start: Cost-Optimized Deployment

```powershell
# 1. Update pom.xml
# Add: com.google.cloud.sql:cloud-sql-connector-mysql-socket-factory

# 2. Update application-gcp.yml datasource URL
# Change to: jdbc:mysql:///my_auth_db?cloudSqlInstance=...

# 3. Build
cd c:\Development\git\userManagementApi
mvn clean package -DskipTests -q

# 4. Build Docker image
docker build -t gcr.io/fincore-npe-project/fincore-api:latest .

# 5. Push to registry
gcloud auth configure-docker gcr.io
docker push gcr.io/fincore-npe-project/fincore-api:latest

# 6. Deploy (NO VPC CONNECTOR)
gcloud run deploy fincore-npe-api `
  --image=gcr.io/fincore-npe-project/fincore-api:latest `
  --region=europe-west2 `
  --memory=256Mi `
  --cpu=0.5 `
  --max-instances=2 `
  --min-instances=0 `
  --service-account=fincore-npe-cloudrun@fincore-npe-project.iam.gserviceaccount.com `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080" `
  --set-secrets="DB_PASSWORD=db-password:latest" `
  --set-cloudsql-instances="fincore-npe-project:europe-west2:fincore-npe-db" `
  --allow-unauthenticated

# 7. Verify
gcloud run services describe fincore-npe-api --region=europe-west2
```

---

## Why Cloud SQL Auth Proxy is Better for NPE

| Feature | VPC Connector | Cloud SQL Auth Proxy |
|---------|---|---|
| **Cost** | $73/month | FREE ✅ |
| **Setup** | Complex networking | Simple (annotation) ✅ |
| **Security** | Private IP in VPC | Service account auth ✅ |
| **Scalability** | Fixed resource | Auto-scales with Cloud Run ✅ |
| **Maintenance** | Requires management | Managed by Google ✅ |
| **Best for** | Production | Development ✅ |

---

## References

- [Cloud SQL Auth Proxy - Cloud Run Docs](https://cloud.google.com/sql/docs/mysql/sql-proxy)
- [Cloud SQL Connector MySQL - GitHub](https://github.com/GoogleCloudPlatform/cloud-sql-java-connector)
- [Cloud Run Cost Optimization](https://cloud.google.com/run/pricing)

---

**Summary:** This optimized approach reduces NPE costs from **$81/month to $8-12/month** while maintaining full functionality and better security. The VPC Connector was unnecessary for development use cases since Cloud SQL Auth Proxy handles private connectivity automatically.
