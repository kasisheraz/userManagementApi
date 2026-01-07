# Quick Fix: How to Run SQL Queries in Cloud SQL

## Method 1: Use Cloud SQL Studio (Easiest)

1. Go to: https://console.cloud.google.com/sql/instances/fincore-npe-db/cloudsql-studio?project=project-07a61357-b791-4255-a9e

OR

1. On the left sidebar, look for "SQL" or expand the hamburger menu (≡)
2. Find "SQL Workspace" or "Cloud SQL Studio"
3. Click it
4. Select your instance: fincore-npe-db
5. Select database: fincore_db
6. You'll see a SQL editor panel

## Method 2: Use Cloud Shell (Alternative)

1. Click the **Cloud Shell icon** (>_) in the top right corner of Google Cloud Console
2. Wait for Cloud Shell to load
3. Run this command:
```bash
gcloud sql connect fincore-npe-db --user=root --database=fincore_db --project=project-07a61357-b791-4255-a9e
```
4. Press Y when asked to continue
5. When prompted for password, paste the password from Secret Manager
6. Once connected, you'll see `mysql>` prompt
7. Paste the diagnostic query

## The Diagnostic Query to Run:

```sql
USE fincore_db;
SHOW VARIABLES LIKE 'lower_case_table_names';
SHOW TABLES;
SELECT COUNT(*) FROM permissions;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM users;
```

## If All Else Fails:

Just tell me: Can you access Cloud Shell (the >_ button at top right)?
