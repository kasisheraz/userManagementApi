# Test Execution Guide

## Running All Tests

### Run all tests
```bash
mvn test
```

### Run tests with coverage
```bash
mvn clean test jacoco:report
```

### Run specific test class
```bash
mvn test -Dtest=AuthServiceTest
```

### Run specific test method
```bash
mvn test -Dtest=AuthServiceTest#login_WithValidCredentials_ShouldReturnToken
```

## Test Structure

### Unit Tests
Located in `src/test/java/com/fincore/usermgmt/`

1. **JwtUtilTest** - JWT token generation and validation
   - Token generation
   - Username extraction
   - Token validation
   - Expiration handling

2. **AuthServiceTest** - Authentication business logic
   - Valid login
   - Invalid credentials
   - Account locking after 5 attempts
   - Failed attempt counter
   - Locked account handling
   - Inactive user handling

3. **AuthControllerTest** - REST API endpoints
   - Login endpoint validation
   - Request validation
   - Error responses
   - HTTP status codes

4. **UserRepositoryTest** - Database operations
   - Find by username
   - Check username exists
   - Check email exists
   - Save user

### Integration Tests

**AuthIntegrationTest** - End-to-end authentication flow
- Complete login flow with database
- Account locking mechanism
- Failed attempt tracking
- Multiple user roles
- Token generation

## Test Coverage

### Expected Coverage
- **Service Layer**: 90%+
- **Controller Layer**: 85%+
- **Repository Layer**: 80%+
- **Security Components**: 85%+

## Test Scenarios Covered

### Authentication Tests
- ✅ Successful login with valid credentials
- ✅ Failed login with invalid username
- ✅ Failed login with invalid password
- ✅ Account locks after 5 failed attempts
- ✅ Locked account cannot login
- ✅ Failed attempt counter increments
- ✅ Failed attempt counter resets on success
- ✅ Inactive user cannot login
- ✅ JWT token generation
- ✅ JWT token validation
- ✅ All three user roles can login

### Security Tests
- ✅ Password encryption (BCrypt)
- ✅ Account locking duration (30 minutes)
- ✅ Session timeout configuration
- ✅ Token expiration (15 minutes)

### Repository Tests
- ✅ Find user by username
- ✅ Check username uniqueness
- ✅ Check email uniqueness
- ✅ User persistence
- ✅ Timestamp auto-generation

## Running Tests in IDE

### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run 'TestName'"
3. View results in Run window

### Eclipse
1. Right-click on test class or method
2. Select "Run As" > "JUnit Test"
3. View results in JUnit view

### VS Code
1. Install Java Test Runner extension
2. Click play button next to test method
3. View results in Test Explorer

## Continuous Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
```

## Test Data

### Default Test Users
| Username | Password | Role | Status |
|----------|----------|------|--------|
| admin | Admin@123456 | SYSTEM_ADMINISTRATOR | ACTIVE |
| compliance | Compliance@123 | COMPLIANCE_OFFICER | ACTIVE |
| staff | Staff@123456 | OPERATIONAL_STAFF | ACTIVE |

### Test Database
- **Type**: H2 in-memory
- **URL**: jdbc:h2:mem:testdb
- **Auto-reset**: Yes (before each test)

## Troubleshooting

### Tests fail with "Connection refused"
- Ensure no other instance is running on port 8080
- Check H2 database configuration

### Tests fail with "User not found"
- Verify data.sql is being executed
- Check test profile is active

### Tests fail randomly
- Check for test isolation issues
- Verify @BeforeEach setup methods
- Ensure tests don't depend on execution order

## Best Practices

1. **Test Isolation**: Each test should be independent
2. **Clear Names**: Use descriptive test method names
3. **Arrange-Act-Assert**: Follow AAA pattern
4. **Mock External Dependencies**: Use @MockBean for external services
5. **Test Edge Cases**: Include boundary conditions
6. **Clean Up**: Reset state in @BeforeEach/@AfterEach

## Next Steps

1. Add more integration tests for future endpoints
2. Implement performance tests
3. Add security penetration tests
4. Set up mutation testing
5. Configure code coverage thresholds
