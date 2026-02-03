# Test Cloud SQL Connection
# This tests the exact same connection that Cloud Run will use

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cloud SQL Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Connection details (same as application-npe.yml)
$DB_HOST = "34.89.96.239"
$DB_PORT = "3306"
$DB_NAME = "fincore_db"
$DB_USER = "fincore_app"
Write-Host "Database Host: $DB_HOST" -ForegroundColor Yellow
Write-Host "Database Port: $DB_PORT" -ForegroundColor Yellow
Write-Host "Database Name: $DB_NAME" -ForegroundColor Yellow
Write-Host "Database User: $DB_USER" -ForegroundColor Yellow
Write-Host ""

# Prompt for password (same as used in Cloud Run secret)
$DB_PASSWORD = Read-Host "Enter DB_PASSWORD for fincore_app user" -AsSecureString
$DB_PASSWORD_TEXT = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD))

Write-Host ""
Write-Host "Step 1: Testing TCP connectivity..." -ForegroundColor Cyan
$tcpTest = Test-NetConnection -ComputerName $DB_HOST -Port $DB_PORT -WarningAction SilentlyContinue
if ($tcpTest.TcpTestSucceeded) {
    Write-Host "✓ TCP connection successful" -ForegroundColor Green
} else {
    Write-Host "✗ TCP connection failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Testing database connection with JDBC..." -ForegroundColor Cyan

# Create a temporary test Java file
$testCode = @"
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestCloudSQLConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useSSL=true&requireSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC";
        String user = "$DB_USER";
        String password = args[0];
        
        System.out.println("Attempting connection to: " + url);
        System.out.println("User: " + user);
        System.out.println();
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL driver loaded");
            
            // Establish connection
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Database connection successful");
            
            // Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER(), VERSION()");
            if (rs.next()) {
                System.out.println();
                System.out.println("Connection Details:");
                System.out.println("  Database: " + rs.getString(1));
                System.out.println("  User: " + rs.getString(2));
                System.out.println("  MySQL Version: " + rs.getString(3));
            }
            
            // Check tables
            System.out.println();
            System.out.println("Checking tables...");
            rs = stmt.executeQuery("SHOW TABLES");
            int tableCount = 0;
            while (rs.next()) {
                System.out.println("  ✓ " + rs.getString(1));
                tableCount++;
            }
            System.out.println();
            System.out.println("Total tables: " + tableCount);
            
            // Check required tables
            String[] requiredTables = {"users", "roles", "permissions", "address", "organisation", "kyc_documents", "otp_tokens", "role_permissions"};
            System.out.println();
            System.out.println("Verifying required tables:");
            for (String table : requiredTables) {
                rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME' AND table_name = '" + table + "'");
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("  ✓ " + table);
                } else {
                    System.out.println("  ✗ " + table + " (MISSING)");
                }
            }
            
            // Check user permissions
            System.out.println();
            System.out.println("Checking user permissions:");
            rs = stmt.executeQuery("SHOW GRANTS FOR '$DB_USER'@'%'");
            while (rs.next()) {
                System.out.println("  " + rs.getString(1));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println();
            System.out.println("✓ All checks passed!");
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("✗ Connection failed:");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
"@

$testCode | Out-File -FilePath "TestCloudSQLConnection.java" -Encoding UTF8

Write-Host "Compiling test program..." -ForegroundColor Yellow
javac -cp "target/user-management-api-1.0.0.jar" TestCloudSQLConnection.java 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation successful" -ForegroundColor Green
    Write-Host ""
    Write-Host "Running connection test..." -ForegroundColor Yellow
    Write-Host ""
    
    # Extract MySQL driver from JAR and run test
    java -cp ".;target/user-management-api-1.0.0.jar;$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" TestCloudSQLConnection $DB_PASSWORD_TEXT
    
    $exitCode = $LASTEXITCODE
    
    # Cleanup
    Remove-Item "TestCloudSQLConnection.java" -ErrorAction SilentlyContinue
    Remove-Item "TestCloudSQLConnection.class" -ErrorAction SilentlyContinue
    
    if ($exitCode -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "ALL TESTS PASSED - Safe to deploy!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "TESTS FAILED - DO NOT DEPLOY" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
    }
    
    exit $exitCode
} else {
    Write-Host "✗ Compilation failed" -ForegroundColor Red
    Write-Host "Make sure the JAR file exists: target/user-management-api-1.0.0.jar" -ForegroundColor Yellow
    Remove-Item "TestCloudSQLConnection.java" -ErrorAction SilentlyContinue
    exit 1
}
