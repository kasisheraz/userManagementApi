# FinCore User Management API

A comprehensive Spring Boot microservice for secure user authentication, role-based access control, organization onboarding, and KYC verification. Deployed on Google Cloud Platform with automated CI/CD.

## 🚀 Live Deployment

- **NPE Environment**: https://fincore-npe-api-994490239798.europe-west2.run.app
- **Health Check**: https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
- **Status**: ✅ Production Ready
- **Database**: Cloud SQL MySQL 8.0 (Private access via Cloud SQL Proxy)

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.0 with **Java 17** (LTS)
- **Database**: MySQL 8.0 (Cloud SQL on GCP, local MySQL for development)
- **Authentication**: JWT with role-based access control (RBAC)
- **Deployment**: Google Cloud Run (serverless containers)
- **CI/CD**: GitHub Actions (automated build, test, deploy)
- **Testing**: JUnit 5, Mockito, JaCoCo (80%+ coverage target)
- **Container**: Docker multi-stage builds
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
- 👥 Role-Based Access Control (4 roles, 21 permissions)
- 🔑 Secure token management (24-hour JWT expiration)
- 👤 User CRUD operations with validation
- 📍 Address management (create, update, delete)
- 🧹 Automatic OTP cleanup (expired tokens removed)

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
- **Users**: 6 endpoints
- **Addresses**: 4 endpoints
- **Organizations**: 6 endpoints
- **KYC Documents**: 8 endpoints
- **KYC Verification**: 9 endpoints
- **Questionnaire**: 10 endpoints
- **Customer Answers**: 11 endpoints

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

## 🔧 Troubleshooting

### Maven Build Fails
```bash
# Use Java 17 (not Java 21+)
export JAVA_HOME="/path/to/jdk-17"
mvn clean compile
```

### MySQL Connection Issues
```bash
# Check MySQL is running
Get-Service MySQL*  # Windows
systemctl status mysql  # Linux
```

## 📝 Contributing

1. Fork repository
2. Create feature branch
3. Write tests (80%+ coverage)
4. Submit Pull Request

---

**Built with ❤️ using Spring Boot, Java 17, and Google Cloud Platform**
