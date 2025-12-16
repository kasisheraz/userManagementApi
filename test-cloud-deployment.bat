@echo off
REM Postman Newman Test Runner for Cloud Run Deployment
REM Usage: test-cloud-deployment.bat

setlocal enabledelayedexpansion

echo.
echo === Testing User Management API on Cloud Run ===
echo Service: https://fincore-npe-api-994490239798.europe-west2.run.app
echo.

set COLLECTION=postman_collection.json
set ENVIRONMENT=postman_environment_cloud.json
set RESULTS_DIR=test-results
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c-%%a-%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a-%%b)
set TIMESTAMP=%mydate%_%mytime%

REM Check if results directory exists
if not exist "%RESULTS_DIR%" (
    mkdir "%RESULTS_DIR%"
    echo + Created results directory
)

REM Check if Newman is installed
newman --version >nul 2>&1
if errorlevel 1 (
    echo ! Newman not found. Installing...
    call npm install -g newman
    if errorlevel 1 (
        echo ERROR: Failed to install Newman
        exit /b 1
    )
)

for /f "tokens=*" %%i in ('newman --version') do set NEWMAN_VERSION=%%i
echo + Newman version: %NEWMAN_VERSION%
echo.

REM Run tests
set OUTPUT_JSON=%RESULTS_DIR%\results_%TIMESTAMP%.json
set OUTPUT_HTML=%RESULTS_DIR%\results_%TIMESTAMP%.html

echo Running tests...
echo Collection: %COLLECTION%
echo Environment: %ENVIRONMENT%
echo Output: %OUTPUT_JSON%
echo.

newman run %COLLECTION% ^
    -e %ENVIRONMENT% ^
    --reporters cli,json,html ^
    --reporter-json-export %OUTPUT_JSON% ^
    --reporter-html-export %OUTPUT_HTML% ^
    --timeout 10000 ^
    --timeout-request 5000

if errorlevel 1 (
    echo.
    echo ERROR: Tests failed. Check results:
    echo   %OUTPUT_JSON%
    exit /b 1
) else (
    echo.
    echo + Tests completed successfully!
    echo.
    echo Results saved to:
    echo   JSON: %OUTPUT_JSON%
    echo   HTML: %OUTPUT_HTML%
    echo.
    echo Open HTML report in browser:
    echo   start %OUTPUT_HTML%
)
