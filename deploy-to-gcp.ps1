# GCP Deployment Script for Windows PowerShell
# Deploy FinCore User Management API to Google Cloud Run

param(
    [Parameter(Mandatory=$true)]
    [string]$ProjectId,
    
    [Parameter(Mandatory=$false)]
    [string]$Region = "europe-west2",
    
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "fincore-npe-api",
    
    [Parameter(Mandatory=$false)]
    [string]$ImageName = "fincore-api"
)

# Color output functions
function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Write-Section {
    param([string]$Title)
    Write-Host "`n========================================" -ForegroundColor Blue
    Write-Host $Title -ForegroundColor Blue
    Write-Host "========================================`n" -ForegroundColor Blue
}

# Main deployment function
function Deploy-ToCloudRun {
    Write-Section "GCP Cloud Run Deployment"
    Write-Info "Project ID: $ProjectId"
    Write-Info "Region: $Region"
    Write-Info "Service Name: $ServiceName"
    Write-Info "Database: fincore_db"
    
    # Check if gcloud is installed
    Write-Section "Checking Prerequisites"
    try {
        $gcloudVersion = gcloud version 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "gcloud CLI not found"
        }
        Write-Success "gcloud CLI is installed"
    }
    catch {
        Write-Error "gcloud CLI is not installed. Please install it from: https://cloud.google.com/sdk/docs/install"
        exit 1
    }
    
    # Check if Maven is installed
    try {
        $mvnVersion = mvn -version 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Maven not found"
        }
        Write-Success "Maven is installed"
    }
    catch {
        Write-Error "Maven is not installed. Please install it from: https://maven.apache.org/download.cgi"
        exit 1
    }
    
    # Authenticate to GCP
    Write-Section "Authenticating to GCP"
    gcloud config set project $ProjectId
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to set GCP project"
        exit 1
    }
    Write-Success "Authenticated to project $ProjectId"
    
    # Build the application
    Write-Section "Building Application"
    Write-Info "Running Maven build..."
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed"
        exit 1
    }
    Write-Success "Application built successfully"
    
    # Check if JAR file exists
    $jarFile = "target\user-management-api-1.0.0.jar"
    if (!(Test-Path $jarFile)) {
        Write-Error "JAR file not found at $jarFile"
        exit 1
    }
    Write-Success "JAR file created: $jarFile"
    
    # Configure Docker for GCR
    Write-Section "Configuring Docker"
    gcloud auth configure-docker gcr.io --quiet
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to configure Docker for GCR"
        exit 1
    }
    Write-Success "Docker configured for GCR"
    
    # Build Docker image
    Write-Section "Building Docker Image"
    $imageTag = "gcr.io/$ProjectId/$ImageName:latest"
    $imageShaTag = "gcr.io/$ProjectId/$ImageName:$(git rev-parse --short HEAD 2>$null)"
    
    Write-Info "Building image: $imageTag"
    docker build -t $imageTag -t $imageShaTag .
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker build failed"
        exit 1
    }
    Write-Success "Docker image built successfully"
    
    # Push Docker image to GCR
    Write-Section "Pushing to Google Container Registry"
    Write-Info "Pushing image: $imageTag"
    docker push $imageTag
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to push Docker image"
        exit 1
    }
    Write-Success "Docker image pushed to GCR"
    
    # Get deployment configuration
    Write-Section "Deployment Configuration"
    Write-Warning "Please provide the following information:"
    
    $dbUser = Read-Host "Database User (default: fincore_app)"
    if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "fincore_app" }
    
    $dbPassword = Read-Host "Database Password" -AsSecureString
    $dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
    )
    
    $cloudSqlInstance = Read-Host "Cloud SQL Instance (format: project:region:instance)"
    
    $serviceAccount = Read-Host "Service Account Email"
    
    # Deploy to Cloud Run
    Write-Section "Deploying to Cloud Run"
    Write-Info "Deploying service: $ServiceName"
    
    $envVars = "SPRING_PROFILES_ACTIVE=npe,DB_NAME=fincore_db,DB_USER=$dbUser,DB_PASSWORD=$dbPasswordPlain,CLOUD_SQL_INSTANCE=$cloudSqlInstance"
    
    gcloud run deploy $ServiceName `
        --image=$imageTag `
        --region=$Region `
        --platform=managed `
        --allow-unauthenticated `
        --service-account=$serviceAccount `
        --memory=1Gi `
        --cpu=1 `
        --timeout=900 `
        --max-instances=3 `
        --min-instances=0 `
        --add-cloudsql-instances=$cloudSqlInstance `
        --set-env-vars=$envVars `
        --port=8080 `
        --project=$ProjectId
        
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Cloud Run deployment failed"
        exit 1
    }
    
    Write-Success "Deployed to Cloud Run successfully!"
    
    # Get service URL
    Write-Section "Deployment Complete"
    $serviceUrl = gcloud run services describe $ServiceName `
        --region=$Region `
        --project=$ProjectId `
        --format="value(status.url)"
    
    Write-Success "Service URL: $serviceUrl"
    Write-Info "Health Check: $serviceUrl/actuator/health"
    
    # Test health endpoint
    Write-Section "Testing Deployment"
    Write-Info "Testing health endpoint..."
    Start-Sleep -Seconds 5
    
    try {
        $response = Invoke-WebRequest -Uri "$serviceUrl/actuator/health" -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Success "Health check passed!"
            Write-Host $response.Content -ForegroundColor Green
        }
    }
    catch {
        Write-Warning "Health check failed. The service may still be starting up."
        Write-Info "Please check manually: $serviceUrl/actuator/health"
    }
    
    Write-Section "Next Steps"
    Write-Info "1. Initialize database schema if this is first deployment:"
    Write-Host "   gcloud sql connect YOUR_INSTANCE_NAME --user=$dbUser --database=fincore_db" -ForegroundColor Cyan
    Write-Info "2. Test the API:"
    Write-Host "   curl -X POST $serviceUrl/api/auth/login -H 'Content-Type: application/json' -d '{`"username`":`"admin`",`"password`":`"Admin@123456`"}'" -ForegroundColor Cyan
    Write-Info "3. View logs:"
    Write-Host "   gcloud run services logs read $ServiceName --region=$Region" -ForegroundColor Cyan
}

# Run the deployment
try {
    Deploy-ToCloudRun
    Write-Host "`n" -NoNewline
    Write-Success "Deployment completed successfully!"
    exit 0
}
catch {
    Write-Host "`n" -NoNewline
    Write-Error "Deployment failed: $_"
    exit 1
}
