#!/usr/bin/env pwsh

# Postman Newman Test Runner for Cloud Run Deployment
# Usage: .\test-cloud-deployment.ps1

Write-Host "=== Testing User Management API on Cloud Run ===" -ForegroundColor Green
Write-Host "Service: https://fincore-npe-api-994490239798.europe-west2.run.app" -ForegroundColor Cyan
Write-Host ""

$collectionPath = "postman_collection.json"
$envPath = "postman_environment_cloud.json"
$resultsDir = "test-results"
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"

# Create results directory
if (-not (Test-Path $resultsDir)) {
    New-Item -ItemType Directory -Path $resultsDir | Out-Null
    Write-Host "✓ Created results directory" -ForegroundColor Green
}

# Check if newman is installed
$newmanVersion = newman --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Newman not found. Installing..." -ForegroundColor Yellow
    npm install -g newman
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Failed to install Newman" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✓ Newman version: $newmanVersion" -ForegroundColor Green
Write-Host ""

# Run collection
$outputFile = "$resultsDir\results_$timestamp.json"
$htmlFile = "$resultsDir\results_$timestamp.html"

Write-Host "Running tests..." -ForegroundColor Cyan
Write-Host "Collection: $collectionPath"
Write-Host "Environment: $envPath"
Write-Host "Output: $outputFile"
Write-Host ""

newman run $collectionPath `
    -e $envPath `
    --reporters cli,json,html `
    --reporter-json-export $outputFile `
    --reporter-html-export $htmlFile `
    --timeout 10000 `
    --timeout-request 5000

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Tests completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Results saved to:" -ForegroundColor Cyan
    Write-Host "  JSON: $outputFile"
    Write-Host "  HTML: $htmlFile"
    Write-Host ""
    Write-Host "View HTML report in browser:" -ForegroundColor Yellow
    Write-Host "  start $htmlFile"
} else {
    Write-Host ""
    Write-Host "✗ Tests failed. Check results:" -ForegroundColor Red
    Write-Host "  $outputFile"
    exit 1
}
