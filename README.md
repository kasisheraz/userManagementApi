# User Management API - FinCore Platform

## Overview
Spring Boot 3.2 REST API for user authentication and management with JWT-based security.

## Features
- JWT-based authentication
- Role-Based Access Control (RBAC)
- Account locking after 5 failed login attempts
- Password encryption with BCrypt
- Session timeout (15 minutes)
- MariaDB database integration

## Requirements
- Java 21
- Maven 3.8+
- MariaDB 10.6+

## Setup

### Option 1: H2 In-Memory Database (Recommended for Testing)
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (leave empty)

### Option 2: MariaDB Database (Production)

1. Create database:
```sql
CREATE DATABASE fincore_db;
```

2. Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/fincore_db
    username: your_username
    password: your_password
```

3. Run:
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123456"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "SYSTEM_ADMINISTRATOR"
}
```

### Protected Endpoints
Include JWT token in Authorization header:
```http
Authorization: Bearer <token>
```

## Security Features

### Password Policy
- Minimum 12 characters
- Complexity requirements enforced
- BCrypt hashing

### Account Locking
- Locks after 5 failed login attempts
- Lock duration: 30 minutes
- Automatic unlock after duration

### Session Management
- Stateless JWT tokens
- Token expiration: 15 minutes
- No server-side session storage

## Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123456 | SYSTEM_ADMINISTRATOR |
| compliance | Compliance@123 | COMPLIANCE_OFFICER |
| staff | Staff@123456 | OPERATIONAL_STAFF |

## Testing with Postman

### Import Collection
1. Open Postman
2. Click Import
3. Select `postman_collection.json`
4. Select `postman_environment.json`
5. Set environment to "FinCore Local Environment"

### Test Scenarios

1. **Successful Login**
   - Use "Login - Admin" request
   - Token is automatically saved to environment

2. **Invalid Credentials**
   - Use "Login - Invalid Credentials" request
   - Should return 401 Unauthorized

3. **Account Locking**
   - Run "Login - Test Account Lock" 5 times
   - 6th attempt should return account locked error

4. **Protected Endpoints**
   - First login to get token
   - Use "Test Protected Endpoint" with saved token

## Project Structure
```
src/main/java/com/fincore/usermgmt/
├── config/          # Security configuration
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── repository/     # Data repositories
├── security/       # JWT and security components
└── service/        # Business logic
```

## Technologies
- Spring Boot 3.2
- Spring Security 6
- Spring Data JPA
- JWT (jjwt 0.12.3)
- H2 Database (in-memory)
- MariaDB (production)
- Lombok
- Maven

## Quick Start
```bash
# Run with H2 in-memory database
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'
```

## Running Tests

### Run all tests
```bash
mvn test
```

### Run specific test
```bash
mvn test -Dtest=AuthServiceTest
```

### Test Coverage
- Unit Tests: JwtUtil, AuthService, AuthController, UserRepository
- Integration Tests: Complete authentication flow
- See TEST_EXECUTION.md for detailed guide
