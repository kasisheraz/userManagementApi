# GCP Cloud Run Deployment Script
# Cost-Optimized NPE Deployment (No VPC Connector)

$ErrorActionPreference = "Stop"

# ============================================================================
# CONFIGURATION
# ============================================================================

$PROJECT_ID = "fincore-npe-project"
$REGION = "europe-west2"
$SERVICE_NAME = "fincore-npe-api"
$IMAGE_NAME = "fincore-api"
$IMAGE_TAG = "latest"
$IMAGE_REGISTRY = "gcr.io/$PROJECT_ID/$IMAGE_NAME"
$SERVICE_ACCOUNT = "fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"
$DB_INSTANCE = "fincore-npe-db"
$DB_NAME = "my_auth_db"

# ============================================================================
# FUNCTION: Check Prerequisites
# ============================================================================

function Test-Prerequisites {
    Write-Host "`n=== Checking Prerequisites ===" -ForegroundColor Cyan
    
    $tools = @("gcloud", "docker")
    $missing = @()
    
    foreach ($tool in $tools) {
        if (Get-Command $tool -ErrorAction SilentlyContinue) {
            Write-Host "✓ $tool found" -ForegroundColor Green
        } else {
            Write-Host "✗ $tool not found" -ForegroundColor Red
            $missing += $tool
        }
    }
    
    if ($missing.Count -gt 0) {
        Write-Host "`nMissing tools: $($missing -join ', ')" -ForegroundColor Red
        Write-Host "`nPlease install:
- gcloud: https://cloud.google.com/sdk/docs/install-windows
- docker: https://docs.docker.com/desktop/install/windows-install/`n" -ForegroundColor Yellow
        exit 1
    }
}

# ============================================================================
# FUNCTION: Build Application
# ============================================================================

function Build-Application {
    Write-Host "`n=== Building Application ===" -ForegroundColor Cyan
    
    Set-Location "c:\Development\git\userManagementApi"
    
    Write-Host "Running: mvn clean package -DskipTests -q"
    mvn clean package -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Build successful" -ForegroundColor Green
        $jar = Get-Item "target\user-management-api-*.jar" | Select-Object -First 1
        Write-Host "  JAR: $($jar.Name) ($($jar.Length / 1MB | Round 2)MB)"
    } else {
        Write-Host "✗ Build failed" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# FUNCTION: Build Docker Image
# ============================================================================

function Build-DockerImage {
    Write-Host "`n=== Building Docker Image ===" -ForegroundColor Cyan
    
    $image = "$IMAGE_REGISTRY`:$IMAGE_TAG"
    Write-Host "Building image: $image"
    
    docker build -t "$image" -t "$IMAGE_REGISTRY`:v1.0.0" .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker build successful" -ForegroundColor Green
        docker images | grep fincore-api
    } else {
        Write-Host "✗ Docker build failed" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# FUNCTION: Setup GCP Authentication
# ============================================================================

function Setup-GCPAuth {
    Write-Host "`n=== Setting Up GCP Authentication ===" -ForegroundColor Cyan
    
    # Set project
    Write-Host "Setting GCP project to: $PROJECT_ID"
    gcloud config set project $PROJECT_ID
    
    # Verify project
    $currentProject = gcloud config get-value project
    Write-Host "✓ Current project: $currentProject" -ForegroundColor Green
    
    # Configure Docker authentication
    Write-Host "`nConfiguring Docker authentication for GCR..."
    gcloud auth configure-docker gcr.io --quiet
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker authentication configured" -ForegroundColor Green
    } else {
        Write-Host "✗ Docker authentication failed" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# FUNCTION: Push Docker Image to GCR
# ============================================================================

function Push-DockerImage {
    Write-Host "`n=== Pushing Docker Image to GCR ===" -ForegroundColor Cyan
    
    $image = "$IMAGE_REGISTRY`:$IMAGE_TAG"
    Write-Host "Pushing: $image"
    
    docker push "$image"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Image pushed successfully" -ForegroundColor Green
        
        # Verify image in registry
        Write-Host "`nVerifying image in registry..."
        gcloud container images describe "$image" --show-package-vulnerability
        Write-Host "✓ Image verified" -ForegroundColor Green
    } else {
        Write-Host "✗ Image push failed" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# FUNCTION: Verify Infrastructure
# ============================================================================

function Verify-Infrastructure {
    Write-Host "`n=== Verifying GCP Infrastructure ===" -ForegroundColor Cyan
    
    # Check Cloud SQL
    Write-Host "`nChecking Cloud SQL instance: $DB_INSTANCE"
    $sqlStatus = gcloud sql instances describe $DB_INSTANCE --region=$REGION --format="value(state)" 2>$null
    
    if ($sqlStatus -eq "RUNNABLE") {
        Write-Host "✓ Cloud SQL instance is RUNNABLE" -ForegroundColor Green
    } else {
        Write-Host "⚠ Cloud SQL status: $sqlStatus" -ForegroundColor Yellow
    }
    
    # Check Service Account
    Write-Host "`nChecking Service Account: $SERVICE_ACCOUNT"
    gcloud iam service-accounts describe $SERVICE_ACCOUNT 2>$null | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Service Account exists" -ForegroundColor Green
    } else {
        Write-Host "✗ Service Account not found" -ForegroundColor Red
        exit 1
    }
    
    # Check Cloud SQL secret
    Write-Host "`nChecking Secrets:"
    $secrets = gcloud secrets list --format="value(name)" 2>$null
    
    foreach ($secret in @("db-password", "jwt-secret")) {
        if ($secrets -contains $secret) {
            Write-Host "✓ Secret '$secret' exists" -ForegroundColor Green
        } else {
            Write-Host "⚠ Secret '$secret' not found" -ForegroundColor Yellow
        }
    }
}

# ============================================================================
# FUNCTION: Deploy to Cloud Run
# ============================================================================

function Deploy-CloudRun {
    Write-Host "`n=== Deploying to Cloud Run ===" -ForegroundColor Cyan
    
    $image = "$IMAGE_REGISTRY`:$IMAGE_TAG"
    
    Write-Host "`nDeployment Configuration:"
    Write-Host "  Service Name: $SERVICE_NAME"
    Write-Host "  Region: $REGION"
    Write-Host "  Image: $image"
    Write-Host "  Service Account: $SERVICE_ACCOUNT"
    Write-Host "  Memory: 256Mi"
    Write-Host "  CPU: 0.5"
    Write-Host "  Max Instances: 2"
    Write-Host "  Min Instances: 0"
    Write-Host "  Cloud SQL: $PROJECT_ID`:$REGION`:$DB_INSTANCE"
    Write-Host ""
    
    Write-Host "Deploying to Cloud Run (this may take 2-3 minutes)..."
    
    gcloud run deploy $SERVICE_NAME `
        --image=$image `
        --region=$REGION `
        --platform=managed `
        --allow-unauthenticated `
        --service-account=$SERVICE_ACCOUNT `
        --memory=256Mi `
        --cpu=0.5 `
        --max-instances=2 `
        --min-instances=0 `
        --timeout=540 `
        --set-env-vars="SPRING_PROFILES_ACTIVE=mysql,PORT=8080,DB_NAME=$DB_NAME,DB_USER=fincore_app" `
        --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest" `
        --set-cloudsql-instances="$PROJECT_ID`:$REGION`:$DB_INSTANCE" `
        --project=$PROJECT_ID
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Deployment successful!" -ForegroundColor Green
    } else {
        Write-Host "`n✗ Deployment failed" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# FUNCTION: Verify Deployment
# ============================================================================

function Verify-Deployment {
    Write-Host "`n=== Verifying Cloud Run Deployment ===" -ForegroundColor Cyan
    
    # Get service URL
    Write-Host "`nGetting service URL..."
    $serviceUrl = gcloud run services describe $SERVICE_NAME `
        --region=$REGION `
        --format='value(status.url)' `
        --project=$PROJECT_ID
    
    Write-Host "✓ Service URL: $serviceUrl" -ForegroundColor Green
    
    # Get service details
    Write-Host "`nService Details:"
    gcloud run services describe $SERVICE_NAME `
        --region=$REGION `
        --format="table(metadata.name, status.conditions[0].status, status.latestReadyRevisionName)" `
        --project=$PROJECT_ID
    
    # Test health endpoint
    Write-Host "`nTesting health endpoint..."
    $maxRetries = 10
    $retryCount = 0
    
    while ($retryCount -lt $maxRetries) {
        try {
            $response = Invoke-WebRequest -Uri "$serviceUrl/actuator/health" -UseBasicParsing -TimeoutSec 10
            
            if ($response.StatusCode -eq 200) {
                Write-Host "✓ Health check passed" -ForegroundColor Green
                Write-Host "  Response: $($response.Content | ConvertFrom-Json | ConvertTo-Json)"
                return
            }
        } catch {
            $retryCount++
            if ($retryCount -lt $maxRetries) {
                Write-Host "⏳ Waiting for service to be ready (attempt $retryCount/$maxRetries)..."
                Start-Sleep -Seconds 5
            }
        }
    }
    
    Write-Host "⚠ Health check failed after $maxRetries attempts" -ForegroundColor Yellow
    Write-Host "  Check logs with: gcloud logging read 'resource.type=cloud_run_revision' --limit=50" -ForegroundColor Yellow
}

# ============================================================================
# FUNCTION: View Logs
# ============================================================================

function View-Logs {
    Write-Host "`n=== Cloud Run Logs ===" -ForegroundColor Cyan
    Write-Host "`nLatest 20 log entries:"
    
    gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE_NAME" `
        --limit=20 `
        --format="table(timestamp, severity, jsonPayload.message)" `
        --project=$PROJECT_ID
    
    Write-Host "`nTo view more logs, run:"
    Write-Host "  gcloud logging read 'resource.type=cloud_run_revision' --follow --limit=50" -ForegroundColor Cyan
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

try {
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║   GCP Cloud Run Deployment - Cost-Optimized NPE (No VPC)       ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    
    # Execute deployment steps
    Test-Prerequisites
    Build-Application
    Build-DockerImage
    Setup-GCPAuth
    Push-DockerImage
    Verify-Infrastructure
    Deploy-CloudRun
    Verify-Deployment
    View-Logs
    
    Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║   ✓ DEPLOYMENT COMPLETE                                        ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    
    Write-Host "`nNext Steps:
1. Monitor logs: gcloud logging read 'resource.type=cloud_run_revision' --follow
2. View metrics: gcloud monitoring time-series list --filter='metric.type:run.*'
3. Test API: curl $serviceUrl/actuator/health
4. Check database: gcloud sql connect $DB_INSTANCE --user=root
" -ForegroundColor Green
    
} catch {
    Write-Host "`n✗ Deployment failed: $_" -ForegroundColor Red
    exit 1
}
