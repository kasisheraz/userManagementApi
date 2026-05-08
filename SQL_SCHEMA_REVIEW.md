# SQL Schema Review & Corrections

## Issues Found in Original SQL

### 1. Syntax Errors

#### Missing Commas
```sql
-- ❌ INCORRECT
Created_Datetime Timestamp DEFAULT CURRENT_TIMESTAMP
Created_By INT,
Last_Modified_Datetime Timestamp DEFAULT CURRENT_TIMESTAMP,

-- ✅ CORRECT
created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by INT,
last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
```

#### Incomplete Column Definitions
```sql
-- ❌ INCORRECT
Status_Description Varchar
Created_By INT

-- ✅ CORRECT
status_description VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
created_by INT,
```

### 2. Foreign Key Errors

#### Wrong Column Reference
```sql
-- ❌ INCORRECT - Table has Customer_Identifier but FK references User_Identifier
CREATE TABLE Customer_KYC_Verification (
    Customer_Identifier INT NOT NULL,
    ...
);
ALTER TABLE Customer_KYC_Verification 
  ADD CONSTRAINT fk_user_id_kyc 
  FOREIGN KEY (User_Identifier)  -- ❌ Column doesn't exist!
  REFERENCES Users(User_Identifier);

-- ✅ CORRECT - Use consistent naming
CREATE TABLE customer_kyc_verification (
    customer_identifier INT NOT NULL,
    ...
    CONSTRAINT fk_customer_id_kyc 
      FOREIGN KEY (customer_identifier) 
      REFERENCES users(User_Identifier)
);
```

#### Missing ON DELETE Clauses
```sql
-- ❌ INCORRECT - No cascade behavior defined
CONSTRAINT fk_verif_id FOREIGN KEY (Verification_Identifier)...

-- ✅ CORRECT - Define cascade behavior
CONSTRAINT fk_verif_id 
  FOREIGN KEY (verification_id) 
  REFERENCES customer_kyc_verification(verification_id) 
  ON DELETE CASCADE
```

### 3. Table Name Case Sensitivity

#### Problem
- Your Java entities use lowercase table names: `@Table(name = "customer_kyc_verification")`
- Provided SQL uses capital letters: `CREATE TABLE Customer_KYC_Verification`
- On Linux MySQL (Cloud SQL), table names are **case-sensitive**
- This causes "Table not found" errors

```sql
-- ❌ INCORRECT
CREATE TABLE Customer_KYC_Verification (...)
-- JPA looks for: customer_kyc_verification
-- Result: ERROR "Table 'Customer_KYC_Verification' doesn't exist"

-- ✅ CORRECT
CREATE TABLE customer_kyc_verification (...)
```

### 4. Missing Indexes

Performance issue - queries will be slow without proper indexes:

```sql
-- ✅ Add indexes for common queries
INDEX idx_customer_id (customer_identifier),
INDEX idx_status (status_description),
INDEX idx_verification_id (verification_id),
INDEX idx_screened_at (screened_at)
```

### 5. Missing Unique Constraints

Data integrity issue - allow duplicate answers:

```sql
-- ✅ Prevent duplicate answers for same question
UNIQUE KEY unique_customer_question (customer_identifier, question_id)
```

### 6. Column Naming Inconsistency

```sql
-- ❌ INCONSISTENT
Customer_Identifier  -- Capital letters with underscores
customer_identifier  -- Lowercase with underscores (JPA standard)

-- ✅ CONSISTENT - Use lowercase throughout
customer_identifier INT NOT NULL,
verification_id INT AUTO_INCREMENT,
created_datetime TIMESTAMP,
```

## Corrected Schema

See `V8.0__Add_KYC_Verification_Tables.sql` for the complete corrected version.

## Key Changes Made

1. ✅ All syntax errors fixed (commas, column definitions)
2. ✅ Foreign key references corrected
3. ✅ Table names changed to lowercase
4. ✅ Proper indexes added
5. ✅ Unique constraints added
6. ✅ ON DELETE CASCADE behaviors defined
7. ✅ Default values and NOT NULL constraints added
8. ✅ ENGINE and CHARSET specified for consistency
9. ✅ Default questionnaire questions inserted
10. ✅ Table comments added for documentation

## Verification Queries

After running the migration, verify with:

```sql
-- Check tables exist
SHOW TABLES LIKE '%kyc%';
SHOW TABLES LIKE '%questionnaire%';
SHOW TABLES LIKE '%aml%';

-- Check indexes
SHOW INDEX FROM customer_kyc_verification;
SHOW INDEX FROM aml_screening_results;

-- Check foreign keys
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'fincore_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Verify default questions inserted
SELECT question_id, question_category, question_text 
FROM questionnaire_questions 
ORDER BY display_order;
```

## Next Steps

1. ✅ Review the corrected SQL in `V8.0__Add_KYC_Verification_Tables.sql`
2. ✅ Test migration in local environment
3. ✅ Run migration in UAT
4. ✅ Verify all tables created correctly
5. ✅ Proceed with implementation plan

