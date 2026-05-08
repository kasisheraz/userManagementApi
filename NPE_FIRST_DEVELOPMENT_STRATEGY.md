# NPE-First Development Strategy for KYC Feature

## Decision Summary

**Date**: May 8, 2026  
**Decision**: Develop KYC verification feature in NPE environment first, then promote to UAT, then Production

---

## Development Flow

```
┌──────────────┐
│  Development │  Weeks 1-3: Build features locally + deploy to NPE incrementally
│    (Local)   │  - Database schema
└──────┬───────┘  - Backend services
       │          - Frontend UI
       │          - Unit tests
       ↓
┌──────────────┐
│     NPE      │  Weeks 4-5: Comprehensive testing in dedicated environment
│  (Non-Prod)  │  - Integration tests
└──────┬───────┘  - E2E tests
       │          - SumSub sandbox integration
       │          - Bug fixes & optimization
       │          - Documentation
       ↓
┌──────────────┐
│     UAT      │  Week 6: Stakeholder testing
│ (Pre-Prod)   │  - Acceptance testing
└──────┬───────┘  - Final validation
       │          - Business sign-off
       │
       ↓
┌──────────────┐
│  Production  │  Week 7+: Gradual rollout
│    (Live)    │  - 10% traffic → 50% → 100%
└──────────────┘  - Monitoring & support
```

---

## Why NPE First?

### Benefits:
1. ✅ **Isolation**: No impact on current UAT users/testing
2. ✅ **Safety**: Dedicated environment for experimentation
3. ✅ **Stability**: UAT remains stable for ongoing testing
4. ✅ **Flexibility**: Can break things in NPE without consequences
5. ✅ **Confidence**: Thoroughly tested before UAT promotion
6. ✅ **Demo-Ready**: Can show working features to stakeholders early

### Risks Mitigated:
- ❌ Avoid breaking UAT with incomplete features
- ❌ No disruption to current UAT test cycles
- ❌ No data corruption in UAT database
- ❌ No downtime for UAT users

---

## Environment Configuration

### NPE Environment Setup

#### 1. Database (Cloud SQL)
```bash
# Create NPE database instance
gcloud sql instances create fincore-npe-db \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=europe-west2 \
  --storage-size=10GB \
  --storage-type=SSD \
  --backup

# Create database
gcloud sql databases create fincore_db --instance=fincore-npe-db

# Create users
gcloud sql users create fincore_admin --instance=fincore-npe-db --password=<admin-password>
gcloud sql users create fincore_app --instance=fincore-npe-db --password=<app-password>
```

#### 2. Backend API (Cloud Run)
```bash
# Build and deploy backend
gcloud builds submit --tag gcr.io/project-07a61357-b791-4255-a9e/fincore-api:npe

gcloud run deploy fincore-npe-api \
  --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:npe \
  --region=europe-west2 \
  --platform=managed \
  --allow-unauthenticated \
  --memory=1Gi \
  --cpu=1 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=npe,DB_NAME=fincore_db" \
  --port=8080
```

#### 3. Frontend UI (Cloud Run)
```bash
# Build and deploy frontend
gcloud builds submit --config=cloudbuild.yaml --substitutions=_BUILD_ENV=npe

gcloud run deploy fincore-webui-npe \
  --image=europe-west2-docker.pkg.dev/project-07a61357-b791-4255-a9e/fincore-webui/app:npe \
  --region=europe-west2 \
  --set-env-vars="API_BASE_URL=https://fincore-npe-api-994490239798.europe-west2.run.app/api" \
  --port=8080
```

#### 4. Application Configuration

**application-npe.yml**:
```yaml
spring:
  profiles: npe
  
  datasource:
    url: jdbc:mysql://fincore-npe-db:3306/fincore_db?useSSL=true
    username: fincore_app
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema
    show-sql: false
    
  flyway:
    enabled: true
    baseline-on-migrate: true

# SumSub Configuration (Sandbox)
sumsub:
  base-url: https://api.sumsub.com
  app-token: ${SUMSUB_APP_TOKEN_SANDBOX}
  secret-key: ${SUMSUB_SECRET_KEY_SANDBOX}
  webhook-secret: ${SUMSUB_WEBHOOK_SECRET}
  level-name: basic-kyc-level

# GCS Configuration
gcs:
  enabled: true
  bucket-name: fincore-npe-kyc-documents
  project-id: project-07a61357-b791-4255-a9e

# Environment info
environment:
  name: NPE
  color: orange
  show-banner: true
```

---

## SumSub Configuration

### Sandbox vs Production

| Feature | Sandbox (NPE/UAT) | Production |
|---------|-------------------|------------|
| **Cost** | FREE (100 checks/month) | $1-5 per check |
| **API URL** | https://api.sumsub.com | https://api.sumsub.com |
| **Credentials** | Sandbox token + key | Production token + key |
| **Webhook** | NPE/UAT URL | Production URL |
| **Test Data** | Can use test documents | Real documents only |
| **Limitations** | 100 checks/month, test mode | No limits, live mode |

### Setup Steps:
1. **Create SumSub Account**: https://cockpit.sumsub.com/
2. **Get Sandbox Credentials**:
   - App Token: `sbx.xxxxxxxxxxxxx`
   - Secret Key: `yyyyyyyyyyyyyyy`
3. **Configure Webhook** (NPE):
   - URL: `https://fincore-npe-api-994490239798.europe-west2.run.app/api/webhooks/sumsub`
   - Secret: Generate strong secret
4. **Test Integration**: Use SumSub sandbox to test KYC flow

---

## Database Migration Strategy

### NPE Database Setup

```bash
# 1. Create NPE database
gcloud sql databases create fincore_db --instance=fincore-npe-db

# 2. Upload migration script to GCS
gsutil cp src/main/resources/db/migration/V8.0__Add_KYC_Verification_Tables.sql \
  gs://fincore-npe-terraform-state/

# 3. Run migration
gcloud sql import sql fincore-npe-db \
  gs://fincore-npe-terraform-state/V8.0__Add_KYC_Verification_Tables.sql \
  --database=fincore_db

# 4. Verify tables created
gcloud sql connect fincore-npe-db --user=fincore_admin --database=fincore_db
mysql> SHOW TABLES LIKE '%kyc%';
mysql> SELECT * FROM questionnaire_questions;  -- Should have 10 questions
```

---

## Testing Strategy (NPE-Centric)

### Phase 1: Unit Testing (Local)
- Services, Controllers, DTOs
- Run: `mvn test`
- Coverage target: 80%+

### Phase 2: Integration Testing (NPE)
- Deploy to NPE
- Test API endpoints:
  ```bash
  # Test KYC workflow start
  curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/kyc/start \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"userId": 1, "level": "BASIC"}'
  
  # Test SumSub applicant creation
  curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/kyc/sumsub/create \
    -H "Authorization: Bearer $TOKEN"
  
  # Test questionnaire
  curl https://fincore-npe-api-994490239798.europe-west2.run.app/api/questionnaire/questions
  ```

### Phase 3: E2E Testing (NPE Frontend)
- Access NPE UI: `https://fincore-webui-npe-994490239798.europe-west2.run.app`
- Test complete KYC flow:
  1. Login as test user
  2. Start KYC verification
  3. Enter user information
  4. Upload documents via SumSub SDK
  5. Complete questionnaire
  6. Review and submit
  7. Check status
- Run Playwright tests:
  ```bash
  npm run test:e2e -- --config=playwright.npe.config.ts
  ```

### Phase 4: Performance Testing (NPE)
- Load testing with k6/JMeter
- Simulate 100 concurrent KYC submissions
- Monitor API response times (<500ms target)
- Check database performance

### Phase 5: Security Testing (NPE)
- Webhook signature verification
- API authentication/authorization
- XSS/CSRF protection
- SQL injection prevention
- Sensitive data encryption

---

## Promotion Process

### NPE → UAT Promotion (Week 6)

**Pre-Promotion Checklist**:
- [ ] All E2E tests passing in NPE
- [ ] 80%+ code coverage achieved
- [ ] Performance tests passed
- [ ] Security audit completed
- [ ] Documentation complete
- [ ] No critical/high bugs open

**Promotion Steps**:
1. **Code Merge**:
   ```bash
   git checkout uat
   git merge npe --no-ff
   git push origin uat
   ```

2. **Database Migration** (UAT):
   ```bash
   gcloud sql import sql fincore-uat-db \
     gs://fincore-uat-terraform-state/V8.0__Add_KYC_Verification_Tables.sql \
     --database=fincore_db
   ```

3. **Backend Deployment**:
   ```bash
   gcloud builds submit --tag gcr.io/project-07a61357-b791-4255-a9e/fincore-api:uat
   
   gcloud run deploy fincore-uat-api \
     --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:uat \
     --set-env-vars="SPRING_PROFILES_ACTIVE=uat" \
     --region=europe-west2
   ```

4. **Frontend Deployment**:
   ```bash
   gcloud builds submit --config=cloudbuild.yaml --substitutions=_BUILD_ENV=uat
   
   gcloud run deploy fincore-webui-uat \
     --image=europe-west2-docker.pkg.dev/project-07a61357-b791-4255-a9e/fincore-webui/app:uat \
     --region=europe-west2
   ```

5. **Smoke Tests** (UAT):
   - Login test
   - Start KYC test
   - Complete one full KYC workflow
   - Admin dashboard access

6. **Stakeholder Testing**:
   - Share UAT URL with stakeholders
   - Collect feedback
   - Fix bugs if needed
   - Get sign-off

---

### UAT → Production Promotion (Week 7+)

**Pre-Production Checklist**:
- [ ] UAT acceptance testing complete
- [ ] Stakeholder sign-off received
- [ ] Production SumSub account created
- [ ] Production API keys configured
- [ ] Rollback plan documented
- [ ] Monitoring/alerting configured
- [ ] Support team trained

**Production Deployment**:
1. **Merge to main**:
   ```bash
   git checkout main
   git merge uat --no-ff
   git tag -a v1.0.0-kyc -m "KYC verification feature release"
   git push origin main --tags
   ```

2. **Database Migration** (Production):
   ```bash
   # Create backup first!
   gcloud sql backups create --instance=fincore-prod-db
   
   # Run migration
   gcloud sql import sql fincore-prod-db \
     gs://fincore-prod-terraform-state/V8.0__Add_KYC_Verification_Tables.sql \
     --database=fincore_db
   ```

3. **Deploy with Canary Strategy**:
   ```bash
   # Deploy new revision (10% traffic)
   gcloud run deploy fincore-prod-api \
     --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:prod \
     --set-env-vars="SPRING_PROFILES_ACTIVE=production" \
     --no-traffic  # Deploy without traffic first
   
   # Route 10% traffic to new revision
   gcloud run services update-traffic fincore-prod-api \
     --to-revisions=LATEST=10,PREVIOUS=90
   
   # Monitor for 30 minutes
   # If healthy, increase to 50%
   gcloud run services update-traffic fincore-prod-api \
     --to-revisions=LATEST=50,PREVIOUS=50
   
   # If still healthy, go to 100%
   gcloud run services update-traffic fincore-prod-api \
     --to-revisions=LATEST=100
   ```

4. **Switch SumSub to Production**:
   - Update env vars with production SumSub credentials
   - Configure production webhook URL
   - Test with real document submission

5. **Monitor**:
   - Check logs for errors
   - Monitor API latency
   - Track KYC submission success rate
   - Monitor SumSub webhook delivery

---

## Branch Strategy

```
main (Production)
  ├── uat (Pre-Production)
  │    └── npe (Development/Testing)
  │         └── feature/kyc-sumsub-integration
  │         └── feature/kyc-questionnaire
  │         └── feature/kyc-aml-screening
```

**Branch Lifecycle**:
1. Create feature branches from `npe`
2. Merge features to `npe` when complete
3. Test thoroughly in NPE environment
4. Promote `npe` → `uat` when stable
5. UAT testing and stakeholder approval
6. Promote `uat` → `main` for production

---

## Rollback Plan

### If Issues Found in NPE:
- Just fix and redeploy (no impact to UAT/Prod)

### If Issues Found in UAT:
```bash
# Revert merge
git checkout uat
git revert <merge-commit-hash>
git push origin uat

# Redeploy previous version
gcloud run deploy fincore-uat-api \
  --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:uat-previous
```

### If Issues Found in Production:
```bash
# Immediate rollback to previous revision
gcloud run services update-traffic fincore-prod-api \
  --to-revisions=PREVIOUS=100

# Or deploy previous version
gcloud run deploy fincore-prod-api \
  --image=gcr.io/project-07a61357-b791-4255-a9e/fincore-api:v0.9.0
```

---

## Success Metrics (NPE Testing)

Track these metrics during NPE testing:

| Metric | Target | Importance |
|--------|--------|------------|
| **Test Coverage** | ≥80% | High |
| **E2E Tests Passing** | 100% | Critical |
| **API Response Time** | <500ms (p95) | High |
| **KYC Success Rate** | >95% | High |
| **SumSub Integration Success** | >99% | Critical |
| **Webhook Delivery Rate** | >99% | High |
| **Bug Severity** | No Critical/High | Critical |
| **Documentation Complete** | 100% | Medium |

---

## Timeline Summary

| Week | Phase | Environment | Key Activities |
|------|-------|-------------|----------------|
| 1-2 | Development | Local + NPE | Build backend services, database schema |
| 3 | Development | Local + NPE | Build frontend UI, integrate SumSub |
| 4 | Testing | NPE | Integration tests, E2E tests |
| 5 | Stabilization | NPE | Bug fixes, performance tuning, documentation |
| 6 | UAT Testing | UAT | Stakeholder testing, feedback, sign-off |
| 7+ | Production | Production | Gradual rollout, monitoring |

**Total Duration**: 6-7 weeks (NPE-first adds ~1 week but increases confidence)

---

## Cost Estimate

### NPE Environment Costs (Monthly):
- Cloud SQL (db-f1-micro): ~$10/month
- Cloud Run Backend (minimal traffic): ~$5-10/month
- Cloud Run Frontend (minimal traffic): ~$5-10/month
- GCS Storage: ~$1-2/month
- SumSub Sandbox: FREE (100 checks/month)
- **Total NPE Cost**: ~$20-30/month

### Investment vs. Risk:
- NPE Cost: $150-200 (6-7 weeks)
- UAT Downtime Cost (if broken): $1,000-5,000 (business impact)
- **NPE ROI**: 5-25x risk mitigation

---

## Decision Approval

**Approved By**: _______________  
**Date**: May 8, 2026  
**Next Review**: After NPE deployment (Week 5)

---

## Quick Reference

### NPE URLs (Once Deployed):
- Frontend: `https://fincore-webui-npe-994490239798.europe-west2.run.app`
- Backend API: `https://fincore-npe-api-994490239798.europe-west2.run.app`
- Swagger Docs: `https://fincore-npe-api-994490239798.europe-west2.run.app/swagger-ui.html`

### SumSub Sandbox:
- Dashboard: `https://cockpit.sumsub.com/`
- API Base URL: `https://api.sumsub.com`

### Key Commands:
```bash
# Switch to NPE branch
git checkout npe

# Deploy to NPE
./deploy-npe.sh

# Run NPE tests
npm run test:e2e:npe

# Promote NPE to UAT
git checkout uat && git merge npe
```

---

**Document Version**: 1.0  
**Last Updated**: May 8, 2026  
**Owner**: Development Team
