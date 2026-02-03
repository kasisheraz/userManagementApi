# Phase 2 Postman Collection Guide

## Quick Start

### 1. Import the Collection
1. Open Postman
2. Click **Import** button (top left)
3. Select `phase2-postman-collection.json`
4. Collection will appear in your sidebar

### 2. Set Base URL
The collection uses `http://localhost:8080` by default.

To change:
1. Click on the collection name
2. Go to **Variables** tab
3. Update `base_url` value

### 3. Get JWT Token (Required)
**All Phase 2 APIs require authentication!**

1. Expand **"0. Authentication"** folder
2. Run **"Login (Get JWT Token)"** request
3. JWT token is automatically saved to `{{jwt_token}}` variable
4. All subsequent requests will use this token

Default credentials:
```json
{
  "phoneNumber": "+1234567890",
  "password": "password123"
}
```

---

## Collection Structure

### Folder 0: Authentication
- **Login (Get JWT Token)** - Get authentication token (saves automatically)

### Folder 1: KYC Verification (9 endpoints)
| Endpoint | Method | Description |
|----------|--------|-------------|
| Submit KYC Verification | POST | Submit new verification (saves `verification_id`) |
| Get KYC by ID | GET | Get verification details + AML results |
| Get KYC by User | GET | All verifications for a user |
| Update KYC Status | PUT | Approve/Reject/Expire verification |
| Get Expired KYC | GET | All expired verifications |
| Get KYC by Status | GET | Filter by PENDING/APPROVED/REJECTED |
| Count KYC by Status | GET | Get count statistics |
| Check Approved KYC | GET | Check if user has approved KYC |
| Delete KYC | DELETE | Remove verification |

### Folder 2: Questionnaire Management (10 endpoints)
| Endpoint | Method | Description |
|----------|--------|-------------|
| Get All Questions | GET | Paginated list (page, size params) |
| Get Question by ID | GET | Single question details |
| Get Active Questions | GET | Only ACTIVE status questions |
| Get Questions by Category | GET | Filter by PERSONAL_INFO, FINANCIAL, etc. |
| Create Question | POST | Add new question (saves `question_id`) |
| Update Question | PUT | Modify question text/category/order |
| Activate Question | PATCH | Set status to ACTIVE |
| Inactivate Question | PATCH | Set status to INACTIVE |
| Delete Question | DELETE | Archive question (ARCHIVED status) |
| Count Active Questions | GET | Total active questions |

### Folder 3: Customer Answers (11 endpoints)
| Endpoint | Method | Description |
|----------|--------|-------------|
| Submit Answer | POST | Submit answer for question (saves `answer_id`) |
| Get Answer by ID | GET | Single answer details |
| Get All Answers for User | GET | All answers by user |
| Get Completed Answers | GET | Only non-empty answers |
| Get Answer by User+Question | GET | Specific user-question answer |
| Check if Answered | GET | Boolean check if answered |
| Update Answer | PUT | Modify existing answer |
| Delete Answer | DELETE | Remove single answer |
| Delete All User Answers | DELETE | Remove all answers for user |
| Count Answers for User | GET | Total answer count |
| Get Completion Rate | GET | Percentage of completed answers |

### Folder 4: Health Check
- **Health Check** - Verify application is running

---

## Usage Examples

### Example 1: Complete KYC Flow
1. **Login** → Get JWT token
2. **Submit KYC Verification** → Get `verification_id`
3. **Get KYC by ID** → View pending verification
4. **Update KYC Status** → Approve with risk level
5. **Get KYC by ID** → Verify status changed to APPROVED

### Example 2: Questionnaire Management
1. **Login** → Get JWT token
2. **Get Active Questions** → See available questions
3. **Create Question** → Add new FINANCIAL question
4. **Activate Question** → Make it live
5. **Get Questions by Category** → Verify FINANCIAL category

### Example 3: User Answer Submission
1. **Login** → Get JWT token
2. **Get Active Questions** → Get `question_id` from response
3. **Submit Answer** → Provide answer text
4. **Get All Answers for User** → See submitted answers
5. **Get Completion Rate** → Check progress (e.g., /completion-rate/10 for 10 questions)

---

## Variables Reference

| Variable | Type | Description | Auto-saved? |
|----------|------|-------------|-------------|
| `base_url` | string | API base URL (default: localhost:8080) | No |
| `jwt_token` | string | JWT authentication token | ✅ Yes (on login) |
| `user_id` | number | User identifier (default: 1) | No |
| `verification_id` | number | KYC verification ID | ✅ Yes (on submit) |
| `question_id` | number | Question identifier | ✅ Yes (on create) |
| `answer_id` | number | Answer identifier | ✅ Yes (on submit) |

**Auto-saved variables** are automatically populated when you run specific requests.

---

## Request Body Templates

### KYC Verification Submission
```json
{
  "userId": 1,
  "verificationLevel": "BASIC",
  "sumsubApplicantId": "SUMSUB_12345"
}
```

**Verification Levels:** `BASIC`, `ENHANCED`, `FULL`

### KYC Status Update
```json
{
  "status": "APPROVED",
  "riskLevel": "LOW",
  "reviewResult": {
    "reviewerId": 100,
    "comments": "Documents verified successfully",
    "approvalReason": "All checks passed"
  },
  "reviewedById": 100
}
```

**Status Options:** `PENDING`, `APPROVED`, `REJECTED`, `EXPIRED`  
**Risk Levels:** `LOW`, `MEDIUM`, `HIGH`

### Question Creation
```json
{
  "questionText": "What is your annual income?",
  "questionCategory": "FINANCIAL",
  "displayOrder": 10
}
```

**Categories:** `PERSONAL_INFO`, `FINANCIAL`, `EMPLOYMENT`, `IDENTIFICATION`, `RISK_ASSESSMENT`, `COMPLIANCE`

### Answer Submission
```json
{
  "userId": 1,
  "questionId": 5,
  "answerText": "$75,000 per year"
}
```

---

## Response Examples

### KYC Verification Response
```json
{
  "verificationId": 1,
  "userId": 1,
  "sumsubApplicantId": "SUMSUB_12345",
  "verificationLevel": "BASIC",
  "status": "APPROVED",
  "riskLevel": "LOW",
  "reviewResult": {
    "reviewerId": 100,
    "comments": "Documents verified",
    "approvalReason": "All checks passed"
  },
  "submittedAt": "2026-02-03T10:00:00Z",
  "reviewedAt": "2026-02-03T11:00:00Z",
  "approvedAt": "2026-02-03T11:00:00Z",
  "expiresAt": "2027-02-03T11:00:00Z"
}
```

### Question Response
```json
{
  "questionId": 5,
  "questionText": "What is your annual income?",
  "questionCategory": "FINANCIAL",
  "displayOrder": 10,
  "status": "ACTIVE",
  "createdBy": 100
}
```

### Answer Response
```json
{
  "answerId": 25,
  "userId": 1,
  "questionId": 5,
  "answerText": "$75,000 per year",
  "answeredAt": "2026-02-03T12:00:00Z"
}
```

### Paginated Response
```json
{
  "content": [ /* array of items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "last": false,
  "first": true
}
```

---

## Testing Scenarios

### Scenario 1: New User KYC Journey
```
1. Login as new user
2. Submit BASIC KYC verification
3. Check verification status (should be PENDING)
4. Admin updates to APPROVED with LOW risk
5. Verify user has approved KYC
6. Check expiration date (should be 1 year from approval)
```

### Scenario 2: Questionnaire Lifecycle
```
1. Admin creates question (INACTIVE by default)
2. Admin activates question
3. Get active questions (new question appears)
4. User submits answer
5. Check completion rate
6. Admin inactivates question
7. Get active questions (question no longer appears)
```

### Scenario 3: Bulk Answer Submission
```
1. Get all active questions
2. Submit answers for each question
3. Get all answers for user (verify all saved)
4. Calculate completion rate (/completion-rate/{totalQuestions})
5. Update one answer (change text)
6. Get completed answers (verify update)
```

---

## Troubleshooting

### Issue: "401 Unauthorized"
**Solution:**
1. Run **"Login (Get JWT Token)"** request first
2. Check that `{{jwt_token}}` variable is set
3. Token expires after 1 hour (login again)

### Issue: "404 Not Found"
**Solution:**
1. Verify application is running: `http://localhost:8080/actuator/health`
2. Check `base_url` variable is correct
3. Ensure you're using correct endpoint paths

### Issue: "400 Bad Request"
**Solution:**
1. Verify request body JSON is valid
2. Check required fields are present
3. Ensure data types match (numbers not strings for IDs)
4. Review validation constraints (e.g., questionText max length)

### Issue: Variables not auto-saving
**Solution:**
1. Check "Test" tab in request (should have auto-save script)
2. Manually set variable: Collection → Variables → Update value
3. Verify response status is 200/201

---

## Performance Testing

### Load Test Parameters
```
Concurrent Users: 10-100
Ramp-up Time: 30s
Duration: 5 min
Think Time: 1-3s between requests
```

### Expected Response Times
- **Authentication:** < 200ms
- **GET requests:** < 100ms
- **POST/PUT requests:** < 300ms
- **Complex queries (with joins):** < 500ms

---

## Next Steps

1. ✅ Import collection into Postman
2. ✅ Run Health Check
3. ✅ Login to get JWT token
4. ✅ Test each folder's requests sequentially
5. ✅ Create test data for your use cases
6. ✅ Export environment variables for team sharing

---

**Total Endpoints in Collection:** 31  
**Authentication Required:** Yes (Bearer Token)  
**Base URL:** http://localhost:8080  
**API Version:** v1  
**Phase:** 2 (KYC & Questionnaire)
