# Mock SumSub Integration Guide

## Overview

We're using a **mock SumSub service** for development until corporate email credentials are available. The mock service replicates all SumSub behavior without external dependencies.

---

## 🎭 How Mock SumSub Works

### 1. **Create Applicant**
```bash
POST /api/kyc/verifications
```
- Creates mock applicant with ID: `MOCK_XXXXXXXX`
- Stores in memory (not real SumSub)
- Returns mock access token for frontend

### 2. **Frontend SDK Simulation**
Instead of real SumSub SDK, frontend will:
- Show file upload UI
- Simulate document verification
- Call mock endpoints to complete verification

### 3. **Simulate Verification**
```bash
# Approve verification (admin only)
POST /api/mock/sumsub/applicants/{applicantId}/simulate-approval

# Reject verification (admin only)
POST /api/mock/sumsub/applicants/{applicantId}/simulate-rejection

# Check status
GET /api/mock/sumsub/applicants/{applicantId}/status
```

---

## 🔄 Complete Workflow Example

### Step 1: User Submits KYC
```bash
POST /api/kyc/verifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "verificationLevel": "FULL"
}
```

**Response:**
```json
{
  "verificationId": 1,
  "sumsubApplicantId": "MOCK_A1B2C3D4",
  "status": "PENDING",
  "accessToken": "MOCK_TOKEN_MOCK_A1B2C3D4_1683543210000"
}
```

### Step 2: Frontend Shows "Upload Documents"
- User uploads documents (stored locally, not sent to SumSub)
- Frontend calls mock completion endpoint

### Step 3: Admin Simulates Approval
```bash
POST /api/mock/sumsub/applicants/MOCK_A1B2C3D4/simulate-approval
Authorization: Bearer <admin-token>
```

**Response:**
```json
{
  "applicantId": "MOCK_A1B2C3D4",
  "reviewStatus": "completed",
  "reviewResult": "GREEN",
  "mock": true
}
```

### Step 4: Check Verification Status
```bash
GET /api/kyc/verifications/1
```

**Response:**
```json
{
  "verificationId": 1,
  "status": "APPROVED",
  "approvedAt": "2026-05-08T14:30:00",
  "sumsubApplicantId": "MOCK_A1B2C3D4"
}
```

---

## 🚀 Environment Configuration

### NPE (Uses Mock)
```yaml
# application-npe.yml
spring:
  profiles:
    active: npe

sumsub:
  mock: true  # Use mock service
```

### Production (Uses Real SumSub)
```yaml
# application-production.yml
spring:
  profiles:
    active: production

sumsub:
  base-url: https://api.sumsub.com
  app-token: ${SUMSUB_APP_TOKEN}
  secret-key: ${SUMSUB_SECRET_KEY}
  webhook-secret: ${SUMSUB_WEBHOOK_SECRET}
  mock: false  # Use real service
```

---

## 🔀 Switching to Real SumSub

When you get corporate email and SumSub credentials:

### 1. Create Real SumSub Service
```java
@Service
@Profile("production")
public class RealSumSubService implements SumSubService {
    // Implement using real SumSub API
    // HTTP calls to api.sumsub.com
    // HMAC-SHA256 signature generation
    // Proper error handling
}
```

### 2. Update Configuration
```yaml
# application-production.yml
sumsub:
  base-url: https://api.sumsub.com
  app-token: YOUR_APP_TOKEN
  secret-key: YOUR_SECRET_KEY
  webhook-secret: YOUR_WEBHOOK_SECRET
```

### 3. Deploy to Production
- NPE continues using mock
- UAT uses mock
- Production uses real SumSub

---

## 🧪 Testing Scenarios

### Scenario 1: Happy Path (Approval)
```bash
# 1. Create verification
POST /api/kyc/verifications

# 2. Simulate approval
POST /api/mock/sumsub/applicants/MOCK_XXX/simulate-approval

# 3. Verify status
GET /api/kyc/verifications/1
# Status should be APPROVED
```

### Scenario 2: Rejection Path
```bash
# 1. Create verification
POST /api/kyc/verifications

# 2. Simulate rejection
POST /api/mock/sumsub/applicants/MOCK_XXX/simulate-rejection

# 3. Verify status
GET /api/kyc/verifications/1
# Status should be REJECTED
```

### Scenario 3: AML Screening
```bash
# 1. Create verification
POST /api/kyc/verifications

# 2. Trigger AML screening
POST /api/kyc/verifications/1/aml-screening

# 3. Check AML results (mock returns no matches)
GET /api/kyc/verifications/1/aml-results
```

---

## 📊 Mock Data Patterns

### Mock Applicant IDs
- Format: `MOCK_XXXXXXXX` (8 random chars)
- Example: `MOCK_A1B2C3D4`

### Mock Access Tokens
- Format: `MOCK_TOKEN_{applicantId}_{timestamp}`
- Example: `MOCK_TOKEN_MOCK_A1B2C3D4_1683543210000`

### Mock Review Results
- `GREEN` = Approved
- `RED` = Rejected
- `YELLOW` = Needs review (not used in mock)

### Mock Review Status
- `init` = Just created
- `pending` = Under review
- `completed` = Review finished

---

## 🎯 Benefits of Mock Approach

✅ **No External Dependencies**
- Develop without SumSub account
- No API costs during development
- Faster iteration

✅ **Complete Control**
- Test approval scenarios instantly
- Test rejection scenarios
- Test error handling

✅ **Realistic Simulation**
- Same interface as real service
- Same data structures
- Same workflow

✅ **Easy Migration**
- Switch to real SumSub by changing profile
- No code changes needed
- Configuration-only change

---

## 📝 Notes

1. **Mock is in-memory**: Applicants stored in memory, not database
2. **Mock endpoints only in NPE**: Not available in production
3. **Admin access required**: Simulation endpoints require ADMIN role
4. **Frontend integration**: Build UI against mock, works with real SumSub later
5. **Migration path**: Clear path to switch to real SumSub when ready

---

## 🔐 Security Notes

- Mock service **NEVER** deployed to production
- Spring profiles control which service is used
- Mock endpoints require authentication
- Admin-only access to simulation endpoints

---

## 📞 Next Steps

1. ✅ Mock service created
2. ⏳ Integrate with KYC verification workflow
3. ⏳ Build frontend UI using mock
4. ⏳ Test complete workflow
5. ⏳ When corporate email available: switch to real SumSub
