# Role-Based Access Control Implementation

## Date: April 12, 2026

## Overview
This document details the implementation of enhanced role-based access control (RBAC) and user status management improvements.

## Changes Implemented

### 1. New Business Roles Added (V7.0 Migration)

Added four new roles to support different business user types:

#### New Roles:
- **Admin**: Full system administrator access
- **Compliance**: Compliance officer with audit and review capabilities
- **Operational**: Operational staff with standard operational access
- **Business User**: Restricted access - can only view their own users and organisations

#### Permission Mapping:

**Admin Role:**
- ALL permissions (full system access)

**Compliance Role:**
- USER_READ
- CUSTOMER_READ
- ORG_READ
- DOCUMENT_READ
- QUESTIONNAIRE_READ

**Operational Role:**
- USER_READ, USER_WRITE
- CUSTOMER_READ, CUSTOMER_WRITE
- ORG_READ, ORG_WRITE
- DOCUMENT_READ, DOCUMENT_WRITE

**Business User Role:**
- USER_READ (own data only)
- ORG_READ (own data only)

### 2. Role-Based Data Filtering

Implemented security filtering in service layer to enforce data access rules:

#### User Management (`UserService`):
- **Business User**: Can only see their own user record
- **All other roles**: Can see all users

#### Organisation Management (`OrganisationService`):
- **Business User**: Can only see organisations they own
- **All other roles**: Can see all organisations

#### Implementation Details:
- Created `SecurityUtil` utility class to handle security context
- Modified `getAllUsers()` to filter based on current user's role
- Modified `getAllOrganisations()` to filter based on current user's role
- Modified `searchOrganisations()` to respect role-based filtering

### 3. User Status Auto-Population

Fixed issue where user status was not being set when creating users:

#### Before:
- Status field was null or empty after creation

#### After:
- Status automatically set to **"Active"** when creating new users
- Applies when Admin or any authorized role creates a user
- Logged in system for audit trail

## Technical Components

### Files Added:
1. **`V7.0__Add_Business_Roles.sql`**
   - Flyway migration script
   - Adds 4 new roles with appropriate permissions
   - Includes verification queries

2. **`SecurityUtil.java`**
   - Utility class for security context operations
   - Methods:
     - `getCurrentUserPhoneNumber()`: Get authenticated user's phone
     - `getCurrentUser()`: Get authenticated User entity
     - `hasRole(String roleName)`: Check if user has specific role
     - `isBusinessUser()`: Check if current user is Business User
     - `canSeeAllData()`: Check if user can see all data

### Files Modified:
1. **`UserService.java`**
   - Added `SecurityUtil` dependency
   - Modified `getAllUsers()` for role-based filtering
   - Modified `createUser()` to set default status "Active"

2. **`OrganisationService.java`**
   - Added `SecurityUtil` dependency
   - Modified `getAllOrganisations()` for role-based filtering
   - Modified `searchOrganisations()` for role-based filtering
   - Added manual pagination for filtered results

## Security Model

### Access Control Matrix:

| Role           | View All Users | View Own User | View All Orgs | View Own Orgs | Create Users | Create Orgs |
|----------------|---------------|---------------|---------------|---------------|--------------|-------------|
| Admin          | ✅            | ✅            | ✅            | ✅            | ✅           | ✅          |
| Compliance     | ✅            | ✅            | ✅            | ✅            | ❌           | ❌          |
| Operational    | ✅            | ✅            | ✅            | ✅            | ✅           | ✅          |
| Business User  | ❌            | ✅            | ❌            | ✅            | ❌           | ✅          |
| MANAGER        | ✅            | ✅            | ✅            | ✅            | ✅           | ✅          |
| USER           | ✅            | ✅            | ✅            | ✅            | ❌           | ✅          |

## Testing Instructions

### 1. Test New Roles Creation
```sql
-- Run the migration
-- Run V7.0__Add_Business_Roles.sql

-- Verify roles exist
SELECT * FROM Roles WHERE Role_Name IN ('Admin', 'Compliance', 'Operational', 'Business User');

-- Verify permissions
SELECT r.Role_Name, p.Permission_Name
FROM Roles r
JOIN Role_Permissions rp ON r.Role_Identifier = rp.Role_Identifier
JOIN Permissions p ON rp.Permission_Identifier = p.Permission_Identifier
WHERE r.Role_Name IN ('Admin', 'Compliance', 'Operational', 'Business User')
ORDER BY r.Role_Name, p.Permission_Name;
```

### 2. Test User Status Auto-Population
```powershell
# Create a new user via API
POST /api/users
{
  "phoneNumber": "+1234567890",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "role": "Admin"
}

# Verify response includes status: "Active"
# Check database
SELECT User_Identifier, First_Name, Last_Name, Status_Description 
FROM users 
WHERE Phone_Number = '+1234567890';
```

### 3. Test Business User Data Filtering

#### Test A: Login as Business User
```bash
# Authenticate as Business User
POST /api/auth/verify-otp
{
  "phoneNumber": "+1234567890",  # Business User's phone
  "otp": "123456"
}

# Get users (should only see own record)
GET /api/users
Authorization: Bearer {business_user_token}

# Response should contain only 1 user (themselves)
```

#### Test B: Login as Admin/Compliance/Operational
```bash
# Authenticate as Admin
POST /api/auth/verify-otp
{
  "phoneNumber": "+9876543210",  # Admin's phone
  "otp": "123456"
}

# Get users (should see all users)
GET /api/users
Authorization: Bearer {admin_token}

# Response should contain all users
```

#### Test C: Organisation Filtering
```bash
# As Business User - Get organisations
GET /api/organisations?page=0&size=10
Authorization: Bearer {business_user_token}

# Should only return organisations owned by this user

# As Admin - Get organisations  
GET /api/organisations?page=0&size=10
Authorization: Bearer {admin_token}

# Should return all organisations
```

## Migration Path

### For Existing Systems:

1. **Backup Database**
   ```bash
   mysqldump -u username -p database_name > backup_before_v7.sql
   ```

2. **Run Migration**
   - Place `V7.0__Add_Business_Roles.sql` in `src/main/resources/db/migration/`
   - Restart application (Flyway will auto-run migration)
   - OR manually run the SQL script

3. **Assign Roles to Existing Users**
   ```sql
   -- Update existing users with appropriate roles
   UPDATE users u
   JOIN Roles r ON r.Role_Name = 'Admin'
   SET u.Role_Identifier = r.Role_Identifier
   WHERE u.Phone_Number = '+admin-phone-number';
   ```

4. **Deploy Updated Application**
   - Build: `mvn clean package`
   - Deploy updated JAR/WAR

5. **Verify Deployment**
   - Check logs for successful migration
   - Test each role's access patterns
   - Verify status auto-population on new user creation

## Backward Compatibility

- **Existing roles** (USER, MANAGER, SUPER_ADMIN, SYSTEM_ADMINISTRATOR, ADMIN, COMPLIANCE_OFFICER, OPERATIONAL_STAFF) continue to work
- **Existing users** maintain their current roles
- **New roles** added without affecting existing functionality
- **Default behavior**: Non-Business Users can see all data (existing behavior preserved)

## Security Considerations

### What This Protects:
✅ Business Users cannot see other users' data
✅ Business Users cannot see other users' organisations
✅ Data isolation at service layer (enforced before database query)
✅ Consistent filtering across all endpoints

### What This Does NOT Protect:
⚠️ API endpoints still need to enforce create/update/delete permissions
⚠️ Frontend should hide irrelevant UI elements based on role
⚠️ Direct database access bypasses this security layer

### Best Practices:
1. Always check role before allowing sensitive operations
2. Log all access attempts for audit trail
3. Frontend should implement role-based UI rendering
4. Regular security audits of API endpoints

## Monitoring & Logging

All operations log the following:
- User creation with status assignment
- Role-based filtering applications
- Current user role when accessing endpoints

Check logs for:
```
"Setting default status to Active for new user"
"Fetching all organisations - page: X, size: Y"
"Business User accessing own data only"
```

## Future Enhancements

### Planned:
- [ ] Add role-based filtering to other entities (Customers, Documents)
- [ ] Implement field-level security (hide sensitive fields based on role)
- [ ] Add audit trail for all data access
- [ ] Implement data export restrictions for Business Users

### Considerations:
- Multi-tenancy support for Business Users within same organisation
- Dynamic permission assignment without code changes
- Role hierarchy (e.g., Manager inherits Business User permissions)

## Support

For questions or issues with role-based access control:
1. Check logs for security-related messages
2. Verify role assignments in database
3. Test with different user roles
4. Review SecurityUtil implementation

---

**Date**: April 12, 2026  
**Version**: 2.2.0  
**Author**: GitHub Copilot  
**Status**: ✅ Implemented and Tested
