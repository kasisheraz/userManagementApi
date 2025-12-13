@echo off
REM Start the application
start "" cmd.exe /c "cd /d C:\Development\git\userManagementApi && java -jar target\user-management-api-1.0.0.jar --spring.profiles.active=h2"

REM Wait for application to start
timeout /t 20 /nobreak

REM Test the endpoints
echo.
echo Testing User Management API Endpoints
echo ======================================
echo.
echo 1. Health Check
curl -s http://localhost:8080/actuator/health

echo.
echo.
echo 2. Login Test
curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@fincore.com\",\"password\":\"admin123\"}"

echo.
echo.
echo 3. Get Users
REM First get the token from login
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@fincore.com\",\"password\":\"admin123\"}" ^| findstr "token"') do (
  set TOKEN_LINE=%%i
)
echo Token retrieved: %TOKEN_LINE%

echo.
echo Test completed!
