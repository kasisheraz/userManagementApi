# User Management API - FinCore Platform

## Overview
Spring Boot 3.2 REST API for user authentication and management with JWT-based security.

## Features
- JWT-based authentication
- Role-Based Access Control (RBAC)
- User CRUD operations
- Account locking after 5 failed login attempts
- Password encryption with BCrypt
- Session timeout (15 minutes)
- H2 in-memory database

## Requirements
- Java 21
- Maven 3.8+

## Setup
```bash
mvn clean install
mvn spring-boot:run
```

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (leave empty)

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

### User Management
*   **`GET /api/users`**: Get all users (ADMIN).
*   **`GET /api/users/{id}`**: Get a user by their ID (ADMIN).
*   **`POST /api/users`**: Create a new user.
*   **`PUT /api/users/{id}`**: Update an existing user (ADMIN).
*   **`DELETE /api/users/{id}`**: Delete a user (ADMIN).

### Health Check
*   **`GET /actuator/health`**: Check the health status of the application.

## Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123456 | ADMIN |
| compliance | Compliance@123 | COMPLIANCE_OFFICER |
| staff | Staff@123456 | OPERATIONAL_STAFF |

## Testing with `test.http`
You can use the `test.http` file with a REST client extension (like the one for VS Code) to test the API.

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
- Lombok
- MapStruct
- Maven

## Quick Start
```bash
# Run with H2 in-memory database
mvn spring-boot:run

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
