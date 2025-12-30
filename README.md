# FinCore User Management API

A Spring Boot microservice providing secure user authentication, role-based access control, and user management capabilities. Deployed on Google Cloud Platform using Cloud Run and Cloud SQL.

## 🚀 Live Deployment

- **NPE Environment**: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
- **Health Check**: https://fincore-npe-api-lfd6ooarra-nw.a.run.app/actuator/health
- **Status**: ✅ Production Ready

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.0 with Java 21
- **Database**: Cloud SQL MySQL 8.0 (GCP)
- **Authentication**: JWT-based with role-based access control  
- **Deployment**: Cloud Run (Containerized, fully managed)
- **CI/CD**: GitHub Actions (Automated build, test, deploy)
- **Infrastructure**: Terraform-managed via [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc)
- **Container Registry**: Google Container Registry (GCR)

### Cloud Infrastructure
- **Platform**: Google Cloud Platform (GCP)
- **Compute**: Cloud Run (serverless containers)
- **Database**: Cloud SQL MySQL 8.0 with built-in connector
- **Networking**: Private VPC + Cloud SQL Socket Factory
- **Secrets**: Cloud Secret Manager
- **Region**: europe-west2 (London)

## ✨ Features

### Security
- 🔐 OAuth2 JWT-based stateless authentication
- 📱 Phone-based Multi-Factor Authentication (MFA) with OTP
- 👥 Role-Based Access Control (RBAC) with 4 predefined roles
- 🔒 Time-limited OTP codes (5-minute expiration)
- 🔑 Secure JWT token generation with HS256
- ⏱️ Configurable JWT token expiration (24 hours default)
- 🛡️ HTTPS-only communication
- 🔐 Secure database connections via Cloud SQL Socket Factory
- 🧹 Automatic cleanup of expired OTP tokens

### User Management
- ✅ User CRUD operations with role-based permissions
- 📊 User status management (ACTIVE, INACTIVE, LOCKED)
- 👤 Comprehensive user profiles (employee ID, department, job title)
- 📧 Email and phone number tracking
- 📅 Last login and account activity tracking
- 🔄 Failed login attempt monitoring

### API & Integration
- 🌐 RESTful API design
- 📄 JSON request/response format
- ❤️ Health check endpoints for monitoring
- 🧪 Postman collection for API testing
- 📈 Spring Boot Actuator for observability

## 📋 Prerequisites

### Local Development
- Java 21 (Temurin or compatible)
- Maven 3.9+
- Docker (optional, for containerization)
- GCP CLI (optional, for Cloud SQL access)

### Deployment
- GCP Project with billing enabled
- GitHub account with repository access
- GitHub Secrets configured (see Deployment section)

## 🚀 Quick Start

### Local Development (H2 In-Memory Database)

```bash
# Clone the repository
git clone https://github.com/kasisheraz/userManagementApi.git
cd userManagementApi

# Build the project
mvn clean install

# Run with H2 profile (default)
mvn spring-boot:run
```

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (leave empty)

### Local Development (Cloud SQL)

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=npe
export DB_USER=fincore_app
export DB_PASSWORD=your_password
export CLOUD_SQL_INSTANCE=your-project:region:instance-name

# Run the application
mvn spring-boot:run
```

## 📚 API Documentation

### Base URL
```
NPE: https://fincore-npe-api-lfd6ooarra-nw.a.run.app
Local: http://localhost:8080
```

### Authentication

The API uses OAuth2 with phone-based Multi-Factor Authentication (MFA). Authentication is a two-step process:

#### Step 1: Request OTP
```http
POST /api/auth/request-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890"
}

Response (200 OK):
{
  "message": "OTP sent to phone number ending in **7890",
  "phoneNumber": "+1234567890",
  "expiresIn": 300
}
```

#### Step 2: Verify OTP and Receive JWT Token
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+1234567890",
  "otp": "123456"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "userId": 1,
    "phoneNumber": "+1234567890",
    "email": "admin@fincore.com",
    "firstName": "System",
    "lastName": "Administrator",
    "role": "SYSTEM_ADMINISTRATOR",
    "status": "ACTIVE"
  }
}
```

**Note**: Include the JWT token in subsequent requests:
```http
Authorization: Bearer {accessToken}
```

**Development Mode**: OTP codes are logged to the console. In production, OTPs should be sent via SMS service (Twilio, AWS SNS, etc.).

### User Management Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/users` | List all users | ADMIN |
| GET | `/api/users/{id}` | Get user by ID | ADMIN |
| POST | `/api/users` | Create new user | Authenticated |
| PUT | `/api/users/{id}` | Update user | ADMIN |
| DELETE | `/api/users/{id}` | Delete user | ADMIN |

#### Test Phone Numbers

Use these phone numbers from the test data:

| Phone Number | Email | Role | Name |
|-------------|-------|------|------|
| +1234567890 | admin@fincore.com | SYSTEM_ADMINISTRATOR | System Administrator |
| +1234567891 | compliance@fincore.com | COMPLIANCE_OFFICER | Compliance Officer |
| +1234567892 | staff@fincore.com | OPERATIONAL_STAFF | Operational Staff |

#### Create User Example
```http
POST /api/users
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "username": "newuser",
  "password": "SecurePass@123",
  "fullName": "New User",
  "email": "newuser@fincore.com",
  "phoneNumber": "+1234567890",
  "employeeId": "EMP004",
  "department": "Finance",
  "jobTitle": "Analyst",
  "roleName": "OPERATIONAL_STAFF"
}
```

### Health & Monitoring

#### Health Check
```http
GET /actuator/health

Response (200 OK):
{
  "status": "UP"
}
```

## 👥 Default Users & Roles

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | Admin@123456 | SYSTEM_ADMINISTRATOR | All permissions |
| compliance | Compliance@123 | COMPLIANCE_OFFICER | Read-only access |
| staff | Staff@123456 | OPERATIONAL_STAFF | Limited operational access |

### Role Permissions Matrix

| Permission | SYSTEM_ADMIN | ADMIN | COMPLIANCE | STAFF |
|------------|--------------|-------|------------|-------|
| USER_READ | ✅ | ✅ | ✅ | ✅ |
| USER_WRITE | ✅ | ✅ | ❌ | ❌ |
| CUSTOMER_READ | ✅ | ✅ | ✅ | ❌ |
| CUSTOMER_WRITE | ✅ | ✅ | ❌ | ❌ |

## 🧪 Testing

### Using Postman
Import the Postman collection included in the repository:
```bash
postman_collection.json
postman_environment.json
postman_environment_cloud.json
```

### Using test.http (VS Code REST Client)
The repository includes a `test.http` file for quick API testing with VS Code REST Client extension.

### Automated Tests
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## 📁 Project Structure

```
src/main/java/com/fincore/usermgmt/
├── config/              # Security & application configuration
│   ├── SecurityConfig.java
│   └── ApplicationStartupListener.java
├── controller/          # REST API controllers
│   ├── AuthController.java
│   └── UserController.java
├── dto/                 # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── UserDTO.java
├── entity/             # JPA entities
│   ├── User.java
│   ├── Role.java
│   ├── Permission.java
│   └── RolePermission.java
├── mapper/             # MapStruct mappers
│   └── UserMapper.java
├── repository/         # JPA repositories
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
├── security/           # JWT & security
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
└── service/            # Business logic
    ├── AuthService.java
    └── UserService.java

src/main/resources/
├── application.yml                    # Base configuration
├── application-npe.yml               # NPE environment
├── application-production.yml        # Production config
├── application-local-h2.yml          # H2 local dev
├── schema.sql                        # Local H2 schema
└── data.sql                          # Local H2 test data
```

## 🐳 Docker

### Build Docker Image
```bash
docker build -t fincore-user-api:latest .
```

### Run Container Locally
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local-h2 \
  fincore-user-api:latest
```

### Multi-Stage Build
The Dockerfile uses a multi-stage build for optimal image size:
- Build stage: Maven + JDK 21
- Runtime stage: JRE 21-alpine (minimal footprint)

## 🚀 Deployment

### GitHub Actions CI/CD

The repository includes an automated deployment pipeline (`.github/workflows/deploy-npe.yml`):

**Pipeline Stages:**
1. **Build & Test**: Compile code and run tests
2. **Docker Build**: Create and tag Docker image
3. **Push to GCR**: Upload to Google Container Registry
4. **Deploy to Cloud Run**: Deploy new revision
5. **Health Check**: Validate deployment
6. **Smoke Tests**: Test critical endpoints

**Required GitHub Secrets:**
```yaml
GCP_PROJECT_ID          # Your GCP project ID
GCP_SA_KEY              # Service account JSON key
DB_USER                 # Database user (fincore_app)
DB_PASSWORD             # Database password
CLOUDSQL_INSTANCE       # Full Cloud SQL instance name
GCP_SERVICE_ACCOUNT     # Service account email
```

### Manual Deployment

```bash
# 1. Build the project
mvn clean package -DskipTests

# 2. Build Docker image
docker build -t gcr.io/YOUR_PROJECT/fincore-api:latest .

# 3. Push to GCR
docker push gcr.io/YOUR_PROJECT/fincore-api:latest

# 4. Deploy to Cloud Run
gcloud run deploy fincore-npe-api \
  --image=gcr.io/YOUR_PROJECT/fincore-api:latest \
  --region=europe-west2 \
  --platform=managed \
  --allow-unauthenticated \
  --memory=1Gi \
  --cpu=1 \
  --max-instances=3 \
  --min-instances=0 \
  --add-cloudsql-instances=YOUR_INSTANCE \
  --set-env-vars="SPRING_PROFILES_ACTIVE=npe,DB_NAME=fincore_db,DB_USER=fincore_app,DB_PASSWORD=xxx,CLOUD_SQL_INSTANCE=xxx"
```

## 🗄️ Database

### Cloud SQL Setup

The database schema is in `cloud-sql-schema.sql`. To set up:

```bash
# 1. Upload schema to Cloud Storage
gsutil cp cloud-sql-schema.sql gs://your-bucket/

# 2. Grant Cloud SQL service account access
gsutil iam ch serviceAccount:SQL_SA:objectViewer gs://your-bucket

# 3. Import schema
gcloud sql import sql INSTANCE_NAME \
  gs://your-bucket/cloud-sql-schema.sql \
  --database=fincore_db
```

### Database Schema

**Tables:**
- `users`: User accounts and profiles
- `roles`: User roles (SYSTEM_ADMINISTRATOR, ADMIN, etc.)
- `permissions`: Granular permissions (USER_READ, USER_WRITE, etc.)
- `role_permissions`: Many-to-many relationship

**Key Features:**
- Auto-incrementing primary keys
- Unique constraints on username and email
- Foreign key relationships
- Indexed columns for performance
- Timestamp tracking (created_at, updated_at)

## 🔧 Configuration

### Spring Profiles

| Profile | Purpose | Database |
|---------|---------|----------|
| `local-h2` | Local development | H2 in-memory |
| `npe` | NPE environment | Cloud SQL (fincore-npe-db) |
| `production` | Production | Cloud SQL (HA configuration) |

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `npe` |
| `DB_NAME` | Database name | `fincore_db` |
| `DB_USER` | Database username | `fincore_app` |
| `DB_PASSWORD` | Database password | `xxx` |
| `CLOUD_SQL_INSTANCE` | Full instance name | `project:region:instance` |
| `PORT` | Application port | `8080` |

## 📊 Monitoring & Logging

### Health Checks
- **Endpoint**: `/actuator/health`
- **Checks**: Database connectivity, application status
- **Format**: JSON response with status details

### Logging
- **Framework**: SLF4J with Logback
- **Levels**: DEBUG, INFO, WARN, ERROR
- **Output**: Cloud Logging (GCP) in production
- **Format**: Structured JSON logs

### Metrics
- Spring Boot Actuator metrics
- Cloud Run metrics (latency, requests, errors)
- Cloud SQL metrics (connections, queries)

## 🔒 Security Best Practices

### Implemented
- ✅ JWT-based authentication
- ✅ BCrypt password hashing (10 rounds)
- ✅ Account lockout mechanism
- ✅ Role-based access control
- ✅ HTTPS-only in production
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ Secrets in Cloud Secret Manager
- ✅ Minimal service account permissions

### Recommendations
- Use strong passwords (8+ chars, mixed case, numbers, symbols)
- Rotate JWT secret regularly
- Enable Cloud SQL automatic backups
- Implement API rate limiting
- Add audit logging
- Enable 2FA for admin users

## 📖 Additional Resources

- **Infrastructure Repository**: [fincore_Iasc](https://github.com/kasisheraz/fincore_Iasc)
- **Architecture Documentation**: See `architecture-documentation.md`
- **Run Instructions**: See `RUN_INSTRUCTIONS.md`
- **Requirements**: See `user-management-requirements.md`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is proprietary and confidential.

## 📧 Contact

For issues or questions, please open a GitHub issue or contact the development team.

---

**Last Updated**: December 20, 2025  
**Version**: 1.0.0  
**Status**: ✅ Production Ready

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
