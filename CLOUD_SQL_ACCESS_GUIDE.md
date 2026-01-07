# STEP-BY-STEP GUIDE: Access Cloud SQL Database
# =====================================================

## Step 1: Get Database Password

1. The browser should have opened Secret Manager automatically
2. Or manually go to: https://console.cloud.google.com/security/secret-manager/secret/fincore-npe-app-password/versions?project=project-07a61357-b791-4255-a9e
3. Click on the version (latest)
4. Click "VIEW SECRET VALUE" button
5. Copy the password (you'll need it in Step 3)

---

## Step 2: Open Cloud SQL Console

1. Go to: https://console.cloud.google.com/sql/instances/fincore-npe-db?project=project-07a61357-b791-4255-a9e
2. Click on the instance name "fincore-npe-db"

---

## Step 3: Connect to Database

### Option A: Using Cloud Shell (Recommended - No password needed!)

1. In Cloud SQL Console, click the "CONNECT USING CLOUD SHELL" button (top right)
2. Cloud Shell will open at the bottom
3. A command will be pre-filled, press ENTER
4. When prompted "Do you want to continue (Y/n)?", type: Y
5. It will connect automatically (no password needed with Cloud Shell!)
6. You'll see: `mysql>`

### Option B: Using SQL Editor in Console

1. Click "DATABASES" tab on the left
2. Click on "fincore_db" database
3. Click "RUN A QUERY" button
4. A SQL editor will open - no connection needed!
5. Paste the diagnostic query (see Step 4)

---

## Step 4: Run Diagnostic Query

Once connected (or in SQL editor), paste this:

```sql
USE fincore_db;

-- Check MySQL case sensitivity setting
SHOW VARIABLES LIKE 'lower_case_table_names';

-- Show all tables
SHOW TABLES;

-- Try to count records
SELECT 'Checking permissions...' AS status;
SELECT COUNT(*) AS permission_count FROM permissions;

SELECT 'Checking roles...' AS status;
SELECT COUNT(*) AS role_count FROM roles;

SELECT 'Checking users...' AS status;
SELECT COUNT(*) AS user_count FROM users;
```

Press ENTER or click "RUN" button.

---

## Step 5: Send Me The Results

Copy and paste the output here so I can see:
- What `lower_case_table_names` value is
- What tables exist
- How many records are in each table
- Any error messages

---

## Quick Troubleshooting

**If you get "Access Denied" error:**
- User: fincore_app
- Password: (from Secret Manager in Step 1)
- Database: fincore_db

**If Cloud Shell connection takes too long:**
- Use Option B (SQL Editor) instead - it's faster and doesn't require password

**If you see "Table doesn't exist" errors:**
- That's exactly what we're diagnosing - tell me which tables are missing
