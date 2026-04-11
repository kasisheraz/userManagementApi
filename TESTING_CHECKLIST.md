# Quick Testing Checklist

## Pre-Test Setup
- [ ] Backend is running (port 8080)
- [ ] Frontend is running (port 3000)
- [ ] Database is accessible
- [ ] Test user account created
- [ ] Browser developer tools open

---

## 1. User Address Feature Tests

### Create User with Addresses
- [ ] Navigate to Users page
- [ ] Click "Create New User"
- [ ] Fill basic info:
  - [ ] First Name: "Test"
  - [ ] Last Name: "User"
  - [ ] Email: "test@example.com"
  - [ ] Phone: "1234567890"
  - [ ] Date of Birth: Select 18+ years ago
  - [ ] Role: "USER"
- [ ] Scroll to Residential Address section
- [ ] Fill residential address:
  - [ ] Address Line 1: "123 Main Street"
  - [ ] City: "London"
  - [ ] Postal Code: "SW1A 1AA"
  - [ ] Country: "United Kingdom"
- [ ] Check "Same as residential address" checkbox
- [ ] Verify postal address section disappears
- [ ] Uncheck "Same as residential address"
- [ ] Verify postal address section reappears
- [ ] Check the checkbox again (keep it checked)
- [ ] Click "Create User"
- [ ] Verify success message
- [ ] Verify user appears in list with full address shown

### Edit User Address
- [ ] Click on the created user
- [ ] Click "Edit"
- [ ] Verify residential address pre-populated
- [ ] Verify postal address pre-populated (same as residential)
- [ ] Uncheck "Same as residential address"
- [ ] Change postal address:
  - [ ] Address Line 1: "456 Oak Avenue"
  - [ ] City: "Manchester"
  - [ ] Postal Code: "M1 1AA"
- [ ] Click "Save"
- [ ] Verify success message
- [ ] Verify user details show different addresses

### View User with Addresses
- [ ] Click on user from list
- [ ] Verify residential address displays completely
- [ ] Verify postal address displays completely
- [ ] All fields visible and formatted correctly

### Create User Without Addresses
- [ ] Create new user
- [ ] Fill only basic info (no addresses)
- [ ] Click "Create User"
- [ ] Verify creation succeeds
- [ ] Verify user shows "No address" or similar

---

## 2. Organization Address Spacing Tests

### Visual Inspection
- [ ] Navigate to Organizations page
- [ ] Click "Create New Organization"
- [ ] Fill Basic Info tab (minimum required fields)
- [ ] Navigate to "Addresses" tab (Tab 6)
- [ ] Verify visual improvements:
  - [ ] Clear spacing between address sections
  - [ ] Dividers visible between sections
  - [ ] Section headers are bold and colored
  - [ ] Each address form has adequate padding
  - [ ] Fields are properly aligned
  - [ ] No overlapping elements
  - [ ] Responsive on different screen sizes

### Functional Test
- [ ] Fill Registered Address
- [ ] Fill Business Address
- [ ] Fill Correspondence Address
- [ ] Navigate back to Basic Info
- [ ] Navigate back to Addresses
- [ ] Verify addresses still filled
- [ ] Complete form and create organization
- [ ] Verify success

---

## 3. KYC Documents Tab Tests

### Tab Navigation
- [ ] Open organization creation form
- [ ] Count tabs (should be 9 total)
- [ ] Click "KYC Documents" tab (last tab)
- [ ] Verify tab opens without errors

### Content Verification
- [ ] Verify heading: "KYC & Compliance Documents"
- [ ] Verify "Optional" chip present
- [ ] Verify informational alert present
- [ ] Verify recommended documents list shows:
  - [ ] Certificate of Incorporation
  - [ ] Proof of Registered Address
  - [ ] Memorandum of Association
  - [ ] Articles of Association
  - [ ] Directors' Register
  - [ ] Shareholders' Register
  - [ ] Tax Registration Certificate
  - [ ] Regulatory License
- [ ] Verify placeholder text about future upload

### Organization Creation with KYC Intent
- [ ] Fill Basic Info
- [ ] Navigate to KYC Documents tab
- [ ] Read instructions
- [ ] Go back and complete form
- [ ] Submit organization
- [ ] Verify creation succeeds
- [ ] Note: Documents can be uploaded later (post-implementation)

---

## 4. API Tests (Optional - Developer Check)

### Test User API with cURL/Postman

**Create User with Addresses:**
```bash
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "firstName": "API",
  "lastName": "Test",
  "email": "api@test.com",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1995-05-15",
  "role": "USER",
  "residentialAddress": {
    "typeCode": 1,
    "addressLine1": "100 API Street",
    "city": "London",
    "postalCode": "SW1A 2AA",
    "country": "United Kingdom"
  },
  "postalAddress": {
    "typeCode": 5,
    "addressLine1": "100 API Street",
    "city": "London",
    "postalCode": "SW1A 2AA",
    "country": "United Kingdom"
  }
}
```

**Verify Response:**
- [ ] Status: 201 Created
- [ ] Response includes user ID
- [ ] Response includes full residential address with ID
- [ ] Response includes full postal address with ID

**Get User:**
```bash
GET http://localhost:8080/api/users/{userId}
```

- [ ] Status: 200 OK
- [ ] Addresses fully populated in response

---

## 5. Browser Console Check

### Frontend
- [ ] Open browser DevTools (F12)
- [ ] Navigate through all features
- [ ] Check Console tab
- [ ] Verify NO errors (red messages)
- [ ] Verify NO warnings related to our changes
- [ ] Check Network tab
- [ ] Verify API requests succeed (200/201 status)
- [ ] Verify response payloads match expected format

### Backend
- [ ] Check terminal/console running backend
- [ ] Verify NO stack traces
- [ ] Look for log messages:
  - "Creating new address"
  - "Created address with ID: X"
  - "Creating user with phone: ..."
  - "Creating residential address for user"
  - "User created with ID: X"

---

## 6. Validation Tests

### User Form Validation
- [ ] Try to create user without first name → Error
- [ ] Try to create user without last name → Error
- [ ] Try to create user without email → Error
- [ ] Try to create user with invalid email → Error
- [ ] Try to create user without phone → Error
- [ ] Try to create user with invalid phone → Error
- [ ] Try to create user under 18 years old → Error

### Address Validation (if address provided)
- [ ] Create user with address missing Address Line 1 → Should fail
- [ ] Create user with address missing Country → Should fail
- [ ] Create user with valid partial address (line1 + country only) → Should succeed

---

## 7. Edge Cases

### User Address Edge Cases
- [ ] Create user, then edit to add addresses (initially created without)
- [ ] Create user, then edit to remove addresses (set to empty)
- [ ] Create two users simultaneously with addresses
- [ ] Create user with very long address fields (test max length)
- [ ] Create user with special characters in address

### Organization Edge Cases
- [ ] Create organization with all 3 addresses
- [ ] Create organization with only registered address
- [ ] Create organization with no addresses
- [ ] Switch between tabs rapidly
- [ ] Fill form, refresh browser, verify data loss handled

---

## 8. Mobile/Responsive Tests

- [ ] Test on mobile viewport (375x667)
- [ ] Test on tablet viewport (768x1024)
- [ ] Test on desktop viewport (1920x1080)
- [ ] Verify address forms stack properly on mobile
- [ ] Verify checkbox and labels readable on small screens
- [ ] Verify organization form tabs scroll on mobile

---

## Pass/Fail Criteria

### PASS if:
- ✅ All user creation flows work with addresses
- ✅ "Same as residential" checkbox functions correctly
- ✅ Organization address spacing is improved and clear
- ✅ KYC Documents tab is visible and informative
- ✅ No console errors
- ✅ API responses include full address objects
- ✅ Address data persists correctly

### FAIL if:
- ❌ User creation with addresses fails
- ❌ Addresses not saved/retrieved
- ❌ Checkbox doesn't copy address
- ❌ Console shows JavaScript errors
- ❌ Layout is broken or overlapping
- ❌ API returns 500 errors
- ❌ Data loss occurs

---

## Test Results Template

```
Test Date: __________
Tester: __________
Environment: [ ] Local [ ] NPE [ ] QA

Feature 1: User Address
- [ ] PASS  [ ] FAIL  [ ] BLOCKED
Notes: _________________________________

Feature 2: Organization Address Spacing
- [ ] PASS  [ ] FAIL  [ ] BLOCKED
Notes: _________________________________

Feature 3: KYC Documents Tab
- [ ] PASS  [ ] FAIL  [ ] BLOCKED
Notes: _________________________________

Overall: [ ] APPROVE [ ] REJECT
```

---

## Bug Reporting Template

If issues found:
```
**Title:** [Brief description]

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Result:**


**Actual Result:**


**Screenshot:** [Attach if applicable]

**Console Errors:** [Copy/paste]

**Environment:**
- Browser: 
- OS: 
- Backend version: 
- Frontend version: 

**Severity:** [ ] Critical [ ] High [ ] Medium [ ] Low
```

---

**Quick Start Command:**
```bash
# Backend
cd Backend\ API
mvn spring-boot:run

# Frontend
cd Frontend\ UI
npm start
```

Good luck with testing! 🚀
