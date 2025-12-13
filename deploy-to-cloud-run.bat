@echo off
REM Cloud Run Deployment Script for User Management API (Windows)

setlocal enabledelayedexpansion

REM Configuration
set PROJECT_ID=%GCP_PROJECT_ID%
set SERVICE_NAME=%CLOUD_RUN_SERVICE_NAME:user-management-api%
set REGION=%CLOUD_RUN_REGION:us-central1%
set IMAGE_NAME=user-management-api
set DB_INSTANCE=%DB_INSTANCE%
set DB_USER=%DB_USER:root%
set DB_PASSWORD=%DB_PASSWORD%
set DB_NAME=%DB_NAME:my_auth_db%
set JWT_SECRET=%JWT_SECRET%

REM Color codes
setlocal disabledelayedexpansion

echo.
echo ================================
echo Cloud Run Deployment Script
echo ================================
echo.

REM Check prerequisites
echo Checking prerequisites...

where /q gcloud
if errorlevel 1 (
    echo Error: gcloud CLI is not installed. Please install it first.
    exit /b 1
)
echo - gcloud CLI found

where /q docker
if errorlevel 1 (
    echo Error: Docker is not installed. Please install it first.
    exit /b 1
)
echo - Docker found

where /q mvn
if errorlevel 1 (
    echo Error: Maven is not installed. Please install it first.
    exit /b 1
)
echo - Maven found

if "!PROJECT_ID!"=="" (
    echo Error: GCP_PROJECT_ID environment variable is not set
    exit /b 1
)
echo - GCP Project ID: !PROJECT_ID!

if "!DB_INSTANCE!"=="" (
    echo Error: DB_INSTANCE environment variable is not set
    exit /b 1
)
echo - Database Instance: !DB_INSTANCE!

echo.
echo ================================
echo Building Application
echo ================================
echo.

call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo Error: Maven build failed
    exit /b 1
)
echo - Application built successfully

echo.
echo ================================
echo Building Docker Image
echo ================================
echo.

set IMAGE_TAG=gcr.io/!PROJECT_ID!/!IMAGE_NAME!:latest

docker build -t !IMAGE_TAG! .
if errorlevel 1 (
    echo Error: Docker build failed
    exit /b 1
)
echo - Docker image built: !IMAGE_TAG!

echo.
echo ================================
echo Pushing Image to GCR
echo ================================
echo.

docker push !IMAGE_TAG!
if errorlevel 1 (
    echo Error: Failed to push image to GCR
    exit /b 1
)
echo - Image pushed to GCR

echo.
echo ================================
echo Deploying to Cloud Run
echo ================================
echo.

gcloud run deploy !SERVICE_NAME! ^
    --image !IMAGE_TAG! ^
    --platform managed ^
    --region !REGION! ^
    --allow-unauthenticated ^
    --set-env-vars="SPRING_PROFILES_ACTIVE=gcp,DB_USER=!DB_USER!,DB_NAME=!DB_NAME!,JWT_SECRET=!JWT_SECRET!,LOG_LEVEL=INFO" ^
    --add-cloudsql-instances="!PROJECT_ID!:!REGION!:!DB_INSTANCE!" ^
    --service-account="!SERVICE_NAME!-sa@!PROJECT_ID!.iam.gserviceaccount.com" ^
    --memory 512Mi ^
    --cpu 1 ^
    --max-instances 10 ^
    --min-instances 1 ^
    --timeout 3600 ^
    --port 8080

if errorlevel 1 (
    echo Error: Failed to deploy to Cloud Run
    exit /b 1
)
echo - Application deployed to Cloud Run

echo.
echo ================================
echo Getting Service URL
echo ================================
echo.

for /f "tokens=*" %%i in ('gcloud run services describe !SERVICE_NAME! --platform managed --region !REGION! --format="value(status.url)"') do set SERVICE_URL=%%i

echo - Service URL: !SERVICE_URL!

echo.
echo ================================
echo Testing Deployment
echo ================================
echo.

echo Testing health endpoint...
for /f "tokens=*" %%i in ('curl -s -o nul -w "%%{http_code}" "!SERVICE_URL!/actuator/health"') do set HEALTH_RESPONSE=%%i

if "!HEALTH_RESPONSE!"=="200" (
    echo - Health check passed
) else (
    echo Warning: Health check returned status code !HEALTH_RESPONSE!
)

echo.
echo ================================
echo Deployment Complete!
echo ================================
echo.
echo Service URL: !SERVICE_URL!
echo.

endlocal
