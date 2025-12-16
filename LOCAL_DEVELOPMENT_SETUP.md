# Local Development Setup - User Management API

## ✅ Service is Running!

Your User Management API is now running locally on **port 8080**.

### Service URL
```
http://localhost:8080
```

### API Endpoints

All endpoints are ready for your UI application to connect to:

| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---|
| `/actuator/health` | GET | Service health check | ❌ No |
| `/api/auth/login` | POST | User login (get JWT token) | ❌ No |
| `/api/auth/register` | POST | Register new user | ❌ No |
| `/api/users` | GET | Get all users | ✅ Yes |
| `/api/users/{id}` | GET | Get user by ID | ✅ Yes |
| `/api/users` | POST | Create new user | ✅ Yes |
| `/api/users/{id}` | PUT | Update user | ✅ Yes |
| `/api/users/{id}` | DELETE | Delete user | ✅ Yes |
| `/h2-console` | GET | H2 Database console | ❌ No |

---

## 🧪 Testing from Your UI (http://localhost:3000/)

### Example: Login Request

```javascript
// From your UI running on http://localhost:3000/
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'password123'
  })
});

const data = await response.json();
console.log('Token:', data.token); // Use this for authenticated requests
```

### Example: Get Users (Authenticated)

```javascript
// Use the token from login
const token = 'eyJ0eXAiOiJKV1QiLCJhbGc...'; // Token from login

const response = await fetch('http://localhost:8080/api/users', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const users = await response.json();
console.log('Users:', users);
```

### Example: Create User

```javascript
const token = 'eyJ0eXAiOiJKV1QiLCJhbGc...'; // Token from login

const response = await fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'newuser',
    email: 'newuser@example.com',
    password: 'SecurePass123!',
    fullName: 'New User',
    department: 'Engineering'
  })
});

const newUser = await response.json();
console.log('Created user:', newUser);
```

---

## 🗄️ Database Access

### H2 Console (In-Memory Database)

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- **URL**: `jdbc:h2:mem:fincore_db`
- **User**: `SA`
- **Password**: (leave blank)

### Available Tables

```sql
-- Users table
SELECT * FROM users;

-- Roles table
SELECT * FROM roles;

-- Permissions table
SELECT * FROM permissions;

-- Role-Permission mappings
SELECT * FROM role_permissions;
```

---

## 🔐 Default Credentials

Pre-loaded user for testing:

| Username | Password | Email |
|----------|----------|-------|
| `admin` | `password123` | `admin@example.com` |

---

## 📝 Sample Workflow

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

### 2. Login as Admin
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
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Get All Users (with JWT token)
```bash
TOKEN="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."

curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Create New User (with JWT token)
```bash
TOKEN="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."

curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "JohnPass123!",
    "fullName": "John Doe",
    "department": "Sales"
  }'
```

---

## 🛑 Stopping the Service

To stop the running service, press **Ctrl+C** in the terminal where it's running.

---

## 🚀 Restarting the Service

If you need to restart after stopping:

```bash
cd c:\Development\git\userManagementApi
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
```

Or if you made code changes, rebuild first:

```bash
cd c:\Development\git\userManagementApi
mvn clean package -DskipTests -q
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=h2
```

---

## 🔄 CORS Configuration for UI

The API is configured to accept requests from your local UI at `http://localhost:3000/`.

If you get CORS errors, check [src/main/java/com/fincore/usermgmt/config/CorsConfig.java](src/main/java/com/fincore/usermgmt/config/CorsConfig.java)

### Common CORS Error Solutions

**Error**: `Access to XMLHttpRequest at 'http://localhost:8080/api/users' has been blocked by CORS policy`

**Solution**: The CORS configuration should already allow `http://localhost:3000`. If you still get errors:

1. Check your UI is actually at `http://localhost:3000`
2. Verify API calls are to `http://localhost:8080` (not https)
3. Check browser console for specific error
4. Clear browser cache and hard refresh (Ctrl+Shift+R)

---

## 📊 Application Configuration

### Startup Logs

The application logs show:
- ✅ H2 Profile active
- ✅ Spring Data JPA repositories found (2 repositories)
- ✅ H2 database initialized: `jdbc:h2:mem:fincore_db`
- ✅ Hibernated schema created
- ✅ Tomcat started on port 8080
- ✅ Application started successfully

### Running Profile

Currently running with: **h2** profile
- H2 in-memory database
- Data persists during session
- Data lost when application restarts

---

## 💻 Your UI Connection

Your UI should be configured to call:

### Base API URL
```javascript
const API_BASE_URL = 'http://localhost:8080';
```

### Example API Service
```javascript
class UserService {
  constructor() {
    this.baseUrl = 'http://localhost:8080/api';
  }

  async login(username, password) {
    const response = await fetch(`${this.baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    return response.json();
  }

  async getUsers(token) {
    const response = await fetch(`${this.baseUrl}/users`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    return response.json();
  }

  async createUser(userData, token) {
    const response = await fetch(`${this.baseUrl}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(userData)
    });
    return response.json();
  }
}
```

---

## 🐛 Debugging

### View Real-Time Logs

The application is currently printing logs to the terminal. Look for:
- Requests coming in
- Database queries
- Errors and exceptions

### Enable Debug Logging

To see more detailed logs, you can add:
```bash
java -jar target/user-management-api-1.0.0.jar \
  --spring.profiles.active=h2 \
  --logging.level.com.fincore=DEBUG \
  --logging.level.org.springframework.web=DEBUG
```

### Check Application Health

```bash
curl http://localhost:8080/actuator/health
```

---

## 📋 Endpoints Reference Card

### Authentication (No Auth Required)
```
POST   /api/auth/login          - Login user
POST   /api/auth/register       - Register new user
GET    /actuator/health         - Health check
```

### Users (Auth Required - Bearer Token)
```
GET    /api/users               - Get all users
POST   /api/users               - Create user
GET    /api/users/{id}          - Get user by ID
PUT    /api/users/{id}          - Update user
DELETE /api/users/{id}          - Delete user
```

### Database Console (No Auth Required)
```
GET    /h2-console              - H2 database web console
```

---

## 🎯 Next Steps

1. ✅ Service is running on `http://localhost:8080`
2. ✅ Test with curl or Postman to verify connectivity
3. ✅ Update your UI to call `http://localhost:8080/api/*` endpoints
4. ✅ Test login flow with default credentials
5. ✅ Build your UI features against the API

---

## 📞 Quick Help

| Issue | Solution |
|-------|----------|
| **"Connection refused"** | Ensure service is running (check terminal) |
| **"CORS error"** | Verify UI is at http://localhost:3000 |
| **"Unauthorized"** | Check JWT token format and login first |
| **"404 Not Found"** | Verify endpoint URL is correct |
| **"Service not responding"** | Restart: Ctrl+C then re-run java command |

---

**Service Status**: ✅ Running
**Port**: 8080
**Database**: H2 (In-Memory)
**Profile**: h2

Ready to test with your UI! 🚀
