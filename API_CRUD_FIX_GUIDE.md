# API CRUD Testing - Quick Fix Guide

## Current Status
- ✓ Backend deployed: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
- ✓ API is healthy (UP status)
- ✗ Login failing (500 Internal Server Error)
- ✗ User creation not working

## Root Cause Analysis
The login failure suggests either:
1. **Database not initialized** - Missing default admin user
2. **Wrong roles** - Still using old role names instead of new ones
3. **No password** - Admin user exists but has no password

## Step-by-Step Fix

### STEP 1: Diagnose the Database
Run the diagnostic script to see what's in your database:

```bash
# Connect to your Cloud SQL instance
mysql -h YOUR_DB_HOST -u YOUR_DB_USER -p YOUR_DB_NAME < diagnose-database.sql
```

Or using MySQL Workbench/HeidiSQL:
1. Open `diagnose-database.sql`
2. Execute it
3. Review the output

### STEP 2: Check Output
Look for these sections:

#### Expected vs Actual:

**ROLES CHECK** - Should show:
```
Role_Name         | Description
------------------+------------------------------------------
Admin             | Administrator with full system access
Compliance        | Compliance officer with audit and review
Operational       | Operational staff with standard access
Business User     | Business user with access to own data only
```

**ADMIN USER CHECK** - Should show:
```
Phone_Number    | Email              | Role_Name | Password_Status
----------------+--------------------+-----------+------------------
+1234567890     | admin@fincore.com  | Admin     | Password EXISTS
```

### STEP 3: Fix Missing Roles
If roles are missing or wrong, run:

```bash
mysql -h YOUR_DB_HOST -u YOUR_DB_USER -p YOUR_DB_NAME < ADD_BUSINESS_ROLES.sql
```

This will:
- Add all 4 new roles (Admin, Compliance, Operational, Business User)
- Map permissions to each role
- Show verification output

### STEP 4: Fix Missing Admin User
If admin user is missing, run this SQL:

```sql
-- Check which roles exist
SELECT Role_Identifier, Role_Name FROM Roles WHERE Role_Name = 'Admin';

-- If Role_Identifier is 1, run:
INSERT INTO Users (
    Phone_Number, Email, Role_Identifier, 
    First_Name, Last_Name, Date_Of_Birth, 
    Status_Description, Password
) VALUES (
    '+1234567890', 
    'admin@fincore.com', 
    1,  -- Adjust this to match the Admin Role_Identifier  
    'System', 
    'Administrator', 
    '1990-01-01', 
    'Active',
    '$2a$10$N.CjAz6DEp2Q9xp.sJd3duLr/aJGJnN8sAz8N4koNzZZw0s.T8F5G'  -- password123
);
```

### STEP 5: Wait for Deployment
After pushing database fixes, GitHub Actions will rebuild and redeploy:
1. Go to: https://github.com/kasisheraz/userManagementApi/actions
2. Wait for "Build & Deploy to NPE" to complete (~5 minutes)
3. Look for green checkmark

### STEP 6: Test the API
After deployment completes, run the test script:

```powershell
cd C:\Development\git\userManagementApi
.\test-api-crud.ps1 -BaseUrl "https://fincore-npe-api-lfd6ooarra-nw.a.run.app"
```

### STEP 7: Expected Test Output
You should see:
```
[STEP 1] Authenticating as Admin...
SUCCESS: Authenticated as System Administrator
  Role: Admin

[STEP 2] Fetching available roles...
SUCCESS: Found 4 roles
  - Admin: Administrator with full system access
  - Compliance: Compliance officer with audit and review capabilities
  - Operational: Operational staff with standard operational access
  - Business User: Business user with access to own data only

[STEP 3] Creating a Business User...
SUCCESS: Created Business User
  Role: Business User
  Status: Active

[STEP 7] Creating an Organization...
SUCCESS: Created Organization
  Legal Name: Test Financial Services Ltd
  Status: Active
```

## Common Issues & Solutions

### Issue: "Role not found: USER"
**Solution:** Frontend has cached JavaScript. Hard refresh browser (Ctrl+Shift+R)

### Issue: Login returns 500 error
**Solutions:**
1. Check admin user exists with password
2. Check database connection in Cloud Run environment variables
3. View logs: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs

### Issue: "Cannot create user - Role not found"
**Solution:** Run `ADD_BUSINESS_ROLES.sql` to add the 4 new roles

### Issue: Organization creation fails
**Solution:** 
1. User must exist first (check User_Identifier)
2. Addresses are required (registeredAddress, businessAddress)

## Manual Testing via Postman/curl

### 1. Login
```bash
curl -X POST https://fincore-npe-api-lfd6ooarra-nw.a.run.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890","password":"password123"}'
```

### 2. Create User (with token from login)
```bash
curl -X POST https://fincore-npe-api-lfd6ooarra-nw.a.run.app/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@test.com",
    "phoneNumber": "+447700900123",
    "dateOfBirth": "1990-01-01",
    "role": "Business User",
    "residentialAddress": {
      "typeCode": 1,
      "addressLine1": "123 Main St",
      "city": "London",
      "stateCode": "London",
      "postalCode": "SW1A 1AA",
      "country": "UK"
    }
  }'
```

### 3. Create Organization
```bash
curl -X POST https://fincore-npe-api-lfd6ooarra-nw.a.run.app/api/organisations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userIdentifier": 1,
    "legalName": "Test Ltd",
    "businessName": "Test Business",
    "organisationTypeDescription": "LTD",
    "registrationNumber": "REG12345",
    "sicCode": "64999",
    "businessDescription": "Testing",
    "incorporationDate": "2020-01-01",
    "countryOfIncorporation": "UK",
    "websiteAddress": "https://test.com",
    "registeredAddress": {
      "typeCode": 3,
      "addressLine1": "10 Business St",
      "city": "London",
      "stateCode": "London",
      "postalCode": "EC1A 1AA",
      "country": "UK"
    }
  }'
```

## Next Steps After Tests Pass
1. Test role-based filtering (Business User sees only own data)
2. Test all 4 roles can create users/organizations
3. Refresh frontend UI (Ctrl+Shift+R) to see new role dropdown
4. Create test users for each role
5. Verify security filtering works correctly
