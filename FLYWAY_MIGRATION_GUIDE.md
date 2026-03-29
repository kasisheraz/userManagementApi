# Flyway Database Migration Guide

## Overview
This project now uses Flyway for database migration management. Flyway automatically applies database schema changes when the application starts, ensuring consistent database state across all environments.

## Why Flyway?

### Benefits
- **Version Control**: Database changes are tracked in version-controlled migration files
- **Consistency**: Ensures all environments (local, NPE, production) have the same schema
- **Safety**: Prevents manual SQL errors and provides rollback capabilities
- **Automation**: Migrations run automatically on application startup
- **Audit Trail**: Complete history of all database changes

## Migration Files Location

All Flyway migration files are located in:
```
src/main/resources/db/migration/
```

### Current Migrations

| Version | File | Description |
|---------|------|-------------|
| V1.0 | `V1.0__Initial_Schema.sql` | Initial database schema (Users, Roles, Permissions, Organisations, KYC, Address) |
| V2.0 | `V2.0__Initial_Data.sql` | Seed data (default roles, permissions, admin users, sample data) |
| V3.0 | `V3.0__Create_KYC_AML_Verification_Tables.sql` | KYC and AML verification tables |
| V4.0 | `V4.0__Add_Users_Address_Foreign_Keys.sql` | Foreign key constraints for Users-Address relationships |

## Configuration

### Application Configuration (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
```

### NPE Environment (application-npe.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Changed from 'update' - Flyway manages schema
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true  # Prevents accidental data loss
```

## How Flyway Works

### Naming Convention
Migration files follow this pattern:
```
V{version}__{description}.sql
```

Examples:
- `V1.0__Initial_Schema.sql`
- `V2.0__Initial_Data.sql`
- `V3.0__Create_KYC_Tables.sql`

### Execution Order
1. Flyway checks the `flyway_schema_history` table
2. Identifies pending migrations (not yet executed)
3. Executes migrations in version order
4. Records successful migrations in `flyway_schema_history`

### First Run (Baseline)
On first run in an existing database:
- `baseline-on-migrate: true` creates a baseline at version 0
- Existing schema is preserved
- Only new migrations are applied

## Creating New Migrations

### Step 1: Create Migration File
Create a new file in `src/main/resources/db/migration/`:
```bash
# Example: Adding a new table
V5.0__Add_Customer_Table.sql
```

### Step 2: Write SQL
```sql
-- V5.0__Add_Customer_Table.sql
CREATE TABLE Customer (
    Customer_Identifier INT PRIMARY KEY AUTO_INCREMENT,
    User_Identifier INT NOT NULL,
    Customer_Type VARCHAR(50),
    Status_Description VARCHAR(20) DEFAULT 'ACTIVE',
    Created_Datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_user FOREIGN KEY (User_Identifier) REFERENCES Users(User_Identifier)
);

CREATE INDEX idx_customer_user ON Customer(User_Identifier);
CREATE INDEX idx_customer_status ON Customer(Status_Description);
```

### Step 3: Test Locally
```bash
# Start application - Flyway runs automatically
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Check logs for migration success
# Look for: "Flyway: Successfully applied X migrations"
```

### Step 4: Commit and Deploy
```bash
git add src/main/resources/db/migration/V5.0__Add_Customer_Table.sql
git commit -m "Add Customer table migration"
git push
```

## Deployment Process

### NPE Deployment (Automatic)

When deploying to NPE:
1. Build the application with Maven
2. Docker image includes migration files
3. Application starts on Cloud Run
4. Flyway automatically runs pending migrations
5. Application becomes ready to serve requests

**No manual database updates required!** ✅

### Deployment Command
```bash
# Using deployment script
./deploy-cloudrun-npe.ps1

# Or manual deployment
gcloud run deploy fincore-npe-api \
  --image=gcr.io/project-id/fincore-api:latest \
  --region=europe-west2 \
  --platform=managed
```

### Monitoring Deployment

Check logs for Flyway execution:
```bash
# Get Cloud Run logs
gcloud run services logs read fincore-npe-api \
  --region=europe-west2 \
  --limit=100 | grep -i flyway
```

Expected output:
```
INFO  Flyway Community Edition by Redgate
INFO  Database: jdbc:mysql://34.89.96.239:3306/fincore_db (MySQL 8.0)
INFO  Successfully validated 4 migrations (execution time 00:00.012s)
INFO  Current version of schema `fincore_db`: 4.0
INFO  Schema `fincore_db` is up to date. No migration necessary.
```

## Verification

### Check Migration Status

Connect to database and verify:
```sql
-- Check Flyway history
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected results:
```
+----------------+---------+--------------------------------------+------+------------------------------------------+---------------------+---------+
| installed_rank | version | description                          | type | script                                   | installed_on        | success |
+----------------+---------+--------------------------------------+------+------------------------------------------+---------------------+---------+
|              1 | 1.0     | Initial Schema                       | SQL  | V1.0__Initial_Schema.sql                 | 2026-03-29 10:00:00 |       1 |
|              2 | 2.0     | Initial Data                         | SQL  | V2.0__Initial_Data.sql                   | 2026-03-29 10:00:01 |       1 |
|              3 | 3.0     | Create KYC AML Verification Tables   | SQL  | V3.0__Create_KYC_AML_Verification...     | 2026-03-29 10:00:02 |       1 |
|              4 | 4.0     | Add Users Address Foreign Keys       | SQL  | V4.0__Add_Users_Address_Foreign_Keys.sql | 2026-03-29 10:00:03 |       1 |
+----------------+---------+--------------------------------------+------+------------------------------------------+---------------------+---------+
```

### Health Check

Verify application health after migration:
```bash
curl https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Troubleshooting

### Migration Failed

If a migration fails:

1. **Check Flyway History**
   ```sql
   SELECT * FROM flyway_schema_history WHERE success = 0;
   ```

2. **Manual Repair** (if needed)
   ```sql
   DELETE FROM flyway_schema_history WHERE success = 0;
   ```

3. **Fix Migration File**
   - Correct the SQL in the migration file
   - Redeploy application

### Out-of-Order Migration

If you need to insert a migration between existing versions:
```yaml
# In application.yml
spring:
  flyway:
    out-of-order: true  # Temporarily enable
```

### Clean Database (Local Only!)

**⚠️ WARNING: Never use in production!**

To reset local database:
```bash
# H2 local database
rm -rf ~/fincore-h2-data/*

# MySQL local database
mysql -u root -p
DROP DATABASE fincore_db;
CREATE DATABASE fincore_db;
```

Then restart application - Flyway will recreate everything.

## Best Practices

### ✅ Do's

1. **Never modify executed migrations** - Create new ones instead
2. **Test migrations locally first** - Before deploying to NPE/Production
3. **Use descriptive names** - Clear version numbers and descriptions
4. **Include rollback scripts** - Document how to reverse changes
5. **Make migrations idempotent** - Use `CREATE TABLE IF NOT EXISTS`
6. **Keep migrations small** - One logical change per migration
7. **Version control all migrations** - Git tracks all changes

### ❌ Don'ts

1. **Don't modify successful migrations** - Will cause checksum errors
2. **Don't use `flyway:clean` in production** - Deletes all data!
3. **Don't skip version numbers** - Maintain sequential order
4. **Don't include sensitive data** - Use environment variables
5. **Don't mix DDL and DML** - Separate schema changes from data changes

## Migration Examples

### Adding a Column
```sql
-- V5.0__Add_Customer_Rating.sql
ALTER TABLE Customer 
ADD COLUMN Rating_Score INT DEFAULT 0 COMMENT 'Customer rating from 0-100';

CREATE INDEX idx_customer_rating ON Customer(Rating_Score);
```

### Adding Data
```sql
-- V6.0__Add_New_Permissions.sql
INSERT INTO Permissions (Permission_Name, Description, Resource, Action) 
VALUES ('REPORT_READ', 'Read reports', 'reports', 'read');

INSERT INTO Permissions (Permission_Name, Description, Resource, Action) 
VALUES ('REPORT_GENERATE', 'Generate reports', 'reports', 'write');
```

### Complex Migration
```sql
-- V7.0__Refactor_User_Status.sql

-- Step 1: Add new column
ALTER TABLE Users 
ADD COLUMN New_Status_Code VARCHAR(20);

-- Step 2: Migrate data
UPDATE Users 
SET New_Status_Code = CASE 
    WHEN Status_Description = 'ACTIVE' THEN 'ACT'
    WHEN Status_Description = 'INACTIVE' THEN 'INA'
    WHEN Status_Description = 'SUSPENDED' THEN 'SUS'
    ELSE 'PEN'
END;

-- Step 3: Drop old column (in separate migration if needed)
-- ALTER TABLE Users DROP COLUMN Status_Description;

-- Step 4: Rename new column
-- ALTER TABLE Users CHANGE New_Status_Code Status_Code VARCHAR(20);
```

## Support

For issues or questions:
- Check Flyway logs: `grep -i flyway application.log`
- Review migration history: `SELECT * FROM flyway_schema_history;`
- Consult Flyway documentation: https://flywaydb.org/documentation/

## Summary

**With Flyway configured:**
- ✅ Database updates happen automatically on deployment
- ✅ No manual SQL execution required
- ✅ All environments stay in sync
- ✅ Complete audit trail of changes
- ✅ Safe rollback capabilities

**Your workflow:**
1. Create migration file
2. Test locally
3. Commit to Git
4. Deploy application
5. Flyway handles the rest automatically! 🚀
