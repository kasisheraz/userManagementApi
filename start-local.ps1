# Start the User Management API locally
Write-Host "Building the application..." -ForegroundColor Cyan
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nStarting application on http://localhost:8080" -ForegroundColor Green
    Write-Host "H2 Console: http://localhost:8080/h2-console" -ForegroundColor Yellow
    Write-Host "Health Check: http://localhost:8080/actuator/health" -ForegroundColor Yellow
    Write-Host "`nPress Ctrl+C to stop the application`n" -ForegroundColor Cyan
    
    $env:SPRING_PROFILES_ACTIVE="local-h2"
    java -jar target/user-management-api-1.0.0.jar
} else {
    Write-Host "`nBuild failed! Please check the errors above." -ForegroundColor Red
}
