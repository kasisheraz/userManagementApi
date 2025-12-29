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

### OAuth2 Authentication Flow

#### Step 1: Request OTP

**Using cURL (Windows CMD)**
```bash
curl -X POST http://localhost:8080/api/auth/request-otp -H "Content-Type: application/json" -d "{\"phoneNumber\":\"+1234567890\"}"
```

**Using PowerShell**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/request-otp" -Method POST -ContentType "application/json" -Body '{"phoneNumber":"+1234567890"}'
```

**Using Browser/Postman**
- URL: `POST http://localhost:8080/api/auth/request-otp`
- Headers: `Content-Type: application/json`
- Body:
```json
{
  "phoneNumber": "+1234567890"
}
```

**Note:** Check the application console logs for the OTP code (in development mode, OTPs are logged).

#### Step 2: Verify OTP and Get JWT Token

**Using cURL (Windows CMD)**
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp -H "Content-Type: application/json" -d "{\"phoneNumber\":\"+1234567890\",\"otp\":\"123456\"}"
```

**Using PowerShell**
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/verify-otp" -Method POST -ContentType "application/json" -Body '{"phoneNumber":"+1234567890","otp":"123456"}'
$token = $response.accessToken
Write-Host "JWT Token: $token"
```

**Using Browser/Postman**
- URL: `POST http://localhost:8080/api/auth/verify-otp`
- Headers: `Content-Type: application/json`
- Body:
```json
{
  "phoneNumber": "+1234567890",
  "otp": "123456"
}
```

#### Step 3: Use JWT Token for Protected Endpoints

**Using cURL with JWT Token**
```bash
curl -X GET http://localhost:8080/api/users -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Using PowerShell with JWT Token**
```powershell
$headers = @{Authorization = "Bearer $token"}
Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method GET -Headers $headers
```

## Test Phone Numbers

Use these phone numbers from the test data:

| Phone Number | Email | Role | Name |
|-------------|-------|------|------|
| +1234567890 | admin@fincore.com | SYSTEM_ADMINISTRATOR | System Administrator |
| +1234567891 | compliance@fincore.com | COMPLIANCE_OFFICER | Compliance Officer |
| +1234567892 | staff@fincore.com | OPERATIONAL_STAFF | Operational Staff |

**Note:** In development mode, OTP codes are logged to the console. In production, integrate with an SMS service (Twilio, AWS SNS, etc.) to send actual SMS messages.

## Access H2 Database Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fincore_db`
- Username: `sa`
- Password: (empty)

## Run Tests
```bash
mvn test
```
Expected: 8 tests passing (UserRepositoryTest, UserServiceTest)
