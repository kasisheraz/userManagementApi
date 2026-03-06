@echo off
REM Deploy Phase 2 Schema to Cloud SQL NPE Database
REM Run this from the project root directory

echo ========================================
echo Deploy Phase 2 Schema to Cloud SQL
echo ========================================
echo.

set DB_HOST=34.89.96.239
set DB_NAME=fincore_db
set DB_USER=fincore_app

echo Host: %DB_HOST%
echo Database: %DB_NAME%
echo User: %DB_USER%
echo.

set /p DB_PASSWORD="Enter database password: "

echo.
echo Deploying schema...
echo.

mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < complete-entity-schema.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Schema deployed to Cloud SQL
    echo ========================================
    echo.
    echo Next step: Redeploy the application
    echo   GitHub Actions will automatically redeploy
    echo   or run: .\deploy-cloudrun-npe.ps1
) else (
    echo.
    echo ========================================
    echo ERROR! Schema deployment failed
    echo ========================================
    echo.
    echo Check:
    echo   1. MySQL client is installed
    echo   2. Database password is correct
    echo   3. Cloud SQL allows connections from your IP
)

pause
