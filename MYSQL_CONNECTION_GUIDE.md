# Connect Cloud Run to MySQL - Complete Guide

**Objective:** Configure User Management API to use Cloud SQL MySQL database

**Current Status:**
- ✅ Cloud SQL MySQL instance: RUNNABLE
- ✅ Database: `my_auth_db` created
- ✅ User: `fincore_app` created
- ⏳ Connection: Needs configuration

---

## 📋 **Step 1: Verify Database Setup**

### Check database exists:
```powershell
gcloud sql databases list --instance=fincore-npe-db --project=project-07a61357-b791-4255-a9e
```

Expected output should show:
```
NAME         CHARSET  COLLATION
my_auth_db   utf8mb4  utf8mb4_0900_ai_ci
```

### Check database user exists:
```powershell
gcloud sql users list --instance=fincore-npe-db --project=project-07a61357-b791-4255-a9e
```

Expected output:
```
NAME           HOST  TYPE
fincore_app         BUILT_IN
root                BUILT_IN
```

---

## 🔐 **Step 2: Set Correct Database Password**

### Set a strong password for fincore_app:
```powershell
$NEW_PASSWORD = "SecurePass123!Fincore@2025"

gcloud sql users set-password fincore_app `
  --instance=fincore-npe-db `
  --password=$NEW_PASSWORD `
  --project=project-07a61357-b791-4255-a9e
```

### Update GCP Secret with new password:
```powershell
echo "SecurePass123!Fincore@2025" | gcloud secrets versions add fincore-npe-db-password `
  --data-file=- `
  --project=project-07a61357-b791-4255-a9e
```

This creates version 3 of the secret.

---

## ✅ **Step 3: Grant Database Permissions**

### Connect to database as root:
```powershell
gcloud sql connect fincore-npe-db `
  --user=root `
  --project=project-07a61357-b791-4255-a9e
```

When prompted for password, press Enter (no password for root initially).

### Grant privileges to fincore_app:
Once connected, run these SQL commands:

```sql
-- Grant all privileges on my_auth_db to fincore_app
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%' WITH GRANT OPTION;

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify permissions
SHOW GRANTS FOR 'fincore_app'@'%';

-- Exit connection
EXIT;
```

---

## 🌐 **Step 4: Configure Application Connection**

### Check application-gcp.yml:
```yaml
# Already configured in src/main/resources/application-gcp.yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/${DB_NAME:my_auth_db}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USER:fincore_app}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**Note:** Cloud Run automatically proxies localhost:3306 to Cloud SQL via Cloud SQL Auth Proxy!

---

## 🔧 **Step 5: Environment Variables for Cloud Run**

These are already set but verify:

```powershell
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"

# Verify service account has Cloud SQL client role
gcloud projects get-iam-policy $PROJECT_ID `
  --flatten="bindings[].members" `
  --filter="bindings.members:fincore-npe-cloudrun" `
  --format="table(bindings.role)"
```

Expected: Should show `roles/cloudsql.client`

---

## 🚀 **Step 6: Redeploy Cloud Run with MySQL**

### Build and push latest image (if code changed):
```powershell
cd c:\Development\git\userManagementApi

# Build Docker image
docker build -t gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest .

# Push to GCR
docker push gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest
```

### Deploy with MySQL profile:
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
  --max-instances=2 `
  --min-instances=0 `
  --timeout=600 `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_NAME=my_auth_db,DB_USER=fincore_app" `
  --set-secrets="DB_PASSWORD=fincore-npe-db-password:3,JWT_SECRET=jwt-secret:latest" `
  --set-cloudsql-instances=$CLOUDSQL `
  --project=$PROJECT_ID
```

**Key points:**
- `SPRING_PROFILES_ACTIVE=mysql` - Use MySQL profile
- `--set-cloudsql-instances=$CLOUDSQL` - Enable Cloud SQL Auth Proxy
- `DB_PASSWORD=fincore-npe-db-password:3` - Use version 3 of secret
- Service account has Cloud SQL client role

---

## 🧪 **Step 7: Verify MySQL Connection**

### Check Cloud Run logs for connection success:
```powershell
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" `
  --limit=30 `
  --project=project-07a61357-b791-4255-a9e `
  --format=text
```

Look for:
- ✅ `HikariPool-1 - Start completed` (connection pool started)
- ✅ `Starting UserManagementApplication` (app started)
- ❌ `Connection refused` (connection failed)
- ❌ `Access denied for user` (wrong password)

### Test health endpoint:
```powershell
$url = "https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health"

$response = Invoke-WebRequest -Uri $url -UseBasicParsing
$response.Content | ConvertFrom-Json | ConvertTo-Json
```

Expected response (with MySQL):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "result": 1
      }
    }
  }
}
```

---

## 🧪 **Step 8: Test API Endpoints with MySQL**

### Run Postman tests with MySQL:
```powershell
cd c:\Development\git\userManagementApi

# Update environment variable if needed or use existing
newman run postman_collection.json `
  -e postman_environment_cloud.json `
  --reporters cli,json `
  --reporter-json-export results-mysql.json
```

### Expected results:
- ✅ All 12 tests pass
- ✅ Health check shows MySQL UP
- ✅ User CRUD operations work
- ✅ Data persists between requests

---

## 🛠️ **Troubleshooting Connection Issues**

### Issue: "Connection refused"
**Cause:** Cloud SQL Auth Proxy not started
**Solution:**
1. Verify service account has `roles/cloudsql.client`
2. Check `--set-cloudsql-instances` is set correctly
3. Redeploy Cloud Run

### Issue: "Access denied for user 'fincore_app'"
**Cause:** Wrong password or user doesn't exist
**Solution:**
```powershell
# Re-set password
$PASSWORD = "SecurePass123!Fincore@2025"
gcloud sql users set-password fincore_app `
  --instance=fincore-npe-db `
  --password=$PASSWORD `
  --project=project-07a61357-b791-4255-a9e

# Update secret
echo $PASSWORD | gcloud secrets versions add fincore-npe-db-password --data-file=-

# Redeploy with new secret version
```

### Issue: "No database selected"
**Cause:** `DB_NAME` environment variable missing
**Solution:**
Ensure `--set-env-vars` includes `DB_NAME=my_auth_db`

### Issue: "Character set 'utf8mb4' is not supported"
**Cause:** MySQL connection string issue
**Solution:**
The connection string already includes proper charset config, shouldn't be an issue.

---

## 📊 **Verification Checklist**

- [ ] Cloud SQL instance is RUNNABLE
- [ ] Database `my_auth_db` exists
- [ ] User `fincore_app` exists with password set
- [ ] Database has proper schema (tables created)
- [ ] Service account has Cloud SQL client role
- [ ] Secret `fincore-npe-db-password` has version 3
- [ ] Cloud Run environment vars set: `SPRING_PROFILES_ACTIVE=mysql`
- [ ] Cloud Run secret refs updated: `DB_PASSWORD=...version:3`
- [ ] Cloud Run has `--set-cloudsql-instances` configured
- [ ] Service deployed successfully (check logs for no errors)
- [ ] Health endpoint shows MySQL UP
- [ ] All API tests pass with Postman

---

## 🎯 **Quick Command Summary**

```powershell
# 1. Set password
gcloud sql users set-password fincore_app --instance=fincore-npe-db --password="SecurePass123!Fincore@2025" --project=project-07a61357-b791-4255-a9e

# 2. Update secret
echo "SecurePass123!Fincore@2025" | gcloud secrets versions add fincore-npe-db-password --data-file=- --project=project-07a61357-b791-4255-a9e

# 3. Redeploy (copy full command from Step 6)

# 4. Check logs
gcloud logging read "resource.type=cloud_run_revision" --limit=30 --follow --project=project-07a61357-b791-4255-a9e

# 5. Test
newman run postman_collection.json -e postman_environment_cloud.json --reporters cli
```

---

## 🎉 **After MySQL Connection Works**

Once MySQL is connected:
- ✅ All data persists in Cloud SQL
- ✅ Multiple instances can share same database
- ✅ Database backups automatically configured
- ✅ Production-ready setup complete

---

**Next Steps:**
1. Follow steps 1-6 above
2. Run tests to verify
3. Monitor logs if issues occur
4. All data now stored in MySQL!
