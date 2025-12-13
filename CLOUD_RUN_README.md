# User Management API - Cloud Run Deployment Guide

## Quick Start

This guide provides everything you need to deploy the User Management API to Google Cloud Run with Cloud SQL.

### Prerequisites
- GCP account with billing enabled
- Google Cloud SDK (gcloud CLI)
- Docker installed
- Maven 3.9+
- Bash shell (or Windows cmd/PowerShell)

### Deployment in 5 Steps

```bash
# 1. Authenticate with GCP
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# 2. Set up GCP infrastructure (one-time)
chmod +x setup-gcp-infrastructure.sh
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID us-central1

# 3. Source the configuration
source ./gcp-config.env

# 4. Deploy to Cloud Run
chmod +x deploy-to-cloud-run.sh
./deploy-to-cloud-run.sh

# 5. Test the deployment
SERVICE_URL=$(gcloud run services describe user-management-api \
    --platform managed \
    --region us-central1 \
    --format='value(status.url)')

curl -X GET "$SERVICE_URL/actuator/health"
```

## Detailed Documentation

### Setup Guides
1. **[GCP Project Setup](CLOUD_RUN_DEPLOYMENT.md#step-1-set-up-gcp-project-environment-variables)** - Initial GCP configuration
2. **[Cloud SQL Setup](CLOUD_RUN_DEPLOYMENT.md#step-3-create-cloud-sql-instance)** - Database creation
3. **[Service Account Setup](CLOUD_RUN_DEPLOYMENT.md#step-4-create-service-account)** - IAM configuration
4. **[Docker Preparation](CLOUD_RUN_DEPLOYMENT.md#step-5-build-and-push-docker-image)** - Build and test container

### Deployment Guide
- **[Full Deployment Guide](CLOUD_RUN_DEPLOYMENT.md)** - Complete step-by-step instructions
- **[Deployment Checklist](CLOUD_RUN_DEPLOYMENT_CHECKLIST.md)** - Pre-deployment checklist

### Automation Scripts
- **`setup-gcp-infrastructure.sh`** - Automated GCP setup (Linux/macOS)
- **`deploy-to-cloud-run.sh`** - Automated deployment script (Linux/macOS)
- **`deploy-to-cloud-run.bat`** - Automated deployment script (Windows)

### Configuration Files
- **`Dockerfile`** - Multi-stage Docker build
- **`.dockerignore`** - Docker build optimization
- **`src/main/resources/application-gcp.yml`** - Cloud Run configuration

## Architecture Overview

```
┌─────────────────────────────────────────┐
│         Cloud Run Service               │
│  (user-management-api)                  │
│  - Java 21 runtime                      │
│  - Spring Boot 3.2                      │
│  - 512MB memory, 1 CPU                  │
└──────────────┬──────────────────────────┘
               │
               │ Cloud SQL Proxy
               │
┌──────────────▼──────────────────────────┐
│      Cloud SQL MySQL Instance           │
│  (user-management-db)                   │
│  - MySQL 8.0                            │
│  - Regional backup                      │
│  - db-f1-micro tier                     │
└─────────────────────────────────────────┘
```

## Key Features

### Scalability
- Auto-scaling from 0 to 10 instances
- Concurrent request handling
- Connection pooling to Cloud SQL

### Security
- Service account with least privilege
- Cloud SQL Auth proxy (no public IP)
- Automatic HTTPS
- Environment variable-based secrets

### Monitoring
- Cloud Logging integration
- Cloud Monitoring metrics
- Health endpoints
- Request tracing

### Cost Optimization
- Pay-per-use billing
- Automatic scaling down
- Optimized Docker image
- Efficient database queries

## Configuration Reference

### Environment Variables

| Variable | Purpose | Example |
|----------|---------|---------|
| `GCP_PROJECT_ID` | GCP project | `my-project-123` |
| `CLOUD_RUN_REGION` | Deployment region | `us-central1` |
| `DB_INSTANCE` | Cloud SQL instance | `user-management-db` |
| `DB_USER` | Database user | `root` |
| `DB_PASSWORD` | Database password | `secure-password` |
| `DB_NAME` | Database name | `my_auth_db` |
| `JWT_SECRET` | JWT signing key | `base64-encoded-secret` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Application Configuration

The application uses `application-gcp.yml` with:
- MySQL connection via Cloud SQL Proxy
- Optimized connection pool for serverless
- Health check endpoints
- Structured logging

## Testing

### Local Testing
```bash
# Build Docker image
docker build -t user-management-api .

# Run locally
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=h2 \
  user-management-api

# Test endpoints
curl http://localhost:8080/actuator/health
```

### Cloud Testing
```bash
SERVICE_URL=$(gcloud run services describe user-management-api \
    --platform managed --region us-central1 --format='value(status.url)')

# Health check
curl -X GET "$SERVICE_URL/actuator/health"

# Login
curl -X POST "$SERVICE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fincore.com","password":"admin123"}'

# Get all users
curl -X GET "$SERVICE_URL/api/users"
```

## Troubleshooting

### Build Issues
```bash
# Clean build locally first
mvn clean compile

# Check Docker installation
docker --version

# View build logs
docker build -t user-management-api . --progress=plain
```

### Deployment Issues
```bash
# View real-time logs
gcloud run logs read user-management-api --follow

# Check service status
gcloud run services describe user-management-api

# View recent deployments
gcloud run revisions list --service=user-management-api
```

### Database Issues
```bash
# Check Cloud SQL instance
gcloud sql instances describe user-management-db

# View database logs
gcloud sql operations list --instance=user-management-db

# Connect to database (requires Cloud SQL Proxy)
cloud_sql_proxy -instances=PROJECT:REGION:INSTANCE
mysql -h 127.0.0.1 -u root -p my_auth_db
```

## Monitoring & Maintenance

### View Logs
```bash
# Last 50 lines
gcloud run logs read user-management-api --limit=50

# Specific revision
gcloud run logs read user-management-api/REVISION_NAME

# Stream logs
gcloud run logs read user-management-api --follow --limit=10
```

### View Metrics
```bash
# Service metrics
gcloud monitoring metrics-descriptors list --filter="service:user-management-api"

# Cloud SQL metrics
gcloud sql backups list --instance=user-management-db
```

### Scaling
```bash
# Update service scaling
gcloud run services update user-management-api \
    --max-instances=20 \
    --min-instances=2 \
    --memory=1Gi \
    --cpu=2
```

## Rollback Procedures

### View Deployment History
```bash
gcloud run revisions list --service=user-management-api
```

### Rollback to Previous Version
```bash
# Deploy previous image
gcloud run deploy user-management-api \
    --image=gcr.io/PROJECT_ID/user-management-api:PREVIOUS_TAG
```

## Database Backups

### Automated Backups
Cloud SQL automatically backs up daily (3:00 AM UTC by default)

### Manual Backup
```bash
gcloud sql backups create --instance=user-management-db
```

### Restore from Backup
```bash
gcloud sql backups restore BACKUP_ID --backup-instance=user-management-db
```

## Cost Estimation

### Approximate Monthly Costs (Light Usage)
- Cloud Run: $0-5 (on-demand, minimal traffic)
- Cloud SQL: $15-30 (db-f1-micro instance)
- Container Registry: $0-1 (storage)
- **Total: $15-36/month**

For detailed pricing: https://cloud.google.com/pricing

## Security Best Practices

1. **Secrets Management**
   - Use Google Secret Manager for DB passwords
   - Rotate secrets regularly
   - Never commit credentials to Git

2. **Network Security**
   - Cloud SQL Auth proxy (no public IP)
   - Service account with minimal permissions
   - HTTPS automatically enforced

3. **Application Security**
   - Keep dependencies updated
   - Regular security scans
   - Rate limiting on APIs
   - Input validation

4. **Audit & Compliance**
   - Enable Cloud Audit Logs
   - Monitor access patterns
   - Regular security reviews

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Deploy to Cloud Run

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build and push
        run: |
          docker build -t gcr.io/${{ secrets.GCP_PROJECT }}/user-management-api .
          docker push gcr.io/${{ secrets.GCP_PROJECT }}/user-management-api
      - name: Deploy
        run: |
          gcloud run deploy user-management-api \
            --image gcr.io/${{ secrets.GCP_PROJECT }}/user-management-api
```

## Support & Resources

- **Cloud Run Docs**: https://cloud.google.com/run/docs
- **Cloud SQL Docs**: https://cloud.google.com/sql/docs
- **Spring Boot Google Cloud**: https://spring.io/guides/gs/cloud-gcp/
- **GCP CLI Reference**: https://cloud.google.com/sdk/gcloud/reference
- **Issue Tracking**: [Create an issue in repository]

## FAQ

**Q: Can I use a smaller database tier?**
A: db-f1-micro is the smallest option. Use it for development/testing only.

**Q: How do I update the application?**
A: Build new Docker image → Push to GCR → Deploy to Cloud Run (see deployment script)

**Q: Can I use a different database?**
A: Yes, update `application-gcp.yml` with your database URL and credentials

**Q: How do I enable CORS?**
A: Add CORS configuration to SecurityConfig.java and redeploy

**Q: Can I use a custom domain?**
A: Yes, configure Cloud Run domain mapping in GCP Console

## Next Steps

1. ✅ Review this documentation
2. ✅ Review [CLOUD_RUN_DEPLOYMENT_CHECKLIST.md](CLOUD_RUN_DEPLOYMENT_CHECKLIST.md)
3. Run `./setup-gcp-infrastructure.sh` to create GCP resources
4. Run `./deploy-to-cloud-run.sh` to deploy the application
5. Test your deployment using the testing commands above
6. Set up monitoring and alerts in GCP Console

---

**Last Updated**: December 2024
**Java Version**: 21
**Spring Boot Version**: 3.2.0
**Database**: MySQL 8.0
