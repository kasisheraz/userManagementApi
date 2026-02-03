Write-Host "Testing Cloud SQL Authentication..." -ForegroundColor Cyan
Write-Host "Host: 34.89.96.239:3306" -ForegroundColor Gray
Write-Host "Database: fincore_db" -ForegroundColor Gray
Write-Host "User: fincore_app" -ForegroundColor Gray
Write-Host ""

# Test TCP connectivity first
Write-Host "[1/2] Testing TCP connectivity..." -ForegroundColor Yellow
$tcpTest = Test-NetConnection -ComputerName 34.89.96.239 -Port 3306 -WarningAction SilentlyContinue
if ($tcpTest.TcpTestSucceeded) {
    Write-Host "✓ TCP connection successful" -ForegroundColor Green
} else {
    Write-Host "✗ TCP connection failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/2] Testing MySQL authentication with Spring Boot..." -ForegroundColor Yellow
Write-Host ""

# Set environment and run Spring Boot
$jdbcUrl = "jdbc:mysql://34.89.96.239:3306/fincore_db?useSSL=true&requireSSL=true"
$env:SPRING_DATASOURCE_URL = $jdbcUrl
$env:SPRING_DATASOURCE_USERNAME = "fincore_app"
$env:SPRING_DATASOURCE_PASSWORD = "FinCore2024Secure"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
$env:SERVER_PORT = "8081"

# Build if needed
if (-not (Test-Path "target\user-management-api-1.0.0.jar")) {
    Write-Host "Building application..." -ForegroundColor Gray
    mvn package -DskipTests -q
}

# Start application with timeout
Write-Host "Starting Spring Boot (will auto-stop after 15 seconds)..." -ForegroundColor Gray
$job = Start-Job -ScriptBlock {
    param($jarPath, $url, $user, $pass, $port)
    $env:SPRING_DATASOURCE_URL = $url
    $env:SPRING_DATASOURCE_USERNAME = $user
    $env:SPRING_DATASOURCE_PASSWORD = $pass
    $env:SERVER_PORT = $port
    java -jar $jarPath 2>&1
} -ArgumentList (Resolve-Path "target\user-management-api-1.0.0.jar"), $jdbcUrl, $env:SPRING_DATASOURCE_USERNAME, $env:SPRING_DATASOURCE_PASSWORD, $env:SERVER_PORT

# Wait and check output
Start-Sleep -Seconds 15
$output = Receive-Job $job | Out-String
Stop-Job $job
Remove-Job $job

# Check results
if ($output -match "Started UserManagementApplication") {
    Write-Host ""
    Write-Host "==================================" -ForegroundColor Green
    Write-Host "✓ AUTHENTICATION SUCCESSFUL" -ForegroundColor Green
    Write-Host "==================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Password is correct. Safe to deploy!" -ForegroundColor Green
    exit 0
} elseif ($output -match "Access denied.*using password: YES") {
    Write-Host ""
    Write-Host "==================================" -ForegroundColor Red
    Write-Host "✗ AUTHENTICATION FAILED" -ForegroundColor Red
    Write-Host "==================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Password mismatch detected! DO NOT DEPLOY!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error details:" -ForegroundColor Yellow
    $output -split "`n" | Select-String "Access denied" | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    exit 1
} else {
    Write-Host ""
    Write-Host "Could not determine authentication status. Check logs:" -ForegroundColor Yellow
    Write-Host $output
    exit 1
}
