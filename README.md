# User Management API

A Spring Boot REST API for user management with role-based authentication and authorization.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2.0 with Java 21
- **Database**: Cloud SQL MySQL 8.0 (GCP) / H2 (Local Development)  
- **Authentication**: JWT-based with role-based access control
- **Deployment**: Cloud Run (GCP) with built-in Cloud SQL connector
- **Infrastructure**: Managed via [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc) Terraform repository

## 🚀 Deployment Status
- **NPE Environment**: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
- **Database**: Cloud SQL MySQL 8.0 via built-in connector  
- **CI/CD**: GitHub Actions (Automated Deployment)
- **Profile**: `npe`

## Features
- JWT-based authentication
- Role-Based Access Control (RBAC)
- User CRUD operations
- Account locking after 5 failed login attempts
- Password encryption with BCrypt
- Session timeout (15 minutes)
- MySQL Cloud SQL (Production) / H2 in-memory (Development)

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
