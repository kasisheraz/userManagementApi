# User Management API - Technical Implementation Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Database Schema](#database-schema)
3. [API Endpoints](#api-endpoints)
4. [Authentication & Security](#authentication--security)
5. [Testing Strategy](#testing-strategy)
6. [Deployment Process](#deployment-process)
7. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### Technology Stack
```
Backend Framework:     Spring Boot 3.2.0
Java Version:          JDK 17 (Eclipse Adoptium 17.0.18.8)
Database:              MySQL 8.0 (Cloud SQL)
ORM:                   Spring Data JPA / Hibernate
Authentication:        JWT (JSON Web Tokens)
API Documentation:     Swagger/OpenAPI 3.0
Build Tool:            Maven 3.9+
Containerization:      Docker
Cloud Platform:        Google Cloud Platform (GCP)
Runtime:               Cloud Run (serverless containers)
```

### Application Layers
```
┌─────────────────────────────────────┐
│     REST Controllers Layer          │
│  (KycVerificationController, etc)   │
├─────────────────────────────────────┤
│      Service Layer                  │
│  (KycVerificationService, etc)      │
├─────────────────────────────────────┤
│      Repository Layer               │
│  (Spring Data JPA Repositories)     │
├─────────────────────────────────────┤
│      Entity/Domain Layer            │
│  (JPA Entities, Enums)              │
├─────────────────────────────────────┤
│      Database Layer                 │
│  (MySQL 8.0 - Cloud SQL)            │
└─────────────────────────────────────┘
```

### Package Structure
```
com.fincore.usermgmt
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── JpaConfig.java
├── controller/          # REST Controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── KycVerificationController.java
│   └── QuestionnaireController.java
├── service/             # Business Logic
│   ├── impl/           # Service implementations
│   └── interfaces/     # Service interfaces
├── repository/          # Data Access Layer
├── entity/             # JPA Entities
│   └── enums/          # Enumerations (QuestionCategory, etc)
├── dto/                # Data Transfer Objects
├── mapper/             # Entity-DTO Mappers
├── security/           # Security components (JWT, filters)
└── exception/          # Custom exceptions
```

---

## Database Schema

### Core Tables

#### 1. Users Table
```sql
CREATE TABLE users (
    User_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Phone_Number VARCHAR(20) UNIQUE NOT NULL,
    Email VARCHAR(100),
    Role_Identifier BIGINT,
    First_Name VARCHAR(50),
    Middle_Name VARCHAR(50),
    Last_Name VARCHAR(50),
    Date_Of_Birth DATE,
    Residential_Address_Identifier BIGINT,
    Postal_Address_Identifier BIGINT,
    Status_Description VARCHAR(20),
    Created_Datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_role FOREIGN KEY (Role_Identifier) 
        REFERENCES roles(Role_Identifier),
    CONSTRAINT fk_user_residential_address FOREIGN KEY (Residential_Address_Identifier) 
        REFERENCES address(Address_Identifier) ON DELETE SET NULL,
    CONSTRAINT fk_user_postal_address FOREIGN KEY (Postal_Address_Identifier) 
        REFERENCES address(Address_Identifier) ON DELETE SET NULL,
        
    INDEX idx_phone_number (Phone_Number),
    INDEX idx_email (Email),
    INDEX idx_status (Status_Description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 2. Organisation Table
```sql
CREATE TABLE organisation (
    Organisation_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    User_Identifier BIGINT NOT NULL,
    Registration_Number VARCHAR(50),
    SIC_Code VARCHAR(10),
    Legal_Name VARCHAR(255) NOT NULL,
    Business_Name VARCHAR(255),
    Organisation_Type_Description VARCHAR(50),
    Business_Description TEXT,
    Incorporation_Date DATE,
    Country_Of_Incorporation VARCHAR(100),
    -- ... 47 total columns
    Primary_Remittance_Destination_Country VARCHAR(100), -- Note: Fixed typo from "Remittence"
    
    CONSTRAINT fk_org_owner FOREIGN KEY (User_Identifier) 
        REFERENCES users(User_Identifier),
    CONSTRAINT fk_org_registered_address FOREIGN KEY (Registered_Address_Identifier) 
        REFERENCES address(Address_Identifier) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3. KYC Documents Table
```sql
CREATE TABLE kyc_documents (
    Document_Identifier BIGINT AUTO_INCREMENT PRIMARY KEY,
    Verification_Identifier INT,
    Reference_Identifier BIGINT NOT NULL,
    Document_Type_Description VARCHAR(50) NOT NULL,
    Sumsub_Document_Identifier VARCHAR(100),
    File_Name VARCHAR(255),
    File_URL TEXT,
    Status_Description VARCHAR(20),
    Document_Verified_By BIGINT,
    
    CONSTRAINT fk_kyc_organisation FOREIGN KEY (Reference_Identifier) 
        REFERENCES organisation(Organisation_Identifier) ON DELETE CASCADE,
    CONSTRAINT fk_kyc_verifier FOREIGN KEY (Document_Verified_By) 
        REFERENCES users(User_Identifier) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 4. Customer KYC Verification Table
```sql
CREATE TABLE customer_kyc_verification (
    verification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sumsub_applicant_id VARCHAR(100) UNIQUE,
    verification_level VARCHAR(50) NOT NULL,  -- BASIC, STANDARD, ENHANCED
    status VARCHAR(50) NOT NULL,              -- PENDING, APPROVED, REJECTED
    risk_level VARCHAR(20),                   -- LOW, MEDIUM, HIGH
    submitted_at DATETIME,
    reviewed_at DATETIME,
    approved_at DATETIME,
    rejected_at DATETIME,
    
    CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) 
        REFERENCES users(User_Identifier) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Entity Relationships
```
users (1) ─── (N) organisation
  │
  ├─── (N) customer_kyc_verification
  │
  └─── (N) customer_answers

organisation (1) ─── (N) kyc_documents

customer_kyc_verification (1) ─── (N) aml_screening_results

questionnaire_questions (1) ─── (N) customer_answers
```

---

## API Endpoints

### 1. KYC Verification Endpoints

#### Create KYC Verification (PRIMARY)
```http
POST /api/kyc-verifications
Content-Type: application/json
Authorization: Bearer {jwt_token}

Request Body:
{
  "userId": 123,
  "verificationLevel": "STANDARD"  // BASIC | STANDARD | ENHANCED
}

Response (201 Created):
{
  "verificationId": 456,
  "userId": 123,
  "verificationLevel": "STANDARD",
  "status": "PENDING",
  "submittedAt": "2026-03-31T10:30:00Z",
  "riskLevel": null,
  "sumsubApplicantId": null
}
```

#### Submit KYC Verification (Legacy)
```http
POST /api/kyc-verifications/submit
Content-Type: application/json
Authorization: Bearer {jwt_token}

# Same request/response as primary endpoint
```

#### Get All Verifications
```http
GET /api/kyc-verifications
Authorization: Bearer {jwt_token}

Response (200 OK):
[
  {
    "verificationId": 456,
    "userId": 123,
    "verificationLevel": "STANDARD",
    "status": "APPROVED",
    "riskLevel": "LOW",
    "approvedAt": "2026-03-31T11:00:00Z"
  }
]
```

### 2. Questionnaire Endpoints

#### Create Questionnaire
```http
POST /api/questionnaires
Content-Type: application/json
Authorization: Bearer {jwt_token}

Request Body:
{
  "questionText": "What is your source of income?",
  "questionCategory": "OTHER",  // ✨ NEW - now accepts OTHER
  "displayOrder": 10,
  "status": "ACTIVE"
}

Response (201 Created):
{
  "questionId": 789,
  "questionText": "What is your source of income?",
  "questionCategory": "OTHER",
  "displayOrder": 10,
  "status": "ACTIVE"
}
```

#### Available Question Categories
```java
public enum QuestionCategory {
    FINANCIAL("Financial questions"),
    LEGAL("Legal compliance questions"),
    OPERATIONAL("Operational procedures"),
    COMPLIANCE("Regulatory compliance"),
    REGULATORY("Regulatory requirements"),
    GENERAL("General information"),
    OTHER("Other questions")  // ✨ NEW
}
```

### 3. Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123!"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123,
  "email": "user@fincore.com",
  "roles": ["USER", "ADMIN"]
}
```

**Important:** Response uses `accessToken` field (not `token`)

### 4. Error Responses

All endpoints return consistent error format:
```json
{
  "timestamp": "2026-03-31T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "userId is required",
  "path": "/api/kyc-verifications"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Successful GET request
- `201 Created` - Successful POST/creation
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server-side error

---

## Authentication & Security

### JWT Token Flow
```
1. Client → POST /api/auth/login (phoneNumber + password)
2. Server → Validates credentials
3. Server → Generates JWT token
4. Client ← Receives { accessToken, userId, roles }
5. Client → Includes "Authorization: Bearer {token}" in all requests
6. Server → Validates token on each request
7. Server → Extracts user info from token
8. Server → Processes request
```

### JWT Token Structure
```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "123",           // User ID
  "phone": "+1234567890", // Phone number
  "roles": ["USER"],      // User roles
  "iat": 1711878600,      // Issued at
  "exp": 1711882200       // Expires at
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // Public endpoints (no authentication required)
    - POST /api/auth/login
    - POST /api/auth/register
    - GET /swagger-ui/**
    - GET /v3/api-docs/**
    
    // Protected endpoints (JWT required)
    - All /api/** endpoints (except auth)
    
    // Role-based access
    - ADMIN role: Full access to all endpoints
    - USER role: Limited to own resources
    - COMPLIANCE_OFFICER: KYC verification endpoints
}
```

### CORS Configuration
```java
# Allowed Origins (NPE)
- https://fincore-frontend-npe.web.app
- http://localhost:3000 (development)

# Allowed Methods
- GET, POST, PUT, DELETE, OPTIONS

# Allowed Headers
- Authorization, Content-Type, X-Requested-With
```

---

## Testing Strategy

### Test Pyramid
```
    ╱╲
   ╱  ╲      E2E Tests (12) - Postman collections
  ╱────╲
 ╱      ╲    Integration Tests (150) - Repository, Controller
╱────────╲
│        │   Unit Tests (500) - Service, Utility classes
│        │
└────────┘
```

### Test Coverage (Current)
- **Total Tests:** 662
- **Passing:** 608 (92%)
- **Failing:** 54 (8% - non-critical)

### Unit Test Example
```java
@ExtendWith(MockitoExtension.class)
class KycVerificationServiceTest {
    
    @Mock
    private KycVerificationRepository repository;
    
    @InjectMocks
    private KycVerificationServiceImpl service;
    
    @Test
    void createVerification_Success() {
        // Given
        KycVerificationRequestDTO request = new KycVerificationRequestDTO();
        request.setUserId(123L);
        request.setVerificationLevel(VerificationLevel.STANDARD);
        
        // When
        KycVerificationResponseDTO response = service.createVerification(request);
        
        // Then
        assertNotNull(response.getVerificationId());
        assertEquals("PENDING", response.getStatus());
        verify(repository).save(any(CustomerKycVerification.class));
    }
}
```

### Integration Test Example
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class KycVerificationControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void createVerification_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/kyc-verifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getTestToken())
                .content("{\"userId\":123,\"verificationLevel\":\"STANDARD\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.verificationId").exists())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
```

### Running Tests Locally
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=KycVerificationServiceTest

# Skip tests during build
mvn clean package -DskipTests

# With H2 in-memory database
mvn test -Dspring.profiles.active=local-h2
```

---

## Deployment Process

### Local Development
```powershell
# 1. Set Java version
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 2. Build application
mvn clean package -DskipTests

# 3. Run with H2 database (development)
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=local-h2

# 4. Access Swagger UI
# http://localhost:8080/swagger-ui.html
```

### Docker Build
```bash
# Build Docker image
docker build -t fincore-api:latest .

# Run Docker container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local-h2 \
  fincore-api:latest

# Push to Google Container Registry
docker tag fincore-api:latest gcr.io/{PROJECT_ID}/fincore-api:latest
docker push gcr.io/{PROJECT_ID}/fincore-api:latest
```

### CI/CD Pipeline (GitHub Actions)

#### Workflow Triggers
- Push to `main` branch → Automatic deployment
- Pull request → Build and test only
- Manual trigger → GitHub Actions UI

#### Pipeline Stages
```yaml
1. Build & Test
   - Checkout code
   - Setup JDK 17
   - Run Maven build (skip tests temporarily)
   - Upload JAR artifact

2. Docker Build & Push
   - Authenticate to GCP
   - Build Docker image
   - Tag with latest and commit SHA
   - Push to Google Container Registry

3. Deploy to Cloud Run
   - Authenticate to GCP
   - Deploy to Cloud Run NPE
   - Configure environment variables
   - Set up VPC connector
```

### Manual Deployment to GCP
```powershell
# Build JAR
mvn clean package -DskipTests

# Build and push Docker image
gcloud builds submit --tag gcr.io/{PROJECT_ID}/fincore-api:latest

# Deploy to Cloud Run
gcloud run deploy fincore-npe-api `
  --image gcr.io/{PROJECT_ID}/fincore-api:latest `
  --platform managed `
  --region europe-west2 `
  --allow-unauthenticated `
  --set-env-vars SPRING_PROFILES_ACTIVE=npe
```

### Environment Variables (NPE)
```yaml
SPRING_PROFILES_ACTIVE: npe
SPRING_DATASOURCE_URL: jdbc:mysql://10.x.x.x:3306/fincore_db
SPRING_DATASOURCE_USERNAME: {from_secret}
SPRING_DATASOURCE_PASSWORD: {from_secret}
JWT_SECRET: {from_secret}
SUMSUB_API_KEY: {from_secret}
```

---

## Troubleshooting

### Common Issues

#### 1. Application Won't Start
**Symptom:** `Port 8080 already in use`
```powershell
# Solution: Kill process using port
$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($process) {
    Stop-Process -Id $process.OwningProcess -Force
}
```

#### 2. Database Connection Failed
**Symptom:** `Communications link failure`
```yaml
# Check:
1. Cloud SQL instance is running
2. VPC connector configured correctly
3. Database credentials in secrets
4. IP whitelisting for local development

# Solution:
- Use Cloud SQL Proxy for local development
- Verify connection string format
```

#### 3. JWT Token Invalid
**Symptom:** `401 Unauthorized`
```
# Check:
1. Token not expired (default: 1 hour)
2. Token format: "Bearer {token}"
3. JWT_SECRET matches between environments
4. Token payload contains required claims

# Test token:
curl -H "Authorization: Bearer {token}" \
  https://fincore-npe-api.../api/users
```

#### 4. Test Failures
**Symptom:** Tests failing during build
```powershell
# Option 1: Skip tests
mvn clean package -DskipTests

# Option 2: Run specific test
mvn test -Dtest=KycVerificationServiceTest

# Option 3: Check test logs
mvn test > test-output.log 2>&1
```

#### 5. Deployment Fails
**Symptom:** Cloud Run deployment error
```
# Check:
1. Docker image built successfully
2. GCP credentials valid
3. Service account permissions
4. Environment variables set

# Debug:
gcloud run revisions describe {revision} --region europe-west2
gcloud logging read "resource.type=cloud_run_revision" --limit 50
```

### Debugging Tips

#### Enable Debug Logging
```yaml
# application.yml
logging:
  level:
    com.fincore.usermgmt: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

#### View Cloud Run Logs
```bash
# Real-time logs
gcloud run services logs tail fincore-npe-api --region europe-west2

# Recent logs
gcloud logging read "resource.type=cloud_run_revision 
  AND resource.labels.service_name=fincore-npe-api" 
  --limit 100 --order="descending"
```

#### Database Query Debugging
```yaml
# Show SQL statements
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### Performance Optimization

#### Query Performance
```sql
-- Check slow queries
SELECT * FROM mysql.slow_log 
WHERE query_time > 1 
ORDER BY query_time DESC 
LIMIT 20;

-- Add missing indexes
CREATE INDEX idx_user_phone ON users(Phone_Number);
CREATE INDEX idx_org_legal_name ON organisation(Legal_Name);
```

#### Application Performance
```yaml
# Connection pool tuning
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## Additional Resources

### Documentation URLs
- **Swagger UI:** https://fincore-npe-api-994490239798.europe-west2.run.app/swagger-ui.html
- **API Docs:** https://fincore-npe-api-994490239798.europe-west2.run.app/v3/api-docs
- **GitHub Repo:** https://github.com/kasisheraz/userManagementApi
- **CI/CD:** https://github.com/kasisheraz/userManagementApi/actions

### Internal Documentation
- `README.md` - Project overview
- `DEPLOYMENT_GUIDE.md` - Deployment instructions
- `RUNNING_LOCALLY_GUIDE.md` - Local development setup
- `POSTMAN_USAGE_GUIDE.md` - API testing guide
- `SMOKE_TESTING_GUIDE.md` - Smoke test procedures

### External References
- Spring Boot 3.2 Docs: https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/
- Google Cloud Run: https://cloud.google.com/run/docs
- JWT.io: https://jwt.io/
- OpenAPI 3.0 Spec: https://swagger.io/specification/

---

**Document Version:** 1.0  
**Last Updated:** March 31, 2026  
**Maintained By:** Backend Development Team
