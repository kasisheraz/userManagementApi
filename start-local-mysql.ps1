# Start the User Management API locally with MySQL
# This script runs the application with the 'local' profile which uses MySQL

Write-Host "`n=== Building User Management API ===" -ForegroundColor Cyan
Write-Host "Using Java 21 and Maven..." -ForegroundColor Yellow

# Build the application
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== Starting Application ===" -ForegroundColor Green
    Write-Host "Profile: local (MySQL)" -ForegroundColor Yellow
    Write-Host "Port: http://localhost:8080" -ForegroundColor Yellow
    Write-Host "Health: http://localhost:8080/actuator/health" -ForegroundColor Yellow
    Write-Host "`nPress Ctrl+C to stop`n" -ForegroundColor Cyan
    
    # Set the active profile to 'local' (MySQL)
    $env:SPRING_PROFILES_ACTIVE = "local"
    
    # Set MySQL password if not already set
    if (-not $env:MYSQL_PASSWORD) {
        Write-Host "Note: Using empty MySQL password. Set MYSQL_PASSWORD environment variable if needed." -ForegroundColor Yellow
    }
    
    # Start the application
    java -jar target\user-management-api-1.0.0.jar
} else {
    Write-Host "`n=== Build Failed ===" -ForegroundColor Red
    Write-Host "Please check the errors above." -ForegroundColor Red
    exit 1
}
