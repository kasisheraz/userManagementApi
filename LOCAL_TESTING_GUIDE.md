# Local Testing Guide - Before Cloud Run Deployment

## Overview

This guide walks you through testing the User Management API locally in a Docker container before deploying to Google Cloud Run. This ensures everything works correctly before production deployment.

## Prerequisites

- Java 21 installed
- Maven 3.9+ installed
- Docker installed and running
- Git repository clean and ready
- curl or Postman for endpoint testing

## Testing Phases

### Phase 1: Application Build & Unit Tests (5 minutes)
### Phase 2: Docker Image Build (5 minutes)
### Phase 3: Docker Container Testing (10 minutes)
### Phase 4: Endpoint Testing (10 minutes)
### Phase 5: Cleanup (5 minutes)

**Total Time: ~35 minutes**

---

## Phase 1: Application Build & Unit Tests

### Step 1.1: Clean Build
```bash
cd c:\Development\git\userManagementApi

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

### Step 1.2: Run All Tests
```bash
# Run unit and integration tests
mvn test

# Expected output: All tests pass
# Example: [INFO] Tests run: XX, Failures: 0, Errors: 0
```

### Step 1.3: Package Application
```bash
# Build JAR file
mvn clean package -DskipTests

# Expected: JAR created in target/
# File: target/user-management-api-1.0.0.jar
```

### Verification Checklist Phase 1
- [ ] `mvn clean compile` succeeds
- [ ] `mvn test` passes all tests
- [ ] `mvn package` creates JAR file
- [ ] No compilation errors
- [ ] No test failures

---

## Phase 2: Docker Image Build

### Step 2.1: Verify Docker is Running
```bash
docker --version
docker ps
```

### Step 2.2: Build Docker Image
```bash
# Navigate to project root
cd c:\Development\git\userManagementApi

# Build image
docker build -t user-management-api:latest .

# Expected output:
# Successfully built <image-id>
# Successfully tagged user-management-api:latest
```

### Step 2.3: Verify Image
```bash
# List Docker images
docker images | grep user-management-api

# Expected: user-management-api   latest   <image-id>   <size>
```

### Step 2.4: Check Image Size
```bash
docker images --format "table {{.Repository}}\t{{.Size}}" | grep user-management-api

# Expected: Should be < 200MB (optimized multi-stage build)
```

### Verification Checklist Phase 2
- [ ] Docker daemon running
- [ ] `docker build` completes successfully
- [ ] Image is tagged as `user-management-api:latest`
- [ ] Image size is reasonable (< 200MB)
- [ ] No build errors

---

## Phase 3: Docker Container Testing

### Step 3.1: Run Container with H2 Database
```bash
# Run container with H2 (in-memory database)
docker run -d \
  --name user-mgmt-test \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=h2 \
  -e LOG_LEVEL=INFO \
  user-management-api:latest

# Expected output: Container ID (64-character hash)
```

### Step 3.2: Wait for Application Startup
```bash
# Wait 10-15 seconds for Spring Boot to start
# You can monitor logs:
docker logs -f user-mgmt-test

# Watch for: "Tomcat started on port(s): 8080"
# Press Ctrl+C to exit log view
```

### Step 3.3: Verify Container is Running
```bash
# Check container status
docker ps | grep user-mgmt-test

# Expected: Container should be running
# Status: Up X seconds
```

### Step 3.4: Check Application Logs
```bash
# View logs (most recent 50 lines)
docker logs user-mgmt-test | tail -50

# Look for:
# - "Started UserManagementApplication"
# - No "ERROR" messages
# - No "Connection refused"
```

### Step 3.5: Test Health Endpoint (First Test!)
```bash
# Test health endpoint
curl -X GET http://localhost:8080/actuator/health

# Expected Response (200 OK):
# {"status":"UP"}
```

### Step 3.6: Stop Container
```bash
# When done testing, stop the container
docker stop user-mgmt-test

# Remove it
docker rm user-mgmt-test
```

### Verification Checklist Phase 3
- [ ] Container starts successfully
- [ ] Application initializes without errors
- [ ] "Started UserManagementApplication" in logs
- [ ] Health endpoint returns 200 OK
- [ ] Response body contains {"status":"UP"}
- [ ] No connection errors in logs
- [ ] Container stops cleanly

---

## Phase 4: Endpoint Testing

### Step 4.1: Start Fresh Container
```bash
# Remove old container if exists
docker rm -f user-mgmt-test 2>/dev/null

# Start new container
docker run -d \
  --name user-mgmt-test \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=h2 \
  -e LOG_LEVEL=INFO \
  user-management-api:latest

# Wait for startup
timeout /t 10

# Verify it's running
docker ps | grep user-mgmt-test
```

### Step 4.2: Test Health Endpoint
```bash
# Health check
curl -X GET http://localhost:8080/actuator/health -v

# Expected:
# HTTP/1.1 200 OK
# {"status":"UP"}
```

### Step 4.3: Test Info Endpoint
```bash
# Application info
curl -X GET http://localhost:8080/actuator/info

# Expected:
# HTTP/1.1 200 OK
# {"app":{"name":"user-management-api",...}}
```

### Step 4.4: Test Login Endpoint
```bash
# Login with default credentials
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fincore.com","password":"admin123"}' \
  -v

# Expected:
# HTTP/1.1 200 OK
# {"accessToken":"eyJ0eXAiOiJKV1QiLCJhbGc...","tokenType":"Bearer"}
```

### Step 4.5: Store Token
```bash
# Extract and save token for next tests
# On Windows PowerShell:
$token = (curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@fincore.com","password":"admin123"}' | ConvertFrom-Json).accessToken

echo $token
```

### Step 4.6: Test Get Users Endpoint
```bash
# Get all users (using token from Step 4.5)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $token" \
  -v

# Expected:
# HTTP/1.1 200 OK
# [{"id":1,"firstName":"admin",...},...]
```

### Step 4.7: Test Get User by ID
```bash
# Get specific user
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $token" \
  -v

# Expected:
# HTTP/1.1 200 OK
# {"id":1,"firstName":"admin",...}
```

### Step 4.8: Test Create User
```bash
# Create new user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"john.doe@example.com",
    "password":"SecurePass123!"
  }' \
  -v

# Expected:
# HTTP/1.1 201 Created
# {"id":2,"firstName":"John",...}
```

### Step 4.9: Test Update User
```bash
# Update user (ID from Step 4.8)
curl -X PUT http://localhost:8080/api/users/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "firstName":"Jane",
    "lastName":"Smith"
  }' \
  -v

# Expected:
# HTTP/1.1 200 OK
# {"id":2,"firstName":"Jane",...}
```

### Step 4.10: Test Validation
```bash
# Test invalid request (missing required field)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "firstName":"Test"
  }' \
  -v

# Expected:
# HTTP/1.1 400 Bad Request
# {"timestamp":"...","message":"..."}
```

### Step 4.11: Test Error Handling
```bash
# Test non-existent user
curl -X GET http://localhost:8080/api/users/99999 \
  -H "Authorization: Bearer $token" \
  -v

# Expected:
# HTTP/1.1 404 Not Found
```

### Step 4.12: Check Container Logs
```bash
# Verify no errors in application logs
docker logs user-mgmt-test | grep -i error

# Expected: No ERROR messages
```

### Verification Checklist Phase 4
- [ ] Health endpoint returns 200 OK
- [ ] Info endpoint returns 200 OK
- [ ] Login endpoint works, returns JWT token
- [ ] Get users endpoint works (returns list)
- [ ] Get user by ID works
- [ ] Create user works (201 Created)
- [ ] Update user works (200 OK)
- [ ] Validation works (400 Bad Request for invalid input)
- [ ] Error handling works (404 for non-existent resource)
- [ ] No error messages in application logs
- [ ] All responses have correct status codes
- [ ] All responses have expected data format

---

## Phase 5: Cleanup

### Step 5.1: Stop and Remove Container
```bash
# Stop container
docker stop user-mgmt-test

# Remove container
docker rm user-mgmt-test

# Verify removed
docker ps | grep user-mgmt-test
# Expected: No output
```

### Step 5.2: Keep or Remove Image
```bash
# Option A: Keep image for later testing
# docker images | grep user-management-api
# (Image remains for next test cycle)

# Option B: Remove image to save disk space
docker rmi user-management-api:latest

# Verify removed
docker images | grep user-management-api
# Expected: No output
```

### Verification Checklist Phase 5
- [ ] Container stopped successfully
- [ ] Container removed
- [ ] Image kept or removed as desired
- [ ] No dangling containers
- [ ] No dangling images

---

## Complete Testing Script (Windows PowerShell)

Save as `test-local.ps1`:

```powershell
# Local Testing Script for User Management API

param(
    [string]$ImageName = "user-management-api",
    [string]$ContainerName = "user-mgmt-test",
    [int]$Port = 8080,
    [string]$Profile = "h2"
)

Write-Host "==========================================" -ForegroundColor Blue
Write-Host "Local Testing - User Management API" -ForegroundColor Blue
Write-Host "==========================================" -ForegroundColor Blue

# Phase 1: Build
Write-Host "`n[Phase 1] Building Application..." -ForegroundColor Green
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful" -ForegroundColor Green

# Phase 2: Docker Build
Write-Host "`n[Phase 2] Building Docker Image..." -ForegroundColor Green
docker build -t ${ImageName}:latest .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker image built successfully" -ForegroundColor Green

# Phase 3: Cleanup old container
Write-Host "`n[Phase 3] Cleaning up old container..." -ForegroundColor Green
docker rm -f $ContainerName 2>$null
Write-Host "✓ Old container removed" -ForegroundColor Green

# Phase 4: Run Container
Write-Host "`n[Phase 4] Starting container..." -ForegroundColor Green
docker run -d `
  --name $ContainerName `
  -p ${Port}:8080 `
  -e SPRING_PROFILES_ACTIVE=$Profile `
  -e LOG_LEVEL=INFO `
  ${ImageName}:latest

if ($LASTEXITCODE -ne 0) {
    Write-Host "Container startup failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Container started" -ForegroundColor Green

# Wait for application startup
Write-Host "`n[Phase 5] Waiting for application startup..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Phase 6: Test Health Endpoint
Write-Host "`n[Phase 6] Testing health endpoint..." -ForegroundColor Green
$healthResponse = curl -X GET "http://localhost:${Port}/actuator/health" -s
if ($healthResponse -contains '"status":"UP"') {
    Write-Host "✓ Health check passed" -ForegroundColor Green
} else {
    Write-Host "✗ Health check failed!" -ForegroundColor Red
    docker logs $ContainerName | tail -20
    exit 1
}

# Phase 7: Test Login Endpoint
Write-Host "`n[Phase 7] Testing login endpoint..." -ForegroundColor Green
$loginResponse = curl -X POST "http://localhost:${Port}/api/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@fincore.com","password":"admin123"}' -s | ConvertFrom-Json

if ($loginResponse.accessToken) {
    Write-Host "✓ Login successful, token obtained" -ForegroundColor Green
    $token = $loginResponse.accessToken
} else {
    Write-Host "✗ Login failed!" -ForegroundColor Red
    exit 1
}

# Phase 8: Test Get Users
Write-Host "`n[Phase 8] Testing get users endpoint..." -ForegroundColor Green
$usersResponse = curl -X GET "http://localhost:${Port}/api/users" `
  -H "Authorization: Bearer $token" -s | ConvertFrom-Json

if ($usersResponse -is [array]) {
    Write-Host "✓ Get users successful, found $($usersResponse.Count) users" -ForegroundColor Green
} else {
    Write-Host "✗ Get users failed!" -ForegroundColor Red
    exit 1
}

# Phase 9: Summary
Write-Host "`n==========================================" -ForegroundColor Blue
Write-Host "✓ All local tests passed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Blue
Write-Host "`nContainer is running at: http://localhost:${Port}" -ForegroundColor Cyan
Write-Host "To stop: docker stop $ContainerName" -ForegroundColor Cyan
Write-Host "To remove: docker rm $ContainerName" -ForegroundColor Cyan
Write-Host "`nReady for Cloud Run deployment!" -ForegroundColor Green
```

### Run Local Testing Script
```bash
# PowerShell
.\test-local.ps1

# Or with custom values
.\test-local.ps1 -ImageName user-management-api -Port 8080 -Profile h2
```

---

## Complete Testing Script (Bash - Linux/macOS)

Save as `test-local.sh`:

```bash
#!/bin/bash

set -e

# Configuration
IMAGE_NAME="${1:-user-management-api}"
CONTAINER_NAME="${2:-user-mgmt-test}"
PORT="${3:-8080}"
PROFILE="${4:-h2}"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Local Testing - User Management API${NC}"
echo -e "${BLUE}========================================${NC}"

# Phase 1: Build
echo -e "\n${GREEN}[Phase 1] Building Application...${NC}"
mvn clean package -DskipTests -q
echo -e "${GREEN}✓ Build successful${NC}"

# Phase 2: Docker Build
echo -e "\n${GREEN}[Phase 2] Building Docker Image...${NC}"
docker build -t ${IMAGE_NAME}:latest .
echo -e "${GREEN}✓ Docker image built successfully${NC}"

# Phase 3: Cleanup
echo -e "\n${GREEN}[Phase 3] Cleaning up old container...${NC}"
docker rm -f $CONTAINER_NAME 2>/dev/null || true
echo -e "${GREEN}✓ Old container removed${NC}"

# Phase 4: Run Container
echo -e "\n${GREEN}[Phase 4] Starting container...${NC}"
docker run -d \
  --name $CONTAINER_NAME \
  -p ${PORT}:8080 \
  -e SPRING_PROFILES_ACTIVE=$PROFILE \
  -e LOG_LEVEL=INFO \
  ${IMAGE_NAME}:latest

echo -e "${GREEN}✓ Container started${NC}"

# Phase 5: Wait for startup
echo -e "\n${YELLOW}[Phase 5] Waiting for application startup...${NC}"
sleep 10

# Phase 6: Health check
echo -e "\n${GREEN}[Phase 6] Testing health endpoint...${NC}"
HEALTH=$(curl -s http://localhost:${PORT}/actuator/health)
if echo "$HEALTH" | grep -q "UP"; then
    echo -e "${GREEN}✓ Health check passed${NC}"
else
    echo -e "${RED}✗ Health check failed!${NC}"
    docker logs $CONTAINER_NAME | tail -20
    exit 1
fi

# Phase 7: Login
echo -e "\n${GREEN}[Phase 7] Testing login endpoint...${NC}"
LOGIN=$(curl -s -X POST http://localhost:${PORT}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fincore.com","password":"admin123"}')

TOKEN=$(echo $LOGIN | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
if [ ! -z "$TOKEN" ]; then
    echo -e "${GREEN}✓ Login successful, token obtained${NC}"
else
    echo -e "${RED}✗ Login failed!${NC}"
    exit 1
fi

# Phase 8: Get Users
echo -e "\n${GREEN}[Phase 8] Testing get users endpoint...${NC}"
USERS=$(curl -s http://localhost:${PORT}/api/users \
  -H "Authorization: Bearer $TOKEN")

if echo "$USERS" | grep -q "id"; then
    echo -e "${GREEN}✓ Get users successful${NC}"
else
    echo -e "${RED}✗ Get users failed!${NC}"
    exit 1
fi

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All local tests passed!${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${YELLOW}Container is running at: http://localhost:${PORT}${NC}"
echo -e "${YELLOW}To stop: docker stop $CONTAINER_NAME${NC}"
echo -e "${YELLOW}To remove: docker rm $CONTAINER_NAME${NC}"
echo -e "\n${GREEN}Ready for Cloud Run deployment!${NC}"
```

### Run Script
```bash
chmod +x test-local.sh
./test-local.sh
```

---

## Testing Checklist Summary

### Pre-Testing
- [ ] Java 21 installed
- [ ] Maven 3.9+ installed
- [ ] Docker installed and running
- [ ] Git repository clean
- [ ] Ports 8080 available (no conflicts)

### Phase 1: Build
- [ ] `mvn clean compile` succeeds
- [ ] `mvn test` passes
- [ ] `mvn package` creates JAR

### Phase 2: Docker Build
- [ ] `docker build` succeeds
- [ ] Image is tagged correctly
- [ ] Image size is reasonable

### Phase 3: Container Startup
- [ ] Container starts successfully
- [ ] Application initializes
- [ ] No startup errors in logs
- [ ] Health endpoint responds

### Phase 4: API Testing
- [ ] Health endpoint returns 200
- [ ] Login endpoint returns JWT
- [ ] Get users returns data
- [ ] Get user by ID works
- [ ] Create user works
- [ ] Update user works
- [ ] Validation works
- [ ] Error handling works

### Phase 5: Cleanup
- [ ] Container stops cleanly
- [ ] Container removed
- [ ] Image removed or kept

---

## Troubleshooting Local Testing

### Issue: Port 8080 already in use
```bash
# Find process using port 8080
# Windows:
netstat -ano | findstr :8080

# Linux/macOS:
lsof -i :8080

# Kill process (Windows):
taskkill /PID <PID> /F

# Or use different port:
docker run -p 8081:8080 ...
```

### Issue: Docker build fails
```bash
# Clean Docker build cache
docker builder prune -a

# Rebuild
docker build -t user-management-api:latest .
```

### Issue: Container exits immediately
```bash
# Check logs
docker logs user-mgmt-test

# Look for errors in output
# Common issues: wrong Java version, missing dependencies
```

### Issue: Health endpoint returns error
```bash
# Check full logs
docker logs user-mgmt-test | tail -50

# Check database initialization
# Look for: "H2 database initialized"
```

### Issue: Login fails
```bash
# Verify data is initialized
# Check logs for: "data.sql loaded"

# Test with different credentials
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

---

## Success Criteria

✅ **All tests must pass before Cloud deployment:**

1. Maven build succeeds
2. All unit tests pass
3. Docker image builds successfully
4. Container starts without errors
5. Health endpoint returns 200 OK
6. Login endpoint returns JWT token
7. All CRUD operations work
8. Validation works correctly
9. Error handling works
10. No errors in application logs

---

## Next Steps After Local Testing

Once all local tests pass:

1. ✅ Review `CLOUD_RUN_DEPLOYMENT.md`
2. ✅ Configure `gcp-config.env`
3. ✅ Run `setup-gcp-infrastructure.sh`
4. ✅ Run `deploy-to-cloud-run.sh`
5. ✅ Test Cloud Run deployment

---

## Quick Reference

| Command | Purpose |
|---------|---------|
| `mvn clean package -DskipTests` | Build JAR |
| `docker build -t user-management-api .` | Build image |
| `docker run -p 8080:8080 user-management-api` | Run container |
| `docker logs user-mgmt-test` | View logs |
| `curl http://localhost:8080/actuator/health` | Test health |
| `docker stop user-mgmt-test` | Stop container |
| `docker rm user-mgmt-test` | Remove container |

---

**Status**: Ready for local testing
**Estimated Time**: 35 minutes
**Success Criteria**: All endpoints working, no errors in logs
**Next**: Cloud Run deployment (after local tests pass)
