# NPE Environment Deployment Plan
## Foreign Key Constraints for Users Table

**Date:** 2026-03-29  
**Environment:** NPE (Non-Production Environment)  
**Database:** Cloud SQL - fincore_db  
**Application:** User Management API  

---

## Overview
This deployment adds foreign key constraints to the `Users` table to enforce referential integrity with the `Address` table for residential and postal addresses.

---

## Changes Summary

### Database Changes
- **Migration File:** `V4.0__Add_Users_Address_Foreign_Keys.sql`
- **SQL Changes:**
  ```sql
  ALTER TABLE Users ADD CONSTRAINT fk_add1_id 
    FOREIGN KEY (Residential_Address_Identifier) REFERENCES Address(Address_Identifier);
  
  ALTER TABLE Users ADD CONSTRAINT fk_add2_id 
    FOREIGN KEY (Postal_Address_Identifier) REFERENCES Address(Address_Identifier);
  ```

### Code Changes
1. Updated `schema.sql` to include foreign key constraints
2. Added Flyway migration `V4.0__Add_Users_Address_Foreign_Keys.sql`
3. Fixed `QuestionCategory` enum (added OCCUPATION and INCOME values)

---

## Deployment Options

### Option 1: Automatic Deployment via Flyway (Recommended)

**Description:** Let the application's Flyway migration automatically apply the constraints when the new version is deployed.

**Prerequisites:**
- Flyway is enabled in the application configuration
- Database user has ALTER TABLE privileges
- No orphaned address references exist in the Users table

**Steps:**
1. **Deploy Application to Cloud Run:**
   ```powershell
   cd /path/to/userManagementApi
   .\deploy-cloudrun-npe.ps1
   ```

2. **Monitor Deployment:**
   - Check Cloud Run logs for Flyway migration messages
   - Verify migration V4.0 was applied successfully

3. **Verify Constraints:**
   ```sql
   SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME
   FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
   WHERE TABLE_SCHEMA = 'fincore_db'
     AND TABLE_NAME = 'Users'
     AND CONSTRAINT_NAME IN ('fk_add1_id', 'fk_add2_id');
   ```

**Expected Outcome:**
- Application deploys successfully
- Flyway applies V4.0 migration
- Foreign key constraints are active

---

### Option 2: Manual Database Update (If Flyway Fails or Not Configured)

**Description:** Manually execute the SQL commands against the Cloud SQL database.

**Prerequisites:**
- Access to Cloud SQL database (via Cloud Console or Cloud SQL Proxy)
- DBA or ALTER TABLE privileges
- Database backup completed

**Steps:**

#### 2.1 Pre-Deployment Checks

1. **Connect to Cloud SQL:**
   ```bash
   # Using Cloud SQL Proxy
   gcloud sql connect fincore-npe-db --user=fincore_app --database=fincore_db
   
   # Or use Cloud Console SQL Editor
   ```

2. **Check for Orphaned Records:**
   ```sql
   SELECT 
       u.User_Identifier,
       u.Residential_Address_Identifier,
       u.Postal_Address_Identifier
   FROM Users u
   WHERE (u.Residential_Address_Identifier IS NOT NULL 
          AND u.Residential_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address))
      OR (u.Postal_Address_Identifier IS NOT NULL 
          AND u.Postal_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address));
   ```
   
   **If orphaned records exist:**
   - Option A: Set them to NULL: `UPDATE Users SET Residential_Address_Identifier = NULL WHERE ...`
   - Option B: Create corresponding Address records
   - Option C: Delete the orphaned user records (if appropriate)

3. **Create Database Backup:**
   ```bash
   gcloud sql backups create --instance=fincore-npe-db
   ```

#### 2.2 Apply Foreign Key Constraints

1. **Execute the Migration Script:**
   - Use the file: `manual-db-migration-foreign-keys.sql`
   - Or execute directly:

   ```sql
   -- Add Foreign Key Constraint for Residential Address
   ALTER TABLE Users 
   ADD CONSTRAINT fk_add1_id 
   FOREIGN KEY (Residential_Address_Identifier) 
   REFERENCES Address(Address_Identifier);

   -- Add Foreign Key Constraint for Postal Address
   ALTER TABLE Users 
   ADD CONSTRAINT fk_add2_id 
   FOREIGN KEY (Postal_Address_Identifier) 
   REFERENCES Address(Address_Identifier);
   ```

2. **Verify Constraints Were Created:**
   ```sql
   SHOW CREATE TABLE Users;
   ```
   
   Look for the constraint definitions in the output.

#### 2.3 Post-Deployment Verification

1. **Test Constraint Enforcement:**
   ```sql
   -- This should fail with foreign key constraint error
   INSERT INTO Users (Phone_Number, Residential_Address_Identifier) 
   VALUES ('+999999999', 99999);
   ```
   
   Expected: Error about foreign key constraint violation

2. **Mark Flyway Migration as Complete** (if using manual approach):
   ```sql
   INSERT INTO flyway_schema_history 
   (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
   VALUES 
   ((SELECT MAX(installed_rank) + 1 FROM flyway_schema_history), 
    '4.0', 
    'Add Users Address Foreign Keys', 
    'SQL', 
    'V4.0__Add_Users_Address_Foreign_Keys.sql', 
    NULL, 
    USER(), 
    0, 
    1);
   ```

---

## Application Deployment

### Deploy to NPE Environment

**Using PowerShell Script:**
```powershell
cd /home/runner/work/userManagementApi/userManagementApi
.\deploy-cloudrun-npe.ps1
```

**Using gcloud CLI Directly:**
```bash
# Set variables
PROJECT_ID="project-07a61357-b791-4255-a9e"
REGION="europe-west2"
SERVICE_NAME="fincore-npe-api"
IMAGE_NAME="fincore-api"

# Deploy
gcloud run deploy $SERVICE_NAME \
  --image=gcr.io/$PROJECT_ID/$IMAGE_NAME:latest \
  --region=$REGION \
  --platform=managed \
  --allow-unauthenticated \
  --service-account=fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com \
  --memory=1Gi \
  --cpu=1 \
  --timeout=900 \
  --max-instances=3 \
  --min-instances=0 \
  --set-env-vars="DB_NAME=fincore_db,DB_USER=fincore_app,DB_PASSWORD=<password>,SPRING_PROFILES_ACTIVE=npe" \
  --port=8080 \
  --project=$PROJECT_ID
```

---

## Verification & Testing

### 1. Health Check
```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe fincore-npe-api --region=europe-west2 --format='value(status.url)')

# Check health
curl $SERVICE_URL/actuator/health
```

Expected response: `{"status":"UP"}`

### 2. Database Verification
```sql
-- Verify foreign keys exist
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'fincore_db'
  AND TABLE_NAME = 'Users'
  AND CONSTRAINT_NAME IN ('fk_add1_id', 'fk_add2_id');
```

### 3. API Testing
- Use Postman collection to test user CRUD operations
- Verify address relationships work correctly
- Test that invalid address IDs are rejected

---

## Rollback Plan

### If Deployment Fails

**Option 1: Rollback Application**
```bash
# Revert to previous version
gcloud run services update-traffic fincore-npe-api \
  --to-revisions=<previous-revision>=100 \
  --region=europe-west2
```

**Option 2: Remove Foreign Key Constraints**
```sql
ALTER TABLE Users DROP FOREIGN KEY fk_add1_id;
ALTER TABLE Users DROP FOREIGN KEY fk_add2_id;

-- Also remove from Flyway history if needed
DELETE FROM flyway_schema_history WHERE version = '4.0';
```

---

## Post-Deployment Tasks

- [ ] Verify application is running in NPE environment
- [ ] Confirm foreign key constraints are active
- [ ] Test user CRUD operations via Postman
- [ ] Verify address validation is working
- [ ] Update Confluence documentation (mentioned in requirements)
- [ ] Monitor application logs for any errors
- [ ] Check database performance impact (minimal expected)

---

## Notes

1. **Data Integrity:** The foreign key constraints will prevent:
   - Creating users with non-existent address IDs
   - Deleting addresses that are referenced by users
   - Invalid address references in the database

2. **Performance Impact:** Minimal - foreign keys add negligible overhead for INSERT/UPDATE operations

3. **Future Considerations:**
   - Consider adding `ON DELETE SET NULL` or `ON DELETE CASCADE` if business logic requires it
   - Monitor for any application errors related to address handling

---

## Contact Information

For questions or issues during deployment, contact:
- Database Team: [DBA contacts]
- DevOps Team: [DevOps contacts]
- Development Team: [Dev team contacts]
