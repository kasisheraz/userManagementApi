# Backend Security Tests - Postman Guide

## Overview
The `postman_security_tests.json` collection validates that protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) cannot be created, modified, or deleted via the API.

## Quick Start

### 1. Import Collection
```bash
# In Postman:
File → Import → Select postman_security_tests.json
```

### 2. Set Base URL Variable
Either:
- **Import existing environment**: `postman_environment.json` (already has `base_url`)
- **Or create new environment**:
  - Name: `Local`
  - Variable: `base_url` = `http://localhost:8080`

### 3. Run Tests
Click **"Run Collection"** button in Postman and select all tests.

**Or run sequentially**:
1. **Folder 1**: Authenticate First
2. **Folder 2**: Create User - Role Security (5 tests)
3. **Folder 3**: Update User - Protected Role Security (2 tests)  
4. **Folder 4**: Delete User - Protected Role Security (2 tests)
5. **Folder 5**: Get Users - Admin Filtering (1 test)

**Total**: 11 security validation tests

---

## Test Structure

### 1. Authenticate First 
Obtains JWT token for subsequent requests.
- Request OTP (auto-saves OTP to variable)
- Verify OTP (auto-saves token to variable)

### 2. Create User - Role Security
Tests that protected roles cannot be created:
- ✅ Create USER role → Should work
- ✅ Create MANAGER role → Should work
- ❌ Attempt ADMIN role → Should default to USER
- ❌ Attempt SUPER_ADMIN role → Should default to USER
- ❌ Attempt SYSTEM_ADMINISTRATOR role → Should default to USER

### 3. Update User - Protected Role Security
Tests that protected users cannot be modified:
- ✅ Update regular user → Should work
- ❌ Attempt to update admin user (ID 1) → Should return 403

### 4. Delete User - Protected Role Security
Tests that protected users cannot be deleted:
- ✅ Delete regular user → Should work
- ❌ Attempt to delete admin user (ID 1) → Should return 403

### 5. Get Users - Admin Filtering
Tests that protected users are filtered from list:
- Verify no ADMIN, SUPER_ADMIN, or SYSTEM_ADMINISTRATOR in response

---

## Expected Results

### All Tests Passing:
```
Tests:        11/11 ✅
Passed:       11
Failed:       0
Duration:     ~10-15 seconds
```

### Test Breakdown:
| Test Category | Tests | Expected |
|---------------|-------|----------|
| Authentication | 2 | All pass ✅ |
| Create Role Security | 5 | All pass ✅ |
| Update Protection | 2 | All pass ✅ |
| Delete Protection | 2 | All pass ✅ |
| Admin Filtering | 1 | Pass ✅ |

---

## Test Assertions

Each test includes automated assertions:

**Example: Attempt ADMIN Role**
```javascript
pm.test('Status is 201 Created', function() {
    pm.response.to.have.status(201);
});

pm.test('Role is USER (NOT ADMIN)', function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.role).to.eql('USER');
    pm.expect(jsonData.role).to.not.eql('ADMIN');
});
```

**Example: Attempt Delete Admin**
```javascript
pm.test('Status is 403 Forbidden', function() {
    pm.response.to.have.status(403);
});

pm.test('Error message mentions protected roles', function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include('protected roles');
});
```

---

## Troubleshooting

### Issue: 403 on all requests
**Cause**: No authentication token
**Solution**: Run "1. Authenticate First" folder first

### Issue: 404 on user operations
**Cause**: Backend not running
**Solution**: Start backend with `mvn spring-boot:run`

### Issue: OTP not auto-saving
**Cause**: Using production environment
**Solution**: Use test/local environment where `devOtp` is returned

---

## Integration with Existing Collections

This security collection complements your existing collections:
- **postman_collection.json**: Phase 1 + Phase 2 functional tests
- **phase2-postman-collection.json**: Phase 2 organization/KYC tests
- **postman_security_tests.json**: Security validation tests (this collection)

Run all three for complete API validation.

---

## Command Line Execution (Optional)

Using Newman CLI:
```bash
# Install Newman
npm install -g newman

# Run security tests
newman run postman_security_tests.json -e postman_environment.json

# Generate HTML report
newman run postman_security_tests.json \
  -e postman_environment.json \
  -r htmlextra \
  --reporter-htmlextra-export security-test-report.html
```

---

## Success Criteria

✅ Protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) cannot be created  
✅ Attempts to create protected roles default to USER  
✅ Protected users cannot be updated (403 Forbidden)  
✅ Protected users cannot be deleted (403 Forbidden)  
✅ Protected users are filtered from GET /users response  
✅ Regular users (USER, MANAGER) can be created/updated/deleted normally  

**All 11 tests must pass for security implementation to be validated.**

---

## Next Steps

1. ✅ Import `postman_security_tests.json`
2. ✅ Set `base_url` environment variable  
3. ✅ Run collection and verify all 11 tests pass
4. ✅ Document results
5. ✅ Include test evidence in PR

If all tests pass → **Backend security implementation validated** ✅
