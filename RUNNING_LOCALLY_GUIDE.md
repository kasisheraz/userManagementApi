# Running Locally with MySQL - Workaround Guide

## Current Issue
Maven compilation fails due to **Lombok + Java 21 incompatibility**. This is a known bug that blocks command-line builds.

## ✅ Solution: Use IntelliJ IDEA or Eclipse

Both IDEs have built-in Lombok support that bypasses the Maven compilation issue.

---

## Option 1: Run in IntelliJ IDEA (Recommended)

### 1. Import Project
```
1. Open IntelliJ IDEA
2. File → Open → Select userManagementApi folder
3. Wait for Maven import to complete
```

### 2. Enable Lombok Plugin
```
1. File → Settings → Plugins
2. Search for "Lombok"
3. Install if not already installed
4. Restart IntelliJ
```

### 3. Enable Annotation Processing
```
1. File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Check "Enable annotation processing"
3. Click OK
```

### 4. Configure Run Configuration
```
1. Run → Edit Configurations
2. Click "+" → Spring Boot
3. Name: "Local MySQL"
4. Main class: com.fincore.usermgmt.UserManagementApplication
5. Environment variables: SPRING_PROFILES_ACTIVE=local;MYSQL_PASSWORD=root
6. Click OK
```

### 5. Update MySQL Password
Edit `application-local.yml` or set environment variable:
```bash
MYSQL_PASSWORD=your_mysql_password
```

### 6. Run Application
```
1. Click the green ▶ Run button
2. Or press Shift+F10
3. Application will start on http://localhost:8080
```

---

## Option 2: Run in Eclipse

### 1. Import Maven Project
```
1. File → Import → Maven → Existing Maven Projects
2. Select userManagementApi folder
3. Click Finish
```

### 2. Install Lombok
```
1. Download lombok.jar from https://projectlombok.org/download
2. Double-click lombok.jar
3. Click "Install/Update"
4. Restart Eclipse
```

### 3. Run Application
```
1. Right-click on UserManagementApplication.java
2. Run As → Java Application
3. Or Run As → Spring Boot App
```

### 4. Set Environment Variables
```
1. Right-click project → Run As → Run Configurations
2. Environment tab
3. Add: SPRING_PROFILES_ACTIVE=local
4. Add: MYSQL_PASSWORD=root
5. Apply and Run
```

---

## Option 3: Use Docker (Alternative)

If you don't want to use an IDE, build inside Docker with Java 17:

### Create Dockerfile-build
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run
```powershell
# Build in Docker (uses Java 17 for compilation)
docker build -f Dockerfile-build -t user-mgmt-api .

# Run with MySQL
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e MYSQL_PASSWORD=root \
  --network host \
  user-mgmt-api
```

---

## MySQL Setup (Required for All Options)

### 1. Ensure MySQL is Running
```powershell
# Check MySQL status
Get-Service MySQL*

# Start MySQL if stopped
Start-Service MySQL80
```

### 2. Create Database
```sql
mysql -uroot -p

CREATE DATABASE IF NOT EXISTS fincore_db;
USE fincore_db;

-- Run your schema creation scripts
SOURCE complete-entity-schema.sql;
```

### 3. Verify Connection
```powershell
mysql -uroot -p -e "USE fincore_db; SHOW TABLES;"
```

Expected tables:
- users
- customer_kyc_verification
- aml_screening_results
- customer_answers
- questionnaire_questions
- kyc_documents
- address
- roles
- otp_tokens

---

## Testing the Application

### 1. Health Check
```powershell
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 2. Use Postman Collection
1. Import `phase2-postman-collection.json` into Postman
2. Follow `PHASE2_POSTMAN_GUIDE.md` for testing all endpoints
3. Start with Authentication → Get JWT token
4. Test KYC, Questionnaire, and Answer APIs

---

## Configuration Files

### application-local.yml (MySQL)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fincore_db
    username: root
    password: ${MYSQL_PASSWORD:root}
  jpa:
    hibernate:
      ddl-auto: validate  # Use existing schema
    show-sql: true
```

### Environment Variables
```
SPRING_PROFILES_ACTIVE=local
MYSQL_PASSWORD=your_password_here
```

---

## Troubleshooting

### Issue: "Cannot connect to database"
**Solutions:**
1. Check MySQL is running: `Get-Service MySQL*`
2. Verify credentials in application-local.yml
3. Test connection: `mysql -uroot -p`
4. Check database exists: `SHOW DATABASES;`

### Issue: "Table doesn't exist"
**Solution:**
Run schema creation:
```sql
SOURCE complete-entity-schema.sql;
```

### Issue: "Port 8080 already in use"
**Solutions:**
1. Change port in application-local.yml: `server.port: 8081`
2. Kill existing process: `Stop-Process -Name java -Force`

### Issue: "Lombok annotations not recognized in IDE"
**IntelliJ:**
1. File → Settings → Plugins → Install Lombok
2. File → Settings → Compiler → Annotation Processors → Enable

**Eclipse:**
1. Download and run lombok.jar installer
2. Restart Eclipse

---

## Quick Start Summary

**Fastest way to run locally:**

1. ✅ Open project in **IntelliJ IDEA**
2. ✅ Enable **Lombok plugin** and **annotation processing**
3. ✅ Set environment variables:
   ```
   SPRING_PROFILES_ACTIVE=local
   MYSQL_PASSWORD=root
   ```
4. ✅ Run **UserManagementApplication** main class
5. ✅ Import **phase2-postman-collection.json** into Postman
6. ✅ Test all APIs using Postman guide

---

## Files Created for You

1. ✅ **start-local-mysql.ps1** - PowerShell script to build & run (blocked by Lombok)
2. ✅ **phase2-postman-collection.json** - Complete Postman collection (31 endpoints)
3. ✅ **PHASE2_POSTMAN_GUIDE.md** - Comprehensive API testing guide
4. ✅ **RUNNING_TESTS_IN_IDE.md** - Guide for running unit tests in VS Code

---

## Next Steps

1. Choose Option 1 (IntelliJ) or Option 2 (Eclipse)
2. Set up MySQL database
3. Run the application
4. Import Postman collection
5. Test Phase 2 APIs

**Note:** Maven CLI won't work until Lombok releases Java 21 fix (expected in version 1.18.36+). Use IDE for now.
