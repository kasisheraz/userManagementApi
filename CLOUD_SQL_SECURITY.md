# Cloud SQL Security Configuration Guide

## 🚨 **CRITICAL: Remove Public IP Access**

Your Cloud SQL instance currently has `0.0.0.0/0` in authorized networks, which allows access from **any IP address on the internet**. This is a severe security vulnerability.

---

## ✅ **Step 1: Remove Public IP Access**

### Option A: Remove 0.0.0.0/0 (Recommended)

1. Go to [Cloud SQL Console](https://console.cloud.google.com/sql/instances/fincore-npe-db/connections/networking?project=project-07a61357-b791-4255-a9e)
2. Click on **fincore-npe-db** instance
3. Click **CONNECTIONS** tab
4. Scroll to **Public IP** section
5. Find the authorized network with `0.0.0.0/0`
6. Click the **🗑️ DELETE** icon next to it
7. Click **SAVE**

### Option B: Disable Public IP Completely (Most Secure)

1. In **CONNECTIONS** tab
2. Under **Public IP** section
3. **Uncheck** "Enable public IP"
4. Click **SAVE**

**Result:** Database is only accessible via Cloud SQL Proxy (private connection)

---

## 🔌 **Step 2: Verify Cloud Run Uses Private Connection**

✅ Your Cloud Run service is **already configured correctly**:

```yaml
# In deploy-npe.yml
--add-cloudsql-instances=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db
```

This uses **Cloud SQL Proxy** (private IP) - no public IP needed!

**Test after removing public access:**
```bash
# Run your smoke tests
curl https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
```

Should still return: `{"status":"UP"}`

---

## 💻 **Step 3: Connect from Local Machine (After Removing Public IP)**

### Install Cloud SQL Proxy

**Windows (PowerShell):**
```powershell
# Download Cloud SQL Proxy
Invoke-WebRequest -Uri "https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.2/cloud-sql-proxy.x64.exe" -OutFile "cloud-sql-proxy.exe"

# Move to a directory in PATH (optional)
Move-Item cloud-sql-proxy.exe C:\Windows\System32\
```

**Mac/Linux:**
```bash
curl -o cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.2/cloud-sql-proxy.darwin.amd64
chmod +x cloud-sql-proxy
sudo mv cloud-sql-proxy /usr/local/bin/
```

### Authenticate with GCP

```bash
# Login to GCP
gcloud auth application-default login

# Set project
gcloud config set project project-07a61357-b791-4255-a9e
```

### Start Cloud SQL Proxy

**PowerShell:**
```powershell
# Start proxy in background
Start-Process -NoNewWindow cloud-sql-proxy `
  "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"

# Or with custom port
cloud-sql-proxy `
  --port=3307 `
  project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db
```

**Bash:**
```bash
# Start proxy in background
cloud-sql-proxy \
  project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db &

# Or with custom port
cloud-sql-proxy \
  --port=3307 \
  project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db
```

### Connect to Database via Proxy

The proxy creates a local connection on `127.0.0.1:3306` (or your custom port)

**Using MySQL Client:**
```bash
mysql -h 127.0.0.1 -u fincore_app -p fincore_db
# Enter password from Secret Manager
```

**Using DBeaver/MySQL Workbench:**
- Host: `127.0.0.1`
- Port: `3306` (or 3307 if you used `--port=3307`)
- Database: `fincore_db`
- Username: `fincore_app`
- Password: (from Secret Manager: `fincore-npe-app-password`)

### Update application-local.yml

```yaml
spring:
  datasource:
    # When Cloud SQL Proxy is running locally
    url: jdbc:mysql://127.0.0.1:3306/fincore_db?useSSL=false
    username: fincore_app
    password: ${DB_PASSWORD}
```

---

## 🔐 **Step 4: Additional Security Best Practices**

### 1. Enable SSL/TLS Connections

In Cloud SQL Console:
1. Go to **CONNECTIONS** → **Security**
2. Enable **Require SSL**
3. Download client certificates if needed

### 2. Enable Binary Logging (for backups)

```bash
gcloud sql instances patch fincore-npe-db \
  --backup-start-time=02:00 \
  --enable-bin-log \
  --project=project-07a61357-b791-4255-a9e
```

### 3. Set Strong Password Policy

```sql
-- Connect as root and set password policy
ALTER USER 'fincore_app'@'%' 
  IDENTIFIED BY 'strong-password-here' 
  PASSWORD EXPIRE INTERVAL 90 DAY;
```

### 4. Enable Audit Logging

In Cloud SQL Console:
1. Go to **Edit** → **Flags**
2. Add flag: `general_log` = `on`
3. Add flag: `slow_query_log` = `on`

### 5. Limit Database User Permissions

```sql
-- Connect as root
mysql -u root -p

-- Revoke all privileges
REVOKE ALL PRIVILEGES ON *.* FROM 'fincore_app'@'%';

-- Grant only necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON fincore_db.* TO 'fincore_app'@'%';

-- Remove CREATE/DROP permissions for production
FLUSH PRIVILEGES;
```

---

## 🧪 **Step 5: Test Security Configuration**

### Test 1: Verify Public IP is Disabled

Try connecting from your local machine WITHOUT proxy:
```bash
mysql -h <CLOUD_SQL_PUBLIC_IP> -u fincore_app -p
```

**Expected result:** Connection timeout or refused (good!)

### Test 2: Verify Cloud SQL Proxy Works

Start proxy and connect:
```bash
cloud-sql-proxy project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db
mysql -h 127.0.0.1 -u fincore_app -p fincore_db
```

**Expected result:** Connection successful ✅

### Test 3: Verify Cloud Run Still Works

```bash
curl https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
```

**Expected result:** `{"status":"UP"}` ✅

---

## 📋 **Quick Reference Commands**

### Get Cloud SQL Public IP
```bash
gcloud sql instances describe fincore-npe-db \
  --project=project-07a61357-b791-4255-a9e \
  --format="value(ipAddresses[0].ipAddress)"
```

### Get Database Password from Secret Manager
```bash
gcloud secrets versions access latest \
  --secret=fincore-npe-app-password \
  --project=project-07a61357-b791-4255-a9e
```

### Check Active Connections
```sql
SHOW PROCESSLIST;
```

### View Connection Source
```sql
SELECT 
    USER, 
    HOST, 
    DB, 
    COMMAND, 
    TIME, 
    STATE 
FROM information_schema.PROCESSLIST;
```

---

## 🚀 **Automated Local Development Script**

Create `start-local-with-proxy.ps1`:

```powershell
# Start Cloud SQL Proxy
Write-Host "Starting Cloud SQL Proxy..." -ForegroundColor Cyan
Start-Process -NoNewWindow cloud-sql-proxy `
  "project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db"

Write-Host "Waiting for proxy to start..." -ForegroundColor Yellow
Start-Sleep 3

# Get database password
Write-Host "Getting database password..." -ForegroundColor Cyan
$DB_PASSWORD = gcloud secrets versions access latest `
  --secret=fincore-npe-app-password `
  --project=project-07a61357-b791-4255-a9e

# Set environment variables
$env:DB_PASSWORD = $DB_PASSWORD
$env:SPRING_PROFILES_ACTIVE = "local"

# Start Spring Boot application
Write-Host "Starting application..." -ForegroundColor Green
mvn spring-boot:run
```

---

## ⚠️ **What Happens After Removing Public IP**

### ✅ Will Still Work:
- Cloud Run connections (uses private Cloud SQL Proxy)
- Cloud Functions connections (if configured with proxy)
- App Engine connections (uses Cloud SQL Proxy)
- Local development via Cloud SQL Proxy

### ❌ Will Stop Working:
- Direct MySQL connections from local machine
- Third-party tools without Cloud SQL Proxy
- Legacy applications using public IP

**Solution:** Use Cloud SQL Proxy for all connections!

---

## 📞 **Troubleshooting**

### Error: "Connection timed out"
**Cause:** Public IP disabled, trying to connect directly  
**Solution:** Use Cloud SQL Proxy

### Error: "Access denied for user"
**Cause:** Wrong password or user doesn't exist  
**Solution:** Check Secret Manager for correct password

### Error: "Could not connect to CloudSQL instance"
**Cause:** Cloud SQL Proxy not running  
**Solution:** Start Cloud SQL Proxy first

### Error: "Your client does not support authentication"
**Cause:** Old MySQL client version  
**Solution:** Upgrade MySQL client to 8.0+

---

## 🎓 **Summary**

**Before (INSECURE):**
```
Internet → 0.0.0.0/0 → Cloud SQL ❌
Anyone can try to connect!
```

**After (SECURE):**
```
Cloud Run → Private Cloud SQL Proxy → Cloud SQL ✅
Local Dev → Cloud SQL Proxy → Cloud SQL ✅
Direct Internet Access → BLOCKED ❌
```

**Next Steps:**
1. ✅ Remove `0.0.0.0/0` from authorized networks NOW
2. ✅ Install Cloud SQL Proxy for local development
3. ✅ Test Cloud Run still works
4. ✅ Test local development with proxy

Your data will be much more secure! 🔒
