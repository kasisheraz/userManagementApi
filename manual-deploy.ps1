# Manual Deployment Script for Cloud Run
# Use this when GitHub Actions is not working

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "  MANUAL DEPLOYMENT TO GOOGLE CLOUD RUN" -ForegroundColor Cyan  
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "⚠️  WARNING: This will deploy directly from your local machine" -ForegroundColor Yellow
Write-Host "   Make sure you have committed all changes first!`n" -ForegroundColor Yellow

# Check current commit
$currentCommit = (git rev-parse --short HEAD)
Write-Host "Current Commit: $currentCommit" -ForegroundColor White
Write-Host "Current Branch: $(git branch --show-current)`n" -ForegroundColor White

# Show what will be deployed  
Write-Host "Recent commits:" -ForegroundColor Cyan
git log --oneline -n 5
Write-Host ""

# Confirm deployment
$response = Read-Host "Do you want to deploy commit $currentCommit to Cloud Run NPE? (yes/no)"
if ($response -ne "yes") {
    Write-Host "`nDeployment cancelled.`n" -ForegroundColor Red
    exit 0
}

Write-Host "`n[1/3] Building with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ Maven build failed! Fix compilation errors first.`n" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Maven build successful`n" -ForegroundColor Green

Write-Host "[2/3] Building and pushing Docker image to Artifact Registry..." -ForegroundColor Yellow
$projectId = "project-07a61357-b791-4255-a9e"
$region = "europe-west2"
$imageName = "fincore-npe-api"
$imageTag = "europe-west2-docker.pkg.dev/$projectId/fincore-npe/$imageName:$currentCommit"

# Build Docker image
Write-Host "Building Docker image: $imageTag" -ForegroundColor Gray
docker build -t $imageTag .
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ Docker build failed!`n" -ForegroundColor Red
    exit 1
}

# Push to Artifact Registry
Write-Host "Pushing image to Artifact Registry..." -ForegroundColor Gray
docker push $imageTag
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ Docker push failed! Check authentication.`n" -ForegroundColor Red
    Write-Host "Run: gcloud auth configure-docker europe-west2-docker.pkg.dev`n" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Image pushed successfully`n" -ForegroundColor Green

Write-Host "[3/3] Deploying to Cloud Run..." -ForegroundColor Yellow
gcloud run deploy $imageName `
    --image=$imageTag `
    --region=$region `
    --platform=managed `
    --allow-unauthenticated `
    --port=8080 `
    --memory=1Gi `
    --cpu=1 `
    --min-instances=1 `
    --max-instances=10 `
    --set-env-vars="SPRING_PROFILES_ACTIVE=npe,BUILD_NUMBER=$currentCommit" `
    --vpc-connector=npe-connector `
    --vpc-egress=all-traffic

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ Cloud Run deployment failed!`n" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Deployment successful!`n" -ForegroundColor Green
Write-Host "Waiting 30 seconds for service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Verify deployment
Write-Host "Verifying deployment..." -ForegroundColor Cyan
try {
    $info = Invoke-RestMethod -Uri "https://fincore-npe-api-994490239798.europe-west2.run.app/api/system/info"
    Write-Host "Deployed Build: $($info.build)" -ForegroundColor $(if ($info.build -eq $currentCommit) { 'Green' } else { 'Yellow' })
    Write-Host "Java Version: $($info.javaVersion)" -ForegroundColor Gray
    Write-Host "Status: $($info.status)" -ForegroundColor Gray
    Write-Host "Timestamp: $($info.timestamp)`n" -ForegroundColor Gray
    
    if ($info.build -eq $currentCommit) {
        Write-Host "🎉 SUCCESS! New version is live!`n" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Deployment may still be rolling out. Build mismatch:`n" -ForegroundColor Yellow
        Write-Host "   Expected: $currentCommit" -ForegroundColor White
        Write-Host "   Got: $($info.build)`n" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Failed to verify deployment: $_" -ForegroundColor Red
    Write-Host "Service may still be starting up. Check Cloud Run console.`n" -ForegroundColor Yellow
}

Write-Host "============================================================`n" -ForegroundColor Cyan
