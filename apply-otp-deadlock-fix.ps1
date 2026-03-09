# Apply OTP Deadlock Fixes to Production Database
# This script adds optimized indexes to prevent deadlock issues

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  OTP DEADLOCK FIX - DATABASE UPDATE" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "This script will:" -ForegroundColor White
Write-Host "  1. Add optimized indexes to otp_tokens table" -ForegroundColor Gray
Write-Host "  2. Analyze table for better performance" -ForegroundColor Gray
Write-Host "  3. Show current table status`n" -ForegroundColor Gray

$proceed = Read-Host "Continue? (Y/N)"
if ($proceed -ne 'Y' -and $proceed -ne 'y') {
    Write-Host "Aborted." -ForegroundColor Red
    exit 1
}

Write-Host "`nConnecting to Cloud SQL (fincore-npe-db)..." -ForegroundColor Cyan
Write-Host "You may be prompted for the database password.`n" -ForegroundColor Yellow

# Execute the SQL script
$sqlCommands = @"
-- Add indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_otp_phone_verified ON otp_tokens(Phone_Number, Verified);
CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp_tokens(Expires_At);
CREATE INDEX IF NOT EXISTS idx_otp_lookup ON otp_tokens(Phone_Number, Otp_Code, Verified);

-- Analyze table
ANALYZE TABLE otp_tokens;

-- Show indexes
SHOW INDEX FROM otp_tokens;

-- Show statistics
SELECT 
    'Deadlock prevention indexes applied' AS Status,
    COUNT(*) AS Total_Records,
    SUM(CASE WHEN Verified = 1 THEN 1 ELSE 0 END) AS Verified_Count,
    SUM(CASE WHEN Verified = 0 THEN 1 ELSE 0 END) AS Unverified_Count,
    SUM(CASE WHEN Expires_At < NOW() THEN 1 ELSE 0 END) AS Expired_Count
FROM otp_tokens;
"@

# Write SQL to temp file
$tempSqlFile = [System.IO.Path]::GetTempFileName() + ".sql"
$sqlCommands | Out-File -FilePath $tempSqlFile -Encoding UTF8

Write-Host "Executing SQL commands..." -ForegroundColor Cyan

try {
    # Use gcloud sql connect with input redirection
    Get-Content $tempSqlFile | gcloud sql connect fincore-npe-db --user=fincore_app --database=fincore_db --quiet
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  SUCCESS!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "`nIndexes have been added to prevent OTP deadlocks." -ForegroundColor Green
    Write-Host "The application code has also been updated with:" -ForegroundColor White
    Write-Host "  - Automatic retry logic (3 attempts)" -ForegroundColor Gray
    Write-Host "  - Exponential backoff" -ForegroundColor Gray
    Write-Host "  - Optimized transaction isolation" -ForegroundColor Gray
    Write-Host "  - Native SQL with LIMIT clauses`n" -ForegroundColor Gray
    
} catch {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  ERROR" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Failed to apply indexes: $_" -ForegroundColor Red
    Write-Host "`nYou can manually apply the SQL from:" -ForegroundColor Yellow
    Write-Host "  fix-otp-deadlock-indexes.sql`n" -ForegroundColor White
} finally {
    # Clean up temp file
    if (Test-Path $tempSqlFile) {
        Remove-Item $tempSqlFile -Force
    }
}
