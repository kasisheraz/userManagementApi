# Manual Test Plan - Sumsub KYC Verification

**Document Version**: 1.0  
**Created**: May 11, 2026  
**Test Environment**: NPE (Mock Sumsub)  
**Application**: FinCore User Management Platform

---

## 📋 Overview

This test plan covers the **Sumsub KYC Verification** workflow using mock Sumsub services. The workflow consists of 4 steps:

1. **Step 1**: User Information Validation
2. **Step 2**: Document Verification (Sumsub Integration)
3. **Step 3**: Compliance Questionnaire
4. **Step 4**: Review & Submit

---

## 🎯 Test Objectives

- ✅ Verify Sumsub mock applicant creation
- ✅ Verify document verification workflow with Sumsub
- ✅ Verify admin can simulate approval/rejection
- ✅ Verify complete end-to-end KYC workflow
- ✅ Verify rejection handling and status updates
- ✅ Verify proper error handling and validations

---

## 🔧 Prerequisites

### Environment Setup
- ✅ Backend running with `npe` or `local` profile (mock Sumsub enabled)
- ✅ Frontend running on `http://localhost:3000`
- ✅ Database accessible and populated with test data

### Test Data Required

#### Test User 1: Business User (KYC Applicant)
```
Name: John Smith
Email: john.smith@example.com
Phone: +447700900001
Role: BUSINESS_USER
Password/OTP: Check backend logs
```

#### Test User 2: System Administrator
```
Name: Admin User
Email: admin@fincore.com
Phone: +447700900999
Role: SYSTEM_ADMINISTRATOR
Password/OTP: Check backend logs
```

---

## 📝 Test Cases

### TC-SUMSUB-001: Start KYC Verification Workflow

**Objective**: Verify user can start a new KYC verification and Sumsub applicant is created

**Preconditions**:
- User is logged in as Business User
- User does not have an active KYC verification

**Test Steps**:

1. Navigate to "KYC Verification" page
2. Click "Start KYC Verification" button
3. Select verification level: "FULL"
4. Click "Start" button

**Expected Results**:
- ✅ KYC workflow is created successfully
- ✅ Verification ID is generated
- ✅ Mock Sumsub applicant ID is created (format: `MOCK_XXXXXXXX`)
- ✅ Success message displayed: "KYC verification started successfully"
- ✅ User is redirected to Step 1 of the wizard
- ✅ Workflow status shows:
  - Current Step: 0 (User Information)
  - All steps marked as incomplete

**Test Data**:
```
Verification Level: FULL
Expected Applicant ID Format: MOCK_[8 random chars]
```

**API Verification**:
```
POST /api/kyc/workflow/start?level=FULL
Response:
{
  "verificationId": 1,
  "sumsubApplicantId": "MOCK_A1B2C3D4",
  "steps": {
    "USER_INFORMATION": false,
    "DOCUMENT_VERIFICATION": false,
    "QUESTIONNAIRE": false,
    "REVIEW": false
  },
  "currentStep": 0
}
```

---

### TC-SUMSUB-002: Complete Step 1 - User Information

**Objective**: Verify user information validation

**Preconditions**:
- TC-SUMSUB-001 passed
- User is on Step 1 of KYC wizard

**Test Steps**:

1. Review user information displayed
2. Verify all required fields are populated:
   - First Name
   - Last Name
   - Email
   - Phone Number
   - Date of Birth
3. Click "Next" button

**Expected Results**:
- ✅ User information is validated
- ✅ Step 1 is marked as complete
- ✅ User advances to Step 2 (Document Verification)
- ✅ Success message: "User information validated successfully"
- ✅ Stepper shows Step 1 as completed (green checkmark)

**Validation Messages**:
- If missing fields: "Please complete your profile before proceeding"
- If successful: "User information validated successfully"

---

### TC-SUMSUB-003: Mock Document Upload (Step 2)

**Objective**: Verify mock document upload simulation

**Preconditions**:
- TC-SUMSUB-002 passed
- User is on Step 2 of KYC wizard

**Test Steps**:

1. Verify "Document Verification" page is displayed
2. Verify message: "In production, this would integrate with SumSub for real document verification"
3. Click "Simulate Document Upload" button
4. Wait for simulation to complete

**Expected Results**:
- ✅ Mock applicant ID is generated (format: `MOCK_XXXXXXXX`)
- ✅ Document upload success message displayed
- ✅ Green checkmark icon appears
- ✅ Applicant ID is displayed below success message
- ✅ "Next" button becomes enabled
- ✅ Success message: "Document verified successfully!"

**UI Verification**:
```
Alert displayed:
✓ Document verified successfully!
  Applicant ID: MOCK_A1B2C3D4
```

---

### TC-SUMSUB-004: Complete Step 2 - Document Verification

**Objective**: Verify Step 2 completion with Sumsub applicant ID

**Preconditions**:
- TC-SUMSUB-003 passed
- Mock document uploaded successfully

**Test Steps**:

1. Verify Sumsub applicant ID is displayed
2. Click "Next" button
3. Wait for Step 2 to complete

**Expected Results**:
- ✅ Step 2 completion API called with Sumsub applicant ID
- ✅ Step 2 marked as complete in workflow status
- ✅ User advances to Step 3 (Questionnaire)
- ✅ Success message: "Document verification completed successfully"
- ✅ Stepper shows Step 2 as completed

**API Verification**:
```
POST /api/kyc/workflow/{verificationId}/step2/sumsub
Request Body:
{
  "sumsubApplicantId": "MOCK_A1B2C3D4"
}

Response:
{
  "steps": {
    "USER_INFORMATION": true,
    "DOCUMENT_VERIFICATION": true,
    "QUESTIONNAIRE": false,
    "REVIEW": false
  },
  "currentStep": 2
}
```

---

### TC-SUMSUB-005: Admin Simulates Sumsub Approval

**Objective**: Verify admin can simulate Sumsub verification approval

**Preconditions**:
- TC-SUMSUB-004 passed
- Admin is logged in
- Admin has Sumsub applicant ID

**Test Steps**:

1. Admin navigates to Mock Sumsub API (via Postman or Swagger)
2. Admin calls simulate approval endpoint:
   ```
   POST /api/mock/sumsub/applicants/{applicantId}/simulate-approval
   ```
3. Verify response

**Expected Results**:
- ✅ Sumsub applicant status updated to "completed"
- ✅ Review result set to "GREEN" (approved)
- ✅ Response shows mock: true
- ✅ Backend logs show: "🎭 MOCK: Simulating approval for applicant {id}"

**API Response**:
```json
{
  "applicantId": "MOCK_A1B2C3D4",
  "reviewStatus": "completed",
  "reviewResult": "GREEN",
  "rejectLabels": null,
  "updatedAt": "2026-05-11T14:30:00",
  "mock": true
}
```

**Backend Log Verification**:
```
[INFO] 🎭 MOCK: Simulating approval for applicant MOCK_A1B2C3D4
[INFO] ✅ MOCK: Verification completed for MOCK_A1B2C3D4 - result: GREEN
```

---

### TC-SUMSUB-006: Check Sumsub Status After Approval

**Objective**: Verify Sumsub status can be retrieved after approval

**Preconditions**:
- TC-SUMSUB-005 passed
- Sumsub verification approved

**Test Steps**:

1. Call Sumsub status endpoint:
   ```
   GET /api/mock/sumsub/applicants/{applicantId}/status
   ```
2. Verify response

**Expected Results**:
- ✅ Status endpoint returns current status
- ✅ Review status: "completed"
- ✅ Review result: "GREEN"
- ✅ No reject labels
- ✅ Updated timestamp present
- ✅ Mock flag: true

**API Response**:
```json
{
  "applicantId": "MOCK_A1B2C3D4",
  "reviewStatus": "completed",
  "reviewResult": "GREEN",
  "rejectLabels": null,
  "updatedAt": "2026-05-11T14:30:00",
  "mock": true
}
```

---

### TC-SUMSUB-007: Complete Step 3 - Questionnaire

**Objective**: Verify compliance questionnaire can be completed

**Preconditions**:
- TC-SUMSUB-004 passed
- User is on Step 3 of KYC wizard

**Test Steps**:

1. Answer all compliance questions:
   - Question 1: "What is your source of income?"
     - Answer: "Employment income"
   - Question 2: "Are you a politically exposed person?"
     - Answer: "No"
   - Question 3: "Purpose of account"
     - Answer: "Personal savings and investment"
2. Click "Next" button

**Expected Results**:
- ✅ All questions must be answered (validation)
- ✅ Step 3 marked as complete
- ✅ User advances to Step 4 (Review)
- ✅ Success message: "Questionnaire completed successfully"
- ✅ Stepper shows Step 3 as completed

**Validation**:
- If unanswered questions: "Please answer all questions before proceeding"

---

### TC-SUMSUB-008: Complete Step 4 - Review & Submit

**Objective**: Verify final review and submission

**Preconditions**:
- TC-SUMSUB-007 passed
- All previous steps completed

**Test Steps**:

1. Review all submitted information:
   - User information
   - Document verification status (Sumsub)
   - Questionnaire answers
2. Click "Submit for Review" button
3. Confirm submission

**Expected Results**:
- ✅ Step 4 marked as complete
- ✅ All workflow steps completed
- ✅ Success message: "Verification submitted for review!"
- ✅ User redirected to status page after 2 seconds
- ✅ Workflow status shows all steps complete
- ✅ Verification status: "PENDING_REVIEW" or "UNDER_REVIEW"

**Workflow Status Verification**:
```json
{
  "verificationId": 1,
  "steps": {
    "USER_INFORMATION": true,
    "DOCUMENT_VERIFICATION": true,
    "QUESTIONNAIRE": true,
    "REVIEW": true
  },
  "allStepsCompleted": true,
  "currentStep": 4
}
```

---

### TC-SUMSUB-009: Rejection Scenario

**Objective**: Verify admin can simulate Sumsub rejection

**Preconditions**:
- New KYC verification started (repeat TC-SUMSUB-001 to TC-SUMSUB-004)
- Admin is logged in

**Test Steps**:

1. Admin calls simulate rejection endpoint:
   ```
   POST /api/mock/sumsub/applicants/{applicantId}/simulate-rejection
   ```
2. Verify response
3. Check Sumsub status

**Expected Results**:
- ✅ Sumsub applicant status updated to "completed"
- ✅ Review result set to "RED" (rejected)
- ✅ Reject labels array contains: ["DOCUMENT_MISMATCH"]
- ✅ Response shows mock: true
- ✅ Backend logs show rejection

**API Response**:
```json
{
  "applicantId": "MOCK_B3C4D5E6",
  "reviewStatus": "completed",
  "reviewResult": "RED",
  "rejectLabels": ["DOCUMENT_MISMATCH"],
  "updatedAt": "2026-05-11T15:00:00",
  "mock": true
}
```

**Backend Log Verification**:
```
[INFO] 🎭 MOCK: Simulating rejection for applicant MOCK_B3C4D5E6
[INFO] ✅ MOCK: Verification completed for MOCK_B3C4D5E6 - result: RED
```

---

### TC-SUMSUB-010: Verify Rejection Status

**Objective**: Verify rejection status is properly reflected

**Preconditions**:
- TC-SUMSUB-009 passed
- Sumsub verification rejected

**Test Steps**:

1. Call Sumsub status endpoint:
   ```
   GET /api/mock/sumsub/applicants/{applicantId}/status
   ```
2. Verify rejection details

**Expected Results**:
- ✅ Review status: "completed"
- ✅ Review result: "RED"
- ✅ Reject labels: ["DOCUMENT_MISMATCH"]
- ✅ Updated timestamp present
- ✅ Mock flag: true

**User Experience**:
- User should see rejection message in UI
- User may need to restart KYC process or upload new documents (depends on business logic)

---

### TC-SUMSUB-011: Invalid Applicant ID Handling

**Objective**: Verify proper error handling for invalid applicant ID

**Preconditions**:
- None

**Test Steps**:

1. Attempt to complete Step 2 with invalid applicant ID:
   ```
   POST /api/kyc/workflow/{verificationId}/step2/sumsub
   Body: { "sumsubApplicantId": "" }
   ```
2. Verify error response

**Expected Results**:
- ✅ HTTP 400 Bad Request
- ✅ Error message: "sumsubApplicantId is required"
- ✅ Step 2 remains incomplete
- ✅ User stays on Step 2

**Error Response**:
```json
{
  "error": "sumsubApplicantId is required"
}
```

---

### TC-SUMSUB-012: Unauthorized Access to Simulation Endpoints

**Objective**: Verify only admins can access simulation endpoints

**Preconditions**:
- Business user is logged in (non-admin)

**Test Steps**:

1. Business user attempts to call:
   ```
   POST /api/mock/sumsub/applicants/{applicantId}/simulate-approval
   ```
2. Verify access denied

**Expected Results**:
- ✅ HTTP 403 Forbidden
- ✅ Error message: "Access denied" or similar
- ✅ Simulation does not execute
- ✅ Applicant status unchanged

**Security Verification**:
- Only ADMIN and SUPER_ADMIN roles can access simulation endpoints
- Business users cannot manipulate Sumsub verification results

---

### TC-SUMSUB-013: Complete End-to-End Happy Path

**Objective**: Verify complete KYC workflow from start to finish with Sumsub

**Preconditions**:
- Fresh test environment
- Business user logged in
- Admin logged in (separate session)

**Test Steps**:

1. **User**: Start KYC verification (TC-SUMSUB-001)
2. **User**: Complete Step 1 (TC-SUMSUB-002)
3. **User**: Simulate document upload (TC-SUMSUB-003)
4. **User**: Complete Step 2 (TC-SUMSUB-004)
5. **Admin**: Simulate Sumsub approval (TC-SUMSUB-005)
6. **User**: Verify Sumsub status (TC-SUMSUB-006)
7. **User**: Complete Step 3 (TC-SUMSUB-007)
8. **User**: Complete Step 4 (TC-SUMSUB-008)
9. **User**: View final verification status

**Expected Results**:
- ✅ All steps complete successfully
- ✅ No errors or warnings
- ✅ Workflow status: All steps marked complete
- ✅ Sumsub applicant: Approved (GREEN)
- ✅ Verification status: Submitted for admin review
- ✅ User can view complete verification details
- ✅ All API responses correct
- ✅ UI updates properly at each step

**Final Status Check**:
```
Verification ID: [number]
Sumsub Applicant ID: MOCK_XXXXXXXX
Verification Status: PENDING_REVIEW or UNDER_REVIEW
Sumsub Status: completed / GREEN
All Steps Completed: true
```

---

## 🎯 Test Data Summary

### Scenario 1: Approval Workflow

```json
{
  "user": {
    "name": "John Smith",
    "phone": "+447700900001",
    "email": "john.smith@example.com",
    "role": "BUSINESS_USER"
  },
  "verification": {
    "level": "FULL",
    "expectedApplicantIdFormat": "MOCK_[8 chars]"
  },
  "questionnaire": {
    "answers": [
      {
        "question": "Source of income",
        "answer": "Employment income"
      },
      {
        "question": "Politically exposed person",
        "answer": "No"
      },
      {
        "question": "Purpose of account",
        "answer": "Personal savings and investment"
      }
    ]
  },
  "expectedSumsubStatus": {
    "reviewStatus": "completed",
    "reviewResult": "GREEN"
  }
}
```

### Scenario 2: Rejection Workflow

```json
{
  "user": {
    "name": "Jane Doe",
    "phone": "+447700900002",
    "email": "jane.doe@example.com",
    "role": "BUSINESS_USER"
  },
  "verification": {
    "level": "FULL",
    "expectedApplicantIdFormat": "MOCK_[8 chars]"
  },
  "expectedSumsubStatus": {
    "reviewStatus": "completed",
    "reviewResult": "RED",
    "rejectLabels": ["DOCUMENT_MISMATCH"]
  }
}
```

---

## 📊 Test Execution Tracking

| Test Case ID | Test Case Name | Status | Pass/Fail | Tester | Date | Comments |
|--------------|----------------|--------|-----------|--------|------|----------|
| TC-SUMSUB-001 | Start KYC Workflow | | | | | |
| TC-SUMSUB-002 | Complete Step 1 | | | | | |
| TC-SUMSUB-003 | Mock Document Upload | | | | | |
| TC-SUMSUB-004 | Complete Step 2 | | | | | |
| TC-SUMSUB-005 | Admin Simulate Approval | | | | | |
| TC-SUMSUB-006 | Check Status After Approval | | | | | |
| TC-SUMSUB-007 | Complete Step 3 | | | | | |
| TC-SUMSUB-008 | Complete Step 4 | | | | | |
| TC-SUMSUB-009 | Rejection Scenario | | | | | |
| TC-SUMSUB-010 | Verify Rejection Status | | | | | |
| TC-SUMSUB-011 | Invalid Applicant ID | | | | | |
| TC-SUMSUB-012 | Unauthorized Access | | | | | |
| TC-SUMSUB-013 | End-to-End Happy Path | | | | | |

---

## 🐛 Bug Reporting Template

When a test fails, use this template to report bugs:

```
Bug ID: SUMSUB-BUG-XXX
Severity: [Critical / High / Medium / Low]
Test Case: TC-SUMSUB-XXX
Summary: [One-line description]

Steps to Reproduce:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected Result:
[What should happen]

Actual Result:
[What actually happened]

Environment:
- Browser: [Chrome/Firefox/Safari]
- Backend URL: [URL]
- Frontend URL: [URL]
- Profile: [npe/local]

Screenshots/Logs:
[Attach relevant screenshots or log excerpts]

Additional Notes:
[Any other relevant information]
```

---

## ✅ Acceptance Criteria

All test cases must pass with the following criteria:

1. ✅ KYC workflow can be started successfully
2. ✅ Mock Sumsub applicant is created with correct format
3. ✅ All 4 workflow steps can be completed
4. ✅ Admin can simulate approval/rejection
5. ✅ Sumsub status reflects approval/rejection correctly
6. ✅ Rejection scenario works properly
7. ✅ Error handling is proper for invalid inputs
8. ✅ Security: Only admins can access simulation endpoints
9. ✅ End-to-end workflow completes without errors
10. ✅ All API responses match expected format
11. ✅ UI updates correctly at each step
12. ✅ Backend logs show proper mock indicators (🎭 emoji)

---

## 📚 Reference Documents

- **Mock Sumsub Guide**: `MOCK_SUMSUB_GUIDE.md`
- **API Documentation**: Swagger UI at `/swagger-ui.html`
- **Postman Collection**: `postman-sumsub-kyc-verification.json`
- **Architecture Documentation**: `ARCHITECTURE.md`
- **KYC Infrastructure Analysis**: `KYC_EXISTING_INFRASTRUCTURE_ANALYSIS.md`

---

## 🔍 Notes

### Mock vs. Real Sumsub

This test plan uses **mock Sumsub service** which:
- ✅ Simulates all Sumsub behavior
- ✅ No external API calls
- ✅ Admin can control approval/rejection
- ✅ Stores applicants in memory (not persistent)
- ✅ Always returns mock: true in responses
- ✅ Uses MOCK_ prefix for applicant IDs

### When to Use Real Sumsub

Switch to real Sumsub when:
- Corporate email credentials available
- Sumsub account created
- Production deployment
- Change Spring profile from 'npe' to 'production'
- Update configuration with real Sumsub API keys

### Environment Profiles

- **local/npe**: Uses mock Sumsub
- **uat**: Can use mock or real (configure via application.yml)
- **production**: Must use real Sumsub

---

**Document Version**: 1.0  
**Last Updated**: May 11, 2026  
**Prepared By**: FinCore QA Team  
**Status**: Ready for Testing
