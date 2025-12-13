# Cloud Run Deployment Checklist

## Pre-Deployment Checklist

### 1. Local Development Setup
- [ ] Java 21 JDK installed
- [ ] Maven 3.9+ installed
- [ ] Docker installed and running
- [ ] Git repository initialized and clean
- [ ] All tests passing locally (`mvn test`)
- [ ] Application builds successfully (`mvn clean package`)
- [ ] Dockerfile tested locally with `docker build -t user-management-api .`

### 2. GCP Project Setup
- [ ] GCP project created
- [ ] Billing enabled on the project
- [ ] Following APIs enabled:
  - [ ] Cloud Run API
  - [ ] Cloud SQL Admin API
  - [ ] Container Registry API
  - [ ] Cloud Build API
  - [ ] Cloud Logging API
  - [ ] Cloud Monitoring API

### 3. GCP Infrastructure Preparation
- [ ] Cloud SQL MySQL 8.0 instance created (name: `user-management-db`)
- [ ] Database created in Cloud SQL (name: `my_auth_db`)
- [ ] Database user created (username: `root`)
- [ ] Service account created: `user-management-api-sa`
- [ ] Service account roles configured:
  - [ ] Cloud SQL Client role assigned
  - [ ] Cloud Run Invoker role assigned
  - [ ] Cloud Logging Writer role assigned

### 4. Configuration & Secrets Management
- [ ] Generate strong JWT secret (if not using existing)
- [ ] Secure database password generated
- [ ] JWT_SECRET environment variable prepared
- [ ] DB_PASSWORD stored in GCP Secret Manager (optional but recommended)
- [ ] All required environment variables documented

### 5. Code Verification
- [ ] `application-gcp.yml` created and configured
- [ ] Dockerfile created and tested
- [ ] `.dockerignore` file created
- [ ] Spring Boot health endpoint configured and working
- [ ] Database connection configuration supports environment variables
- [ ] Application logs configured properly
- [ ] No hardcoded credentials in code or configs

### 6. Docker Image Preparation
- [ ] Multi-stage Dockerfile optimized
- [ ] Non-root user configured in Docker image
- [ ] Health check configured in Dockerfile
- [ ] Appropriate log levels set for Cloud Run
- [ ] Image size optimized (should be < 200MB)
- [ ] Test Docker image builds: `docker build -t user-management-api .`
- [ ] Test Docker image runs locally:
  ```bash
  docker run -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=mysql \
    -e DB_HOST=localhost \
    user-management-api
  ```

### 7. Authentication & Authorization
- [ ] gcloud CLI installed
- [ ] Authenticated to gcloud: `gcloud auth login`
- [ ] Project set: `gcloud config set project <PROJECT_ID>`
- [ ] Docker authenticated to GCR: `gcloud auth configure-docker gcr.io`
- [ ] Service account key created (if needed for CI/CD)

### 8. Testing & Validation
- [ ] Unit tests passing: `mvn test`
- [ ] Integration tests passing (if applicable): `mvn integration-test`
- [ ] Application health check working locally
- [ ] Docker image starts without errors
- [ ] All required endpoints documented and tested

### 9. Deployment Scripts
- [ ] `deploy-to-cloud-run.sh` reviewed and permissions set
- [ ] `deploy-to-cloud-run.bat` reviewed (for Windows)
- [ ] All environment variables configured before running scripts
- [ ] Deployment scripts tested in dry-run mode if possible

### 10. Documentation
- [ ] `CLOUD_RUN_DEPLOYMENT.md` reviewed
- [ ] Deployment URLs documented
- [ ] Rollback procedure understood
- [ ] Team members notified of deployment plan

## Deployment Day Checklist

### Pre-Deployment
- [ ] All code changes committed to Git
- [ ] Latest code pulled locally
- [ ] No uncommitted changes present
- [ ] Last successful test results available
- [ ] Service account verified to have correct permissions
- [ ] Cloud SQL instance status checked (running)
- [ ] Backup of database taken (if production)

### During Deployment
- [ ] Time window identified for deployment
- [ ] Deployment script executed with correct environment variables
- [ ] Build process monitored for errors
- [ ] Docker image push to GCR monitored
- [ ] Cloud Run deployment progress monitored
- [ ] Service URL captured after deployment
- [ ] No errors in deployment logs

### Post-Deployment
- [ ] Service health check passed
- [ ] Application logs checked for startup errors
- [ ] Test endpoint accessed and working: `curl $SERVICE_URL/actuator/health`
- [ ] Database connection verified in logs
- [ ] Authentication endpoints tested
- [ ] Sample API calls executed and verified
- [ ] Cloud Run metrics visible in GCP console

## Environment Variables Checklist

Before running deployment scripts, ensure these are set:

```bash
# Required variables
GCP_PROJECT_ID=your-project-id
CLOUD_RUN_SERVICE_NAME=user-management-api
CLOUD_RUN_REGION=us-central1
DB_INSTANCE=user-management-db
DB_USER=root
DB_PASSWORD=your-secure-password
DB_NAME=my_auth_db
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Verification checklist
- [ ] GCP_PROJECT_ID is set
- [ ] CLOUD_RUN_SERVICE_NAME is set
- [ ] CLOUD_RUN_REGION is valid
- [ ] DB_INSTANCE matches created instance
- [ ] DB_USER matches created user
- [ ] DB_PASSWORD is strong (min 8 chars, special chars recommended)
- [ ] DB_NAME matches created database
- [ ] JWT_SECRET is properly formatted (base64)
```

## Post-Deployment Validation

### Health & Status Checks
```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe user-management-api \
    --region us-central1 \
    --format='value(status.url)')

# Health check
curl -X GET "$SERVICE_URL/actuator/health"

# Info endpoint
curl -X GET "$SERVICE_URL/actuator/info"

# Metrics (if exposed)
curl -X GET "$SERVICE_URL/actuator/metrics"
```

### Application Functionality Tests
- [ ] Health endpoint returns 200 OK
- [ ] Login endpoint accessible
- [ ] User creation endpoint working
- [ ] Database queries executing
- [ ] Error handling working properly
- [ ] Logs appearing in Cloud Logging

### Performance & Resource Checks
- [ ] Cloud Run metrics showing normal CPU usage
- [ ] Cloud Run metrics showing normal memory usage
- [ ] Request latency acceptable
- [ ] Database connection pool healthy
- [ ] No error spikes in logs

### Security Verification
- [ ] HTTPS enforced (Cloud Run default)
- [ ] Service account permissions verified
- [ ] Database credentials not in logs
- [ ] Sensitive data not exposed in error messages
- [ ] API authentication working

## Rollback Plan

If deployment issues occur:

1. [ ] Identify the issue from Cloud Run logs
2. [ ] Document the error
3. [ ] Identify previous stable version/tag
4. [ ] Re-deploy with previous image:
   ```bash
   gcloud run deploy user-management-api \
       --image gcr.io/$GCP_PROJECT_ID/user-management-api:previous-tag \
       --region us-central1
   ```
5. [ ] Verify service health
6. [ ] Notify stakeholders
7. [ ] Investigate root cause
8. [ ] Fix issue locally
9. [ ] Re-deploy fixed version

## Post-Deployment Monitoring

- [ ] Set up Cloud Monitoring alerts for:
  - [ ] High error rates
  - [ ] High latency
  - [ ] Memory usage > 80%
  - [ ] CPU usage > 80%
- [ ] Configure Cloud Logging to retain logs for 30+ days
- [ ] Set up notification channels in GCP Console
- [ ] Schedule log review cadence

## Troubleshooting Resources

- **Cloud Run Logs**: `gcloud run logs read user-management-api --limit 50`
- **Service Details**: `gcloud run services describe user-management-api`
- **Deployment History**: `gcloud run revisions list --service=user-management-api`
- **Cloud SQL Connection**: Test from Cloud Run with proxy enabled
- **GCP Documentation**: https://cloud.google.com/run/docs

## Sign-Off

- [ ] QA Lead approved deployment
- [ ] DevOps/Infrastructure team reviewed configuration
- [ ] Product Owner aware of deployment timing
- [ ] On-call engineer briefed on rollback procedures
- [ ] Deployment successfully completed
- [ ] All stakeholders notified of live status
