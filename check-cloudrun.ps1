# Check Cloud Run Service Details
Write-Host "Getting Cloud Run service details..." -ForegroundColor Cyan

# Get service description
Write-Host "`n=== Service Configuration ===" -ForegroundColor Yellow
gcloud run services describe fincore-npe-api --region=europe-west2

# Get latest revision
Write-Host "`n=== Latest Revision ===" -ForegroundColor Yellow
$latestRevision = gcloud run revisions list --service=fincore-npe-api --region=europe-west2 --limit=1 --format="value(metadata.name)"
Write-Host "Latest Revision: $latestRevision" -ForegroundColor Green

# Get logs from latest revision
Write-Host "`n=== Recent Logs (Last 50 lines) ===" -ForegroundColor Yellow
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" --limit=50 --format=json | ConvertFrom-Json | ForEach-Object {
    Write-Host "[$($_.timestamp)] $($_.textPayload)" -ForegroundColor Cyan
}

# Get environment variables
Write-Host "`n=== Environment Variables ===" -ForegroundColor Yellow
gcloud run services describe fincore-npe-api --region=europe-west2 --format="value(spec.template.spec.containers[0].env)"

Write-Host "`nDone!" -ForegroundColor Green
