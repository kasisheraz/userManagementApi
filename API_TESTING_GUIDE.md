# API Testing Guide - Swagger UI & Postman

**Date:** 2026-03-30  
**Status:** ✅ Ready for Testing

---

## 📋 SUMMARY OF CHANGES

### 1. **Schema Alignment**
- ✅ User entity: `residentialAddressIdentifier` and `postalAddressIdentifier` changed from `Integer` to `Long`
- ✅ All User DTOs updated to use `Long` for address identifiers
- ✅ Database migration V5.0 created to update column types and add foreign keys

### 2. **Swagger Documentation Enhanced**
- ✅ Added comprehensive `@Schema` annotations to **UserCreateDTO**
- ✅ Added comprehensive `@Schema` annotations to **UserUpdateDTO**  
- ✅ Added comprehensive `@Schema` annotations to **UserDTO**
- ✅ All fields now have descriptions, examples, and data type specifications
- ✅ Address identifier fields properly documented as `int64` (Long)

### 3. **Postman Collections Updated**
- ✅ **postman_collection.json** - Create User request includes address identifiers
- ✅ **postman_collection.json** - Update User request includes address identifiers
- ✅ All address identifier fields set to `null` as default examples

---

## 🚀 HOW TO TEST WITH SWAGGER UI

### Step 1: Start the Application

```powershell
# Configure Java 17
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Start application
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=local-h2 --server.port=8081
```

Wait for: `Started UserManagementApiApplication in X.XXX seconds`

### Step 2: Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8081/swagger-ui/index.html
```

**Note:** If you get 403 Forbidden, restore the Swagger fix from stash:
```bash
git stash pop
```

### Step 3: Authenticate

1. **Click** on "Authorize" button (top right, lock icon)
2. **Get OTP:**
   - Use endpoint: `POST /api/auth/request-otp`
   - Request body:
     ```json
     {
       "phoneNumber": "+1234567890"
     }
     ```
   - Check console logs for OTP code (dev mode)

3. **Login:**
   - Use endpoint: `POST /api/auth/login`
   - Request body:
     ```json
     {
       "phoneNumber": "+1234567890",
       "otp": "123456"
     }
     ```
   - Copy the JWT token from response

4. **Set Authorization:**
   - Paste token in "Value" field
   - Click "Authorize"
   - Click "Close"

### Step 4: Test User APIs with Address Identifiers

#### **Create User with Addresses**

1. Expand `POST /api/users` endpoint
2. Click "Try it out"
3. Use this sample request body:

```json
{
  "phoneNumber": "+1555000001",
  "email": "testuser@example.com",
  "firstName": "John",
  "middleName": "Michael",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "residentialAddressIdentifier": null,
  "postalAddressIdentifier": null,
  "statusDescription": "ACTIVE",
  "role": "OPERATIONAL_STAFF"
}
```

**With Valid Addresses:**
```json
{
  "phoneNumber": "+1555000002",
  "email": "testuser2@example.com",
  "firstName": "Jane",
  "middleName": "Elizabeth",
  "lastName": "Smith",
  "dateOfBirth": "1992-03-20",
  "residentialAddressIdentifier": 1,
  "postalAddressIdentifier": 2,
  "statusDescription": "ACTIVE",
  "role": "OPERATIONAL_STAFF"
}
```

4. Click "Execute"
5. Verify response shows:
   - `201 Created` status
   - User object with `residentialAddressIdentifier` and `postalAddressIdentifier` as Long values

#### **Update User with Address Changes**

1. Expand `PUT /api/users/{id}` endpoint
2. Click "Try it out"
3. Enter user ID (e.g., `1`)
4. Use request body:

```json
{
  "firstName": "Updated",
  "lastName": "Name",
  "email": "updated@example.com",
  "residentialAddressIdentifier": 3,
  "postalAddressIdentifier": null
}
```

5. Click "Execute"
6. Verify `200 OK` with updated user data

#### **Get User by ID**

1. Expand `GET /api/users/{id}` endpoint
2. Click "Try it out"
3. Enter user ID
4. Click "Execute"
5. Verify response shows both address identifiers as Long types

---

## 📮 TESTING WITH POSTMAN

### Import Collections

1. Open Postman
2. Import these files:
   - `postman_collection.json` (Phase 1 & 2 APIs)
   - `postman_environment.json` (Local environment)
   - `postman_environment_cloud.json` (Cloud environment)

### Setup Environment

1. Select environment: **FinCore User Management - Local**
2. Verify variables:
   - `base_url`: `http://localhost:8081`
   - `authToken`: (will be set automatically)

### Test Sequence

#### 1. Health Check
- Run: **Health Check → Health Endpoint**
- Expected: `200 OK` with `{"status":"UP"}`

#### 2. Authentication
- Run: **Authentication → 1. Request OTP - Admin**
  - Check console logs for OTP
  - OTP automatically saved to `{{otpCode}}`
- Run: **Authentication → 2. Login with OTP - Admin**
  - JWT token automatically saved to `{{authToken}}`

#### 3. Create User with Addresses
- Run: **User Management → Create User**
- Verify:
  - `201 Created` status
  - Response includes `residentialAddressIdentifier` and `postalAddressIdentifier` (both null in example)

#### 4. Update User with Address
- Run: **User Management → Update User**
- Verify:
  - `200 OK` status
  - Address identifiers updated if provided

#### 5. Get User by ID
- Run: **User Management → Get User by ID**
- Verify:
  - `200 OK` status
  - User object shows Long values for address identifiers

---

## 🔍 FIELD VALIDATION TESTS

### Test Cases for Address Identifiers

| Test Case | `residentialAddressIdentifier` | `postalAddressIdentifier` | Expected Result |
|-----------|-------------------------------|---------------------------|-----------------|
| Null values | `null` | `null` | ✅ Success - valid |
| Valid IDs | `1` | `2` | ✅ Success - FK verified |
| One null | `1` | `null` | ✅ Success - partial address |
| Invalid ID | `9999` | `null` | ❌ Error - FK constraint violation |
| Negative | `-1` | `null` | ❌ Error - invalid ID |
| String (wrong type) | `"abc"` | `null` | ❌ Error - type mismatch |

### Swagger UI Testing Script

1. **Test null addresses:**
   ```json
   {
     "phoneNumber": "+1555111111",
     "email": "test1@example.com",
     "firstName": "Test",
     "lastName": "One",
     "residentialAddressIdentifier": null,
     "postalAddressIdentifier": null,
     "statusDescription": "ACTIVE"
   }
   ```
   Expected: ✅ `201 Created`

2. **Test valid address IDs:**
   ```json
   {
     "phoneNumber": "+1555222222",
     "email": "test2@example.com",
     "firstName": "Test",
     "lastName": "Two",
     "residentialAddressIdentifier": 1,
     "postalAddressIdentifier": 2,
     "statusDescription": "ACTIVE"
   }
   ```
   Expected: ✅ `201 Created`

3. **Test invalid address ID (FK violation):**
   ```json
   {
     "phoneNumber": "+1555333333",
     "email": "test3@example.com",
     "firstName": "Test",
     "lastName": "Three",
     "residentialAddressIdentifier": 99999,
     "postalAddressIdentifier": null,
     "statusDescription": "ACTIVE"
   }
   ```
   Expected: ❌ `500 Internal Server Error` or `400 Bad Request` (after V5.0 migration applies FK constraints)

---

## 📊 NEW SWAGGER FIELD DOCUMENTATION

All User DTO fields now include:

### UserCreateDTO
- **phoneNumber**: User's phone number (unique) - `+1234567890`
- **email**: User's email address - `user@example.com`
- **firstName**: User's first name - `John`
- **middleName**: User's middle name - `Michael`
- **lastName**: User's last name - `Doe`
- **dateOfBirth**: User's date of birth - `1990-01-15`
- **residentialAddressIdentifier**: ID of the user's residential address (FK to Address table) - Type: `int64` (Long)
- **postalAddressIdentifier**: ID of the user's postal address (FK to Address table) - Type: `int64` (Long)
- **statusDescription**: User status - `ACTIVE`, `INACTIVE`, `PENDING`, `SUSPENDED`
- **role**: User role name - `OPERATIONAL_STAFF`

### UserUpdateDTO
Same fields as CreateDTO, but all optional (no @NotBlank)

### UserDTO (Response)
All fields from CreateDTO plus:
- **id**: Unique identifier (READ_ONLY)
- **createdDatetime**: Timestamp when created (READ_ONLY)
- **lastModifiedDatetime**: Timestamp when last modified (READ_ONLY)

---

## 🗄️ DATABASE MIGRATION V5.0

When the application starts, Flyway will automatically apply:

```sql
-- Change column types from INT to BIGINT
ALTER TABLE Users 
    MODIFY COLUMN Residential_Address_Identifier BIGINT,
    MODIFY COLUMN Postal_Address_Identifier BIGINT;

-- Add foreign key constraints
ALTER TABLE Users ADD CONSTRAINT fk_add1_id 
FOREIGN KEY (Residential_Address_Identifier) 
REFERENCES Address(Address_Identifier) ON DELETE SET NULL;

ALTER TABLE Users ADD CONSTRAINT fk_add2_id 
FOREIGN KEY (Postal_Address_Identifier) 
REFERENCES Address(Address_Identifier) ON DELETE SET NULL;
```

**Impact:**
- ✅ Data type changed to support larger ID ranges
- ✅ Foreign key constraints ensure referential integrity
- ✅ ON DELETE SET NULL prevents orphaned records
- ✅ Invalid address IDs will be rejected

---

## ✅ VERIFICATION CHECKLIST

### Before Starting Application
- [x] Java 17 configured
- [x] Code compiled successfully
- [x] Migration V5.0 exists in `src/main/resources/db/migration/`

### After Starting Application
- [ ] Application starts without errors
- [ ] Flyway migration V5.0 applied successfully (check logs)
- [ ] Swagger UI accessible at `http://localhost:8081/swagger-ui/index.html`
- [ ] OpenAPI spec shows Long type for address identifiers
- [ ] Field descriptions visible in Swagger UI

### API Testing
- [ ] Can create user with null address identifiers
- [ ] Can create user with valid address IDs (1, 2, 3)
- [ ] Cannot create user with invalid address ID (FK violation)
- [ ] Can update user's address identifiers
- [ ] Can set address identifier to null (removes association)
- [ ] Get user response shows Long type for addresses

### Postman Testing
- [ ] All authentication flows work
- [ ] User CRUD operations successful
- [ ] Address identifiers properly sent/received as Long
- [ ] Tests pass without type errors

---

## 🔧 TROUBLESHOOTING

### Issue: Swagger UI Returns 403 Forbidden

**Solution:**
```bash
# Restore SecurityConfig.java from stash
git stash pop

# Rebuild
mvn compile -Dmaven.test.skip=true

# Restart application
```

### Issue: TypeError - Cannot convert Integer to Long

**Cause:** Old JAR still running with Integer types

**Solution:**
1. Stop running application (Ctrl+C)
2. Delete old JAR (if locked, use Task Manager)
3. Rebuild: `mvn package -Dmaven.test.skip=true`
4. Restart application

### Issue: FK Constraint Violation

**Cause:** Trying to use address ID that doesn't exist

**Solution:**
1. Set address identifiers to `null`, OR
2. Create addresses first using Address API, OR
3. Use existing address IDs (1, 2, 3 from seed data)

### Issue: Swagger Shows Integer Instead of Long

**Cause:** Browser cache or old API spec

**Solution:**
1. Hard refresh browser (Ctrl+Shift+R)
2. Clear browser cache
3. Check OpenAPI JSON: `http://localhost:8081/v3/api-docs`
4. Verify `format: "int64"` is present

---

## 📁 FILES MODIFIED

### Code Changes (7 files)
1. `src/main/java/com/fincore/usermgmt/entity/User.java`
2. `src/main/java/com/fincore/usermgmt/dto/UserCreateDTO.java` (+ @Schema annotations)
3. `src/main/java/com/fincore/usermgmt/dto/UserUpdateDTO.java` (+ @Schema annotations)
4. `src/main/java/com/fincore/usermgmt/dto/UserDTO.java` (+ @Schema annotations)
5. `src/main/resources/db/migration/V5.0__Fix_User_Address_Foreign_Keys.sql` (NEW)

### Postman Changes (1 file)
1. `postman_collection.json` (Create User and Update User requests)

### Documentation (3 files)
1. `DATA_MODEL_ANALYSIS.md`
2. `SCHEMA_ALIGNMENT_REPORT.md`
3. `API_TESTING_GUIDE.md` (this file)

---

## 🎯 NEXT STEPS

1. ✅ **Test locally with H2**
   - Start application
   - Verify V5.0 migration applies
   - Test User APIs with Swagger UI

2. ✅ **Run Postman collection**
   - Import latest `postman_collection.json`
   - Execute full test suite
   - Verify all tests pass

3. ✅ **Test on MySQL** (optional)
   - Update `application-local.yml` with MySQL connection
   - Start with profile: `--spring.profiles.active=local`
   - Verify FK constraints work on MySQL

4. ✅ **Commit changes**
   ```bash
   git add .
   git commit -m "feat: Add Swagger documentation and update User address identifiers

   - Add @Schema annotations to User DTOs for complete API documentation
   - Update Postman collections with address identifier examples
   - Update User entity to use Long for address identifiers
   - Create V5.0 migration for FK constraints and data type changes
   - All address identifiers now properly typed as int64 (Long)
   "
   ```

5. ✅ **Deploy to NPE environment**
   - Follow existing deployment process
   - Monitor Flyway migration logs
   - Run smoke tests

---

## 💡 TIPS

- **Use Swagger UI for quick API exploration** - all fields documented with examples
- **Use Postman for automated testing** - full test suite with assertions
- **Check OpenAPI JSON directly** - `http://localhost:8081/v3/api-docs` for raw spec
- **Export Postman test results** - for documentation and CI/CD integration

---

## 📞 SUPPORT

If you encounter issues:
1. Check application logs for errors
2. Verify Java 17 is configured
3. Ensure H2 database is not locked
4. Review Flyway migration logs
5. Check Swagger UI field types match documentation

**Build Status:** ✅ SUCCESS  
**Migration Status:** ⏳ Ready to apply on next startup  
**API Documentation:** ✅ Complete with @Schema annotations  
**Postman Collections:** ✅ Updated

