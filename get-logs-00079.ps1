# Get Logs for Revision 00079-knm
$ErrorActionPreference = "Continue"

$REVISION = "fincore-npe-api-00079-knm"
$SERVICE = "fincore-npe-api"
$PROJECT = "project-07a61357-b791-4255-a9e"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fetching Logs for: $REVISION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Getting ERROR and WARNING logs..." -ForegroundColor Yellow
Write-Host ""

cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION AND (severity>=ERROR OR severity=WARNING)`" --limit=50 --format=`"table(timestamp,severity,textPayload)`" --project=$PROJECT 2>&1"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Key application startup logs..." -ForegroundColor Yellow
Write-Host ""

cmd /c "gcloud logging read `"resource.type=cloud_run_revision AND resource.labels.service_name=$SERVICE AND resource.labels.revision_name=$REVISION AND textPayload=~'(Starting|Schema-validation|missing|HikariPool|Application run)' `" --limit=30 --format=`"table(timestamp,textPayload)`" --project=$PROJECT 2>&1"
