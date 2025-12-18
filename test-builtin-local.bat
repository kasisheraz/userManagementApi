@echo off
REM Local Testing Script for Cloud SQL Built-in Integration (Windows)
REM This script collects secrets from Google Secret Manager and tests locally

echo === Setting up local environment for Cloud SQL Built-in Integration ===

REM Set project context
set PROJECT_ID=project-07a61357-b791-4255-a9e
set REGION=europe-west2

echo ✓ Using Project: %PROJECT_ID%
echo ✓ Using Region: %REGION%

echo.
echo === Collecting secrets from Google Secret Manager ===

REM Get Cloud SQL instance connection string
for /f "delims=" %%i in ('gcloud secrets versions access latest --secret="CLOUDSQL_INSTANCE"') do set CLOUD_SQL_INSTANCE=%%i
echo ✓ CLOUD_SQL_INSTANCE: %CLOUD_SQL_INSTANCE%

REM Get database password
for /f "delims=" %%i in ('gcloud secrets versions access latest --secret="DB_PASSWORD"') do set DB_PASSWORD=%%i
echo ✓ DB_PASSWORD: [HIDDEN]

REM Set other environment variables
set DB_NAME=my_auth_db
set DB_USER=fincore_app
set PORT=8080

echo.
echo === Environment Variables Set ===
echo CLOUD_SQL_INSTANCE=%CLOUD_SQL_INSTANCE%
echo DB_NAME=%DB_NAME%
echo DB_USER=%DB_USER%
echo PORT=%PORT%
echo DB_PASSWORD=[HIDDEN]

echo.
echo === Testing Cloud SQL connection locally ===

REM Check gcloud authentication
echo Checking gcloud authentication...
gcloud auth list

echo.
echo Setting up Application Default Credentials for local development...
echo Please complete the authentication flow in your browser...
gcloud auth application-default login

echo.
echo === Starting Spring Boot application with built-in Cloud SQL integration ===
echo Profile: gcp-builtin
echo This will use the SocketFactory to connect directly to Cloud SQL

REM Start the application with the gcp-builtin profile
mvn spring-boot:run -Dspring-boot.run.profiles=gcp-builtin -Dspring-boot.run.jvmArguments="-DCLOUD_SQL_INSTANCE=%CLOUD_SQL_INSTANCE% -DDB_NAME=%DB_NAME% -DDB_USER=%DB_USER% -DDB_PASSWORD=%DB_PASSWORD% -DPORT=%PORT%"