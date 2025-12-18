# Manual Cloud SQL Permission Fix Instructions

## Issue: 
The Cloud SQL built-in integration fails with:
```
Access denied for user 'fincore_app'@'cloudsqlproxy~149.50.15.88' (using password: YES)
```

## Root Cause:
The `fincore_app` user exists but lacks proper database permissions for connections through the built-in Cloud SQL connector (cloudsqlproxy~%).

## Solution:
Execute the following SQL commands in Cloud SQL console or via Cloud Shell:

### Step 1: Connect to Cloud SQL
```bash
gcloud sql connect fincore-npe-db --user=root --project=project-07a61357-b791-4255-a9e
```

### Step 2: Execute Permission Fix SQL
```sql
-- Grant all privileges on my_auth_db to fincore_app for all connection types
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'%';

-- Grant specific privileges for cloudsqlproxy connections (critical for built-in connector)
GRANT ALL PRIVILEGES ON my_auth_db.* TO 'fincore_app'@'cloudsqlproxy~%';

-- Grant basic connection privileges
GRANT USAGE ON *.* TO 'fincore_app'@'%';
GRANT USAGE ON *.* TO 'fincore_app'@'cloudsqlproxy~%';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify permissions (optional)
SHOW GRANTS FOR 'fincore_app'@'%';
```

### Step 3: Test Built-in Integration
After applying the permissions, test the built-in integration by:

1. Run locally: `.\test-builtin-integration.ps1`
2. Or deploy to GitHub Actions with the updated workflow

## Files Modified:
- ✅ `application-gcp-builtin.yml` - Built-in Cloud SQL connector configuration
- ✅ `.github/workflows/deploy-npe.yml` - Updated to use gcp-builtin profile
- ✅ Added CLOUD_SQL_INSTANCE environment variable to deployment

## Expected Result:
After permissions fix, the application should start with:
```
✅ UserMgmtPool - Start completed
✅ Database connection is healthy
✅ Application ready to accept requests
```