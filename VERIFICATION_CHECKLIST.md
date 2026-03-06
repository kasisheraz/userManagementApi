# Schema Import Verification Checklist

## ✅ Import Status
Based on gcloud logs, both imports completed successfully:
- **17:28:05** - Drop Phase 2 tables (DONE)
- **17:30:54** - Import complete schema (DONE)

## 🔍 Manual Verification Steps

### Option 1: Via GCP Console SQL Query Editor

1. Go to: https://console.cloud.google.com/sql/instances/fincore-npe-db/query
2. Click "OPEN QUERY EDITOR"
3. Copy and paste the contents of `verify-schema.sql`
4. Click "RUN"
5. Check results:

**Expected Results:**
```
check_name                                    | actual_value  | expected_value | status
----------------------------------------------|---------------|----------------|--------
aml_screening_results.screening_type          | varchar(20)   | varchar(20)    | ✓ PASS
aml_screening_results.screened_at             | datetime      | datetime       | ✓ PASS
questionnaire_questions.question_id           | int           | int            | ✓ PASS
questionnaire_questions.question_category     | varchar(50)   | varchar(50)    | ✓ PASS
customer_answers.answer                       | varchar(500)  | varchar(500)   | ✓ PASS
customer_answers.question_id                  | int           | int            | ✓ PASS
customer_kyc_verification.status              | varchar(50)   | varchar(50)    | ✓ PASS
```

The second query (OLD COLUMNS CHECK) should return **0 rows** (no old columns found = GOOD!)

### Option 2: Quick DESCRIBE Check

Run these commands in GCP Cloud SQL Query Editor:

```sql
USE fincore_db;

DESCRIBE aml_screening_results;
-- Look for: screening_type varchar(20), screened_at datetime

DESCRIBE questionnaire_questions;
-- Look for: question_id int, question_category varchar(50)

DESCRIBE customer_answers;
-- Look for: answer varchar(500), question_id int

DESCRIBE customer_kyc_verification;
-- Look for: status varchar(50)
```

## ✅ Critical Checks

All these must be TRUE:
- [ ] `aml_screening_results.screening_type` = VARCHAR(20) ✓
- [ ] `aml_screening_results.screened_at` exists (not screening_date) ✓
- [ ] `questionnaire_questions.question_id` = INT (not BIGINT) ✓
- [ ] `questionnaire_questions.question_category` = VARCHAR(50) ✓
- [ ] `customer_answers.answer` = VARCHAR(500) (not answer_text TEXT) ✓
- [ ] `customer_answers.question_id` = INT (not BIGINT) ✓
- [ ] `customer_kyc_verification.status` = VARCHAR(50) ✓

## 🚀 Next Steps

### If All Checks Pass:
✅ **Schema is correct!** Proceed to redeploy:

1. **Trigger Redeploy:**
   ```powershell
   # Option A: Manual commit and push (recommended)
   git commit --allow-empty -m "Trigger redeploy after schema fix"
   git push origin main
   
   # Option B: Use trigger script
   .\trigger-redeploy.ps1
   ```

2. **Monitor Deployment:**
   - Go to: https://github.com/kasisheraz/userManagementApi/actions
   - Watch the latest workflow run
   - Deployment should take ~5-7 minutes

3. **Expected Result:**
   - ✅ Deployment succeeds
   - ✅ Health check passes
   - ✅ No schema validation errors

### If Any Checks Fail:
❌ **Schema is incorrect** - DO NOT redeploy yet!

Contact me with the output from the verification query and we'll fix the remaining issues.

## 📊 Verification Scripts Available

1. **verify-schema.sql** - Run in GCP Console Query Editor (RECOMMENDED)
2. **quick-verify-db.ps1** - PowerShell script (requires gcloud)
3. **verify-database-schema.ps1** - Detailed PS script (may have syntax issues)

## ⏱️ Timeline

If schema is correct:
- Trigger redeploy: 1 minute
- GitHub Actions build: 3-4 minutes
- Cloud Run deployment: 2-3 minutes
- **Total: ~7 minutes to live application**

## 🎯 Success Criteria

Application deployment succeeds when:
- ✅ Container starts without errors
- ✅ Hibernate schema validation passes
- ✅ Health endpoint responds: `{"status":"UP"}`
- ✅ All 30+ API endpoints accessible

---

**Ready to verify?** Run `verify-schema.sql` in GCP Console now!
