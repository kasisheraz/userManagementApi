# Customer KYC Verification Feature - Implementation Plan

## 📋 Overview
Implementation plan for a comprehensive customer KYC verification system with SumSub integration, AML screening, and questionnaire-based compliance checks.

## 🔧 SQL Schema Fixes

### Issues Found in Provided SQL:
1. ❌ **Missing commas** between column definitions
2. ❌ **Incomplete column definitions** (e.g., `Status_Description Varchar` missing length)
3. ❌ **Wrong foreign key reference** - `Customer_KYC_Verification` references `User_Identifier` but should be `Customer_Identifier`
4. ❌ **Inconsistent naming** - mixing `Customer_Identifier` with `User_Identifier`
5. ❌ **Table names case mismatch** - Using capital letters while JPA expects lowercase

### ✅ Corrected SQL Schema:

```sql
-- ============================================
-- Customer KYC Verification Tables (Phase 3)
-- ============================================

-- Customer KYC verification table
CREATE TABLE customer_kyc_verification (
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_identifier INT NOT NULL,
    sumsub_applicant_id VARCHAR(100) UNIQUE,
    verification_level VARCHAR(50),
    status_description VARCHAR(50),
    reason_description VARCHAR(100),
    review_result JSON,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH'),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    approved_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by INT,
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_status (status_description),
    INDEX idx_sumsub_applicant (sumsub_applicant_id),
    CONSTRAINT fk_customer_id_kyc FOREIGN KEY (customer_identifier) REFERENCES users(User_Identifier)
);

-- AML screening results
CREATE TABLE aml_screening_results (
    screening_id INT AUTO_INCREMENT PRIMARY KEY,
    verification_id INT NOT NULL,
    customer_identifier INT NOT NULL,
    screening_type ENUM('PEP', 'SANCTIONS', 'ADVERSE_MEDIA') NOT NULL,
    match_found BOOLEAN DEFAULT FALSE,
    match_details JSON,
    risk_score INT,
    screened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_verification_id (verification_id),
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_screening_type (screening_type),
    CONSTRAINT fk_verif_id FOREIGN KEY (verification_id) REFERENCES customer_kyc_verification(verification_id) ON DELETE CASCADE
);

-- Questions table
CREATE TABLE questionnaire_questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_category VARCHAR(50),
    display_order INT,
    status_description VARCHAR(20) DEFAULT 'ACTIVE',
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by INT,
    INDEX idx_status (status_description),
    INDEX idx_category (question_category),
    INDEX idx_display_order (display_order)
);

-- Customer answers table
CREATE TABLE customer_answers (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_identifier INT NOT NULL,
    question_id INT NOT NULL,
    answer ENUM('YES', 'NO') NOT NULL,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_identifier INT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by_user_identifier INT,
    INDEX idx_customer_id (customer_identifier),
    INDEX idx_question_id (question_id),
    CONSTRAINT fk_customer_id_ans FOREIGN KEY (customer_identifier) REFERENCES users(User_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_qst_id FOREIGN KEY (question_id) REFERENCES questionnaire_questions(question_id)
);
```

## 🔍 Current Implementation Status

### ✅ Already Implemented (Existing Code):
1. **Backend Entities**:
   - ✅ `CustomerKycVerification.java`
   - ✅ `AmlScreeningResult.java`
   - ✅ `QuestionnaireQuestion.java`
   - ✅ `CustomerAnswer.java`

2. **Backend Services**:
   - ✅ `KycVerificationService.java` - Basic CRUD operations
   - ✅ `KycDocumentService.java` - Document management

3. **Backend Controllers**:
   - ✅ `KycVerificationController.java` - REST endpoints
   - ✅ `KycDocumentController.java` - Document upload/download

4. **Frontend Structure**:
   - ✅ `/pages/kyc/` directory exists
   - ✅ `/pages/questionnaire/` directory exists

### ❌ Missing / Needs Enhancement:

1. **SumSub Integration** - No integration yet
2. **AML Screening Logic** - No implementation yet
3. **Questionnaire Workflow** - Partial implementation
4. **Frontend UI** - Needs completion
5. **Test Coverage** - Below 80% target
6. **Workflow Orchestration** - No 4-step process flow

## 🎯 Implementation Plan

### Phase 1: Database Schema Updates (1-2 days)

#### Tasks:
1. ✅ Create Flyway migration script with corrected SQL
2. ✅ Update existing entity mappings to match new schema
3. ✅ Add indexes for performance
4. ✅ Run migration on UAT database
5. ✅ Verify schema in all environments

**Deliverables**:
- `V8.0__Add_KYC_Verification_Tables.sql` (Flyway migration)
- Updated entity classes with correct @Column mappings

---

### Phase 2: Identity Verification Provider Research & Selection (2-3 days)

#### Option 1: SumSub (Recommended)
**Pros**:
- ✅ Comprehensive KYC/AML solution
- ✅ Global coverage (220+ countries)
- ✅ Document verification + liveness detection
- ✅ PEP, sanctions, adverse media screening built-in
- ✅ RESTful API + webhooks
- ✅ Customizable verification flows
- ✅ Compliance with GDPR, KYC, AML regulations

**Cons**:
- ❌ Cost: ~$1-5 per verification (volume-based pricing)
- ❌ Requires account setup and integration effort

**Integration Points**:
- REST API: `https://api.sumsub.com/resources/*`
- Webhooks for status updates
- SDK for frontend embedding

#### Option 2: Onfido
**Pros**:
- ✅ Strong document verification
- ✅ Good facial recognition
- ✅ Studio for custom workflows

**Cons**:
- ❌ More expensive than SumSub
- ❌ Complex integration

#### Option 3: Jumio
**Pros**:
- ✅ Fast verification
- ✅ High accuracy

**Cons**:
- ❌ Limited AML screening
- ❌ Expensive

#### Option 4: Custom Build (Not Recommended)
**Cons**:
- ❌ High development cost
- ❌ Compliance risks
- ❌ Maintenance burden
- ❌ No global coverage

**Recommendation**: **SumSub** - Best balance of features, cost, and ease of integration

#### Tasks:
1. ✅ Create SumSub account (sandbox)
2. ✅ Review API documentation
3. ✅ Design integration architecture
4. ✅ Create DTOs for SumSub requests/responses
5. ✅ Implement service layer for SumSub API calls

**Deliverables**:
- `SumSubIntegrationService.java`
- `SumSubWebhookController.java`
- Configuration properties for API keys
- Architecture diagram

---

### Phase 3: Backend API Development (5-7 days)

#### 3.1 Service Layer Enhancements

**New Services**:
1. **`SumSubIntegrationService`**:
   - Create applicant
   - Upload documents
   - Get verification status
   - Handle webhooks
   - Sync verification results

2. **`AmlScreeningService`**:
   - Screen against PEP lists
   - Check sanctions
   - Monitor adverse media
   - Calculate risk score

3. **`QuestionnaireService`**:
   - Get active questions by category
   - Save customer answers
   - Validate completeness
   - Calculate compliance score

4. **`KycWorkflowService`** (Orchestrator):
   - Step 1: Collect user information
   - Step 2: Trigger SumSub verification
   - Step 3: Run AML screening
   - Step 4: Blocklist check (placeholder for future)
   - Track overall progress

#### 3.2 Controller Endpoints

**New Endpoints**:

```java
POST   /api/kyc/start              // Start KYC process
POST   /api/kyc/submit-info        // Submit user information (Step 1)
POST   /api/kyc/sumsub/create      // Create SumSub applicant (Step 2)
GET    /api/kyc/sumsub/status      // Get SumSub verification status
POST   /api/kyc/webhooks/sumsub    // SumSub webhook receiver
POST   /api/kyc/aml/screen         // Trigger AML screening (Step 3)
GET    /api/kyc/progress/:id       // Get KYC progress status
GET    /api/kyc/verifications      // List all verifications (admin)
PUT    /api/kyc/verifications/:id  // Update verification status

// Questionnaire endpoints
GET    /api/questionnaire/questions          // Get questions by category
POST   /api/questionnaire/answers            // Submit answers
GET    /api/questionnaire/progress/:userId   // Get completion status
```

#### 3.3 DTO Layer

**Required DTOs**:
- `KycStartRequestDTO` - Start KYC process
- `KycProgressResponseDTO` - Current step and status
- `SumSubApplicantRequestDTO` - Create applicant
- `SumSubApplicantResponseDTO` - Applicant details
- `SumSubWebhookPayloadDTO` - Webhook data
- `AmlScreeningRequestDTO` - Screening parameters
- `AmlScreeningResultDTO` - Screening results
- `QuestionnaireResponseDTO` - Questions with answers

#### Tasks:
1. ✅ Implement SumSub integration service
2. ✅ Implement AML screening service
3. ✅ Enhance questionnaire service
4. ✅ Create workflow orchestrator
5. ✅ Add all DTOs and mappers
6. ✅ Implement REST controllers
7. ✅ Add validation and error handling
8. ✅ Configure security (role-based access)

**Deliverables**:
- 7 new service classes
- 4 new controllers
- 15+ DTOs
- Unit tests for all services

---

### Phase 4: Frontend UI Development (7-10 days)

#### 4.1 KYC Workflow UI

**Pages to Create**:

1. **`KycStart.tsx`** - Landing page
   - Explain KYC process
   - Display 4 steps
   - Start button

2. **`KycStep1UserInfo.tsx`** - User Information
   - Form: Name, DOB, Address, ID number
   - Validation
   - Progress indicator (Step 1/4)

3. **`KycStep2Sumsub.tsx`** - Document Verification
   - Embed SumSub SDK
   - Upload documents (ID, proof of address)
   - Liveness check
   - Progress indicator (Step 2/4)

4. **`KycStep3Questionnaire.tsx`** - Compliance Questions
   - Display questions by category
   - YES/NO answers
   - Progress tracker
   - Save button

5. **`KycStep4Review.tsx`** - Review & Submit
   - Summary of all information
   - Terms & conditions
   - Submit button
   - Status: Processing, Approved, Rejected

6. **`KycStatus.tsx`** - Verification Status
   - Current step indicator
   - Status badge (Pending, Under Review, Approved, Rejected)
   - Estimated time
   - Next actions

7. **`KycAdmin.tsx`** - Admin Dashboard
   - List all verifications
   - Filter by status
   - Approve/Reject buttons
   - AML screening results
   - Review details modal

#### 4.2 Components

**Reusable Components**:
- `KycProgressStepper` - 4-step progress indicator
- `DocumentUpload` - Drag & drop upload
- `QuestionCard` - Display question with YES/NO buttons
- `VerificationStatusBadge` - Color-coded status
- `AmlResultsTable` - Display screening results
- `RiskLevelIndicator` - Visual risk level

#### 4.3 Services (Frontend)

**API Services**:
```typescript
// src/services/kycService.ts
export const kycService = {
  startKyc: () => Promise<KycResponse>,
  submitUserInfo: (data) => Promise<void>,
  getSumsubToken: () => Promise<string>,
  checkProgress: (id) => Promise<KycProgress>,
  getVerificationStatus: (id) => Promise<VerificationStatus>,
  submitQuestionnaire: (answers) => Promise<void>,
  // Admin
  getAllVerifications: () => Promise<Verification[]>,
  approveVerification: (id) => Promise<void>,
  rejectVerification: (id, reason) => Promise<void>
};
```

#### Tasks:
1. ✅ Create all page components
2. ✅ Implement step navigation
3. ✅ Integrate SumSub SDK
4. ✅ Add form validation
5. ✅ Connect to backend APIs
6. ✅ Add loading states
7. ✅ Add error handling
8. ✅ Implement admin dashboard
9. ✅ Add responsive design
10. ✅ Add accessibility features

**Deliverables**:
- 7 new pages
- 6 reusable components
- Frontend API service
- State management (Context/Redux)

---

### Phase 5: Testing & Quality Assurance (5-7 days)

#### 5.1 Backend Testing

**Unit Tests** (Target: 80%+ coverage):
- All service methods
- Controller endpoints
- DTO validations
- Mapper functions
- Utility classes

**Integration Tests**:
- Database operations
- Service layer integration
- Controller integration
- Repository layer

**E2E Tests**:
- Complete KYC workflow
- SumSub integration (mocked)
- AML screening flow
- Questionnaire submission

#### 5.2 Frontend Testing

**Unit Tests**:
- Component rendering
- Form validation
- Button clicks
- State management

**Integration Tests**:
- API calls
- Navigation flow
- Data submission

**E2E Tests** (Playwright):
```typescript
// kyc-workflow.spec.ts
test('Complete KYC workflow', async ({ page }) => {
  // 1. Start KYC
  await page.goto('/kyc/start');
  await page.click('button:has-text("Start Verification")');
  
  // 2. Submit user info
  await page.fill('input[name="firstName"]', 'Test');
  await page.fill('input[name="lastName"]', 'User');
  await page.click('button:has-text("Next")');
  
  // 3. SumSub verification (mocked)
  await page.waitForSelector('.sumsub-iframe');
  // Mock SumSub completion
  
  // 4. Answer questionnaire
  await page.click('button[data-answer="YES"]');
  await page.click('button:has-text("Submit")');
  
  // 5. Verify completion
  await expect(page.locator('.status-badge')).toHaveText('Under Review');
});
```

#### 5.3 Manual Testing Checklist

- [ ] User can start KYC process
- [ ] Form validation works
- [ ] SumSub SDK loads correctly
- [ ] Documents uploaded successfully
- [ ] Questionnaire saves answers
- [ ] Progress indicator updates
- [ ] Status changes reflect in UI
- [ ] Admin can approve/reject
- [ ] AML screening runs automatically
- [ ] Webhook receives SumSub updates
- [ ] Email notifications sent
- [ ] Error handling works
- [ ] Loading states display
- [ ] Mobile responsive

#### Tasks:
1. ✅ Write unit tests (80%+ coverage)
2. ✅ Write integration tests
3. ✅ Write E2E tests
4. ✅ Perform load testing
5. ✅ Security testing
6. ✅ Manual QA testing
7. ✅ Fix bugs
8. ✅ Code review

**Deliverables**:
- Test suite with 80%+ coverage
- Test reports
- Bug fix commits

---

### Phase 6: Deployment & Documentation (2-3 days)

#### 6.1 Deployment

**UAT Deployment**:
1. Run Flyway migrations
2. Deploy backend API
3. Deploy frontend
4. Configure SumSub API keys
5. Smoke tests

**Production Deployment** (After UAT approval):
1. Final testing in UAT
2. Deploy to production
3. Monitor logs
4. Verify all integrations

#### 6.2 Documentation

**Technical Documentation**:
- API documentation (Swagger)
- Database schema diagram
- Architecture diagram
- Integration guide (SumSub)
- Webhook setup guide

**User Documentation**:
- KYC process guide
- Admin user guide
- Troubleshooting guide

**Developer Documentation**:
- Setup guide
- Testing guide
- Deployment guide

#### Tasks:
1. ✅ Create deployment scripts
2. ✅ Write technical docs
3. ✅ Write user guides
4. ✅ Update Swagger docs
5. ✅ Create runbooks
6. ✅ Deploy to UAT
7. ✅ Deploy to Production

**Deliverables**:
- Deployment scripts
- Complete documentation
- User guides

---

## 📊 Effort Estimation

| Phase | Duration | Complexity |
|-------|----------|------------|
| Phase 1: Database Schema | 1-2 days | Low |
| Phase 2: Provider Research | 2-3 days | Medium |
| Phase 3: Backend API | 5-7 days | High |
| Phase 4: Frontend UI | 7-10 days | High |
| Phase 5: Testing | 5-7 days | High |
| Phase 6: Deployment & Docs | 2-3 days | Medium |
| **Total** | **22-32 days** | **High** |

**Team Requirement**: 2-3 developers (1 backend, 1 frontend, 1 QA)

---

## 🔐 Security Considerations

1. **SumSub API Keys**: Store in environment variables, never in code
2. **Webhook Authentication**: Verify SumSub signature
3. **Data Encryption**: Encrypt sensitive PII at rest
4. **Access Control**: Role-based access (Admin, Compliance, User)
5. **Audit Trail**: Log all verification actions
6. **GDPR Compliance**: Add data retention/deletion policies
7. **Rate Limiting**: Prevent abuse of API endpoints

---

## 📈 Success Metrics

1. ✅ Test coverage ≥ 80%
2. ✅ API response time < 500ms (p95)
3. ✅ UI load time < 2s
4. ✅ SumSub verification success rate > 95%
5. ✅ Zero security vulnerabilities (high/critical)
6. ✅ 100% of critical user journeys covered by E2E tests

---

## 🚀 Next Steps

1. **Immediate**:
   - Review and approve this plan
   - Set up SumSub sandbox account
   - Assign development resources

2. **Week 1**:
   - Phase 1: Database schema updates
   - Phase 2: SumSub integration research

3. **Week 2-3**:
   - Phase 3: Backend API development
   - Start Phase 4: Frontend UI

4. **Week 4-5**:
   - Complete Phase 4: Frontend UI
   - Phase 5: Testing

5. **Week 6**:
   - Final testing
   - UAT deployment
   - Production deployment

---

## 📞 Contact & Support

For questions or clarifications on this implementation plan:
- **Technical Lead**: [Your Name]
- **Product Owner**: [PO Name]
- **Slack Channel**: #kyc-implementation

---

**Document Version**: 1.0  
**Last Updated**: May 8, 2026  
**Status**: Draft - Pending Approval
