# Cloud Run Deployment - Quick Reference Card

## 🚀 5-Minute Quick Start

```bash
# 1. Configure environment
cp gcp-config.env.template gcp-config.env
# Edit gcp-config.env with your values

# 2. Set up GCP (first time only)
chmod +x setup-gcp-infrastructure.sh
./setup-gcp-infrastructure.sh YOUR_PROJECT_ID us-central1

# 3. Deploy application
chmod +x deploy-to-cloud-run.sh
./deploy-to-cloud-run.sh

# 4. Get service URL and test
SERVICE_URL=$(gcloud run services describe user-management-api \
    --region us-central1 --format='value(status.url)')
curl $SERVICE_URL/actuator/health
```

## 📋 Pre-Deployment Checklist

- [ ] Java 21 installed: `java -version`
- [ ] Maven installed: `mvn --version`
- [ ] Docker installed: `docker --version`
- [ ] gcloud CLI installed: `gcloud --version`
- [ ] GCP project ID ready
- [ ] Authenticated: `gcloud auth login`
- [ ] Environment file configured: `gcp-config.env`

## 🔧 Essential Environment Variables

```bash
GCP_PROJECT_ID=your-project-id
CLOUD_RUN_REGION=us-central1
CLOUD_RUN_SERVICE_NAME=user-management-api
DB_INSTANCE=user-management-db
DB_USER=root
DB_PASSWORD=your-secure-password
DB_NAME=my_auth_db
JWT_SECRET=your-base64-secret
```

## 📁 File Reference

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage Docker build |
| `.dockerignore` | Optimize build context |
| `application-gcp.yml` | Cloud Run configuration |
| `setup-gcp-infrastructure.sh` | Automated GCP setup |
| `deploy-to-cloud-run.sh` | Automated deployment |
| `gcp-config.env.template` | Configuration template |

## 🧪 Testing Commands

```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe user-management-api \
    --platform managed --region us-central1 --format='value(status.url)')

# Health check (should return 200)
curl -X GET "$SERVICE_URL/actuator/health"

# Login (requires seeded data)
curl -X POST "$SERVICE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@fincore.com","password":"admin123"}'

# Get all users
curl -X GET "$SERVICE_URL/api/users"

# Create user
curl -X POST "$SERVICE_URL/api/users" \
    -H "Content-Type: application/json" \
    -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}'
```

## 📊 Monitoring Commands

```bash
# View logs (real-time)
gcloud run logs read user-management-api --follow

# View last 50 lines
gcloud run logs read user-management-api --limit 50

# Service details
gcloud run services describe user-management-api --region us-central1

# View revisions
gcloud run revisions list --service=user-management-api

# Service URL
gcloud run services describe user-management-api \
    --region us-central1 --format='value(status.url)'
```

## 🔄 Update Application

```bash
# 1. Make code changes and commit
git commit -am "Update application"

# 2. Run deployment script
./deploy-to-cloud-run.sh

# Or manually:
# Build application
mvn clean package -DskipTests

# Build Docker image
docker build -t gcr.io/$GCP_PROJECT_ID/user-management-api:latest .

# Push to GCR
docker push gcr.io/$GCP_PROJECT_ID/user-management-api:latest

# Deploy to Cloud Run
gcloud run deploy user-management-api \
    --image gcr.io/$GCP_PROJECT_ID/user-management-api:latest \
    --platform managed \
    --region us-central1
```

## ⚙️ Adjust Scaling

```bash
# Increase memory and CPU
gcloud run services update user-management-api \
    --memory 1Gi \
    --cpu 2 \
    --max-instances 50

# Reduce scaling for cost savings
gcloud run services update user-management-api \
    --memory 512Mi \
    --cpu 1 \
    --max-instances 10 \
    --min-instances 0
```

## 🔙 Rollback

```bash
# List revisions
gcloud run revisions list --service=user-management-api

# Deploy previous revision
gcloud run deploy user-management-api \
    --image gcr.io/$GCP_PROJECT_ID/user-management-api:PREVIOUS_TAG \
    --region us-central1
```

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails | Run `mvn clean compile` locally |
| Docker error | Check `docker ps`, ensure Docker is running |
| Deployment timeout | Check logs: `gcloud run logs read user-management-api` |
| Database error | Verify Cloud SQL instance: `gcloud sql instances describe user-management-db` |
| Health check fails | View logs: `gcloud run logs read user-management-api --limit 50` |
| Permission denied | Check service account: `gcloud iam service-accounts describe user-management-api-sa` |

## 📚 Documentation Links

- **Full Guide**: `CLOUD_RUN_DEPLOYMENT.md`
- **Checklist**: `CLOUD_RUN_DEPLOYMENT_CHECKLIST.md`
- **Summary**: `CLOUD_RUN_DEPLOYMENT_SUMMARY.md`
- **README**: `CLOUD_RUN_README.md`
- **Configuration Template**: `gcp-config.env.template`

## 💰 Cost Monitoring

```bash
# View recent costs in GCP Console
# https://console.cloud.google.com/billing

# Estimate monthly usage:
# Cloud Run: $0-10/month (pay-per-request)
# Cloud SQL: $15-20/month (db-f1-micro)
# Total: ~$15-30/month
```

## 🔐 Security Checklist

- [ ] Database password is strong (8+ characters, special chars)
- [ ] JWT secret is properly generated
- [ ] Service account has minimal permissions
- [ ] No hardcoded credentials in code
- [ ] HTTPS enforced (automatic in Cloud Run)
- [ ] Audit logs enabled
- [ ] Backup strategy configured

## 📞 Support Resources

- **Cloud Run Docs**: https://cloud.google.com/run/docs
- **Cloud SQL Docs**: https://cloud.google.com/sql/docs
- **Spring Boot Google Cloud**: https://spring.io/guides/gs/cloud-gcp/
- **GCP Status**: https://status.cloud.google.com/

---

## Key Takeaways

✅ **Easy Deployment**: Single command deployment with `deploy-to-cloud-run.sh`
✅ **Scalable**: Auto-scales from 0-10 instances based on demand
✅ **Secure**: Cloud SQL proxy, HTTPS, service accounts
✅ **Cost-Effective**: ~$15-30/month for light usage
✅ **Monitoring**: Cloud Logging and Monitoring integrated

**Ready to deploy?** → Start with `gcp-config.env` configuration
