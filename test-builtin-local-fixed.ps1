# Local Testing Script for Cloud SQL Built-in Integration (PowerShell)
# This script collects secrets from Google Secret Manager and tests locally

Write-Host "=== Setting up local environment for Cloud SQL Built-in Integration ===" -ForegroundColor Green

# Set project context
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"

Write-Host "✓ Using Project: $PROJECT_ID" -ForegroundColor Green
Write-Host "✓ Using Region: $REGION" -ForegroundColor Green

Write-Host ""
Write-Host "=== Collecting secrets from Google Secret Manager ===" -ForegroundColor Yellow

# Get Cloud SQL instance connection string
try {
    $CLOUD_SQL_INSTANCE = gcloud secrets versions access latest --secret="CLOUDSQL_INSTANCE" 2>$null
    Write-Host "✓ CLOUD_SQL_INSTANCE: $CLOUD_SQL_INSTANCE" -ForegroundColor Green
}
catch {
    Write-Host "✗ Failed to retrieve CLOUD_SQL_INSTANCE" -ForegroundColor Red
    exit 1
}

# Get database password
try {
    $DB_PASSWORD = gcloud secrets versions access latest --secret="DB_PASSWORD" 2>$null
    Write-Host "✓ DB_PASSWORD: [HIDDEN]" -ForegroundColor Green
}
catch {
    Write-Host "✗ Failed to retrieve DB_PASSWORD" -ForegroundColor Red
    exit 1
}

# Set other environment variables
$DB_NAME = "my_auth_db"
$DB_USER = "fincore_app"
$PORT = "8080"

Write-Host ""
Write-Host "=== Environment Variables Set ===" -ForegroundColor Cyan
Write-Host "CLOUD_SQL_INSTANCE=$CLOUD_SQL_INSTANCE"
Write-Host "DB_NAME=$DB_NAME"
Write-Host "DB_USER=$DB_USER"
Write-Host "PORT=$PORT"
Write-Host "DB_PASSWORD=[HIDDEN]"

Write-Host ""
Write-Host "=== Testing Cloud SQL connection locally ===" -ForegroundColor Yellow

# Check gcloud authentication
Write-Host "Checking gcloud authentication..." -ForegroundColor Cyan
gcloud auth list

Write-Host ""
Write-Host "Checking if Application Default Credentials are set..." -ForegroundColor Cyan
try {
    $adcStatus = gcloud auth application-default print-access-token 2>$null
    Write-Host "✓ Application Default Credentials are already set" -ForegroundColor Green
}
catch {
    Write-Host "Setting up Application Default Credentials for local development..." -ForegroundColor Yellow
    Write-Host "Please complete the authentication flow in your browser..." -ForegroundColor Magenta
    gcloud auth application-default login
}

Write-Host ""
Write-Host "=== Starting Spring Boot application with built-in Cloud SQL integration ===" -ForegroundColor Green
Write-Host "Profile: gcp-builtin" -ForegroundColor Cyan
Write-Host "This will use the SocketFactory to connect directly to Cloud SQL" -ForegroundColor Cyan

# Set environment variables for the current session
$env:CLOUD_SQL_INSTANCE = $CLOUD_SQL_INSTANCE
$env:DB_NAME = $DB_NAME
$env:DB_USER = $DB_USER
$env:DB_PASSWORD = $DB_PASSWORD
$env:PORT = $PORT

Write-Host ""
Write-Host "Starting Maven with Spring Boot..." -ForegroundColor Green

# Start the application with the gcp-builtin profile
& mvn spring-boot:run "-Dspring-boot.run.profiles=gcp-builtin" "-Dspring-boot.run.jvmArguments=-DCLOUD_SQL_INSTANCE=$CLOUD_SQL_INSTANCE -DDB_NAME=$DB_NAME -DDB_USER=$DB_USER -DDB_PASSWORD=$DB_PASSWORD -DPORT=$PORT"