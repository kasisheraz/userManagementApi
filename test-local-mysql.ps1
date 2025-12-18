# Test script for local MySQL database
Write-Host "=== Testing Local MySQL Database Connection ===" -ForegroundColor Green

# Add MySQL to PATH if not already there
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin"
if ((Test-Path "$mysqlPath\mysql.exe") -and ($env:PATH -notlike "*$mysqlPath*")) {
    $env:PATH += ";$mysqlPath"
    Write-Host "Added MySQL to PATH: $mysqlPath" -ForegroundColor Cyan
}

# Prompt for local database password if not set
if (-not $env:LOCAL_DB_PASSWORD) {
    $securePassword = Read-Host "Enter your local MySQL root password" -AsSecureString
    $env:LOCAL_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword))
}

# Set local database environment variables
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "my_auth_db"
$env:DB_USER = "root"
$env:DB_PASSWORD = $env:LOCAL_DB_PASSWORD
$env:PORT = "8080"

Write-Host "Database Configuration:" -ForegroundColor Cyan
Write-Host "Host: $env:DB_HOST" -ForegroundColor Yellow
Write-Host "Port: $env:DB_PORT" -ForegroundColor Yellow
Write-Host "Database: $env:DB_NAME" -ForegroundColor Yellow
Write-Host "User: $env:DB_USER" -ForegroundColor Yellow
Write-Host "Password: $($env:DB_PASSWORD.Substring(0, [Math]::Min(3, $env:DB_PASSWORD.Length)))***" -ForegroundColor Yellow

Write-Host "`n=== Verifying Database Setup ===" -ForegroundColor Green
Write-Host "Testing database connection..." -ForegroundColor Cyan

# Test database connection
$testConnection = mysql -u $env:DB_USER -p$env:DB_PASSWORD -e "SHOW DATABASES;" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Database connection successful!" -ForegroundColor Green
    
    # Check if my_auth_db exists
    $dbExists = mysql -u $env:DB_USER -p$env:DB_PASSWORD -e "SHOW DATABASES LIKE 'my_auth_db';" 2>&1
    if ($dbExists -like "*my_auth_db*") {
        Write-Host "✅ Database 'my_auth_db' exists!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Database 'my_auth_db' does not exist. Creating it..." -ForegroundColor Yellow
        Get-Content setup-local-db.sql | mysql -u $env:DB_USER -p$env:DB_PASSWORD
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Database setup completed!" -ForegroundColor Green
        } else {
            Write-Host "❌ Database setup failed!" -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "❌ Database connection failed!" -ForegroundColor Red
    Write-Host "Error: $testConnection" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== Starting Spring Boot with MySQL profile ===" -ForegroundColor Green

# Clean compile first
Write-Host "Cleaning and compiling..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Starting application..." -ForegroundColor Green
    
    # Kill any existing Java processes
    Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
    
    # Start with mysql profile
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}