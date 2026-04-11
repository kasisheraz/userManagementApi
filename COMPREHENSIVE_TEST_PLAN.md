# Comprehensive Test Plan - User and Organization Creation

## Current Status

**Problem**: Backend compilation failing due to Lombok/Java version incompatibility (Lombok trying to access `com.sun.tools.javac.code.TypeTag::UNKNOWN` which doesn't exist in Java 17).

**This is NOT a code issue** - it's an environment/build tool configuration problem.

## Solution: Manual Testing Steps

### Step 1: Fix the Build Environment

**Option A: Use IntelliJ IDEA or Eclipse (Recommended)**
These IDEs handle Lombok properly. If you're using VS Code, try:

```powershell
# Clean and rebuild with Maven directly
cd c:\Development\git\userManagementApi
mvn clean
mvn compile -DskipTests -X
```

**Option B: Use Pre-compiled JAR** 
If Maven won't compile, try:
```powershell
# Use the last working JAR from target folder
cd c:\Development\git\userManagementApi
java -jar target\user-management-api-*.jar
```

**Option C: Update Lombok Version**
Check pom.xml and update Lombok to latest:
```xml
<lombok.version>1.18.34</lombok.version>
```

---

### Step 2: Verify Database has Required Roles

Once backend starts, check if roles exist:

**SQL Query to Run**:
```sql
-- Check what roles exist
SELECT Role_Identifier, Role_Name, Role_Description 
FROM Roles 
ORDER BY Role_Identifier;

-- Expected Results:
-- 1, SYSTEM_ADMINISTRATOR, Full system access
-- 2, ADMIN, Administrator with user management capabilities
-- 3, COMPLIANCE_OFFICER, Compliance and AML access
-- 4, OPERATIONAL_STAFF, Operational access
-- 5, USER, Standard user with basic access (from V6.0 migration)
-- 6, MANAGER, Manager with operational oversight (from V6.0 migration)
-- 7, SUPER_ADMIN, Super administrator with elevated privileges (from V6.0 migration)
```

**If roles 5, 6, 7 are missing**, the V6.0 migration didn't run. Manually run:

```sql
-- Add missing roles
INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('USER', 'Standard user with basic access');

INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('MANAGER', 'Manager with operational oversight');

INSERT IGNORE INTO Roles (Role_Name, Role_Description) 
VALUES ('SUPER_ADMIN', 'Super administrator with elevated privileges');

-- Verify
SELECT * FROM Roles;
```

---

### Step 3: Test User Creation

#### Test 3.1: Create User via UI

1. **Start Frontend**:
   ```powershell
   cd c:\Development\git\fincore_WebUI
   npm start
   ```

2. **Open Browser**: http://localhost:3000

3. **Navigate to Users**: Click "Users" in the sidebar

4. **Create New User**:
   - Click "+ New User"
   - Fill in:
     * First Name: `Test`
     * Last Name: `User`
     * Email: `test.user@example.com`
     * Phone: `+447700900001`
     * Date of Birth: `1990-01-01`
     * Role: `User` (should be available in dropdown)
   - **Don't fill addresses** (they're optional)
   - Click "Save"

5. **Expected Result**: ✅ User created successfully
6. **If Error**: Check browser console (F12) → Console tab AND backend terminal for error message

#### Test 3.2: Verify via API (Using Postman or curl)

**Create User Request**:
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+447700900002",
    "email": "test2@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1995-05-15",
    "role": "USER",
    "statusDescription": "ACTIVE"
  }'
```

**Expected Response** (200 OK):
```json
{
  "id": 4,
  "phoneNumber": "+447700900002",
  "email": "test2@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1995-05-15",
  "role": "USER",
  "statusDescription": "ACTIVE"
}
```

**Error Scenarios to Check**:

| Error Message | Cause | Solution |
|---------------|-------|----------|
| `Role not found: USER` | V6.0 migration didn't run | Run SQL from Step 2 |
| `Phone number already exists` | Duplicate phone | Use unique phone number |
| `Email already exists` | Duplicate email | Use unique email |

---

### Step 4: Test Organization Creation

#### Test 4.1: Minimal Organization (No Addresses)

1. **Navigate to Organizations**: http://localhost:3000/organizations

2. **Click "+ New Organization"**

3. **Fill ONLY Required Fields**:
   - Legal Name: `Test Organization 001`
   - Organization Type: `GOVERNMENT` (or any type)
   - **Owner ID**: Should auto-fill from logged-in user

4. **Leave ALL tabs empty** (Basic Info, Addresses, KYC, etc.)

5. **Click "Save"**

6. **Expected Result**: ✅ Organization created successfully

7. **If 500 Error**:
   - Open Browser DevTools (F12)
   - Go to Network tab
   - Click the failed POST request
   - Check Response tab for error message
   - **Copy and share the error message**

#### Test 4.2: Organization with Registered Address

1. **Create Another Organization**

2. **Fill Basic Info**:
   - Legal Name: `Test Organization 002`
   - Organization Type: `LTD`

3. **Go to Addresses Tab**

4. **Fill Registered Address**:
   - Address Line 1: `10 Test Street`
   - City: `London`
   - Postal Code: `SW1A 1AA`
   - Country: `United Kingdom`

5. **Click "Save"**

6. **Expected Result**: ✅ Organization with address created

---

### Step 5: Debug 500 Errors

If you get 500 errors, follow these steps:

#### 5.1: Check Backend Logs

Look for these error patterns in the backend terminal:

**Pattern 1: Missing Owner ID**
```
Owner user not found with ID: null
```
**Fix**: Ensure you're logged in. The form should set `ownerId` automatically.

**Pattern 2: Address Validation Failure**
```
cannot be null: addressLine1
cannot be null: country
cannot be null: typeCode
```
**Fix**: If providing addresses, ALL required fields must be filled.

**Pattern 3: Duplicate Data**
```
Organisation with registration number already exists
```
**Fix**: Use unique registration numbers for testing.

#### 5.2: Check Frontend Payload

In Browser DevTools → Network tab:
1. Find the failed POST request to `/organizations`
2. Click "Payload" or "Request" tab
3. Verify the JSON being sent

**Example of GOOD payload**:
```json
{
  "legalName": "Test Org",
  "organisationType": "GOVERNMENT",
  "ownerId": 1,  // MUST be present and > 0
  "registeredAddress": null,  // OK - addresses are optional
  "businessAddress": null,
  "correspondenceAddress": null
}
```

**Example of BAD payload** (will cause 500):
```json
{
  "legalName": "Test Org",
  "organisationType": "GOVERNMENT",
  "ownerId": 0,  // ❌ Invalid - must be > 0
  "registeredAddress": {
    "addressLine1": "123 Main St",
    "country": null  // ❌ Country is required if address provided
  }
}
```

---

### Step 6: Verification Queries

After successful creation, verify data persistence:

```sql
-- Check created users
SELECT 
    User_Identifier, 
    Phone_Number, 
    Email, 
    First_Name, 
    Last_Name,
    Role_Identifier,
    Status_Description
FROM Users
ORDER BY User_Identifier DESC
LIMIT 5;

-- Check user roles
SELECT 
    u.User_Identifier,
    u.First_Name,
    u.Last_Name,
    r.Role_Name
FROM Users u
LEFT JOIN Roles r ON u.Role_Identifier = r.Role_Identifier
ORDER BY u.User_Identifier DESC
LIMIT 5;

-- Check created organizations
SELECT 
    Organisation_Identifier,
    Legal_Name,
    Organisation_Type_Description,
    User_Identifier as Owner_ID,
    Status_Description
FROM Organisation
ORDER BY Organisation_Identifier DESC
LIMIT 5;

-- Check organization addresses
SELECT 
    o.Organisation_Identifier,
    o.Legal_Name,
    ra.Address_Line1 as Registered_Address,
    ba.Address_Line1 as Business_Address
FROM Organisation o
LEFT JOIN Address ra ON o.Registered_Address_Identifier = ra.Address_Identifier
LEFT JOIN Address ba ON o.Business_Address_Identifier = ba.Address_Identifier
ORDER BY o.Organisation_Identifier DESC
LIMIT 5;
```

---

## Common Issues & Solutions

### Issue 1: Backend Won't Start
**Error**: `Fatal error compiling: java.lang.ExceptionInInitializerError`

**Solutions**:
1. Update Lombok in pom.xml to v1.18.34
2. Use IntelliJ IDEA or Eclipse instead of VS Code
3. Try: `mvn clean install -DskipTests -U`
4. Check if a previous instance is running on port 8080

### Issue 2: Role Not Found
**Error**: `Role not found: USER`

**Solution**: Migration V6.0 didn't run. Manually insert roles (see Step 2)

### Issue 3: Owner ID Missing
**Error**: `Owner user not found with ID: null`

**Solution**: 
1. Ensure you're logged in to the UI
2. Check `OrganizationForm.tsx` line 64: `ownerId: user?.id || 0`
3. If user is null, login first

### Issue 4: Address Validation Errors
**Error**: `cannot be null: country`

**Solution**:
- Either fill ALL required address fields (addressLine1, country, typeCode)
- OR leave the entire address section empty
- Don't partially fill addresses

---

## Test Results Template

Please fill this out and share results:

```
### Test Results

**Environment**:
- Backend Running: [ ] Yes [ ] No
- Frontend Running: [ ] Yes [ ] No
- Database: [ ] MySQL [ ] H2 [ ] Other

**User Creation Test**:
- Via UI: [ ] ✅ Success [ ] ❌ Failed - Error: _____________
- Via API: [ ] ✅ Success [ ] ❌ Failed - Error: _____________

**Organization Creation Test**:
- Minimal (no addresses): [ ] ✅ Success [ ] ❌ Failed - Error: _____________
- With Addresses: [ ] ✅ Success [ ] ❌ Failed - Error: _____________

**Database Verification**:
- Roles 1-4 exist: [ ] Yes [ ] No
- Roles 5-7 exist (USER, MANAGER, SUPER_ADMIN): [ ] Yes [ ] No
- Created users visible: [ ] Yes [ ] No
- Created orgs visible: [ ] Yes [ ] No

**Error Messages** (if any):
Backend Terminal:
```
[paste error here]
```

Browser Console:
```
[paste error here]
```

Browser Network Response:
```
[paste error here]
```
```

---

## Next Steps

1. Try to start the backend using one of the methods in Step 1
2. If backend starts, follow Steps 2-6 to test CRUD operations
3. Share the test results template above with actual error messages
4. I can then provide targeted fixes based on the specific errors you encounter
