# Get Logs for Latest Failed Revision
$ErrorActionPreference = "Continue"

$REVISION = "fincore-npe-api-00078-hc9"
$SERVICE = "fincore-npe-api"
$PROJECT = "project-07a61357-b791-4255-a9e"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fetching Logs for Revision: $REVISION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Getting ERROR logs..." -ForegroundColor Yellow
Write-Host ""

cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION AND severity>=ERROR`" --limit=100 --format=`"table(timestamp,severity,textPayload)`" --project=$PROJECT 2>&1"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Getting ALL logs (last 50 entries)..." -ForegroundColor Yellow
Write-Host ""

cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION`" --limit=50 --format=`"table(timestamp,severity,textPayload)`" --project=$PROJECT 2>&1"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Environment variables for this revision:" -ForegroundColor Yellow
Write-Host ""

cmd /c "gcloud run revisions describe $REVISION --region=europe-west2 --project=$PROJECT --format=`"value(spec.containers[0].env)`" 2>&1"
