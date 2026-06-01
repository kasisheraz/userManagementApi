# Beneficiary Management - UI Test Plan

**Version:** 2.2.0  
**Module:** Beneficiary Management  
**Test Environment:** NPE (Non-Production Environment)  
**Test Date:** June 1, 2026  
**Tester:** _____________________

## Prerequisites

### Environment Setup
- **Frontend URL:** https://fincore-npe-ui-[PROJECT-ID].europe-west2.run.app (TBD after frontend deployment)
- **Backend API URL:** https://fincore-npe-api-994490239798.europe-west2.run.app
- **Test Users Required:**
  - Business User (OPERATIONAL_USER or BUSINESS_USER role)
  - Admin User (COMPLIANCE_OFFICER or SYSTEM_ADMINISTRATOR role)

### Test Data Preparation
```
Business User Test Account:
- Phone: +1234567890
- OTP: 123456 (dev mode)

Admin User Test Account:
- Phone: +9876543210
- OTP: 123456 (dev mode)
```

---

## Test Suite 1: Business User - Basic CRUD Operations

### Test Case 1.1: Create Standard Beneficiary
**Objective:** Verify business user can create a standard beneficiary

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Login as Business User | Dashboard loads successfully | ☐ |
| 2 | Navigate to Beneficiaries menu | Beneficiaries list page displays with "Add Beneficiary" button | ☐ |
| 3 | Click "Add Beneficiary" button | Beneficiary form opens | ☐ |
| 4 | Fill Basic Info:<br>- Beneficiary Name: "John Smith"<br>- Nick Name: "Johnny"<br>- Business Name: "Smith Enterprises"<br>- Country: "United States" | All fields populate correctly | ☐ |
| 5 | Fill Address section:<br>- Address Line 1: "123 Main St"<br>- City: "New York"<br>- State: "NY"<br>- Postal Code: "10001"<br>- Country: "United States" | Address form accepts all inputs | ☐ |
| 6 | Leave "Counter Over Counter" unchecked | Collector Contact field remains hidden | ☐ |
| 7 | Click "Save" button | Success message appears, redirected to beneficiary details page | ☐ |
| 8 | Verify beneficiary details | Status shows "PENDING", all entered data displays correctly | ☐ |

**Test Data:**
```
Beneficiary Name: John Smith
Nick Name: Johnny
Business Name: Smith Enterprises
Country: United States
Address: 123 Main St, New York, NY 10001
C2C: No
```

---

### Test Case 1.2: Create Counter-Over-Counter (C2C) Beneficiary
**Objective:** Verify C2C beneficiary creation with collector phone validation

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Click "Add Beneficiary" | Form opens | ☐ |
| 2 | Fill Basic Info:<br>- Beneficiary Name: "Maria Garcia"<br>- Nick Name: "Maria"<br>- Country: "Mexico" | Fields populate | ☐ |
| 3 | Fill Address section (complete address) | Address saves | ☐ |
| 4 | Check "Counter Over Counter" checkbox | Collector Contact field appears (required) | ☐ |
| 5 | Leave Collector Contact empty and click Save | Validation error: "Collector contact is required for C2C" | ☐ |
| 6 | Enter invalid phone: "123" | Validation error for invalid phone format | ☐ |
| 7 | Enter valid phone: "+52-555-1234567" | Validation passes | ☐ |
| 8 | Click "Save" | C2C beneficiary created successfully | ☐ |
| 9 | Verify details page | C2C flag shows "Yes", Collector Contact displays | ☐ |

**Test Data:**
```
Beneficiary Name: Maria Garcia
Nick Name: Maria
Country: Mexico
Address: Calle Principal 456, Mexico City, CDMX 03100
C2C: Yes
Collector Phone: +52-555-1234567
```

---

### Test Case 1.3: Edit Beneficiary (PENDING Status Only)
**Objective:** Verify only PENDING beneficiaries can be edited

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | From beneficiaries list, find PENDING beneficiary | PENDING status shows in list | ☐ |
| 2 | Click "Edit" icon/button | Edit form opens with existing data | ☐ |
| 3 | Modify Nick Name: "Johnny Boy" | Field updates | ☐ |
| 4 | Modify Business Name: "Smith & Co" | Field updates | ☐ |
| 5 | Click "Save" | Updates saved successfully | ☐ |
| 6 | Verify details page | Updated values display correctly | ☐ |
| 7 | Submit beneficiary for review (see Test 1.7) | Status changes to UNDER_REVIEW | ☐ |
| 8 | Try to click "Edit" on UNDER_REVIEW beneficiary | Edit button disabled or shows error: "Can only edit PENDING beneficiaries" | ☐ |

---

### Test Case 1.4: View Beneficiary Details
**Objective:** Verify all beneficiary information displays correctly

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Click on any beneficiary from list | Details page opens | ☐ |
| 2 | Verify Basic Info section | Shows: Name, Nick Name, Business Name, Country, C2C flag | ☐ |
| 3 | Verify Address section | Shows complete address with all fields | ☐ |
| 4 | Verify Status banner | Shows current status with color coding:<br>- PENDING: Blue<br>- UNDER_REVIEW: Orange<br>- ACTIVE: Green<br>- REJECTED: Red<br>- SUSPENDED: Gray | ☐ |
| 5 | If REJECTED/SUSPENDED | Reason displays in banner | ☐ |
| 6 | Verify KYC Documents section | Lists required documents with upload status | ☐ |
| 7 | Verify Audit Info | Shows Created By, Created At, Modified By, Modified At | ☐ |

---

## Test Suite 2: KYC Document Management

### Test Case 2.1: Upload Required KYC Documents (Standard Beneficiary)
**Objective:** Verify KYC document upload for standard beneficiary

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open PENDING beneficiary details | KYC Documents section shows required list | ☐ |
| 2 | Verify required documents list | Shows:<br>1. BENEFICIARY_ID_PROOF<br>2. BENEFICIARY_ADDRESS_PROOF<br>3. BENEFICIARY_BANK_DETAILS<br>4. BENEFICIARY_OTHER (optional) | ☐ |
| 3 | Click "Upload" for BENEFICIARY_ID_PROOF | File picker opens | ☐ |
| 4 | Select valid PDF file (< 10MB) | Upload progresses and completes | ☐ |
| 5 | Verify uploaded document | Shows in table with file name, type, upload date | ☐ |
| 6 | Upload BENEFICIARY_ADDRESS_PROOF (image: JPG) | Uploads successfully | ☐ |
| 7 | Upload BENEFICIARY_BANK_DETAILS (PDF) | Uploads successfully | ☐ |
| 8 | Try to upload file > 10MB | Validation error: "File size exceeds 10MB limit" | ☐ |
| 9 | Try to upload invalid format (.exe) | Validation error: "Invalid file format" | ☐ |

**Test Files Needed:**
- Valid PDF < 10MB
- Valid JPG/PNG < 10MB
- PDF > 10MB (for validation test)
- Invalid file type (.exe, .txt)

---

### Test Case 2.2: Upload C2C-Specific Documents
**Objective:** Verify C2C beneficiaries require additional document

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open C2C beneficiary details | KYC Documents section displays | ☐ |
| 2 | Verify required documents list | Shows 4 required (including BENEFICIARY_C2C_AUTHORIZATION) + 1 optional | ☐ |
| 3 | Upload all 3 standard documents | Documents upload successfully | ☐ |
| 4 | Click "Submit for Review" without C2C doc | Error: "C2C beneficiary requires C2C Authorization document" | ☐ |
| 5 | Upload BENEFICIARY_C2C_AUTHORIZATION | Uploads successfully | ☐ |
| 6 | Click "Submit for Review" | Submission proceeds successfully | ☐ |

---

### Test Case 2.3: View and Download Documents
**Objective:** Verify document viewing and download functionality

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open beneficiary with uploaded documents | Documents table displays | ☐ |
| 2 | Click "View" on a document | Document opens in new tab with signed URL | ☐ |
| 3 | Verify document content | Correct document displays | ☐ |
| 4 | Click "Download" button | Document downloads to local machine | ☐ |
| 5 | Verify downloaded file | File is valid and matches uploaded content | ☐ |

---

### Test Case 2.4: Delete Uploaded Document
**Objective:** Verify document deletion (only for PENDING status)

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open PENDING beneficiary with documents | Documents table shows with Delete option | ☐ |
| 2 | Click "Delete" on a document | Confirmation dialog appears | ☐ |
| 3 | Click "Cancel" | Dialog closes, document remains | ☐ |
| 4 | Click "Delete" again, then "Confirm" | Document removed from list | ☐ |
| 5 | Submit beneficiary (status becomes UNDER_REVIEW) | Status changes | ☐ |
| 6 | Try to delete document from UNDER_REVIEW beneficiary | Delete button disabled or error shown | ☐ |

---

## Test Suite 3: Workflow Operations

### Test Case 3.1: Submit Beneficiary for Review
**Objective:** Verify submission validates required documents

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Create new beneficiary (PENDING) | Beneficiary created | ☐ |
| 2 | Click "Submit for Review" without documents | Error: "Please upload required KYC documents" | ☐ |
| 3 | Upload only 1 of 3 required documents | Submit still disabled/shows error | ☐ |
| 4 | Upload remaining required documents (total 3) | Submit button becomes enabled | ☐ |
| 5 | Click "Submit for Review" | Confirmation dialog appears | ☐ |
| 6 | Click "Confirm" | Success message, status changes to UNDER_REVIEW | ☐ |
| 7 | Verify beneficiary details | Status banner shows "UNDER_REVIEW", Edit/Delete buttons disabled | ☐ |
| 8 | Check beneficiaries list | Beneficiary appears in "Under Review" tab | ☐ |

---

### Test Case 3.2: Delete Beneficiary (PENDING Only)
**Objective:** Verify only PENDING beneficiaries can be deleted

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | From list, find PENDING beneficiary | Delete icon/button visible | ☐ |
| 2 | Click "Delete" | Confirmation dialog appears: "Are you sure?" | ☐ |
| 3 | Click "Cancel" | Dialog closes, beneficiary remains | ☐ |
| 4 | Click "Delete" again, then "Confirm" | Beneficiary removed from list | ☐ |
| 5 | Verify beneficiary count | Count decreases by 1 | ☐ |
| 6 | Find UNDER_REVIEW or ACTIVE beneficiary | Delete button disabled/hidden | ☐ |

---

## Test Suite 4: Search and Filter

### Test Case 4.1: Status Filter Tabs
**Objective:** Verify status-based filtering

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Navigate to Beneficiaries list | Default shows "All" tab | ☐ |
| 2 | Verify tab counts | Each tab shows count: All (X), Pending (Y), Under Review (Z), Active (A), Rejected (B), Suspended (C) | ☐ |
| 3 | Click "Pending" tab | Only PENDING beneficiaries display | ☐ |
| 4 | Click "Under Review" tab | Only UNDER_REVIEW beneficiaries display | ☐ |
| 5 | Click "Active" tab | Only ACTIVE beneficiaries display | ☐ |
| 6 | Click "Rejected" tab | Only REJECTED beneficiaries display | ☐ |
| 7 | Click "Suspended" tab | Only SUSPENDED beneficiaries display | ☐ |
| 8 | Click "All" tab | All beneficiaries display regardless of status | ☐ |

---

### Test Case 4.2: Search Functionality
**Objective:** Verify search across multiple fields

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Enter beneficiary name in search box | Results filter to matching names | ☐ |
| 2 | Clear search, enter nick name | Results filter to matching nick names | ☐ |
| 3 | Clear search, enter business name | Results filter to matching business names | ☐ |
| 4 | Enter partial text (e.g., "Smi" for "Smith") | Partial matches appear | ☐ |
| 5 | Enter text with no matches | "No results found" message displays | ☐ |
| 6 | Clear search | All beneficiaries reappear | ☐ |

---

### Test Case 4.3: Country Filter
**Objective:** Verify country-based filtering

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open country dropdown filter | List of countries displays | ☐ |
| 2 | Select "United States" | Only US beneficiaries display | ☐ |
| 3 | Verify count badge | Count shows filtered total | ☐ |
| 4 | Change to "Mexico" | Only Mexico beneficiaries display | ☐ |
| 5 | Select "All Countries" | All beneficiaries display | ☐ |

---

### Test Case 4.4: Combined Filters
**Objective:** Verify filters work together

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Select "Pending" status tab | PENDING beneficiaries display | ☐ |
| 2 | Enter search text | Results filtered by both status AND search | ☐ |
| 3 | Add country filter | Results filtered by status, search, AND country | ☐ |
| 4 | Clear all filters | All beneficiaries display | ☐ |

---

## Test Suite 5: Business Rules Validation

### Test Case 5.1: 20-Beneficiary Limit
**Objective:** Verify user cannot create more than 20 beneficiaries

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Check current beneficiary count | Count displays in header/button | ☐ |
| 2 | If count < 20, create beneficiaries until reaching 20 | Each creation succeeds | ☐ |
| 3 | Verify count shows "20/20" | Limit indicator displays | ☐ |
| 4 | Try to click "Add Beneficiary" | Button disabled or error message: "Maximum limit of 20 beneficiaries reached" | ☐ |
| 5 | Delete 1 beneficiary | Count becomes "19/20", Add button re-enabled | ☐ |
| 6 | Create new beneficiary | Creation succeeds, count returns to "20/20" | ☐ |

**Note:** This test requires creating multiple test beneficiaries. Use distinct names like "Test Beneficiary 01", "Test Beneficiary 02", etc.

---

### Test Case 5.2: C2C Collector Phone Validation
**Objective:** Verify phone number format validation

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Create C2C beneficiary | Form opens | ☐ |
| 2 | Enter invalid phone formats and test:<br>- "123" ❌<br>- "abcdefg" ❌<br>- "123-456-789" ❌<br>- "12345" ❌ | Each shows validation error | ☐ |
| 3 | Enter valid phone formats and test:<br>- "+1-234-567-8900" ✅<br>- "+52-555-1234567" ✅<br>- "+44-20-1234-5678" ✅ | Each passes validation | ☐ |

---

## Test Suite 6: Admin Operations

**Login as Admin User for these tests**

### Test Case 6.1: View All Beneficiaries (Admin)
**Objective:** Verify admin can see all users' beneficiaries

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Login as Admin | Dashboard loads | ☐ |
| 2 | Navigate to Beneficiaries | List shows all beneficiaries from all users | ☐ |
| 3 | Verify each beneficiary shows owner info | "Created By" field shows user name | ☐ |

---

### Test Case 6.2: View Pending Approvals
**Objective:** Verify admin can filter pending approvals

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Click "Under Review" tab | Only UNDER_REVIEW beneficiaries display | ☐ |
| 2 | Verify action buttons available | Approve and Reject buttons visible for each | ☐ |

---

### Test Case 6.3: Approve Beneficiary
**Objective:** Verify admin can approve beneficiaries

| Step | Action | expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Find UNDER_REVIEW beneficiary | Status shows correctly | ☐ |
| 2 | Click "Approve" button | Confirmation dialog appears | ☐ |
| 3 | Click "Confirm" | Success message appears | ☐ |
| 4 | Verify status change | Status updates to ACTIVE | ☐ |
| 5 | Check "Active" tab | Approved beneficiary appears there | ☐ |
| 6 | Check "Under Review" tab | Beneficiary removed from this list | ☐ |
| 7 | View beneficiary details | Status banner shows "ACTIVE" in green | ☐ |

---

### Test Case 6.4: Reject Beneficiary with Reason
**Objective:** Verify admin can reject with mandatory reason

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Find UNDER_REVIEW beneficiary | Reject button visible | ☐ |
| 2 | Click "Reject" button | Rejection dialog opens with reason text field | ☐ |
| 3 | Leave reason empty, click Submit | Validation error: "Rejection reason is required" | ☐ |
| 4 | Enter reason: "Incomplete documentation" | Text accepts input | ☐ |
| 5 | Click "Submit" | Success message, status changes to REJECTED | ☐ |
| 6 | View beneficiary details | Status banner shows "REJECTED" in red with reason displayed | ☐ |
| 7 | Check "Rejected" tab | Beneficiary appears in this list | ☐ |
| 8 | Verify reason visible in list | Reason truncated with "..." or tooltip | ☐ |

---

### Test Case 6.5: Suspend Active Beneficiary with Reason
**Objective:** Verify admin can suspend active beneficiaries

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Find ACTIVE beneficiary | Suspend button visible | ☐ |
| 2 | Click "Suspend" button | Suspension dialog opens with reason field | ☐ |
| 3 | Leave reason empty, click Submit | Validation error: "Suspension reason is required" | ☐ |
| 4 | Enter reason: "Pending verification" | Text accepts input | ☐ |
| 5 | Click "Submit" | Success message, status changes to SUSPENDED | ☐ |
| 6 | View beneficiary details | Status banner shows "SUSPENDED" in gray with reason | ☐ |
| 7 | Check "Suspended" tab | Beneficiary appears there | ☐ |
| 8 | Verify Reactivate button visible | Reactivate action available | ☐ |

---

### Test Case 6.6: Reactivate Suspended Beneficiary
**Objective:** Verify admin can reactivate suspended beneficiaries

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Find SUSPENDED beneficiary | Reactivate button visible | ☐ |
| 2 | Click "Reactivate" button | Confirmation dialog appears | ☐ |
| 3 | Click "Confirm" | Success message, status changes to ACTIVE | ☐ |
| 4 | View beneficiary details | Status banner shows "ACTIVE", suspension reason cleared | ☐ |
| 5 | Check "Active" tab | Beneficiary appears there | ☐ |
| 6 | Verify Suspend button available | Can suspend again if needed | ☐ |

---

### Test Case 6.7: Admin Search and Statistics
**Objective:** Verify admin-specific search and statistics features

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Navigate to Beneficiaries (Admin view) | Statistics panel visible | ☐ |
| 2 | Verify statistics display | Shows:<br>- Total Beneficiaries<br>- Pending Count<br>- Under Review Count<br>- Active Count<br>- Rejected Count<br>- Suspended Count | ☐ |
| 3 | Use admin search to find by user | Can filter by "Created By" user | ☐ |
| 4 | Verify counts update with filters | Statistics reflect filtered results | ☐ |

---

## Test Suite 7: Role-Based Access Control

### Test Case 7.1: Business User Permissions
**Objective:** Verify business users cannot access admin functions

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Login as Business User | Access granted | ☐ |
| 2 | Navigate to Beneficiaries | Can view own beneficiaries only | ☐ |
| 3 | Check for admin actions | Approve/Reject/Suspend/Reactivate buttons NOT visible | ☐ |
| 4 | View UNDER_REVIEW beneficiary | Can view but cannot approve/reject | ☐ |
| 5 | Try to access admin statistics | Statistics panel NOT visible or shows limited info | ☐ |

---

### Test Case 7.2: Admin User Permissions
**Objective:** Verify admins have full access

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Login as Admin | Access granted | ☐ |
| 2 | Navigate to Beneficiaries | Can view all users' beneficiaries | ☐ |
| 3 | Check available actions | All admin actions visible: Approve, Reject, Suspend, Reactivate | ☐ |
| 4 | View statistics dashboard | Full statistics visible | ☐ |
| 5 | Perform admin action (Approve) | Action executes successfully | ☐ |

---

## Test Suite 8: UI/UX Validation

### Test Case 8.1: Responsive Design
**Objective:** Verify UI works on different screen sizes

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Open application on desktop (1920x1080) | Layout displays properly | ☐ |
| 2 | Resize to tablet (768x1024) | Layout adjusts, all features accessible | ☐ |
| 3 | Resize to mobile (375x667) | Layout stacks vertically, navigation works | ☐ |
| 4 | Test form inputs on mobile | All fields tappable and usable | ☐ |

---

### Test Case 8.2: Status Color Coding
**Objective:** Verify status badges use correct colors

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | View beneficiary with PENDING status | Badge is BLUE | ☐ |
| 2 | View beneficiary with UNDER_REVIEW status | Badge is ORANGE/YELLOW | ☐ |
| 3 | View beneficiary with ACTIVE status | Badge is GREEN | ☐ |
| 4 | View beneficiary with REJECTED status | Badge is RED | ☐ |
| 5 | View beneficiary with SUSPENDED status | Badge is GRAY | ☐ |

---

### Test Case 8.3: Loading States
**Objective:** Verify loading indicators display correctly

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Navigate to Beneficiaries list | Loading spinner appears while data loads | ☐ |
| 2 | Submit beneficiary for review | Loading indicator on submit button | ☐ |
| 3 | Upload KYC document | Upload progress indicator displays | ☐ |
| 4 | Perform admin action | Loading state shows during processing | ☐ |

---

### Test Case 8.4: Error Handling
**Objective:** Verify user-friendly error messages

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Submit form with missing required fields | Clear validation messages appear | ☐ |
| 2 | Attempt invalid operation (edit ACTIVE beneficiary) | Error message explains why action is blocked | ☐ |
| 3 | Simulate network error (disconnect internet) | Error message: "Network error, please try again" | ☐ |
| 4 | Upload invalid file type | Error specifies allowed formats | ☐ |

---

## Test Suite 9: Data Integrity

### Test Case 9.1: Audit Trail Verification
**Objective:** Verify audit information is captured correctly

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Create beneficiary as User A | Created By shows User A's name | ☐ |
| 2 | Note Created At timestamp | Timestamp matches current time | ☐ |
| 3 | Edit beneficiary as User B (if admin) | Modified By changes to User B | ☐ |
| 4 | Note Last Modified At timestamp | Timestamp updates to modification time | ☐ |
| 5 | View audit trail | All changes tracked with user and timestamp | ☐ |

---

### Test Case 9.2: Data Persistence
**Objective:** Verify data persists across sessions

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Create beneficiary with specific data | Beneficiary created | ☐ |
| 2 | Logout | User logged out | ☐ |
| 3 | Login again | User logged in | ☐ |
| 4 | Navigate to Beneficiaries | Previously created beneficiary still exists | ☐ |
| 5 | Verify all details | All data matches original input | ☐ |

---

## Test Suite 10: End-to-End Workflow

### Test Case 10.1: Complete Beneficiary Lifecycle (Happy Path)
**Objective:** Test complete workflow from creation to activation

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| **Business User Actions** ||||
| 1 | Login as Business User | Login successful | ☐ |
| 2 | Create new beneficiary | PENDING status | ☐ |
| 3 | Upload 3 required KYC documents | All documents uploaded | ☐ |
| 4 | Submit for review | Status changes to UNDER_REVIEW | ☐ |
| 5 | Verify cannot edit or delete | Actions disabled | ☐ |
| 6 | Logout | Logout successful | ☐ |
| **Admin User Actions** ||||
| 7 | Login as Admin | Login successful | ☐ |
| 8 | Navigate to Beneficiaries → Under Review tab | Submitted beneficiary appears | ☐ |
| 9 | Review beneficiary details | All information and documents visible | ☐ |
| 10 | Click Approve | Approval confirmation dialog | ☐ |
| 11 | Confirm approval | Status changes to ACTIVE | ☐ |
| 12 | Verify in Active tab | Beneficiary now in Active list | ☐ |
| 13 | Logout | Logout successful | ☐ |
| **Verification** ||||
| 14 | Login as original Business User | Login successful | ☐ |
| 15 | View beneficiary in Active tab | Beneficiary shows as ACTIVE | ☐ |
| 16 | Verify Edit/Delete still disabled | Cannot modify ACTIVE beneficiary | ☐ |

---

### Test Case 10.2: Rejection and Resubmission Workflow
**Objective:** Test rejection and resubmission process

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Business User submits beneficiary | UNDER_REVIEW status | ☐ |
| 2 | Admin rejects with reason: "Missing bank statement" | Status becomes REJECTED | ☐ |
| 3 | Business User views rejected beneficiary | Sees rejection reason | ☐ |
| 4 | Business User cannot resubmit REJECTED beneficiary | No resubmit option (must create new) | ☐ |
| 5 | Business User creates new beneficiary with correct documents | New beneficiary created | ☐ |
| 6 | Submit new beneficiary | Goes to UNDER_REVIEW | ☐ |
| 7 | Admin approves | Status changes to ACTIVE | ☐ |

---

### Test Case 10.3: Suspension and Reactivation Workflow
**Objective:** Test suspension and reactivation process

| Step | Action | Expected Result | Pass/Fail |
|------|--------|----------------|-----------|
| 1 | Admin finds ACTIVE beneficiary | Beneficiary in Active tab | ☐ |
| 2 | Admin clicks Suspend with reason: "Routine compliance check" | Status changes to SUSPENDED | ☐ |
| 3 | Business User views suspended beneficiary | Sees SUSPENDED status and reason | ☐ |
| 4 | Admin completes verification, clicks Reactivate | Status changes back to ACTIVE | ☐ |
| 5 | Business User confirms beneficiary active again | Shows as ACTIVE | ☐ |

---

## Test Execution Summary

### Overall Results

| Category | Total Tests | Passed | Failed | Blocked | Not Tested |
|----------|-------------|--------|--------|---------|------------|
| **CRUD Operations** | 4 | ☐ | ☐ | ☐ | ☐ |
| **KYC Management** | 4 | ☐ | ☐ | ☐ | ☐ |
| **Workflow Operations** | 2 | ☐ | ☐ | ☐ | ☐ |
| **Search & Filter** | 4 | ☐ | ☐ | ☐ | ☐ |
| **Business Rules** | 2 | ☐ | ☐ | ☐ | ☐ |
| **Admin Operations** | 7 | ☐ | ☐ | ☐ | ☐ |
| **Access Control** | 2 | ☐ | ☐ | ☐ | ☐ |
| **UI/UX** | 4 | ☐ | ☐ | ☐ | ☐ |
| **Data Integrity** | 2 | ☐ | ☐ | ☐ | ☐ |
| **End-to-End** | 3 | ☐ | ☐ | ☐ | ☐ |
| **TOTAL** | **34** | **0** | **0** | **0** | **34** |

### Test Coverage
- [ ] All Business User features tested
- [ ] All Admin features tested
- [ ] All validation rules tested
- [ ] All workflow states tested
- [ ] Edge cases covered
- [ ] Error handling verified
- [ ] UI/UX validated

---

## Issues & Defects

### Critical Issues
_No issues found / List issues here_

### Major Issues
_No issues found / List issues here_

### Minor Issues
_No issues found / List issues here_

### Enhancement Requests
_List any enhancement suggestions_

---

## Sign-Off

**Tester Name:** _____________________  
**Tester Signature:** _____________________  
**Date:** _____________________

**Test Status:** ☐ Passed ☐ Passed with Minor Issues ☐ Failed

**Comments:**
_Add any additional observations or recommendations_

---

## Appendix A: Test Environment Details

```yaml
Backend API:
  URL: https://fincore-npe-api-994490239798.europe-west2.run.app
  Version: 2.2.0
  Database: Cloud SQL MySQL 8.0
  
Frontend UI:
  URL: [To be updated after deployment]
  Version: 2.2.0
  Framework: React 18 + TypeScript

Authentication:
  Method: JWT (Phone + OTP)
  Dev Mode OTP: 123456
  Token Expiry: 24 hours

File Upload:
  Storage: Google Cloud Storage
  Bucket: fincore-kyc-documents
  Max File Size: 10MB
  Allowed Formats: PDF, JPG, JPEG, PNG
```

---

## Appendix B: Quick Reference - Status Meanings

| Status | Description | Who Can Change | Next States |
|--------|-------------|----------------|-------------|
| **PENDING** | Draft state after creation | Business User (Edit/Delete) | UNDER_REVIEW (Submit) |
| **UNDER_REVIEW** | Submitted for admin review | Admin only | ACTIVE (Approve)<br>REJECTED (Reject) |
| **ACTIVE** | Approved and operational | Admin only | SUSPENDED (Suspend) |
| **REJECTED** | Rejected by admin | Cannot be changed | N/A (Create new instead) |
| **SUSPENDED** | Temporarily inactive | Admin only | ACTIVE (Reactivate) |

---

## Appendix C: Test Data Templates

### Standard Beneficiary Template
```
Beneficiary Name: [Full Name]
Nick Name: [Short Name]
Business Name: [Company Name]
Country: [Country]
Address:
  Line 1: [Street Address]
  Line 2: [Apt/Suite] (optional)
  City: [City]
  State/Province: [State]
  Postal Code: [ZIP/Postal]
  Country: [Country]
C2C: No
```

### C2C Beneficiary Template
```
Beneficiary Name: [Full Name]
Nick Name: [Short Name]
Business Name: [Company Name]
Country: [Country]
Address: [Complete Address]
C2C: Yes
Collector Phone: +[Country Code]-[Phone Number]
```

---

**END OF TEST PLAN**
