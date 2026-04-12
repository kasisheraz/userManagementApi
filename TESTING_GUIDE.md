# API Testing Guide - Quick Start

## Prerequisites
Your backend code has been pushed and GitHub Actions is building/deploying it automatically.

## Step 1: Check Deployment Status
1. Go to: https://github.com/kasisheraz/userManagementApi/actions
2. Look for the latest "Build & Deploy to NPE" workflow
3. Wait for it to complete (green checkmark)

## Step 2: Get Your API URL
After deployment completes, your API will be at:
```
https://fincore-npe-api-[hash]-ew.a.run.app
```

You can find the exact URL by running:
```powershell
gcloud run services describe fincore-npe-api --region=europe-west2 --format='value(status.url)'
```

## Step 3: Run Comprehensive Tests
Once you have the URL, run the test script:

```powershell
cd C:\Development\git\userManagementApi
.\test-api-crud.ps1 -BaseUrl "YOUR_CLOUD_RUN_URL"
```

Example:
```powershell
.\test-api-crud.ps1 -BaseUrl "https://fincore-npe-api-abc123-ew.a.run.app"
```

## Step 4: What the Test Does
The script will test:
- ✓ Admin authentication
- ✓ Fetch all roles (should show: Admin, Compliance, Operational, Business User)
- ✓ Create Business User
- ✓ Create Operational User  
- ✓ Get all users
- ✓ Update user
- ✓ Create organization
- ✓ Get all organizations
- ✓ Update organization

## Expected Results
All steps should show SUCCESS in green.

If you see errors:
1. **"Role not found"** - Database not migrated. Run V7.0 migration.
2. **"401 Unauthorized"** - Check admin credentials
3. **"500 Internal Server Error"** - Check Cloud Run logs

## Quick Fix: Run DB Migration
If roles are missing, connect to your Cloud SQL and run:
```sql
-- Check current roles
SELECT * FROM Roles;

-- If empty or wrong, run:
INSERT IGNORE INTO Roles (Role_Name, Role_Description) VALUES 
('Admin', 'Administrator with full system access'),
('Compliance', 'Compliance officer with audit and review capabilities'),
('Operational', 'Operational staff with standard operational access'),
('Business User', 'Business user with access to own data only');
```

## Testing Role-Based Filtering
The script creates a Business User. To test if they can only see their own data:
1. Note the Business User's phone number from the test output
2. Set a password for them (using admin)
3. Login as that Business User
4. Check they only see their own user record and organizations

## Check Cloud Run Logs
If anything fails:
```powershell
gcloud logs read --service=fincore-npe-api --limit=50
```

Or use: https://console.cloud.google.com/run/detail/europe-west2/fincore-npe-api/logs
