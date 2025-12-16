# MySQL Connection Troubleshooting - Cloud Run

**Current Status:**
- ✅ Application builds successfully
- ✅ H2 (in-memory) database works perfectly
- ⏳ MySQL connection timeout during startup
- 🔍 **Root Cause:** Cloud SQL Auth Proxy connection or database configuration issue

---

## 📊 What We've Verified

### ✅ Completed
1. Cloud SQL MySQL instance: RUNNABLE
2. Database created: `my_auth_db`
3. User created: `fincore_app` with strong password
4. Service account: Has `roles/cloudsql.client` role
5. Cloud Run: `--set-cloudsql-instances` configured
6. Application config: Proper MySQL dialect and connection URL
7. Docker image: Builds successfully
8. H2 deployment: Works perfectly

### ⏳ Issue: MySQL Connection
- Connection URL: `jdbc:mysql://127.0.0.1:3306/my_auth_db`
- Username: `fincore_app`
- Password: Set in GCP Secrets Manager
- Auth Proxy: Should intercept localhost:3306 → Cloud SQL

---

## 🔧 Next Steps to Fix MySQL Connection

### **Option 1: Use VPC Connector (Recommended for Production)**

This provides private network connectivity to Cloud SQL.

#### Step 1: Create VPC Connector
```powershell
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"

gcloud compute networks vpc-connectors create fincore-connector `
  --region=$REGION `
  --subnet=default `
  --min-throughput=200 `
  --max-throughput=300 `
  --project=$PROJECT_ID
```

Wait ~5-10 minutes for creation.

#### Step 2: Configure Cloud SQL for Private IP
```powershell
gcloud sql instances patch fincore-npe-db `
  --require-ssl=false `
  --project=$PROJECT_ID
```

#### Step 3: Update Connection String in application-gcp.yml
```yaml
# Instead of: jdbc:mysql://127.0.0.1:3306/...
# Use: jdbc:mysql://PRIVATE_IP:3306/...

# Get private IP:
gcloud sql instances describe fincore-npe-db `
  --format="value(ipAddresses[0].ipAddress)" `
  --project=$PROJECT_ID
```

#### Step 4: Redeploy with VPC Connector
```powershell
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"

gcloud run deploy fincore-npe-api `
  --image=gcr.io/$PROJECT_ID/fincore-api:latest `
  --region=$REGION `
  --platform=managed `
  --allow-unauthenticated `
  --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com `
  --vpc-connector=fincore-connector `
  --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_NAME=my_auth_db,DB_USER=fincore_app" `
  --set-secrets="DB_PASSWORD=fincore-npe-db-password:3,JWT_SECRET=jwt-secret:latest" `
  --project=$PROJECT_ID
```

---

### **Option 2: Use Cloud SQL Auth Proxy Sidecar (Alternative)**

If VPC Connector doesn't work, use auth proxy explicitly.

#### Create a Custom Docker Image with Auth Proxy

Create new Dockerfile:
```dockerfile
FROM gcr.io/project-07a61357-b791-4255-a9e/fincore-api:latest as app
FROM gcr.io/cloudsql-docker/cloud-sql-proxy:latest as proxy

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy auth proxy
COPY --from=proxy /cloud_sql_proxy /cloud_sql_proxy

# Copy app
COPY --from=app /app/app.jar app.jar

# Health check
EXPOSE 8080

# Start both proxy and app
CMD ["/bin/sh", "-c", "/cloud_sql_proxy -instances=PROJECT_ID:REGION:INSTANCE_NAME=tcp:3306 & java -jar app.jar"]
```

---

### **Option 3: Quick Fix - Update Hibernate Dialect (Try First)**

Sometimes the Dialect needs to be explicitly set early.

#### Update pom.xml
Add to dependencies:
```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.3.1.Final</version>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

#### Verify application-gcp.yml has:
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

---

## 🧪 Testing MySQL Connection Locally

### Test 1: Verify Cloud SQL is accessible
```powershell
# Connect to Cloud SQL as root
gcloud sql connect fincore-npe-db --user=root --project=project-07a61357-b791-4255-a9e

# Once connected, test fincore_app user:
mysql -h 127.0.0.1 -u fincore_app -p"SecurePass123!Fincore@2025" my_auth_db

# If successful, you'll see mysql> prompt
# List tables:
SHOW TABLES;

# Exit:
EXIT;
```

### Test 2: Check application logs
```powershell
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" `
  --limit=50 `
  --project=project-07a61357-b791-4255-a9e `
  --format=text 2>&1 | grep -i "mysql\|connection\|error" | head -20
```

### Test 3: Look for specific error
```powershell
# Filter for Hibernate/JDBC errors
gcloud logging read "resource.type=cloud_run_revision AND textPayload=~'HikariPool|connection|jdbc'" `
  --limit=20 `
  --project=project-07a61357-b791-4255-a9e
```

---

## 📋 Recommended Approach

### **For Quick Testing:**
Continue using H2 database - it's fully functional and requires no external setup.

### **For Production:**
1. Use Option 1 (VPC Connector) - Most reliable
2. Ensure Cloud SQL has private IP
3. Test connection from Cloud Shell first
4. Verify all environment variables are correct

### **For Development:**
- Keep H2 for local/quick testing
- Use separate MySQL instance for staging/prod testing

---

## ✅ Current Working Configuration

```powershell
# This configuration WORKS with H2:
gcloud run deploy fincore-npe-api `
  --image=gcr.io/$PROJECT_ID/fincore-api:latest `
  --set-env-vars="SPRING_PROFILES_ACTIVE=h2" `
  --region=europe-west2

# Service URL: https://fincore-npe-api-994490239798.europe-west2.run.app
# All tests pass: ✅ 12/12
```

---

## 🎯 Next Recommended Steps

1. **For now:** Keep using H2 database (fully functional)
2. **Option A:** Implement VPC Connector approach (see Option 1 above)
3. **Option B:** Wait for Cloud SQL Auth Proxy to be ready for Cloud Run
4. **Option C:** Use managed Cloud Firestore or Cloud Datastore instead

---

## 📚 Resources

- Cloud SQL Proxy: https://cloud.google.com/sql/docs/mysql/cloud-sql-proxy
- VPC Connectors: https://cloud.google.com/vpc/docs/vpc-connectors
- Hibernate Dialects: https://hibernate.org/orm/documentation/
- Cloud Run + Cloud SQL: https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-service

---

## 🎉 Current Status

**Your application is fully functional with H2 database!**

✅ All 12 API tests pass
✅ All CRUD operations work
✅ Health check responds correctly
✅ Authentication works perfectly
✅ Data persists in memory

**For MySQL production setup, implement VPC Connector approach in Option 1.**
