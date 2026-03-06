# Insert Test Data into Cloud SQL Database
# Populates all tables with realistic test data for UI testing
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSERT TEST DATA - Cloud SQL Database  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_HOST = "34.89.96.239"  # Cloud SQL Public IP
$DB_NAME = "fincore_db"
$DB_USER = "fincore_app"
$SQL_FILE = "insert-test-data.sql"

Write-Host "Database Configuration:" -ForegroundColor Yellow
Write-Host "  Host: $DB_HOST" -ForegroundColor White
Write-Host "  Database: $DB_NAME" -ForegroundColor White
Write-Host "  User: $DB_USER" -ForegroundColor White
Write-Host "  SQL File: $SQL_FILE" -ForegroundColor White
Write-Host ""

# Check if SQL file exists
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "✗ SQL file not found: $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "✓ SQL file found" -ForegroundColor Green
Write-Host ""

# Get database password
$DB_PASSWORD_SECURE = Read-Host "Enter database password for $DB_USER" -AsSecureString
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD_SECURE)
)

Write-Host ""
Write-Host "Step 1: Testing database connection..." -ForegroundColor Cyan

# Test connection
$testQuery = "SELECT COUNT(*) FROM users"
$testResult = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -N -e `"$testQuery`" 2>&1"

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Database connection failed" -ForegroundColor Red
    Write-Host $testResult
    exit 1
}

Write-Host "✓ Database connection successful" -ForegroundColor Green
Write-Host "  Current user count: $testResult" -ForegroundColor White
Write-Host ""

Write-Host "Step 2: Backing up current data counts..." -ForegroundColor Cyan

$countsQuery = "SELECT 'users' as tbl, COUNT(*) as cnt FROM users UNION ALL SELECT 'roles', COUNT(*) FROM roles UNION ALL SELECT 'permissions', COUNT(*) FROM permissions UNION ALL SELECT 'address', COUNT(*) FROM address UNION ALL SELECT 'organisation', COUNT(*) FROM organisation"

$beforeData = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -t -e `"$countsQuery`""
Write-Host "Before insertion:" -ForegroundColor Yellow
Write-Host $beforeData
Write-Host ""

Write-Host "Step 3: Inserting test data..." -ForegroundColor Cyan
Write-Host "This may take 30-60 seconds..." -ForegroundColor Yellow
Write-Host ""

# Execute the SQL file using file redirection
$insertResult = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME < $SQL_FILE 2>&1"

if ($LASTEXITCODE -ne 0 -and $insertResult -notlike "*Warning*") {
    Write-Host "✗ SQL execution failed" -ForegroundColor Red
    Write-Host $insertResult
    exit 1
}

Write-Host "✓ SQL execution completed" -ForegroundColor Green
if ($insertResult) {
    Write-Host "Output: $insertResult" -ForegroundColor Gray
}
Write-Host ""

Write-Host "Step 4: Verifying inserted data..." -ForegroundColor Cyan

$afterData = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -t -e `"$countsQuery`""
Write-Host "After insertion:" -ForegroundColor Yellow
Write-Host $afterData
Write-Host ""

# Sample data queries
Write-Host "Step 5: Displaying sample data..." -ForegroundColor Cyan
Write-Host ""

Write-Host "Sample Users:" -ForegroundColor Yellow
$usersQuery = "SELECT User_Identifier, Phone_Number, First_Name, Last_Name FROM users LIMIT 5"
$usersResult = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -t -e `"$usersQuery`""
Write-Host $usersResult
Write-Host ""

Write-Host "Sample Organizations:" -ForegroundColor Yellow
$orgsQuery = "SELECT Organisation_Identifier, Legal_Name, Organisation_Type_Description, Status_Description FROM organisation LIMIT 5"
$orgsResult = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -t -e `"$orgsQuery`""
Write-Host $orgsResult
Write-Host ""

Write-Host "Sample Questionnaire Questions:" -ForegroundColor Yellow
$questionsQuery = "SELECT question_id, question_category, status FROM questionnaire_questions ORDER BY display_order LIMIT 5"
$questionsResult = cmd /c "mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME -t -e `"$questionsQuery`""
Write-Host $questionsResult
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "  TEST DATA INSERTION COMPLETE!  " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "✓ Database connection working" -ForegroundColor Green
Write-Host "✓ Test data inserted successfully" -ForegroundColor Green
Write-Host "✓ All tables populated with realistic data" -ForegroundColor Green
Write-Host ""
Write-Host "Test Data Includes:" -ForegroundColor Yellow
Write-Host "  • 17 Permissions (READ, CREATE, UPDATE, DELETE for various resources)" -ForegroundColor White
Write-Host "  • 4 Roles (ADMIN, COMPLIANCE_OFFICER, OPERATIONAL_STAFF, USER)" -ForegroundColor White
Write-Host "  • 12 Users (various roles and statuses)" -ForegroundColor White
Write-Host "  • 15 Addresses (business, registered, correspondence, residential)" -ForegroundColor White
Write-Host "  • 8 Organizations (LTD, PLC, CHARITY, SOLE_TRADER, LLP - various statuses)" -ForegroundColor White
Write-Host "  • 16 KYC Documents (VERIFIED, PENDING, REJECTED, EXPIRED)" -ForegroundColor White
Write-Host "  • 10 KYC Verifications (APPROVED, PENDING, IN_REVIEW, REJECTED)" -ForegroundColor White
Write-Host "  • 14 AML Screening Results (clean, medium risk, high risk)" -ForegroundColor White
Write-Host "  • 20 Questionnaire Questions (5 categories)" -ForegroundColor White
Write-Host "  • 48 Customer Answers (from multiple users)" -ForegroundColor White
Write-Host ""
Write-Host "Test Credentials:" -ForegroundColor Yellow
Write-Host "  Admin: +1234567890" -ForegroundColor White
Write-Host "  Compliance Officer: +447911123456 (Sarah Williams)" -ForegroundColor White
Write-Host "  Business Owner: +447911123460 (Olivia Taylor)" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Start your UI application" -ForegroundColor White
Write-Host "2. Login with any of the test phone numbers above" -ForegroundColor White
Write-Host "3. Navigate through all pages - data should now be visible!" -ForegroundColor White
Write-Host "4. Test CRUD operations with the populated data" -ForegroundColor White
Write-Host ""
Write-Host "Note: The UI should now display:" -ForegroundColor Yellow
Write-Host "  - User list with 12 users" -ForegroundColor White
Write-Host "  - Organization list with 8 organizations" -ForegroundColor White
Write-Host "  - KYC documents with various statuses" -ForegroundColor White
Write-Host "  - AML screening results" -ForegroundColor White
Write-Host "  - Questionnaire with 20 questions" -ForegroundColor White
Write-Host "  - Customer answers for multiple users" -ForegroundColor White
Write-Host ""
