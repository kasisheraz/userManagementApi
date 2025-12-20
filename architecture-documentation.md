# FinCore User Management API - Architecture Documentation

## 1. Executive Summary

The FinCore User Management API is a cloud-native microservice built on Google Cloud Platform (GCP), providing secure user authentication, role-based access control, and user management capabilities. The system leverages modern containerization, managed database services, and automated CI/CD pipelines to deliver high availability, scalability, and security.

## 2. System Architecture Overview

### 2.1 Architecture Principles
- **Cloud-Native**: Built for GCP using Cloud Run and Cloud SQL
- **Containerized**: Docker-based deployment for consistency and portability
- **Security-First**: JWT authentication, RBAC, encrypted connections
- **Infrastructure as Code**: Terraform-managed infrastructure via separate IaC repository
- **Automated CI/CD**: GitHub Actions for continuous deployment

### 2.2 High-Level Architecture

```
┌─────────────────────────────────────────┐
│         Client Applications             │
│  (Web, Mobile, API Consumers)           │
└─────────────────┬───────────────────────┘
                  │ HTTPS
                  │
┌─────────────────▼───────────────────────┐
│         Cloud Run Service               │
│    (fincore-npe-api)                    │
│  ┌────────────────────────────────┐     │
│  │  Spring Boot 3.2 + Java 21     │     │
│  │  - JWT Authentication          │     │
│  │  - RBAC Authorization          │     │
│  │  - User Management APIs        │     │
│  │  - Health Checks               │     │
│  └────────────────────────────────┘     │
└─────────────────┬───────────────────────┘
                  │ Built-in Socket Factory
                  │
┌─────────────────▼───────────────────────┐
│        Cloud SQL MySQL 8.0              │
│    (fincore-npe-db)                     │
│  ┌────────────────────────────────┐     │
│  │  Database: my_auth_db          │     │
│  │  User: fincore_app             │     │
│  │  - users table                 │     │
│  │  - roles table                 │     │
│  │  - permissions table           │     │
│  │  - role_permissions table      │     │
│  └────────────────────────────────┘     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│          CI/CD Pipeline                 │
│        (GitHub Actions)                 │
│  ┌────────────────────────────────┐     │
│  │  1. Build & Test               │     │
│  │  2. Build Docker Image         │     │
│  │  3. Push to GCR                │     │
│  │  4. Deploy to Cloud Run        │     │
│  │  5. Health Check & Smoke Tests │     │
│  └────────────────────────────────┘     │
└─────────────────────────────────────────┘
```

## 3. Core Components

### 3.1 Application Layer (Cloud Run)

#### Service Configuration
- **Service Name**: `fincore-npe-api`
- **Region**: `europe-west2`
- **Platform**: Cloud Run (fully managed)
- **Container**: Docker image hosted in Google Container Registry (GCR)
- **Resources**:
  - Memory: 1Gi
  - CPU: 1 vCPU
  - Timeout: 900s (15 minutes)
  - CPU Throttling: Enabled
  - Autoscaling: 0-3 instances

#### Application Stack
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21 (Temurin distribution)
- **Build Tool**: Maven 3.9+
- **Container Base**: Eclipse Temurin JRE 21-alpine

#### Key Features
- JWT-based authentication with configurable expiration
- Role-Based Access Control (RBAC) with 4 predefined roles
- Account lockout after 5 failed login attempts
- BCrypt password encryption
- Session timeout management
- RESTful API design
- Health check endpoints for monitoring

### 3.2 Database Layer (Cloud SQL)

#### Database Configuration
```yaml
Cloud SQL Instance:
  Name: fincore-npe-db
  Type: MySQL 8.0
  Region: europe-west2
  Connection: Built-in Cloud SQL Connector (Socket Factory)
  Private Network: fincore-npe-vpc
  Public IP: Enabled (with authorized networks)
  
Database:
  Name: my_auth_db
  User: fincore_app
  Connection Method: Built-in Socket Factory
  SSL: Enabled
  
Connection String:
  jdbc:mysql://google/my_auth_db?cloudSqlInstance=<INSTANCE>&socketFactory=com.google.cloud.sql.mysql.SocketFactory
```

#### Database Schema
#### Database Schema

**Core Tables**
```sql
users:
  - id (BIGINT, PK, AUTO_INCREMENT)
  - username (VARCHAR(255), UNIQUE)
  - password (VARCHAR(255), BCrypt hashed)
  - full_name (VARCHAR(255))
  - email (VARCHAR(255), UNIQUE)
  - phone_number (VARCHAR(255))
  - employee_id (VARCHAR(255))
  - department (VARCHAR(255))
  - job_title (VARCHAR(255))
  - status (ENUM: ACTIVE, INACTIVE, LOCKED)
  - role_id (BIGINT, FK to roles)
  - failed_login_attempts (INTEGER)
  - locked_until (DATETIME)
  - last_login_at (DATETIME)
  - created_at (DATETIME)
  - updated_at (DATETIME)

roles:
  - id (BIGINT, PK, AUTO_INCREMENT)
  - name (VARCHAR(255), UNIQUE)
  - description (VARCHAR(255))
  
permissions:
  - id (BIGINT, PK, AUTO_INCREMENT)
  - name (VARCHAR(255), UNIQUE)
  - description (VARCHAR(255))
  - module (VARCHAR(255))

role_permissions:
  - role_id (BIGINT, PK, FK to roles)
  - permission_id (BIGINT, PK, FK to permissions)
```

**Predefined Roles**
1. **SYSTEM_ADMINISTRATOR**: Full system access (all permissions)
2. **ADMIN**: User management and read permissions
3. **COMPLIANCE_OFFICER**: Compliance and read-only access
4. **OPERATIONAL_STAFF**: Limited operational access

**Default Permissions**
- `USER_READ`: Read user information
- `USER_WRITE`: Create and update users
- `CUSTOMER_READ`: Read customer information
- `CUSTOMER_WRITE`: Create and update customers

### 3.3 Infrastructure Layer

#### Cloud Infrastructure (Terraform Managed)
- **Infrastructure Repository**: [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc)
- **VPC**: Custom VPC with private subnet for Cloud SQL
- **Service Account**: Dedicated service account for Cloud Run with minimal required permissions
- **Secrets Management**: Cloud Secret Manager for JWT secrets
- **Container Registry**: Google Container Registry (GCR) for Docker images

#### Network Configuration
- Cloud SQL: Private VPC network + Public IP with authorized networks
- Cloud Run: Public endpoint (unauthenticated access for API)
- Cloud SQL Connector: Built-in socket factory (no Cloud SQL Proxy needed)

## 4. Security Architecture

### 4.1 Authentication & Authorization

#### JWT Authentication
```yaml
JWT Configuration:
  Algorithm: HS256
  Secret: Stored in Cloud Secret Manager
  Token Expiration: Configurable (default: 24 hours)
  Token Format: Bearer token in Authorization header
  Claims:
    - sub: username
    - roles: user roles array
    - iat: issued at timestamp
    - exp: expiration timestamp
```

#### Role-Based Access Control
```yaml
Access Control:
  - Endpoint-level authorization using @PreAuthorize
  - Method-level security with Spring Security
  - Role hierarchy enforcement
  - Permission-based granular access
```

### 4.2 Data Protection

#### Encryption
- **Database**: Cloud SQL encryption at rest (Google-managed keys)
- **Transit**: TLS 1.2+ for all connections
- **Passwords**: BCrypt with configurable strength (default: 10 rounds)
- **JWT Secret**: Stored securely in Cloud Secret Manager

#### Account Security
- **Account Lockout**: Automatic after 5 failed login attempts
- **Lockout Duration**: Configurable (default: 15 minutes)
- **Password Requirements**: Enforced via validation
- **Session Management**: JWT-based stateless sessions

### 4.3 Network Security

```yaml
Security Layers:
  Cloud Run:
    - HTTPS only (automatic SSL/TLS)
    - Ingress control (allow all for API)
    - Service-to-service authentication
    
  Cloud SQL:
    - Private IP for internal communication
    - Public IP with authorized networks
    - SSL/TLS enforcement
    - Built-in DDoS protection
```

## 5. CI/CD Pipeline

### 5.1 GitHub Actions Workflow

#### Pipeline Stages
```yaml
Build & Test:
  - Checkout source code
  - Set up JDK 21
  - Cache Maven dependencies
  - Build with Maven (skip tests currently)
  - Upload JAR artifact

Docker Build & Push:
  - Authenticate to GCP
  - Configure Docker for GCR
  - Build Docker image (multi-tag: latest, commit SHA)
  - Push to Container Registry
  
Deploy to Cloud Run:
  - Deploy latest image
  - Configure environment variables
  - Set Cloud SQL connection
  - Update service configuration
  - Wait for deployment completion
  
Health Check & Smoke Tests:
  - Wait for service readiness
  - Health endpoint check
  - Login API smoke test
  - Validate response format
```

#### Environment Variables
```yaml
Deployment Variables:
  - SPRING_PROFILES_ACTIVE: npe
  - DB_NAME: my_auth_db
  - DB_USER: fincore_app (from GitHub secret)
  - DB_PASSWORD: (from GitHub secret)
  - CLOUD_SQL_INSTANCE: (from GitHub secret)
```

### 5.2 Deployment Configuration

```yaml
Cloud Run Deployment:
  Image: gcr.io/PROJECT_ID/fincore-api:latest
  Region: europe-west2
  Platform: managed
  Access: allow-unauthenticated
  Service Account: fincore-github-actions
  Resources:
    Memory: 1Gi
    CPU: 1
    Timeout: 900s
  Scaling:
    Min instances: 0
    Max instances: 3
  Cloud SQL:
    - Instance connection via --add-cloudsql-instances flag
```

## 6. API Design & Endpoints

### 6.1 API Structure

#### Base URL
- **NPE Environment**: `https://fincore-npe-api-lfd6ooarra-nw.a.run.app`

#### Endpoints

**Authentication**
```http
POST /api/auth/login
Content-Type: application/json

Request:
{
  "username": "string",
  "password": "string"
}

Response (200 OK):
{
  "token": "api-key-{userId}-{timestamp}",
  "username": "string",
  "fullName": "string",
  "role": "string"
}
```

**User Management**
```http
GET    /api/users              # List all users (ADMIN)
GET    /api/users/{id}         # Get user by ID (ADMIN)
POST   /api/users              # Create user (requires auth)
PUT    /api/users/{id}         # Update user (ADMIN)
DELETE /api/users/{id}         # Delete user (ADMIN)
```

**Health & Monitoring**
```http
GET /actuator/health           # Service health status
```

### 6.2 Error Handling

```json
Standard Error Response:
{
  "message": "Error description",
  "status": 400
}
```

## 7. Monitoring & Observability

### 7.1 Health Checks
- **Endpoint**: `/actuator/health`
- **Checks**: Database connectivity, application status
- **Frequency**: Continuous via Cloud Run health checks
- **Format**: Spring Boot Actuator JSON format

### 7.2 Logging
```yaml
Logging Strategy:
  - Cloud Logging (Stackdriver) integration
  - Structured logging with Spring Boot
  - Log levels: DEBUG, INFO, WARN, ERROR
  - Request/response logging (configurable)
  - Database query logging (disabled in production)
```

### 7.3 Metrics & Alerts
- Cloud Run metrics (requests, latency, errors)
- Cloud SQL metrics (connections, queries, storage)
- Custom application metrics via Spring Boot Actuator
- Cloud Monitoring for alerting

## 8. Deployment Environments

### 8.1 Environment Strategy

```yaml
NPE (Non-Production Environment):
  Purpose: Testing and validation
  URL: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
  Profile: npe
  Database: Cloud SQL (fincore-npe-db)
  Scaling: 0-3 instances
  
Production (Future):
  Purpose: Live system
  Profile: production
  Database: Cloud SQL with HA
  Scaling: 1-10 instances (minimum 1)
  Backup: Automated daily backups
```

## 9. Infrastructure as Code

### 9.1 Terraform Management
- **Repository**: [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc)
- **Resources Managed**:
  - VPC and networking
  - Cloud SQL instance
  - Service accounts and IAM
  - Cloud Run initial setup
  - Secrets configuration

### 9.2 Configuration Files
```yaml
Repository Structure:
  - Dockerfile: Multi-stage build configuration
  - pom.xml: Maven dependencies and build
  - application-*.yml: Spring profiles
  - cloud-sql-schema.sql: Database schema
  - .github/workflows/: CI/CD pipelines
  - postman_collection.json: API tests
```

## 10. Scalability & Performance

### 10.1 Auto-Scaling
```yaml
Cloud Run Scaling:
  - Scales to zero when idle (cost optimization)
  - Automatic scale-up based on traffic
  - Concurrent requests per instance: 80 (default)
  - Cold start optimization: Startup CPU boost enabled
  
Cloud SQL Scaling:
  - Vertical scaling (machine type upgrade)
  - Read replicas (future enhancement)
  - Connection pooling (HikariCP)
```

### 10.2 Performance Optimization
- **Connection Pooling**: HikariCP with optimized settings
  - Maximum pool size: 3 (NPE), 20 (Production)
  - Minimum idle: 1 (NPE), 5 (Production)
  - Connection timeout: 60s (NPE), 30s (Production)
- **JPA Optimization**: Lazy loading, batch operations
- **Container Optimization**: Alpine base image, minimal layers
- **Build Optimization**: Maven dependency caching in CI/CD

## 11. Security Best Practices

### 11.1 Implemented Security Measures
- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing
- ✅ Account lockout mechanism
- ✅ Role-based access control
- ✅ HTTPS-only communication
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ Secrets management (Cloud Secret Manager)
- ✅ Dedicated service account with minimal permissions
- ✅ Database user with limited privileges (fincore_app)

### 11.2 Security Recommendations
- [ ] Enable Cloud Armor for DDoS protection
- [ ] Implement rate limiting at API level
- [ ] Add API key authentication for external consumers
- [ ] Enable Cloud SQL automatic backups
- [ ] Implement audit logging for all user actions
- [ ] Add password complexity requirements
- [ ] Implement password rotation policy
- [ ] Enable Cloud Run VPC ingress control

## 12. Cost Optimization

### 12.1 Current Cost Structure
```yaml
Cloud Run:
  - Pay-per-use pricing
  - Scales to zero (no idle cost)
  - $0.00002400 per vCPU-second
  - $0.00000250 per GiB-second
  
Cloud SQL:
  - MySQL instance cost (24/7)
  - Storage cost (per GB)
  - Network egress cost
  
Container Registry:
  - Storage: $0.026 per GB/month
  - Minimal cost for image storage
```

## 13. Future Enhancements

### 13.1 Planned Features
- [ ] Production environment deployment
- [ ] Read replicas for Cloud SQL
- [ ] Redis caching layer
- [ ] Enhanced audit logging
- [ ] Email notification service
- [ ] Password reset functionality
- [ ] Two-factor authentication (2FA)
- [ ] API rate limiting
- [ ] Advanced monitoring dashboards

### 13.2 Scalability Improvements
- [ ] Multi-region deployment
- [ ] Global load balancing
- [ ] Cloud CDN integration
- [ ] Database sharding strategy
- [ ] Microservices decomposition

---

## Appendix: Quick Reference

### Deployed Endpoints
- **NPE API**: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
- **Health Check**: https://fincore-npe-api-lfd6ooarra-nw.a.run.app/actuator/health

### Default Credentials (NPE)
| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123456 | SYSTEM_ADMINISTRATOR |
| compliance | Compliance@123 | COMPLIANCE_OFFICER |
| staff | Staff@123456 | OPERATIONAL_STAFF |

### Key Resources
- **Source Code**: Current repository
- **Infrastructure**: [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc)
- **CI/CD**: GitHub Actions (`.github/workflows/deploy-npe.yml`)

---

*Last Updated: December 20, 2025*  
*This architecture documentation reflects the current deployed state of the FinCore User Management API on Google Cloud Platform.*
