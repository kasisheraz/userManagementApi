#!/bin/bash

# Local Testing Script for Cloud SQL Built-in Integration
# This script collects secrets from Google Secret Manager and tests locally

echo "=== Setting up local environment for Cloud SQL Built-in Integration ==="

# Set project context
export PROJECT_ID="project-07a61357-b791-4255-a9e"
export REGION="europe-west2"

echo "✓ Using Project: $PROJECT_ID"
echo "✓ Using Region: $REGION"

# Collect database details from Google Secret Manager
echo ""
echo "=== Collecting secrets from Google Secret Manager ==="

# Get Cloud SQL instance connection string
export CLOUD_SQL_INSTANCE=$(gcloud secrets versions access latest --secret="CLOUDSQL_INSTANCE")
echo "✓ CLOUD_SQL_INSTANCE: $CLOUD_SQL_INSTANCE"

# Get database password
export DB_PASSWORD=$(gcloud secrets versions access latest --secret="DB_PASSWORD")
echo "✓ DB_PASSWORD: [HIDDEN]"

# Set other environment variables
export DB_NAME="my_auth_db"
export DB_USER="fincore_app"
export PORT="8080"

echo ""
echo "=== Environment Variables Set ==="
echo "CLOUD_SQL_INSTANCE=$CLOUD_SQL_INSTANCE"
echo "DB_NAME=$DB_NAME"
echo "DB_USER=$DB_USER"
echo "PORT=$PORT"
echo "DB_PASSWORD=[HIDDEN]"

echo ""
echo "=== Testing Cloud SQL connection locally ==="

# For local testing with built-in integration, we need to:
# 1. Ensure we're authenticated with gcloud
# 2. Use Application Default Credentials (ADC)

echo "Checking gcloud authentication..."
gcloud auth list

echo ""
echo "Setting up Application Default Credentials for local development..."
gcloud auth application-default login

echo ""
echo "=== Starting Spring Boot application with built-in Cloud SQL integration ==="
echo "Profile: gcp-builtin"
echo "This will use the SocketFactory to connect directly to Cloud SQL"

# Start the application with the gcp-builtin profile
mvn spring-boot:run -Dspring-boot.run.profiles=gcp-builtin \
    -Dspring-boot.run.jvmArguments="-DCLOUD_SQL_INSTANCE=$CLOUD_SQL_INSTANCE -DDB_NAME=$DB_NAME -DDB_USER=$DB_USER -DDB_PASSWORD=$DB_PASSWORD -DPORT=$PORT"