# FinCore Platform - System Architecture

**Version**: 2.1.0  
**Last Updated**: May 11, 2026  
**Status**: Production Ready

---

## 📐 Architecture Overview

FinCore is a cloud-native financial services platform built with a microservices architecture, deployed on Google Cloud Platform. The system follows a three-tier architecture with clear separation of concerns.

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT TIER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  React SPA (TypeScript)                                   │  │
│  │  - Material-UI Components                                 │  │
│  │  - React Router (Navigation)                              │  │
│  │  - Axios (HTTP Client)                                    │  │
│  │  - Context API (State Management)                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                      APPLICATION TIER                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Spring Boot REST API (Java 17)                          │  │
│  │  ┌────────────┬────────────┬────────────┬──────────────┐│  │
│  │  │Controllers │  Services  │ Repositories│  Security   ││  │
│  │  │  (REST)    │  (Business)│    (JPA)   │   (JWT)     ││  │
│  │  └────────────┴────────────┴────────────┴──────────────┘│  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ JDBC
┌─────────────────────────────────────────────────────────────────┐
│                        DATA TIER                                 │
│  ┌────────────────────┐         ┌──────────────────────────┐   │
│  │  Cloud SQL MySQL   │         │ Google Cloud Storage     │   │
│  │  - User Data       │         │ - KYC Documents (PDF)    │   │
│  │  - Organizations   │         │ - Images (JPG, PNG)      │   │
│  │  - KYC Metadata    │         │ - Signed URLs            │   │
│  └────────────────────┘         └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Component Architecture

### Frontend Architecture (React SPA)

```
fincore_WebUI/
│
├── Presentation Layer
│   ├── Pages (Route-level components)
│   │   ├── Dashboard
│   │   ├── Organizations (List, Create, Details)
│   │   ├── KYC Documents
│   │   └── Users
│   │
│   ├── Components (Reusable UI)
│   │   ├── FileDropZone (Drag & drop upload)
│   │   ├── KYCDocumentsUploadTab
│   │   ├── OrganizationForm (7-step wizard)
│   │   └── OrganizationRejectDialog
│   │
│   └── Layout
│       ├── Header (Navigation)
│       ├── Sidebar (Menu)
│       └── Footer
│
├── Business Logic Layer
│   ├── Services (API calls)
│   │   ├── authService
│   │   ├── organizationService
│   │   ├── kycDocumentService
│   │   └── enumService
│   │
│   └── Context (State management)
│       ├── AuthContext (User session)
│       └── ThemeContext (UI theme)
│
└── Utilities Layer
    ├── apiClient (Axios instance)
    ├── errorHandler
    └── validators
```

### Backend Architecture (Spring Boot)

```
userManagementApi/
│
├── Web Layer
│   ├── Controllers (REST endpoints)
│   │   ├── AuthController (/api/auth)
│   │   ├── UserController (/api/users)
│   │   ├── OrganisationController (/api/organizations)
│   │   └── KycDocumentController (/api/kyc-documents)
│   │
│   └── DTOs (Data Transfer Objects)
│       ├── Request DTOs (UserCreateDTO, OrganisationRejectionDTO)
│       └── Response DTOs (UserDTO, OrganisationDTO)
│
├── Business Layer
│   ├── Services (Business logic)
│   │   ├── AuthService (Authentication, OTP)
│   │   ├── UserService (User CRUD)
│   │   ├── OrganisationService (Org management, approval workflow)
│   │   └── KycDocumentService (File upload, GCS integration)
│   │
│   └── Security
│       ├── JwtUtil (Token generation/validation)
│       ├── JwtAuthenticationFilter
│       └── SecurityConfig (Authorization rules)
│
├── Data Access Layer
│   ├── Repositories (JPA)
│   │   ├── UserRepository
│   │   ├── OrganisationRepository
│   │   └── KycDocumentRepository
│   │
│   └── Entities (JPA Entities)
│       ├── User
│       ├── Organisation
│       └── KycDocument
│
└── Infrastructure Layer
    ├── Configuration (Spring beans)
    ├── Exception Handling (Global error handler)
    └── Utilities (Helper classes)
```

---

## 🔄 Request Flow

### 1. User Authentication Flow

```
┌──────┐         ┌──────────┐         ┌─────────┐         ┌──────┐
│Client│         │  API     │         │ Service │         │  DB  │
└──┬───┘         └────┬─────┘         └────┬────┘         └──┬───┘
   │                  │                     │                  │
   │ POST /auth/request-otp                │                  │
   ├─────────────────>│                     │                  │
   │                  │ generateOtp()       │                  │
   │                  ├────────────────────>│                  │
   │                  │                     │ save(otp)        │
   │                  │                     ├─────────────────>│
   │                  │                     │<─────────────────┤
   │                  │<────────────────────┤                  │
   │<─────────────────┤                     │                  │
   │                  │                     │                  │
   │ POST /auth/login (phone + OTP)        │                  │
   ├─────────────────>│                     │                  │
   │                  │ validateOtp()       │                  │
   │                  ├────────────────────>│                  │
   │                  │                     │ findByPhone()    │
   │                  │                     ├─────────────────>│
   │                  │                     │<─────────────────┤
   │                  │ generateJWT()       │                  │
   │                  │<────────────────────┤                  │
   │<─────────────────┤                     │                  │
   │ {token, user}    │                     │                  │
```

### 2. Organization KYC Workflow

```
┌──────┐    ┌──────────┐    ┌─────────┐    ┌──────┐    ┌─────┐
│Client│    │   API    │    │ Service │    │  DB  │    │ GCS │
└──┬───┘    └────┬─────┘    └────┬────┘    └──┬───┘    └──┬──┘
   │             │                │             │           │
   │ 1. Create Organization       │             │           │
   ├────────────>│                │             │           │
   │             │ create()       │             │           │
   │             ├───────────────>│             │           │
   │             │                │ save(org)   │           │
   │             │                ├────────────>│           │
   │             │                │<────────────┤           │
   │             │<───────────────┤             │           │
   │<────────────┤ {org, status: PENDING}      │           │
   │             │                │             │           │
   │ 2. Upload KYC Documents      │             │           │
   ├────────────>│                │             │           │
   │             │ upload(file)   │             │           │
   │             ├───────────────>│             │           │
   │             │                │             │ upload()  │
   │             │                │             ├──────────>│
   │             │                │             │<──────────┤
   │             │                │ save(doc)   │  {url}    │
   │             │                ├────────────>│           │
   │             │                │<────────────┤           │
   │             │<───────────────┤             │           │
   │<────────────┤ {doc, status: PENDING}      │           │
   │             │                │             │           │
   │ 3. Submit for Review         │             │           │
   ├────────────>│                │             │           │
   │             │ submit()       │             │           │
   │             ├───────────────>│             │           │
   │             │                │ update(org) │           │
   │             │                ├────────────>│           │
   │             │                │ update(docs)│           │
   │             │                ├────────────>│           │
   │             │                │<────────────┤           │
   │             │<───────────────┤             │           │
   │<────────────┤ {org, status: UNDER_REVIEW} │           │
   │             │                │             │           │
   │ 4. Admin Approve/Reject      │             │           │
   ├────────────>│                │             │           │
   │             │ approve()      │             │           │
   │             ├───────────────>│             │           │
   │             │                │ update(org) │           │
   │             │                ├────────────>│           │
   │             │                │ update(docs)│           │
   │             │                ├────────────>│           │
   │             │                │<────────────┤           │
   │             │<───────────────┤             │           │
   │<────────────┤ {org, status: ACTIVE}       │           │
```

### 3. Document-Level Rejection Flow

```
Admin                Frontend            Backend Service        Database
  │                     │                       │                  │
  │ Click Reject        │                       │                  │
  ├────────────────────>│                       │                  │
  │                     │ GET /organizations/{id}/kyc-documents   │
  │                     ├──────────────────────>│                  │
  │                     │                       │ findByOrgId()    │
  │                     │                       ├─────────────────>│
  │                     │                       │<─────────────────┤
  │                     │<──────────────────────┤                  │
  │<────────────────────┤ Display dialog with document list       │
  │                     │                       │                  │
  │ Select docs + reasons                       │                  │
  ├────────────────────>│                       │                  │
  │                     │ PUT /organizations/{id}/reject          │
  │                     ├──────────────────────>│                  │
  │                     │                       │ Transaction Begin│
  │                     │                       │ 1. Reject selected docs
  │                     │                       ├─────────────────>│
  │                     │                       │ 2. Verify non-selected docs
  │                     │                       ├─────────────────>│
  │                     │                       │ 3. Update org status
  │                     │                       ├─────────────────>│
  │                     │                       │ 4. Set org summary
  │                     │                       ├─────────────────>│
  │                     │                       │ Transaction Commit
  │                     │                       │<─────────────────┤
  │                     │<──────────────────────┤                  │
  │<────────────────────┤ Success notification │                  │
```

---

## 🗄️ Database Schema

### Core Tables

#### users
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    date_of_birth DATE,
    user_status VARCHAR(20) DEFAULT 'ACTIVE',
    role VARCHAR(50) DEFAULT 'BUSINESS_USER',
    residential_address_id BIGINT,
    postal_address_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_status (user_status)
);
```

#### organisation
```sql
CREATE TABLE organisation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    legal_name VARCHAR(200) NOT NULL,
    trading_name VARCHAR(200),
    organisation_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    tax_id VARCHAR(50),
    incorporation_date DATE,
    owner_id BIGINT NOT NULL,
    organisation_status VARCHAR(50) DEFAULT 'PENDING',
    reason_description TEXT,  -- Rejection summary
    business_address_id BIGINT,
    primary_contact_id BIGINT,
    industry VARCHAR(50),
    company_size VARCHAR(20),
    annual_revenue VARCHAR(50),
    number_of_employees INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_status (organisation_status),
    INDEX idx_owner (owner_id),
    INDEX idx_reg_number (registration_number)
);
```

#### kyc_document
```sql
CREATE TABLE kyc_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organisation_id BIGINT,
    beneficiary_id BIGINT,  -- NEW: Support for beneficiary documents
    document_type VARCHAR(100) NOT NULL,
    document_number VARCHAR(100),
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    document_status VARCHAR(50) DEFAULT 'PENDING',
    reason_description TEXT,  -- Admin rejection feedback
    issue_date DATE,
    expiry_date DATE,
    uploaded_by BIGINT,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE,
    FOREIGN KEY (beneficiary_id) REFERENCES beneficiary(id) ON DELETE CASCADE,  -- NEW
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (verified_by) REFERENCES users(id),
    INDEX idx_org (organisation_id),
    INDEX idx_beneficiary (beneficiary_id),  -- NEW
    INDEX idx_status (document_status),
    INDEX idx_type (document_type),
    CONSTRAINT chk_reference CHECK (
        (organisation_id IS NOT NULL AND beneficiary_id IS NULL) OR 
        (organisation_id IS NULL AND beneficiary_id IS NOT NULL)
    )  -- NEW: Ensure only one reference type
);
```

#### beneficiary
```sql
CREATE TABLE beneficiary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    beneficiary_identifier VARCHAR(50) UNIQUE NOT NULL,
    beneficiary_name VARCHAR(255) NOT NULL,
    nick_name VARCHAR(100),
    business_name VARCHAR(255),
    country VARCHAR(100) NOT NULL,
    user_identifier BIGINT NOT NULL,
    registered_address_identifier BIGINT NOT NULL,
    is_counter_over_counter BOOLEAN DEFAULT FALSE,
    collector_contact_number VARCHAR(20),
    status_description VARCHAR(50) DEFAULT 'PENDING',
    reason_description TEXT,  -- Rejection/suspension reason
    created_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    FOREIGN KEY (user_identifier) REFERENCES users(id),
    FOREIGN KEY (registered_address_identifier) REFERENCES address(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (last_modified_by) REFERENCES users(id),
    INDEX idx_user (user_identifier),
    INDEX idx_status (status_description),
    INDEX idx_country (country),
    INDEX idx_c2c (is_counter_over_counter),
    CONSTRAINT chk_c2c_phone CHECK (
        (is_counter_over_counter = FALSE) OR 
        (is_counter_over_counter = TRUE AND collector_contact_number IS NOT NULL)
    )  -- C2C requires phone number
);
```

#### otp_record
```sql
CREATE TABLE otp_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_phone (phone_number),
    INDEX idx_expiry (expiry_time)
);
```

### Entity Relationships

```
                  ┌─────────────┐
                  │    users    │
                  └──────┬──────┘
                         │ 1
              ┌──────────┼──────────┐
              │          │          │
         owns │     owns │          │ owns
              │          │          │
              │ *        │ *        │ *
   ┌──────────┴─────┐    │    ┌────┴────────┐
   │  organisation  │    │    │ beneficiary │
   └──────┬─────────┘    │    └────┬────────┘
          │ 1            │         │ 1
          │              │         │
          │ has          │         │ has
          │              │         │
          │ *            │ *       │ *
   ┌──────┴──────────────┴─────────┴──────┐
   │         kyc_document                  │
   │  (organisation_id OR beneficiary_id)  │
   └───────────────────────────────────────┘

Legend:
1 = one
* = many

Notes:
- Each user can own 1 organisation and up to 20 beneficiaries
- Each organisation/beneficiary can have multiple KYC documents
- Each KYC document belongs to either an organisation OR a beneficiary (enforced by CHECK constraint)
```

---

## 🔐 Security Architecture

### Authentication Flow

```
1. User enters phone number
   ↓
2. Backend generates 6-digit OTP
   ↓
3. OTP stored in database (5-minute expiry)
   ↓
4. OTP sent to user (currently logged, SMS pending)
   ↓
5. User enters OTP
   ↓
6. Backend validates OTP
   ↓
7. JWT token generated (24-hour expiry)
   ↓
8. Token sent to client
   ↓
9. Client stores token (localStorage)
   ↓
10. Token included in all subsequent API calls (Authorization header)
```

### Authorization Layers

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: Route Guards (Frontend)                           │
│  - Check if user is authenticated                           │
│  - Redirect to login if not                                 │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 2: UI Element Visibility (Frontend)                  │
│  - Hide buttons based on role                               │
│  - Disable actions based on status                          │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 3: JWT Validation (Backend)                          │
│  - Validate token signature                                 │
│  - Check token expiry                                       │
│  - Extract user ID and role                                 │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 4: Method-Level Security (Backend)                   │
│  - @PreAuthorize annotations                                │
│  - Check user role matches required role                    │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│  Layer 5: Business Logic Security (Backend)                 │
│  - Check resource ownership                                 │
│  - Validate status transitions                              │
│  - Enforce business rules                                   │
└─────────────────────────────────────────────────────────────┘
```

### Role-Based Access Control

```
┌───────────────────────────────────────────────────────────────┐
│                    SYSTEM_ADMINISTRATOR                        │
│  Full access to all resources                                 │
│  - Approve/Reject organizations                               │
│  - Manage all users                                           │
│  - View all data                                              │
│  - System configuration                                       │
└───────────────────────────────────────────────────────────────┘
                         ↑
                         │ inherits
┌───────────────────────────────────────────────────────────────┐
│                    COMPLIANCE_OFFICER                          │
│  Compliance and risk management                               │
│  - View all organizations                                     │
│  - Review KYC documents                                       │
│  - Generate compliance reports                                │
└───────────────────────────────────────────────────────────────┘
                         ↑
                         │ inherits
┌───────────────────────────────────────────────────────────────┐
│                    OPERATIONAL_USER                            │
│  Operational tasks                                            │
│  - Process applications                                       │
│  - Update organization details                                │
└───────────────────────────────────────────────────────────────┘
                         ↑
                         │ inherits
┌───────────────────────────────────────────────────────────────┐
│                      BUSINESS_USER                             │
│  Limited to own resources                                     │
│  - Create own organization                                    │
│  - Upload own documents                                       │
│  - Submit for review                                          │
│  - View own data only                                         │
└───────────────────────────────────────────────────────────────┘
```

---

## ☁️ Cloud Infrastructure

### Google Cloud Platform Architecture

```
                         ┌──────────────────────┐
                         │   Users (Browser)    │
                         └──────────┬───────────┘
                                    │ HTTPS
                                    ↓
                         ┌──────────────────────┐
                         │  Cloud Load Balancer │
                         └──────────┬───────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ↓                               ↓
        ┌───────────────────────┐     ┌───────────────────────┐
        │  Cloud Run (Frontend) │     │  Cloud Run (Backend)  │
        │  - React SPA          │     │  - Spring Boot API    │
        │  - Nginx              │     │  - Autoscaling 0-3    │
        │  - Autoscaling 0-3    │     │  - europe-west2       │
        └───────────────────────┘     └───────────┬───────────┘
                                                   │
                               ┌───────────────────┼───────────────────┐
                               ↓                   ↓                   ↓
                    ┌──────────────────┐  ┌──────────────┐  ┌─────────────────┐
                    │ Cloud SQL MySQL  │  │ Cloud Storage│  │ Secret Manager  │
                    │ - Private IP     │  │ - KYC Docs   │  │ - JWT Secret    │
                    │ - Automated      │  │ - PDF/Images │  │ - DB Password   │
                    │   Backups        │  │ - Signed URLs│  │                 │
                    └──────────────────┘  └──────────────┘  └─────────────────┘
```

### Resource Configuration

#### Cloud Run (Frontend)
```yaml
Service: fincore-webui-npe
Region: europe-west2
Container: nginx:alpine with React build
Port: 80
CPU: 1 vCPU
Memory: 512 MB
Min Instances: 0
Max Instances: 3
Timeout: 300s
Concurrency: 80
```

#### Cloud Run (Backend)
```yaml
Service: fincore-npe-api
Region: europe-west2
Container: openjdk:17-slim with Spring Boot JAR
Port: 8080
CPU: 1 vCPU
Memory: 512 MB
Min Instances: 0
Max Instances: 3
Timeout: 300s
Concurrency: 80
Environment Variables:
  - SPRING_PROFILES_ACTIVE=cloud
  - DATABASE_URL=jdbc:mysql://...
  - GCS_BUCKET_NAME=fincore-kyc-documents
```

#### Cloud SQL
```yaml
Instance: fincore-mysql-npe
Version: MySQL 8.0
Tier: db-f1-micro
vCPUs: 1
RAM: 3.75 GB
Storage: 10 GB SSD
Backup: Daily (7-day retention)
Region: europe-west2
IP: Private only (Cloud SQL Proxy)
```

#### Cloud Storage
```yaml
Bucket: fincore-kyc-documents
Location: europe-west2
Storage Class: Standard
Access: Private (signed URLs only)
Versioning: Disabled
Lifecycle: 7-day soft delete
```

---

## 📊 Data Flow Diagrams

### Document Upload Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. Select file (PDF/JPG/PNG, max 10MB)
     ↓
┌─────────────────┐
│ FileDropZone    │ 2. Validate file type and size
│ Component       │
└────┬────────────┘
     │ 3. File validated
     ↓
┌─────────────────┐
│ KYC Upload Tab  │ 4. Click "Upload" button
└────┬────────────┘
     │ 5. POST /api/kyc-documents/upload (multipart/form-data)
     ↓
┌─────────────────┐
│ Backend API     │ 6. Validate request (JWT, file, org ownership)
└────┬────────────┘
     │ 7. Generate unique filename
     ↓
┌─────────────────┐
│ GCS Service     │ 8. Upload file to Google Cloud Storage
└────┬────────────┘
     │ 9. Return file URL
     ↓
┌─────────────────┐
│ Database        │ 10. Save document metadata (name, URL, status=PENDING)
└────┬────────────┘
     │ 11. Return KycDocumentDTO
     ↓
┌─────────────────┐
│ Frontend        │ 12. Update UI (show in table)
│                 │ 13. Show success notification
└─────────────────┘
```

### Approval Workflow State Machine

```
                    ┌─────────────┐
                    │   PENDING   │ (Initial state)
                    └──────┬──────┘
                           │
                           │ submitForReview()
                           ↓
                    ┌─────────────┐
              ┌────>│UNDER_REVIEW │<────┐
              │     └──────┬──────┘     │
              │            │             │
              │            ├─────────────┤
              │            ↓             ↓
              │     ┌──────────┐  ┌─────────────────────┐
              │     │  ACTIVE  │  │REQUIRES_RESUBMISSION│
              │     └──────────┘  └──────────┬──────────┘
              │       (Approved)              │
              │                               │ resubmit()
              └───────────────────────────────┘

Status Transitions:
- submitForReview(): PENDING → UNDER_REVIEW
- approveOrganisation(): UNDER_REVIEW → ACTIVE
- rejectOrganisation(): UNDER_REVIEW → REQUIRES_RESUBMISSION
- submitForReview(): REQUIRES_RESUBMISSION → UNDER_REVIEW
```

---

## 🔄 Integration Points

### Current Integrations

1. **Google Cloud Storage**
   - Purpose: KYC document storage
   - Integration: Google Cloud Storage Client Library
   - Authentication: Service Account JSON key
   - Operations: Upload, download, delete files

2. **Cloud SQL**
   - Purpose: Relational data storage
   - Integration: JDBC with HikariCP connection pool
   - Authentication: Cloud SQL Proxy with IAM
   - Features: Automated backups, high availability ready

3. **Cloud Secret Manager**
   - Purpose: Secure secrets storage
   - Integration: Google Cloud Secret Manager Client Library
   - Secrets: JWT secret key, database password, GCS credentials

### Planned Integrations

1. **Twilio (SMS)**
   - Purpose: OTP delivery via SMS
   - Status: Ready for integration (placeholder in code)
   - Required: Twilio account, phone number

2. **Sumsub (KYC Verification)**
   - Purpose: Biometric verification, document OCR
   - Status: Mock endpoints created
   - Required: Sumsub API key

3. **Email Service (SendGrid/AWS SES)**
   - Purpose: Email notifications
   - Status: Not yet implemented
   - Use cases: Account verification, status updates

4. **Refinitiv/Dow Jones (AML Screening)**
   - Purpose: Sanctions screening, PEP checks
   - Status: Data model ready
   - Required: API credentials

---

## 🛠️ Development & Build Process

### Backend Build Pipeline

```
1. Source Code (Java 17)
   ↓
2. Maven Compile (mvn compile)
   ↓
3. Run Unit Tests (mvn test)
   ↓
4. Package JAR (mvn package)
   ↓
5. Build Docker Image
   ↓
6. Push to Google Container Registry
   ↓
7. Deploy to Cloud Run
```

### Frontend Build Pipeline

```
1. Source Code (TypeScript)
   ↓
2. npm install (dependencies)
   ↓
3. TypeScript Compilation (tsc)
   ↓
4. React Build (npm run build)
   ↓
5. Build Docker Image (Nginx + static files)
   ↓
6. Push to Google Container Registry
   ↓
7. Deploy to Cloud Run
```

---

## 📈 Scalability & Performance

### Horizontal Scaling
- **Cloud Run**: Auto-scales 0-3 instances based on load
- **Database**: Can be scaled vertically (upgrade tier)
- **Storage**: Unlimited capacity

### Caching Strategy
- **Frontend**: Browser caching for static assets
- **Backend**: JPA second-level cache (Hibernate)
- **Database**: Query result caching

### Performance Optimization
- **Pagination**: All list endpoints support pagination
- **Lazy Loading**: JPA relationships configured for lazy loading
- **Connection Pooling**: HikariCP with 10 connections
- **Asset Optimization**: Frontend builds minified and compressed

---

## 🔍 Monitoring & Observability

### Current Monitoring
- **Cloud Run Metrics**: Request count, latency, error rate
- **Database Metrics**: CPU, memory, connections, slow queries
- **Storage Metrics**: Object count, bandwidth usage
- **Application Logs**: Centralized in Cloud Logging

### Logging
- **Backend**: SLF4J with Logback
- **Frontend**: Console logging (development)
- **Log Levels**: INFO (production), DEBUG (development)

### Future Enhancements
- [ ] Application Performance Monitoring (APM)
- [ ] Custom metrics and dashboards
- [ ] Alerting rules (Slack/Email)
- [ ] Distributed tracing

---

## 📚 API Documentation

### OpenAPI/Swagger
- **URL**: `/swagger-ui.html` (when running)
- **API Docs**: `/v3/api-docs`
- **Format**: OpenAPI 3.0
- **Features**: Interactive API testing, request/response examples

### Postman Collections
- **Organization KYC Workflow**: 85+ requests
- **User & Address Management**: 30+ requests
- **Security Tests**: 15+ requests
- **Environment Files**: Local + Cloud configurations

---

## 🎯 Design Principles

### Backend (Spring Boot)
1. **Single Responsibility**: Each service has one clear purpose
2. **Dependency Injection**: Constructor-based DI
3. **Separation of Concerns**: Controllers, services, repositories
4. **DTO Pattern**: Separate DTOs from entities
5. **Exception Handling**: Global exception handler
6. **Transaction Management**: @Transactional for data consistency

### Frontend (React)
1. **Component Composition**: Small, reusable components
2. **Single Source of Truth**: Context API for global state
3. **Controlled Components**: Forms managed by React state
4. **Separation of Concerns**: Business logic in services
5. **Type Safety**: Full TypeScript coverage
6. **Responsive Design**: Mobile-first approach

---

**Document Version**: 1.0  
**Prepared By**: FinCore Development Team  
**Last Updated**: May 11, 2026
