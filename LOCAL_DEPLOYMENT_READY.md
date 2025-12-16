# ✅ Local Development - Setup Complete!

**Date**: December 16, 2025  
**Status**: 🚀 **SERVICE RUNNING & READY FOR TESTING**

---

## 🎯 Quick Summary

Your User Management API is now **running locally** on your machine and ready to be tested from your UI application.

### Service Details
- **URL**: `http://localhost:8080`
- **Status**: ✅ Running and responding
- **Database**: H2 (in-memory)
- **Port**: 8080
- **Startup Time**: ~15 seconds

---

## 📍 Access Points

### Your UI Application
```
http://localhost:3000
```

### Backend API
```
http://localhost:8080
```

### H2 Database Console
```
http://localhost:8080/h2-console
```

Connection Details:
- URL: `jdbc:h2:mem:fincore_db`
- User: `SA`
- Password: (leave blank)

---

## 🧪 Quick Test Examples

### 1. Check Service Health

```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### 2. Login (Get JWT Token)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "fullName": "Admin User",
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDcxOTk5OCwiZXhwIjoxNzMwNzIzNTk4fQ.abcdef..."
}
```

### 3. Get All Users (Requires JWT Token)

```bash
# First, get the token from login above
TOKEN="eyJ0eXAiOiJKV1QiLCJhbGc..."

# Then use it to get users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Admin User",
    "department": "IT",
    "status": "ACTIVE"
  }
]
```

### 4. Create New User

```bash
TOKEN="eyJ0eXAiOiJKV1QiLCJhbGc..."

curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe",
    "department": "Sales"
  }'
```

---

## 💻 From Your React/Vue UI

### Base API Configuration

```javascript
// In your UI environment file or constants
const API_BASE_URL = 'http://localhost:8080';

// Or if running via HTTPS
const API_BASE_URL = 'http://localhost:8080';
```

### Example Authentication Service

```javascript
class AuthService {
  async login(username, password) {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw new Error('Login failed');
    }
    
    const data = await response.json();
    // Save token to localStorage
    localStorage.setItem('authToken', data.token);
    return data;
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  logout() {
    localStorage.removeItem('authToken');
  }
}
```

### Example API Service

```javascript
class UserService {
  constructor() {
    this.baseUrl = 'http://localhost:8080/api';
  }

  async getUsers() {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseUrl}/users`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch users');
    }
    
    return response.json();
  }

  async createUser(userData) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseUrl}/users`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });
    
    if (!response.ok) {
      throw new Error('Failed to create user');
    }
    
    return response.json();
  }

  async updateUser(userId, userData) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseUrl}/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });
    
    return response.json();
  }

  async deleteUser(userId) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${this.baseUrl}/users/${userId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    return response.status === 204 || response.ok;
  }
}
```

---

## 🔐 Default Test Account

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `password123` |
| **Email** | `admin@example.com` |
| **Full Name** | `Admin User` |

---

## 📋 Available API Endpoints

### Authentication (No Auth Required)
```
POST   /api/auth/login              - User login (returns JWT)
POST   /api/auth/register           - Register new user
GET    /actuator/health             - Service health check
```

### Users (Auth Required - Bearer Token in header)
```
GET    /api/users                   - Get all users
POST   /api/users                   - Create new user
GET    /api/users/{id}              - Get user by ID
PUT    /api/users/{id}              - Update user
DELETE /api/users/{id}              - Delete user
```

### Database Console (No Auth)
```
GET    /h2-console                  - H2 database web console
```

---

## 🔄 Typical Workflow

### 1. User Logs In (from your UI)
```javascript
POST /api/auth/login
Request: { "username": "admin", "password": "password123" }
Response: { "id": 1, "token": "eyJ...", ... }
```

### 2. Save Token
```javascript
localStorage.setItem('authToken', response.token);
```

### 3. Use Token for Subsequent Requests
```javascript
headers: {
  'Authorization': 'Bearer eyJ...',
  'Content-Type': 'application/json'
}
```

### 4. Make API Calls
```javascript
// Get users
GET /api/users

// Create user
POST /api/users

// Update user
PUT /api/users/2

// Delete user
DELETE /api/users/2
```

---

## ⚙️ Configuration Notes

### CORS (Cross-Origin Resource Sharing)

The API is configured to accept requests from `http://localhost:3000` (your UI).

If you get CORS errors:
1. Check your UI is running at `http://localhost:3000`
2. Verify API calls are to `http://localhost:8080` (not https)
3. Check browser console for specific error message
4. Try clearing browser cache and refreshing

### Database

- H2 in-memory database
- Data is fresh on every startup (tables recreated)
- Data persists during the session
- **Data is lost when service restarts**

### JWT Tokens

- Tokens are valid for 1 hour
- Stored in localStorage on your UI
- Required for all user endpoints
- Include in header as: `Authorization: Bearer <token>`

---

## 🧪 Testing Tools

### Option 1: Use curl (Command Line)
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer TOKEN"
```

### Option 2: Use Postman
1. Set base URL: `http://localhost:8080`
2. Create requests for each endpoint
3. Use Collections to organize
4. Save environment variables for tokens

### Option 3: Use Browser DevTools
1. Open your UI at `http://localhost:3000`
2. Open DevTools (F12)
3. Use Console tab to test API calls
4. Check Network tab to see requests/responses

### Option 4: Use test script
```bash
# Windows
test-api-local.bat

# Linux/Mac
bash test-api-local.sh
```

---

## 🆘 Troubleshooting

### Service Not Responding

**Problem**: `curl: (7) Failed to connect to localhost port 8080`

**Solutions**:
1. Check if service is running (should see logs in terminal)
2. Verify port 8080 is not in use: `netstat -ano | findstr :8080`
3. Restart service: Stop (Ctrl+C) and restart with:
   ```bash
   java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
   ```

### CORS Error

**Problem**: `Access to XMLHttpRequest blocked by CORS policy`

**Solutions**:
1. Verify UI is at `http://localhost:3000`
2. Verify API calls are to `http://localhost:8080` (not https)
3. Clear browser cache: Ctrl+Shift+Delete
4. Hard refresh: Ctrl+Shift+R

### 401 Unauthorized

**Problem**: `{"error":"Unauthorized"}`

**Solutions**:
1. Login first to get token
2. Verify token is included in header: `Authorization: Bearer <token>`
3. Check token hasn't expired (1 hour timeout)
4. Get new token with login endpoint

### 404 Not Found

**Problem**: `{"error":"Not Found"}`

**Solutions**:
1. Check endpoint URL spelling
2. Verify endpoint path is correct
3. Check HTTP method (GET vs POST, etc.)
4. Verify resource ID exists (for /users/{id})

---

## 📊 Service Information

### Startup Sequence

```
1. Spring Boot initializes (v3.2.0)
2. H2 Profile active
3. Spring Data JPA repositories loaded (2 found)
4. Tomcat server starts on port 8080
5. H2 database initialized in memory
6. Hibernate creates schema
7. Service ready to accept requests
Total time: ~15 seconds
```

### Logs

The service logs are displayed in your terminal. You can see:
- Incoming HTTP requests
- Database queries
- Error messages
- Performance metrics

### Monitoring

Check service health anytime:
```bash
curl http://localhost:8080/actuator/health
```

---

## 🚀 Next Steps

1. ✅ Service is running on `http://localhost:8080`
2. ✅ Test login endpoint to get JWT token
3. ✅ Update your UI to call `http://localhost:8080/api/*`
4. ✅ Use JWT token in Authorization header
5. ✅ Test all user management features
6. ✅ Build your UI features

---

## 💾 Useful Commands

### Check if service is running
```bash
curl http://localhost:8080/actuator/health
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

### Get users (replace TOKEN with actual token)
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer TOKEN"
```

### Create user
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"pass","fullName":"Test User"}'
```

### View database
```
Open: http://localhost:8080/h2-console
Connection: jdbc:h2:mem:fincore_db
User: SA
Password: (blank)
```

---

## 📝 Documentation

For more details, see:
- [LOCAL_DEVELOPMENT_SETUP.md](LOCAL_DEVELOPMENT_SETUP.md) - Detailed setup guide
- [README.md](README.md) - Project overview

---

## ✨ Status Summary

| Component | Status |
|-----------|--------|
| Service | ✅ Running |
| Port | ✅ 8080 |
| Database | ✅ H2 (in-memory) |
| Health Check | ✅ Responding |
| Ready for Testing | ✅ Yes |
| UI URL | ✅ http://localhost:3000 |
| API URL | ✅ http://localhost:8080 |

---

**Ready to test your UI with the API!** 🎉

For questions, check [LOCAL_DEVELOPMENT_SETUP.md](LOCAL_DEVELOPMENT_SETUP.md)
