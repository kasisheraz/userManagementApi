# Smoke Testing Documentation

## Overview

Comprehensive smoke tests are automatically executed after every successful deployment to ensure all Phase 1 and Phase 2 features are working correctly.

## Automated Tests (GitHub Actions)

### Location
`.github/workflows/deploy-npe.yml` - Step: "Run smoke tests"

### Tests Executed

#### Phase 1 Tests (Original Features)
1. **Health Check** - Validates service is running
   - Endpoint: `GET /actuator/health`
   - Expected: `{"status":"UP"}`

2. **OTP Request** - Tests user authentication and database connectivity
   - Endpoint: `POST /api/auth/request-otp`
   - Tests table: `users`
   - Validates: User lookup, OTP generation

3. **OTP Verification** - Tests authentication flow
   - Endpoint: `POST /api/auth/verify-otp`
   - Tests tables: `otp_tokens`, `roles`
   - Validates: Token generation, user roles

#### Phase 2 Tests (KYC & Questionnaire Features)
4. **KYC Verification** - Tests Phase 2 KYC endpoints
   - Endpoint: `POST /api/kyc/verification`
   - Tests table: `customer_kyc_verification`
   - Validates: KYC submission, verification levels

5. **Questionnaire Questions** - Tests questionnaire management
   - Endpoint: `GET /api/questionnaire/questions`
   - Tests table: `questionnaire_questions`
   - Validates: Question retrieval, categories

### Database Tables Covered

**Phase 1:**
- ✓ users
- ✓ otp_tokens
- ✓ roles

**Phase 2:**
- ✓ customer_kyc_verification
- ✓ aml_screening_results (embedded)
- ✓ questionnaire_questions
- ✓ customer_answers

## Manual Testing Script

### Comprehensive Phase 2 Test Suite

**File:** `test-phase2-apis.ps1`

**Usage:**
```powershell
# Test against NPE environment
.\test-phase2-apis.ps1

# Test against custom URL
.\test-phase2-apis.ps1 -ServiceUrl "https://your-service-url.run.app"

# Test with custom phone
.\test-phase2-apis.ps1 -PhoneNumber "+9876543210"
```

### Test Coverage (16 Tests)

1. ✅ Health Check
2. ✅ Request OTP
3. ✅ Verify OTP & Get Token
4. ✅ Submit KYC Verification
5. ✅ Get User KYC Verifications
6. ✅ Get Verification by ID
7. ✅ Update Verification Status
8. ✅ Create Question
9. ✅ Get All Questions
10. ✅ Get Question by ID
11. ✅ Get Questions by Category
12. ✅ Submit Answer
13. ✅ Get User Answers
14. ✅ Get Answer by ID
15. ✅ Update Answer
16. ✅ Get KYC with AML Results

### Features Tested
- **Full CRUD Operations** on all Phase 2 entities
- **Authentication Flow** with JWT tokens
- **Data Validation** across all endpoints
- **Database Integrity** for all 4 Phase 2 tables
- **Auto Cleanup** of test data

## Postman Collections

### Phase 1 Collection
**File:** `postman_collection.json`
- Authentication endpoints
- User management
- Organization management
- Address management

### Phase 2 Collection
**File:** `phase2-postman-collection.json`
- KYC Verification (10+ endpoints)
- AML Screening (embedded)
- Questionnaire Management (8+ endpoints)
- Customer Answers (6+ endpoints)

**Usage:**
1. Import collection into Postman
2. Import environment file (`postman_environment_cloud.json`)
3. Set `base_url` variable to service URL
4. Run entire collection or individual folders

## CI/CD Integration

### Workflow Steps
1. **Build** → Compile and package JAR
2. **Docker Build** → Create container image
3. **Push to GCR** → Upload to Google Container Registry
4. **Deploy** → Deploy to Cloud Run
5. **Health Check** → Wait for service to be ready (up to 5 minutes)
6. **Smoke Tests** → Run automated tests ✨ **(NEW)**

### Test Execution
- Runs automatically after successful deployment
- Failures block deployment completion
- Detailed output in GitHub Actions logs
- Service URL available in job output

### Expected Output
```
✅ ALL SMOKE TESTS PASSED
========================================
Phase 1 tables tested:
  ✓ users (via OTP request)
  ✓ otp_tokens (via OTP verification)
  ✓ roles (via user authentication)

Phase 2 tables tested:
  ✓ customer_kyc_verification (via KYC API)
  ✓ questionnaire_questions (via Questions API)
  ✓ aml_screening_results (embedded in KYC)
  ✓ customer_answers (via Answers API)

Service is fully operational with all Phase 2 features!
```

## Monitoring & Alerts

### Success Indicators
- All smoke tests pass
- HTTP 200/201 responses
- JWT token generated
- Database records created/retrieved

### Failure Scenarios
- Health check timeout (5 minutes)
- Authentication failures
- Database connectivity issues
- API permission errors

### Debug Information
Each test outputs:
- HTTP status codes
- Response bodies (on failure)
- Token snippets (for verification)
- Record IDs (for traceability)

## Future Enhancements

### Planned Additions
- [ ] Performance benchmarking (response times)
- [ ] Load testing (concurrent requests)
- [ ] Data validation (schema compliance)
- [ ] Negative test cases (error handling)
- [ ] Integration with monitoring tools

### Extensibility
The smoke test framework is designed to be extended:
1. Add new test cases to workflow YAML
2. Update `test-phase2-apis.ps1` for manual testing
3. Create new Postman collection folders
4. Document in this file

## Best Practices

### When to Run Manual Tests
- After schema changes
- Before major releases
- When investigating issues
- For performance validation

### Test Data Management
- Use unique identifiers (timestamps)
- Clean up test data after execution
- Don't rely on specific record IDs
- Use dedicated test accounts

### Troubleshooting
1. Check service logs in Cloud Run console
2. Verify database schema matches entities
3. Confirm authentication tokens are valid
4. Review API permissions and roles

## Related Documentation
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Full deployment process
- [POSTMAN_USAGE_GUIDE.md](POSTMAN_USAGE_GUIDE.md) - Postman collection usage
- [PHASE2_POSTMAN_GUIDE.md](PHASE2_POSTMAN_GUIDE.md) - Phase 2 specific testing
- [API_TESTING_STRATEGY.md](API_TESTING_STRATEGY.md) - Overall testing strategy
