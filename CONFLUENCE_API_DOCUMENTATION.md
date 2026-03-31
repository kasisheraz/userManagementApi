# FinCore User Management API - Complete Documentation
**For Confluence Publication**

---

## Table of Contents
1. [Overview](#overview)
2. [API Base URLs](#api-base-urls)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Testing](#testing)
8. [Deployment](#deployment)
9. [Recent Updates](#recent-updates)

---

## Overview

The FinCore User Management API is a comprehensive microservice providing:
- **User Authentication** - JWT-based with phone OTP
- **Role-Based Access Control** - 4 roles, 21 permissions  
- **Organization Management** - Complete CRUD for business entities
- **KYC Verification** - Document upload, verification, status tracking
- **AML Screening** - Sanctions, PEP, adverse media checks
- **Questionnaire System** - Dynamic questions and customer answers

**Technology Stack**:
- Spring Boot 3.2.0
- Java 17 (JDK 17.0.18.8)
- MySQL 8.0
- Google Cloud Run
- OpenAPI 3.0 (Swagger)

---

## API Base URLs

### Production (NPE)
```
https://fincore-npe-api-994490239798.europe-west2.run.app
```

### Local Development
```
http://localhost:8080
```

### Health Check
```
GET /actuator/health
```

**Response**:
```json
{
  "status": "UP"
}
```

---

## Authentication

### Method 1: OTP-based Authentication (Phone)

#### Step 1: Request OTP
```http
POST /api/auth/otp/request
Content-Type: application/json

{
  "phoneNumber": "+447700900000"
}
```

**Response**: `200 OK`
```json
{
  "message": "OTP sent successfully",
  "expiresIn": "5 minutes"
}
```

#### Step 2: Verify OTP
```http
POST /api/auth/otp/verify
Content-Type: application/json

{
  "phoneNumber": "+447700900000",
  "otpCode": "123456"
}
```

**Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### Method 2: Direct JWT Token (Development)

For testing, use pre-generated token:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

**Token Structure**:
- **Algorithm**: HS256
- **Expiration**: 24 hours
- **Claims**: phoneNumber, userId, roles

---

## API Endpoints

### 1. Authentication & OTP

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/otp/request` | Request OTP code | No |
| POST | `/api/auth/otp/verify` | Verify OTP and get JWT | No |

---

### 2. User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | Get all users (paginated) | Yes |
| GET | `/api/users/{id}` | Get user by ID | Yes |
| GET | `/api/users/phone/{phoneNumber}` | Find user by phone | Yes |
| POST | `/api/users` | Create new user | Yes |
| PUT | `/api/users/{id}` | Update user | Yes |
| DELETE | `/api/users/{id}` | Delete user | Yes |
| GET | `/api/users/{id}/organisations` | Get user's organizations | Yes |

**Example: Create User**
```http
POST /api/users
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "phoneNumber": "+447700900123",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "roleIdentifier": 1,
  "statusDescription": "ACTIVE"
}
```

---

### 3. Organization Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/organisations` | Get all organisations (paginated) | Yes |
| GET | `/api/organisations/{id}` | Get organisation by ID | Yes |
| GET | `/api/organisations/owner/{userId}` | Get organisations by owner | Yes |
| GET | `/api/organisations/status/{status}` | Filter by status | Yes |
| POST | `/api/organisations` | Create organisation | Yes |
| PUT | `/api/organisations/{id}` | Update organisation | Yes |
| PATCH | `/api/organisations/{id}/status` | Update status only | Yes |
| DELETE | `/api/organisations/{id}` | Delete organisation | Yes |
| GET | `/api/organisations/exists/{regNumber}` | Check registration number | Yes |

**Example: Create Organisation**
```http
POST /api/organisations
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "userIdentifier": 1,
  "legalName": "FinCore Money Services Ltd",
  "businessName": "FinCore Remittance",
  "organisationTypeDescription": "LTD",
  "registrationNumber": "REG12345678",
  "sicCode": "64999",
  "countryOfIncorporation": "United Kingdom",
  "incorporationDate": "2018-06-15",
  "companyNumber": "CN12345678",
  "websiteAddress": "https://fincore.com",
  "statusDescription": "ACTIVE"
}
```

---

### 4. Address Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/addresses` | Get all addresses | Yes |
| GET | `/api/addresses/{id}` | Get address by ID | Yes |
| GET | `/api/addresses/type/{typeCode}` | Filter by type | Yes |
| POST | `/api/addresses` | Create address | Yes |
| PUT | `/api/addresses/{id}` | Update address | Yes |
| DELETE | `/api/addresses/{id}` | Delete address | Yes |

**Address Types**:
- `1` - Residential
- `2` - Business
- `3` - Registered
- `4` - Postal
- `5` - Correspondence

---

### 5. KYC Verification Management

⭐ **NEW**: Backwards-compatible endpoint added!

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **POST** | **/api/kyc-verifications** | **Submit KYC (NEW)** | **Yes** |
| POST | `/api/kyc-verifications/submit` | Submit KYC (original) | Yes |
| GET | `/api/kyc-verifications/{id}` | Get verification + AML results | Yes |
| GET | `/api/kyc-verifications/user/{userId}` | Get all for user | Yes |
| GET | `/api/kyc-verifications/user/{userId}/latest` | Get latest verification | Yes |
| PUT | `/api/kyc-verifications/{id}/status` | Update status | Yes |
| PUT | `/api/kyc-verifications/{id}/approve` | Approve verification | Yes |
| PUT | `/api/kyc-verifications/{id}/reject` | Reject verification | Yes |
| GET | `/api/kyc-verifications/status/{status}` | Filter by status | Yes |
| GET | `/api/kyc-verifications/expired` | Get expired verifications | Yes |
| GET | `/api/kyc-verifications/count/status` | Count by status | Yes |
| GET | `/api/kyc-verifications/approved/{userId}` | Check if user approved | Yes |
| DELETE | `/api/kyc-verifications/{id}` | Delete verification | Yes |

**Example: Submit KYC Verification (NEW endpoint)**
```http
POST /api/kyc-verifications
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "userId": 1,
  "verificationLevel": "STANDARD",
  "status": "PENDING",
  "riskLevel": "MEDIUM"
}
```

**Verification Levels**:
- `BASIC` - Basic identity verification
- `STANDARD` - Standard KYC checks
- `ENHANCED` - Enhanced due diligence

**Status Values**:
- `SUBMITTED` - Initial submission
- `PENDING` - Awaiting review
- `IN_PROGRESS` - Under review
- `APPROVED` - Verification approved
- `REJECTED` - Verification rejected
- `EXPIRED` - Verification expired

---

### 6. KYC Document Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/kyc-documents` | Get all documents | Yes |
| GET | `/api/kyc-documents/{id}` | Get document by ID | Yes |
| GET | `/api/kyc-documents/organisation/{orgId}` | Get org documents | Yes |
| GET | `/api/kyc-documents/type/{type}` | Filter by document type | Yes |
| GET | `/api/kyc-documents/status/{status}` | Filter by status | Yes |
| POST | `/api/kyc-documents` | Upload document | Yes |
| PUT | `/api/kyc-documents/{id}` | Update document | Yes |
| PATCH | `/api/kyc-documents/{id}/verify` | Verify document | Yes |
| PATCH | `/api/kyc-documents/{id}/reject` | Reject document | Yes |
| DELETE | `/api/kyc-documents/{id}` | Delete document | Yes |
| GET | `/api/kyc-documents/organisation/{orgId}/verified` | Get verified docs | Yes |
| GET | `/api/kyc-documents/organisation/{orgId}/pending` | Get pending docs | Yes |

**Document Types**:
- `PASSPORT`
- `DRIVING_LICENSE`
- `NATIONAL_ID`
- `CERTIFICATE_OF_INCORPORATION`
- `PROOF_OF_ADDRESS`
- `BANK_STATEMENT`
- `UTILITY_BILL`
- `HMRC_REGISTRATION`
- `FCA_AUTHORISATION`

---

### 7. Questionnaire Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/questionnaires` | Get all questions (paginated) | Yes |
| GET | `/api/questionnaires/{id}` | Get question by ID | Yes |
| GET | `/api/questionnaires/active` | Get active questions only | Yes |
| GET | `/api/questionnaires/category/{category}` | Filter by category | Yes |
| POST | `/api/questionnaires` | Create question | Yes |
| PUT | `/api/questionnaires/{id}` | Update question | Yes |
| PATCH | `/api/questionnaires/{id}/activate` | Activate question | Yes |
| PATCH | `/api/questionnaires/{id}/inactivate` | Inactivate question | Yes |
| PATCH | `/api/questionnaires/{id}/reorder` | Change display order | Yes |
| DELETE | `/api/questionnaires/{id}` | Archive question | Yes |
| GET | `/api/questionnaires/count/active` | Count active questions | Yes |

**Question Categories**:
- `FINANCIAL` - Financial information
- `LEGAL` - Legal background
- `OPERATIONAL` - Business operations
- `COMPLIANCE` - Compliance requirements
- `REGULATORY` - Regulatory information
- `GENERAL` - General information
- `OTHER` - Other categories ⭐ **NEW**

**Example: Create Question**
```http
POST /api/questionnaires
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "questionText": "What is your estimated annual income?",
  "questionCategory": "FINANCIAL",
  "displayOrder": 1,
  "status": "ACTIVE"
}
```

---

### 8. Customer Answers

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/customer-answers` | Get all answers | Yes |
| GET | `/api/customer-answers/{id}` | Get answer by ID | Yes |
| GET | `/api/customer-answers/user/{userId}` | Get all user answers | Yes |
| GET | `/api/customer-answers/user/{userId}/completed` | Get completed answers | Yes |
| GET | `/api/customer-answers/user/{userId}/question/{questionId}` | Get specific answer | Yes |
| GET | `/api/customer-answers/check/{userId}/{questionId}` | Check if answered | Yes |
| POST | `/api/customer-answers` | Submit answer | Yes |
| POST | `/api/customer-answers/bulk` | Submit multiple answers | Yes |
| PUT | `/api/customer-answers/{id}` | Update answer | Yes |
| DELETE | `/api/customer-answers/{id}` | Delete answer | Yes |
| DELETE | `/api/customer-answers/user/{userId}` | Delete all user answers | Yes |
| GET | `/api/customer-answers/count/{userId}` | Count user answers | Yes |
| GET | `/api/customer-answers/completion-rate/{userId}` | Get completion percentage | Yes |

**Example: Submit Answer**
```http
POST /api/customer-answers
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "userId": 1,
  "questionId": 5,
  "answer": "£50,000 - £100,000"
}
```

**Example: Bulk Submit**
```http
POST /api/customer-answers/bulk
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

[
  {
    "userId": 1,
    "questionId": 1,
    "answer": "£50,000 - £100,000"
  },
  {
    "userId": 1,
    "questionId": 2,
    "answer": "Employment income"
  }
]
```

---

### 9. Roles & Permissions

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/roles` | Get all roles | Yes |
| GET | `/api/roles/{id}` | Get role by ID | Yes |
| GET | `/api/permissions` | Get all permissions | Yes |

**Available Roles**:
1. `SYSTEM_ADMINISTRATOR` - Full system access
2. `ADMIN` - Administrator access
3. `COMPLIANCE_OFFICER` - Compliance and AML access
4. `OPERATIONAL_STAFF` - Operational access

---

## Data Models

### User
```json
{
  "userIdentifier": 1,
  "phoneNumber": "+447700900000",
  "email": "user@example.com",
  "firstName": "John",
  "middleName": "James",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "roleIdentifier": 1,
  "residentialAddressIdentifier": 5,
  "postalAddressIdentifier": 5,
  "statusDescription": "ACTIVE",
  "createdDatetime": "2026-03-31T10:00:00"
}
```

### Organisation
```json
{
  "organisationIdentifier": 1,
  "userIdentifier": 1,
  "legalName": "FinCore Money Services Ltd",
  "businessName": "FinCore Remittance",
  "organisationTypeDescription": "LTD",
  "registrationNumber": "REG12345678",
  "sicCode": "64999",
  "countryOfIncorporation": "United Kingdom",
  "incorporationDate": "2018-06-15",
  "companyNumber": "CN12345678",
  "websiteAddress": "https://fincore.com",
  "statusDescription": "ACTIVE",
  "registeredAddressIdentifier": 10,
  "businessAddressIdentifier": 11,
  "correspondenceAddressIdentifier": 12
}
```

### KYC Verification (with AML Results)
```json
{
  "verificationId": 1,
  "userId": 1,
  "sumsubApplicantId": "SUMSUB_12345",
  "verificationLevel": "STANDARD",
  "status": "APPROVED",
  "riskLevel": "LOW",
  "submittedAt": "2026-03-31T10:00:00",
  "approvedAt": "2026-03-31T11:30:00",
  "amlScreenings": [
    {
      "screeningId": 1,
      "screeningType": "SANCTIONS_LIST",
      "matchFound": false,
      "riskScore": 5,
      "screenedAt": "2026-03-31T10:01:00"
    },
    {
      "screeningId": 2,
      "screeningType": "PEP_CHECK",
      "matchFound": false,
      "riskScore": 0,
      "screenedAt": "2026-03-31T10:01:30"
    }
  ]
}
```

### Questionnaire Question
```json
{
  "questionId": 1,
  "questionText": "What is your estimated annual income?",
  "questionCategory": "FINANCIAL",
  "displayOrder": 1,
  "status": "ACTIVE",
  "createdDatetime": "2026-01-15T09:00:00"
}
```

### Customer Answer
```json
{
  "answerId": 1,
  "userId": 1,
  "questionId": 1,
  "answer": "£50,000 - £100,000",
  "answeredAt": "2026-03-31T14:30:00",
  "createdDatetime": "2026-03-31T14:30:00"
}
```

---

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2026-03-31T10:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999"
}
```

### HTTP Status Codes

| Code | Description | When Used |
|------|-------------|-----------|
| 200 | OK | Successful GET/PUT/PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request body or parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (e.g., phone number exists) |
| 500 | Internal Server Error | Server error |

### Common Error Scenarios

**Invalid JWT Token**:
```http
HTTP/1.1 401 Unauthorized

{
  "timestamp": "2026-03-31T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

**Validation Error**:
```http
HTTP/1.1 400 Bad Request

{
  "timestamp": "2026-03-31T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Legal name is required"
}
```

---

## Testing

### Test Coverage
- **Total Tests**: 662
- **Passing**: 608 (92%)
- **Test Framework**: JUnit 5, Mockito
- **Integration Tests**: H2 in-memory database

### Postman Collections

**Phase 1 & 2 Combined**: `postman_collection.json`
- Authentication (2 endpoints)
- Users (7 endpoints)
- Organisations (9 endpoints)
- Addresses (6 endpoints)
- KYC Documents (12 endpoints)
- KYC Verifications (13 endpoints)
- Questionnaires (11 endpoints)
- Customer Answers (13 endpoints)

**Phase 2 Only**: `phase2-postman-collection.json`
- KYC Verifications (9 endpoints)
- Questionnaires (10 endpoints)
- Customer Answers (11 endpoints)

### Environment Variables
```json
{
  "base_url": "https://fincore-npe-api-994490239798.europe-west2.run.app",
  "jwt_token": "",
  "user_id": "1",
  "verification_id": "",
  "question_id": "",
  "answer_id": ""
}
```

---

## Deployment

### GCP Cloud Run
- **Service Name**: fincore-npe-api
- **Region**: europe-west2 (London)
- **Platform**: Cloud Run (managed)
- **Image**: us-central1-docker.pkg.dev/fincore-npe/userapi-repo/user-management-api
- **Min Instances**: 0
- **Max Instances**: 3
- **Memory**: 512 MB
- **CPU**: 1

### Database
- **Type**: Cloud SQL MySQL 8.0
- **Instance**: fincore-db
- **Region**: europe-west2
- **Connection**: Private (via Cloud SQL Proxy)
- **Tier**: db-n1-standard-1 (1 vCPU, 3.75 GB RAM)

### Deployment Commands
```powershell
# Build and deploy
.\deploy-to-gcp.ps1

# Check deployment status
gcloud run services describe fincore-npe-api --region=europe-west2

# View logs
gcloud run logs read fincore-npe-api --region=europe-west2 --limit=50
```

---

## Recent Updates

### March 31, 2026 Release

#### 🐛 Bug Fixes
1. **KYC Verifications CREATE** - Added backwards-compatible POST endpoint at `/api/kyc-verifications`
   - **Issue**: Frontend expecting base path, backend had `/submit` suffix
   - **Resolution**: Both paths now supported
   - **Status**: ✅ Deployed and verified

2. **Questionnaire Category** - Added `OTHER` to QuestionCategory enum
   - **Issue**: Frontend validation error "Name is null"
   - **Resolution**: Expanded enum values
   - **Status**: ✅ Fixed, awaiting deployment

#### 📊 Test Suite
- Fixed 12 test compilation errors
- All 662 tests now compile successfully
- 92% pass rate (608 passing)

#### 📄 Documentation
- Created comprehensive release notes
- Updated DDL schema alignment report
- Refreshed Postman collection guides
- Updated README with current status

#### 🗄️ Database
- Completed full DDL alignment analysis
- Verified all 4 core tables match specification
- Confirmed enhancements (BIGINT, proper constraints)
- All foreign keys properly configured with CASCADE/SET NULL

---

## API Versioning

**Current Version**: v1 (implied)  
**Base Path**: `/api`

Future versions will use explicit versioning:
- `/api/v2/users`
- `/api/v3/kyc-verifications`

---

## Rate Limiting

**Current**: No rate limiting enforced  
**Future**: 1000 requests/minute per IP (planned)

---

## Support & Resources

### Documentation
- **Swagger UI**: https://fincore-npe-api-994490239798.europe-west2.run.app/swagger-ui.html
- **OpenAPI Spec**: https://fincore-npe-api-994490239798.europe-west2.run.app/v3/api-docs
- **GitHub**: https://github.com/kasisheraz/userManagementApi
- **Confluence**: [This Page]

### Contact
- **Team**: FinCore Engineering
- **Environment**: NPE (Non-Production)
- **Updated**: March 31, 2026

---

## Quick Start Guide

### 1. Get JWT Token
```bash
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+447700900000"}'

curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+447700900000", "otpCode": "123456"}'
```

### 2. Create User
```bash
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+447700900123",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "roleIdentifier": 1
  }'
```

### 3. Submit KYC Verification
```bash
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/kyc-verifications \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "verificationLevel": "STANDARD",
    "status": "PENDING"
  }'
```

### 4. Create Questionnaire Question
```bash
curl -X POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/questionnaires \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "What is your annual income?",
    "questionCategory": "FINANCIAL",
    "displayOrder": 1,
    "status": "ACTIVE"
  }'
```

---

**End of Documentation**  
**Version**: 1.0.0  
**Last Updated**: March 31, 2026  
**Status**: ✅ Current
