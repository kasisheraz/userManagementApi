# Quick Start: Deploy Foreign Key Constraints to NPE

## What's Been Done
- ✅ Added foreign key constraints to Users table for address references
- ✅ Created Flyway migration (V4.0)
- ✅ Updated schema.sql
- ✅ Code compiles successfully

## Two Ways to Deploy

### 🚀 Option 1: Automatic (Recommended)

**Just deploy the app - Flyway handles everything:**

```powershell
# Navigate to project directory
cd /home/runner/work/userManagementApi/userManagementApi

# Run deployment script
.\deploy-cloudrun-npe.ps1
```

That's it! Flyway will automatically:
- Apply the V4.0 migration
- Create the foreign key constraints
- Verify database integrity

**Verify it worked:**
```bash
# Get service URL
gcloud run services describe fincore-npe-api --region=europe-west2 --format='value(status.url)'

# Test health
curl <SERVICE_URL>/actuator/health
```

---

### 🔧 Option 2: Manual Database Update

**If you prefer to update the database directly:**

1. **Connect to Cloud SQL:**
   ```bash
   gcloud sql connect fincore-npe-db --user=fincore_app --database=fincore_db
   ```

2. **Check for data issues** (optional but recommended):
   ```sql
   SELECT u.User_Identifier, u.Residential_Address_Identifier, u.Postal_Address_Identifier
   FROM Users u
   WHERE (u.Residential_Address_Identifier IS NOT NULL 
          AND u.Residential_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address))
      OR (u.Postal_Address_Identifier IS NOT NULL 
          AND u.Postal_Address_Identifier NOT IN (SELECT Address_Identifier FROM Address));
   ```

3. **Apply the constraints:**
   ```sql
   ALTER TABLE Users 
   ADD CONSTRAINT fk_add1_id 
   FOREIGN KEY (Residential_Address_Identifier) 
   REFERENCES Address(Address_Identifier);

   ALTER TABLE Users 
   ADD CONSTRAINT fk_add2_id 
   FOREIGN KEY (Postal_Address_Identifier) 
   REFERENCES Address(Address_Identifier);
   ```

4. **Verify:**
   ```sql
   SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME
   FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
   WHERE TABLE_NAME = 'Users' 
     AND CONSTRAINT_NAME IN ('fk_add1_id', 'fk_add2_id');
   ```

5. **Deploy the app:**
   ```powershell
   .\deploy-cloudrun-npe.ps1
   ```

---

## Verification Checklist

After deployment, verify:
- [ ] Application is running: Check Cloud Run console
- [ ] Health endpoint responds: `/actuator/health`
- [ ] Foreign keys exist: Run verification SQL
- [ ] API works: Test with Postman collection
- [ ] No errors in logs: Check Cloud Run logs

---

## Files Reference

- **Flyway Migration:** `src/main/resources/db/migration/V4.0__Add_Users_Address_Foreign_Keys.sql`
- **Manual Script:** `manual-db-migration-foreign-keys.sql`
- **Full Guide:** `NPE_DEPLOYMENT_PLAN.md`

---

## Need Help?

- **Detailed deployment steps:** See `NPE_DEPLOYMENT_PLAN.md`
- **Manual migration steps:** See `manual-db-migration-foreign-keys.sql`
- **Rollback instructions:** See `NPE_DEPLOYMENT_PLAN.md` → Rollback section

---

## What These Constraints Do

The foreign key constraints ensure:
- Users can only reference valid addresses
- Can't delete addresses that are in use
- Database maintains referential integrity
- Better data quality and consistency

**Constraint Details:**
- `fk_add1_id`: Links `Users.Residential_Address_Identifier` to `Address.Address_Identifier`
- `fk_add2_id`: Links `Users.Postal_Address_Identifier` to `Address.Address_Identifier`
