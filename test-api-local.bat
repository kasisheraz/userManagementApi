@echo off
REM Local API Testing Script
REM This script tests the local User Management API running on http://localhost:8080

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║       Local User Management API - Quick Test                  ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo Step 1: Health Check
echo ────────────────────────────────────────────────────────────────
curl -X GET http://localhost:8080/actuator/health
echo.
echo.

echo Step 2: Login
echo ────────────────────────────────────────────────────────────────
echo Logging in as admin...
for /f "delims=" %%A in ('curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password123\"}" ^
  ^| findstr /R "token"') do (
  set "TOKEN=%%A"
)
echo Response: %TOKEN%
echo.

REM Extract token (simplified - in production use jq)
REM For now, we'll just show the response
echo.
echo Step 3: Get Users (with Bearer token)
echo ────────────────────────────────────────────────────────────────
echo Note: Copy the token from Step 2 and paste below to test authenticated endpoints
echo.
echo Example:
echo   curl -X GET http://localhost:8080/api/users ^
echo     -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc..."
echo.

pause
