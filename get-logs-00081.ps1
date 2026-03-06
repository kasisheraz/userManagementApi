# Fetch logs for revision 00081-5gv
Write-Host "Fetching logs for revision fincore-npe-api-00081-5gv..." -ForegroundColor Cyan
Write-Host ""

gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api AND resource.labels.revision_name=fincore-npe-api-00081-5gv" --limit 100 --format json --project project-07a61357-b791-4255-a9e | ConvertFrom-Json | ForEach-Object {
    $timestamp = $_.timestamp
    $severity = $_.severity
    $message = $_.textPayload
    if (-not $message) {
        $message = $_.jsonPayload | ConvertTo-Json -Compress
    }
    
    $color = switch ($severity) {
        "ERROR" { "Red" }
        "WARNING" { "Yellow" }
        default { "White" }
    }
    
    Write-Host "[$timestamp] $severity" -ForegroundColor $color
    Write-Host $message
    Write-Host ""
}
