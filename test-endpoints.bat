@echo off
REM Local testing script for User Management API

echo =========================================
echo Local Testing - User Management API
echo =========================================

echo.
echo Waiting for application to start (15 seconds)...
timeout /t 15 /nobreak

echo.
echo Testing Health Endpoint...
curl -X GET http://localhost:8080/actuator/health
echo.

echo Testing Login Endpoint...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@fincore.com\",\"password\":\"admin123\"}"
echo.

echo All tests complete!
echo.
pause
