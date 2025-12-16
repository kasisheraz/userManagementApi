# MySQL Setup Guide for Cloud Run

**Status**: Application deployed and working with H2  
**Goal**: Connect to MySQL in Cloud SQL

---

## 🔍 Current Issue

**Error**: `Connection refused on 127.0.0.1:3306`

**Root Cause**: Cloud SQL Auth Proxy is not being properly injected by Cloud Run

**Solution Options**:
1. Use VPC Connector (Recommended for production)
2. Use Cloud SQL Proxy sidecar container
3. Enable Cloud Run-Cloud SQL native connection

---

## ✅ What's Already Configured

- ✅ `my_auth_db` database created in Cloud SQL
- ✅ `fincore_app` user created with password
- ✅ Database secrets stored in Secret Manager
- ✅ Cloud SQL Admin API enabled
- ✅ Service account has `cloudsql.client` role
- ✅ `--set-cloudsql-instances` parameter is correct

---

## 🛠️ Steps to Get MySQL Working

### **Option 1: Use VPC Connector (RECOMMENDED)**

#### Step 1: Create VPC Connector

```powershell
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"

gcloud compute networks vpc-access connectors create fincore-connector `
    --region=$REGION `
    --subnet=default `
    --machine-type=f1-micro `
    --min-instances=2 `
    --max-instances=3 `
    --project=$PROJECT_ID
```

**Wait for creation**: 5-10 minutes

#### Step 2: Verify Connector Created

```powershell
gcloud compute networks vpc-access connectors describe fincore-connector `
    --region=europe-west2 `
    --project=project-07a61357-b791-4255-a9e
```

#### Step 3: Deploy with VPC Connector

```powershell
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"
$CLOUDSQL = "$PROJECT_ID`:$REGION`:fincore-npe-db"

gcloud run deploy fincore-npe-api `
    --image=gcr.io/$PROJECT_ID/fincore-api:latest `
    --region=$REGION `
    --platform=managed `
    --allow-unauthenticated `
    --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com `
    --memory=512Mi `
    --cpu=1 `
    --vpc-connector=fincore-connector `
    --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_NAME=my_auth_db,DB_USER=fincore_app" `
    --set-secrets="DB_PASSWORD=fincore-npe-db-password:2,JWT_SECRET=jwt-secret:latest" `
    --set-cloudsql-instances=$CLOUDSQL `
    --project=$PROJECT_ID
```

---

### **Option 2: Use Private IP (IF VPC available)**

If your Cloud SQL instance has a private IP:

```powershell
# Get private IP
gcloud sql instances describe fincore-npe-db `
    --project=project-07a61357-b791-4255-a9e `
    --format='value(ipAddresses[0].ipAddress)'
```

Update `application-gcp.yml`:
```yaml
datasource:
  url: jdbc:mysql://PRIVATE_IP:3306/my_auth_db
```

---

## 🧪 Test MySQL Connection

### Local Test (Before Cloud Run)

```powershell
# Download Cloud SQL Proxy
# https://cloud.google.com/sql/docs/mysql/sql-proxy

# Run locally:
cloud_sql_proxy -instances=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db=tcp:3306

# In new terminal:
$response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
$response.Content
```

---

## 📋 Database Permissions (If Needed)

Grant permissions manually using Cloud Shell:

```bash
# In Cloud Console → Cloud Shell
gcloud sql connect fincore-npe-db --user=root

# Then in MySQL prompt:
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%';
FLUSH PRIVILEGES;
```

---

## 🔍 Troubleshooting

### **Error**: `Connection refused on 127.0.0.1:3306`

**Cause**: Cloud SQL Auth Proxy not injected

**Solutions**:
1. Use VPC Connector (see Option 1 above)
2. Use Private IP with VPC
3. Ensure service account has `cloudsql.client` role
   ```powershell
   gcloud projects get-iam-policy project-07a61357-b791-4255-a9e `
       --flatten="bindings[].members" `
       --filter="bindings.members:fincore-npe-cloudrun*"
   ```

### **Error**: `Unknown database 'my_auth_db'`

**Solution**: Database exists but user lacks permissions
```bash
gcloud sql connect fincore-npe-db --user=root
SHOW GRANTS FOR 'fincore_app'@'%';
GRANT ALL ON my_auth_db.* TO 'fincore_app'@'%';
```

### **Error**: `Access denied for user 'fincore_app'`

**Solution**: Password mismatch

```powershell
# Update password:
$NEW_PASSWORD = "NewPassword123!Fincore"
gcloud sql users set-password fincore_app `
    --instance=fincore-npe-db `
    --password=$NEW_PASSWORD `
    --project=project-07a61357-b791-4255-a9e

# Update secret:
echo $NEW_PASSWORD | gcloud secrets versions add fincore-npe-db-password `
    --data-file=- `
    --project=project-07a61357-b791-4255-a9e
```

---

## 📊 Current Working Setup

**Service**: Running with H2 database
```
https://fincore-npe-api-994490239798.europe-west2.run.app
```

**Status**: ✅ All API endpoints working
- ✅ Health check
- ✅ Login
- ✅ User CRUD operations
- ✅ Protected endpoints

---

## 🚀 Next: Migrate to MySQL

### Manual Steps:

1. **Create VPC Connector** (5-10 min)
2. **Redeploy with `--vpc-connector=fincore-connector`**
3. **Test endpoints** (health, login, users)
4. **Run Postman tests** against MySQL

### Quick Commands:

```powershell
# 1. Create connector
gcloud compute networks vpc-access connectors create fincore-connector `
    --region=europe-west2 `
    --subnet=default `
    --machine-type=f1-micro `
    --project=project-07a61357-b791-4255-a9e

# 2. Deploy with MySQL + VPC
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"
$CLOUDSQL = "$PROJECT_ID`:$REGION`:fincore-npe-db"

gcloud run deploy fincore-npe-api `
    --image=gcr.io/$PROJECT_ID/fincore-api:latest `
    --region=$REGION `
    --platform=managed `
    --allow-unauthenticated `
    --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com `
    --memory=512Mi `
    --cpu=1 `
    --vpc-connector=fincore-connector `
    --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_NAME=my_auth_db,DB_USER=fincore_app" `
    --set-secrets="DB_PASSWORD=fincore-npe-db-password:2,JWT_SECRET=jwt-secret:latest" `
    --set-cloudsql-instances=$CLOUDSQL `
    --project=$PROJECT_ID

# 3. Test
.\test-cloud-deployment.ps1
```

---

## ✅ Summary

- **Current**: H2 in-memory database (working perfectly)
- **Goal**: Connect to MySQL in Cloud SQL
- **Blocker**: Cloud SQL Auth Proxy injection
- **Solution**: Use VPC Connector
- **Timeline**: 15-20 minutes total

**Ready to proceed?** Follow Option 1 above to set up VPC Connector and MySQL.
