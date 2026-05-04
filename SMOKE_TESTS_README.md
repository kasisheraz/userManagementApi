# 🧪 UAT Smoke Tests

Quick validation tests to verify UAT deployment health.

## 📦 What's Included

### 1. **User Guide** - Manual Testing
- **File**: [UAT_LOGIN_GUIDE.md](./UAT_LOGIN_GUIDE.md)
- **Purpose**: Step-by-step guide for logging into UAT using browser DevTools
- **Audience**: Developers, QA testers, anyone testing UAT
- **Key Features**:
  - How to see devOtp in Network tab
  - Screenshot examples
  - Troubleshooting tips
  - Alternative login methods

### 2. **API Smoke Tests** - Automated
- **File**: `scripts/smoke-test-uat-api.js`
- **Purpose**: Fast automated tests of critical API endpoints
- **Tests**: Health, Auth, User endpoints (6 tests)
- **Duration**: ~30 seconds
- **Usage**:
  ```bash
  npm run smoke-test:uat
  ```

### 3. **Postman Collection Tests** - Newman
- **Files**: `postman_collection_uat.json`, `postman_environment_uat.json`
- **Purpose**: Run Postman tests from command line
- **Tests**: 7 API requests with validation
- **Duration**: ~15 seconds
- **Usage**:
  ```bash
  npm run smoke-test:newman
  ```

### 4. **GitHub Actions Integration**
- **File**: `.github/workflows/deploy-uat-with-tests.yml`
- **Purpose**: Automated deployment + smoke tests on push to UAT
- **Stages**:
  1. Build (Cloud Build)
  2. Deploy (Cloud Run)
  3. Smoke Tests (Validate)
  4. Notify (Alert team)

### 5. **Strategy Guide** - Best Practices
- **File**: [UAT_DEPLOYMENT_STRATEGY.md](./UAT_DEPLOYMENT_STRATEGY.md)
- **Purpose**: Explains the approach and rationale
- **Topics**:
  - Why "deploy then test"
  - Rollback strategies
  - Metrics to track
  - Alternative approaches

## 🚀 Quick Start

### Run Smoke Tests Locally

```bash
# Install dependencies
npm install

# Run API smoke tests
npm run smoke-test:uat

# Run Postman/Newman tests
npm run smoke-test:newman

# Run both
npm run smoke-test:all
```

### Expected Output (Success)

```
========================================
🧪 UAT API Smoke Tests
========================================
API URL: https://fincore-uat-api-994490239798.europe-west2.run.app
Test Phone: +447700900000
Time: 2026-05-04T19:30:00Z

✅ Health Check - API is UP
   Status: UP
✅ Request OTP - Success (devOtp returned)
   OTP: 123456, Expires: 300s
✅ Verify OTP - Success
   User: Admin User (Admin), Token expires: 900s
✅ Get Current User - Success
   Admin User - admin@fincore.test
✅ Get All Users - Success
   Found 4 users
✅ Database Connectivity - Success
   Auth and queries working

========================================
📊 Test Summary
========================================
✅ Passed: 6
❌ Failed: 0
Total: 6

✅ All smoke tests PASSED - UAT is healthy!
```

### Expected Output (Failure)

```
✅ Health Check - API is UP
   Status: UP
❌ Request OTP - User Not Found
   Test user +447700900000 missing from database
❌ Verify OTP - Skipped
   No OTP from previous test
...

========================================
📊 Test Summary
========================================
✅ Passed: 1
❌ Failed: 5
Total: 6

❌ Smoke tests FAILED - UAT deployment may have issues
```

## 🔄 Automated Deployment Flow

### Trigger: Push to `uat` branch

```bash
git push origin uat
```

### What Happens:

1. **Build** (5-10 min)
   - Docker image built via Cloud Build
   - Tagged with commit SHA

2. **Deploy** (2-3 min)
   - Deployed to Cloud Run UAT
   - Environment variables configured
   - 30 second stabilization wait

3. **Smoke Tests** (1-2 min)
   - API tests run automatically
   - Newman tests run automatically
   - Results uploaded as artifacts

4. **Notify** (instant)
   - ✅ Success: Ready for testing
   - ⚠️ Warning: Deployed but tests failed
   - ❌ Failure: Deployment failed

### View Results

- GitHub Actions: https://github.com/kasisheraz/userManagementApi/actions
- Click on latest workflow run
- Check "Smoke Tests" job
- Download artifacts for detailed results

## 📋 What Gets Tested

### ✅ Critical Paths (Smoke Tests)

```
Health Check       → API is responding
Request OTP        → Auth flow starts, devOtp returned
Verify OTP         → Authentication works, JWT issued
Get Current User   → JWT validation, user retrieval works
Get All Users      → Database queries work
Database Check     → Connectivity verified
```

### ❌ NOT Tested (Not Smoke Tests)

```
Full workflows     → E2E tests
Edge cases         → Integration tests
Performance        → Load tests
Security           → Penetration tests
UI functionality   → Frontend E2E tests
```

**Smoke tests = Fast critical path validation**

## 🐛 Troubleshooting

### Tests Pass Locally, Fail in CI

**Possible Causes:**
- Environment variables not set in GitHub Secrets
- Network connectivity issues in CI
- Test timing issues (too fast, service not ready)

**Solutions:**
1. Check GitHub Secrets are configured
2. Increase wait time after deployment (currently 30s)
3. Add retry logic to tests

### "User Not Found" Error

**Cause**: Test user `+447700900000` doesn't exist in UAT database

**Fix**:
```bash
# Check if user exists
gcloud sql databases list --instance=fincore-uat-db

# Import test data if missing
gcloud sql import sql fincore-uat-db gs://fincore-uat-terraform-state/uat-test-data.sql --database=fincore_db
```

### Newman Tests Fail but API Tests Pass

**Cause**: 
- Postman collection out of sync with API
- Variable names changed
- Endpoint URLs incorrect

**Fix**:
1. Update `postman_collection_uat.json`
2. Test locally: `npm run smoke-test:newman`
3. Commit and push

## 📊 Monitoring & Alerts

### GitHub Actions Notifications

GitHub sends email/notifications on:
- ✅ Workflow success
- ❌ Workflow failure
- ⚠️ Job warnings

### Future Enhancements (Planned)

- [ ] Slack notifications
- [ ] Email alerts to team
- [ ] Status page integration
- [ ] Synthetic monitoring (run tests every 10 min)
- [ ] Performance regression alerts

## 🎯 Success Metrics

Track these over time:

| Metric | Target | Current |
|--------|--------|---------|
| Deployment Success Rate | >95% | 🆕 |
| Smoke Test Pass Rate | >90% | 🆕 |
| Average Deployment Time | <15 min | ~10 min |
| Rollback Frequency | <5% | 🆕 |
| False Positive Rate | <5% | 🆕 |

## 📝 Maintenance

### Weekly Tasks
- [ ] Review failed deployments
- [ ] Update test credentials if expired
- [ ] Check test execution times

### Monthly Tasks
- [ ] Review and update smoke test coverage
- [ ] Rotate test account passwords
- [ ] Update documentation if process changes
- [ ] Review metrics and trends

### Quarterly Tasks
- [ ] Evaluate test effectiveness
- [ ] Consider adding/removing tests
- [ ] Benchmark against industry standards

## 🤝 Contributing

### Adding New Smoke Tests

1. Add test function to `scripts/smoke-test-uat-api.js`
2. Test locally: `npm run smoke-test:uat`
3. Update this README with new test description
4. Commit and push

### Modifying Postman Collection

1. Edit collection in Postman app
2. Export updated collection
3. Overwrite `postman_collection_uat.json`
4. Test: `npm run smoke-test:newman`
5. Commit and push

## 📚 Related Documentation

- [UAT_LOGIN_GUIDE.md](./UAT_LOGIN_GUIDE.md) - Manual login with DevTools
- [UAT_DEPLOYMENT_STRATEGY.md](./UAT_DEPLOYMENT_STRATEGY.md) - Strategy and rationale
- [POSTMAN_UAT_GUIDE.md](./POSTMAN_UAT_GUIDE.md) - Postman API testing guide
- [UAT_NPE_PARITY_FIX.md](./UAT_NPE_PARITY_FIX.md) - devOtp configuration history

## 🆘 Support

### Issues with Tests
- Check GitHub Actions logs
- Run tests locally to reproduce
- Review test output for specific failures

### Issues with Deployment
- Check Cloud Run logs: `gcloud run logs read fincore-uat-api --region=europe-west2`
- Verify environment variables
- Check database connectivity

### Questions
- Contact DevOps team
- Check documentation files above
- Review GitHub Actions workflow file

---

**Last Updated**: May 4, 2026  
**Status**: ✅ Production Ready  
**Maintenance**: DevOps Team
