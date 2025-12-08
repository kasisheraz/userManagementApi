@echo off
echo ========================================
echo Running User Management API Tests
echo ========================================
echo.

echo Running all tests...
call mvn clean test

echo.
echo ========================================
echo Test execution completed!
echo ========================================
pause
