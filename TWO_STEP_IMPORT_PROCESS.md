# Two-Step Schema Import Process
**CRITICAL:** The existing Phase 2 tables have the OLD, incorrect schema. You must DROP them first!

## Step 1: Drop Old Tables

1. Go to GCP Console Cloud SQL: https://console.cloud.google.com/sql/instances/fincore-npe-db/import
2. Click **IMPORT**
3. Select `drop-phase2-tables.sql`
4. Database: `fincore_db`
5. Click **IMPORT**
6. Wait for completion (~30 seconds)

**This will DELETE:**
- customer_answers
- aml_screening_results
- questionnaire_questions
- customer_kyc_verification

⚠️ **Note:** Any test data in these tables will be lost. That's OK for now.

## Step 2: Import Corrected Schema

1. Go back to import page (same URL)
2. Click **IMPORT**
3. Select `complete-entity-schema.sql`
4. Database: `fincore_db`
5. Click **IMPORT**
6. Wait for completion (~1-2 minutes)

**This will CREATE:**
- All 4 Phase 2 tables with CORRECT schema
- All indexes and foreign keys
- Seed data (permissions, roles, etc.)

## Step 3: Verify Import

Run this to confirm tables were recreated:
```powershell
.\quick-check-import.ps1
```

## Step 4: Redeploy Application

Trigger GitHub Actions redeploy or:
```powershell
.\trigger-redeploy.ps1
```

## Why This Is Necessary

MySQL's `CREATE TABLE` statement has an **implicit `IF NOT EXISTS`** behavior when the table already exists. The import skips the CREATE TABLE and doesn't modify the existing structure.

The database still has:
- ❌ `screening_type VARCHAR(100)` (should be 20)
- ❌ `question_category VARCHAR(100)` (should be 50)
- ❌ `answer_text TEXT` (should be answer VARCHAR(500))
- ❌ `question_id BIGINT` (should be INT)

After dropping and recreating, it will have:
- ✅ `screening_type VARCHAR(20)`
- ✅ `question_category VARCHAR(50)`
- ✅ `answer VARCHAR(500)`
- ✅ `question_id INT`

## Timeline

- Drop tables: 30 seconds
- Import schema: 1-2 minutes
- Redeploy: 5-7 minutes
- **Total: ~10 minutes to working application**

## Next Expected Result

✅ Deployment succeeds  
✅ Health endpoint responds  
✅ All Phase 2 APIs functional
