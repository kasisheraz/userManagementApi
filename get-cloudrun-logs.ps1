# Get Cloud Run Logs for Failed Revision
$ErrorActionPreference = "Continue"

$REVISION = "fincore-npe-api-00076-nm4"
$SERVICE = "fincore-npe-api"
$REGION = "europe-west2"

Write-Host "Fetching logs for revision: $REVISION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get the most recent logs
Write-Host "Recent logs (last 100 entries):" -ForegroundColor Yellow
cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION`" --limit=100 --format=`"table(timestamp,severity,textPayload)`" --project=project-07a61357-b791-4255-a9e"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Checking for ERROR logs:" -ForegroundColor Yellow
cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION AND severity>=ERROR`" --limit=50 --format=`"table(timestamp,severity,textPayload)`" --project=project-07a61357-b791-4255-a9e"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Checking environment variables:" -ForegroundColor Yellow
cmd /c "gcloud run revisions describe $REVISION --region=$REGION --project=project-07a61357-b791-4255-a9e --format=`"value(spec.containers[0].env)`""
