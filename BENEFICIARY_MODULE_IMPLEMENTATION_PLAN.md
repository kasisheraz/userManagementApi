# Beneficiary Module - Implementation Plan

**Version**: 1.0.0  
**Date**: June 1, 2026  
**Status**: Pending Approval  
**Module**: Beneficiary Management (Payout Clients)

---

## 📋 Executive Summary

This document provides a comprehensive implementation plan for adding the **Beneficiary Module** to the FinCore platform. Beneficiaries are payout clients/institutions on the receiving end of financial transactions. The module will allow business users to register beneficiaries (including Counter Over Counter option) and submit them for admin approval, similar to the Organization module workflow.

### Key Features
- ✅ Beneficiary registration (name, nickname, contact, country, address)
- ✅ Beneficiary-specific KYC document upload (5 document types)
- ✅ One beneficiary per client (user) business rule
- ✅ Admin approval workflow (submit → review → approve/reject)
- ✅ Role-based access control (users see only their beneficiaries)
- ✅ Document verification with feedback mechanism
- ✅ Full audit trail

---

## 🔍 SQL Schema Review & Corrections

### ❌ Issues Identified in Provided SQL

Your provided SQL has several issues that need correction:

```sql
-- ISSUE 1: Wrong syntax - using {} instead of ()
CREATE Table Beneficiary { ... }  ❌

-- ISSUE 2: Column name mismatch in foreign key
ALTER TABLE Beneficiary ADD CONSTRAINT fk_reg_adrs_id 
FOREIGN KEY (Address) REFERENCES Roles(Address_Identifier);  ❌
-- Problem: Column "Address" doesn't exist, should be "Registered_Address_Identifier"
-- Problem: References "Roles" table but should reference "Address" table

-- ISSUE 3: Column name mismatch in foreign key
ALTER TABLE Beneficiary ADD CONSTRAINT fk_usr_id1 
FOREIGN KEY (Users) REFERENCES Roles(User_Identifier);  ❌
-- Problem: Column "Users" doesn't exist, should be "User_Identifier"
-- Problem: References "Roles" table but should reference "Users" table

-- ISSUE 4: KYC_Documents foreign key error
ALTER TABLE KYC_Documents ADD CONSTRAINT fk_ref_id1 
FOREIGN KEY (Beneficiary) REFERENCES Roles(Beneficiary_Identifier);  ❌
-- Problem: Column "Beneficiary" doesn't exist in KYC_Documents
-- Problem: References "Roles" table but should reference "Beneficiary" table

-- ISSUE 5: Missing columns mentioned in requirements
-- - Country (mentioned in requirements)
-- - Nick_Name (mentioned as "Nick Name")
-- - Created_By, Last_Modified_By (audit trail - consistent with other tables)
-- - Status_Description (all entities have status tracking)
```

### ✅ FINALIZED SQL Schema (All Requirements Approved)

```sql
-- ============================================
-- Beneficiary Module - FINAL Schema
-- Based on ERD + Client Requirements
-- All fields confirmed and approved
-- ============================================

CREATE TABLE Beneficiary (
    -- Primary Key
    Beneficiary_Identifier INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Core Fields (from ERD)
    Beneficiary_Name VARCHAR(100) NOT NULL COMMENT 'Full legal name of beneficiary institution',
    Nick_Name VARCHAR(100) COMMENT 'Friendly/short name for beneficiary', -- ✅ APPROVED
    Business_Name VARCHAR(100) COMMENT 'Business trading name',
    Country VARCHAR(50) NOT NULL COMMENT 'Beneficiary country', -- ✅ APPROVED: Separate field for fast queries
    User_Identifier INT NOT NULL COMMENT 'Owner who created this beneficiary (FK to Users)',
    Registered_Address_Identifier INT COMMENT 'Physical address of beneficiary (FK to Address)',
    
    -- Counter Over Counter Fields -- ✅ APPROVED: Explicit flag
    Is_Counter_Over_Counter BOOLEAN DEFAULT FALSE COMMENT 'True if beneficiary uses C2C collection',
    Collector_Contact_Number VARCHAR(20) COMMENT 'Phone for C2C collection (required if Is_Counter_Over_Counter = true)',
    
    -- Workflow Fields -- ✅ APPROVED
    Status_Description VARCHAR(30) DEFAULT 'PENDING' COMMENT 'PENDING, UNDER_REVIEW, ACTIVE, REJECTED, SUSPENDED',
    Reason_Description VARCHAR(255) COMMENT 'Rejection or status change reason',
    
    -- Audit Fields -- ✅ APPROVED
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_By INT COMMENT 'User who created this beneficiary',
    Last_Modified_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    Last_Modified_By INT COMMENT 'User who last modified this beneficiary',
    
    -- Foreign Keys
    CONSTRAINT fk_beneficiary_user FOREIGN KEY (User_Identifier) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_beneficiary_address FOREIGN KEY (Registered_Address_Identifier) REFERENCES Address(Address_Identifier),
    CONSTRAINT fk_beneficiary_created_by FOREIGN KEY (Created_By) REFERENCES Users(User_Identifier),
    CONSTRAINT fk_beneficiary_modified_by FOREIGN KEY (Last_Modified_By) REFERENCES Users(User_Identifier)
);

-- Indexes for performance
CREATE INDEX idx_beneficiary_user ON Beneficiary(User_Identifier);
CREATE INDEX idx_beneficiary_status ON Beneficiary(Status_Description);
CREATE INDEX idx_beneficiary_name ON Beneficiary(Beneficiary_Name);
CREATE INDEX idx_beneficiary_country ON Beneficiary(Country);
CREATE INDEX idx_beneficiary_c2c ON Beneficiary(Is_Counter_Over_Counter);

-- ============================================
-- Extend KYC_Documents to support Beneficiary
-- ============================================

-- Add new column to KYC_Documents for beneficiary reference
ALTER TABLE KYC_Documents 
ADD COLUMN Beneficiary_Identifier INT COMMENT 'Reference to Beneficiary (if document is for beneficiary)';

-- Add foreign key constraint
ALTER TABLE KYC_Documents 
ADD CONSTRAINT fk_kyc_beneficiary 
FOREIGN KEY (Beneficiary_Identifier) REFERENCES Beneficiary(Beneficiary_Identifier);

-- Add check constraint: only ONE reference type per document
ALTER TABLE KYC_Documents 
ADD CONSTRAINT chk_kyc_reference_type 
CHECK (
  (Reference_Identifier IS NOT NULL AND Beneficiary_Identifier IS NULL) OR 
  (Reference_Identifier IS NULL AND Beneficiary_Identifier IS NOT NULL)
);

-- Add index for performance
CREATE INDEX idx_kyc_beneficiary ON KYC_Documents(Beneficiary_Identifier);

-- NOTE: KYC_Documents now supports BOTH Organisation and Beneficiary
-- Reference_Identifier remains for Organisation (backward compatibility)
-- Beneficiary_Identifier is new for Beneficiary documents
-- Only ONE should be populated per document record (enforced by CHECK constraint)

-- ============================================
-- Document Types for Beneficiary (via enum)
-- ============================================

-- These will be added to DocumentType enum in Java code:
-- - CLIENT_AUTHORISATION_LETTER (Required always)
-- - BENEFICIARY_COMPANY_KYC (Required always)
-- - BENEFICIARY_AGREEMENT (Required always)
-- - COLLECTOR_IDENTIFICATION (Required only if Is_Counter_Over_Counter = true)
-- - OPTIONAL_DOCUMENTATION (Optional)
```

### Key Corrections Made:
1. ✅ Fixed syntax: `()` instead of `{}`
2. ✅ Corrected column names in foreign key constraints
3. ✅ Fixed table references (Address, Users, Beneficiary instead of Roles)
4. ✅ Added missing columns: Country, Nick_Name, Created_By, Last_Modified_By, Status_Description, Reason_Description
5. ✅ Added proper indexes for performance
6. ✅ Made KYC_Documents support both Organisation AND Beneficiary (via new column)
7. ✅ Added comments for clarity

---

## 🏗️ Implementation Architecture

### Database Changes
```
Beneficiary Table (NEW)
├── Core Fields
│   ├── Beneficiary_Identifier (PK)
│   ├── Beneficiary_Name
│   ├── Nick_Name
│   ├── Business_Name
│   ├── Collector_Contact_Number
│   ├── Country
│   └── Registered_Address_Identifier (FK → Address)
├── User Relationship
│   └── User_Identifier (FK → Users) - Owner
├── Status Workflow
│   ├── Status_Description (PENDING, UNDER_REVIEW, ACTIVE, REJECTED)
│   └── Reason_Description (rejection feedback)
└── Audit Trail
    ├── Created_Datetime, Created_By
    └── Last_Modified_Datetime, Last_Modified_By

KYC_Documents (EXTENDED)
├── Existing: Reference_Identifier (FK → Organisation)
└── NEW: Beneficiary_Identifier (FK → Beneficiary)
    └── Only ONE reference should be populated per document
```

### Backend Architecture
```
Backend (Spring Boot)
│
├── Entity Layer
│   ├── Beneficiary.java (NEW)
│   ├── BeneficiaryStatus.java (NEW enum)
│   ├── DocumentType.java (EXTEND with 5 new types)
│   └── KycDocument.java (EXTEND with beneficiary field)
│
├── Repository Layer
│   └── BeneficiaryRepository.java (NEW)
│
├── Service Layer
│   ├── BeneficiaryService.java (NEW)
│   └── KycDocumentService.java (EXTEND for beneficiary docs)
│
├── Controller Layer
│   └── BeneficiaryController.java (NEW) - 15+ endpoints
│
└── DTO Layer
    ├── BeneficiaryDTO.java (NEW)
    ├── BeneficiaryCreateDTO.java (NEW)
    └── BeneficiaryUpdateDTO.java (NEW)
```

### Frontend Architecture
```
Frontend (React TypeScript)
│
├── Pages
│   ├── BeneficiariesPage.tsx (NEW) - List view with search/filter
│   └── BeneficiaryDetailsPage.tsx (NEW) - Create/Edit form
│
├── Components
│   ├── BeneficiaryForm.tsx (NEW) - Multi-section form
│   ├── BeneficiaryKYCUpload.tsx (NEW) - Document upload
│   └── BeneficiaryRejectDialog.tsx (NEW) - Admin rejection UI
│
├── Services
│   └── beneficiaryService.ts (NEW) - API calls
│
└── Types
    └── beneficiary.types.ts (NEW) - TypeScript interfaces
```

---

## 📅 Implementation Phases

### **Phase 1: Database & Backend Foundation** (3-4 days)

#### Task 1.1: Database Migration
- [ ] Create Flyway migration file: `V4.0__Create_Beneficiary_Table.sql`
- [ ] Execute migration on local H2 database
- [ ] Execute migration on NPE Cloud SQL
- [ ] Verify indexes created correctly
- [ ] Test foreign key constraints

**Deliverable**: Database table created with all constraints

#### Task 1.2: Backend Entity Layer
- [ ] Create `Beneficiary.java` entity with JPA annotations
- [ ] Create `BeneficiaryStatus.java` enum (PENDING, UNDER_REVIEW, ACTIVE, REJECTED, SUSPENDED)
- [ ] Extend `DocumentType.java` enum with 5 new beneficiary document types
- [ ] Extend `KycDocument.java` entity with `beneficiaryIdentifier` field
- [ ] Add `@ManyToOne` relationship from KycDocument to Beneficiary
- [ ] Write unit tests for Beneficiary entity

**Deliverable**: JPA entities with proper relationships

#### Task 1.3: Repository Layer
- [ ] Create `BeneficiaryRepository.java` interface extending JpaRepository
- [ ] Add custom query methods:
  - `findByUserId(Long userId)` - Get user's beneficiaries
  - `findByUserIdAndStatus(Long userId, BeneficiaryStatus status)`
  - `findByCountry(String country)`
  - `findByBeneficiaryNameContaining(String name)` - Search
  - `existsByUserId(Long userId)` - Check one-beneficiary-per-user
- [ ] Write repository tests

**Deliverable**: Repository with custom queries tested

#### Task 1.4: Service Layer (Core Business Logic)
- [ ] Create `BeneficiaryService.java` with methods:
  - `createBeneficiary(BeneficiaryCreateDTO)` - Validate one-per-user rule
  - `updateBeneficiary(Long id, BeneficiaryUpdateDTO)`
  - `getBeneficiaryById(Long id)` - With role-based filtering
  - `getAllBeneficiaries(Pageable)` - Business users see only theirs
  - `submitForReview(Long id)` - Change status to UNDER_REVIEW
  - `approveBeneficiary(Long id)` - Admin only, set ACTIVE
  - `rejectBeneficiary(Long id, RejectionDTO)` - Admin only, document-level feedback
  - `searchBeneficiaries(SearchParams)` - Multi-criteria search
  - `deleteBeneficiary(Long id)` - Soft delete or hard delete
- [ ] Implement role-based data filtering (BUSINESS_USER sees only own data)
- [ ] Implement one-beneficiary-per-user validation
- [ ] Write comprehensive unit tests (Mockito)

**Deliverable**: Fully tested service layer with business rules

#### Task 1.5: Extend KYC Document Service
- [ ] Extend `KycDocumentService.java` to handle beneficiary documents:
  - `uploadBeneficiaryDocument(BeneficiaryId, DocumentType, File)`
  - `getBeneficiaryDocuments(BeneficiaryId)`
  - `deleteBeneficiaryDocument(DocumentId)`
  - `verifyBeneficiaryDocument(DocumentId)` - Admin only
  - `rejectBeneficiaryDocument(DocumentId, feedback)` - Admin only
- [ ] Update `createDocument()` to accept beneficiaryId
- [ ] Update GCS file path to include `/beneficiaries/{id}/` folder
- [ ] Write unit tests

**Deliverable**: KYC service supporting beneficiary documents

#### Task 1.6: Controller Layer (REST API)
- [ ] Create `BeneficiaryController.java` with endpoints:

```java
// Beneficiary CRUD
POST   /api/beneficiaries              - Create beneficiary
GET    /api/beneficiaries              - List with pagination/search
GET    /api/beneficiaries/{id}         - Get by ID
PUT    /api/beneficiaries/{id}         - Update beneficiary
DELETE /api/beneficiaries/{id}         - Delete beneficiary

// Workflow
PUT    /api/beneficiaries/{id}/submit  - Submit for review
PUT    /api/beneficiaries/{id}/approve - Approve (admin only)
POST   /api/beneficiaries/{id}/reject  - Reject with feedback (admin only)

// Documents
POST   /api/beneficiaries/{id}/documents              - Upload document
GET    /api/beneficiaries/{id}/documents              - List documents
GET    /api/beneficiaries/{id}/documents/{docId}      - Get document
DELETE /api/beneficiaries/{id}/documents/{docId}      - Delete document
PUT    /api/beneficiaries/{id}/documents/{docId}/verify  - Verify (admin)
POST   /api/beneficiaries/{id}/documents/{docId}/reject - Reject (admin)

// Search
GET    /api/beneficiaries/search      - Advanced search
```

- [ ] Add role-based security annotations (`@PreAuthorize`)
- [ ] Write integration tests for all endpoints
- [ ] Add Swagger/OpenAPI documentation

**Deliverable**: 15+ REST endpoints with security and tests

#### Task 1.7: DTO and Mapping
- [ ] Create DTOs:
  - `BeneficiaryDTO.java` - Response DTO
  - `BeneficiaryCreateDTO.java` - Request for creation
  - `BeneficiaryUpdateDTO.java` - Request for update
  - `BeneficiarySearchDTO.java` - Search parameters
  - `BeneficiaryRejectionDTO.java` - Rejection with document feedback
- [ ] Create `BeneficiaryMapper.java` using MapStruct
- [ ] Write mapper tests

**Deliverable**: Complete DTO layer with mapping

---

### **Phase 2: Frontend Implementation** (4-5 days)

#### Task 2.1: TypeScript Types
- [ ] Create `src/types/beneficiary.types.ts`:
  - `Beneficiary` interface
  - `CreateBeneficiaryDTO` interface
  - `UpdateBeneficiaryDTO` interface
  - `BeneficiaryStatus` enum
  - `BeneficiaryDocument` interface
  - `BeneficiarySearchParams` interface

**Deliverable**: TypeScript type definitions

#### Task 2.2: API Service Layer
- [ ] Create `src/services/beneficiaryService.ts`:
  - `getAll(params)` - List with pagination
  - `getById(id)` - Get beneficiary details
  - `create(data)` - Create beneficiary
  - `update(id, data)` - Update beneficiary
  - `delete(id)` - Delete beneficiary
  - `submitForReview(id)` - Submit workflow
  - `approve(id)` - Admin approve
  - `reject(id, data)` - Admin reject with feedback
  - `uploadDocument(id, file, type)` - Upload document
  - `getDocuments(id)` - List documents
  - `deleteDocument(id, docId)` - Delete document
  - `search(params)` - Search beneficiaries

**Deliverable**: Service layer for API integration

#### Task 2.3: Beneficiaries List Page
- [ ] Create `src/pages/beneficiaries/BeneficiariesPage.tsx`:
  - Data table with columns: Name, Country, Status, Created Date, Actions
  - "Create Beneficiary" button (visible to BUSINESS_USER, disabled if already has one)
  - Search box (search by name, country)
  - Status filter dropdown (ALL, PENDING, UNDER_REVIEW, ACTIVE, REJECTED)
  - Pagination controls
  - "Submit for Review" button (owner only, status = PENDING)
  - "Approve" button (admin only, green checkmark, status = UNDER_REVIEW)
  - "Reject" button (admin only, red X, status = UNDER_REVIEW)
  - Color-coded status badges:
    - PENDING: Gray
    - UNDER_REVIEW: Blue
    - ACTIVE: Green
    - REJECTED: Red
    - REQUIRES_RESUBMISSION: Orange
- [ ] Implement role-based button visibility
- [ ] Add loading states and error handling
- [ ] Responsive design for mobile

**Deliverable**: Beneficiaries list page with search/filter

#### Task 2.4: Beneficiary Form Component
- [ ] Create `src/components/beneficiaries/BeneficiaryForm.tsx`:
  - Form sections:
    1. **Basic Information**
       - Beneficiary Name (required)
       - Nick Name (optional)
       - Business Name (optional)
    2. **Contact Information**
       - Collector Contact Phone (required for Counter Over Counter)
       - Country dropdown (required)
    3. **Address Information**
       - Address Line 1 (required)
       - Address Line 2 (optional)
       - City (required)
       - State/County (optional)
       - Postal Code (required)
       - Country (required)
    4. **KYC Documents** (NEW tab)
       - Document upload interface (see Task 2.5)
  - Form validation (required fields, phone format, postal code format)
  - Save as Draft functionality
  - Cancel button (navigate back)
  - Submit button (validate all sections)
- [ ] Integrate with beneficiaryService
- [ ] Add success/error notifications

**Deliverable**: Multi-section beneficiary form

#### Task 2.5: Beneficiary KYC Upload Component
- [ ] Create `src/components/beneficiaries/BeneficiaryKYCUpload.tsx`:
  - Document type dropdown with 5 types:
    1. Client Authorisation Letter (required)
    2. Beneficiary Company KYC (required)
    3. Beneficiary Agreement (required)
    4. Collector Identification (required for Counter Over Counter)
    5. Optional Documentation (optional)
  - Drag & drop upload zone (reuse FileDropZone component)
  - File browse button
  - File preview (name, size, type)
  - Upload button with progress indicator
  - Documents table displaying:
    - Document Type
    - File Name
    - Upload Date
    - Status badge (PENDING, UNDER_REVIEW, VERIFIED, REJECTED)
    - Admin Feedback (if rejected)
    - Actions (View, Download, Delete)
  - Required documents checklist (visual indicator)
  - Delete confirmation dialog
- [ ] File validation (PDF, JPG, PNG, max 10MB)
- [ ] Error handling for failed uploads

**Deliverable**: Document upload component for beneficiaries

#### Task 2.6: Admin Rejection Dialog
- [ ] Create `src/components/beneficiaries/BeneficiaryRejectDialog.tsx`:
  - Dialog layout similar to OrganizationRejectDialog
  - List all KYC documents with checkboxes
  - Feedback text area per document
  - "Submit Rejection" button
  - Cancel button
  - Validation: At least one document must be selected
- [ ] Call beneficiaryService.reject() with document-level feedback
- [ ] Close dialog on success, show notification
- [ ] Handle errors

**Deliverable**: Admin rejection dialog with feedback

#### Task 2.7: Navigation & Routing
- [ ] Add "Beneficiaries" menu item to sidebar navigation
- [ ] Add routes in App.tsx:
  - `/beneficiaries` → BeneficiariesPage
  - `/beneficiaries/new` → BeneficiaryForm (create mode)
  - `/beneficiaries/:id` → BeneficiaryForm (edit mode)
- [ ] Add role-based route guards (BUSINESS_USER, ADMIN)
- [ ] Update breadcrumbs

**Deliverable**: Navigation integration

---

### **Phase 3: Testing & Quality Assurance** (3-4 days)

#### Task 3.1: Backend Unit Tests
- [ ] Entity tests: `BeneficiaryEntityTest.java` (10+ assertions)
- [ ] Repository tests: `BeneficiaryRepositoryTest.java` (custom queries)
- [ ] Service tests: `BeneficiaryServiceTest.java` (business logic, 20+ tests)
- [ ] Controller tests: `BeneficiaryControllerTest.java` (REST endpoints, 15+ tests)
- [ ] Mapper tests: `BeneficiaryMapperTest.java`
- [ ] Run all tests: `mvn clean test`
- [ ] Target: >90% code coverage for Beneficiary module

**Deliverable**: Comprehensive backend test suite

#### Task 3.2: Backend Integration Tests
- [ ] Create `BeneficiaryIntegrationTest.java`:
  - Test full workflow: Create → Submit → Approve
  - Test rejection workflow: Create → Submit → Reject → Resubmit
  - Test role-based access (Business User vs Admin)
  - Test one-beneficiary-per-user validation
  - Test document upload and approval
- [ ] Run integration tests with H2 database
- [ ] Verify all endpoints return correct status codes

**Deliverable**: Integration tests covering workflows

#### Task 3.3: Frontend Unit Tests (Optional)
- [ ] Create test files:
  - `BeneficiaryForm.test.tsx` - Form validation
  - `BeneficiaryKYCUpload.test.tsx` - File upload logic
  - `beneficiaryService.test.ts` - API calls (mock)
- [ ] Run tests: `npm test`

**Deliverable**: Frontend unit tests (if time permits)

#### Task 3.4: Manual UI Testing
- [ ] Create manual test plan document (similar to MANUAL_TEST_PLAN.md)
- [ ] Test scenarios to cover:

**Test Cases (15+)**:
1. **TC-BEN-001**: Create beneficiary as Business User
2. **TC-BEN-002**: One-beneficiary-per-user validation (try to create second)
3. **TC-BEN-003**: Upload all 5 KYC document types
4. **TC-BEN-004**: Submit beneficiary for review
5. **TC-BEN-005**: Admin approves beneficiary
6. **TC-BEN-006**: Admin rejects specific documents with feedback
7. **TC-BEN-007**: View rejection feedback as Business User
8. **TC-BEN-008**: Resubmit corrected documents
9. **TC-BEN-009**: Search beneficiaries by name
10. **TC-BEN-010**: Filter beneficiaries by status
11. **TC-BEN-011**: Role-based access (Business User sees only own beneficiary)
12. **TC-BEN-012**: Role-based access (Admin sees all beneficiaries)
13. **TC-BEN-013**: Update beneficiary details
14. **TC-BEN-014**: Delete document
15. **TC-BEN-015**: File validation (wrong format, oversized file)
16. **TC-BEN-016**: Form validation (missing required fields)
17. **TC-BEN-017**: Pagination and sorting

- [ ] Execute all test cases in NPE environment
- [ ] Record results in Excel tracker
- [ ] Capture screenshots of bugs
- [ ] Report bugs to development team

**Deliverable**: Manual test plan with execution results

#### Task 3.5: AI Feedback Loop for Bug Fixing
- [ ] QA executes manual tests and logs bugs in Excel
- [ ] QA exports failed tests and submits to AI:
  ```
  "I found UI bugs during beneficiary module testing:
  
  Test ID: TC-BEN-002
  Issue: 'Create Beneficiary' button not disabled after creating first beneficiary
  Steps to Reproduce: ...
  Expected: Button should be disabled
  Actual: Button remains clickable
  Screenshot: [attached]
  
  Please analyze and fix this bug."
  ```
- [ ] AI analyzes bug, fixes code, provides updated files
- [ ] Developer deploys fix to NPE
- [ ] QA retests and updates Excel tracker
- [ ] Repeat until all tests pass ✅

**Deliverable**: Bug-free module via AI feedback loop

---

### **Phase 4: Documentation & Deployment** (2 days)

#### Task 4.1: API Documentation
- [ ] Update Swagger/OpenAPI documentation:
  - Add Beneficiary endpoints group
  - Add request/response examples
  - Add error response codes
- [ ] Generate API documentation: `http://localhost:8080/swagger-ui.html`
- [ ] Update `README.md` with Beneficiary API endpoints

**Deliverable**: API documentation updated

#### Task 4.2: User Documentation
- [ ] Create `BENEFICIARY_USER_GUIDE.md`:
  - How to create a beneficiary
  - How to upload KYC documents
  - How to submit for review
  - How to view rejection feedback
  - How to resubmit documents
  - FAQ section
- [ ] Create `BENEFICIARY_ADMIN_GUIDE.md`:
  - How to review beneficiaries
  - How to approve beneficiaries
  - How to reject with document-level feedback
  - How to search and filter

**Deliverable**: User guides for Business Users and Admins

#### Task 4.3: Architecture Documentation
- [ ] Update `ARCHITECTURE.md`:
  - Add Beneficiary entity to entity diagram
  - Update database schema diagram
  - Update component architecture diagram
  - Add Beneficiary workflow diagram
- [ ] Update `PROJECT_STATUS.md`:
  - Mark Beneficiary Module as 100% complete
  - List all features and endpoints
  - Add test coverage metrics

**Deliverable**: Architecture documentation updated

#### Task 4.4: Database Migration on NPE
- [ ] Backup NPE Cloud SQL database
- [ ] Execute Flyway migration on NPE:
  ```bash
  mvn flyway:migrate -Dspring.profiles.active=npe
  ```
- [ ] Verify table created successfully
- [ ] Verify foreign keys and indexes
- [ ] Test one beneficiary creation manually

**Deliverable**: NPE database migrated successfully

#### Task 4.5: Backend Deployment to NPE
- [ ] Build Docker image:
  ```bash
  mvn clean package -DskipTests
  docker build -t gcr.io/fincore-project/usermgmt-api:v2.2.0 .
  ```
- [ ] Push to Google Container Registry:
  ```bash
  docker push gcr.io/fincore-project/usermgmt-api:v2.2.0
  ```
- [ ] Deploy to Cloud Run:
  ```bash
  gcloud run deploy fincore-npe-api \
    --image gcr.io/fincore-project/usermgmt-api:v2.2.0 \
    --platform managed \
    --region europe-west2
  ```
- [ ] Verify health check: `/actuator/health`
- [ ] Test one API endpoint: `GET /api/beneficiaries`

**Deliverable**: Backend deployed to NPE

#### Task 4.6: Frontend Deployment to NPE
- [ ] Build production bundle:
  ```bash
  npm run build
  ```
- [ ] Build Docker image:
  ```bash
  docker build -t gcr.io/fincore-project/fincore-ui:v2.2.0 .
  ```
- [ ] Push to GCR:
  ```bash
  docker push gcr.io/fincore-project/fincore-ui:v2.2.0
  ```
- [ ] Deploy to Cloud Run:
  ```bash
  gcloud run deploy fincore-npe-ui \
    --image gcr.io/fincore-project/fincore-ui:v2.2.0 \
    --platform managed \
    --region europe-west2
  ```
- [ ] Verify UI loads: `https://fincore-npe-ui-...run.app`
- [ ] Test Beneficiaries page navigation

**Deliverable**: Frontend deployed to NPE

#### Task 4.7: End-to-End Smoke Test
- [ ] Login to NPE UI as Business User
- [ ] Create a beneficiary with all details
- [ ] Upload all 5 required documents
- [ ] Submit for review
- [ ] Login as Admin
- [ ] Approve the beneficiary
- [ ] Verify beneficiary status = ACTIVE
- [ ] Verify documents status = VERIFIED

**Deliverable**: Smoke test passed ✅

---

## 🧪 Testing Strategy

### Backend Testing

#### Unit Tests (Target: >90% coverage)
- **Entity Tests** (10 tests)
  - Beneficiary lifecycle (onCreate, onUpdate)
  - Status transitions
  - Enum values
  
- **Repository Tests** (8 tests)
  - CRUD operations
  - Custom query methods
  - Relationship queries
  
- **Service Tests** (25 tests)
  - Create beneficiary with validation
  - One-beneficiary-per-user rule
  - Role-based data filtering
  - Submit workflow
  - Approve workflow
  - Reject workflow with document feedback
  - Search and pagination
  - Document upload/delete
  
- **Controller Tests** (15 tests)
  - All REST endpoints (success cases)
  - Error cases (404, 400, 403)
  - Role-based security
  - Request/response validation

#### Integration Tests (10 scenarios)
- End-to-end workflows with real database
- Multi-user scenarios
- Concurrent access testing

### Frontend Testing

#### Manual UI Testing (17 test cases)
- See Task 3.4 for complete list
- Focus on user workflows
- Role-based access testing
- Form validation testing
- Document upload testing

#### Browser Compatibility
- Chrome (latest)
- Edge (latest)
- Firefox (latest)

#### Responsive Testing
- Desktop: 1920x1080
- Tablet: 768x1024
- Mobile: 375x667

---

## 📊 Success Criteria

### Feature Completeness
- ✅ All 15+ backend REST endpoints working
- ✅ All 3 frontend pages functional
- ✅ All 5 document types uploadable
- ✅ Submit → Review → Approve/Reject workflow complete
- ✅ Role-based access control enforced
- ✅ One-beneficiary-per-user rule working

### Code Quality
- ✅ Backend test coverage >90%
- ✅ All unit tests passing (mvn test)
- ✅ All integration tests passing
- ✅ No critical SonarQube issues
- ✅ Swagger documentation complete

### Testing
- ✅ All 17 manual UI test cases pass
- ✅ No critical bugs in NPE environment
- ✅ Performance acceptable (API response <1s)
- ✅ Document upload/download working

### Documentation
- ✅ User guides created (Business User + Admin)
- ✅ Architecture documentation updated
- ✅ API documentation complete (Swagger)
- ✅ Manual test plan created

### Deployment
- ✅ Database migration successful (NPE)
- ✅ Backend deployed to NPE Cloud Run
- ✅ Frontend deployed to NPE Cloud Run
- ✅ Smoke test passed in NPE

---

## ⏱️ Estimated Timeline

| Phase | Duration | Dependencies |
|-------|----------|-------------|
| **Phase 1: Database & Backend** | 3-4 days | None |
| **Phase 2: Frontend** | 4-5 days | Phase 1 complete |
| **Phase 3: Testing & QA** | 3-4 days | Phase 2 complete |
| **Phase 4: Documentation & Deployment** | 2 days | Phase 3 complete |
| **Total** | **12-15 days** | Sequential execution |

### Development Velocity Assumptions:
- 1 full-time developer
- 6-8 hours productive coding per day
- No major blockers or dependencies

### Potential Accelerators:
- Reuse existing patterns from Organization module (reduces dev time by 20-30%)
- AI-assisted code generation (entity, repository, mapper boilerplate)
- AI-assisted testing (test case generation)

---

## 🚨 Risks & Mitigation

### Risk 1: Database Migration Failure on NPE
**Impact**: High  
**Probability**: Low  
**Mitigation**: 
- Test migration on local H2 first
- Backup NPE database before migration
- Have rollback script ready
- Test migration on UAT environment first

### Risk 2: One-Beneficiary-Per-User Rule Breaks Existing Users
**Impact**: Medium  
**Probability**: Low  
**Mitigation**:
- Check existing users in database (likely none have beneficiaries yet)
- Add data migration script if needed
- Add feature flag to disable rule temporarily if issues arise

### Risk 3: KYC_Documents Extension Breaks Existing Organization Documents
**Impact**: High  
**Probability**: Low  
**Mitigation**:
- New column `Beneficiary_Identifier` is nullable (won't affect existing rows)
- Add database constraint: `CHECK ((Reference_Identifier IS NOT NULL AND Beneficiary_Identifier IS NULL) OR (Reference_Identifier IS NULL AND Beneficiary_Identifier IS NOT NULL))`
- Test existing organization document upload after migration

### Risk 4: Frontend Navigation Conflicts
**Impact**: Low  
**Probability**: Low  
**Mitigation**:
- Use unique routes: `/beneficiaries` (not `/organizations`)
- Test all navigation links after adding new menu item

### Risk 5: Testing Takes Longer Than Expected
**Impact**: Medium  
**Probability**: Medium  
**Mitigation**:
- Prioritize critical test cases (workflows, security)
- Use AI feedback loop to fix bugs faster
- Allocate buffer time (3-4 days allows flexibility)

---

## 📝 Approval Checklist

Before proceeding with implementation, please review and approve:

- [ ] **SQL Schema Corrections**: Reviewed and approved corrected SQL
- [ ] **Database Design**: Beneficiary table structure is correct
- [ ] **KYC_Documents Extension**: Adding Beneficiary_Identifier column is acceptable
- [ ] **Document Types**: 5 new document types are correct
- [ ] **One-Beneficiary-Per-User**: Business rule is correct (or should allow multiple?)
- [ ] **Workflow**: PENDING → UNDER_REVIEW → ACTIVE/REJECTED is correct
- [ ] **Admin Approval**: Admin can approve/reject with document-level feedback
- [ ] **Role-Based Access**: Business Users see only their beneficiaries, Admins see all
- [ ] **Timeline**: 12-15 days is acceptable
- [ ] **Phases**: 4-phase approach is appropriate
- [ ] **Testing Strategy**: Manual testing + AI feedback loop is acceptable

---

## 🎯 Next Steps

Once you approve this plan:

1. **I will start with Phase 1, Task 1.1** (Database Migration)
2. **Create Flyway migration file** with corrected SQL
3. **Execute migration on local H2** for testing
4. **Proceed sequentially** through all tasks
5. **Provide daily status updates** on progress
6. **Deliver working module** ready for QA testing

**Please review this plan and confirm approval to proceed with implementation.** 🚀

---

## ✅ ALL REQUIREMENTS FINALIZED AND APPROVED

### Confirmed Decisions:

1. ✅ **Multiple Beneficiaries:** Users can create up to **20 beneficiaries maximum**
2. ✅ **Nick_Name Field:** Added to Beneficiary table
3. ✅ **Country Field:** Added as **separate field** (not from Address) for fast queries
4. ✅ **Status & Reason Fields:** Added for approval workflow
5. ✅ **Audit Fields:** Added Created_By and Last_Modified_By
6. ✅ **C2C Flag:** Added `Is_Counter_Over_Counter BOOLEAN` explicit flag
7. ✅ **KYC_Documents Link:** Using Option A - add Beneficiary_Identifier column
8. ✅ **Document Requirements:**
   - CLIENT_AUTHORISATION_LETTER: **Required always**
   - BENEFICIARY_COMPANY_KYC: **Required always**
   - BENEFICIARY_AGREEMENT: **Required always**
   - COLLECTOR_IDENTIFICATION: **Required only if Is_Counter_Over_Counter = true**
   - OPTIONAL_DOCUMENTATION: **Optional**
9. ✅ **Notifications:** Send **email** on beneficiary approval/rejection

### Business Rules Summary:

| Rule | Implementation |
|------|----------------|
| **Max Beneficiaries** | 20 per user (validated in service layer) |
| **Country Required** | Yes, must be filled |
| **C2C Validation** | If Is_Counter_Over_Counter = true, Collector_Contact_Number is required |
| **C2C Documents** | If Is_Counter_Over_Counter = true, COLLECTOR_IDENTIFICATION document required |
| **Country Consistency** | beneficiary.country should match address.country (warning if different) |
| **Workflow** | PENDING → UNDER_REVIEW → ACTIVE/REJECTED |
| **Admin Actions** | Approve (set ACTIVE) or Reject with document-level feedback |
| **Email Notifications** | Send on approval, rejection, document rejection |

---

## 📞 Questions Previously Asked (NOW ALL ANSWERED)

After analyzing the ERD diagram you provided, I have these **important questions** before implementation:

### **Question 1: Multiple Beneficiaries Per User** ⚠️ **MOST IMPORTANT**

The ERD shows **Users (0..1) ← → Beneficiary (*)** which means **one user can have MANY beneficiaries**.

This is **DIFFERENT** from Organizations where it's one-to-one.

**Please confirm:**
- ✅ **YES, users can create multiple beneficiaries** (remove one-per-user validation)
- ❌ **NO, it should be one-to-one** (add validation like Organization module)
- 🔢 **YES but with a limit** (e.g., max 10 or 50 beneficiaries per user)

**Impact:** This affects validation logic, UI behavior, and business rules.

---

### **Question 2: Missing Fields - Nick_Name and Country**

Your requirements text mentioned:
- "Nick Name" for beneficiary
- "Country" for beneficiary

But the **ERD does NOT show these columns**.

**Should I:**
- ✅ **Add them** (Nick_Name VARCHAR(100), Country VARCHAR(50))
- ❌ **Skip them** (follow ERD exactly - no Nick_Name, get Country from Address table only)

**My recommendation:** Add both for better UX (nickname for quick reference, country for filtering)

---

### **Question 3: Workflow Fields (Status & Reason)**

Requirements mention "admin approval for beneficiary" but ERD doesn't show:
- `Status_Description` (PENDING → UNDER_REVIEW → ACTIVE/REJECTED)
- `Reason_Description` (rejection feedback)

**Should I:**
- ✅ **Add them** (required for approval workflow, consistent with Organisation)
- ❌ **Skip workflow** (simpler - beneficiaries are auto-approved?)

**My recommendation:** Add Status and Reason fields - approval workflow is core requirement.

---

### **Question 4: Audit Fields (Created_By, Last_Modified_By)**

ERD doesn't show `Created_By` and `Last_Modified_By`, but **all other tables** (Users, Organisation, KYC_Documents) have them.

**Should I:**
- ✅ **Add them** (consistency with other tables, audit trail requirement)
- ❌ **Skip them** (follow ERD exactly)

**My recommendation:** Add for consistency and audit compliance.

---

### **Question 5: KYC_Documents Relationship**

ERD does **NOT show a line** between KYC_Documents and Beneficiary tables.

Your requirements clearly state beneficiaries need 5 document types uploaded.

**How should documents link to beneficiaries?**

**Option A (Recommended):** Add `Beneficiary_Identifier INT` column to KYC_Documents
```sql
-- Clean separation: Organisation docs use Reference_Identifier, 
-- Beneficiary docs use Beneficiary_Identifier
ALTER TABLE KYC_Documents ADD COLUMN Beneficiary_Identifier INT;
```

**Option B:** Reuse `Reference_Identifier` for both, add `Reference_Type` discriminator column

**Option C:** Create separate `Beneficiary_Documents` table (more complex, not recommended)

**Which option do you prefer?** I strongly recommend **Option A**.

---

### **Question 6: Document Requirements**

Requirements mention 5 document types:
1. Client Authorisation Letter
2. Beneficiary Company KYC
3. Beneficiary Agreement
4. Collector Identification
5. Optional Documentation

**Please clarify:**
- Are **all 5 required** before submission? Or only first 3?
- Is "Collector Identification" **only required if** Collector_Contact_Number is filled (Counter Over Counter)?
- Can user submit with missing documents and upload later?

---

### **Question 7: Counter Over Counter Flag**

Should there be an explicit checkbox/flag for "Counter Over Counter" beneficiary?

**Option A:** No flag - implied when `Collector_Contact_Number` is filled  
**Option B:** Add `Is_Counter_Over_Counter BOOLEAN` flag for clarity

**Which approach?**

---

### **Question 8: Notifications**

Should we send email/SMS notifications when:
- Beneficiary is approved by admin?
- Beneficiary is rejected by admin?
- Documents are rejected with feedback?

**Or skip notifications for now** (add later)?

---

## ✅ My Recommendations Summary

Based on ERD + Requirements analysis, I recommend:

| Field/Feature | Recommendation | Reason |
|---------------|---------------|---------|
| **Multiple beneficiaries per user** | ✅ YES - allow multiple | ERD shows one-to-many |
| **Nick_Name** | ✅ ADD | User-friendly, mentioned in requirements |
| **Country** | ✅ ADD | Filtering, mentioned in requirements |
| **Status_Description** | ✅ ADD (REQUIRED) | Approval workflow is core requirement |
| **Reason_Description** | ✅ ADD (REQUIRED) | Rejection feedback is core requirement |
| **Created_By / Last_Modified_By** | ✅ ADD | Audit trail, consistency with other tables |
| **KYC_Documents link** | ✅ Option A - Add Beneficiary_Identifier column | Cleanest approach |
| **Document requirements** | ⏳ AWAITING YOUR ANSWER | Need clarification |
| **Counter Over Counter** | ⏳ AWAITING YOUR ANSWER | Need clarification |
| **Notifications** | ⏸️ SKIP for Phase 1 | Can add in Phase 2 |

---

**Please answer the 8 questions above so I can finalize the implementation.** Thank you! 🙏
