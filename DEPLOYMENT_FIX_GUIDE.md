# Cloud Run Deployment Issue - RESOLVED

## Root Cause

The application fails to start because **Phase 2 database tables are missing** from the Cloud SQL database.

### Error from Logs:
```
org.hibernate.tool.schema.spi.SchemaManagementException: 
Schema-validation: missing table [aml_screening_results]
```

### Why This Happens:
1. The application uses `spring.jpa.hibernate.ddl-auto: validate` in NPE profile
2. This setting expects all tables to exist but does NOT create them
3. Phase 2 added 4 new tables that were never deployed to Cloud SQL:
   - `aml_screening_results`
   - `customer_kyc_verification`
   - `questionnaire_questions`
   - `customer_answers`

## Solution

Deploy the Phase 2 schema to Cloud SQL database before deploying the application.

### Option 1: Quick Deploy (Batch Script) - EASIEST

```cmd
deploy-phase2-schema.bat
```

This will:
- Connect directly to Cloud SQL public IP (34.89.96.239)
- Deploy complete-entity-schema.sql
- Create all Phase 2 tables

**Requirements:**
- MySQL client installed
- Database password
- Your IP must be in Cloud SQL authorized networks

### Option 2: PowerShell Script with Options

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\deploy-phase2-schema.ps1
```

Choose between:
1. Cloud SQL Proxy (more secure)
2. gcloud sql import (via Cloud Storage)

### Option 3: Manual via gcloud

```bash
# 1. Upload schema to Cloud Storage
gsutil cp complete-entity-schema.sql gs://fincore-npe-db-imports/phase2-schema.sql

# 2. Import to Cloud SQL
gcloud sql import sql fincore-npe-db:europe-west2:fincore-npe-db \
  gs://fincore-npe-db-imports/phase2-schema.sql \
  --database=fincore_db \
  --project=project-07a61357-b791-4255-a9e
```

### Option 4: Manual via MySQL Client

```bash
mysql -h 34.89.96.239 -u fincore_app -p fincore_db < complete-entity-schema.sql
```

## After Schema Deployment

Once the schema is deployed, the application will start successfully. You can:

1. **Wait for GitHub Actions** (if you've pushed commits)
   - It will automatically redeploy
   - Check: https://github.com/kasisheraz/userManagementApi/actions

2. **Manual Redeploy**
   ```powershell
   .\deploy-cloudrun-npe.ps1
   ```

## Verification

After deployment, verify the tables exist:

```sql
USE fincore_db;
SHOW TABLES;

-- Should see:
-- aml_screening_results
-- customer_kyc_verification  
-- questionnaire_questions
-- customer_answers
```

Or run the check script:
```powershell
.\check-phase2-tables.ps1
```

## Why This Wasn't Caught Earlier

- Local development uses H2 or local MySQL with `ddl-auto: update` (creates tables automatically)
- NPE environment uses `ddl-auto: validate` (expects tables to exist)
- Phase 2 tables were created locally but never deployed to Cloud SQL

## Prevention

For future schema changes:
1. Always update `complete-entity-schema.sql` when adding new entities
2. Deploy schema changes to Cloud SQL BEFORE deploying application code
3. Consider adding schema migration scripts (Flyway/Liquibase) for automated deployments

## Quick Reference

**Cloud SQL Details:**
- Instance: `project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db`
- Public IP: `34.89.96.239`
- Database: `fincore_db`
- User: `fincore_app`

**Phase 2 Tables:**
1. `aml_screening_results` - AML screening results for KYC
2. `customer_kyc_verification` - Customer KYC verification records
3. `questionnaire_questions` - KYC questionnaire questions
4. `customer_answers` - Customer answers to questionnaire

**Application Configuration:**
- Profile: `npe` (application-npe.yml)
- Schema validation: ENABLED
- Auto-create tables: DISABLED
