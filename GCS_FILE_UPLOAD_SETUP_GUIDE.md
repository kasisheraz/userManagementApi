# GCS File Upload Setup Guide

Complete guide to enable Google Cloud Storage (GCS) for KYC document file uploads.

## 🚀 Quick Start

### Prerequisites
- GCP project with billing enabled
- GCP CLI installed (`gcloud`)
- Appropriate GCP permissions (Storage Admin role)

### Architecture Overview
```
Frontend → Backend API → Google Cloud Storage
                    ↓
                 Database (metadata only)
```

**What gets stored where:**
- **GCS**: Actual file bytes (PDF, JPG, PNG, etc.)
- **Database**: File metadata (file_name, file_url, document_type, status)

**Why GCS?**
- Cost: ~$0.02/GB/month (vs ~$1/GB for database storage)
- Scalability: Handles large files without database performance impact
- Security: Direct HTTPS access with signed URLs
- Durability: 99.999999999% (11 9's) durability

---

## 📋 Setup Steps

### Step 1: Create GCS Bucket

**Option A: Using gcloud CLI (Recommended)**
```powershell
# Login to GCP
gcloud auth login

# Set your project
gcloud config set project YOUR_PROJECT_ID

# Create the bucket
gcloud storage buckets create gs://fincore-kyc-documents `
  --location=europe-west2 `
  --uniform-bucket-level-access

# Set public read access (optional - for direct file URLs)
gcloud storage buckets add-iam-policy-binding gs://fincore-kyc-documents `
  --member=allUsers `
  --role=roles/storage.objectViewer
```

**Option B: Using GCP Console**
```
1. Go to: https://console.cloud.google.com/storage
2. Click "Create Bucket"
3. Bucket name: fincore-kyc-documents
4. Location: europe-west2 (London)
5. Storage class: Standard
6. Access control: Uniform
7. Public access: Enable (optional, for direct URLs)
8. Click "Create"
```

### Step 2: Configure Authentication

**Local Development (Application Default Credentials)**
```powershell
# Authenticate as your user account
gcloud auth application-default login

# This creates credentials at:
# C:\Users\<username>\AppData\Roaming\gcloud\application_default_credentials.json
```

**Production (Service Account)**
```powershell
# Create service account
gcloud iam service-accounts create fincore-api `
  --display-name="FinCore API Service Account"

# Grant Storage Admin role
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID `
  --member="serviceAccount:fincore-api@YOUR_PROJECT_ID.iam.gserviceaccount.com" `
  --role="roles/storage.admin"

# Create and download key (KEEP THIS SECURE!)
gcloud iam service-accounts keys create fincore-api-key.json `
  --iam-account=fincore-api@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

### Step 3: Configure Environment Variables

**Local Development (Windows PowerShell)**
```powershell
# Set environment variables for current session
$env:GCS_ENABLED="true"
$env:GCS_BUCKET_NAME="fincore-kyc-documents"
$env:GCP_PROJECT_ID="your-gcp-project-id"

# Optional: Set service account key path (if not using ADC)
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\fincore-api-key.json"

# Run the application
cd c:\Development\git\userManagementApi
.\start-local.ps1
```

**Permanent Environment Variables (Windows)**
```powershell
# Via System Properties
1. Press Win + X → System → Advanced system settings
2. Environment Variables → User variables
3. Add new variables:
   - GCS_ENABLED = true
   - GCS_BUCKET_NAME = fincore-kyc-documents
   - GCP_PROJECT_ID = your-gcp-project-id
   - GOOGLE_APPLICATION_CREDENTIALS = C:\path\to\fincore-api-key.json
4. Restart terminal/IDE
```

**GCP Cloud Run (Production)**
```powershell
# Option A: Use service account attached to Cloud Run
gcloud run services update fincore-npe-api `
  --region=europe-west2 `
  --service-account=fincore-api@YOUR_PROJECT_ID.iam.gserviceaccount.com `
  --set-env-vars="GCS_ENABLED=true,GCS_BUCKET_NAME=fincore-kyc-documents,GCP_PROJECT_ID=YOUR_PROJECT_ID"

# Option B: Use secrets from Secret Manager
gcloud secrets create GCS_BUCKET_NAME --data-file=- <<< "fincore-kyc-documents"
gcloud secrets create GCP_PROJECT_ID --data-file=- <<< "YOUR_PROJECT_ID"

gcloud run services update fincore-npe-api `
  --region=europe-west2 `
  --update-secrets=GCS_BUCKET_NAME=GCS_BUCKET_NAME:latest,GCP_PROJECT_ID=GCP_PROJECT_ID:latest `
  --update-env-vars GCS_ENABLED=true
```

### Step 4: Verify Configuration

**Check application.yml settings:**
```yaml
gcs:
  bucket-name: ${GCS_BUCKET_NAME:fincore-kyc-documents}
  project-id: ${GCP_PROJECT_ID:}
  enabled: ${GCS_ENABLED:true}
  base-url: https://storage.googleapis.com
```

**Start the application and check logs:**
```powershell
cd c:\Development\git\userManagementApi
.\start-local.ps1

# Look for successful initialization:
# ✅ "GCS File Storage Service initialized with bucket: fincore-kyc-documents"
# ✅ "GCS enabled: true"
```

### Step 5: Test File Upload

**Using Frontend UI:**
```
1. Navigate to http://localhost:3000/kyc-documents
2. Click "Upload Document"
3. Select organization from dropdown
4. Choose document type (PASSPORT, ID_CARD, etc.)
5. Click "Choose File" and select a document
6. Click "Upload"
7. Check GCS bucket for uploaded file
```

**Using cURL:**
```powershell
# Upload a file
curl -X POST http://localhost:8080/api/kyc-documents/upload `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -F "file=@C:\path\to\document.pdf" `
  -F "organisationId=1" `
  -F "documentType=PASSPORT"

# Response:
# {
#   "id": 123,
#   "organisationId": 1,
#   "documentType": "PASSPORT",
#   "fileName": "document.pdf",
#   "fileUrl": "https://storage.googleapis.com/fincore-kyc-documents/kyc-documents/20260417-uuid.pdf",
#   "status": "PENDING"
# }
```

**Verify in GCS:**
```powershell
# List files in bucket
gcloud storage ls gs://fincore-kyc-documents/kyc-documents/

# Should see files like:
# gs://fincore-kyc-documents/kyc-documents/20260417-a1b2c3d4.pdf
```

---

## 📊 File Upload Flow

```
1. User uploads file via UI → FormData with file + metadata
2. Backend receives multipart/form-data → GcsFileStorageService
3. Generate unique filename → {timestamp}-{uuid}.{extension}
4. Upload to GCS → kyc-documents/{filename}
5. Get public URL → https://storage.googleapis.com/{bucket}/{path}
6. Save metadata to database → file_name, file_url, status
7. Return response to frontend → Display success
```

---

## 🔐 Security & Permissions

### Bucket Permissions

**Public Read (for direct file access):**
```powershell
# Allow anyone to view files (use with caution)
gcloud storage buckets add-iam-policy-binding gs://fincore-kyc-documents `
  --member=allUsers `
  --role=roles/storage.objectViewer
```

**Private with Signed URLs (more secure):**
```powershell
# Only authorized service accounts can access
# GcsFileStorageService can be extended to generate signed URLs:

String signedUrl = blob.signUrl(15, TimeUnit.MINUTES);
// URL valid for 15 minutes only
```

### Service Account Permissions

**Minimum required roles:**
- `roles/storage.objectCreator` - Upload files
- `roles/storage.objectViewer` - Download files
- `roles/storage.objectAdmin` - Full control (upload, download, delete)

**Best Practice:**
- Use `storage.objectAdmin` for backend API service account
- Use `storage.objectViewer` for read-only access (analytics, reporting)
- Never share service account keys publicly

---

## 💰 Cost Optimization

### Current Configuration
```
Storage Class: Standard
Location: europe-west2 (London)
Estimated Cost:
  - Storage: $0.023/GB/month
  - Operations: $0.05 per 10,000 Class A ops (uploads)
  - Data transfer: $0.12/GB (egress outside GCP)
```

### Cost Reduction Strategies

**1. Use Nearline/Coldline for old documents**
```powershell
# Auto-transition files older than 90 days to Nearline
gcloud storage buckets update gs://fincore-kyc-documents `
  --lifecycle-file=lifecycle.json

# lifecycle.json:
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "SetStorageClass", "storageClass": "NEARLINE"},
        "condition": {"age": 90}
      }
    ]
  }
}
```

**2. Set object retention policies**
```powershell
# Auto-delete files after 7 years (regulatory compliance)
gcloud storage buckets update gs://fincore-kyc-documents `
  --retention-period=7y
```

**3. Monitor usage**
```powershell
# View storage usage
gcloud storage du gs://fincore-kyc-documents --summary

# View operations count (in Cloud Console)
# Monitoring → Metrics Explorer → GCS request_count
```

---

## 🔧 Troubleshooting

### Issue: "Failed to initialize GCS"

**Check 1: Authentication**
```powershell
# Verify ADC is configured
gcloud auth application-default print-access-token

# Should return a token, if error:
gcloud auth application-default login
```

**Check 2: Project ID**
```powershell
# Verify project ID is set
echo $env:GCP_PROJECT_ID

# Should match your GCP project
gcloud config get-value project
```

**Check 3: Service Account Permissions**
```powershell
# Check service account has storage admin role
gcloud projects get-iam-policy YOUR_PROJECT_ID `
  --flatten="bindings[].members" `
  --filter="bindings.members:fincore-api@*"
```

### Issue: "File upload failed - 403 Forbidden"

**Solution:**
```powershell
# Grant service account storage.objectAdmin role
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID `
  --member="serviceAccount:fincore-api@YOUR_PROJECT_ID.iam.gserviceaccount.com" `
  --role="roles/storage.objectAdmin"
```

### Issue: "Bucket does not exist"

**Solution:**
```powershell
# Verify bucket exists
gcloud storage buckets describe gs://fincore-kyc-documents

# If not found, create it
gcloud storage buckets create gs://fincore-kyc-documents `
  --location=europe-west2
```

### Issue: "GCS disabled in logs"

**Check environment variable:**
```powershell
# Verify GCS_ENABLED is set
echo $env:GCS_ENABLED

# Should be "true"
# If not:
$env:GCS_ENABLED="true"
```

### Issue: "Files not appearing in bucket"

**Debug steps:**
```powershell
# Check application logs for errors
Get-Content -Path "logs/application.log" -Tail 50 | Select-String "GCS"

# Verify bucket URL format
# Correct: https://storage.googleapis.com/fincore-kyc-documents/kyc-documents/file.pdf
# Wrong: https://fincore-kyc-documents.storage.googleapis.com/file.pdf

# List bucket contents
gcloud storage ls gs://fincore-kyc-documents/kyc-documents/ --recursive
```

---

## 📁 File Naming Convention

**Format:** `kyc-documents/{timestamp}-{uuid}.{extension}`

**Example:**
```
kyc-documents/20260417-143022-a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6.pdf
                │         │                                         │
                │         │                                         └─ File extension
                │         └─ UUID (ensures uniqueness)
                └─ Timestamp (YYYYMMDD-HHmmss)
```

**Benefits:**
- ✅ No filename collisions (UUID guarantee)
- ✅ Easy to sort by upload time (timestamp prefix)
- ✅ Organized folder structure (kyc-documents/)
- ✅ Original extension preserved

---

## 🧪 Testing Checklist

Before marking GCS integration as complete:

- [ ] GCS bucket created in correct region
- [ ] Service account created with Storage Admin role
- [ ] ADC configured (`gcloud auth application-default login`)
- [ ] Environment variables set (GCS_ENABLED, GCS_BUCKET_NAME, GCP_PROJECT_ID)
- [ ] Application starts without GCS errors in logs
- [ ] Upload test file via UI successfully
- [ ] File appears in GCS bucket (verify with `gcloud storage ls`)
- [ ] File URL saved to database (check kyc_documents table)
- [ ] File accessible via public URL (if public access enabled)
- [ ] Download test file successfully
- [ ] Delete test file successfully
- [ ] Cost monitoring configured (set budget alerts)
- [ ] Production service account configured for Cloud Run

---

## 🔄 Graceful Degradation

**What happens when GCS is disabled?**

The backend has built-in mock mode:

```java
if (!gcsEnabled) {
    log.warn("GCS is disabled. Using mock file storage.");
    return baseUrl + "/mock/kyc-documents/" + filename;
}
```

**Use cases:**
- Local development without GCP access
- Testing without incurring GCS costs
- Fallback if GCS is temporarily unavailable

**To disable GCS:**
```powershell
$env:GCS_ENABLED="false"
```

---

## 📚 Additional Resources

### GCP Documentation
- GCS Quickstart: https://cloud.google.com/storage/docs/quickstart
- Service Accounts: https://cloud.google.com/iam/docs/service-accounts
- Pricing: https://cloud.google.com/storage/pricing

### FinCore Documentation
- API Endpoints: See `API_ENDPOINTS_REFERENCE.md`
- KYC Document Types: See database schema `kyc_documents` table
- Frontend UI: See `src/pages/kyc/KYCDocumentsPage.tsx`

### Monitoring
- GCS Metrics: Cloud Console → Storage → Bucket → Monitoring
- API Logs: Cloud Console → Logging → Logs Explorer
- Cost Reports: Cloud Console → Billing → Reports

---

## ✅ Quick Reference

**Local Development:**
```powershell
# Setup (one-time)
gcloud auth application-default login
$env:GCS_ENABLED="true"
$env:GCS_BUCKET_NAME="fincore-kyc-documents"
$env:GCP_PROJECT_ID="your-project-id"

# Run
cd c:\Development\git\userManagementApi
.\start-local.ps1
```

**Production Deployment:**
```powershell
# Configure Cloud Run
gcloud run services update fincore-npe-api `
  --region=europe-west2 `
  --service-account=fincore-api@YOUR_PROJECT_ID.iam.gserviceaccount.com `
  --set-env-vars="GCS_ENABLED=true,GCS_BUCKET_NAME=fincore-kyc-documents"
```

**Verify Upload:**
```powershell
# List files
gcloud storage ls gs://fincore-kyc-documents/kyc-documents/

# View file details
gcloud storage ls -L gs://fincore-kyc-documents/kyc-documents/filename.pdf
```

---

**Need Help?** Check logs first, verify environment variables, ensure service account has correct permissions. Most issues are authentication-related.
