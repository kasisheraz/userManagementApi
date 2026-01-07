# Initialize Local MySQL Database
# This script creates the database and runs the schema

Write-Host "Initializing local database..." -ForegroundColor Green

# Check if MySQL is accessible
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "Error: MySQL command not found. Please ensure MySQL is installed and in your PATH" -ForegroundColor Red
    Write-Host "Common MySQL installation paths:" -ForegroundColor Yellow
    Write-Host "  - C:\Program Files\MySQL\MySQL Server 8.0\bin\" -ForegroundColor Yellow
    Write-Host "  - C:\xampp\mysql\bin\" -ForegroundColor Yellow
    exit 1
}

# Prompt for MySQL root password if not set as environment variable
if (-not $env:MYSQL_PASSWORD) {
    $password = Read-Host "Enter MySQL root password" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
    $mysqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
} else {
    $mysqlPassword = $env:MYSQL_PASSWORD
}

# Create database if it doesn't exist
Write-Host "Creating database fincore_db if it doesn't exist..." -ForegroundColor Cyan
$createDbCmd = "CREATE DATABASE IF NOT EXISTS fincore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($mysqlPassword) {
    $createDbCmd | mysql -u root -p"$mysqlPassword"
} else {
    $createDbCmd | mysql -u root
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error creating database. Please check your MySQL credentials." -ForegroundColor Red
    exit 1
}

# Run the schema script
Write-Host "Running schema.sql..." -ForegroundColor Cyan
if ($mysqlPassword) {
    Get-Content ".\src\main\resources\schema.sql" | mysql -u root -p"$mysqlPassword" fincore_db
} else {
    Get-Content ".\src\main\resources\schema.sql" | mysql -u root fincore_db
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error running schema.sql" -ForegroundColor Red
    exit 1
}

# Run the data script
Write-Host "Running data.sql..." -ForegroundColor Cyan
if ($mysqlPassword) {
    Get-Content ".\src\main\resources\data.sql" | mysql -u root -p"$mysqlPassword" fincore_db
} else {
    Get-Content ".\src\main\resources\data.sql" | mysql -u root fincore_db
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: Error running data.sql (this might be okay if you don't have seed data)" -ForegroundColor Yellow
}

Write-Host "`nDatabase initialized successfully!" -ForegroundColor Green
Write-Host "You can now start your application." -ForegroundColor Green
