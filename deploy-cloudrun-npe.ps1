# Cloud Run Deployment Script with Proper Configuration
# This script deploys with the exact settings from GitHub Actions workflow

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cloud Run Deployment to NPE Environment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration from GitHub Actions
$PROJECT_ID = "project-07a61357-b791-4255-a9e"
$REGION = "europe-west2"
$SERVICE_NAME = "fincore-npe-api"
$IMAGE_NAME = "fincore-api"

Write-Host "Project ID: $PROJECT_ID" -ForegroundColor Yellow
Write-Host "Region: $REGION" -ForegroundColor Yellow
Write-Host "Service: $SERVICE_NAME" -ForegroundColor Yellow
Write-Host ""

# Get secrets (you'll need to provide these)
Write-Host "Please provide the following configuration:" -ForegroundColor Cyan
$DB_USER = Read-Host "Database User [fincore_app]"
if ([string]::IsNullOrWhiteSpace($DB_USER)) { $DB_USER = "fincore_app" }

$DB_PASSWORD_SECURE = Read-Host "Database Password" -AsSecureString
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD_SECURE)
)

$SERVICE_ACCOUNT = Read-Host "Service Account Email [fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com]"
if ([string]::IsNullOrWhiteSpace($SERVICE_ACCOUNT)) { 
    $SERVICE_ACCOUNT = "fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com" 
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploying to Cloud Run..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Deploy command matching GitHub Actions exactly
$deployCmd = @"
gcloud run deploy $SERVICE_NAME ``
  --image=gcr.io/$PROJECT_ID/$IMAGE_NAME:latest ``
  --region=$REGION ``
  --platform=managed ``
  --allow-unauthenticated ``
  --service-account=$SERVICE_ACCOUNT ``
  --memory=1Gi ``
  --cpu=1 ``
  --timeout=900 ``
  --cpu-boost ``
  --max-instances=3 ``
  --min-instances=0 ``
  --clear-vpc-connector ``
  --set-env-vars="DB_NAME=fincore_db,DB_USER=$DB_USER,DB_PASSWORD=$DB_PASSWORD,SPRING_PROFILES_ACTIVE=npe,JAVA_TOOL_OPTIONS=-Dcom.google.cloud.sql.core.CoreSocketFactory.enableIamAuth=false" ``
  --port=8080 ``
  --no-cpu-throttling ``
  --project=$PROJECT_ID
"@

Write-Host "Deployment Command:" -ForegroundColor Yellow
Write-Host $deployCmd -ForegroundColor Gray
Write-Host ""

# Execute deployment
try {
    Invoke-Expression "cmd /c `"$deployCmd`""
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✅ Deployment Successful!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        
        # Get service URL
        $SERVICE_URL = cmd /c "gcloud run services describe $SERVICE_NAME --region=$REGION --project=$PROJECT_ID --format=`"value(status.url)`""
        Write-Host "Service URL: $SERVICE_URL" -ForegroundColor Cyan
        Write-Host "Health Check: $SERVICE_URL/actuator/health" -ForegroundColor Cyan
        
        # Wait and test health
        Write-Host ""
        Write-Host "Testing health endpoint in 10 seconds..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        
        try {
            $healthResponse = Invoke-RestMethod -Uri "$SERVICE_URL/actuator/health" -Method Get
            Write-Host "✅ Health Status: $($healthResponse.status)" -ForegroundColor Green
        }
        catch {
            Write-Host "⚠️ Health check failed. Service may still be starting..." -ForegroundColor Yellow
            Write-Host "Check manually: $SERVICE_URL/actuator/health" -ForegroundColor Cyan
        }
    }
    else {
        throw "Deployment command failed with exit code: $LASTEXITCODE"
    }
}
catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ Deployment Failed!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check logs at:" -ForegroundColor Yellow
    Write-Host "https://console.cloud.google.com/run/detail/$REGION/$SERVICE_NAME/logs" -ForegroundColor Cyan
    exit 1
}
