# Postman Collection Usage Guide - Phase 2

## Overview
The updated `postman_collection_phase2.json` now uses the correct OTP-based authentication flow and proper data types for all Phase 2 endpoints.

## Changes Made

### 1. ✅ Authentication Flow Updated
- **Old**: Username/password at `/api/auth/login` (doesn't exist)
- **New**: OTP-based flow with two steps:
  1. Request OTP: `/api/auth/request-otp`
  2. Verify OTP: `/api/auth/verify-otp`

### 2. ✅ Address Type Codes Fixed
- **Old**: `"typeCode": "BUSINESS"` (string - caused 400 error)
- **New**: `"typeCode": 2` (integer - correct format)

Type Code Values:
- `1` = Residential
- `2` = Business
- `3` = Registered
- `4` = Correspondence
- `5` = Postal

### 3. ✅ Auto-Save Variables
The collection now automatically saves:
- OTP code in `{{otpCode}}` variable (from Request OTP response)
- JWT token in `{{authToken}}` variable (from Verify OTP response)
- IDs for created resources (organisation, address, kyc document)

---

## How to Use

### Step 1: Import the Collection
1. Open Postman
2. Click **Import**
3. Select `postman_collection_phase2.json`
4. Click **Import**

### Step 2: Authenticate (First Time)

#### Option A: Admin User
1. Run **"1. Request OTP - Admin"**
   - Phone: `+1234567890`
   - Response will include `devOtp` field with the OTP code
   - OTP is auto-saved to `{{otpCode}}` variable

2. Run **"2. Verify OTP - Admin"**
   - Uses the saved `{{otpCode}}` automatically
   - JWT token is auto-saved to `{{authToken}}` variable
   - All subsequent requests will use this token

#### Option B: Other Users
Choose one of:
- **Compliance Officer**: `+1234567891`
- **Operational Staff**: `+1234567892`

Run their respective "Request OTP" and "Verify OTP" requests.

### Step 3: Test Phase 2 Endpoints

Once authenticated (token saved), you can run any request in these folders:
- **Addresses** - Create, read, update, delete addresses
- **Organisations** - Manage organisations
- **KYC Documents** - Upload and verify documents

All requests automatically use `{{authToken}}` in the Authorization header.

---

## Request Examples

### Create Address (Registered)
```json
POST {{baseUrl}}/api/addresses
Authorization: Bearer {{authToken}}

{
    "typeCode": 3,
    "addressLine1": "123 High Street",
    "addressLine2": "Floor 3",
    "postalCode": "EC1A 1BB",
    "stateCode": "Greater London",
    "city": "London",
    "country": "United Kingdom",
    "statusDescription": "ACTIVE"
}
```

### Create Address (Business)
```json
POST {{baseUrl}}/api/addresses
Authorization: Bearer {{authToken}}

{
    "typeCode": 2,
    "addressLine1": "456 Business Park",
    "addressLine2": "Unit 7",
    "postalCode": "M1 2AB",
    "stateCode": "Greater Manchester",
    "city": "Manchester",
    "country": "United Kingdom",
    "statusDescription": "ACTIVE"
}
```

### Create Organisation with Addresses
```json
POST {{baseUrl}}/api/organisations
Authorization: Bearer {{authToken}}

{
    "userIdentifier": 1,
    "registrationNumber": "REG00012345",
    "sicCode": "64999",
    "legalName": "Test Money Services Ltd",
    "businessName": "Test Remittance",
    "organisationTypeDescription": "LTD",
    "businessDescription": "International money transfer services",
    "incorporationDate": "2020-01-15",
    "countryOfIncorporation": "United Kingdom",
    "hmrcMlrNumber": "XMLR00999888",
    "fcaNumber": "FRN999888",
    "numberOfBranches": "3",
    "numberOfAgents": "10",
    "companyNumber": "CN99988877",
    "websiteAddress": "https://test-remittance.com",
    "primaryRemittanceDestinationCountry": "India",
    "monthlyTurnoverRange": "100000-500000",
    "numberOfIncomingTransactions": "1000",
    "numberOfOutgoingTransactions": "900",
    "statusDescription": "PENDING",
    "registeredAddress": {
        "typeCode": 3,
        "addressLine1": "789 Registry Lane",
        "addressLine2": "Suite 100",
        "postalCode": "EC2A 2BB",
        "stateCode": "Greater London",
        "city": "London",
        "country": "United Kingdom",
        "statusDescription": "ACTIVE"
    },
    "businessAddress": {
        "typeCode": 2,
        "addressLine1": "101 Commerce Road",
        "addressLine2": "Building A",
        "postalCode": "EC3A 3CC",
        "stateCode": "Greater London",
        "city": "London",
        "country": "United Kingdom",
        "statusDescription": "ACTIVE"
    }
}
```

---

## Testing Workflow

### Quick Test Sequence

1. **Authenticate**
   - Run: `1. Request OTP - Admin`
   - Run: `2. Verify OTP - Admin`

2. **Create Resources**
   - Run: `Create Address - Registered`
   - Run: `Create Address - Business`
   - Run: `Create Organisation`
   - Run: `Create KYC Document`

3. **Read Resources**
   - Run: `Get All Addresses`
   - Run: `Get All Organisations (Paginated)`
   - Run: `Get All KYC Documents (Paginated)`

4. **Update Resources**
   - Run: `Update Address`
   - Run: `Update Organisation`
   - Run: `Verify KYC Document (Approve)`

5. **Delete Resources** (Optional)
   - Run: `Delete Address`
   - Run: `Delete Organisation`
   - Run: `Delete KYC Document`

---

## Available Test Users

| User Type | Phone Number | Role | Features |
|-----------|-------------|------|----------|
| Admin | `+1234567890` | SYSTEM_ADMINISTRATOR | Full access to all endpoints |
| Compliance | `+1234567891` | COMPLIANCE_OFFICER | KYC verification, read access |
| Staff | `+1234567892` | OPERATIONAL_STAFF | Operational access |

---

## Variables Reference

The collection uses these variables (auto-managed):

| Variable | Description | Set By |
|----------|-------------|--------|
| `{{baseUrl}}` | API base URL | Collection (default: http://localhost:8080) |
| `{{authToken}}` | JWT authentication token | Verify OTP response |
| `{{otpCode}}` | OTP code for verification | Request OTP response |
| `{{organisationId}}` | Created organisation ID | Create Organisation response |
| `{{addressId}}` | Created address ID | Create Address response |
| `{{kycDocumentId}}` | Created KYC document ID | Create KYC Document response |

You can view/edit these in Postman:
- Click collection name
- Go to **Variables** tab

---

## Console Output

Each request logs useful information to the Postman Console:
- OTP codes (in dev mode)
- JWT tokens
- Created resource IDs
- User information

To view console:
- **View** → **Show Postman Console** (or `Ctrl+Alt+C`)

---

## Troubleshooting

### Issue: OTP not showing in response
**Solution**: The `devOtp` field only appears in non-production environments (local, npe, test, dev, h2). If you don't see it, check the server terminal logs for the OTP.

### Issue: 403 Forbidden
**Solution**: 
1. Make sure you ran "Verify OTP" request successfully
2. Check that `{{authToken}}` variable is set (View → Variables)
3. Token may have expired - request and verify a new OTP

### Issue: 400 Bad Request on Create Address
**Solution**: Ensure `typeCode` is an integer (e.g., `2`), not a string (e.g., `"BUSINESS"`)

### Issue: Variables not saving
**Solution**: 
1. Check the **Tests** tab of the request - it should have JavaScript to save variables
2. Ensure the response returns the expected field names
3. View Postman Console to see what's being saved

---

## Testing Tips

1. **Use Collection Runner** for automated testing:
   - Click collection name → **Run**
   - Select requests to run
   - Click **Run Collection**

2. **Save Responses** for reference:
   - Click **Save Response** → **Save as Example**

3. **Environment Variables** (optional):
   - Create environment for different setups (local, dev, staging)
   - Override `baseUrl` per environment

4. **Pre-request Scripts** (advanced):
   - Auto-request OTP if token expired
   - Generate random test data

---

## Complete Test Checklist

- [ ] Authentication flow works (Request OTP → Verify OTP)
- [ ] Create Address - Registered (typeCode: 3)
- [ ] Create Address - Business (typeCode: 2)
- [ ] Get All Addresses returns results
- [ ] Get Address by ID works
- [ ] Get Addresses by Type works
- [ ] Get Addresses by Country works
- [ ] Update Address succeeds
- [ ] Create Organisation with embedded addresses
- [ ] Get All Organisations returns results
- [ ] Get Organisation by ID works
- [ ] Search Organisations works
- [ ] Update Organisation succeeds
- [ ] Update Organisation Status (PENDING → ACTIVE)
- [ ] Create KYC Document
- [ ] Get KYC Documents by Organisation
- [ ] Verify KYC Document (Approve)
- [ ] Get Document Counts by Status

---

## Next Steps

1. ✅ Import updated collection into Postman
2. ✅ Ensure application is running: `$env:MYSQL_PASSWORD = "abc123"; mvn spring-boot:run "-Dspring-boot.run.profiles=local"`
3. ✅ Run authentication flow first (Request OTP → Verify OTP)
4. ✅ Test all endpoints in sequence
5. ✅ Check responses and console logs

Happy testing! 🚀
