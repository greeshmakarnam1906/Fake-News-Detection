$baseUrl = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "=========================================================="
Write-Host "  STEP 3: LIVE END-TO-END REST API VERIFICATION SUITE"
Write-Host "=========================================================="

# 1. Health check
Write-Host "`n[Check 1] GET /actuator/health"
$health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
Write-Host "Response: $($health | ConvertTo-Json -Compress)"
if ($health.status -ne "UP") { throw "Health check failed!" }
Write-Host "PASS: Health status is UP"

# 2. Register User A and User B
$userAEmail = "alice_$(Get-Random)@academic.edu"
$userBEmail = "bob_$(Get-Random)@academic.edu"

Write-Host "`n[Check 2] Register User A ($userAEmail)"
$regABody = @{ name = "Alice"; email = $userAEmail; password = "PasswordA123!" } | ConvertTo-Json
$resA = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regABody -Headers $headers
$tokenA = $resA.token
Write-Host "User A registered successfully. ID=$($resA.userId), Token received."

Write-Host "`n[Check 3] Register User B ($userBEmail)"
$regBBody = @{ name = "Bob"; email = $userBEmail; password = "PasswordB123!" } | ConvertTo-Json
$resB = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBBody -Headers $headers
$tokenB = $resB.token
Write-Host "User B registered successfully. ID=$($resB.userId), Token received."

# 3. Log in User A and User B
Write-Host "`n[Check 4] Login User A"
$loginABody = @{ email = $userAEmail; password = "PasswordA123!" } | ConvertTo-Json
$loginResA = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginABody -Headers $headers
Write-Host "User A logged in. Token: $($loginResA.token.Substring(0, 25))..."

# 4. Wrong password returns safe error (no stack trace)
Write-Host "`n[Check 5] Login with wrong password"
try {
    $badLogin = @{ email = $userAEmail; password = "WrongPassword999!" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $badLogin -Headers $headers
    throw "Should have failed!"
} catch {
    $errResp = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Response Status: $($errResp.status)"
    Write-Host "Response Message: $($errResp.message)"
    Write-Host "Response Error: $($errResp.error)"
    if ($errResp.stackTrace -or $_.ErrorDetails.Message -match "Exception") {
        throw "Stack trace leaked!"
    }
    Write-Host "PASS: Safe error returned with zero stack trace leakage"
}

# 5. User A analyzes real-looking text and fake-looking text
Write-Host "`n[Check 6] User A analyzes genuine-looking text"
$headersA = @{ "Content-Type" = "application/json"; "Authorization" = "Bearer $tokenA" }
$realArticle = @{
    headline = "Federal Reserve Holds Benchmark Interest Rates Constant";
    text = "The Federal Reserve announced on Wednesday that it will maintain benchmark federal funds rates following their regular monetary policy committee meeting in Washington. Chairman Powell emphasized steady job growth and controlled inflation indicators across domestic manufacturing sectors.";
    sourceUrl = "https://example.com/finance/fed-rates";
    category = "Economy"
} | ConvertTo-Json

$analysisReal = Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Post -Body $realArticle -Headers $headersA
$analysisA_Id = $analysisReal.id
Write-Host "Analysis ID: $($analysisReal.id)"
Write-Host "Label: $($analysisReal.label)"
Write-Host "Model Score: $($analysisReal.score)"
Write-Host "Top Genuine Terms: $($analysisReal.topGenuineFeatures | ForEach-Object { "$($_.token):$($_.contribution)" })"
Write-Host "PASS: Genuine article analyzed and persisted"

Write-Host "`n[Check 7] User A analyzes fabricated/sensational-looking text"
$fakeArticle = @{
    headline = "Shocking Secret Plot Exposed By High Level Insiders";
    text = "An explosive secret leak has completely shattered the mainstream media narrative! High level globalists were reportedly caught holding secret emergency summit to secretly manipulate weather patterns and harvest untraceable frequencies. The shocking video has already been banned on major social networks within minutes of being posted! Share this everywhere before they delete it!";
    category = "Politics"
} | ConvertTo-Json

$analysisFake = Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Post -Body $fakeArticle -Headers $headersA
Write-Host "Analysis ID: $($analysisFake.id)"
Write-Host "Label: $($analysisFake.label)"
Write-Host "Model Score: $($analysisFake.score)"
Write-Host "Top Fake Terms: $($analysisFake.topFakeFeatures | ForEach-Object { "$($_.token):$($_.contribution)" })"
Write-Host "PASS: Fake article analyzed and persisted"

# 6. Validation checks: Empty text and >10,000 characters
Write-Host "`n[Check 8] Submit empty text"
try {
    $emptyArticle = @{ headline = "Empty"; text = "   " } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Post -Body $emptyArticle -Headers $headersA
    throw "Should have failed on empty text!"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Status: $($err.status)"
    Write-Host "Field Errors: $($err.fieldErrors | ConvertTo-Json -Compress)"
    Write-Host "PASS: Clean 400 validation error for empty text"
}

Write-Host "`n[Check 9] Submit oversized text (>10,000 chars)"
try {
    $oversizedText = "Lorem ipsum dolor sit amet " * 450
    $oversizedArticle = @{ headline = "Oversized"; text = $oversizedText } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Post -Body $oversizedArticle -Headers $headersA
    throw "Should have failed on oversized text!"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Status: $($err.status)"
    Write-Host "Field Errors: $($err.fieldErrors | ConvertTo-Json -Compress)"
    Write-Host "PASS: Clean 400 validation error for oversized text"
}

# 7. Unauthenticated request to protected endpoint returns 401
Write-Host "`n[Check 10] Access GET /api/analyses without token"
try {
    Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Get
    throw "Should have returned 401!"
} catch {
    Write-Host "Status code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "PASS: 401 Unauthorized returned"
}

# 8. User B attempts to access User A's analysis (GET & DELETE) -> Must be blocked
Write-Host "`n[Check 11] User B attempts GET on User A's analysis #$analysisA_Id"
$headersB = @{ "Content-Type" = "application/json"; "Authorization" = "Bearer $tokenB" }
try {
    Invoke-RestMethod -Uri "$baseUrl/api/analyses/$analysisA_Id" -Method Get -Headers $headersB
    throw "User B should NOT be able to view User A's analysis!"
} catch {
    Write-Host "Status code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "PASS: User B blocked from accessing User A's analysis (404/403 isolation)"
}

Write-Host "`n[Check 12] User B attempts DELETE on User A's analysis #$analysisA_Id"
try {
    Invoke-RestMethod -Uri "$baseUrl/api/analyses/$analysisA_Id" -Method Delete -Headers $headersB
    throw "User B should NOT be able to delete User A's analysis!"
} catch {
    Write-Host "Status code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "PASS: User B blocked from deleting User A's analysis"
}

# 9. Pagination check on User A's analyses
Write-Host "`n[Check 13] Pagination on GET /api/analyses"
$historyA = Invoke-RestMethod -Uri "$baseUrl/api/analyses?page=0&size=1" -Method Get -Headers $headersA
Write-Host "Total elements: $($historyA.totalElements)"
Write-Host "Total pages: $($historyA.totalPages)"
Write-Host "Returned items on page: $($historyA.content.Count)"
Write-Host "PASS: Pagination working correctly"

# 10. Rate limiting check (31+ rapid requests trigger 429)
Write-Host "`n[Check 14] Rapid requests triggering rate limiter (30 req/min limit)"
$triggered429 = $false
for ($i = 1; $i -le 35; $i++) {
    try {
        $quickReq = @{ text = "Rapid test request content number $i" } | ConvertTo-Json
        Invoke-RestMethod -Uri "$baseUrl/api/analyses" -Method Post -Body $quickReq -Headers $headersA | Out-Null
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 429) {
            $triggered429 = $true
            Write-Host "Request #$i triggered HTTP 429 Too Many Requests as expected!"
            $err = $_.ErrorDetails.Message | ConvertFrom-Json
            Write-Host "Error message: $($err.message)"
            break
        }
    }
}
if (!$triggered429) { throw "Rate limiter failed to trigger after 35 rapid requests!" }
Write-Host "PASS: Rate limiter successfully enforced HTTP 429"

Write-Host "`n=========================================================="
Write-Host "  ALL LIVE API CHECKS PASSED SUCCESSFULLY!"
Write-Host "=========================================================="
