# User Management API - Clean Project Structure

## 📁 Project Layout

```
userManagementApi/
├── 📄 Core Documentation
│   ├── README.md                          # Main project documentation
│   ├── API_STATUS_REPORT.md               # Current API status and test results
│   ├── DEPLOYMENT_GUIDE.md                # How to deploy to GCP
│   ├── RUNNING_LOCALLY_GUIDE.md           # Local development setup
│   ├── SMOKE_TESTING_GUIDE.md             # Testing guidelines
│   ├── TEST_DATA_GUIDE.md                 # Test data management
│   ├── POSTMAN_USAGE_GUIDE.md             # API testing with Postman
│   ├── PHASE2_POSTMAN_GUIDE.md            # Phase 2 API testing
│   ├── CLOUD_SQL_SECURITY.md              # Database security config
│   ├── CLEANUP_ANDCODE_REVIEW.md          # Code quality and cleanup report
│   ├── PROJECT_STRUCTURE.md               # This file - project organization
│   ├── architecture-documentation.md      # System architecture
│   └── RUN_INSTRUCTIONS.md                # Quick start guide
│
├── 🐳 Docker & Deployment
│   ├── Dockerfile                         # Container definition
│   ├── .dockerignore                      # Docker ignore rules
│   ├── .gcloudignore                      # GCloud ignore rules
│   ├── app.yaml                           # App Engine config (if needed)
│   └── service.yaml                       # Cloud Run service config
│
├── 🔧 Configuration
│   ├── pom.xml                            # Maven build configuration
│   ├── gcp-config.env.template            # GCP environment template
│   └── .github/workflows/                 # GitHub Actions CI/CD
│       └── deploy-npe.yml                 # NPE deployment workflow
│
├── 📝 Deployment Scripts (Active)
│   ├── deploy-to-gcp.ps1                  # Main deployment script
│   ├── deploy-cloudrun-npe.ps1            # NPE-specific deployment
│   ├── quick-deploy.ps1                   # Quick manual deployment
│   ├── manual-deploy.ps1                  # Detailed manual deployment
│   ├── get-cloudrun-logs.ps1              # View Cloud Run logs
│   ├── get-latest-logs.ps1                # Get recent logs
│   └── check-cloudrun.ps1                 # Check deployment status
│
├── 🧪 Testing Scripts (Active)
│   ├── test-all-phase2-apis.ps1           # Complete API test suite
│   ├── test-otp-deadlock-fix.ps1          # OTP deadlock testing
│   ├── insert-test-data.ps1               # Load test data
│   ├── quick-test-gcp.ps1                 # Quick GCP API test
│   ├── start-local.ps1                    # Start local dev server
│   └── start-local-mysql.ps1              # Start with local MySQL
│
├── 🗄️ Database Scripts
│   ├── complete-entity-schema.sql         # Complete database schema
│   ├── init-database.sql                  # Database initialization
│   ├── init-local-db.ps1                  # Local database setup
│   ├── insert-test-data.sql               # Test data SQL
│   ├── fix-otp-deadlock-indexes.sql       # OTP table optimization
│   └── apply-otp-deadlock-fix.ps1         # Apply OTP fixes
│
├── 📮 Postman Collections
│   ├── postman_collection.json            # Phase 1 APIs
│   ├── phase2-postman-collection.json     # Phase 2 APIs
│   ├── postman_environment.json           # Local environment
│   └── postman_environment_cloud.json     # GCP environment
│
├── 📦 Source Code
│   └── src/
│       ├── main/
│       │   ├── java/com/fincore/usermgmt/
│       │   │   ├── config/               # Configuration classes
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   └── OtpIndexMigration.java
│       │   │   ├── controller/           # REST controllers
│       │   │   │   ├── AuthenticationController.java
│       │   │   │   ├── UserController.java
│       │   │   │   ├── OrganisationController.java
│       │   │   │   ├── QuestionnaireController.java
│       │   │   │   ├── QuestionController.java
│       │   │   │   ├── KycDocumentController.java
│       │   │   │   ├── KycVerificationController.java
│       │   │   │   ├── CustomerAnswerController.java
│       │   │   │   └── SystemInfoController.java
│       │   │   ├── dto/                  # Data Transfer Objects
│       │   │   ├── entity/               # JPA entities
│       │   │   │   ├── User.java
│       │   │   │   ├── Role.java
│       │   │   │   ├── Organisation.java
│       │   │   │   ├── OtpToken.java
│       │   │   │   ├── CustomerKycVerification.java
│       │   │   │   └── enums/
│       │   │   │       └── VerificationLevel.java
│       │   │   ├── mapper/               # Entity-DTO mappers
│       │   │   ├── repository/           # JPA repositories
│       │   │   │   ├── OtpTokenRepository.java
│       │   │   │   └── ...
│       │   │   ├── security/             # Security components
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   └── JwtAuthenticationFilter.java
│       │   │   ├── service/              # Business logic
│       │   │   │   ├── AuthenticationService.java
│       │   │   │   ├── OtpService.java
│       │   │   │   └── ...
│       │   │   └── UserManagementApplication.java
│       │   └── resources/
│       │       ├── application.yml        # Main config
│       │       ├── application-local-h2.yml
│       │       ├── application-local.yml
│       │       ├── application-npe.yml
│       │       ├── application-production.yml
│       │       ├── schema.sql            # H2 schema
│       │       └── data.sql              # H2 test data
│       └── test/
│           └── java/com/fincore/usermgmt/ # Unit & integration tests
│
└── 🎯 Build Output
    └── target/                            # Maven build artifacts (gitignored)
```

## 🎯 Key Files Purpose

### Essential Configuration
- **application-npe.yml**: NPE environment (currently deployed)
- **Dockerfile**: Multi-stage build with JDK 21
- **deploy-npe.yml**: GitHub Actions workflow (needs manual trigger)

### Active Scripts
- **test-all-phase2-apis.ps1**: Main testing script - verifies all 12 APIs
- **quick-deploy.ps1**: Fast manual deployment to Cloud Run
- **fix-otp-deadlock-indexes.sql**: Database optimization for OTP

### Documentation
- **README.md**: Complete project overview
- **DEPLOYMENT_GUIDE.md**: Step-by-step deployment instructions
- **TEST_DATA_GUIDE.md**: How to manage test data

## 🧹 Recently Cleaned Up

Removed 64 unnecessary files:
- ✅ Old troubleshooting guides (consolidated)
- ✅ Temporary log files
- ✅ Duplicate test scripts
- ✅ Outdated SQL files
- ✅ Obsolete configuration files

## 📊 Current Status

**Deployment:**
- ✅ Production: https://fincore-npe-api-994490239798.europe-west2.run.app
- ✅ Build: d6a9603 (OTP deadlock fix + working APIs)
- ✅ Database: Cloud SQL MySQL 8.0 (fincore-npe-db)

**APIs Status:**
- ✅ 12/12 APIs Working (100%)
- ✅ JWT Authentication Working
- ✅ OTP System Working (with deadlock prevention)
- ✅ All CRUD operations functional

**Recent Fixes:**
1. ✅ Added STANDARD enum to VerificationLevel
2. ✅ Fixed SystemInfoController to read BUILD_NUMBER from env
3. ✅ Implemented OTP deadlock retry logic
4. ✅ Added database indexes for OTP table
5. ✅ Optimized transaction isolation

## 🚀 Next Steps

1. **For Development:**
   - Run `.\start-local.ps1` for H2 in-memory database
   - Run `.\start-local-mysql.ps1` for local MySQL

2. **For Testing:**
   - Run `.\test-all-phase2-apis.ps1` to test all APIs
   - Import Postman collections for manual testing

3. **For Deployment:**
   - Run `.\quick-deploy.ps1` for manual deployment
   - Or push to GitHub and manually trigger Actions workflow

## 📝 Notes

- The workspace is now clean and organized
- All active scripts are documented above
- Test files compile with Maven (IDE errors are false positives due to Lombok)
- GitHub Actions requires manual trigger (automatic deployment disabled)
