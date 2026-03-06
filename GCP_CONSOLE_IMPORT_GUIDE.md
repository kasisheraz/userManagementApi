# Manual Import to Cloud SQL via GCP Console

## Step-by-Step Instructions

### 1. Navigate to Cloud SQL
1. Go to: https://console.cloud.google.com/sql/instances
2. Select project: `project-07a61357-b791-4255-a9e`
3. Click on instance: `fincore-npe-db`

### 2. Import the SQL File

#### Option A: Import from Local File
1. Click **"IMPORT"** button at the top
2. Choose **"Browse"** to select file
3. Select: `complete-entity-schema.sql` from your local machine
4. Database: `fincore_db`
5. Click **"IMPORT"**

#### Option B: Import from Cloud Storage (Recommended for large files)
1. First, upload the file to Cloud Storage:
   ```bash
   gsutil cp complete-entity-schema.sql gs://fincore-npe-db-imports/complete-schema.sql
   ```
   
2. In Cloud SQL Console:
   - Click **"IMPORT"** button
   - Choose **"Browse"** and select `gs://fincore-npe-db-imports/complete-schema.sql`
   - Database: `fincore_db`
   - Click **"IMPORT"**

### 3. Wait for Import to Complete
- Import typically takes 1-2 minutes
- You'll see a notification when complete
- Status will show as "Operation completed"

### 4. Verify Import
1. Go to Cloud SQL instance page
2. Click **"DATABASES"** tab
3. Click on `fincore_db`
4. You should see the Phase 2 tables:
   - `aml_screening_results`
   - `customer_kyc_verification`
   - `questionnaire_questions`
   - `customer_answers`

### 5. Redeploy Application
After successful import:

**GitHub Actions will automatically redeploy** (from your recent push)

OR manually trigger:
```bash
# From GitHub UI
Go to Actions → Build & Deploy to NPE → Re-run jobs

# OR from local
.\deploy-cloudrun-npe.ps1
```

## Troubleshooting

### If Import Fails:
1. **Check file encoding**: Must be UTF-8
2. **Check database name**: Must select `fincore_db` in import dialog
3. **Check for errors**: View operation details for specific SQL errors

### Common Issues:
- **"Table already exists"**: This is OK, the script has `DROP TABLE IF EXISTS`
- **"Unknown database"**: Make sure `fincore_db` is selected in the import dialog
- **"Access denied"**: Check service account permissions

### Verify Tables After Import:
Using Cloud SQL Studio (in console):
```sql
USE fincore_db;
SHOW TABLES;

-- Check Phase 2 tables exist:
SELECT COUNT(*) FROM aml_screening_results;
SELECT COUNT(*) FROM customer_kyc_verification;
SELECT COUNT(*) FROM questionnaire_questions;
SELECT COUNT(*) FROM customer_answers;
```

## What Happens Next

1. ✅ Schema imported to Cloud SQL
2. ✅ GitHub Actions redeploys application (automatic)
3. ✅ Application starts successfully
4. ✅ Health check passes: `https://fincore-npe-api-xxx.run.app/actuator/health`
5. ✅ API endpoints ready to use

## Important Notes

- The script is **idempotent** - safe to run multiple times
- It uses `DROP TABLE IF EXISTS` - will recreate all tables
- **Data will be lost** if tables already exist with data
- All existing users/roles will be removed (default data will be inserted)

## Direct Console Links

- Cloud SQL Instances: https://console.cloud.google.com/sql/instances?project=project-07a61357-b791-4255-a9e
- Import Page: https://console.cloud.google.com/sql/instances/fincore-npe-db/import?project=project-07a61357-b791-4255-a9e
- Cloud Storage Bucket: https://console.cloud.google.com/storage/browser/fincore-npe-db-imports?project=project-07a61357-b791-4255-a9e
