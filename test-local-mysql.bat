@echo off
echo === Testing Local MySQL Connection ===

REM Add MySQL to PATH
set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin"
if exist "%MYSQL_PATH%\mysql.exe" (
    set "PATH=%PATH%;%MYSQL_PATH%"
    echo Added MySQL to PATH
)

echo Testing common MySQL passwords...

REM Test empty password
echo Testing with empty password...
echo SHOW DATABASES; | mysql -u root 2>nul
if %errorlevel%==0 (
    echo SUCCESS: Empty password works!
    set DB_PASSWORD=
    goto :success
)

REM Test common passwords
for %%p in ("" "root" "admin" "password" "123456" "abc123") do (
    echo Testing password: %%p
    echo SHOW DATABASES; | mysql -u root -p%%p 2>nul
    if !errorlevel!==0 (
        echo SUCCESS: Password %%p works!
        set DB_PASSWORD=%%p
        goto :success
    )
)

echo No common passwords worked. Please enter your MySQL password manually.
set /p DB_PASSWORD="Enter MySQL root password: "

:success
echo.
echo === Database Configuration ===
echo Host: localhost
echo Port: 3306  
echo Database: my_auth_db
echo User: root
echo Password: %DB_PASSWORD%

echo.
echo === Testing Database Connection ===
echo SHOW DATABASES; | mysql -u root -p%DB_PASSWORD%
if %errorlevel%==0 (
    echo Database connection successful!
    
    echo Checking if my_auth_db exists...
    echo SHOW DATABASES LIKE 'my_auth_db'; | mysql -u root -p%DB_PASSWORD% | findstr my_auth_db >nul
    if %errorlevel%==0 (
        echo Database 'my_auth_db' exists!
    ) else (
        echo Creating database 'my_auth_db'...
        echo CREATE DATABASE IF NOT EXISTS my_auth_db; | mysql -u root -p%DB_PASSWORD%
    )
    
    echo.
    echo === Starting Spring Boot Application ===
    set SPRING_PROFILES_ACTIVE=mysql
    set DB_HOST=localhost
    set DB_PORT=3306
    set DB_NAME=my_auth_db
    set DB_USER=root
    set PORT=8080
    
    echo Starting with profile: mysql
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
) else (
    echo Database connection failed!
)