#!/bin/bash

# Test Built-in Cloud SQL Connector Locally
# This script tests the new Cloud SQL Java connector approach

echo "🔧 Testing Built-in Cloud SQL Connector Approach"
echo "================================================="

# Set environment variables for testing
export SPRING_PROFILES_ACTIVE=gcp-builtin
export DB_NAME=my_auth_db
export DB_USER=fincore_app
export CLOUD_SQL_INSTANCE=project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db

# Check if gcloud is authenticated
echo "1. Checking GCP authentication..."
if ! gcloud auth print-access-token > /dev/null 2>&1; then
    echo "❌ Not authenticated with GCP. Please run:"
    echo "   gcloud auth login"
    echo "   gcloud auth application-default login"
    exit 1
fi

echo "✅ GCP authentication verified"

# Verify Cloud SQL instance exists
echo "2. Verifying Cloud SQL instance..."
if ! gcloud sql instances describe fincore-npe-db --quiet > /dev/null 2>&1; then
    echo "❌ Cloud SQL instance 'fincore-npe-db' not found"
    exit 1
fi

echo "✅ Cloud SQL instance verified"

# Get database password from environment or prompt
if [ -z "$DB_PASSWORD" ]; then
    echo "3. Database password required..."
    echo -n "Enter database password for user '$DB_USER': "
    read -s DB_PASSWORD
    echo
    export DB_PASSWORD
fi

# Build the application
echo "4. Building application..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

# Test connection with built-in connector
echo "5. Testing built-in Cloud SQL connector..."
echo "   Profile: gcp-builtin"
echo "   Instance: $CLOUD_SQL_INSTANCE"
echo "   Database: $DB_NAME"
echo "   User: $DB_USER"

# Start the application in test mode
timeout 30s mvn spring-boot:run -Dspring-boot.run.profiles=gcp-builtin -q &
APP_PID=$!

# Wait for application to start
sleep 15

# Test health endpoint
echo "6. Testing health endpoint..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "✅ Health check passed - Built-in connector working!"
    
    # Test database connection indirectly via API
    echo "7. Testing database connection via API..."
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"Admin@123456"}')
    
    if echo "$LOGIN_RESPONSE" | grep -q "token"; then
        echo "✅ Database connection verified - Login successful!"
    else
        echo "❌ Database connection test failed"
        echo "Response: $LOGIN_RESPONSE"
    fi
else
    echo "❌ Health check failed"
fi

# Clean up
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo ""
echo "🎯 Test Summary:"
echo "   Built-in Cloud SQL Java connector tested"
echo "   Connection string: jdbc:mysql://google/$DB_NAME"
echo "   Socket factory: com.google.cloud.sql.mysql.SocketFactory"
echo ""
echo "Next steps:"
echo "   1. If tests pass, update GitHub Actions to use gcp-builtin profile"
echo "   2. Remove public IP dependency from Cloud SQL instance"
echo "   3. Deploy to Cloud Run with built-in connector"