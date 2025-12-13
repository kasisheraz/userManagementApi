# Project Context: User Management API

## Project Overview

This project is a User Management API built with Java and Spring Boot. Its primary function is to authenticate users based on credentials stored in a relational database and issue a JSON Web Token (JWT) upon successful authentication.

The application is designed as a microservice focused solely on authentication.

## Technology Stack

*   **Java Version**: Java 21
*   **Framework**: Spring Boot 3.2
*   **Security**: Spring Security
*   **Data Access**: Spring Data JPA
*   **Database Support**:
    *   MariaDB (Default)
    *   MySQL
    *   H2 (For testing/development)
*   **JWT Library**: JJWT

## API Endpoints

The application exposes the following REST endpoints:

### Authentication

*   **`POST /api/auth/login`**
    *   **Request Body**: `LoginRequest` object containing `username` and `password`.
    *   **Success Response**: `LoginResponse` object containing a `token` (JWT).
    *   **Error Response**: HTTP status codes indicating authentication failure (e.g., 401 Unauthorized).

### User Management

*   **`GET /api/users`**: Get all users.
*   **`GET /api/users/{id}`**: Get a user by their ID.
*   **`POST /api/users`**: Create a new user.
*   **`PUT /api/users/{id}`**: Update an existing user.
*   **`DELETE /api/users/{id}`**: Delete a user.

## Core Functionality

### Authentication Logic

The core authentication logic is handled by the `AuthService`. The process is as follows:

1.  The service receives a username and password.
2.  It retrieves the user from the database.
3.  It validates the provided password against the stored hashed password.
4.  Upon successful validation, it generates a JWT.

### User Management

The `UserService` handles the business logic for user management. This includes creating, retrieving, updating, and deleting users.

### Security Features

*   **Account Locking**: To prevent brute-force attacks, the application implements an account locking mechanism.
    *   An account is locked after **5 failed login attempts**.
    *   The lock is active for **30 minutes**.
    *   This is configurable in `application.yml`.
*   **JWT**:
    *   A JWT is generated on successful login.
    *   The token has an expiration time of **15 minutes**.
    *   This is configurable in `application.yml`.
*   **Authorization**:
    *   The user management endpoints (`/api/users/**`) are protected and require the `ADMIN` authority.
    *   The user creation endpoint (`POST /api/users`) is public.


## Key Files

*   `pom.xml`: Defines all project dependencies, including Spring Boot starters, security libraries, and database drivers.
*   `src/main/resources/application.yml`: Main application configuration file. It contains settings for the database connection, JWT secret, and security policies (account locking, token expiration).
*   `src/main/java/com/fincore/usermgmt/controller/AuthController.java`: The Spring MVC controller that defines the `/api/auth/login` endpoint.
*   `src/main/java/com/fincore/usermgmt/service/AuthService.java`: Contains the core business logic for user authentication, password validation, and the account locking mechanism.
*   `src/main/java/com/fincore/usermgmt/entity/User.java`: The JPA entity representing the `User` data model.
*   `src/main/java/com/fincore/usermgmt/config/SecurityConfig.java`: Configures Spring Security, including password encoding and JWT filter integration.
*   `src/main/java/com/fincore/usermgmt/security/JwtUtil.java`: Utility class for generating and validating JWTs.

## Database

The application uses Spring Data JPA for database interaction and is configured to work with MariaDB, MySQL, and the in-memory H2 database.

*   The default configuration in `application.yml` is for **MariaDB**.
*   Alternative configurations for MySQL and H2 are provided in `application-mysql.yml` and `application-h2.yml`.
*   The database schema and initial data can be found in `src/main/resources/data.sql`.
