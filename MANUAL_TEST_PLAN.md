# FinCore Platform - UI Manual Test Plan

**Version**: 3.0.0 - UI Testing Only  
**Date**: May 18, 2026  
**Status**: Ready for NPE Testing  
**Test Environment**: NPE (Non-Production Environment)  
**Platform**: FinCore User Management & Organization Onboarding System  
**Testing Scope**: Frontend UI Testing (No API Testing)

---

## 📌 IMPORTANT: UI TESTING ONLY

**This test plan focuses exclusively on testing the web application user interface (UI).**

### ✅ What You WILL Test (UI):
- Login page functionality and form validation
- Dashboard and navigation menu
- Button clicks and link navigation
- Form inputs, dropdowns, date pickers
- File upload and download via browser
- Success and error messages displayed on screen
- Tables, lists, and data display
- Page loading and UI responsiveness
- Role-based UI visibility (what users can/cannot see)
- Visual elements (colors, icons, status badges)

### ❌ What You Will NOT Test (API/Backend):
- API endpoints or REST calls
- Database queries or data validation at DB level
- Backend logs or server console
- Direct file storage verification
- Backend health checks or monitoring

### 🎯 Testing Approach:
All testing is performed through the **web browser only**. You interact with the application as a real user would:
1. Open browser and navigate to UI URL
2. Click buttons, fill forms, upload files
3. Observe what appears on screen
4. Verify expected results are displayed
5. Capture screenshots of any issues

**Tools Needed**: Web browser (Chrome/Edge/Firefox), Excel tracker, Screenshot tool

---

---

## 📋 Table of Contents

1. [Introduction for QA Team](#1-introduction-for-qa-team)
2. [Features Overview](#2-features-overview)
3. [Test Environment Setup](#3-test-environment-setup)
4. [Test Data Repository](#4-test-data-repository)
5. [Test Execution Guide](#5-test-execution-guide)
6. [Module 1: User Management & Authentication](#module-1-user-management--authentication)
7. [Module 2: Organization Management](#module-2-organization-management)
8. [Module 3: KYC Document Upload & Verification](#module-3-kyc-document-upload--verification)
9. [Module 4: Admin Approval Workflow](#module-4-admin-approval-workflow)
10. [Module 5: Rejection & Resubmission Workflow](#module-5-rejection--resubmission-workflow)
11. [Module 6: Questionnaire Management](#module-6-questionnaire-management)
12. [Module 7: Customer Answers](#module-7-customer-answers)
13. [Bug Reporting & AI Feedback Process](#bug-reporting--ai-feedback-process)
14. [Test Completion Checklist](#test-completion-checklist)

---

# 1. Introduction for QA Team

## Welcome to FinCore UI Testing! 👋

This document provides a comprehensive **UI-focused manual testing plan** for the FinCore platform - a financial services application that manages users, organizations, and KYC (Know Your Customer) verification workflows.

### What is FinCore?

FinCore is a **cloud-based financial compliance platform** with a modern web interface built using React and Material-UI. This test plan focuses exclusively on testing the **user interface** through the web browser.

**Technology Stack (UI)**:
- **Frontend UI**: React TypeScript application with Material-UI components
- **Deployment**: Google Cloud Platform
- **Browser Support**: Chrome, Edge, Firefox, Safari

### Your Role as QA

You will manually test **all UI features** in the NPE environment using only the web browser to ensure:
✅ UI elements display correctly and are responsive  
✅ User workflows are smooth and intuitive  
✅ Form validation works correctly with helpful error messages  
✅ Buttons, links, and navigation function properly  
✅ Role-based UI visibility works correctly  
✅ Documents upload and download through UI successfully  
✅ Data displays accurately in tables and forms  
✅ Success and error messages are clear and timely

**Testing Approach**: All testing will be performed through the web browser interface only. No API testing, database queries, or backend console access required.  

### How This Testing Works

1. **Study** the features overview (Section 2)
2. **Setup** your test environment (Section 3)
3. **Execute** test cases module by module (Sections 6-12)
4. **Record** results in the Excel tracker (provided separately)
5. **Report** bugs and issues following Section 13
6. **Feedback** to AI for automated bug fixes

### Testing Timeline

- **Estimated Duration**: 5-7 business days (full regression)
- **Daily Testing**: 4-6 hours per day
- **Test Cycles**: 2 cycles (initial + regression after fixes)

---

# 2. Features Overview

This section explains all completed features in the FinCore platform. Read this carefully to understand what you're testing.

## 2.1 User Management & Authentication ✅

### What It Does
Manages user accounts with secure phone-based authentication and role-based access control.

### Key Features
- **Phone-Based Login**: Users login with mobile number + OTP (One-Time Password)
- **4 Business Roles**:
  - `SYSTEM_ADMINISTRATOR`: Full system access (admin features)
  - `COMPLIANCE_OFFICER`: Compliance operations
  - `OPERATIONAL_USER`: Operational tasks
  - `BUSINESS_USER`: Limited access (can only see their own data)
- **JWT Authentication**: Secure token-based authentication (24-hour expiration)
- **User CRUD**: Create, Read, Update, Delete users
- **Address Management**: 3 address types (Residential, Postal, Business)
- **Data Filtering**: Business Users only see their own organizations/data

### User Workflow
```UI Testing Focus
- ✅ Login page display and functionality
- ✅ Phone number input validation and formatting
- ✅ OTP input field behavior
- ✅ Login with different roles via UI
- ✅ Error message display for incorrect OTP
- ✅ Session timeout behavior (redirect to login after 24 hours)
- ✅ Role-based UI visibility (Business Users don't see admin buttons)
- ✅ User dashboard and navigation menu display
- ✅ User list page with search and filters
- ✅ User creation/edit forms with address management
- ✅ Form field validation and error messages
- Login with different roles
- OTP validation (correct/incorrect codes)
- Token expiration after 24 hours
- Role-based UI visibility (Business Users don't see admin buttons)
- Address creation and management

---

## 2.2 Organization Management ✅

### What It Does
Allows users to create and manage their business organizations with detailed information.

### Key Features
- **6 Organization Types**:
  1. `LIMITED_COMPANY` (LTD)
  2. `PUBLIC_LIMITED_COMPANY` (PLC)
  3. `LIMITED_LIABILITY_PARTNERSHIP` (LLP)
  4. `SOLE_TRADER`
  5. `CHARITY`
  6. `PARTNERSHIP`
- **Multi-Step Creation Wizard**: 7 tabs to collect comprehensive information
- **One Organization Per User Rule**: Business Users can create only ONE organization
- **Organization Status Lifecycle**: PENDING → UNDER_REVIEW → ACTIVE
- **Search & Filtering**: Find organizations by name, type, status, date range
- **Address Management**: Registered address, business address, correspondence address

### 7-Step Creation Wizard
1. **Basic Information**: Legal name, trading name, organization type
2. **Registration Details**: Registration number, incorporation date, VAT number
3. **Business Address**: Registered, business, and correspondence addresses
4. **Contact Information**: Email, phone, website, primary contact person
5. **Business Details**: Industry, number of employees, annual revenue
6. **Financial Information**: Bank details, accounting reference date
7. **KYC Documents Upload**: Upload required compliance documents (NEW!)

### Organization Status Flow
```
PENDING (after creation) 
   ↓ (User clicks "Submit for Review")
UNDER_REVIEW (waiting for admin)
   ↓ (Admin approves)
ACTIVE (organization approved)
   OR
   ↓UI Testing Focus
- ✅ Organization list page display and layout
- ✅ "Create Organization" button visibility based on role
- ✅ 7-step wizard UI and tab navigation
- ✅ Form field display and input controls
- ✅ Dropdown population with all 6 organization types
- ✅ Required field validation messages
- ✅ Tab completion indicators (checkmarks)
- ✅ "Same as" checkbox functionality for addresses
- ✅ Save button behavior and success messages
- ✅ Organization appearing in list after creation
- ✅ Status badges display (color-coded)
- ✅ Search box functionality
- ✅ Filter dropdowns (type, status, date range)
- ✅ "Submit for Review" button visibility and behavior
- ✅ One-organization-per-user UI enforcement
- Create organizations of all 6 types
- Validate one-organization-per-user rule
- Complete all 7 wizard steps
- Required field validation
- Submit for review functionality
- Search and filter capabilities

---

## 2.3 KYC Document Upload & Verification ✅

### What It Does
Manages the upload, storage, and verification of KYC (Know Your Customer) compliance documents.

### Key Features
- **9 Document Types Supported**:
  1. `PASSPORT`: Passport copy
  2. `DRIVING_LICENSE`: Driver's license
  3. `NATIONAL_ID`: National ID card
  4. `PROOF_OF_ADDRESS`: Utility bill, bank statement
  5. `CERTIFICATE_OF_INCORPORATION`: Company registration
  6. `MEMORANDUM_OF_ASSOCIATION`: Company bylaws
  7. `ARTICLES_OF_ASSOCIATION`: Company articles
  8. `DIRECTORS_REGISTER`: List of directors
  9. `SHAREHOLDERS_REGISTER`: List of shareholders

- **Inline Upload**: Documents uploaded during organization creation (Step 7)
- **UI Testing Focus
- ✅ KYC Documents tab display in organization wizard
- ✅ Document type dropdown population (all 9 types)
- ✅ Drag & drop zone visual feedback
- ✅ File browse button functionality
- ✅ File preview display (name, size, type)
- ✅ Upload button and progress indicator
- ✅ File validation error messages (format, size)
- ✅ Documents table display with columns
- ✅ Status badges for documents (color-coded)
- ✅ Download/View buttons functionality
- ✅ Delete button with confirmation dialog
- ✅ Multiple document handling in UI
- ✅ Required documents checklist displayNG → UNDER_REVIEW → VERIFIED/REJECTED

### Document Workflow
```
1. User uploads document → 2. File validated → 3. Uploaded to GCS → 
4. Status: PENDING → 5. User submits org for review → 6. Status: UNDER_REVIEW →
7. Admin reviews → 8. Status: VERIFIED or REJECTED
```

### Testing Focus
- Upload all 9 document types
- Drag and drop functionality
- File validation (wrong format, oversized files)
- Document preview/download
- Status transitions
- Multiple documents per organization

---

## 2.4 Admin Approval Workflow ✅

### What It Does
Enables admin users to review and approve/reject organizations submitted by business users.

### Key Features
- **Role-Based Buttons**: Only SYSTEM_ADMINISTRATOR role sees approve/reject buttons
- **Two Actions**:
  1. **Approve Organization**: 
     - Changes organization status to ACTIVE
     - All documents marked as VERIFIED
     - Organization can start operations
  2. **Reject Organization**:
     - Opens rejection dialog
     - Admin selects specific documents to reject
     - Provides detailed feedback for each rejected document
     - Non-selected documents automatically VERIFIED
     - Organization status → REQUIRES_RESUBMISSION

### Approve Workflow
```
Admin sees org with status UNDER_REVIEW → Clicks green checkmark button →
ConfUI Testing Focus
- ✅ Admin-only button visibility (Approve/Reject)
- ✅ Green checkmark (Approve) button display
- ✅ Red X (Reject) button display
- ✅ Approve confirmation dialog
- ✅ Reject dialog opening and layout
- ✅ Document list with checkboxes in reject dialog
- ✅ Feedback text areas per document
- ✅ Submit/Cancel buttons in dialogs
- ✅ Success message display after approval/rejection
- ✅ Status badge updates in UI after action
- ✅ Organization list refresh to show new statuson dialog opens → 
Admin selects documents to reject → Enters rejection reasons → Submits →
Org status: REQUIRES_RESUBMISSION, Selected docs: REJECTED, Others: VERIFIED
```

### Document-Level Rejection Features
- **Selective Rejection**: Admin can reject specific documents (e.g., only passport and proof of address)
- **Per-Document Feedback**: Each rejected document gets a specific reason
- **Automatic Verification**: Non-rejected documents are automatically verified
- **Summary Generation**: System generates "X of Y documents rejected" message

### Testing Focus
- Admin login and button visibility
- Approve entire organization
- Reject specific documents with feedback
- Verify status transitions
- Check feedback is saved correctly

---

## 2.5 Rejection & Resubmission Workflow ✅

### What It Does
Allows organization owners to see rejection feedback and resubmit corrected documents.

### Key Features
- **Rejection Alert Display**: 
  - Red alert banner on Organizations page
  - Shows organization name and rejection summary
  - "View Details" link to see full feedback
- **Document-Level Feedback**:
  - Each rejected document shows admin's specific reason
  - Color-coded status badges (red for rejected, green for verified)
  - Clear action items for user
- **Resubmission Process**:
  - UI Testing Focus
- ✅ Red rejection alert banner display on Organizations page
- ✅ Alert message content and formatting
- ✅ "View Details" link functionality
- ✅ Document-level feedback display in UI
- ✅ Color-coded status badges (red for rejected, green for verified)
- ✅ Admin Feedback column in documents table
- ✅ Tooltip/expandable text for long feedback
- ✅ Upload new document UI after rejection
- ✅ "Submit for Review" button reappearance
- ✅ Alert dismissal after resubmission
- ✅ Status badge color change after resubmission
### User Experience Flow
```
1. User logs in → Sees red rejection alert
2. Clicks "View Details" → Sees organization page
3. Sees which documents rejected + reasons
4. Uploads corrected documents
5. Clicks "Submit for Review"
6. Status: UNDER_REVIEW (back to admin)
```

### Rejection Feedback Display
- **Organizations Page**: Summary alert with org name
- **Organization Details Page**: Full list of rejected documents with reasons
- **KYC Tab**: Document status badges (REJECTED in red)
- **Clear Messaging**: Explains what needs to be fixed

### Testing Focus
- View rejection alerts
- Read document-specific feedback
- Upload replacement documents
- Resubmit for review
- Verify status changes correctly

---

## 2.6 Questionnaire Management ✅

### What It Does
Enables admins to create dynamic questionnaires for compliance and onboarding purposes.

### Key Features
- **UI Testing Focus
- ✅ Questionnaires page display and layout
- ✅ "Create Question" button visibility (admin only)
- ✅ Question creation form display
- ✅ Category dropdown population (6 categories)
- ✅ Question type dropdown options
- ✅ Display order input field
- ✅ Status dropdown (ACTIVE, INACTIVE, ARCHIVED)
- ✅ Required checkbox behavior
- ✅ Save button and success message
- ✅ Questions list/table display
- ✅ Move Up/Down buttons
- ✅ Archive button and confirmation
- ✅ Status badges for questiontional details
  4. `COMPLIANCE`: Regulatory compliance
  5. `RISK_ASSESSMENT`: Risk evaluation
  6. `OTHER`: Miscellaneous questions
- **Question Status**: ACTIVE, INACTIVE, ARCHIVED
- **Display Order**: Reorder questions for logical flow
- **Question Types**: Text, multiple choice, yes/no, date
- **Archive/Restore**: Soft delete with restore capability

### Questionnaire Workflow
```
Admin creates question → Sets category & order → Saves → Question: ACTIVE →
Users can see question → Optionally: Archive question → Question: ARCHIVED
```
UI Testing Focus
- ✅ Questionnaire/Answers page display for users
- ✅ Active questions list display
- ✅ Answer input fields (text, dropdown, date picker)
- ✅ Required field indicators (asterisks)
- ✅ Submit button behavior
- ✅ Success message after submission
- ✅ Edit button for existing answers
- ✅ Update functionality and confirmation
- ✅ Bulk submission UI (Submit All button)
- ✅ Completion rate display (progress bar or percentage)
- ✅ Required validation error messages

---

## 2.7 Customer Answers ✅

### What It Does
Allows users to submit answers to questionnaire questions for compliance purposes.

### Key Features
- **Submit Answers**: Users provide answers to active questions
- **Update Answers**: Users can modify previously submitted answers
- **Bulk Submission**: Submit multiple answers at once
- **Answer Tracking**: Track which questions are answered
- **Completion Rate**: System calculates % of questions answered
- **Required Validation**: Enforces required questions

### Answer Workflow
```
User sees questionnaire → Fills in answers → Submits → Answers saved →
Can update later → Completion rate updated
```

### Testing Focus
- Submit answers to questions
- Update existing answers
- Bulk submission
- Required question validation
- Completion rate calculation

---

# 3. Test Environment Setup

## 3.1 Environment URLs

### NPE Environment (UI Testing)
- **Frontend UI**: `https://fincore-npe-ui-994490239798.europe-west2.run.app` 
- **Login Page**: Access the frontend URL to reach the login page

**Note**: This test plan focuses on UI testing only. You will interact exclusively with the web application through your browser. No API endpoints or backend testing required.

### Verify UI Access
Before testing, verify you can access the application:
1. Open the frontend UI URL in your browser
2. Should see the FinCore login page with:
   - FinCore logo/branding
   - Phone number input field
   - "Send OTP" button
   - Clean, professional UI layout
3. If login page doesn't load:
   - Check internet connection
   - Try different browser
   - Clear browser cache
   - Contact development team

---

## 3.2 Test Accounts

### Admin Account
```
Phone Number: +44-7700-900001
OTP: 123456 (development mode - fixed OTP)
Role: SYSTEM_ADMINISTRATOR
Name: Admin User
Email: admin@fincore.com
```

**What This Account Can Do:**
- Create/edit/delete users
- Create/edit organizations
- View all organizations (not just own)
- Approve/reject organizations
- Create questionnaires
- Full system access

---

### Business User Account 1
```
Phone Number: +44-7700-900002
OTP: 123456
Role: BUSINESS_USER
Name: Business Owner One
Email: business1@testcompany.com
```

**What This Account Can Do:**
- Create ONE organization
- Upload KYC documents
- Submit organization for review
- View rejection feedback
- Resubmit after rejection
- Answer questionnaires
- View own data only

---

### Business User Account 2
```
Phone Number: +44-7700-900003
OTP: 123456
Role: BUSINESS_USER
Name: Business Owner Two
Email: business2@anothercompany.com
```

**What This Account Can Do:**
- Same as Business User 1
- Used to test data isolation (cannot see User 1's data)

---

### Compliance Officer Account
```
Phone Number: +44-7700-900004
OTP: 123456
Role: COMPLIANCE_OFFICER
Name: Compliance Manager
Email: compliance@fincore.com
```

**What This Account Can Do:**
- View all organizations
- Perform compliance checks
- Access reports
- Cannot approve organizations (admin only)

---

## 3.3 Test Files Preparation

Create a folder on your computer called `FinCore_TestFiles` and prepare these files:

### Valid Test Documents

1. **passport_valid.pdf** (2MB)
   - Valid passport document for testing
   - PDF format
   - Size: ~2MB

2. **driving_license_valid.pdf** (1.5MB)
   - Valid driving license document
   - PDF format
   - Size: ~1.5MB

3. **certificate_incorporation.pdf** (1MB)
   - Certificate of Incorporation
   - PDF format
   - Size: ~1MB

4. **proof_address_utility.pdf** (800KB)
   - Utility bill as proof of address
   - PDF format
   - Size: ~800KB

5. **directors_register.pdf** (500KB)
   - List of company directors
   - PDF format
   - Size: ~500KB

6. **valid_image_passport.jpg** (1MB)
   - Passport as JPG image
   - JPG format
   - Size: ~1MB

7. **valid_image_license.png** (1.2MB)
   - License as PNG image
   - PNG format
   - Size: ~1.2MB

### Invalid Test Documents (for negative testing)

8. **invalid_format.txt** (100KB)
   - Text file (not allowed format)
   - TXT format
   - Expected: Upload should fail

9. **oversized_document.pdf** (15MB)
   - PDF larger than 10MB limit
   - PDF format
   - Size: 15MB
   - Expected: Upload should fail

10. **corrupted_file.pdf** (500KB)
    - Corrupted PDF file
    - Cannot be opened
    - Expected: Upload may succeed but preview fails

**Note**: If you don't have real documents, you can create dummy PDF files using any PDF creator or online tools. Content doesn't matter for testing purposes.

---

## 3.4 Browser Setup

### Recommended Browser
- **Primary**: Google Chrome (latest version)
- **Secondary**: Microsoft Edge (for cross-browser testing)

### Browser Extensions to Install
- **JSON Formatter**: For viewing API responses
- **React Developer Tools**: For inspecting React components (optional)

### Browser Settings
- ✅ Enable JavaScript
- ✅ Allow cookies (required for session management)
- ✅ Clear cache before each test session
- ✅ Disable ad blockers (may interfere with file uploads)
- ✅ Set zoom to 100% for consistent UI testing
- ✅ Use Incognito/Private mode for clean testing sessions

### Screen Resolution
- **Recommended**: 1920x1080 (Full HD) for desktop testing
- **Test Responsiv (UI Testing)

1. **Web Browser**: Chrome, Edge, or Firefox (latest version) ✅
2. **Excel/Google Sheets**: For test execution tracker ✅
3. **Screenshot Tool**: Windows Snipping Tool or Snagit ✅
4. **Notepad**: For copying error messages ✅

### Optional Tools

1. **Video Recording**: OBS Studio or Windows Game Bar (Win+G) for recording bugs
2. **Color Picker**: To verify UI colors match design (optional)
3. **Browser DevTools**: F12 for inspecting UI elements (advanced users)
   - **Console tab**: View JavaScript errors
   - **Network tab**: Check if requests are failing
   - **Elements tab**: Inspect UI components

**Note**: No API testing tools (Postman, cURL) required for this UI-focused test plan.ng bugs
4. **Stopwatch/Timer**: For performance testing (page load times)

### Optional Tools

1. **Postman**: For API testing (advanced users)
2. **Browser DevTools**: F12 for network inspection
3. **Video Recording**: OBS Studio for recording bugs

---

# 4. Test Data Repository

## 4.1 Organization Test Data

### Scenario 1: Limited Company (Happy Path)

```json
{
  "legalName": "Tech Innovations Limited",
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

**Expectedcd c:\Development\git\fincore_WebUI
npx playwright test tests/e2e/sumsub-kyc-verification.spec.tscd c:\Development\git\fincore_WebUI
npx playwright test tests/e2e/sumsub-kyc-verification.spec.ts Result**: Rejection feedback visible, resubmission successful, final approval granted

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

# Module 1: User Management & Authentication

## Test Cases

### TC-AUTH-001: Successful Login
**Priority**: Critical  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Open application URL | NPE frontend URL | Login page displayed |
| 2 | Enter phone number | +44-7700-900002 | Number accepted |
| 3 | Click "Send OTP" | - | "OTP sent" message shown |
| 4 | Enter OTP | 123456 | OTP field populated |
| 5 | Click "Login" | - | Redirected to dashboard |
| 6 | Verify user name | - | "Business Owner One" displayed in header |
| 7 | Verify navigation menu | - | User menu items visible |

---

### TC-AUTH-002: Invalid OTP
**Priority**: High  
**Type**: Negative Testing

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Open login page | - | Login page displayed |
| 2 | Enter phone number | +44-7700-900002 | Number accepted |
| 3 | Click "Send OTP" | - | "OTP sent" message shown |
| 4 | Enter incorrect OTP | 999999 | OTP field populated |
| 5 | Click "Login" | - | Error: "Invalid OTP code" |
| 6 | Verify still on login page | - | Not logged in |

---

### TC-AUTH-003: JWT Token Expiration
**Priority**: Medium  
**Type**: Security

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login successfully | Valid credentials | Dashboard displayed |
| 2 | Note current time | - | Record timestamp |
| 3 | Leave browser open for 24+ hours | - | Token expires |
| 4 | Try to navigate or perform action | - | Redirected to login page |
| 5 | See timeout message | - | "Session expired, please login again" |

---

### TC-USER-001: Create New User
**Priority**: High  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Admin | Admin credentials | Dashboard displayed |
| 2 | Navigate to Users | Click Users menu | Users page displayed |
| 3 | Click "Create User" | - | User creation form opens |
| 4 | Enter first name | "Alice" | Field populated |
| 5 | Enter last name | "Johnson" | Field populated |
| 6 | Enter email | "alice.johnson@test.com" | Field populated |
| 7 | Enter phone | "+44-7700-900010" | Field formatted correctly |
| 8 | Select role | BUSINESS_USER | Dropdown selected |
| 9 | Select status | ACTIVE | Dropdown selected |
| 10 | Add residential address | "10 Test St, London, SW1A 1AA" | Address fields filled |
| 11 | Click "Save" | - | Success message shown |
| 12 | Verify user in list | - | "Alice Johnson" appears in users table |

---

### TC-USER-002: Update User Details
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Users page | - | Users list displayed |
| 2 | Search for user | "Alice Johnson" | User found |
| 3 | Click "Edit" button | - | Edit form opens with current data |
| 4 | Update phone number | "+44-7700-900011" | New number entered |
| 5 | Update role | COMPLIANCE_OFFICER | Role changed |
| 6 | Click "Save" | - | Success message shown |
| 7 | Verify changes | - | Phone and role updated in table |

---

# Module 2: Organization Management

## Test Cases

### TC-ORG-003: Create LIMITED_COMPANY Organization
**Priority**: Critical  
**Type**: Functional

**Precondition**: Login as Business User with no existing organization

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Organizations | Click menu | Organizations page displayed |
| 2 | Click "Create Organization" | - | 7-step wizard opens |
| 3 | **TAB 1: Basic Information** | | |
| 4 | Enter Legal Name | "Tech Innovations Limited" | Field populated |
| 5 | Enter Trading Name | "TechInn" | Field populated |
| 6 | Select Organization Type | LIMITED_COMPANY | Dropdown selected |
| 7 | Enter Description | "Software development company" | Field populated |
| 8 | Click "Next" or Tab 2 | - | Tab 2 opens |
| 9 | **TAB 2: Registration Details** | | |
| 10 | Enter Registration Number | "12345678" | Field populated |
| 11 | Select Incorporation Date | 2020-01-15 | Date picked |
| 12 | Enter VAT Number | "GB123456789" | Field populated |
| 13 | Click "Next" or Tab 3 | - | Tab 3 opens |
| 14 | **TAB 3: Business Address** | | |
| 15 | Enter Address Line 1 | "123 Tech Street" | Field populated |
| 16 | Enter City | "London" | Field populated |
| 17 | Enter Postcode | "SW1A 1AA" | Field populated |
| 18 | Select Country | "United Kingdom" | Dropdown selected |
| 19 | Check "Same as Registered Address" | - | Business address auto-filled |
| 20 | Click "Next" or Tab 4 | - | Tab 4 opens |
| 21 | **TAB 4: Contact Information** | | |
| 22 | Enter Email | "contact@techinnovations.co.uk" | Field populated |
| 23 | Enter Phone | "+44-7700-900100" | Field populated |
| 24 | Enter Website | "www.techinnovations.co.uk" | Field populated |
| 25 | Enter Primary Contact Name | "John Smith" | Field populated |
| 26 | Enter Primary Contact Position | "Managing Director" | Field populated |
| 27 | Click "Next" or Tab 5 | - | Tab 5 opens |
| 28 | **TAB 5: Business Details** | | |
| 29 | Select Industry | TECHNOLOGY | Dropdown selected |
| 30 | Select Company Size | SMALL | Dropdown selected |
| 31 | Enter Annual Revenue | "£500,000" | Field populated |
| 32 | Enter Number of Employees | 25 | Field populated |
| 33 | Click "Next" or Tab 6 | - | Tab 6 opens |
| 34 | **TAB 6: Financial Information** | | |
| 35 | Enter Bank Name | "Barclays Bank PLC" | Field populated |
| 36 | Enter Bank Account Number | "12345678" | Field populated |
| 37 | Enter Sort Code | "20-00-00" | Field populated |
| 38 | Select Accounting Reference Date | 31-March | Date selected |
| 39 | Click "Next" or Tab 7 | - | Tab 7 opens (KYC Documents) |
| 40 | **TAB 7: KYC Documents** | | |
| 41 | Verify upload component displayed | - | Drag & drop area visible |
| 42 | Click "Save" at bottom | - | Organization saved as PENDING |
| 43 | Verify success message | - | "Organization created successfully" |
| 44 | Verify in organizations list | - | "Tech Innovations Limited" appears |
| 45 | Verify status | - | Status = PENDING |

---

### TC-ORG-004: Required Field Validation
**Priority**: High  
**Type**: Validation

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Start creating organization | - | Wizard opens |
| 2 | Leave Legal Name empty | - | Red error shown |
| 3 | Try to click Next | - | Button disabled or validation error |
| 4 | Enter Legal Name | "Test Company" | Error cleared |
| 5 | Leave Organization Type empty | - | Validation error shown |
| 6 | Try to navigate to Tab 2 | - | Tab navigation blocked |
| 7 | Select Organization Type | LIMITED_COMPANY | Error cleared, can proceed |
| 8 | Navigate to Tab 3 (Address) | - | Tab 3 opens |
| 9 | Leave Address Line 1 empty | - | Validation error shown |
| 10 | Try to save | - | Cannot save with required fields empty |
| 11 | Fill all required fields | Valid data | Save button enabled |
| 12 | Click Save | - | Organization created successfully |

---

### TC-ORG-005: Search Organizations
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Admin | Admin credentials | Dashboard displayed |
| 2 | Navigate to Organizations | - | All organizations visible |
| 3 | Enter search term in search box | "Tech" | Search field populated |
| 4 | Press Enter or click Search | - | Filtered results shown |
| 5 | Verify results | - | Only orgs with "Tech" in name shown |
| 6 | Clear search | - | All organizations displayed again |
| 7 | Filter by Organization Type | LIMITED_COMPANY | Only LTD companies shown |
| 8 | Filter by Status | ACTIVE | Only active orgs shown |
| 9 | Combine filters | Type=LTD, Status=ACTIVE | Both filters applied |
| 10 | Clear all filters | - | All organizations displayed |

---

# Module 3: KYC Document Upload & Verification

## Test Cases

### TC-KYC-002: Drag and Drop Upload
**Priority**: High  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Open organization KYC Documents tab | - | Upload interface displayed |
| 2 | Select document type | CERTIFICATE_OF_INCORPORATION | Dropdown populated |
| 3 | Drag file to drop zone | certificate_incorporation.pdf | File highlighted in drop zone |
| 4 | Drop file | - | File preview shown with name & size |
| 5 | Verify file details | - | File name and size displayed |
| 6 | Click "Upload" button | - | Progress bar appears |
| 7 | Wait for upload to complete | - | Success message: "Document uploaded" |
| 8 | Verify document in table | - | Document listed with status PENDING |
| 9 | Verify document type | - | Type = CERTIFICATE_OF_INCORPORATION |
| 10 | Verify uploaded date | - | Today's date shown |

---

### TC-KYC-003: Click to Upload
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Open KYC Documents tab | - | Upload interface displayed |
| 2 | Select document type | PROOF_OF_ADDRESS | Dropdown selected |
| 3 | Click "Browse Files" or drop zone | - | File picker dialog opens |
| 4 | Select file from computer | proof_address_utility.pdf | File selected |
| 5 | Click "Open" in dialog | - | File preview shown |
| 6 | Click "Upload" button | - | Upload starts |
| 7 | Verify upload completes | - | Document appears in table |

---

### TC-KYC-004: Multiple Documents Upload
**Priority**: High  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Upload Certificate of Incorporation | certificate_incorporation.pdf | Uploaded successfully |
| 2 | Upload Proof of Address | proof_address_utility.pdf | Uploaded successfully |
| 3 | Upload Directors Register | directors_register.pdf | Uploaded successfully |
| 4 | Verify all 3 documents in table | - | 3 rows in documents table |
| 5 | Verify all statuses | - | All = PENDING |
| 6 | Verify required docs checklist | - | All 3 required docs checked ✓ |

---

### TC-KYC-005: View/Download Document
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to KYC Documents | - | Documents table displayed |
| 2 | Locate uploaded document | Certificate of Incorporation | Document row visible |
| 3 | Click "View" or "Download" button | - | Action initiated |
| 4 | Verify download starts | - | File downloads to browser |
| 5 | Open downloaded file | - | PDF opens correctly |
| 6 | Verify file content | - | Content matches original upload |

---

### TC-KYC-006: Delete Document
**Priority**: Low  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to KYC Documents | - | Documents table displayed |
| 2 | Locate document to delete | Proof of Address | Document row visible |
| 3 | Click "Delete" button | - | Confirmation dialog appears |
| 4 | Click "Cancel" | - | Dialog closes, document still present |
| 5 | Click "Delete" again | - | Confirmation dialog appears |
| 6 | Click "Confirm" | - | Document deleted |
| 7 | Verify document removed | - | No longer in table |
| 8 | Verify required docs checklist | - | Proof of Address unchecked |

---

# Module 4: Admin Approval Workflow

## Test Cases

### TC-APPROVE-002: Full Organization Approval
**Priority**: Critical  
**Type**: End-to-End

**Precondition**: Organization "Tech Innovations Ltd" with status UNDER_REVIEW and 3 documents

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Admin | +44-7700-900001 | Admin dashboard |
| 2 | Navigate to Organizations | - | All organizations displayed |
| 3 | Apply filter | Status = UNDER_REVIEW | Filtered list shown |
| 4 | Locate organization | "Tech Innovations Ltd" | Organization found |
| 5 | Verify status badge | - | Orange "UNDER_REVIEW" badge |
| 6 | Verify Approve button visible | - | Green checkmark button shown |
| 7 | Click organization row/name | - | Organization details open (or stay on list) |
| 8 | Click "Approve" button | - | Confirmation dialog appears |
| 9 | Read confirmation message | - | "Are you sure you want to approve...?" |
| 10 | Click "Cancel" | - | Dialog closes, no change |
| 11 | Click "Approve" button again | - | Dialog appears |
| 12 | Click "Confirm" | - | Success message shown |
| 13 | Verify organization status | - | Status = ACTIVE |
| 14 | Navigate to organization details | - | Details page opens |
| 15 | Go to KYC Documents tab | - | Documents table displayed |
| 16 | Verify all document statuses | - | All documents = VERIFIED |
| 17 | Verify reasonDescription | - | Empty/null (no rejection text) |
| 18 | Logout admin | - | Logged out successfully |
| 19 | Login as Business User (owner) | +44-7700-900002 | User dashboard |
| 20 | Navigate to Organizations | - | Organizations page |
| 21 | Verify organization status | - | Status = ACTIVE (green badge) |
| 22 | Verify no rejection alerts | - | No warning banners shown |

---

### TC-REJECT-002: Selective Document Rejection
**Priority**: Critical  
**Type**: End-to-End

**Precondition**: Organization "Global Finance Solutions PLC" with status UNDER_REVIEW and 3 documents

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Admin | +44-7700-900001 | Admin dashboard |
| 2 | Navigate to Organizations | - | All organizations displayed |
| 3 | Filter by UNDER_REVIEW | - | Pending reviews shown |
| 4 | Locate organization | "Global Finance Solutions PLC" | Organization found |
| 5 | Click "Reject" button (red X) | - | Rejection dialog opens |
| 6 | Verify dialog title | - | "Reject Organization Documents" |
| 7 | Verify document list | - | 3 documents with checkboxes listed |
| 8 | Verify all unchecked initially | - | No documents selected |
| 9 | Check "Certificate of Incorporation" | - | Checkbox checked |
| 10 | Verify feedback textbox appears | - | Text area enabled for this doc |
| 11 | Enter rejection reason | "The document is blurry and unreadable. Please upload a clearer version showing company name and registration number." | Text entered |
| 12 | Check "Proof of Address" | - | Second checkbox checked |
| 13 | Enter rejection reason | "This document is more than 3 months old. Please provide a recent utility bill or bank statement dated within the last 90 days." | Text entered |
| 14 | Leave "Directors Register" unchecked | - | Third document unselected |
| 15 | Verify submit button enabled | - | "Submit Rejection" button active |
| 16 | Click "Submit Rejection" | - | Processing indicator shown |
| 17 | Wait for confirmation | - | Success: "Organization rejected with feedback" |
| 18 | Verify dialog closes | - | Back to organizations list |
| 19 | Verify organization status | - | Status = REQUIRES_RESUBMISSION |
| 20 | Click organization to view details | - | Details page opens |
| 21 | Verify reasonDescription | - | "2 of 3 documents rejected" |
| 22 | Navigate to KYC Documents | - | Documents table displayed |
| 23 | Verify Certificate status | - | Status = REJECTED (red badge) |
| 24 | Verify Proof of Address status | - | Status = REJECTED (red badge) |
| 25 | Verify Directors Register status | - | Status = VERIFIED (green badge) |
| 26 | Check Admin Feedback column | - | Column present |
| 27 | Read Certificate feedback | - | "The document is blurry..." shown |
| 28 | Read Proof of Address feedback | - | "This document is more than 3 months..." shown |
| 29 | Read Directors Register feedback | - | Empty (document was verified) |

---

### TC-REJECT-003: Validation - Must Select Documents
**Priority**: Medium  
**Type**: Validation

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Open rejection dialog | - | Dialog displayed |
| 2 | Leave all checkboxes unchecked | - | No documents selected |
| 3 | Try to click "Submit Rejection" | - | Button disabled OR |
| 4 | If button enabled, click it | - | Error: "Please select at least one document" |
| 5 | Select one document | Certificate | Checkbox checked |
| 6 | Leave feedback empty | - | Empty text field |
| 7 | Try to submit | - | Error: "Please provide rejection reason" |
| 8 | Enter feedback | "Invalid document" | Text entered |
| 9 | Click "Submit Rejection" | - | Rejection processed successfully |

---

# Module 5: Rejection & Resubmission Workflow

## Test Cases

### TC-RESUBMIT-002: Complete Resubmission Flow
**Priority**: Critical  
**Type**: End-to-End

**Precondition**: Organization "Global Finance Solutions PLC" rejected with 2 rejected documents

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Business User (owner) | +44-7700-900003 | Dashboard displayed |
| 2 | Navigate to Organizations | - | Organizations page opens |
| 3 | Verify rejection alert banner | - | Red warning banner at top |
| 4 | Read alert message | - | "Global Finance Solutions PLC: 2 of 3 documents rejected..." |
| 5 | Click "View Details" link | - | Organization details open |
| 6 | Verify organization status | - | Status = REQUIRES_RESUBMISSION (orange) |
| 7 | Navigate to KYC Documents tab | - | Documents table displayed |
| 8 | Verify rejection alert on page | - | Error alert listing rejected documents |
| 9 | Read rejection details | - | "Certificate: Document is blurry..." |
| 10 | Read rejection details | - | "Proof of Address: Document is more than 3 months old..." |
| 11 | Verify Document 1 status | - | Certificate = REJECTED (red badge) |
| 12 | Verify Document 2 status | - | Proof of Address = REJECTED (red badge) |
| 13 | Verify Document 3 status | - | Directors Register = VERIFIED (green badge) |
| 14 | Hover over feedback text | - | Full feedback shown in tooltip |
| 15 | Locate rejected Certificate | - | Row highlighted/marked |
| 16 | Click "Delete" on Certificate | - | Confirmation dialog |
| 17 | Confirm deletion | - | Document removed |
| 18 | Select document type | CERTIFICATE_OF_INCORPORATION | Dropdown selected |
| 19 | Upload new clear certificate | certificate_incorporation_clear.pdf | File uploaded |
| 20 | Verify new document status | - | Status = PENDING |
| 21 | Delete rejected Proof of Address | - | Document removed |
| 22 | Upload new recent proof | proof_address_recent.pdf | File uploaded |
| 23 | Verify new document status | - | Status = PENDING |
| 24 | Verify Directors Register | - | Still VERIFIED (not touched) |
| 25 | Navigate back to Organizations | - | Organizations list |
| 26 | Verify "Submit for Review" button | - | Button visible (resubmission allowed) |
| 27 | Click "Submit for Review" | - | Confirmation dialog |
| 28 | Confirm submission | - | Success message shown |
| 29 | Verify organization status | - | Status = UNDER_REVIEW |
| 30 | Verify all document statuses | - | All docs (incl. new ones) = UNDER_REVIEW |
| 31 | Verify reasonDescription cleared | - | No rejection text visible |
| 32 | Logout | - | Logged out |
| 33 | Login as Admin | +44-7700-900001 | Admin dashboard |
| 34 | Navigate to Organizations | - | All organizations visible |
| 35 | Filter by UNDER_REVIEW | - | GFS PLC appears in list |
| 36 | Verify resubmitted organization | - | Available for re-review |

---

# Module 6: Questionnaire Management

## Test Cases

### TC-QUEST-001: Create Question - Financial Category
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Admin | +44-7700-900001 | Admin dashboard |
| 2 | Navigate to Questionnaires | - | Questionnaires page displayed |
| 3 | Click "Create Question" | - | Question creation form opens |
| 4 | Enter question text | "What is your company's annual revenue?" | Field populated |
| 5 | Select category | FINANCIAL | Dropdown selected |
| 6 | Select question type | TEXT | Dropdown selected |
| 7 | Set display order | 1 | Number entered |
| 8 | Set status | ACTIVE | Dropdown selected |
| 9 | Check "Required" checkbox | - | Checkbox checked |
| 10 | Enter help text | "Please provide your latest annual revenue" | Field populated |
| 11 | Click "Save" | - | Success message shown |
| 12 | Verify question in list | - | Question appears in table |
| 13 | Verify category | - | Category = FINANCIAL |
| 14 | Verify status | - | Status = ACTIVE |

---

### TC-QUEST-002: Reorder Questions
**Priority**: Low  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Questionnaires | - | Questions list displayed |
| 2 | Verify current order | - | Questions sorted by display order |
| 3 | Select a question | Question at order 3 | Question selected |
| 4 | Click "Move Up" button | - | Question moves to order 2 |
| 5 | Verify new order | - | Question now at position 2 |
| 6 | Select another question | Question at order 1 | Question selected |
| 7 | Click "Move Down" button | - | Question moves to order 2 |
| 8 | Verify list reordered | - | Display order updated |

---

### TC-QUEST-003: Archive Question
**Priority**: Low  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Questionnaires | - | Questions list displayed |
| 2 | Locate active question | "What is your annual revenue?" | Question found |
| 3 | Click "Archive" button | - | Confirmation dialog |
| 4 | Confirm archiving | - | Success message |
| 5 | Verify question status | - | Status = ARCHIVED |
| 6 | Verify question still in list | - | Appears with "ARCHIVED" badge |
| 7 | Filter by ACTIVE | - | Archived question not shown |
| 8 | Clear filter or select ARCHIVED | - | Question reappears |

---

# Module 7: Customer Answers

## Test Cases

### TC-ANSWER-001: Submit Answer to Question
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Login as Business User | +44-7700-900002 | Dashboard displayed |
| 2 | Navigate to Questionnaires/Answers | - | Questionnaire page displayed |
| 3 | Verify active questions | - | List of ACTIVE questions shown |
| 4 | Locate first question | "What is your annual revenue?" | Question displayed |
| 5 | Enter answer | "£500,000" | Answer field populated |
| 6 | Click "Submit Answer" or "Next" | - | Answer saved |
| 7 | Verify success message | - | "Answer submitted successfully" |
| 8 | Verify answer appears | - | Answer shows in field (if editable) |
| 9 | Check completion rate | - | "1 of X questions answered" |

---

### TC-ANSWER-002: Update Existing Answer
**Priority**: Medium  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Questionnaires | - | Page displayed |
| 2 | Locate previously answered question | "What is your annual revenue?" | Question found |
| 3 | Verify current answer | - | Answer = "£500,000" |
| 4 | Click "Edit" button | - | Answer field becomes editable |
| 5 | Change answer | "£650,000" | New value entered |
| 6 | Click "Update" or "Save" | - | Answer updated |
| 7 | Verify success message | - | "Answer updated successfully" |
| 8 | Verify new answer displayed | - | Shows "£650,000" |

---

### TC-ANSWER-003: Bulk Answer Submission
**Priority**: Low  
**Type**: Functional

| Step | Action | Test Data | Expected Result |
|------|--------|-----------|----------------|
| 1 | Navigate to Questionnaires | - | All questions displayed |
| 2 | Answer question 1 | "£650,000" | Field populated |
| 3 | Answer question 2 | "10 employees" | Field populated |
| 4 | Answer question 3 | "Yes, we are registered" | Field populated |
| 5 | Answer question 4 | "Technology sector" | Field populated |
| 6 | Click "Submit All Answers" | - | All answers saved |
| 7 | Verify success message | - | "4 answers submitted successfully" |
| 8 | Verify completion rate | - | "4 of X questions answered" or "100%" |

---

# 13. Bug Reporting & AI Feedback Process

## 13.1 How to Report Bugs

When you find a bug or issue during testing, record it using the following format in the **Excel Test Tracker**:

### Bug Report Template

| Field | Description | Example |
|-------|-------------|---------|
| **Bug ID** | Unique identifier | BUG-001 |
| **Test Case ID** | Related test case | TC-ORG-001 |
| **Module** | Feature area | Organization Management |
| **Severity** | Critical / High / Medium / Low | High |
| **Priority** | Critical / High / Medium / Low | High |
| **Summary** | One-line description | Unable to save organization without legal name |
| **Steps to Reproduce** | Numbered steps | 1. Click Create Org<br>2. Leave Legal Name empty<br>3. Click Save |
| **Expected Result** | What should happen | Validation error shown, cannot save |
| **Actual Result** | What actually happened | Organization saved with empty legal name |
| **Screenshots** | File name of screenshot | bug-001-empty-name.png |
| **Browser** | Browser + version | Chrome 120.0 |
| **Test Date** | When found | 2026-05-18 |
| **Tested By** | Your name | QA Tester |
| **Status** | New / In Review / Fixed / Verified | New |

---

## 13.2 Bug Severity Definitions

### Critical 🔴
- **Impact**: System crash, data loss, security breach
- **Examples**: 
  - Application won't load
  - Cannot login at all
  - Data corruption or loss
  - Unauthorized access to admin features
- **Action**: Report immediately, stop related testing

### High 🟠
- **Impact**: Major feature broken, no workaround
- **Examples**:
  - Cannot upload documents at all
  - Cannot submit organization for review
  - Approval button not working
- **Action**: Report same day, continue with other tests

### Medium 🟡
- **Impact**: Feature partially broken, workaround exists
- **Examples**:
  - Drag & drop doesn't work but click upload does
  - Search works but filter doesn't
  - UI display issues that don't prevent functionality
- **Action**: Complete testing, report in daily summary

### Low 🟢
- **Impact**: Minor cosmetic or usability issue
- **Examples**:
  - Spelling error in message
  - Button alignment slightly off
  - Missing tooltip
- **Action**: Report in final test summary

---

## 13.3 AI Feedback Loop Process

### Step 1: Complete Testing Cycle
- Execute all test cases in a module
- Record results in Excel tracker
- Mark each test as PASS or FAIL
- For failures, complete bug report section

### Step 2: Export Failures
After completing a module (e.g., Organization Management), export failed tests:

1. Filter Excel to show only FAIL status
2. Copy the following columns:
   - Test Case ID
   - Test Name
   - Steps to Reproduce
   - Expected Result
   - Actual Result
   - Screenshots (describe what's shown)

### Step 3: Format for AI
Create a message for the AI using this template:

```
I completed testing of [Module Name] in the FinCore platform NPE environment.

SUMMARY:
- Total Tests: [number]
- Passed: [number]
- Failed: [number]
- Pass Rate: [percentage]%

FAILED TESTS:

Test ID: TC-ORG-001
Test Name: Create LIMITED_COMPANY Organization
Steps: 
1. Navigate to Organizations
2. Click Create Organization
3. Fill Tab 1: Legal Name = "Tech Innovations Limited"
4. [continue with all steps]

Expected: Organization saved with status PENDING
Actual: Error message "Internal Server Error 500" shown. Organization not created.

Error Details: [paste any error messages from console or UI]

Screenshot: [describe what screenshot shows]

---

[Repeat for each failed test]

---

ENVIRONMENT:
- Backend API: https://fincore-npe-api-994490239798.europe-west2.run.app
- Frontend URL: [frontend URL]
- Test Date: 2026-05-18
- Browser: Chrome 120.0

Please analyze these failures and fix the issues.
```

### Step 4: Send to AI
- Open your AI chat interface (where AI has access to the codebase)
- Paste the formatted feedback
- Wait for AI to analyze and fix issues

### Step 5: Verify Fixes
After AI reports fixes have been deployed:
1. Clear browser cache
2. Refresh the application
3. Re-run the failed tests
4. Update Excel tracker:
   - If fixed: Change status to PASS
   - If still failing: Update "Actual Result" with new behavior
   - If partially fixed: Note what's fixed and what remains

### Step 6: Regression Testing
After all fixes deployed:
1. Re-run ALL test cases (even those that passed initially)
2. Verify fixes didn't break other features
3. Update final test results

---

## 13.4 Daily Testing Checklist

### Morning
- [ ] Verify NPE environment is UP (check health endpoint)
- [ ] Clear browser cache
- [ ] Login with test accounts to verify access
- [ ] Review yesterday's bug fixes (if any)

### During Testing
- [ ] Follow test cases step by step
- [ ] Take screenshots of failures
- [ ] Copy error messages to notepad
- [ ] Update Excel tracker in real-time
- [ ] Note any new issues discovered

### End of Day
- [ ] Complete Excel tracker for the day
- [ ] Save screenshots to organized folder
- [ ] If >= 3 failures, prepare AI feedback
- [ ] Send feedback to AI
- [ ] Update project manager on progress

---

# 14. Test Completion Checklist

## Module Completion Status

| Module | Test Cases | Executed | Passed | Failed | Status |
|--------|-----------|----------|--------|--------|--------|
| User Management & Authentication | 5 | 0 | 0 | 0 | ⬜ Not Started |
| Organization Management | 8 | 0 | 0 | 0 | ⬜ Not Started |
| KYC Document Upload | 6 | 0 | 0 | 0 | ⬜ Not Started |
| Admin Approval Workflow | 5 | 0 | 0 | 0 | ⬜ Not Started |
| Rejection & Resubmission | 4 | 0 | 0 | 0 | ⬜ Not Started |
| Questionnaire Management | 3 | 0 | 0 | 0 | ⬜ Not Started |
| Customer Answers | 3 | 0 | 0 | 0 | ⬜ Not Started |
| **TOTAL** | **34** | **0** | **0** | **0** | **0%** |

---

## Final Sign-Off

### QA Tester
- **Name**: ______________________________
- **Date**: ______________________________
- **Signature**: ______________________________

### Test Summary
- **Total Test Cases**: 34
- **Total Tests Passed**: _______
- **Total Tests Failed**: _______
- **Pass Rate**: _______% 
- **Critical Issues Found**: _______
- **High Issues Found**: _______
- **Medium Issues Found**: _______
- **Low Issues Found**: _______

### Recommendation
- [ ] **PASS**: Application ready for production
- [ ] **CONDITIONAL PASS**: Application acceptable with minor issues
- [ ] **FAIL**: Critical issues found, requires fixes and re-testing

**Comments**: _______________________________________________________________

---

## Appendix A: Test Environment Details

### NPE Environment
- **Backend API Health**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
- **Backend API Base**: https://fincore-npe-api-994490239798.europe-west2.run.app/api
- **Frontend URL**: [To be updated with actual frontend URL]
- **Database**: Cloud SQL MySQL 8.0 (managed)
- **File Storage**: Google Cloud Storage (GCS)
- **Region**: europe-west2 (London)

### Test Data Cleanup
After testing, the following data can be cleaned:
- Test organizations created during testing
- Uploaded KYC documents
- Test user accounts (if created)

**Note**: Coordinate with development team before deleting any data.

---

## Appendix B: Common Issues & Troubleshooting

### Issue: Cannot Login
- **Check**: Environment is UP (health endpoint)
- **Check**: Phone number format correct (+44-...)
- **Check**: OTP is 123456 (development mode)
- **Solution**: Clear browser cache, try again

### Issue: File Upload Fails
- **Check**: File size < 10MB
- **Check**: File format is PDF/JPG/PNG
- **Check**: Internet connection stable
- **Solution**: Try smaller file or different format

### Issue: Buttons Not Visible
- **Check**: Logged in with correct role (Admin vs Business User)
- **Check**: Organization status is correct for the action
- **Solution**: Logout and login with appropriate role

### Issue: Data Not Saved
- **Check**: Required fields filled
- **Check**: No validation errors shown
- **Check**: Network tab in browser DevTools for errors
- **Solution**: Check console for error messages, report to AI

---

## Appendix C: Browser DevTools Usage

### Opening DevTools
- **Windows**: Press `F12` or `Ctrl+Shift+I`
- **Mac**: Press `Cmd+Option+I`

### Useful Tabs

#### Console Tab
- Shows JavaScript errors and logs
- Copy error messages for bug reports

#### Network Tab
- Shows API calls
- Check for failed requests (red entries)
- Click on failed request to see error details

#### Application Tab
- View stored data (JWT token, user info)
- Clear cache and storage

### How to Copy Error Messages
1. Open Console tab
2. Look for red error messages
3. Right-click error
4. Select "Copy message"
5. Paste into bug report

---

**End of Test Plan**

**Version**: 3.0.0  
**Document Owner**: Development Team  
**Last Updated**: May 18, 2026

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
