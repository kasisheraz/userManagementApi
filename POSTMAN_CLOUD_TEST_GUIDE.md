# Postman Setup Guide - Test Cloud Run Deployment

**Objective:** Configure Postman to test the Cloud Run API deployment

**Service URL:** https://fincore-npe-api-994490239798.europe-west2.run.app

---

## Prerequisites

1. **Install Postman Desktop App**
   - Download: https://www.postman.com/downloads/
   - Install and launch

2. **Files Required** (already in workspace):
   - `postman_collection.json` - API test collection
   - `postman_environment.json` - Environment variables

---

## Step 1: Import Collection into Postman

1. Open **Postman Desktop**
2. Click **File** → **Import** (or use Import button)
3. Select **Upload Files** tab
4. Browse to: `c:\Development\git\userManagementApi\postman_collection.json`
5. Click **Import**
6. You should see collection "User Management API" in the left sidebar

---

## Step 2: Create Cloud Run Environment

### Option A: Import & Modify (Faster)

1. Click **Import** again
2. Select `postman_environment.json`
3. Click **Import**
4. In Postman, click **Environments** on the left
5. Select **FinCore Local Environment**
6. Edit the `base_url` variable:
   - **Current:** `http://localhost:8080`
   - **Change to:** `https://fincore-npe-api-994490239798.europe-west2.run.app`
7. Click **Save**

### Option B: Create Manually

1. Click **Environments** (left sidebar)
2. Click **Create New** or **+**
3. Name: `FinCore Cloud Environment`
4. Add variables:
   ```
   base_url: https://fincore-npe-api-994490239798.europe-west2.run.app
   jwt_token: (leave empty - will be populated after login)
   username: admin
   ```
5. Click **Save**

---

## Step 3: Select Active Environment

1. Top-right corner, click **Environment dropdown**
2. Select **FinCore Cloud Environment** (or your Cloud environment name)
3. You should see the environment name appear in top-right

---

## Step 4: API Request Examples

### 1️⃣ **Health Check** (No Auth Required)

**Endpoint:** `{{base_url}}/actuator/health`
- **Method:** GET
- **Expected Response:** 200 OK with status "UP"

**Test it:**
1. Click **Collections** → **User Management API**
2. Expand and find **Health Check** request (or create new GET request)
3. Click **Send**
4. Response should show: `{"status": "UP", "components": {...}}`

---

### 2️⃣ **Login** (Get JWT Token)

**Endpoint:** `{{base_url}}/api/auth/login`
- **Method:** POST
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (Raw JSON):**
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```

**Test it:**
1. Create new POST request
2. URL: `{{base_url}}/api/auth/login`
3. Go to **Body** tab → **raw** → select **JSON**
4. Paste the body above
5. Click **Send**
6. Expected: 200 OK with token response

**Capture JWT Token automatically:**
1. Go to **Tests** tab in request
2. Add script:
   ```javascript
   if (pm.response.code === 200) {
       var jsonData = pm.response.json();
       pm.environment.set("jwt_token", jsonData.token);
   }
   ```
3. Now token auto-saves to `{{jwt_token}}` variable

---

### 3️⃣ **Get All Users** (Requires Auth)

**Endpoint:** `{{base_url}}/api/users`
- **Method:** GET
- **Headers:**
  ```
  Authorization: Bearer {{jwt_token}}
  ```

**Test it:**
1. Create new GET request
2. URL: `{{base_url}}/api/users`
3. Go to **Headers** tab
4. Add:
   - **Key:** `Authorization`
   - **Value:** `Bearer {{jwt_token}}`
5. Click **Send**
6. Expected: 200 OK with user list

---

### 4️⃣ **Create New User**

**Endpoint:** `{{base_url}}/api/users`
- **Method:** POST
- **Headers:**
  ```
  Authorization: Bearer {{jwt_token}}
  Content-Type: application/json
  ```
- **Body:**
  ```json
  {
    "username": "newuser",
    "email": "newuser@example.com",
    "fullName": "New User",
    "password": "SecurePass123!"
  }
  ```

---

## Step 5: Run Full Test Suite

### Using Postman UI

1. Click **Collections** → **User Management API**
2. Click the **Run** button (▶️)
3. Select tests you want to run
4. Click **Run Collection**
5. View results in **Results** tab

### Using Newman (Command Line)

**Install Newman:**
```powershell
npm install -g newman
```

**Run tests:**
```powershell
cd c:\Development\git\userManagementApi

# Run against Cloud Run
newman run postman_collection.json `
  -e postman_environment.json `
  --environment-var "base_url=https://fincore-npe-api-994490239798.europe-west2.run.app" `
  --reporters cli,json `
  --reporter-json-export results.json
```

**View results:**
```powershell
cat results.json | ConvertFrom-Json | ConvertTo-Json
```

---

## Step 6: Troubleshooting

### ❌ **"Unauthorized - Invalid or missing API Key"**
- **Cause:** Missing or invalid JWT token
- **Fix:** 
  1. Run Login endpoint first
  2. Check `jwt_token` variable is populated
  3. Ensure Bearer token is in Authorization header

### ❌ **"CORS Error"**
- **Cause:** Browser policy (doesn't apply to Postman desktop)
- **Fix:** Should not happen with Postman desktop app

### ❌ **"Connection timeout"**
- **Cause:** Cloud Run service not responding
- **Fix:** 
  1. Check service URL is correct
  2. Verify service is running: `gcloud run services list`
  3. Check Cloud Run logs

### ❌ **"Invalid credentials"**
- **Cause:** Wrong username/password
- **Fix:** Use `admin` / `admin123` for testing

---

## Quick Reference - Common Endpoints

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/actuator/health` | GET | ❌ | Health check |
| `/api/auth/login` | POST | ❌ | Login & get token |
| `/api/users` | GET | ✅ | List all users |
| `/api/users` | POST | ✅ | Create user |
| `/api/users/{id}` | GET | ✅ | Get user by ID |
| `/api/users/{id}` | PUT | ✅ | Update user |
| `/api/users/{id}` | DELETE | ✅ | Delete user |

---

## Environment Variables

Save these in Postman environment:

```json
{
  "base_url": "https://fincore-npe-api-994490239798.europe-west2.run.app",
  "jwt_token": "",
  "username": "admin",
  "user_id": "1"
}
```

Use them as: `{{variable_name}}`

---

## Quick Start - 5 Minutes

1. **Import collection:** File → Import → `postman_collection.json`
2. **Import environment:** File → Import → `postman_environment.json`
3. **Edit environment:**
   - Change `base_url` to Cloud Run URL
   - Save
4. **Select environment:** Top-right dropdown
5. **Run Login request:**
   - Open "User Management API" → "Auth" → "Login"
   - Click **Send**
   - Token auto-saves
6. **Run any protected endpoint** - token is ready to use!

---

## Testing Full Flow

**Sequence to test:**
1. ✅ Health Check (GET `/actuator/health`)
2. ✅ Login (POST `/api/auth/login`)
3. ✅ Get Users (GET `/api/users`)
4. ✅ Create User (POST `/api/users`)
5. ✅ Get Single User (GET `/api/users/{id}`)
6. ✅ Update User (PUT `/api/users/{id}`)
7. ✅ Delete User (DELETE `/api/users/{id}`)

---

## Export Test Results

After running tests:

1. Click **Results** tab
2. Click **Export Results** button
3. Save as JSON or HTML
4. Share with team for validation

---

**Done!** 🎉 You can now test the Cloud Run deployment locally using Postman.

For API documentation, see: `userManagementAPI.md`
