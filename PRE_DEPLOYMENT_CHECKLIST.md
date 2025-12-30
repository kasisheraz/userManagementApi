# ✅ Pre-Deployment Checklist

## 📋 Before You Deploy

Use this checklist to ensure everything is ready for deployment to GCP.

### ✅ Prerequisites

- [ ] **Google Cloud Account**
  - [ ] GCP account created and billing enabled
  - [ ] GCP Project created (note the Project ID)
  
- [ ] **Local Tools Installed**
  - [ ] Java 21 installed (`java -version`)
  - [ ] Maven 3.9+ installed (`mvn -version`)
  - [ ] Google Cloud SDK installed (`gcloud version`)
  - [ ] Docker installed (optional, `docker --version`)
  - [ ] Git installed and configured

- [ ] **Code Ready**
  - [ ] Latest code pulled from repository
  - [ ] All changes committed (check with `git status`)
  - [ ] Database name updated to `fincore_db` ✅ (Already done!)

---

## 🔧 Configuration Checklist

### Database Configuration
- [ ] Database name is `fincore_db` in all config files ✅
  - [x] [app.yaml](app.yaml#L6)
  - [x] [service.yaml](service.yaml#L28-L29)
  - [x] [application-npe.yml](src/main/resources/application-npe.yml#L10)
  - [x] [application-production.yml](src/main/resources/application-production.yml#L10)
  - [x] [setup-gcp-infrastructure.sh](setup-gcp-infrastructure.sh#L19)
  - [x] [GitHub Actions workflow](.github/workflows/deploy-npe.yml#L138)

### GCP Project Setup
- [ ] GCP Project ID ready: `_____________________`
- [ ] Preferred region selected (default: `europe-west2`)
- [ ] Service account name decided (default: `fincore-npe-cloudrun`)

### Database Credentials
- [ ] Database username chosen (default: `fincore_app`)
- [ ] Strong database password created (min 12 characters, mixed case, numbers, symbols)
- [ ] Database instance name decided (default: `fincore-npe-db`)

### GitHub Secrets (if using GitHub Actions)
- [ ] Repository settings → Secrets configured:
  - [ ] `GCP_PROJECT_ID`
  - [ ] `GCP_SA_KEY` (service account JSON)
  - [ ] `DB_USER`
  - [ ] `DB_PASSWORD`
  - [ ] `CLOUDSQL_INSTANCE` (format: `project:region:instance`)
  - [ ] `GCP_SERVICE_ACCOUNT` (email format)

---

## 🚀 Deployment Method Selection

Choose ONE deployment method:

### Option A: Automated Deployment (Recommended)
- [ ] GitHub repository is set up
- [ ] GitHub Secrets are configured
- [ ] `.github/workflows/deploy-npe.yml` exists ✅
- [ ] Ready to push to `main` branch

**Deployment Command:**
```bash
git add .
git commit -m "Deploy with fincore_db"
git push origin main
```

### Option B: Manual Deployment
- [ ] `deploy-to-gcp.ps1` script exists ✅ (Windows)
- [ ] `setup-gcp-infrastructure.sh` script exists ✅ (Linux/Mac)
- [ ] GCP CLI authenticated (`gcloud auth login`)
- [ ] Project set in gcloud config

**Deployment Command (Windows):**
```powershell
.\deploy-to-gcp.ps1 -ProjectId "YOUR_PROJECT_ID" -Region "europe-west2"
```

**Deployment Command (Linux/Mac):**
```bash
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID europe-west2
```

---

## 🔍 Pre-Deployment Tests

### Local Build Test
```bash
# Test that the application builds
mvn clean package -DskipTests
```
- [ ] Build completes successfully
- [ ] JAR file created at `target/user-management-api-1.0.0.jar`

### Local Run Test (Optional)
```bash
# Run with H2 in-memory database
mvn spring-boot:run
```
- [ ] Application starts without errors
- [ ] Health endpoint responds: http://localhost:8080/actuator/health
- [ ] Login endpoint works: http://localhost:8080/api/auth/login

### Docker Build Test (Optional)
```bash
# Build Docker image locally
docker build -t fincore-api:test .

# Run container locally
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=local-h2 fincore-api:test
```
- [ ] Docker image builds successfully
- [ ] Container runs without errors
- [ ] Can access application on http://localhost:8080

---

## 🎯 GCP Infrastructure Checklist

### Before First Deployment
- [ ] Run infrastructure setup script OR manually create:
  - [ ] Cloud SQL MySQL 8.0 instance
  - [ ] Database `fincore_db` created
  - [ ] Database user `fincore_app` created with password
  - [ ] Service account created with IAM roles:
    - [ ] `roles/cloudsql.client`
    - [ ] `roles/run.invoker`
    - [ ] `roles/logging.logWriter`
  - [ ] Required APIs enabled:
    - [ ] Cloud Run API
    - [ ] Cloud SQL Admin API
    - [ ] Container Registry API
    - [ ] Cloud Build API

### Verify Infrastructure
```bash
# Check Cloud SQL instance
gcloud sql instances list

# Check service accounts
gcloud iam service-accounts list

# Check enabled APIs
gcloud services list --enabled
```

---

## 📊 Deployment Verification

### After Deployment
- [ ] Deployment completes without errors
- [ ] Service URL obtained
- [ ] Health check endpoint responds
- [ ] Login endpoint works
- [ ] Database connection successful
- [ ] No errors in Cloud Run logs

### Test Commands
```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --format='value(status.url)')

# Test health
curl $SERVICE_URL/actuator/health

# Test login
curl -X POST $SERVICE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'
```

Expected Results:
- [ ] Health endpoint returns `{"status":"UP"}`
- [ ] Login endpoint returns JWT token and OTP in logs

---

## 🗄️ Database Initialization

### First-Time Setup
- [ ] Connect to Cloud SQL database
- [ ] Run schema creation script
- [ ] Verify tables created
- [ ] Check default data loaded

```bash
# Connect to database
gcloud sql connect fincore-npe-db --user=fincore_app --database=fincore_db

# Run schema (in MySQL prompt)
mysql> source cloud-sql-schema.sql;
mysql> SHOW TABLES;
mysql> SELECT COUNT(*) FROM users;
mysql> exit;
```

Expected Tables:
- [ ] `users`
- [ ] `roles`
- [ ] `permissions`
- [ ] `role_permissions`
- [ ] `otp_codes`

---

## 🔒 Security Checklist

### Credentials Security
- [ ] Database password is strong (12+ characters)
- [ ] Database password not committed to Git
- [ ] JWT secret is secure and not default
- [ ] Service account key JSON file secured (not in Git)
- [ ] GitHub Secrets are encrypted

### GCP Security
- [ ] Service account has minimal required permissions
- [ ] Cloud SQL instance has authorized networks configured (if needed)
- [ ] Cloud SQL automatic backups enabled
- [ ] HTTPS enforced (Cloud Run default)

### Application Security
- [ ] Default admin password will be changed after first login
- [ ] Rate limiting configured (if needed)
- [ ] CORS configured properly

---

## 📱 Monitoring Setup

### After Deployment
- [ ] Cloud Logging enabled
- [ ] Cloud Monitoring dashboard created
- [ ] Alerts configured (optional):
  - [ ] High error rate
  - [ ] High latency
  - [ ] Database connection failures
  - [ ] CPU/Memory threshold

### Access Monitoring
- Cloud Run Dashboard: https://console.cloud.google.com/run
- Cloud SQL Dashboard: https://console.cloud.google.com/sql
- Logs: https://console.cloud.google.com/logs

---

## 🎉 Post-Deployment Tasks

### Immediate
- [ ] Test all API endpoints
- [ ] Verify database connectivity
- [ ] Check logs for errors
- [ ] Test authentication flow
- [ ] Verify health checks

### Within 24 Hours
- [ ] Change default admin password
- [ ] Configure custom domain (if needed)
- [ ] Set up monitoring alerts
- [ ] Document service URL
- [ ] Share API documentation with team

### Within 1 Week
- [ ] Configure automatic backups schedule
- [ ] Set up staging environment (if needed)
- [ ] Configure CI/CD pipeline for automated deployments
- [ ] Load testing (if needed)
- [ ] Security audit

---

## 📞 Quick Reference

### Important URLs
- **Deployment Guide**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- **Quick Commands**: [QUICK_DEPLOY_COMMANDS.md](QUICK_DEPLOY_COMMANDS.md)
- **README**: [README.md](README.md)

### Key Configuration Values
- **Database Name**: `fincore_db` ✅
- **Database User**: `fincore_app`
- **Default Region**: `europe-west2`
- **Service Name**: `fincore-npe-api`
- **Spring Profile**: `npe`

### Support Resources
- GCP Documentation: https://cloud.google.com/docs
- Spring Boot on Cloud Run: https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service
- Cloud SQL MySQL: https://cloud.google.com/sql/docs/mysql

---

## ✅ Final Check

Before you deploy, confirm:
- [ ] All items in this checklist are reviewed
- [ ] GCP project and billing are set up
- [ ] Database name is `fincore_db` in all configs ✅
- [ ] Credentials are secure and not in Git
- [ ] Deployment method is chosen
- [ ] Infrastructure is ready (if manual deployment)
- [ ] GitHub Secrets configured (if automated deployment)
- [ ] Local build test passed

---

## 🚀 Ready to Deploy!

You're all set! Choose your deployment method:

### Automated (GitHub Actions)
```bash
git push origin main
```

### Manual (Windows)
```powershell
.\deploy-to-gcp.ps1 -ProjectId "YOUR_PROJECT_ID"
```

### Manual (Linux/Mac)
```bash
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID europe-west2
```

---

**Good luck with your deployment!** 🎉

If you encounter any issues:
1. Check the [Troubleshooting section](DEPLOYMENT_GUIDE.md#-troubleshooting) in DEPLOYMENT_GUIDE.md
2. Review Cloud Run logs: `gcloud run services logs read fincore-npe-api`
3. Verify database connection: `gcloud sql instances describe YOUR_INSTANCE`

**Last Updated**: December 30, 2025  
**Database**: fincore_db ✅  
**Status**: Ready for Deployment 🚀
