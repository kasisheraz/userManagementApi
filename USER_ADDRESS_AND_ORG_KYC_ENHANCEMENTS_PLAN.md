# User Address & Organization Enhancement Plan

**Date:** April 11, 2026  
**Status:** Implementation Ready

## Overview

This document outlines the comprehensive plan for three key enhancements to the user management and organization onboarding systems:

1. **Full Address Capture for Users** - Expand user address fields from FK-only to full address object capture
2. **Organization Address Spacing Fix** - Improve UI layout for organization address fields
3. **KYC Document Upload in Organization Creation** - Add KYC document upload capability during organization creation

---

## 1. User Address Enhancement

### Current State
- Users table has `Residential_Address_Identifier` and `Postal_Address_Identifier` (FK to Address table)
- UI only captures address ID numbers
- No ability to create/manage addresses during user creation/editing

### Target State
- Users can create full addresses (with all fields) during user creation/editing
- Checkbox option: "Postal address same as residential address"
- Both residential and postal addresses captured with all fields from Address table
- Address entities created automatically and linked to user

### Database Changes
**No schema changes required** - Address table already exists with all necessary fields:
- Address_Identifier (PK)
- Type_Code (1=Residential, 5=Postal)
- Address_Line1, Address_Line2
- City, State_Code, Postal_Code, Country
- Status_Description, Created_Datetime, Created_By

### Backend API Changes

#### 1.1 DTOs - Create New Address DTOs for Users
**File:** `src/main/java/com/fincore/usermgmt/dto/AddressDTO.java` (NEW)
```java
- id
- typeCode
- addressLine1
- addressLine2
- city
- stateCode
- postalCode
- country
- statusDescription
- createdDatetime
```

**File:** `src/main/java/com/fincore/usermgmt/dto/CreateAddressDTO.java` (NEW)
```java
- typeCode
- addressLine1 (required)
- addressLine2
- city
- stateCode
- postalCode
- country (required)
```

#### 1.2 Update User DTOs
**Files to modify:**
- `UserCreateDTO.java` - Replace ID fields with nested CreateAddressDTO objects
- `UserUpdateDTO.java` - Replace ID fields with nested CreateAddressDTO objects
- `UserDTO.java` - Replace ID fields with nested AddressDTO objects

**Changes:**
```java
// OLD:
private Long residentialAddressIdentifier;
private Long postalAddressIdentifier;

// NEW:
private CreateAddressDTO residentialAddress;
private CreateAddressDTO postalAddress;
```

#### 1.3 Services
**File:** `UserService.java`
- Add AddressService injection
- Update `createUser()` to:
  1. Create residential address if provided
  2. Create postal address if provided (or copy residential)
  3. Link address IDs to user entity
- Update `updateUser()` similarly

**New File:** `AddressService.java`
```java
- createAddress(CreateAddressDTO, userId)
- updateAddress(Long id, CreateAddressDTO)
- getAddressById(Long id)
- deleteAddress(Long id)
```

#### 1.4 Repositories
**New File:** `AddressRepository.java`
```java
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCreatedBy(Long userId);
    Optional<Address> findByIdAndCreatedBy(Long id, Long userId);
}
```

#### 1.5 Mappers
**Update:** `UserMapper.java`
- Add address mapping methods
- Handle nested address conversions

**New:** `AddressMapper.java`
```java
@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDTO toAddressDTO(Address address);
    Address toAddress(CreateAddressDTO dto);
    void updateAddressFromDto(CreateAddressDTO dto, @MappingTarget Address address);
}
```

#### 1.6 Controllers
**File:** `UserController.java` - No changes to endpoints, DTO structure changes handled internally

**New File:** `AddressController.java` (Optional - for address CRUD)
```java
@RestController
@RequestMapping("/api/addresses")
- GET /{id}
- POST /
- PUT /{id}
- DELETE /{id}
```

### Frontend UI Changes

#### 1.7 Types
**File:** `src/types/user.types.ts`
```typescript
// Add Address interface (if not exists globally)
export interface Address {
  id?: number;
  typeCode: number;
  addressLine1: string;
  addressLine2?: string;
  city?: string;
  stateCode?: string;
  postalCode?: string;
  country: string;
  statusDescription?: string;
}

// Update User interface
export interface User {
  // ... existing fields
  residentialAddress?: Address;
  postalAddress?: Address;
  // Remove: residentialAddressIdentifier, postalAddressIdentifier
}

// Update DTOs
export interface CreateUserDTO {
  // ... existing fields
  residentialAddress?: Address;
  postalAddress?: Address;
}
```

#### 1.8 Components
**File:** `src/components/users/UserForm.tsx`

**Changes:**
1. Add state for addresses:
   ```typescript
   const [residentialAddress, setResidentialAddress] = useState<Address | null>(null);
   const [postalAddress, setPostalAddress] = useState<Address | null>(null);
   const [sameAsResidential, setSameAsResidential] = useState(false);
   ```

2. Add AddressForm components for both addresses
3. Add checkbox: "Postal address same as residential"
4. When checkbox checked, copy residential to postal
5. Update form submission to include full address objects

**New sections in form:**
```tsx
{/* Residential Address Section */}
<Grid item xs={12}>
  <Typography variant="subtitle1">Residential Address</Typography>
</Grid>
<AddressForm
  address={formData.residentialAddress}
  typeCode={1} // Residential
  onDataChange={(data, valid) => handleAddressChange('residential', data, valid)}
  required={false}
/>

{/* Postal Address Section */}
<Grid item xs={12}>
  <FormControlLabel
    control={
      <Checkbox
        checked={sameAsResidential}
        onChange={(e) => handleSameAsResidential(e.target.checked)}
      />
    }
    label="Postal address same as residential address"
  />
</Grid>

{!sameAsResidential && (
  <>
    <Grid item xs={12}>
      <Typography variant="subtitle1">Postal Address</Typography>
    </Grid>
    <AddressForm
      address={formData.postalAddress}
      typeCode={5} // Postal
      onDataChange={(data, valid) => handleAddressChange('postal', data, valid)}
      required={false}
    />
  </>
)}
```

### Testing Changes

#### 1.9 Backend Tests

**New Test Files:**
- `AddressServiceTest.java`
- `AddressRepositoryTest.java`
- `AddressMapperTest.java`
- `AddressControllerTest.java` (if controller added)

**Update Test Files:**
- `UserServiceTest.java` - Update to test address creation
- `UserControllerTest.java` - Update DTOs in tests
- `UserMapperTest.java` - Add address mapping tests
- `UserCreateDTOTest.java` - Update validation tests
- `UserUpdateDTOTest.java` - Update validation tests
- Integration tests for end-to-end address flows

#### 1.10 Frontend Tests

**Update Test Files:**
- `UserForm.test.tsx` - Add address form tests
- Add tests for "same as residential" checkbox
- Test address validation
- Test form submission with addresses

---

## 2. Organization Address Spacing Fix

### Current Issue
- Address fields in OrganizationForm lack proper spacing
- Fields appear cramped/unclear

### Solution
**File:** `src/components/organizations/OrganizationForm.tsx`

**Tab 6: Addresses section** - Add proper spacing with:
1. Box wrapper with padding
2. Dividers between address sections
3. Consistent Grid spacing (spacing={3})
4. Typography headers with margins

**Changes:**
```tsx
<TabPanel value={currentTab} index={6}>
  <Typography variant="h6" gutterBottom>
    Organization Addresses
  </Typography>
  <Divider sx={{ mb: 3 }} />
  
  {/* Registered Address */}
  <Box sx={{ mb: 4 }}>
    <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold' }}>
      Registered Address *
    </Typography>
    <Grid container spacing={3}>
      <AddressForm
        typeCode={3}
        address={formData.registeredAddress}
        onDataChange={handleAddressChange('registered')}
        required={true}
      />
    </Grid>
  </Box>
  
  <Divider sx={{ my: 3 }} />
  
  {/* Business Address */}
  <Box sx={{ mb: 4 }}>
    <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold' }}>
      Business Address
    </Typography>
    <Grid container spacing={3}>
      <AddressForm
        typeCode={2}
        address={formData.businessAddress}
        onDataChange={handleAddressChange('business')}
        required={false}
      />
    </Grid>
  </Box>
  
  <Divider sx={{ my: 3 }} />
  
  {/* Correspondence Address */}
  <Box sx={{ mb: 4 }}>
    <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold' }}>
      Correspondence Address
    </Typography>
    <Grid container spacing={3}>
      <AddressForm
        typeCode={4}
        address={formData.correspondenceAddress}
        onDataChange={handleAddressChange('correspondence')}
        required={false}
      />
    </Grid>
  </Box>
</TabPanel>
```

---

## 3. KYC Document Upload in Organization Creation

### Current State
- KYC documents handled separately after organization creation
- No document upload during organization creation flow

### Target State
- Add "KYC Documents" tab to OrganizationForm
- Allow multiple document uploads during creation
- Documents linked to organization after creation

### Database Changes
**No schema changes required** - KYC_Documents table already exists:
- Document_Identifier (PK)
- Reference_Identifier (FK to Organisation_Identifier)
- Document_Type_Description
- File_Name
- File_URL
- Status_Description
- Created_Datetime, Created_By

### Backend API Changes

#### 3.1 DTOs
**New File:** `src/main/java/com/fincore/usermgmt/dto/CreateKycDocumentDTO.java`
```java
- referenceIdentifier (Organization ID)
- documentTypeDescription (CERTIFICATE_OF_INCORPORATION, PROOF_OF_ADDRESS, etc.)
- fileName
- fileUrl
- statusDescription (default: PENDING)
```

**File:** `src/main/java/com/fincore/usermgmt/dto/CreateOrganizationDTO.java`
**Add field:**
```java
private List<CreateKycDocumentDTO> kycDocuments;
```

#### 3.2 Services
**New File:** `KycDocumentService.java`
```java
- createDocument(CreateKycDocumentDTO, userId)
- uploadDocument(Long orgId, MultipartFile file, String docType)
- getDocumentsByOrganization(Long orgId)
- updateDocumentStatus(Long docId, String status)
- deleteDocument(Long docId)
```

**Update:** `OrganizationService.java`
- Inject KycDocumentService
- After creating organization, create associated KYC documents
- Transaction management to rollback if document creation fails

#### 3.3 Controllers
**New File:** `KycDocumentController.java`
```java
@RestController
@RequestMapping("/api/organizations/{orgId}/kyc-documents")
- POST / - Upload document
- GET / - List documents for org
- GET /{id} - Get specific document
- PUT /{id}/status - Update status
- DELETE /{id} - Delete document
```

#### 3.4 File Storage
**Implementation approach:**
1. Local storage for development (uploads/ directory)
2. Google Cloud Storage for production
3. Return file URL after upload

**New Service:** `FileStorageService.java`
```java
- uploadFile(MultipartFile file, String folder)
- deleteFile(String fileUrl)
- getFile(String fileUrl)
```

### Frontend UI Changes

#### 3.5 Types
**Update:** `src/types/organization.types.ts`
```typescript
export interface KYCDocumentUpload {
  documentType: string; // CERTIFICATE_OF_INCORPORATION, etc.
  file?: File;
  fileName?: string;
  fileUrl?: string;
  status?: string;
}

export interface CreateOrganizationDTO {
  // ... existing fields
  kycDocuments?: KYCDocumentUpload[];
}
```

#### 3.6 Components

**Update:** `src/components/organizations/OrganizationForm.tsx`

**Add new tab (Tab 8): KYC Documents**
```tsx
<Tab label="KYC Documents" />

<TabPanel value={currentTab} index={8}>
  <Typography variant="h6" gutterBottom>
    KYC & Compliance Documents
    <Chip label="Optional" color="info" size="small" sx={{ ml: 1 }} />
  </Typography>
  <Divider sx={{ mb: 3 }} />
  
  <KycDocumentUpload
    documents={formData.kycDocuments || []}
    onDocumentsChange={handleDocumentsChange}
  />
</TabPanel>
```

**New Component:** `src/components/organizations/KycDocumentUpload.tsx`
```tsx
// Features:
- Multiple file upload with drag-and-drop
- Document type selection for each file
- File preview/list
- Remove uploaded files
- Validation (file size, types)
- Display document type options:
  * CERTIFICATE_OF_INCORPORATION
  * PROOF_OF_ADDRESS
  * MEMORANDUM_OF_ASSOCIATION
  * ARTICLES_OF_ASSOCIATION
  * SHAREHOLDERS_REGISTER
  * DIRECTORS_REGISTER
  * TAX_REGISTRATION
  * REGULATORY_LICENSE
  * OTHER
```

#### 3.7 File Upload Handling

**API Service:** `src/services/organizationService.ts`
- Update `createOrganization` to handle FormData for file uploads
- Use multipart/form-data content type
- Send organization data + files

```typescript
export const createOrganization = async (
  data: CreateOrganizationDTO,
  files?: File[]
): Promise<Organization> => {
  const formData = new FormData();
  formData.append('organization', JSON.stringify(data));
  
  // Append files
  files?.forEach((file, index) => {
    formData.append(`documents[${index}].file`, file);
    formData.append(`documents[${index}].documentType`, data.kycDocuments![index].documentType);
  });
  
  const response = await apiClient.post('/organizations', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  
  return response.data;
};
```

### Testing Changes

#### 3.8 Backend Tests
**New Test Files:**
- `KycDocumentServiceTest.java`
- `KycDocumentControllerTest.java`
- `FileStorageServiceTest.java`

**Update Test Files:**
- `OrganizationServiceTest.java` - Test KYC document creation with org
- `OrganizationControllerTest.java` - Test multipart file upload

#### 3.9 Frontend Tests
**New Test Files:**
- `KycDocumentUpload.test.tsx`

**Update Test Files:**
- `OrganizationForm.test.tsx` - Test document upload tab
- Test file validation
- Test multiple file handling

---

## Implementation Order

### Phase 1: Backend Foundation (Days 1-2)
1. Create Address DTOs and service
2. Create AddressRepository and mapper
3. Update User DTOs to use nested addresses
4. Update UserService to handle address creation
5. Write unit tests for address functionality

### Phase 2: Backend KYC & File Storage (Days 2-3)
6. Implement FileStorageService
7. Create KYC Document DTOs and service
8. Create KycDocumentRepository
9. Update OrganizationService for KYC documents
10. Create KycDocumentController
11. Write unit tests for KYC functionality

### Phase 3: Frontend User Address (Days 3-4)
12. Update user types
13. Modify UserForm to include address sections
14. Add "same as residential" checkbox logic
15. Update form validation
16. Write component tests

### Phase 4: Frontend Organization Fixes (Day 4)
17. Fix Organization address tab spacing
18. Create KycDocumentUpload component
19. Add KYC tab to OrganizationForm
20. Update organization service for file upload
21. Write component tests

### Phase 5: Integration & E2E Testing (Day 5)
22. Integration tests (backend)
23. E2E tests (frontend)
24. Manual testing end-to-end
25. Bug fixes and refinements

### Phase 6: Documentation & Deployment (Day 6)
26. Update API documentation (Swagger)
27. Update Confluence documentation
28. Create migration guide
29. Deploy to NPE environment
30. Validation and sign-off

---

## API Endpoints Summary

### New/Updated Endpoints

#### Address Management
```
POST   /api/addresses              - Create address
GET    /api/addresses/{id}         - Get address by ID
PUT    /api/addresses/{id}         - Update address
DELETE /api/addresses/{id}         - Delete address
```

#### User Management (Updated)
```
POST   /api/users                  - Create user (now accepts nested address objects)
PUT    /api/users/{id}             - Update user (now accepts nested address objects)
GET    /api/users/{id}             - Get user (now returns nested address objects)
```

#### KYC Documents
```
POST   /api/organizations/{orgId}/kyc-documents              - Upload KYC document
GET    /api/organizations/{orgId}/kyc-documents              - List org documents
GET    /api/organizations/{orgId}/kyc-documents/{id}         - Get document
PUT    /api/organizations/{orgId}/kyc-documents/{id}/status  - Update status
DELETE /api/organizations/{orgId}/kyc-documents/{id}         - Delete document
```

#### Organization (Updated)
```
POST   /api/organizations          - Create org (now accepts KYC documents)
```

---

## Validation Rules

### Address Validation
- addressLine1: Required, max 100 chars
- country: Required, max 50 chars
- addressLine2: Optional, max 100 chars
- city: Optional, max 50 chars
- stateCode: Optional, max 20 chars
- postalCode: Optional, max 20 chars

### KYC Document Validation
- File size: Max 10 MB per file
- Allowed types: PDF, JPG, JPEG, PNG
- Document type: Required (from predefined list)
- At least one document recommended for organization

---

## Database Indexes

Already exist, no new indexes needed:
- `idx_users_phone` on Users(Phone_Number)
- `idx_address_type` on Address(Type_Code)
- `idx_kyc_reference` on KYC_Documents(Reference_Identifier)
- `idx_kyc_status` on KYC_Documents(Status_Description)

---

## Rollback Plan

### If issues arise:
1. **Backend:** Revert to previous DTOs with ID-only fields
2. **Frontend:** Revert to previous UserForm with ID input fields
3. **Database:** No rollback needed (no schema changes)
4. **KYC:** Feature flag to disable document upload in org creation

---

## Success Criteria

### User Address Enhancement
- ✅ Users can create full residential address during user creation
- ✅ Users can create full postal address during user creation
- ✅ "Same as residential" checkbox works correctly
- ✅ Addresses are persisted to Address table
- ✅ User record correctly references created addresses
- ✅ All tests pass (unit, integration, E2E)

### Organization Address Spacing
- ✅ Address fields have proper visual spacing
- ✅ Clear section headers for each address type
- ✅ Improved UX/readability

### KYC Document Upload
- ✅ Multiple documents can be uploaded during org creation
- ✅ File upload works (drag-drop and click)
- ✅ Document types can be selected
- ✅ Files are stored correctly (local/cloud)
- ✅ Documents are linked to organization
- ✅ All tests pass

---

## Security Considerations

1. **File Upload Security:**
   - Validate file types (whitelist)
   - Scan for malware
   - Limit file sizes
   - Store files outside web root
   - Generate unique file names

2. **Access Control:**
   - Users can only access their own addresses
   - Only authorized users can upload/view KYC documents
   - Role-based access for document verification

3. **Data Validation:**
   - Server-side validation for all inputs
   - Sanitize file names
   - Validate address data formats

---

## Performance Considerations

1. **Address Creation:**
   - Consider bulk address operations
   - Optimize database transactions
   - Cache frequently accessed addresses

2. **File Storage:**
   - Async file upload processing
   - CDN for file delivery
   - Image compression for uploads
   - Consider pagination for document lists

---

## Questions & Decisions

### Resolved:
1. **Q:** Should users be able to edit addresses after creation?
   **A:** Yes, via address update endpoint

2. **Q:** What if postal address is same as residential?
   **A:** Create two separate records with same data for data integrity

3. **Q:** File storage location?
   **A:** Local for dev, GCS for prod

### Open:
1. Address history/audit trail needed?
2. Document encryption at rest required?
3. Document retention policy?

---

## Related Documentation

- [Address Table Schema](./complete-entity-schema.sql)
- [KYC_Documents Table Schema](./src/main/resources/schema.sql)
- [Organization API Documentation](./CONFLUENCE_API_DOCUMENTATION.md)
- [Testing Guide](./API_TESTING_GUIDE.md)

---

**Plan Created By:** GitHub Copilot  
**Reviewed By:** [Pending]  
**Approved By:** [Pending]  
**Implementation Start Date:** [TBD]
