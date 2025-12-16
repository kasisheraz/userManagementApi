# GitHub Actions CI/CD Setup Guide

## Overview

This project uses GitHub Actions for continuous integration and deployment to Google Cloud Run's NPE (Non-Production Environment).

### Workflows

1. **deploy-npe.yml** - Triggers on push to main branch
   - Builds Maven project
   - Runs tests
   - Builds Docker image
   - Pushes to Google Container Registry (GCR)
   - Deploys to Cloud Run
   - Runs smoke tests

2. **test.yml** - Triggers on pull requests
   - Builds Maven project
   - Runs unit tests
   - Publishes test results

## Setup Instructions

### Step 1: Create GCP Service Account

The GitHub Actions workflow needs a service account to authenticate with GCP. Create one with the following roles:

```bash
# Set project variables
PROJECT_ID="project-07a61357-b791-4255-a9e"
SERVICE_ACCOUNT_NAME="github-actions"

# Create service account
gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
  --display-name="GitHub Actions CI/CD" \
  --project=$PROJECT_ID

# Grant necessary roles
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/run.admin

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/storage.admin

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/cloudsql.client

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/iam.serviceAccountUser

# Required for GCR access
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com \
  --role=roles/editor
```

### Step 2: Create Service Account Key

```bash
PROJECT_ID="project-07a61357-b791-4255-a9e"
SERVICE_ACCOUNT_NAME="github-actions"

# Create JSON key
gcloud iam service-accounts keys create github-actions-key.json \
  --iam-account=$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com
```

**Important**: Keep `github-actions-key.json` secure. You'll use its contents in the next step.

### Step 3: Configure GitHub Secrets

Add the following secrets to your GitHub repository:

1. Navigate to: **Settings → Secrets and variables → Actions**

2. Add these secrets:

| Secret Name | Value |
|------------|-------|
| `GCP_PROJECT_ID` | `project-07a61357-b791-4255-a9e` |
| `GCP_SA_KEY` | Contents of `github-actions-key.json` (the entire JSON file as a string) |
| `GCP_SERVICE_ACCOUNT` | `fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com` |

### Step 4: Create GitHub Environment (Optional but Recommended)

To add approval gates before deployment to NPE:

1. Go to **Settings → Environments**
2. Click **New environment** and name it `npe`
3. Add **Required reviewers** if desired
4. Configure deployment branches: `main`

## Workflow Triggers

### Deploy to NPE (`deploy-npe.yml`)

**Triggers on:**
- Push to `main` branch

**Steps:**
1. ✅ Checkout code
2. ✅ Set up JDK 21
3. ✅ Build with Maven
4. ✅ Run unit tests
5. ✅ Build Docker image (tagged with commit SHA and 'latest')
6. ✅ Push to GCR
7. ✅ Deploy to Cloud Run (NPE service)
8. ✅ Health check (waits up to 5 minutes)
9. ✅ Smoke tests (health endpoint + login endpoint)

**Example workflow run:**
```
main branch push detected
    ↓
Build job starts
    ↓
Maven clean package → Tests pass
    ↓
Docker build-push job starts (needs: build)
    ↓
Docker image built and pushed to GCR
    ↓
Deploy-npe job starts (needs: docker-build-push)
    ↓
Cloud Run deployment initiated
    ↓
Health check runs (30 attempts, 10 seconds apart)
    ↓
Smoke tests validate endpoints
    ↓
Deployment complete ✅
```

### Test on PR (`test.yml`)

**Triggers on:**
- Pull requests to `main` or `develop` branches

**Steps:**
1. ✅ Checkout code
2. ✅ Set up JDK 21
3. ✅ Build with Maven
4. ✅ Run tests
5. ✅ Upload test results
6. ✅ Publish test report in PR

## Environment Variables in Workflow

These are set in the workflow file:

```yaml
PROJECT_ID: project-07a61357-b791-4255-a9e
REGION: europe-west2
SERVICE_NAME: fincore-npe-api
IMAGE_NAME: fincore-api
VPC_CONNECTOR: npe-connector
```

## Cloud Run Deployment Configuration

The workflow deploys to Cloud Run with these settings:

```
Service: fincore-npe-api
Region: europe-west2
Platform: managed
Authentication: unauthenticated (public API)
Memory: 512Mi
CPU: 1
VPC Connector: npe-connector
Spring Profile: h2
Environment Secrets:
  - JWT_SECRET=jwt-secret:latest
Service Account: fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com
```

## Deployment Endpoints

After successful deployment, the service is available at:

```
https://fincore-npe-api-994490239798.europe-west2.run.app
```

### Available Endpoints

- `GET /actuator/health` - Health check
- `POST /api/auth/login` - Authentication
- `POST /api/auth/register` - User registration
- `GET /api/users` - List users (requires JWT)
- `GET /api/users/{id}` - Get user by ID (requires JWT)
- `PUT /api/users/{id}` - Update user (requires JWT)
- `DELETE /api/users/{id}` - Delete user (requires JWT)

## Monitoring Deployments

### View Workflow Runs

1. Go to **Actions** tab in your GitHub repository
2. Click on the workflow run to see details
3. View logs for each job

### View Cloud Run Logs

```bash
# Get logs for the service
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" \
  --project=project-07a61357-b791-4255-a9e \
  --limit=50 \
  --format=json | jq .

# Or view in Cloud Console
# https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs
```

### Check Deployment Status

```bash
gcloud run services describe fincore-npe-api \
  --region=europe-west2 \
  --project=project-07a61357-b791-4255-a9e
```

## Troubleshooting

### Build Fails

**Problem**: Maven build fails
**Solution**: 
- Check test output in GitHub Actions logs
- Run locally: `mvn clean package`
- Ensure Java 21 is configured

### Docker Push Fails

**Problem**: "Failed to push to GCR"
**Solution**:
- Verify `GCP_SA_KEY` secret is set correctly
- Check service account has `roles/editor` role
- Ensure GCR API is enabled: `gcloud services enable containerregistry.googleapis.com`

### Cloud Run Deployment Fails

**Problem**: "Service failed to start"
**Solution**:
- Check Cloud Run logs in console
- Verify environment variables are correct
- Ensure Spring profile `h2` is valid
- Check VPC Connector `npe-connector` is in READY state

### Health Check Times Out

**Problem**: "Service health check failed after 5 minutes"
**Solution**:
- Service startup is slow (> 5 minutes)
- Check Cloud Run logs for errors
- Increase health check timeout in workflow
- Check if database connection is blocking startup

## Advanced Configuration

### Switch to MySQL Profile

To deploy with MySQL instead of H2, modify the workflow:

```yaml
# In deploy-npe job, under "Deploy to Cloud Run" step
--set-env-vars="SPRING_PROFILES_ACTIVE=mysql,DB_HOST=127.0.0.1,DB_PORT=3306,DB_NAME=my_auth_db,DB_USER=fincore_app,CLOUDSQL_INSTANCE=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db" \
--set-secrets="DB_PASSWORD=fincore-npe-db-password:latest,JWT_SECRET=jwt-secret:latest"
```

### Add Slack Notifications

Add to `deploy-npe.yml`:

```yaml
- name: Notify Slack on Success
  if: success()
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "✅ Deployment to NPE successful",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*Deployment Successful* 🚀\nService: fincore-npe-api\nRef: ${{ github.ref }}\nCommit: ${{ github.sha }}"
            }
          }
        ]
      }
```

### Add Email Notifications

GitHub Actions provides built-in notifications. Configure in:
**Settings → Notifications**

## Best Practices

1. **Always test locally first**
   ```bash
   mvn clean package
   docker build -t fincore-api:test .
   docker run -p 8080:8080 fincore-api:test
   ```

2. **Keep secrets secure**
   - Never commit credentials
   - Rotate service account keys regularly
   - Use GitHub's encrypted secrets feature

3. **Use meaningful commit messages**
   - Helps with debugging and tracking changes
   - Appears in Cloud Run revision description

4. **Monitor deployments**
   - Check GitHub Actions logs
   - Monitor Cloud Run service metrics
   - Set up alerts for failures

5. **Test database migrations**
   - If using MySQL, test schema changes locally first
   - Use liquibase or flyway for versioning

## Security Considerations

1. **Service Account Permissions**
   - Give minimal required permissions
   - Regularly audit IAM bindings
   - Rotate keys every 90 days

2. **Secret Management**
   - Rotate `JWT_SECRET` periodically
   - Update database password regularly
   - Use Secret Manager for all sensitive data

3. **Container Registry**
   - Only push verified images
   - Use SHA tags for production
   - Clean up old images: `gcloud container images delete --quiet`

4. **Network Security**
   - Use VPC Connectors for database access
   - Enable VPC Flow Logs for monitoring
   - Restrict Cloud Run ingress if needed

## Related Files

- `.github/workflows/deploy-npe.yml` - Main deployment workflow
- `.github/workflows/test.yml` - PR test workflow
- `Dockerfile` - Container image definition
- `pom.xml` - Maven build configuration
- `src/main/resources/application-*.yml` - Spring profiles

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Google Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Google Cloud SDK Setup](https://cloud.google.com/sdk/docs/install)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

---

Last Updated: 2025-12-16
