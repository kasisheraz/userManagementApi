# Testing Guide - User Management API

## Quick Start

### 1. Run the Application
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

The application will start on: **http://localhost:8080**

### 2. Access H2 Console (Optional)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (leave empty)

## Test with cURL

### Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"Admin@123456\"}"
```

### Login as Compliance Officer
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"compliance\",\"password\":\"Compliance@123\"}"
```

### Login as Staff
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"staff\",\"password\":\"Staff@123456\"}"
```

## Test Users

| Username   | Password        | Role                  |
|------------|-----------------|----------------------|
| admin      | Admin@123456    | SYSTEM_ADMINISTRATOR |
| compliance | Compliance@123  | COMPLIANCE_OFFICER   |
| staff      | Staff@123456    | OPERATIONAL_STAFF    |

## Expected Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "SYSTEM_ADMINISTRATOR"
}
```

## Run Tests
```bash
mvn test
```

Expected: **30 tests passing**
