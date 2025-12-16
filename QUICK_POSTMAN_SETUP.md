# Quick Setup - Postman Testing for Cloud Run

## 📋 Complete Instructions

### **1. Install Prerequisites**
```powershell
# Install Node.js (if not already installed)
# Download: https://nodejs.org/

# Install Newman (command-line Postman)
npm install -g newman
```

### **2. Files Ready in Your Project**
✅ `postman_collection.json` - All API test cases
✅ `postman_environment_cloud.json` - Cloud Run environment config
✅ `test-cloud-deployment.ps1` - Automated test script (PowerShell)
✅ `test-cloud-deployment.bat` - Automated test script (Batch)
✅ `POSTMAN_CLOUD_TEST_GUIDE.md` - Detailed guide

---

## 🚀 **Quick Start (2 Options)**

### **Option 1: Automated Testing (Recommended)**

**PowerShell:**
```powershell
cd c:\Development\git\userManagementApi
.\test-cloud-deployment.ps1
```

**Command Prompt:**
```cmd
cd c:\Development\git\userManagementApi
test-cloud-deployment.bat
```

**What it does:**
- Runs all API tests automatically
- Generates JSON + HTML reports
- Shows pass/fail results in console
- Saves results in `test-results/` folder

---

### **Option 2: Manual Testing with Postman UI**

1. **Open Postman** (desktop app)
2. **Import files:**
   - File → Import
   - Choose: `postman_collection.json`
   - Repeat for: `postman_environment_cloud.json`
3. **Select environment:** Top-right dropdown → `FinCore Cloud Run Environment`
4. **Run requests:** Click collection items and click **Send**

---

## 🧪 **Test Sequence**

### **Health Check** (No Auth)
```
GET https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
```
Expected: `{"status": "UP"}`

### **Login** (Get JWT Token)
```
POST https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/login

Body:
{
  "username": "admin",
  "password": "admin123"
}
```
Expected: 200 OK with JWT token

### **Get Users** (Protected)
```
GET https://fincore-npe-api-994490239798.europe-west2.run.app/api/users

Headers:
Authorization: Bearer {{jwt_token}}
```
Expected: 200 OK with user list

---

## 📊 **View Test Results**

### **After running tests:**
```powershell
# Results automatically saved to:
# test-results/results_YYYY-MM-DD_HH-MM-SS.json
# test-results/results_YYYY-MM-DD_HH-MM-SS.html

# Open HTML report in browser:
start test-results/results_*.html
```

---

## ⚙️ **Environment Variables**

### Cloud Run Environment (`postman_environment_cloud.json`)
```json
{
  "base_url": "https://fincore-npe-api-994490239798.europe-west2.run.app",
  "jwt_token": "",           // Auto-filled after login
  "username": "admin",
  "password": "admin123",
  "user_id": "1"
}
```

---

## 🔧 **Troubleshooting**

| Issue | Solution |
|-------|----------|
| `newman: command not found` | Run: `npm install -g newman` |
| `"Unauthorized"` response | JWT token expired - re-run login |
| Connection timeout | Verify Cloud Run service is running |
| 404 errors | Check base_url is correct |

---

## 📝 **Manual Request Examples**

### Using PowerShell
```powershell
# Health Check
Invoke-WebRequest -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health" -UseBasicParsing

# Login
$response = Invoke-WebRequest -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/auth/login" `
    -Method POST `
    -Body '{"username":"admin","password":"admin123"}' `
    -ContentType "application/json" `
    -UseBasicParsing

$token = ($response.Content | ConvertFrom-Json).token

# Get Users
Invoke-WebRequest -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/users" `
    -Headers @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
```

---

## 🎯 **What to Test**

✅ Health endpoint
✅ User authentication  
✅ List users
✅ Create user
✅ Get user by ID
✅ Update user
✅ Delete user
✅ Error handling (401, 404, 400)
✅ Invalid credentials
✅ Missing JWT token

---

## 📚 **More Information**

- Full guide: [POSTMAN_CLOUD_TEST_GUIDE.md](POSTMAN_CLOUD_TEST_GUIDE.md)
- API docs: [userManagementAPI.md](userManagementAPI.md)
- Local testing: [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md)

---

## ✅ **Done!**

You now have:
- ✅ Postman collection for all API tests
- ✅ Cloud environment configuration
- ✅ Automated test scripts (PS1 + BAT)
- ✅ Detailed testing guides

**Start testing:**
```powershell
.\test-cloud-deployment.ps1
```
