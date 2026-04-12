# FinCore User Management API

A comprehensive Spring Boot microservice for secure user authentication, role-based access control, organization onboarding, and KYC verification. Deployed on Google Cloud Platform with automated CI/CD.

## 🚀 Live Deployment

- **NPE Environment**: https://fincore-npe-api-994490239798.europe-west2.run.app
- **Health Check**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
- **Status**: ✅ Production Ready (80+ APIs working)
- **Latest Release**: March 31, 2026 - Bug fixes deployed
- **Test Coverage**: 608/662 tests passing (92%)
- **Database**: Cloud SQL MySQL 8.0 (Private access via Cloud SQL Proxy)

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.0 with **Java 17** (JDK 17.0.18.8)
- **Database**: MySQL 8.0 (Cloud SQL on GCP, H2 for testing)
- **Authentication**: JWT (HS256) with phone-based OTP and role-based access control (RBAC)
- **Deployment**: Google Cloud Run (serverless containers, autoscaling)
- **CI/CD**: Manual deployment via PowerShell scripts
- **Testing**: JUnit 5, Mockito (662 tests, 92% passing)
- **API Documentation**: OpenAPI 3.0 (Swagger UI available)
- **Container**: Docker with Maven build
- **Build Tool**: Maven 3.9+

### Cloud Infrastructure
- **Platform**: Google Cloud Platform (GCP)
- **Compute**: Cloud Run (autoscaling 0-3 instances)
- **Database**: Cloud SQL MySQL 8.0 (europe-west2, private access only)
- **Connection**: Cloud SQL Proxy (no public IP exposure)
- **Secrets**: Cloud Secret Manager (JWT secrets, passwords)
- **Registry**: Google Container Registry (GCR)
- **Region**: europe-west2 (London)
- **Security**: HTTPS only, private database, automated scans

## ✨ Features

### Phase 1: User Management & Authentication
- 🔐 JWT-based stateless authentication (HS256)
- 📱 Phone-based OTP authentication (6-digit, 5-minute expiration)
- 👥 Role-Based Access Control (4 business roles: Admin, Compliance, Operational, Business User)
- 🔑 Secure token management (24-hour JWT expiration)
- 👤 User CRUD operations with validation
- 📍 Address management (create, update, delete)
- 🧹 Automatic OTP cleanup (expired tokens removed)
- 🎯 **Role-Based Data Filtering**: Business Users see only their own data
- 📊 **Dynamic Enums API**: All dropdown values exposed via `/api/enums` endpoint

### Phase 2: Organization Onboarding & KYC
- 🏢 **Organization Management**
  - Create organizations (6 types: LTD, PLC, LLP, SOLE_TRADER, CHARITY, PARTNERSHIP)
  - Update organization details
  - Search/filter organizations (name, type, status, date range)
  - Pagination and sorting
  - Organization status lifecycle (PENDING → ACTIVE → SUSPENDED → CLOSED)

- 📋 **KYC Document Verification**
  - Upload KYC documents (9 types: PASSPORT, DRIVING_LICENSE, NATIONAL_ID, etc.)
  - Document status workflow (PENDING → UNDER_REVIEW → VERIFIED/REJECTED)
  - Sumsub integration ready
  - Document verification by admin users
  - Audit trail (created/modified timestamps)

- 🔍 **KYC Verification & AML Screening**
  - Submit KYC verification (3 levels: BASIC, STANDARD, ENHANCED)
  - Track verification status (SUBMITTED → PENDING → IN_PROGRESS → APPROVED/REJECTED)
  - AML screening integration (sanctions lists, PEP checks)
  - Risk assessment (LOW, MEDIUM, HIGH) based on AML results
  - Automated risk calculation (score-based + match detection)
  - Verification expiry management

- 📝 **Questionnaire Management**
  - Create dynamic questionnaires
  - Question categories (FINANCIAL, LEGAL, OPERATIONAL, COMPLIANCE, etc.)
  - Question status lifecycle (ACTIVE → INACTIVE → ARCHIVED)
  - Reorder questions (display order management)
  - Archive/restore questions

- 💬 **Customer Answers**
  - Submit answers to questionnaire questions
  - Update existing answers
  - Bulk answer submission (multiple questions at once)
  - Answer completion rate tracking
  - Required answer validation

## 📊 Test Coverage

### Unit Tests (200+ tests across 40 files)
- **Controllers**: 14 test classes
- **Services**: 12 test classes  
- **Entities**: 10 test classes
- **Security**: 2 test classes
- **DTOs**: 4 test classes

### Integration Tests
- Full end-to-end workflow tests
- User registration and login
- Address CRUD operations
- Organization management
- KYC document workflows

### Test Statistics
- **Total Tests**: 200+
- **Coverage Target**: 80%+
- **Test Frameworks**: JUnit 5, Mockito, Spring Boot Test
- **Test Execution**: `mvn test` (requires JDK 17)

## 🚀 Quick Start

### Prerequisites
- **Java 17** (OpenJDK or Eclipse Temurin)
- **Maven 3.9+**
- **MySQL 8.0** (local development)

### Local Development

1. **Start MySQL and Run Application**
```powershell
# Windows
./start-local-mysql.ps1

# Linux/Mac
./start.sh
```

2. **Verify Health**
```bash
curl http://localhost:8080/actuator/health
```

### Manual Start
```bash
export JAVA_HOME="/path/to/jdk-17"
export SPRING_PROFILES_ACTIVE=local
export MYSQL_PASSWORD=root
mvn spring-boot:run
```

## 📱 SMS OTP Configuration

### Current Status
⚠️ **Development Mode**: OTP codes are currently logged to console only. For production SMS delivery, SMS provider integration is required.

### Integrating SMS Provider (Twilio Recommended)

1. **Add Twilio Dependency** (pom.xml)
```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.14.1</version>
</dependency>
```

2. **Configure SMS Settings** (application.yml)
```yaml
sms:
  provider: twilio
  twilio:
    account-sid: ${TWILIO_ACCOUNT_SID}
    auth-token: ${TWILIO_AUTH_TOKEN}
    from-number: ${TWILIO_FROM_NUMBER}  # Your Twilio phone number
```

3. **Create SmsService**
```java
@Service
public class SmsService {
    @Value("${sms.twilio.account-sid}")
    private String accountSid;
    
    @Value("${sms.twilio.auth-token}")
    private String authToken;
    
    @Value("${sms.twilio.from-number}")
    private String fromNumber;
    
    public void sendSms(String toPhoneNumber, String message) {
        Twilio.init(accountSid, authToken);
        Message.creator(
            new PhoneNumber(toPhoneNumber),
            new PhoneNumber(fromNumber),
            message
        ).create();
    }
}
```

4. **Update OtpService.java (Line 106)**
Replace:
```java
log.info("Generated OTP for {}: {} (This would be sent via SMS in production)", 
         phoneNumber, otpCode);
```

With:
```java
String message = String.format("Your FinCore OTP code is: %s. Valid for 5 minutes.", otpCode);
smsService.sendSms(phoneNumber, message);
log.info("OTP sent via SMS to {}", phoneNumber);
```

5. **Set Environment Variables**
```bash
export TWILIO_ACCOUNT_SID=your_account_sid
export TWILIO_AUTH_TOKEN=your_auth_token
export TWILIO_FROM_NUMBER=+44xxxxxxxxxx  # Your Twilio UK number
```

### Phone Number Format
- UK numbers: `+447878282674` (E.164 international format)
- Must include country code with + prefix
- No spaces or special characters

### Alternative SMS Providers
- **AWS SNS**: Enterprise-grade, good for scale
- **Azure Communication Services**: Good if already on Azure
- **MessageBird**: European focus, good UK coverage
- **Vonage (Nexmo)**: Global coverage

## 🧪 Testing

### Run All Tests
```bash
export JAVA_HOME="/path/to/jdk-17"
mvn test
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

## 📬 API Testing with Postman

### Import Collection
1. Open Postman
2. Import `postman_collection.json` (56+ endpoints)
3. Import `postman_environment.json`
4. Review `POSTMAN_USAGE_GUIDE.md`

### API Endpoints (56+ total)
- **Authentication**: 2 endpoints
  - POST `/api/auth/otp/request` - Request OTP code
  - POST `/api/auth/otp/verify` - Verify OTP and get JWT token
- **Users**: 6 endpoints
- **Addresses**: 4 endpoints
- **Organizations**: 6 endpoints
- **KYC Documents**: 8 endpoints
- **KYC Verification**: 9 endpoints
- **Questionnaire**: 10 endpoints
- **Customer Answers**: 11 endpoints
- **Enums**: 10+ endpoints
  - GET `/api/enums` - Get all enums (single call, recommended)
  - GET `/api/enums/user-status` - Get user status options
  - GET `/api/enums/organization-type` - Get organization types (8 types)
  - GET `/api/enums/document-type` - Get document types (18 types)
  - GET `/api/enums/document-status` - Get document statuses
  - GET `/api/enums/address-type` - Get address types
  - GET `/api/enums/verification-status` - Get verification statuses
  - GET `/api/enums/verification-level` - Get verification levels
  - GET `/api/enums/screening-type` - Get AML screening types
  - GET `/api/enums/risk-level` - Get risk level options
  - GET `/api/enums/question-category` - Get questionnaire categories
- **Roles**: 1 endpoint
  - GET `/api/roles` - Get all available roles (dynamic from database)

### Dynamic Enum Architecture
All dropdown values are exposed via the `/api/enums` endpoints. Each enum returns:
```json
{
  "value": "ACTIVE",
  "label": "Active",
  "description": "User is active and can access the system"
}
```

The `/api/enums` endpoint returns all enums in a single call for optimal performance:
```json
{
  "userStatus": [...],
  "organizationType": [...],
  "documentType": [...],
  ...
}
```

Frontend components fetch these enums dynamically via `enumService.ts`, eliminating hardcoded values.

See `PHASE2_POSTMAN_GUIDE.md` for Phase 2 API documentation.

## 🗄️ Database Schema

### Core Tables
- **users**, **roles**, **permissions**, **role_permission**, **otp**, **address**

### Organization Tables
- **organisation**, **organisation_member**, **kyc_documents**

### KYC & Compliance Tables
- **customer_kyc_verification**, **aml_screening_results**, **questionnaire_questions**, **customer_answers**

## 🌐 GCP Deployment

```bash
./deploy-to-gcp.ps1
```

See `DEPLOYMENT_GUIDE.md` for details.

## 📚 Documentation

- **[POSTMAN_USAGE_GUIDE.md](POSTMAN_USAGE_GUIDE.md)** - Complete API testing
- **[PHASE2_POSTMAN_GUIDE.md](PHASE2_POSTMAN_GUIDE.md)** - Phase 2 APIs
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - GCP deployment
- **[RUNNING_LOCALLY_GUIDE.md](RUNNING_LOCALLY_GUIDE.md)** - Local setup
- **[architecture-documentation.md](architecture-documentation.md)** - Architecture
- **[CLEANUP_AND_CODE_REVIEW.md](CLEANUP_AND_CODE_REVIEW.md)** - Code quality and cleanup report
- **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - Project organization and file structure

## 🔄 Recent Updates (January 2025)

### Latest Deployment (Build d6a9603)
- ✅ **OTP Deadlock Fix**: Implemented retry logic with exponential backoff for concurrent OTP requests
- ✅ **Database Indexes**: Added automatic index creation for OTP tokens (phone_number, created_at)
- ✅ **Query Optimization**: Native SQL queries with LIMIT clauses for OTP retrieval
- ✅ **API Path Fixes**: Corrected all Phase 2 API endpoints (Organizations, Questionnaires, Questions, etc.)
- ✅ **STANDARD Verification Level**: Added missing enum value to VerificationLevel
- ✅ **Java 21 Upgrade**: Upgraded runtime from Java 17 to Java 21 (JDK 21.0.10)
- ✅ **Code Cleanup**: Removed 64 unnecessary files, organized project structure

### Current Status
- **Production APIs**: 12/12 working ✅
  - Phase 1: Users, Addresses, KYC Documents (3/3)
  - Phase 2: Organizations, Questionnaires, Questions, KYC Verifications, Customer Answers (5/5)
  - Auth: OTP Request, OTP Verify (2/2)
  - System: Health, Info (2/2)
- **Database**: All tables synced, complete-entity-schema.sql deployed
- **Testing**: Automated test suite passing, zero deadlock errors in concurrent OTP tests
- **Performance**: OTP generation <100ms, 5/5 concurrent requests successful

### Known Issues
- 🟡 None reported - all APIs tested and working

## 🔧 Troubleshooting

### Maven Build Fails
```bash
# Use Java 21 (upgraded from Java 17)
export JAVA_HOME="/path/to/jdk-21"
mvn clean compile
```

### MySQL Connection Issues
```bash
# Check MySQL is running
Get-Service MySQL*  # Windows
systemctl status mysql  # Linux

# Test database connection
.\test-db-simple.ps1  # Windows
```

### API Testing
```bash
# Test all Phase 1 & 2 APIs
.\test-all-phase2-apis.ps1  # Tests 8 core endpoints

# Test OTP flow
.\test-otp-deadlock-fix.ps1  # Tests concurrent OTP requests
```

## 📝 Contributing

1. Fork repository
2. Create feature branch
3. Write tests (80%+ coverage)
4. Submit Pull Request

---

**Built with ❤️ using Spring Boot, Java 21, and Google Cloud Platform**
