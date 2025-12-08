# How to Run the Project

## Issue: Port 8080 Already in Use

If you see "Port 8080 was already in use", follow these steps:

### Option 1: Stop the Process on Port 8080
```bash
# Find the process ID (PID)
netstat -ano | findstr :8080

# Kill the process (replace PID with actual number)
taskkill /PID <PID> /F
```

### Option 2: Run on Different Port
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2 -Dspring-boot.run.arguments=--server.port=8081
```
Then access at: http://localhost:8081

## Start the Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## Test the API

### Using cURL (Windows CMD)
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"Admin@123456\"}"
```

### Using PowerShell
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"Admin@123456"}'
```

### Using Browser/Postman
- URL: `POST http://localhost:8080/api/auth/login`
- Headers: `Content-Type: application/json`
- Body:
```json
{
  "username": "admin",
  "password": "Admin@123456"
}
```

## Test Credentials

| Username   | Password        | Role                  |
|------------|-----------------|----------------------|
| admin      | Admin@123456    | SYSTEM_ADMINISTRATOR |
| compliance | Compliance@123  | COMPLIANCE_OFFICER   |
| staff      | Staff@123456    | OPERATIONAL_STAFF    |

## Access H2 Database Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (empty)

## Run Tests
```bash
mvn test
```
Expected: 30 tests passing
