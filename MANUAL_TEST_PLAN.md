# Manual Test Plan - Organization KYC & Rejection Workflow

**Version**: 2.1.0  
**Date**: May 11, 2026  
**Status**: Ready for Testing  
**Test Environment**: NPE/UAT

---

## 📋 Table of Contents

1. [Test Overview](#test-overview)
2. [Test Prerequisites](#test-prerequisites)
3. [Test Data](#test-data)
4. [Test Scenarios](#test-scenarios)
5. [Test Cases](#test-cases)
6. [Expected Results](#expected-results)
7. [Bug Reporting](#bug-reporting)

---

## Test Overview

### Features Under Test
1. **Organization KYC Document Upload** - Inline document upload during organization creation
2. **Submit for Review Workflow** - Organization owner can submit for admin review
3. **Admin Approval Workflow** - Admin can approve/reject organizations
4. **Document-Level Rejection** - Admin can reject specific documents with detailed feedback
5. **Rejection Feedback Display** - Organization owners can see rejection reasons
6. **Resubmission Workflow** - Owners can fix and resubmit rejected documents

### Test Objectives
- Verify complete end-to-end workflow from organization creation to approval/rejection
- Validate document upload, status transitions, and feedback mechanisms
- Ensure proper role-based access control (Admin vs. Business User)
- Confirm data persistence and UI responsiveness

---

## Test Prerequisites

### Environment Setup
- **Backend API**: Running on NPE/UAT environment
- **Frontend UI**: Running on NPE/UAT environment
- **Database**: MySQL with clean test data
- **Browser**: Chrome/Edge (latest version)

### Test Accounts

#### Admin Account
```
Phone: +44-7700-900001
OTP: 123456 (or use actual OTP)
Role: SYSTEM_ADMINISTRATOR
```

#### Business User Account 1
```
Phone: +44-7700-900002
OTP: 123456
Role: BUSINESS_USER
```

#### Business User Account 2
```
Phone: +44-7700-900003
OTP: 123456
Role: BUSINESS_USER
```

### Test Files
Prepare the following files in a test folder:

1. **Certificate of Incorporation** (PDF, 2MB)
   - File: `certificate_of_incorporation.pdf`
   
2. **Proof of Address** (PDF, 1.5MB)
   - File: `proof_of_address.pdf`
   
3. **Directors Register** (PDF, 1MB)
   - File: `directors_register.pdf`
   
4. **Invalid File** (TXT, 500KB)
   - File: `invalid_document.txt` (for negative testing)
   
5. **Oversized File** (PDF, 15MB)
   - File: `oversized_document.pdf` (for negative testing)

---

## Test Data

### Organization Data - Scenario 1 (Happy Path)

```json
{
  "legalName": "Tech Innovations Ltd",
  "tradingName": "TechInn",
  "organisationType": "LIMITED_COMPANY",
  "registrationNumber": "12345678",
  "taxId": "GB123456789",
  "incorporationDate": "2020-01-15",
  "businessAddress": {
    "addressLine1": "123 Tech Street",
    "addressLine2": "Innovation District",
    "city": "London",
    "county": "Greater London",
    "postcode": "SW1A 1AA",
    "country": "United Kingdom"
  },
  "primaryContact": {
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@techinnovations.co.uk",
    "phone": "+44-7700-900100",
    "position": "Managing Director"
  },
  "industry": "TECHNOLOGY",
  "companySize": "SMALL",
  "annualRevenue": "£500,000",
  "numberOfEmployees": 25
}
```

### Organization Data - Scenario 2 (Rejection Path)

```json
{
  "legalName": "Global Finance Solutions PLC",
  "tradingName": "GFS",
  "organisationType": "PUBLIC_LIMITED_COMPANY",
  "registrationNumber": "87654321",
  "taxId": "GB987654321",
  "incorporationDate": "2018-06-10",
  "businessAddress": {
    "addressLine1": "456 Finance Avenue",
    "addressLine2": "Business Park",
    "city": "Manchester",
    "county": "Greater Manchester",
    "postcode": "M1 1AA",
    "country": "United Kingdom"
  },
  "primaryContact": {
    "firstName": "Sarah",
    "lastName": "Johnson",
    "email": "sarah.johnson@gfs.co.uk",
    "phone": "+44-7700-900200",
    "position": "CEO"
  },
  "industry": "FINANCIAL_SERVICES",
  "companySize": "MEDIUM",
  "annualRevenue": "£2,500,000",
  "numberOfEmployees": 75
}
```

### KYC Document Types
```
1. CERTIFICATE_OF_INCORPORATION (Required)
2. PROOF_OF_ADDRESS (Required)
3. DIRECTORS_REGISTER (Required)
4. MEMORANDUM_OF_ASSOCIATION (Optional)
5. ARTICLES_OF_ASSOCIATION (Optional)
6. SHAREHOLDERS_REGISTER (Optional)
7. BANK_REFERENCE_LETTER (Optional)
8. AUDITED_FINANCIAL_STATEMENTS (Optional)
```

### Rejection Feedback Examples
```
Document: Certificate of Incorporation
Reason: "The document is blurry and unreadable. Please upload a clearer version showing company name and registration number."

Document: Proof of Address
Reason: "This document is more than 3 months old. Please provide a recent utility bill or bank statement dated within the last 90 days."

Document: Directors Register
Reason: "The directors' list is incomplete. Please include full names, addresses, and dates of appointment for all directors."
```

---

## Test Scenarios

### Scenario 1: Happy Path - Complete Approval
**Objective**: Test successful organization creation, KYC upload, submission, and approval

**Steps**:
1. Business User logs in
2. Creates a new organization (Tech Innovations Ltd)
3. Uploads all 3 required KYC documents
4. Submits for review
5. Admin logs in
6. Reviews and approves organization
7. Business User verifies organization is ACTIVE

**Expected Result**: Organization approved, all documents verified, status = ACTIVE

---

### Scenario 2: Rejection and Resubmission
**Objective**: Test rejection workflow with detailed feedback and successful resubmission

**Steps**:
1. Business User logs in
2. Creates a new organization (Global Finance Solutions PLC)
3. Uploads 3 KYC documents (some will be "faulty")
4. Submits for review
5. Admin logs in
6. Rejects 2 out of 3 documents with detailed feedback
7. Business User views rejection feedback
8. Business User resubmits corrected documents
9. Admin approves organization

**Expected Result**: Rejection feedback visible, resubmission successful, final approval granted

---

### Scenario 3: Incomplete Submission Prevention
**Objective**: Verify users cannot submit without required documents

**Steps**:
1. Business User creates organization
2. Uploads only 1 of 3 required documents
3. Attempts to submit for review

**Expected Result**: Submit button disabled or validation error shown

---

### Scenario 4: File Validation
**Objective**: Test file type and size validation

**Steps**:
1. Business User creates organization
2. Attempts to upload .txt file (invalid type)
3. Attempts to upload 15MB file (oversized)
4. Uploads valid PDF files

**Expected Result**: Invalid uploads rejected with clear error messages, valid uploads succeed

---

## Test Cases

### Test Case 1: Organization Creation
**Test ID**: TC-ORG-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Business User | Dashboard displayed |
| 2 | Navigate to Organizations | Organizations page loaded |
| 3 | Click "Create Organization" | Multi-step form opens (7 tabs) |
| 4 | Fill Tab 1: Basic Info | Fields validated, can proceed to Tab 2 |
| 5 | Fill Tab 2: Registration | Fields validated, can proceed to Tab 3 |
| 6 | Fill Tab 3: Business Address | Fields validated, can proceed to Tab 4 |
| 7 | Fill Tab 4: Contact Info | Fields validated, can proceed to Tab 5 |
| 8 | Fill Tab 5: Business Details | Fields validated, can proceed to Tab 6 |
| 9 | Fill Tab 6: Financial Info | Fields validated, can proceed to Tab 7 |
| 10 | View Tab 7: KYC Documents | KYC upload component displayed |
| 11 | Save as Draft | Organization saved with status = PENDING |

**Test Data**: Use Organization Data - Scenario 1

---

### Test Case 2: KYC Document Upload
**Test ID**: TC-KYC-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Open organization in edit mode | Form loaded with saved data |
| 2 | Navigate to Tab 7 (KYC Documents) | Upload interface displayed |
| 3 | Select document type "Certificate of Incorporation" | Dropdown populated |
| 4 | Drag & drop certificate_of_incorporation.pdf | File selected, preview shown |
| 5 | Click "Upload" button | Progress indicator shown, file uploads |
| 6 | Verify document in table | Document listed with status PENDING |
| 7 | Repeat for "Proof of Address" | Second document uploaded successfully |
| 8 | Repeat for "Directors Register" | Third document uploaded successfully |
| 9 | Verify required documents checklist | All 3 required documents checked ✓ |
| 10 | Verify tab completion | Tab 7 marked as complete |

**Test Data**: 
- certificate_of_incorporation.pdf
- proof_of_address.pdf
- directors_register.pdf

---

### Test Case 3: Submit for Review
**Test ID**: TC-SUBMIT-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Navigate to Organizations page | Organizations list displayed |
| 2 | Verify organization status | Status = PENDING |
| 3 | Verify "Submit for Review" button visible | Blue send icon button shown |
| 4 | Click "Submit for Review" | Confirmation dialog appears |
| 5 | Confirm submission | Organization status changes to UNDER_REVIEW |
| 6 | Verify all documents status | All document statuses = UNDER_REVIEW |
| 7 | Verify "Submit" button hidden | Button no longer visible (already submitted) |
| 8 | Verify organization in admin queue | Admin can see in UNDER_REVIEW filter |

**Precondition**: Organization with 3 uploaded KYC documents

---

### Test Case 4: Admin Approval
**Test ID**: TC-APPROVE-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Admin | Admin dashboard displayed |
| 2 | Navigate to Organizations | All organizations visible |
| 3 | Filter by status "UNDER_REVIEW" | Only pending reviews shown |
| 4 | Select organization "Tech Innovations Ltd" | Organization details displayed |
| 5 | Click organization name/row | Opens organization details |
| 6 | Click "View Documents" or navigate to KYC tab | All 3 documents displayed |
| 7 | Review each document (download/view) | Documents accessible |
| 8 | Navigate back to Organizations list | List displayed |
| 9 | Click "Approve" button (green checkmark) | Confirmation dialog appears |
| 10 | Confirm approval | Success message shown |
| 11 | Verify organization status | Status = ACTIVE |
| 12 | Verify documents status | All documents status = VERIFIED |
| 13 | Verify reasonDescription cleared | No rejection text present |

**Precondition**: Organization submitted for review with 3 documents

---

### Test Case 5: Admin Rejection with Feedback
**Test ID**: TC-REJECT-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Admin | Admin dashboard displayed |
| 2 | Navigate to Organizations | All organizations visible |
| 3 | Filter by status "UNDER_REVIEW" | Pending reviews shown |
| 4 | Select organization "Global Finance Solutions PLC" | Organization details displayed |
| 5 | Click "Reject" button (red X icon) | Rejection dialog opens |
| 6 | Verify document list | All 3 KYC documents listed with checkboxes |
| 7 | Select "Certificate of Incorporation" checkbox | Checkbox checked |
| 8 | Enter feedback: "Document is blurry and unreadable..." | Text entered in feedback field |
| 9 | Select "Proof of Address" checkbox | Checkbox checked |
| 10 | Enter feedback: "Document is more than 3 months old..." | Text entered in feedback field |
| 11 | Leave "Directors Register" unchecked | Third document will be auto-approved |
| 12 | Click "Submit Rejection" | Rejection processed |
| 13 | Verify success message | "Organization rejected successfully" shown |
| 14 | Verify organization status | Status = REQUIRES_RESUBMISSION |
| 15 | Verify organization reasonDescription | "2 of 3 documents rejected" |
| 16 | Verify rejected documents | Certificate & Proof of Address = REJECTED |
| 17 | Verify non-rejected document | Directors Register = VERIFIED |
| 18 | Verify rejection feedback stored | Each rejected doc has admin's feedback |

**Precondition**: Organization "Global Finance Solutions PLC" submitted for review

---

### Test Case 6: View Rejection Feedback (Business User)
**Test ID**: TC-FEEDBACK-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Business User (owner of GFS PLC) | Dashboard displayed |
| 2 | Navigate to Organizations page | Organizations list displayed |
| 3 | Verify rejection alert banner | Warning alert shown at top of page |
| 4 | Read alert message | "Global Finance Solutions PLC: 2 of 3 documents rejected. Please check KYC documents for details." |
| 5 | Click organization name | Organization details opened |
| 6 | Navigate to "KYC Documents" tab | KYC documents page displayed |
| 7 | Verify rejection alert | Error alert listing rejected documents |
| 8 | Read rejection details | "Certificate of Incorporation: Document is blurry..." |
| 9 | Verify "Admin Feedback" column | Column present in documents table |
| 10 | Check feedback for Certificate | Red text: "Document is blurry and unreadable..." |
| 11 | Check feedback for Proof of Address | Red text: "Document is more than 3 months old..." |
| 12 | Check feedback for Directors Register | Empty (document was verified) |
| 13 | Hover over long feedback text | Tooltip shows full message |
| 14 | Verify resubmission instructions | Alert includes "Upload new documents and resubmit" |

**Precondition**: Organization rejected by admin with document-level feedback

---

### Test Case 7: Resubmit Corrected Documents
**Test ID**: TC-RESUBMIT-001  
**Priority**: High  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Continue from TC-FEEDBACK-001 | On KYC Documents page |
| 2 | Verify rejected documents status | Status = REJECTED (red) |
| 3 | Verify verified document status | Status = VERIFIED (green) |
| 4 | Click "Delete" on rejected Certificate | Confirmation dialog appears |
| 5 | Confirm deletion | Document removed from list |
| 6 | Select document type "Certificate of Incorporation" | Dropdown populated |
| 7 | Upload new clear certificate PDF | File uploaded successfully |
| 8 | Verify new document status | Status = PENDING |
| 9 | Repeat steps 4-8 for Proof of Address | New document uploaded |
| 10 | Navigate back to Organizations page | Organizations list displayed |
| 11 | Verify organization status | Still REQUIRES_RESUBMISSION |
| 12 | Click "Submit for Review" button | Button visible (resubmission allowed) |
| 13 | Confirm submission | Organization status → UNDER_REVIEW |
| 14 | Verify all documents status | All documents → UNDER_REVIEW |
| 15 | Verify rejection feedback cleared | reasonDescription cleared |

**Precondition**: Organization rejected with 2 rejected documents

---

### Test Case 8: File Type Validation
**Test ID**: TC-VALIDATE-001  
**Priority**: Medium  
**Type**: Negative Testing

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Open KYC Documents upload tab | Upload interface displayed |
| 2 | Select document type | Dropdown populated |
| 3 | Try to upload invalid_document.txt | Error: "Invalid file type. Only PDF, JPG, PNG allowed" |
| 4 | Verify file not selected | No file preview shown |
| 5 | Try to upload .docx file | Error: "Invalid file type..." |
| 6 | Try to upload .xlsx file | Error: "Invalid file type..." |
| 7 | Upload valid .pdf file | File accepted and uploaded |
| 8 | Upload valid .jpg file | File accepted and uploaded |
| 9 | Upload valid .png file | File accepted and uploaded |

**Test Data**: invalid_document.txt, valid PDFs/JPGs/PNGs

---

### Test Case 9: File Size Validation
**Test ID**: TC-VALIDATE-002  
**Priority**: Medium  
**Type**: Negative Testing

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Open KYC Documents upload tab | Upload interface displayed |
| 2 | Select document type | Dropdown populated |
| 3 | Try to upload 15MB PDF file | Error: "File size exceeds maximum (10MB)" |
| 4 | Verify file not selected | No file preview shown |
| 5 | Upload 9.5MB PDF file | File accepted (just under limit) |
| 6 | Verify file preview | File name and size displayed |
| 7 | Click "Upload" | File uploads successfully |

**Test Data**: oversized_document.pdf (15MB), large_but_valid.pdf (9.5MB)

---

### Test Case 10: One Organization Per User Restriction
**Test ID**: TC-ORG-002  
**Priority**: High  
**Type**: Business Rule Validation

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Business User | Dashboard displayed |
| 2 | Navigate to Organizations | Organizations page displayed |
| 3 | Verify existing organization | User already has "Tech Innovations Ltd" |
| 4 | Click "Create Organization" button | Button disabled or error message |
| 5 | Verify error message | "You already have an organization. Only one organization per user is allowed." |
| 6 | Try API call to create 2nd org | Backend returns 400 Bad Request error |
| 7 | Verify error response | "User already has an active organization" |

**Precondition**: Business User already owns one organization

---

### Test Case 11: Role-Based Access Control
**Test ID**: TC-RBAC-001  
**Priority**: High  
**Type**: Security

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Business User | Dashboard displayed |
| 2 | Navigate to Organizations | Only own organization visible |
| 3 | Verify Approve/Reject buttons | Buttons NOT visible (non-admin) |
| 4 | Verify "Submit for Review" button | Button visible (owner capability) |
| 5 | Logout and login as Admin | Admin dashboard displayed |
| 6 | Navigate to Organizations | All organizations visible |
| 7 | Filter by UNDER_REVIEW | All pending organizations shown |
| 8 | Verify Approve/Reject buttons | Buttons visible (admin capability) |
| 9 | Verify "Submit for Review" button | Button NOT visible (admin doesn't submit) |

**Test Data**: Both Business User and Admin accounts

---

### Test Case 12: Document Download
**Test ID**: TC-DOC-001  
**Priority**: Medium  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Admin | Dashboard displayed |
| 2 | Navigate to organization KYC documents | Documents table displayed |
| 3 | Click "Download" button on first document | File download initiated |
| 4 | Verify file downloaded | PDF file saved to Downloads folder |
| 5 | Open downloaded file | File opens correctly in PDF reader |
| 6 | Verify file content | Content matches original upload |
| 7 | Repeat for other documents | All documents downloadable |

**Precondition**: Organization with uploaded KYC documents

---

### Test Case 13: Pagination and Sorting
**Test ID**: TC-UI-001  
**Priority**: Low  
**Type**: Functional

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as Admin | Dashboard displayed |
| 2 | Navigate to Organizations | Organizations list with pagination |
| 3 | Verify page size options | 10, 20, 50 rows per page available |
| 4 | Select 10 rows per page | Shows 10 organizations |
| 5 | Click "Next Page" | Page 2 displayed |
| 6 | Click "Previous Page" | Back to page 1 |
| 7 | Sort by "Legal Name" ascending | Organizations sorted A-Z |
| 8 | Sort by "Legal Name" descending | Organizations sorted Z-A |
| 9 | Sort by "Created Date" | Sorted by creation date |
| 10 | Sort by "Status" | Grouped by status |

**Precondition**: Database with 25+ organizations

---

## Expected Results

### Status Transitions

| Action | Before Status | After Status | Document Status |
|--------|--------------|--------------|-----------------|
| Create Organization | - | PENDING | - |
| Upload Document | PENDING | PENDING | PENDING |
| Submit for Review | PENDING | UNDER_REVIEW | UNDER_REVIEW |
| Admin Approve | UNDER_REVIEW | ACTIVE | VERIFIED |
| Admin Reject (all) | UNDER_REVIEW | REQUIRES_RESUBMISSION | REJECTED |
| Admin Reject (partial) | UNDER_REVIEW | REQUIRES_RESUBMISSION | REJECTED (selected), VERIFIED (others) |
| Resubmit | REQUIRES_RESUBMISSION | UNDER_REVIEW | UNDER_REVIEW |

### Button Visibility Matrix

| Button | Business User (Owner) | Business User (Non-Owner) | Admin |
|--------|--------------------|------------------------|-------|
| Create Organization | ✓ (if no org) | ✓ (if no org) | ✓ |
| Submit for Review | ✓ (PENDING/REQUIRES_RESUBMISSION) | ✗ | ✗ |
| Approve | ✗ | ✗ | ✓ (UNDER_REVIEW) |
| Reject | ✗ | ✗ | ✓ (UNDER_REVIEW) |
| Edit Organization | ✓ | ✗ | ✓ |
| Delete Document | ✓ (PENDING/REJECTED docs) | ✗ | ✓ |

### Feedback Visibility

| User Type | Organizations Page Alert | KYC Documents Page Alert | Admin Feedback Column |
|-----------|-------------------------|-------------------------|---------------------|
| Organization Owner | ✓ (if REQUIRES_RESUBMISSION) | ✓ (lists rejected docs) | ✓ (shows feedback) |
| Other Business User | ✗ | ✗ | ✗ |
| Admin | ✗ (admins don't see owner alerts) | ✓ (can see feedback) | ✓ (can see feedback) |

---

## Bug Reporting

### Bug Report Template

```markdown
**Bug ID**: BUG-XXX
**Title**: [Brief description]
**Severity**: Critical / High / Medium / Low
**Test Case**: TC-XXX-XXX
**Environment**: NPE / UAT / Local

**Steps to Reproduce**:
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Result**:
[What should happen]

**Actual Result**:
[What actually happened]

**Screenshots**:
[Attach screenshots]

**Browser**: Chrome 120 / Edge 120
**User Role**: Admin / Business User
**Organization ID**: 123
**Document IDs**: 456, 789
```

### Common Issues to Watch For

1. **Status not updating** after approval/rejection
2. **Documents not showing** after upload
3. **Rejection feedback not visible** to organization owner
4. **Submit button still visible** after submission
5. **File upload fails** without error message
6. **Pagination breaks** with large datasets
7. **Role-based buttons visible** to wrong user types
8. **One org per user rule** not enforced
9. **Document deletion fails** for verified documents
10. **Resubmission clears** verified documents incorrectly

---

## Test Sign-Off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| **Test Lead** | | | |
| **Business Analyst** | | | |
| **Product Owner** | | | |
| **Developer** | | | |

---

## Appendix

### API Endpoints Reference

```
POST   /api/organizations                    # Create organization
GET    /api/organizations/{id}                # Get organization by ID
PUT    /api/organizations/{id}                # Update organization
DELETE /api/organizations/{id}                # Delete organization
PUT    /api/organizations/{id}/submit         # Submit for review
PUT    /api/organizations/{id}/approve        # Approve (admin)
PUT    /api/organizations/{id}/reject         # Reject (admin)
GET    /api/organizations/{id}/kyc-documents  # Get KYC documents

POST   /api/kyc-documents/upload              # Upload KYC document
GET    /api/kyc-documents/{id}                # Get document by ID
DELETE /api/kyc-documents/{id}                # Delete document
GET    /api/kyc-documents/{id}/download       # Download document
GET    /api/kyc-documents/organization/{id}   # Get docs by organization
```

### Database Queries for Validation

```sql
-- Check organization status
SELECT id, legal_name, organisation_status, reason_description 
FROM organisation 
WHERE id = ?;

-- Check document statuses
SELECT id, document_type, document_status, reason_description 
FROM kyc_document 
WHERE organisation_id = ?;

-- Count organizations per user
SELECT owner_id, COUNT(*) as org_count 
FROM organisation 
GROUP BY owner_id;

-- Get rejection summary
SELECT o.legal_name, 
       COUNT(kd.id) as total_docs,
       SUM(CASE WHEN kd.document_status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_docs
FROM organisation o
LEFT JOIN kyc_document kd ON o.id = kd.organisation_id
WHERE o.organisation_status = 'REQUIRES_RESUBMISSION'
GROUP BY o.id;
```

---

**Document Version**: 1.0  
**Last Updated**: May 11, 2026  
**Prepared By**: FinCore Development Team  
**Status**: Ready for Testing
