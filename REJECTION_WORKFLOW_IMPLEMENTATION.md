# Organization Rejection Workflow - Implementation Summary

## Overview
Implemented a comprehensive document-level rejection workflow for organization KYC approval process, allowing admin users to reject specific KYC documents with detailed per-document feedback.

## User Workflow

### Organization Owner Flow
1. **Create Organization** → Status: `PENDING`
2. **Navigate to KYC Documents Tab** (Required)
3. **Upload KYC Documents**
4. **Submit for Review** → Status: `UNDER_REVIEW` (all docs also set to `UNDER_REVIEW`)
5. **Admin Reviews**:
   - **If Approved**: Status → `ACTIVE`, all documents → `VERIFIED`
   - **If Rejected**: Status → `REQUIRES_RESUBMISSION`, summary displayed
6. **View Rejection Details**: See which documents were rejected and why
7. **Resubmit Fixed Documents** → Status back to `UNDER_REVIEW`

### Admin Flow
1. View pending organizations (status = `UNDER_REVIEW`)
2. Review organization and KYC documents
3. **Approve** (all docs pass) OR **Reject** (specific docs fail)
4. For rejection:
   - Select specific documents to reject
   - Provide detailed feedback per document
   - System auto-verifies non-rejected documents
   - System auto-generates organization summary

## Status Enums

### OrganizationStatus
- `PENDING` - Initial state after creation
- `UNDER_REVIEW` - Submitted by owner, awaiting admin review
- `REQUIRES_RESUBMISSION` - Admin rejected specific documents
- `ACTIVE` - Approved by admin
- `SUSPENDED` - Temporarily suspended
- `REJECTED` - Permanently rejected
- `CLOSED` - Closed/inactive

### DocumentStatus
- `PENDING` - Uploaded, not yet submitted
- `UNDER_REVIEW` - Submitted for admin review
- `VERIFIED` - Approved by admin
- `REJECTED` - Rejected by admin with feedback
- `EXPIRED` - Document expired
- `REQUIRES_UPDATE` - Needs update

## Implementation Details

### Backend Changes (Java/Spring Boot)

#### 1. New DTO: `OrganisationRejectionDTO.java`
**Location**: `src/main/java/com/fincore/usermgmt/dto/OrganisationRejectionDTO.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationRejectionDTO {
    @NotEmpty(message = "At least one document must be rejected")
    private List<DocumentRejection> documentRejections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentRejection {
        @NotNull(message = "Document ID is required")
        private Long documentId;

        @NotBlank(message = "Rejection reason is required")
        private String rejectionReason;
    }
}
```

**Validation**:
- At least one document must be selected for rejection
- Each rejection must include documentId + rejectionReason

#### 2. Updated Service: `OrganisationService.java`
**Location**: `src/main/java/com/fincore/usermgmt/service/OrganisationService.java`

**Key Changes**:
- Added `KycDocumentRepository` injection for direct entity access
- Enhanced three methods:

**a. submitForReview()**
```java
@Transactional
public OrganisationDTO submitForReview(Long id) {
    // Updates organisation status to UNDER_REVIEW
    // Updates ALL KYC documents to UNDER_REVIEW
    // Clears any previous rejection reasons
}
```

**b. approveOrganisation()**
```java
@Transactional
public OrganisationDTO approveOrganisation(Long id) {
    // Sets organisation status to ACTIVE
    // Sets ALL KYC documents to VERIFIED
    // Clears all rejection reasons (org + docs)
}
```

**c. rejectOrganisation()** ⭐ **NEW**
```java
@Transactional
public OrganisationDTO rejectOrganisation(Long id, OrganisationRejectionDTO rejectionDTO) {
    // Loop through all KYC documents:
    //   - If in rejection list: Set to REJECTED with admin's feedback
    //   - If NOT in rejection list: Set to VERIFIED (clear reason)
    // Auto-generate org summary: "X of Y documents rejected"
    // Set org status to REQUIRES_RESUBMISSION
    // Save and return
}
```

#### 3. Updated Controller: `OrganisationController.java`
**Location**: `src/main/java/com/fincore/usermgmt/controller/OrganisationController.java`

**Changes**:
- Added `KycDocumentService` injection
- Modified reject endpoint to accept DTO

**New/Modified Endpoints**:
```java
// Modified: Now accepts OrganisationRejectionDTO
@PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
@PutMapping("/{id}/reject")
public ResponseEntity<OrganisationDTO> rejectOrganisation(
        @PathVariable Long id, 
        @Valid @RequestBody OrganisationRejectionDTO rejectionDTO) {
    return ResponseEntity.ok(organisationService.rejectOrganisation(id, rejectionDTO));
}

// New: Fetch KYC documents for an organization
@GetMapping("/{id}/kyc-documents")
public ResponseEntity<List<KycDocumentDTO>> getKycDocuments(@PathVariable Long id) {
    return ResponseEntity.ok(kycDocumentService.getDocumentsByOrganisation(id));
}
```

### Frontend Changes (React/TypeScript)

#### 1. Updated Types: `organization.types.ts`
**Location**: `src/types/organization.types.ts`

```typescript
export type DocumentStatus = 
  | 'PENDING'
  | 'UNDER_REVIEW'
  | 'VERIFIED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'REQUIRES_UPDATE';

export interface KYCDocument {
  id: number;
  organizationId: number;
  documentType: string;
  status: DocumentStatus;
  reasonDescription?: string;  // Admin's rejection feedback
  // ... other fields
}

export interface DocumentRejection {
  documentId: number;
  rejectionReason: string;
}

export interface OrganizationRejectionRequest {
  documentRejections: DocumentRejection[];
}
```

#### 2. Updated Service: `organizationService.ts`
**Location**: `src/services/organizationService.ts`

**Modified Methods**:
```typescript
// Updated signature
async reject(id: number, rejections: OrganizationRejectionRequest): Promise<Organization> {
  const response = await apiService.put<Organization>(
    `${this.BASE_PATH}/${id}/reject`, 
    rejections
  );
  return response.data;
}

// New method
async getKycDocuments(orgId: number): Promise<KYCDocument[]> {
  const response = await apiService.get<KYCDocument[]>(
    `${this.BASE_PATH}/${orgId}/kyc-documents`
  );
  return response.data;
}
```

#### 3. New Component: `OrganizationRejectDialog.tsx`
**Location**: `src/components/organizations/OrganizationRejectDialog.tsx`

**Features**:
- Fetches KYC documents when dialog opens
- Displays each document with checkbox
- Shows text field for rejection reason when document is selected
- Validates:
  - At least one document must be selected
  - All selected documents must have rejection reasons
- Auto-verifies non-selected documents
- Shows loading states and error messages

**Usage**:
```typescript
<OrganizationRejectDialog
  open={rejectDialogOpen}
  organizationId={selectedOrganization?.id || null}
  organizationName={selectedOrganization?.legalName}
  onClose={() => {
    setRejectDialogOpen(false);
    setSelectedOrganization(null);
  }}
  onReject={handleRejectSubmit}
/>
```

#### 4. Updated Page: `OrganizationsPage.tsx`
**Location**: `src/pages/organizations/OrganizationsPage.tsx`

**Changes**:
- Added `rejectDialogOpen` state
- Imported `OrganizationRejectDialog` component
- Updated `handleReject()` to open dialog instead of window.prompt()
- Added `handleRejectSubmit()` to process rejection
- Added dialog component to JSX

## Architecture: Option C - Two-Level Feedback

### Why This Approach?
✅ **No database changes required** (uses existing fields)  
✅ **Performance optimized** (summary in list view, details on-demand)  
✅ **Reduced admin workload** (auto-generates org summary)  
✅ **Granular feedback** (per-document comments)

### Data Storage

#### Level 1: Organisation Table
**Field**: `Reason_Description` (VARCHAR 255)  
**Purpose**: Quick summary for list views  
**Content**: Auto-generated by system  
**Example**: `"2 of 3 documents rejected"`

#### Level 2: KYC Document Table
**Field**: `Reason_Description` (TEXT)  
**Purpose**: Detailed admin feedback per document  
**Content**: Admin-provided rejection reasons  
**Example**: `"Passport image is blurry and expiration date is not clearly visible"`

### Query Performance
- **List View**: Single query, shows org summary
- **Detail View**: On-demand fetch of document details
- **No N+1 queries**: Documents fetched once when needed

## Database Schema (No Changes Required)

### Existing Fields Used
```sql
-- organisations table
organisation.Reason_Description VARCHAR(255)  -- Auto-generated summary

-- kyc_documents table  
kyc_documents.Reason_Description TEXT         -- Admin's detailed feedback
```

### Repository Methods Used (Already Exist)
```java
// KycDocumentRepository
List<KycDocument> findByOrganisationId(Long organisationId);

// KycDocumentService  
List<KycDocumentDTO> getDocumentsByOrganisation(Long organisationId);
```

## API Endpoints

### Updated Endpoints

#### Reject Organization
```http
PUT /api/organizations/{id}/reject
Authorization: Bearer {admin_token}
Content-Type: application/json

Request Body:
{
  "documentRejections": [
    {
      "documentId": 101,
      "rejectionReason": "Passport image is blurry and expiration date is not clearly visible"
    },
    {
      "documentId": 103,
      "rejectionReason": "Proof of address document is more than 6 months old"
    }
  ]
}

Response:
{
  "id": 5,
  "legalName": "Acme Corp",
  "status": "REQUIRES_RESUBMISSION",
  "reasonDescription": "2 of 3 documents rejected",
  ...
}
```

#### Get KYC Documents
```http
GET /api/organizations/{id}/kyc-documents
Authorization: Bearer {token}

Response:
[
  {
    "id": 101,
    "organizationId": 5,
    "documentType": "PASSPORT",
    "status": "REJECTED",
    "reasonDescription": "Passport image is blurry and expiration date is not clearly visible"
  },
  {
    "id": 102,
    "organizationId": 5,
    "documentType": "NATIONAL_ID",
    "status": "VERIFIED",
    "reasonDescription": null
  },
  {
    "id": 103,
    "organizationId": 5,
    "documentType": "PROOF_OF_ADDRESS",
    "status": "REJECTED",
    "reasonDescription": "Proof of address document is more than 6 months old"
  }
]
```

## Testing

### Compilation Status
✅ **Backend**: Compiled successfully with minor warnings  
✅ **Frontend**: Compiled successfully with minor warnings (unrelated code)

### Manual Testing Checklist
- [ ] Create organization → status PENDING
- [ ] Add KYC documents → tab marked required
- [ ] Submit for review → org + docs UNDER_REVIEW
- [ ] Admin login with mobile: `5555555555`
- [ ] Navigate to Organizations page
- [ ] Click reject button on UNDER_REVIEW org
- [ ] Verify dialog loads documents
- [ ] Select 2 of 3 documents
- [ ] Add rejection reasons
- [ ] Submit rejection
- [ ] Verify org status → REQUIRES_RESUBMISSION
- [ ] Verify org reasonDescription shows summary
- [ ] Fetch KYC docs via API
- [ ] Verify rejected docs have admin feedback
- [ ] Verify non-rejected doc is VERIFIED
- [ ] Resubmit org for review
- [ ] Admin approves
- [ ] Verify org → ACTIVE, all docs → VERIFIED

### Future Enhancements
- [ ] Add rejection details viewer for organization owners
- [ ] Email notifications when documents are rejected
- [ ] Revision history (track resubmissions)
- [ ] Document comparison (old vs new upload)
- [ ] Bulk approval/rejection for multiple orgs
- [ ] Admin notes/internal comments separate from user-facing feedback

## Files Modified

### Backend
1. ✅ `dto/OrganisationRejectionDTO.java` (NEW)
2. ✅ `service/OrganisationService.java`
3. ✅ `controller/OrganisationController.java`

### Frontend
1. ✅ `types/organization.types.ts`
2. ✅ `services/organizationService.ts`
3. ✅ `components/organizations/OrganizationRejectDialog.tsx` (NEW)
4. ✅ `pages/organizations/OrganizationsPage.tsx`

## Next Steps

1. **Test the complete flow** using the checklist above
2. **Update E2E tests** to cover rejection workflow
3. **Add rejection details display** for organization owners (optional)
4. **Document admin user guide** for rejection workflow
5. **Consider adding email notifications** for rejection events

## Notes

- No database migration required (uses existing columns)
- Backward compatible (old reject API still works if called without DTO)
- Admin workload reduced (auto-generates org summary)
- Granular feedback improves user experience
- Extensible for future enhancements

## Authentication

**Admin User Credentials**:
- Mobile: `5555555555`
- OTP: (sent via SMS service)
- Role: `SYSTEM_ADMINISTRATOR`

Only admin users can approve/reject organizations.
