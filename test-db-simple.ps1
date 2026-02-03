# Simple Cloud SQL Connection Test
param(
    [Parameter(Mandatory=$true)]
    [string]$Password
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cloud SQL Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Connection details
$DB_HOST = "34.89.96.239"
$DB_PORT = "3306"
$DB_NAME = "fincore_db"
$DB_USER = "fincore_app"

Write-Host "Testing connection to:" -ForegroundColor Yellow
Write-Host "  Host: $DB_HOST" -ForegroundColor White
Write-Host "  Port: $DB_PORT" -ForegroundColor White
Write-Host "  Database: $DB_NAME" -ForegroundColor White
Write-Host "  User: $DB_USER" -ForegroundColor White
Write-Host ""

# Test TCP connectivity
Write-Host "Step 1: Testing TCP connectivity..." -ForegroundColor Cyan
$tcpTest = Test-NetConnection -ComputerName $DB_HOST -Port $DB_PORT -WarningAction SilentlyContinue
if ($tcpTest.TcpTestSucceeded) {
    Write-Host "  ✓ TCP connection successful" -ForegroundColor Green
} else {
    Write-Host "  ✗ TCP connection failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test MySQL connection using mysql client
Write-Host "Step 2: Testing MySQL connection..." -ForegroundColor Cyan

# Check if mysql client is available
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "  ✗ MySQL client not found" -ForegroundColor Red
    Write-Host "  Install MySQL client or use the Java test instead" -ForegroundColor Yellow
    exit 1
}

# Test connection and run queries
$env:MYSQL_PWD = $Password
$testQuery = @"
SELECT 'Connection successful!' as Status;
SELECT DATABASE() as CurrentDatabase, USER() as CurrentUser, VERSION() as MySQLVersion;
SHOW TABLES;
"@

try {
    $result = $testQuery | mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ MySQL connection successful" -ForegroundColor Green
        Write-Host ""
        Write-Host "Query Results:" -ForegroundColor Cyan
        Write-Host $result -ForegroundColor White
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✓ ALL TESTS PASSED - Safe to deploy!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "  ✗ MySQL connection failed" -ForegroundColor Red
        Write-Host "Error:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ✗ Connection error: $_" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item Env:\MYSQL_PWD
}
