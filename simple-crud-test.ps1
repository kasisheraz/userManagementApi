$url = "https://fincore-npe-api-994490239798.europe-west2.run.app"
Write-Host "Auth..." -ForegroundColor Yellow
$otp = (Invoke-RestMethod -Uri "$url/api/auth/request-otp" -Method POST -Body '{"phoneNumber":"+1234567890"}' -ContentType "application/json").devOtp
$auth = Invoke-RestMethod -Uri "$url/api/auth/verify-otp" -Method POST -Body "{`"phoneNumber`":`"+1234567890`",`"otp`":`"$otp`"}" -ContentType "application/json"
$token = if($auth.token){$auth.token}else{$auth.accessToken}
$h = @{Authorization="Bearer $token"}
Write-Host "Token OK`n"

Write-Host "TEST 1: Create Organization" -ForegroundColor Cyan
$orgBody = '{"ownerId":1,"legalName":"Test Org","organisationType":"LTD"}'
try {
    $r = Invoke-RestMethod -Uri "$url/api/organisations" -Method POST -Body $orgBody -Headers $h -ContentType "application/json"
    Write-Host "SUCCESS" -ForegroundColor Green
    $r | ConvertTo-Json
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message
}

Write-Host "`nTEST 2: Create User" -ForegroundColor Cyan
$ts = Get-Date -Format 'HHmmss'
$userBody = "{`"phoneNumber`":`"+44$ts`",`"firstName`":`"John`",`"lastName`":`"Doe`",`"email`":`"j$ts@t.com`"}"
try {
    $r = Invoke-RestMethod -Uri "$url/api/users" -Method POST -Body $userBody -Headers $h -ContentType "application/json"
    Write-Host "SUCCESS" -ForegroundColor Green
    $r | ConvertTo-Json
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message
}
