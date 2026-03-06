# URGENT: Fix Database Schema Using ALTER TABLE

## Problem
The database tables still have the OLD schema. The DROP + IMPORT approach didn't work because:
- Either the DROP script wasn't executed properly
- Or the tables were recreated from cached SQL with old structure
- Result: Same schema validation error persists

## Solution: Use ALTER TABLE to Modify Existing Tables

Instead of dropping and recreating, we'll ALTER the existing tables to fix the schema in place.

## Step-by-Step Instructions

### 1. Open GCP Cloud SQL Query Editor
Go to: https://console.cloud.google.com/sql/instances/fincore-npe-db/query

### 2. Run the ALTER TABLE Script
1. Click "OPEN QUERY EDITOR"
2. Copy the ENTIRE contents of `fix-schema-alter-tables.sql`
3. Paste into the query editor
4. Click "RUN"
5. Wait for completion (~30 seconds)

### 3. Verify Results
The script will automatically show verification results at the end. You should see:

```
column_name                                | COLUMN_TYPE  | status
-------------------------------------------|--------------|--------
aml_screening_results.screening_type       | varchar(20)  | ✓ PASS
aml_screening_results.screened_at          | datetime     | ✓ PASS
customer_answers.answer                    | varchar(500) | ✓ PASS
customer_answers.question_id               | int          | ✓ PASS
questionnaire_questions.question_category  | varchar(50)  | ✓ PASS
questionnaire_questions.question_id        | int          | ✓ PASS
```

**All 6 checks must show ✓ PASS**

### 4. Redeploy Application
Once ALL checks pass:

```powershell
# Trigger redeploy
git commit --allow-empty -m "Redeploy after ALTER TABLE schema fix"
git push origin main
```

### 5. Monitor Deployment
- Watch: https://github.com/kasisheraz/userManagementApi/actions
- Deployment should succeed in ~5-7 minutes
- Health check should pass

## What This Script Does

1. **ALTER screening_type**: Changes from VARCHAR(100) to VARCHAR(20)
2. **RENAME screening_date**: Renames to screened_at (if column exists)
3. **ALTER question_id**: Changes from BIGINT to INT in both tables
4. **ALTER question_category**: Changes from VARCHAR(100) to VARCHAR(50)
5. **RENAME answer_text**: Renames to answer and changes from TEXT to VARCHAR(500)
6. **RECREATE foreign key**: Drops and recreates FK with correct INT type

## Why This Works

- ALTER TABLE modifies existing structure without losing data
- No need to drop and recreate tables
- Foreign keys are handled correctly
- Atomic operation within transaction

## Expected Timeline

- Run ALTER script: 30 seconds
- Verify results: 10 seconds  
- Trigger redeploy: 1 minute
- Build & deploy: 5-7 minutes
- **Total: ~9 minutes to working application**

## Success Criteria

✅ All 6 columns show PASS in verification  
✅ Deployment succeeds without schema errors  
✅ Health endpoint returns {"status":"UP"}  
✅ No more VARCHAR/ENUM errors in logs

---

**Run `fix-schema-alter-tables.sql` in GCP Console NOW!**
