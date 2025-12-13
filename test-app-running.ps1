# Simple script to test if app is running
Start-Sleep -Seconds 20

Write-Host "Testing health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ SUCCESS! Health endpoint responded:"
    Write-Host $response | ConvertTo-Json -Depth 10
    exit 0
} catch {
    Write-Host "❌ FAILED to connect: $($_)"
    exit 1
}
