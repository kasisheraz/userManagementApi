# FinCore Platform - Test Execution Tracker (Excel Template)

**Purpose**: Track manual test execution, record results, and capture bug details for AI feedback  
**Format**: Microsoft Excel (.xlsx) or Google Sheets  
**Version**: 1.0  
**Date**: May 18, 2026

---

## Excel Workbook Structure

The Excel workbook should contain **5 worksheets**:

1. **Summary Dashboard** - Overall test progress and metrics
2. **Test Execution** - Main test case execution tracking
3. **Bug Tracker** - Detailed bug/issue tracking
4. **Test Data** - Reference data for testing
5. **AI Feedback Log** - Record of AI feedback cycles

---

## Worksheet 1: Summary Dashboard

### Purpose
Provides at-a-glance view of testing progress for management and QA team.

### Layout

#### Section 1: Header Information
| Field | Value |
|-------|-------|
| **Project Name** | FinCore Platform |
| **Test Environment** | NPE |
| **Test Cycle** | Cycle 1 |
| **Start Date** | [Enter date] |
| **End Date** | [Enter date] |
| **QA Tester** | [Enter name] |
| **Status** | In Progress / Completed |

#### Section 2: Test Progress Metrics
| Metric | Value | Formula/Calculation |
|--------|-------|---------------------|
| **Total Test Cases** | 34 | =COUNTA(TestExecution!A2:A35) |
| **Executed** | 0 | =COUNTIF(TestExecution!K:K,"PASS")+COUNTIF(TestExecution!K:K,"FAIL") |
| **Passed** | 0 | =COUNTIF(TestExecution!K:K,"PASS") |
| **Failed** | 0 | =COUNTIF(TestExecution!K:K,"FAIL") |
| **Blocked** | 0 | =COUNTIF(TestExecution!K:K,"BLOCKED") |
| **Not Executed** | 34 | =B2-B3 |
| **Pass Rate %** | 0% | =B4/B3*100 |
| **Execution Progress %** | 0% | =B3/B2*100 |

#### Section 3: Module-wise Summary
| Module | Total | Executed | Passed | Failed | Pass % |
|--------|-------|----------|--------|--------|--------|
| User Management & Authentication | 5 | 0 | 0 | 0 | 0% |
| Organization Management | 8 | 0 | 0 | 0 | 0% |
| KYC Document Upload | 6 | 0 | 0 | 0 | 0% |
| Admin Approval Workflow | 5 | 0 | 0 | 0 | 0% |
| Rejection & Resubmission | 4 | 0 | 0 | 0 | 0% |
| Questionnaire Management | 3 | 0 | 0 | 0 | 0% |
| Customer Answers | 3 | 0 | 0 | 0 | 0% |

**Formulas**:
- Executed: `=COUNTIFS(TestExecution!$B:$B,"User Management",TestExecution!$K:$K,"<>NOT EXECUTED")`
- Passed: `=COUNTIFS(TestExecution!$B:$B,"User Management",TestExecution!$K:$K,"PASS")`
- Failed: `=COUNTIFS(TestExecution!$B:$B,"User Management",TestExecution!$K:$K,"FAIL")`
- Pass %: `=(Passed/Executed)*100`

#### Section 4: Bug Summary
| Severity | Count | Percentage |
|----------|-------|------------|
| Critical 🔴 | 0 | 0% |
| High 🟠 | 0 | 0% |
| Medium 🟡 | 0 | 0% |
| Low 🟢 | 0 | 0% |
| **Total** | 0 | 100% |

**Formulas**: `=COUNTIF(BugTracker!E:E,"Critical")`

#### Section 5: Daily Progress Chart
Create a line chart showing:
- X-axis: Date
- Y-axis: Number of tests
- Lines: Executed (cumulative), Passed (cumulative), Failed (cumulative)

---

## Worksheet 2: Test Execution

### Purpose
Main worksheet for recording test case execution results.

### Column Structure

| Col | Column Name | Width | Data Type | Validation/Format | Description |
|-----|-------------|-------|-----------|-------------------|-------------|
| A | **Test Case ID** | 15 | Text | - | Unique identifier (e.g., TC-AUTH-001) |
| B | **Module** | 20 | Dropdown | List: User Management, Organization Management, KYC Documents, Admin Approval, Rejection & Resubmission, Questionnaire, Customer Answers | Feature module |
| C | **Test Case Name** | 40 | Text | - | Descriptive name of test case |
| D | **Priority** | 12 | Dropdown | List: Critical, High, Medium, Low | Test priority |
| E | **Type** | 15 | Dropdown | List: Functional, Validation, Security, Negative, End-to-End | Test type |
| F | **Preconditions** | 30 | Text | Wrap text | Prerequisites for test |
| G | **Test Steps** | 50 | Text | Wrap text | Numbered steps to execute |
| H | **Expected Result** | 40 | Text | Wrap text | What should happen |
| I | **Test Data** | 25 | Text | Wrap text | Data used for testing |
| J | **Executed By** | 15 | Text | - | QA tester name |
| K | **Status** | 12 | Dropdown | List: NOT EXECUTED, PASS, FAIL, BLOCKED | Test result |
| L | **Execution Date** | 15 | Date | dd-mmm-yyyy | When test was run |
| M | **Actual Result** | 40 | Text | Wrap text | What actually happened |
| N | **Bug ID** | 12 | Text | - | Link to bug (if failed) |
| O | **Screenshots** | 20 | Text | - | Screenshot file names |
| P | **Comments** | 35 | Text | Wrap text | Additional notes |
| Q | **Retest Status** | 12 | Dropdown | List: N/A, PASS, FAIL | After bug fix |
| R | **Retest Date** | 15 | Date | dd-mmm-yyyy | When retested |

### Conditional Formatting Rules

1. **Status Column (K)**:
   - PASS = Green fill (RGB: 198, 239, 206), dark green text
   - FAIL = Red fill (RGB: 255, 199, 206), dark red text
   - BLOCKED = Orange fill (RGB: 255, 235, 156), dark orange text
   - NOT EXECUTED = Gray fill (RGB: 242, 242, 242), gray text

2. **Priority Column (D)**:
   - Critical = Red text, bold
   - High = Orange text, bold
   - Medium = Blue text
   - Low = Gray text

3. **Entire Row**:
   - If Status = FAIL, highlight entire row with light red background

### Sample Data Rows

| A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | User Management | Successful Login | Critical | Functional | NPE env running, test accounts exist | 1. Open URL<br>2. Enter +44-7700-900002<br>3. Click Send OTP<br>4. Enter 123456<br>5. Click Login | Dashboard displayed with user name | +44-7700-900002, OTP: 123456 | QA Tester | NOT EXECUTED | | | | | | N/A | |
| TC-AUTH-002 | User Management | Invalid OTP | High | Negative | NPE env running | 1. Open URL<br>2. Enter +44-7700-900002<br>3. Click Send OTP<br>4. Enter 999999<br>5. Click Login | Error: "Invalid OTP code" shown | +44-7700-900002, OTP: 999999 | QA Tester | NOT EXECUTED | | | | | | N/A | |

**Continue for all 34 test cases...**

### Tips for Using This Worksheet
1. **Filter by Module**: Use filter to focus on one module at a time
2. **Sort by Priority**: Execute Critical tests first
3. **Color Coding**: Use conditional formatting to quickly see pass/fail
4. **Daily Updates**: Update after each test execution
5. **Export Failures**: Filter Status=FAIL to prepare AI feedback

---

## Worksheet 3: Bug Tracker

### Purpose
Detailed tracking of all bugs/issues found during testing.

### Column Structure

| Col | Column Name | Width | Data Type | Validation/Format | Description |
|-----|-------------|-------|-----------|-------------------|-------------|
| A | **Bug ID** | 12 | Text | BUG-001, BUG-002... | Unique bug identifier |
| B | **Test Case ID** | 15 | Text | - | Related test case |
| C | **Module** | 20 | Dropdown | Same as Test Execution | Feature area |
| D | **Bug Summary** | 45 | Text | Wrap text | One-line description |
| E | **Severity** | 12 | Dropdown | List: Critical, High, Medium, Low | Impact level |
| F | **Priority** | 12 | Dropdown | List: Critical, High, Medium, Low | Fix urgency |
| G | **Steps to Reproduce** | 50 | Text | Wrap text | Numbered steps |
| H | **Expected Result** | 35 | Text | Wrap text | What should happen |
| I | **Actual Result** | 35 | Text | Wrap text | What actually happened |
| J | **Error Message** | 40 | Text | Wrap text | Error text from UI/console |
| K | **Screenshots** | 25 | Text | - | Screenshot file names |
| L | **Browser** | 15 | Text | - | Browser + version |
| M | **Reported By** | 15 | Text | - | QA tester name |
| N | **Reported Date** | 15 | Date | dd-mmm-yyyy | When found |
| O | **Status** | 15 | Dropdown | List: New, Sent to AI, In Progress, Fixed, Verified, Won't Fix | Bug status |
| P | **AI Feedback Sent** | 15 | Date | dd-mmm-yyyy | When sent to AI |
| Q | **Fixed Date** | 15 | Date | dd-mmm-yyyy | When deployed |
| R | **Verified Date** | 15 | Date | dd-mmm-yyyy | When QA verified fix |
| S | **Notes** | 35 | Text | Wrap text | Additional comments |

### Conditional Formatting Rules

1. **Severity Column (E)**:
   - Critical = Red fill, white text, bold
   - High = Orange fill, black text, bold
   - Medium = Yellow fill, black text
   - Low = Light green fill, black text

2. **Status Column (O)**:
   - New = Red fill
   - Sent to AI = Orange fill
   - In Progress = Yellow fill
   - Fixed = Light blue fill
   - Verified = Green fill
   - Won't Fix = Gray fill

### Sample Data Row

| A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P | Q | R | S |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| BUG-001 | TC-ORG-001 | Organization Management | Cannot save organization without legal name | High | High | 1. Navigate to Organizations<br>2. Click Create Organization<br>3. Leave Legal Name empty<br>4. Fill other required fields<br>5. Click Save | Validation error shown, cannot save | Organization saved with empty legal name. No validation error. | None shown | bug-001-screenshot.png | Chrome 120.0 | QA Tester | 2026-05-18 | New | | | | Found in first test run |

### Tips for Bug Tracker
1. **Unique Bug IDs**: Use sequential numbering (BUG-001, BUG-002, etc.)
2. **Link to Test Case**: Always reference related test case ID
3. **Detailed Steps**: Clear reproduction steps help AI fix faster
4. **Screenshot Naming**: Use bug ID in screenshot filename for easy matching
5. **Track AI Feedback**: Update status when sent to AI and when fix deployed

---

## Worksheet 4: Test Data

### Purpose
Repository of test data used during testing for reference and reuse.

### Section 1: Test Accounts

| Account Type | Phone Number | OTP | Role | Email | Password | Notes |
|--------------|--------------|-----|------|-------|----------|-------|
| Admin | +44-7700-900001 | 123456 | SYSTEM_ADMINISTRATOR | admin@fincore.com | - | Full access |
| Business User 1 | +44-7700-900002 | 123456 | BUSINESS_USER | business1@testcompany.com | - | Owns Tech Innovations Ltd |
| Business User 2 | +44-7700-900003 | 123456 | BUSINESS_USER | business2@anothercompany.com | - | Owns Global Finance Solutions |
| Compliance Officer | +44-7700-900004 | 123456 | COMPLIANCE_OFFICER | compliance@fincore.com | - | Compliance access |

### Section 2: Organization Test Data

| Scenario | Legal Name | Trading Name | Type | Reg Number | Contact Email | Contact Phone | Purpose |
|----------|------------|--------------|------|------------|---------------|---------------|---------|
| Happy Path | Tech Innovations Limited | TechInn | LIMITED_COMPANY | 12345678 | contact@techinnovations.co.uk | +44-7700-900100 | Full approval flow |
| Rejection Path | Global Finance Solutions PLC | GFS | PUBLIC_LIMITED_COMPANY | 87654321 | contact@gfs.co.uk | +44-7700-900200 | Rejection & resubmission |
| LLP Test | Premier Partners LLP | PPL | LIMITED_LIABILITY_PARTNERSHIP | 11223344 | info@premierpartners.co.uk | +44-7700-900300 | Test LLP type |

### Section 3: Document Files

| Document Type | File Name | Size | Format | Purpose |
|---------------|-----------|------|--------|---------|
| Certificate of Incorporation | certificate_incorporation.pdf | 2MB | PDF | Valid document upload |
| Proof of Address | proof_address_utility.pdf | 800KB | PDF | Valid document upload |
| Directors Register | directors_register.pdf | 500KB | PDF | Valid document upload |
| Passport | passport_valid.pdf | 2MB | PDF | Valid ID document |
| Passport Image | passport_valid.jpg | 1MB | JPG | Test image upload |
| Invalid Format | invalid_document.txt | 100KB | TXT | Negative test - wrong format |
| Oversized File | oversized_document.pdf | 15MB | PDF | Negative test - size limit |

### Section 4: Rejection Feedback Templates

| Document | Rejection Reason | Use Case |
|----------|------------------|----------|
| Certificate of Incorporation | "The document is blurry and unreadable. Please upload a clearer version showing company name and registration number." | Document quality issue |
| Proof of Address | "This document is more than 3 months old. Please provide a recent utility bill or bank statement dated within the last 90 days." | Document age issue |
| Directors Register | "The directors' list is incomplete. Please include full names, addresses, and dates of appointment for all directors." | Missing information |
| Passport | "The passport has expired. Please upload a valid, current passport." | Expired document |
| Bank Statement | "The document is partially cut off. Please upload a complete bank statement showing all corners and information." | Incomplete document |

---

## Worksheet 5: AI Feedback Log

### Purpose
Track all AI feedback cycles, what was sent, and what was fixed.

### Column Structure

| Col | Column Name | Width | Data Type | Description |
|-----|-------------|-------|-----------|-------------|
| A | **Feedback ID** | 12 | Text | FB-001, FB-002... |
| B | **Date Sent** | 15 | Date | When feedback sent to AI |
| C | **Module** | 20 | Text | Which module was tested |
| D | **Total Tests** | 10 | Number | Number of tests in module |
| E | **Failed Tests** | 10 | Number | Number of failures |
| F | **Bug IDs Included** | 25 | Text | List of bugs reported (e.g., BUG-001, BUG-002) |
| G | **Summary** | 50 | Text | Brief description of issues |
| H | **AI Response Date** | 15 | Date | When AI responded |
| I | **AI Actions Taken** | 50 | Text | What AI said it fixed |
| J | **Deployment Date** | 15 | Date | When fixes deployed to NPE |
| K | **Retest Date** | 15 | Date | When QA retested |
| L | **Retest Result** | 40 | Text | Pass/Fail count after retest |
| M | **Status** | 15 | Dropdown | List: Sent, Fixed, Verified, Partial Fix, No Fix |
| N | **Notes** | 40 | Text | Additional comments |

### Sample Data Row

| A | B | C | D | E | F | G | H | I | J | K | L | M | N |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| FB-001 | 2026-05-18 | Organization Management | 8 | 3 | BUG-001, BUG-002, BUG-003 | Validation issues, save failures, display bugs | 2026-05-18 | Fixed validation on save, corrected status display, added error handling | 2026-05-19 | 2026-05-19 | 3 Pass, 0 Fail | Verified | All issues resolved successfully |

### Tips for AI Feedback Log
1. **Sequential IDs**: Use FB-001, FB-002, etc.
2. **Track Everything**: Record all interactions with AI
3. **Link to Bugs**: Reference specific bug IDs
4. **Measure Effectiveness**: Track how many bugs were fixed per cycle
5. **Learn Patterns**: Identify common issues for future prevention

---

## How to Create This Excel File

### Option 1: Manual Creation in Excel
1. Open Microsoft Excel
2. Create new workbook
3. Create 5 worksheets (rename Sheet1-Sheet5)
4. Add columns as specified above
5. Apply conditional formatting
6. Add data validation dropdowns
7. Create formulas for Summary Dashboard

### Option 2: Use This Template Structure
1. Copy each section into Excel
2. Adjust column widths
3. Set up dropdowns (Data > Data Validation > List)
4. Apply conditional formatting (Home > Conditional Formatting > New Rule)
5. Test formulas to ensure they work

### Option 3: Request Pre-Built Template
- Contact development team for pre-built Excel template
- Or use provided CSV files and import into Excel

---

## Data Validation Setup

### Creating Dropdowns in Excel

#### For Status Column (TestExecution!K)
1. Select column K (all cells)
2. Data > Data Validation
3. Allow: List
4. Source: `NOT EXECUTED,PASS,FAIL,BLOCKED`
5. Click OK

#### For Module Column (TestExecution!B)
Source: `User Management,Organization Management,KYC Documents,Admin Approval,Rejection & Resubmission,Questionnaire,Customer Answers`

#### For Priority Column (TestExecution!D)
Source: `Critical,High,Medium,Low`

#### For Type Column (TestExecution!E)
Source: `Functional,Validation,Security,Negative,End-to-End`

#### For Severity Column (BugTracker!E)
Source: `Critical,High,Medium,Low`

#### For Bug Status Column (BugTracker!O)
Source: `New,Sent to AI,In Progress,Fixed,Verified,Won't Fix`

---

## Conditional Formatting Setup

### For Test Execution Status

1. Select column K (Status)
2. Home > Conditional Formatting > New Rule
3. Select "Format only cells that contain"
4. Rule 1: Cell Value = "PASS"
   - Format: Fill = Green (RGB: 198, 239, 206), Font = Dark Green, Bold
5. Repeat for FAIL (Red), BLOCKED (Orange), NOT EXECUTED (Gray)

### For Bug Severity

1. Select column E (Severity) in Bug Tracker
2. Apply same process as above
3. Critical = Red fill, white text, bold
4. High = Orange fill, black text, bold
5. Medium = Yellow fill, black text
6. Low = Light green fill, black text

---

## Daily Usage Workflow

### Morning Routine
1. Open Excel tracker
2. Review Summary Dashboard
3. Note which module to test today
4. Prepare test data from Worksheet 4

### During Testing
1. Go to Test Execution worksheet
2. Filter by module you're testing
3. For each test case:
   - Update Status (PASS/FAIL)
   - Enter Execution Date
   - Add Actual Result
   - If FAIL: Create bug in Bug Tracker worksheet
   - Add Screenshots column reference
   - Add any Comments

### End of Day
1. Count tests executed today
2. Review failed tests
3. If >= 3 failures in a module:
   - Go to Bug Tracker
   - Filter by today's date
   - Copy relevant bug details
   - Prepare AI feedback message (see main test plan Section 13)
4. Update Summary Dashboard (formulas auto-update)
5. Save file with date: `FinCore_TestTracker_2026-05-18.xlsx`

### After AI Fixes Deployed
1. Go to AI Feedback Log
2. Record deployment date
3. Filter Test Execution by failed tests
4. Update Retest Status and Retest Date
5. Update Bug Tracker status to "Verified" or note if still failing

---

## File Naming Convention

### Daily Files
- Format: `FinCore_TestTracker_[Environment]_[Date].xlsx`
- Example: `FinCore_TestTracker_NPE_2026-05-18.xlsx`

### Backup Files
- Save daily copy at end of day
- Keep in organized folder: `FinCore_TestResults/2026-05/`

### Screenshot Files
- Format: `[Bug-ID]_[Brief-Description].png`
- Example: `BUG-001_Empty-Legal-Name.png`
- Store in folder: `FinCore_Screenshots/2026-05-18/`

---

## Tips for Effective Tracking

### ✅ Best Practices
1. **Update in Real-Time**: Don't wait until end of day
2. **Be Detailed**: Clear steps help AI understand issues
3. **Use Screenshots**: Visual proof is invaluable
4. **Link Everything**: Connect tests → bugs → feedback cycles
5. **Save Often**: Don't lose your work
6. **Version Control**: Daily file copies with dates
7. **Consistent Naming**: Stick to naming conventions

### ❌ Common Mistakes to Avoid
1. Don't leave Status as "NOT EXECUTED" without reason
2. Don't skip Actual Result even if test passes
3. Don't forget to link bugs to test cases
4. Don't use vague bug descriptions
5. Don't delete rows - mark as "N/A" or "Blocked" if needed
6. Don't forget to retest after fixes

---

## Sample Excel Formulas

### For Summary Dashboard

#### Total Test Cases
```excel
=COUNTA(TestExecution!A2:A35)
```

#### Executed Tests
```excel
=COUNTIFS(TestExecution!K:K,"PASS")+COUNTIFS(TestExecution!K:K,"FAIL")+COUNTIFS(TestExecution!K:K,"BLOCKED")
```

#### Passed Tests
```excel
=COUNTIF(TestExecution!K:K,"PASS")
```

#### Failed Tests
```excel
=COUNTIF(TestExecution!K:K,"FAIL")
```

#### Pass Rate %
```excel
=IF(B3>0,B4/B3*100,0)
```
Where B3 = Executed, B4 = Passed

#### Execution Progress %
```excel
=IF(B2>0,B3/B2*100,0)
```
Where B2 = Total, B3 = Executed

#### Module-wise Executed Count
```excel
=COUNTIFS(TestExecution!$B:$B,"User Management",TestExecution!$K:$K,"<>NOT EXECUTED")
```

#### Module-wise Pass Count
```excel
=COUNTIFS(TestExecution!$B:$B,"User Management",TestExecution!$K:$K,"PASS")
```

#### Critical Bugs Count
```excel
=COUNTIF(BugTracker!E:E,"Critical")
```

---

## Exporting Data for AI Feedback

### Step-by-Step Export Process

1. **Go to Test Execution worksheet**
2. **Apply filter**: Status = "FAIL"
3. **Select columns**: A (Test ID), C (Test Name), G (Steps), H (Expected), M (Actual), O (Screenshots)
4. **Copy visible cells only**
5. **Paste into Word/Text document**
6. **Go to Bug Tracker worksheet**
7. **Filter**: Status = "New" or "Sent to AI"
8. **Copy relevant bug details**
9. **Format for AI as per Section 13.3 in main test plan**

### Example Export Format

```
FAILED TESTS SUMMARY
Date: 2026-05-18
Module: Organization Management

TC-ORG-001: Create LIMITED_COMPANY Organization
Expected: Organization saved with status PENDING
Actual: Error 500 shown, organization not created
Screenshot: BUG-001_Org-Create-Error.png

TC-ORG-004: Required Field Validation  
Expected: Validation error prevents save
Actual: Organization saved with empty legal name
Screenshot: BUG-002_No-Validation.png

TC-ORG-005: Search Organizations
Expected: Filtered results shown
Actual: Search box not responsive, no results
Screenshot: BUG-003_Search-Not-Working.png
```

---

## Questions or Issues?

If you have questions about using this tracker:
1. Refer to main Test Plan document (MANUAL_TEST_PLAN.md)
2. Contact development team
3. Document issues in Notes column for review

---

**End of Template Guide**

**Version**: 1.0  
**Created**: May 18, 2026  
**For**: FinCore Platform NPE Testing

