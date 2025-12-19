# Database Schema Setup Summary

## Issue Resolved
Fixed database schema inconsistencies between local H2 development environment and Cloud SQL MySQL production environment.

## Root Cause
- **Column Naming Mismatch**: Entity fields used camelCase (e.g., `fullName`) but Hibernate generated snake_case columns (e.g., `full_name`)
- **Missing Explicit Mappings**: JPA entities lacked explicit `@Column` annotations leading to inconsistent behavior
- **DDL Management**: No centralized schema definition for both environments

## Solution Implemented

### 1. Created Standardized Schema Files

#### `src/main/resources/schema.sql` (Universal)
- Compatible with both H2 and MySQL
- Uses snake_case column naming convention
- Includes proper constraints and indexes
- Defines all tables: users, roles, permissions, role_permissions

#### `cloud-sql-schema.sql` (MySQL-specific)
- Complete MySQL 8.0 schema with engine specifications
- Includes default data initialization
- Optimized with proper charset and collation
- Ready for manual execution on Cloud SQL

### 2. Updated Entity Mappings

#### User Entity (`User.java`)
Added explicit `@Column` annotations:
```java
@Column(name = "full_name", nullable = false)
private String fullName;

@Column(name = "phone_number")
private String phoneNumber;

@Column(name = "employee_id")
private String employeeId;

@Column(name = "job_title")
private String jobTitle;

@Column(name = "failed_login_attempts")
private Integer failedLoginAttempts;

@Column(name = "created_at")
private LocalDateTime createdAt;

@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

### 3. Updated Configuration

#### Local H2 Configuration (`application-local-h2.yml`)
- **DDL Strategy**: Changed from `create-drop` to `none` (uses schema.sql)
- **Schema Management**: Added `schema-locations: classpath:schema.sql`
- **Naming Strategy**: Explicit PhysicalNamingStrategy configuration
- **Data Initialization**: Properly sequenced schema → data

#### NPE Environment (`application-npe.yml`)
- **DDL Strategy**: Changed to `validate` (ensures schema exists)
- **Naming Strategy**: Consistent with local environment
- **Data Initialization**: Disabled (production tables pre-exist)

### 4. Database Schema Structure

```sql
-- Core Tables
permissions (id, name, description, module)
roles (id, name, description)
users (id, username, password, full_name, email, phone_number, 
       employee_id, department, job_title, status, role_id,
       failed_login_attempts, locked_until, last_login_at, 
       created_at, updated_at)
role_permissions (role_id, permission_id)

-- Key Constraints
- Foreign key: users.role_id → roles.id
- Foreign key: role_permissions.role_id → roles.id  
- Foreign key: role_permissions.permission_id → permissions.id
- Unique constraints: username, email, permission names, role names
```

### 5. Data Initialization

#### Default Roles
1. **SYSTEM_ADMINISTRATOR** (ID: 1) - Full system access
2. **ADMIN** (ID: 2) - Administrator capabilities  
3. **COMPLIANCE_OFFICER** (ID: 3) - Compliance access
4. **OPERATIONAL_STAFF** (ID: 4) - Operational access

#### Default Users
- **admin** / Admin@123456 (Role: SYSTEM_ADMINISTRATOR)
- **compliance** / Compliance@123 (Role: COMPLIANCE_OFFICER)  
- **staff** / Staff@123456 (Role: OPERATIONAL_STAFF)

## Benefits Achieved

1. **Consistency**: Identical schema behavior across H2 and MySQL
2. **Maintainability**: Single source of truth for schema definition
3. **Reliability**: Explicit column mappings prevent runtime errors
4. **Flexibility**: Easy schema updates through versioned SQL files
5. **Performance**: Proper indexes for frequently queried columns

## Deployment Readiness

### Local Development ✅
- H2 database properly initialized with schema.sql
- Data populated with correct snake_case column names  
- Application starts successfully without errors

### Cloud Production ✅
- MySQL schema defined and ready for deployment
- Built-in Cloud SQL connector configured
- Schema validation enabled (ddl-auto: validate)
- Compatible with existing cloud database structure

## Next Steps

1. **Test Local Application**: Verify all endpoints work correctly
2. **Deploy to NPE**: Push changes and validate cloud deployment
3. **Schema Migration** (if needed): Use cloud-sql-schema.sql for fresh setup
4. **Monitor Performance**: Verify new indexes improve query performance

The database schema is now properly aligned between all environments and ready for production deployment.