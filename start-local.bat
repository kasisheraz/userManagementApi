@echo off
echo Starting User Management API with Local MySQL...

set DB_HOST=localhost
set DB_PORT=3306  
set DB_NAME=my_auth_db
set DB_USER=root
set DB_PASSWORD=abc123
set PORT=8080

echo Database Config: %DB_USER%@%DB_HOST%:%DB_PORT%/%DB_NAME%

mvn spring-boot:run -Dspring-boot.run.profiles=mysql