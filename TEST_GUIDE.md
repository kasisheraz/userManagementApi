# Testing Guide - User Management API

## Quick Start

### 1. Start Application with H2 Database
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### 2. Verify Application is Running
Open browser: http://localhost:8080/h2-console

## Testing with Postman

### Setup
1. Import `postman_collection.json` into Postman
2. Import `postman_environment.json` into Postman
3. Select "FinCore Local Environment" from environment dropdown

### Test Cases

#### TC-01: Successful Login (Admin)
**Request:** Login - Admin
**Expected Result:**
- Status: 200 OK
- Response contains: token, username, fullName, role
- Token automatically saved to environment variable

**Sample Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "SYSTEM_ADMINISTRATOR"
}
```

#### TC-02: Successful Login (Compliance Officer)
**Request:** Login - Compliance Officer
**Credentials:** compliance / Compliance@123
**Expected Result:** 200 OK with token

#### TC-03: Successful Login (Operational Staff)
**Request:** Login - Operational Staff
**Credentials:** staff / Staff@123456
**Expected Result:** 200 OK with token

#### TC-04: Invalid Credentials
**Request:** Login - Invalid Credentials
**Expected Result:**
- Status: 401 Unauthorized
- Error message: "Invalid credentials"

#### TC-05: Account Locking Mechanism
**Steps:**
1. Run "Login - Test Account Lock" request 5 times with wrong password
2. On 6th attempt, account should be locked

**Expected Results:**
- Attempts 1-4: 401 Unauthorized with "Invalid credentials"
- Attempt 5: 401 Unauthorized with "Invalid credentials"
- Attempt 6+: 401 Unauthorized with "Account is locked"

**Note:** Account unlocks automatically after 30 minutes (1800 seconds)

#### TC-06: JWT Token Expiration
**Steps:**
1. Login successfully and save token
2. Wait 15 minutes (900 seconds)
3. Try to access protected endpoint

**Expected Result:** 401 Unauthorized (token expired)

#### TC-07: Access Protected Endpoint with Valid Token
**Steps:**
1. Login using "Login - Admin"
2. Use "Test Protected Endpoint" request

**Expected Result:** 200 OK (if endpoint exists) or 404 (endpoint not implemented yet)

## Testing with cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/test \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Database Verification

### Access H2 Console
1. Open: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:fincore_db`
3. Username: `sa`
4. Password: (empty)

### Verify Data
```sql
-- Check users
SELECT * FROM users;

-- Check roles
SELECT * FROM roles;

-- Check permissions
SELECT * FROM permissions;

-- Check role-permission mapping
SELECT r.name as role, p.name as permission 
FROM roles r 
JOIN role_permissions rp ON r.id = rp.role_id 
JOIN permissions p ON p.id = rp.permission_id;

-- Check failed login attempts
SELECT username, failed_login_attempts, locked_until, status 
FROM users;
```

## Security Testing Checklist

- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials fails
- [ ] Account locks after 5 failed attempts
- [ ] Locked account cannot login
- [ ] JWT token is generated on successful login
- [ ] JWT token expires after 15 minutes
- [ ] Protected endpoints require valid token
- [ ] Expired token is rejected
- [ ] Invalid token is rejected
- [ ] Password is stored as BCrypt hash (not plain text)

## Performance Testing

### Load Test Login Endpoint
```bash
# Using Apache Bench (if installed)
ab -n 100 -c 10 -p login.json -T application/json http://localhost:8080/api/auth/login
```

Where `login.json` contains:
```json
{"username":"admin","password":"Admin@123456"}
```

## Troubleshooting

### Application won't start
- Check Java version: `java -version` (should be 21)
- Check Maven version: `mvn -version` (should be 3.8+)
- Check port 8080 is not in use

### Login fails with 500 error
- Check H2 console to verify data is loaded
- Check application logs for stack trace

### Token not working
- Verify token is not expired (15 min lifetime)
- Check Authorization header format: `Bearer <token>`
- Ensure no extra spaces in header value
