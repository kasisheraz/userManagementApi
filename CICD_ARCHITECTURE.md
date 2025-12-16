# CI/CD Architecture & Workflow Documentation

## System Overview

This document describes the complete CI/CD pipeline for the User Management API project.

```
GitHub Repository
    │
    ├─ main branch
    │   ├─ Push event
    │   │   └─→ GitHub Actions: deploy-npe.yml
    │   │       ├─ Build Job
    │   │       ├─ Docker Build & Push Job
    │   │       └─ Deploy to NPE Job
    │   │
    │   └─ Pull Request
    │       └─→ GitHub Actions: test.yml
    │           └─ Test Job
    │
    ├─ Secrets (configured in GitHub)
    │   ├─ GCP_PROJECT_ID
    │   ├─ GCP_SA_KEY
    │   └─ GCP_SERVICE_ACCOUNT
    │
    └─ Workflows (.github/workflows/)
        ├─ deploy-npe.yml (Main deployment)
        └─ test.yml (PR testing)
```

## Pipeline Stages

### 1. Trigger Events

| Event | Workflow | Action |
|-------|----------|--------|
| Push to main | deploy-npe.yml | Build → Test → Docker → Deploy |
| Pull Request to main | test.yml | Build → Test → Report |
| Manual trigger | (Optional) | Can be enabled for emergency deployments |

### 2. Build Stage

**Job Name**: `build`
**Runs on**: `ubuntu-latest`

```yaml
Steps:
  1. Checkout code from repository
  2. Setup JDK 21 (Eclipse Temurin)
  3. Build with Maven (clean package)
  4. Run Maven tests
  5. Upload JAR artifact
```

**Cached Dependencies**: Maven dependencies are cached between runs for speed

**Output**: `target/user-management-api-1.0.0.jar`

### 3. Docker Build & Push Stage

**Job Name**: `docker-build-push`
**Runs on**: `ubuntu-latest`
**Needs**: `build` (waits for build to succeed)
**Condition**: Only on main branch push

```yaml
Steps:
  1. Checkout code
  2. Setup Google Cloud SDK
  3. Configure Docker for GCR
  4. Build Docker image (2 tags)
     - Latest tag: gcr.io/.../fincore-api:latest
     - SHA tag: gcr.io/.../fincore-api:abcd1234
  5. Push both tags to GCR
```

**Docker Image Details**:
- Base: `eclipse-temurin:21-jre-alpine`
- Size: ~300MB (optimized with multi-stage build)
- Includes: Cloud SQL Auth Proxy binary
- User: Non-root `appuser` (UID 1000)
- Port: 8080

### 4. Deploy to Cloud Run Stage

**Job Name**: `deploy-npe`
**Runs on**: `ubuntu-latest`
**Needs**: `docker-build-push` (waits for image push)
**Condition**: Only on main branch push
**Environment**: `npe` (with URL output)

```yaml
Steps:
  1. Checkout code
  2. Setup Google Cloud SDK with service account key
  3. Deploy to Cloud Run
     - Service: fincore-npe-api
     - Image: gcr.io/.../fincore-api:latest
     - Region: europe-west2
     - Memory: 512Mi
     - CPU: 1
     - VPC Connector: npe-connector
     - Env Profile: h2
  4. Health Check (30 attempts, 10s intervals)
     - Tests: GET /actuator/health
     - Timeout: 5 minutes
  5. Smoke Tests
     - Health endpoint
     - Login endpoint
     - Response validation
  6. Output service URL
```

**Cloud Run Configuration**:
```
Service Name: fincore-npe-api
Region: europe-west2
Platform: Managed
Authentication: Unauthenticated (public)
Memory: 512Mi
CPU: 1
Max Concurrency: 100 (default)
VPC Connector: npe-connector (READY state)
Service Account: fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com
```

### 5. Test (PR) Stage

**Job Name**: `test`
**Runs on**: `ubuntu-latest`
**Trigger**: Pull requests to main/develop

```yaml
Steps:
  1. Checkout code
  2. Setup JDK 21
  3. Build and test with Maven
  4. Upload test results as artifact
  5. Publish test report in PR
```

## Deployment Flow Diagram

```
┌─────────────────────┐
│ Git Push to main    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ GitHub Actions      │
│ Trigger Event       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────┐
│ Build Stage             │
│ - Maven build           │
│ - Run tests             │
│ - Upload artifact       │
│ Duration: ~3-5 min      │
└──────────┬──────────────┘
           │
           ▼ (if successful)
┌──────────────────────────┐
│ Docker Build & Push      │
│ - Build image            │
│ - Tag (latest, SHA)      │
│ - Push to GCR            │
│ Duration: ~2-3 min       │
└──────────┬───────────────┘
           │
           ▼ (if successful)
┌──────────────────────────┐
│ Deploy to Cloud Run      │
│ - Deploy service         │
│ - Health check (30x)     │
│ - Smoke tests            │
│ Duration: ~3-5 min       │
└──────────┬───────────────┘
           │
           ▼ (if all pass)
┌──────────────────────────┐
│ ✅ Deployment Success    │
│ Service is live at:      │
│ https://fincore-npe-api- │
│ 994490239798.region.     │
│ run.app                  │
└──────────────────────────┘
```

**Total Duration**: 8-13 minutes from push to live deployment

## Environment Configuration

### GitHub Secrets

```
GCP_PROJECT_ID
├─ Value: project-07a61357-b791-4255-a9e
├─ Used by: gcloud commands for project scope
└─ Type: Text

GCP_SA_KEY
├─ Value: (JSON key file contents)
├─ Used by: google-github-actions/setup-gcloud
└─ Type: JSON (plain text in secret)

GCP_SERVICE_ACCOUNT
├─ Value: fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com
├─ Used by: Cloud Run deployment
└─ Type: Text
```

### Environment Variables (in Workflow)

```yaml
PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
REGION: europe-west2
SERVICE_NAME: fincore-npe-api
IMAGE_NAME: fincore-api
VPC_CONNECTOR: npe-connector
```

### Cloud Run Environment Variables

```
SPRING_PROFILES_ACTIVE=h2
```

### Cloud Run Secrets

```
JWT_SECRET (from Secret Manager: jwt-secret:latest)
```

## Security Architecture

### Access Control

```
GitHub Actions Runner
    │
    ├─ GCP_SA_KEY (service account JSON)
    │   └─ Authenticates to Google Cloud
    │
    └─ google-github-actions/setup-gcloud
        └─ Configures gcloud CLI
            ├─ Pulls docker image credentials
            ├─ Authenticates to GCR
            ├─ Authenticates to Cloud Run
            └─ Authenticates to Secret Manager
```

### Service Account Permissions

The `github-actions` service account has these roles:

```
roles/run.admin
├─ Deploy Cloud Run services
├─ Update service configurations
└─ View Cloud Run logs

roles/storage.admin
├─ Read/write to GCR buckets
└─ Manage container images

roles/cloudsql.client
├─ Connect to Cloud SQL instances
└─ Access database resources

roles/iam.serviceAccountUser
├─ Use service accounts
└─ Pass service accounts to Cloud Run

roles/editor
├─ General resource management
└─ Necessary for GCR operations
```

### Secret Management

```
GitHub Secrets (encrypted at rest)
    │
    ├─ GCP_SA_KEY
    │   └─ Used only to authenticate during workflow
    │   └─ Never stored in Cloud Run environment
    │
└─ GCP Secret Manager (Google Cloud side)
    ├─ JWT_SECRET: Used by application
    ├─ fincore-npe-db-password: (if using MySQL)
    └─ Accessed at runtime by Cloud Run service
```

## Continuous Integration

### Code Changes Flow

```
Developer
    │
    ├─ Create branch
    │   └─ Make changes
    │       └─ Commit locally
    │
    ├─ Push to GitHub
    │   └─ Create Pull Request
    │       └─ GitHub Actions: test.yml triggers
    │           ├─ Build project
    │           ├─ Run tests
    │           └─ Report results in PR
    │
    ├─ Code Review
    │   └─ Check test results
    │       └─ Approve and Merge
    │
    └─ Merge to main
        └─ GitHub Actions: deploy-npe.yml triggers
            ├─ Build
            ├─ Docker build & push
            ├─ Deploy to Cloud Run
            ├─ Health checks
            └─ Smoke tests
                └─ ✅ Live on NPE
```

## Monitoring & Observability

### GitHub Actions Dashboard

- **URL**: https://github.com/kasisheraz/userManagementApi/actions
- **Information**: Workflow runs, status, duration, logs
- **Retention**: 90 days default

### Cloud Run Dashboard

- **URL**: https://console.cloud.google.com/run
- **Metrics**: Requests/sec, latency, error rate
- **Logs**: Cloud Logging integration

### Deployment Logs

```bash
# View latest 50 logs from Cloud Run
gcloud logging read "resource.type=cloud_run_revision AND \
  resource.labels.service_name=fincore-npe-api" \
  --project=project-07a61357-b791-4255-a9e \
  --limit=50 \
  --format=json

# View logs from specific revision
gcloud logging read "resource.labels.revision_name=fincore-npe-api-00023-r84" \
  --project=project-07a61357-b791-4255-a9e
```

### Health Monitoring

```bash
# Check service status
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e

# View recent revisions
gcloud run revisions list \
  --service=fincore-npe-api \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e
```

## Error Handling

### Build Failures

**Issue**: Maven build fails
**Detection**: In Build job, `mvn clean package` returns non-zero exit code
**Notification**: GitHub Actions marks workflow as failed, PR shows failed status
**Resolution**: Fix code, push new commit, workflow retries automatically

### Test Failures

**Issue**: Unit tests fail
**Detection**: In Build job, `mvn test` returns non-zero exit code
**Notification**: GitHub Actions shows failed test results in PR
**Resolution**: Fix code, commit, push; workflow retries

### Docker Build Failures

**Issue**: Docker image fails to build
**Detection**: In Docker Build & Push job, `docker build` fails
**Notification**: Workflow fails, no image pushed to GCR
**Resolution**: Fix Dockerfile, commit, push; workflow retries

### Cloud Run Deployment Failures

**Issue**: Service fails to start on Cloud Run
**Detection**: Health check times out after 5 minutes
**Notification**: Workflow fails with health check error
**Resolution**: 
- Check Cloud Run logs for startup errors
- Fix application code
- Commit, push; workflow retries
- Previous revision remains active until new one succeeds

### Smoke Test Failures

**Issue**: Service starts but endpoints fail
**Detection**: Smoke tests fail (health check or login endpoint)
**Notification**: Workflow fails after deployment
**Resolution**: Check application logs, fix issue, retry

## Rollback Procedures

### Automatic Rollback (on deployment failure)

```
New deployment fails
    │
    └─ GitHub Actions stops
        └─ Cloud Run previous revision
            continues serving traffic
```

**Note**: Cloud Run automatically keeps previous working revisions

### Manual Rollback

```bash
# List revisions
gcloud run revisions list \
  --service=fincore-npe-api \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e

# Route traffic to previous revision
gcloud run services update-traffic fincore-npe-api \
  --to-revisions=fincore-npe-api-00022-XXXX=100 \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e
```

## Performance Optimization

### Build Caching

- Maven dependencies cached between runs (saves ~30-60 seconds)
- Docker layers cached (speeds up rebuilds)
- GitHub Actions cache uses 5GB limit

### Parallel Jobs

```
build (3-5 min)
    │
    └─ docker-build-push (2-3 min) [parallel if multiple runners]
        │
        └─ deploy-npe (3-5 min)

Total time: 8-13 minutes (sequential dependency)
```

### Optimization Opportunities

1. **Build optimization**: Consider gradle build cache
2. **Docker layer optimization**: More granular layers for caching
3. **Parallel smoke tests**: Run multiple endpoint tests simultaneously
4. **Region selection**: Edge locations for faster GCR access

## Disaster Recovery

### Data Loss Scenarios

#### H2 Database (current)
- **Data stored in**: Memory
- **Persistence**: Lost on restart
- **Recovery**: Data re-initialized from schema on startup
- **Backup**: None (development database)

#### MySQL Database (optional)
- **Data stored in**: Cloud SQL managed service
- **Persistence**: Persistent across restarts
- **Recovery**: Cloud SQL backups available
- **Backup**: Automated daily backups

### Service Failure Recovery

```
Service Crash
    │
    ├─ Cloud Run auto-restarts failed instances
    │   └─ Should recover within 30-60 seconds
    │
    ├─ If startup fails repeatedly
    │   └─ Manual intervention required
    │       ├─ Check logs
    │       ├─ Fix issue
    │       └─ New deployment triggers (git push)
    │
    └─ If immediate workaround needed
        └─ Route to previous working revision
```

## Migration Paths

### Add New Deployment Environment

To deploy to QA or production:

1. Create new workflow file (e.g., `deploy-qa.yml`)
2. Copy `deploy-npe.yml` structure
3. Modify:
   - `SERVICE_NAME`: `fincore-qa-api`
   - `VPC_CONNECTOR`: appropriate QA connector
   - Trigger branch: `release-qa` or similar
4. Commit and push

### Switch to MySQL

1. Update `.github/workflows/deploy-npe.yml`
2. Change `SPRING_PROFILES_ACTIVE` from `h2` to `mysql`
3. Add `DB_*` environment variables
4. Add `DB_PASSWORD` secret mapping
5. Commit and push (workflow handles MySQL deployment)

### Add Slack Notifications

Add to workflow:
```yaml
- name: Notify Slack
  if: always()
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
```

## Best Practices

### ✅ DO

- Keep main branch deployable (tests must pass)
- Use descriptive commit messages
- Review code before merging
- Monitor Cloud Run metrics
- Rotate secrets periodically
- Test locally before pushing

### ❌ DON'T

- Commit secrets or credentials
- Push directly to main (use PRs)
- Disable health checks
- Use default Cloud Run configurations
- Ignore workflow failures
- Store sensitive data in environment variables (use Secret Manager)

## Related Documentation

- [GitHub Actions Setup Guide](GITHUB_ACTIONS_SETUP.md)
- [GitHub Actions Quick Start](GITHUB_ACTIONS_QUICKSTART.md)
- [Cloud Run Deployment Manual](CLOUD_RUN_DEPLOYMENT_MANUAL.md)
- [MySQL Migration Status](MYSQL_MIGRATION_STATUS.md)

---

**Last Updated**: 2025-12-16
**Version**: 1.0
**Status**: Production Ready
