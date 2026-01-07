# Test build with JDK 21 in Docker container
Write-Host "Testing build with JDK 21 in Docker..." -ForegroundColor Cyan

# Build just the Maven build stage
docker build --target builder -t userapi-test:latest .

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Build successful with JDK 21!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Build failed!" -ForegroundColor Red
    exit 1
}
