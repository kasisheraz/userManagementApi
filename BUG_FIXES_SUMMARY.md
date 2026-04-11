# Bug Fixes - User and Organization Creation Issues

## Issues Fixed

### 1. ✅ User Creation - "Cannot find user role" Error

**Problem**: When creating a user, the system returned an error "Role not found: USER"

**Root Cause**: The database only had these roles seeded:
- SYSTEM_ADMINISTRATOR
- ADMIN
- COMPLIANCE_OFFICER  
- OPERATIONAL_STAFF

But the frontend was trying to create users with these roles:
- USER (default)
- MANAGER

**Fix Applied**:
- Created new Flyway migration `V6.0__Add_Missing_User_Roles.sql`
- Added missing roles:
  - **USER**: Standard user with read-only access (USER_READ, CUSTOMER_READ)
  - **MANAGER**: Manager with operational oversight (USER_READ, CUSTOMER_READ, CUSTOMER_WRITE, ORG_READ)
  - **SUPER_ADMIN**: Super administrator with all permissions

**Files Modified**:
- `src/main/resources/db/migration/V6.0__Add_Missing_User_Roles.sql` (new file)

**Commit**: `6940ac2` - "fix: Add missing user roles required by frontend"

---

### 2. ⚠️ Organization Creation - 500 Error

**Problem**: When creating an organization, the system returns a 500 Internal Server Error

**Possible Causes**:

1. **Missing Owner ID**: The `ownerId` is required but might be null
2. **Invalid Address Data**: Address DTOs require `typeCode`, `addressLine1`, and `country`
3. **Database Constraint Violations**: Duplicate registration/company numbers
4. **KYC Document Issues**: Problems with KYC document creation

**Debugging Steps**:

#### Step 1: Check Backend Logs
When the 500 error occurs, check the backend logs for the specific error:

```bash
# Windows PowerShell
cd c:\Development\git\userManagementApi
mvn spring-boot:run

# Look for ERROR messages in the console
```

#### Step 2: Verify Owner ID
The organization form should auto-fill `ownerId` from the logged-in user. Check browser DevTools Network tab:

1. Open Browser DevTools (F12)
2. Go to Network tab
3. Try to create organization
4. Click on the failed POST request to `/organizations`
5. Check the Request Payload - verify `ownerId` is present and non-zero

Example payload should look like:
```json
{
  "legalName": "Test Org",
  "organisationType": "GOVERNMENT",
  "ownerId": 1,  // <-- Must be present!
  "registeredAddress": {
    "typeCode": 1,        // <-- Required
    "addressLine1": "123 Main St",  // <-- Required
    "country": "United Kingdom"     // <-- Required
  }
}
```

#### Step 3: Check Address Data
Verify addresses include required fields:
- `typeCode` (1=Registered, 2=Business, 3=Correspondence)
- `addressLine1`
- `country`

#### Step 4: Test with Minimal Data
Try creating an organization with ONLY required fields:
1. Legal Name
2. Organization Type
3. NO addresses (all optional)
4. NO KYC documents

If this works, the issue is with optional field handling.

#### Step 5: Check for Duplicates
The system checks for:
- Duplicate Registration Number
- Duplicate Company Number

If you're testing, use unique values each time.

---

## Testing Instructions

### Test User Creation (After Fix)

1. **Start the backend** (migration will run automatically):
   ```powershell
   cd c:\Development\git\userManagementApi
   mvn spring-boot:run
   ```

2. **Start the frontend**:
   ```powershell
   cd c:\Development\git\fincore_WebUI
   npm start
   ```

3. **Create a new user**:
   - Navigate to http://localhost:3000/users
   - Click "+ New User"
   - Fill in required fields:
     * First Name
     * Last Name
     * Email (unique)
     * Phone Number (unique)
     * Date of Birth
     * **Role**: Should default to "User" (now available!)
   - Click Save
   - ✅ Should succeed without "Role not found" error

### Test Organization Creation

1. **Open Browser DevTools** (F12) before testing

2. **Navigate to Organizations**:
   - Go to http://localhost:3000/organizations
   - Click "+ New Organization"

3. **Fill ONLY required fields** (minimal test):
   - Legal Name: "Test Organization 001"
   - Organization Type: Select any type
   - Leave all addresses EMPTY
   - Click Save

4. **If 500 error occurs**:
   - Check Console tab for JavaScript errors
   - Check Network tab → Failed request → Response tab for error message
   - Check backend terminal for stack trace
   - Copy error message and share for further debugging

5. **If minimal test works, add addresses**:
   - Edit the organization
   - Go to Addresses tab
   - Fill in Registered Address:
     * Address Line 1: "123 Test Street"
     * City: "London"
     * Country: "United Kingdom"
   - Click Save
   - If this fails, the issue is with address handling

---

## Known Issues & Workarounds

### Issue: "Owner ID is required" validation error
**Workaround**: Make sure you're logged in. The form reads ownerId from the authenticated user.

### Issue: Address validation failing
**Workaround**: Either fill ALL required address fields OR leave the address completely empty. Don't partially fill addresses.

---

## Next Steps

1. ✅ Pull latest backend code (migration V6.0 included)
2. ✅ Restart backend to run migration
3. ✅ Test user creation - should work now
4. ⚠️ Test organization creation with debugging steps above
5. 📤 Share backend error logs if 500 error persists

---

## Files Changed

### Backend (userManagementApi)
- `src/main/resources/db/migration/V6.0__Add_Missing_User_Roles.sql` (NEW)

### Frontend (fincore_WebUI)
- No changes required - frontend code is correct

---

## Deployment Status

- ✅ Backend: Committed and pushed (commit `6940ac2`)
- ✅ Frontend: Already deployed (commit `22f01a2`)
- ⏳ Database: Migration V6.0 will run automatically on next backend startup

---

## Additional Notes

The USER and MANAGER roles are now properly seeded with appropriate permissions:

- **USER Role**: 
  - Can read user information
  - Can read customer information
  - Cannot create/modify data

- **MANAGER Role**:
  - Can read users and customers
  - Can create/modify customers
  - Can read organizations
  - Limited write access for operational tasks

- **SUPER_ADMIN Role**:
  - All permissions
  - Full system access
