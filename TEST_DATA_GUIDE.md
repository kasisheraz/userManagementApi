# Test Data Summary & Loading Instructions

## Overview
Comprehensive test data has been prepared for UI testing. The data includes users, organizations, KYC documents, questionnaires, and more.

## What's Been Created

### 📄 Files Generated
1. **[insert-test-data.sql](insert-test-data.sql)** (553 lines)
   - Complete SQL script with INSERT statements
   - Ready to import into Cloud SQL database

2. **[load-test-data.ps1](load-test-data.ps1)**
   - PowerShell script for automated import via GCS
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())# View latest logs
gcloud run logs read fincore-npe-api --region=europe-west2 --limit=100

# Tail logs in real-time
gcloud run logs tail fincore-npe-api --region=europe-west2

# Filter for specific text (e.g., JWT Filter logs)
gcloud run logs read fincore-npe-api --region=europe-west2 --limit=200 | Select-String "JWT"

# View logs for specific time period (last 1 hour)
gcloud run logs read fincore-npe-api --region=europe-west2 --limit=500 --format="table(timestamp,textPayload)"
3. **[UI_INTEGRATION_GUIDE.md](UI_INT EGRATION_GUIDE.md)**
   - Complete guide for UI integration and testing

## Test Data Contents

### Core Access Control
- **17 Permissions**: READ, CREATE, UPDATE, DELETE for all resources
- **4 Roles**: 
  - ADMIN (full access)
  - COMPLIANCE_OFFICER (KYC, AML, user/org read)
  - OPERATIONAL_STAFF (limited access)
  - USER (basic access)

### Users (12 total)
| Phone | Email | Role | Name | Status |
|-------|-------|------|------|--------|
| +1234567890 | admin@fincore.com | ADMIN | System Administrator | ACTIVE |
| +447911123456 | sarah.williams@fintech.com | COMPLIANCE_OFFICER | Sarah Williams | ACTIVE |
| +447911123457 | john.smith@fintech.com | COMPLIANCE_OFFICER | John Smith | ACTIVE |
| +447911123458 | emma.brown@fintech.com | OPERATIONAL_STAFF | Emma Brown | ACTIVE |
| +447911123459 | michael.jones@fintech.com | OPERATIONAL_STAFF | Michael Jones | ACTIVE |
| +447911123460 | olivia.taylor@business.com | USER | Olivia Taylor | ACTIVE |
| +447911123461 | james.wilson@business.com | USER | James Wilson | ACTIVE |
| +447911123462 | sophia.davies@startup.com | USER | Sophia Davies | ACTIVE |
| +447911123463 | william.evans@corp.com | USER | William Evans | ACTIVE |
| +447911123464 | isabella.moore@enterprise.com | USER | Isabella Moore | ACTIVE |
| +447911123465 | oliver.martin@company.com | USER | Oliver Martin | INACTIVE |
| +447911123466 | ava.clark@business.co.uk | USER | Ava Clark | PENDING |

### Organizations (8 total)
| ID | Legal Name | Type | Owner | Status |
|----|------------|------|-------|--------|
| 1 | FinTech Solutions Limited | LTD | Admin | ACTIVE |
| 2 | Digital Payments UK Ltd | LTD | Olivia | ACTIVE |
| 3 | GlobalTransfer Corp | PLC | James | ACTIVE |
| 4 | Financial Inclusion Foundation | CHARITY | Sophia | ACTIVE |
| 5 | Smith Financial Consulting | SOLE_TRADER | William | ACTIVE |
| 6 | Brown & Associates LLP | LLP | Isabella | ACTIVE |
| 7 | NewPay Technologies Ltd | LTD | Oliver | PENDING |
| 8 | Legacy Transfers Ltd | LTD | Ava | SUSPENDED |

### Addresses (15 total)
- Business addresses in London, Manchester, Birmingham, Edinburgh, Leeds
- Registered addresses across UK cities
- Correspondence and residential addresses

### KYC Documents (16 total)
- Certificate of Incorporation
- Proof of Address
- Directors ID
- FCA Licenses
- Financial Statements
- Charity Registration
- Statuses: VERIFIED, PENDING, REJECTED, EXPIRED

### Customer KYC Verifications (10 total)
- Statuses: APPROVED (6), PENDING (2), IN_REVIEW (1), REJECTED (1)
- Verification levels: BASIC, ENHANCED
- Risk levels: LOW, MEDIUM, HIGH
- Complete with Sumsub applicant IDs

### AML Screening Results (14 total)
- PEP screening
- Sanctions screening
- Adverse media screening
- Risk scores: 0-75
- Match details in JSON format

### Questionnaire Questions (20 total)
Categories:
- BUSINESS_PURPOSE (4 questions)
- SOURCE_OF_FUNDS (3 questions)
- BUSINESS_OPERATIONS (4 questions)
- COMPLIANCE (4 questions)
- RISK_ASSESSMENT (3 questions)
- ADDITIONAL (2 questions)

### Customer Answers (48 total)
- Complete questionnaire responses from 3 users
- Realistic business scenarios
- Olivia Taylor (DigiPay UK): 20 answers
- James Wilson (GlobalTransfer): 20 answers
- Sophia Davies (startup): 8 partial answers

## How to Load Test Data

### Option 1: Cloud Console (Recommended)
1. Go to [Cloud SQL Instances](https://console.cloud.google.com/sql/instances?project=fincore-platform)
2. Click on instance: **fincore-npe-db**
3. Go to **Import** tab
4. Click **Import**
5. Upload [insert-test-data.sql](insert-test-data.sql)
6. Select database: **fincore_db**
7. Click **Import**
8. Wait 2-3 minutes for completion

### Option 2: GCS Import (Automated)
```powershell
# Step 1: Upload SQL file to GCS Bucket
gsutil cp insert-test-data.sql gs://fincore-npe-db-backups/test-data/

# Step 2: Import from GCS
gcloud sql import sql fincore-npe-db `
  gs://fincore-npe-db-backups/test-data/insert-test-data.sql `
  --database=fincore_db `
  --project=fincore-platform

# Step 3: Clean up
gsutil rm gs://fincore-npe-db-backups/test-data/insert-test-data.sql
```

### Option 3: MySQL Client (If Installed)
```bash
mysql -h 34.89.96.239 -u fincore_app -p fincore_db < insert-test-data.sql
```

### Option 4: Via Application Endpoint (Future)
Create an admin-only endpoint to populate test data programmatically.

## Verification After Import

### Quick Check
```powershell
.\quick-test-gcp.ps1
```

### Manual Verification Queries
```sql
-- Check record counts
SELECT 'users' as tbl, COUNT(*) as cnt FROM users
UNION ALL SELECT 'roles', COUNT(*) FROM roles
UNION ALL SELECT 'organisation', COUNT(*) FROM organisation
UNION ALL SELECT 'kyc_documents', COUNT(*) FROM kyc_documents
UNION ALL SELECT 'questionnaire_questions', COUNT(*) FROM questionnaire_questions;

-- Expected counts:
-- users: 12
-- roles: 4
-- organisation: 8
-- kyc_documents: 16
-- questionnaire_questions: 20
```

## UI Testing Scenarios

### Scenario 1: View User List
1. Login as admin (+1234567890)
2. Navigate to Users page
3. Should see 12 users
4. Check filters (role, status)
5. Test pagination if implemented

### Scenario 2: View Organization List
1. Login as compliance officer (+447911123456)
2. Navigate to Organizations page
3. Should see 8 organizations
4. Various types: LTD, PLC, CHARITY, etc.
5. Various statuses: ACTIVE, PENDING, SUSPENDED

### Scenario 3: Review KYC Documents
1. Login as compliance officer
2. Navigate to KYC Documents page
3. Should see 16 documents
4. Filter by status: VERIFIED, PENDING, REJECTED
5. Test document verification workflow

### Scenario 4: View AML Screening
1. Login as compliance officer
2. Navigate to AML Screening page
3. Should see 14 screening results
4. Check risk scores and match details
5. Filter by screening type (PEP, SANCTIONS, ADVERSE_MEDIA)

### Scenario 5: Manage Questionnaires
1. Login as admin
2. Navigate to Questionnaires page
3. Should see 20 questions across 5 categories
4. View customer answers (48 total)
5. Test question management (create, update, deactivate)

### Scenario 6: Organization Owner View
1. Login as Olivia (+447911123460)
2. Should see "Digital Payments UK Ltd" organization
3. View associated KYC documents
4. View answered questionnaire (20 answers)
5. Test updating organization details

### Scenario 7: Create New Records
1. Login as admin
2. Create new user (phone: +447999999999)
3. Create new organization
4. Upload KYC documents
5. Verify records appear in lists

### Scenario 8: Permission Testing
1. Login as USER role
2. Verify limited access (should NOT see):
   - AML screening results
   - Other users' KYC documents
   - System-wide reports
3. Should see:
   - Own profile
   - Own organization(s)
   - Public questionnaires

## Expected UI Behavior

### Users Page
- Display 12 users in table/list
- Show role badges (ADMIN, COMPLIANCE_OFFICER, etc.)
- Show status indicators (ACTIVE, INACTIVE, PENDING)
- Filter by role and status
- Search by name, email, phone

### Organizations Page
- Display 8 organizations
- Show organization type (LTD, PLC, etc.)
- Show status (ACTIVE, PENDING, SUSPENDED)
- Show owner name
- Link to KYC documents
- Filter by type and status

### KYC Documents Page
- Display 16 documents
- Group by organization
- Show document type and status
- Show verification date and verifier
- Allow upload of new documents
- Verify/reject actions for compliance officers

### AML Screening Page
- Display 14 screening results
- Show screening type, risk score
- Display match details
- Filter by match found (true/false)
- Link to user/verification record

### Questionnaires Page
- Display 20 questions
- Organize by category
- Show display order
- View answers (48 total from 3 users)
- Admin can manage questions

## Common Issues & Solutions

### Issue: Data Already Exists
**Symptom**: Import fails with "Duplicate entry" error  
**Cause**: Test data was partially imported before  
**Solution**:
- Option A: Clear data first (admin only):
  ```sql
  DELETE FROM customer_answers WHERE user_id > 1;
  DELETE FROM questionnaire_questions WHERE question_id > 0;
  -- etc...
  ```
- Option B: Modify SQL to use `INSERT IGNORE`
- Option C: Accept existing data and only add missing records

### Issue: Foreign Key Violations
**Symptom**: Import fails with foreign key constraint error  
**Cause**: Parent records don't exist  
**Solution**: Ensure admin user (ID=1) exists before running import

### Issue: Permission Denied
**Symptom**: GCS upload fails  
**Solution**: 
```powershell
gcloud auth login
gcloud config set project fincore-platform
```

### Issue: Data Not Visible in UI
**Symptom**: Import succeeds but UI shows empty  
**Cause**: UI might be cached or filtering data  
**Solution**:
- Clear browser cache
- Check browser console for errors
- Verify API endpoints return data (use Postman)
- Check JWT token includes correct permissions

## Next Steps

1. **Import the Test Data**
   - Use Cloud Console Import (easiest)
   - Or run GCS import script

2. **Wait for CORS Deployment**
   - GitHub Actions should complete (~10 minutes from last push)
   - New revision will have CORS fix

3. **Start UI Testing**
   - Login with test users
   - Navigate all pages
   - Verify data displays correctly

4. **API Endpoint Testing**
   - Use Postman collection: [phase2-postman-collection.json](phase2-postman-collection.json)
   - Test all CRUD operations
   - Verify permissions and role-based access

5. **Report Issues**
   - Document any missing data
   - Note UI display problems
   - Check API response formats

## Summary

✅ Test data prepared and documented  
✅ 120+ records across 11 tables  
✅ Realistic business scenarios  
✅ Multiple user roles and permissions  
✅ Various statuses for testing workflows  
✅ Ready for comprehensive UI testing  

**File**: [insert-test-data.sql](insert-test-data.sql) - Ready to import!  
**Size**: 553 lines, ~45KB  
**Import Time**: 2-3 minutes  
**Recommended Method**: Cloud Console Import  

---

**Generated**: March 6, 2026  
**Purpose**: UI Testing & Development  
**Environment**: NPE (Non-Production)  
**Database**: fincore_db @ fincore-npe-db  
