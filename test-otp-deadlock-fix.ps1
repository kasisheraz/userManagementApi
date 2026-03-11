# Test OTP Deadlock Fix
# This script sends multiple simultaneous OTP requests to verify the deadlock fix

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  TESTING OTP DEADLOCK FIX" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "https://fincore-npe-api-994490239798.europe-west2.run.app"
$phoneNumber = "+1234567890"
$concurrentRequests = 5

Write-Host "Testing with $concurrentRequests concurrent requests..." -ForegroundColor White
Write-Host "Phone Number: $phoneNumber`n" -ForegroundColor Gray

# Function to request OTP
$requestOtp = {
    param($url, $phone, $requestId)
    
    $body = @{
        phoneNumber = $phone
    } | ConvertTo-Json
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $response = Invoke-RestMethod -Uri "$url/api/auth/request-otp" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body `
            -TimeoutSec 30
        
        $stopwatch.Stop()
        
        return @{
            RequestId = $requestId
            Success = $true
            OTP = $response.otp
            Message = $response.message
            Duration = $stopwatch.ElapsedMilliseconds
            Error = $null
        }
    } catch {
        $stopwatch.Stop()
        
        $errorMsg = $_.Exception.Message
        if ($_.ErrorDetails.Message) {
            $errorMsg = $_.ErrorDetails.Message
        }
        
        return @{
            RequestId = $requestId
            Success = $false
            OTP = $null
            Message = $null
            Duration = $stopwatch.ElapsedMilliseconds
            Error = $errorMsg
        }
    }
}

Write-Host "Starting $concurrentRequests concurrent OTP requests..." -ForegroundColor Cyan

# Create jobs for concurrent requests
$jobs = @()
for ($i = 1; $i -le $concurrentRequests; $i++) {
    $jobs += Start-Job -ScriptBlock $requestOtp -ArgumentList $baseUrl, $phoneNumber, $i
    Start-Sleep -Milliseconds 50  # Small delay to stagger requests slightly
}

Write-Host "Waiting for all requests to complete...`n" -ForegroundColor Yellow

# Wait for all jobs and collect results
$results = $jobs | Wait-Job | Receive-Job

# Clean up jobs
$jobs | Remove-Job

# Display results
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESULTS" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

$successCount = 0
$failCount = 0
$deadlockCount = 0

foreach ($result in $results) {
    Write-Host "Request #$($result.RequestId):" -ForegroundColor White
    
    if ($result.Success) {
        $successCount++
        Write-Host "  Status: SUCCESS" -ForegroundColor Green
        Write-Host "  OTP: $($result.OTP)" -ForegroundColor Gray
        Write-Host "  Duration: $($result.Duration)ms" -ForegroundColor Gray
    } else {
        $failCount++
        Write-Host "  Status: FAILED" -ForegroundColor Red
        Write-Host "  Error: $($result.Error)" -ForegroundColor Red
        Write-Host "  Duration: $($result.Duration)ms" -ForegroundColor Gray
        
        if ($result.Error -match "deadlock|lock") {
            $deadlockCount++
            Write-Host "  ⚠️  DEADLOCK DETECTED!" -ForegroundColor Magenta
        }
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SUMMARY" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Requests: $concurrentRequests" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor $(if ($failCount -eq 0) { 'Green' } else { 'Red' })
Write-Host "Deadlocks: $deadlockCount" -ForegroundColor $(if ($deadlockCount -eq 0) { 'Green' } else { 'Red' })

$avgDuration = ($results | Measure-Object -Property Duration -Average).Average
Write-Host "Avg Duration: $([math]::Round($avgDuration, 2))ms" -ForegroundColor Gray

Write-Host "`n" -ForegroundColor White

if ($deadlockCount -eq 0) {
    Write-Host "✅ SUCCESS! No deadlocks detected!" -ForegroundColor Green
    Write-Host "The retry logic and optimized queries are working!" -ForegroundColor Green
} else {
    Write-Host "⚠️  WARNING: Deadlocks still occurring!" -ForegroundColor Red
    Write-Host "Please check application logs for more details." -ForegroundColor Yellow
}

Write-Host ""
