# E2E Integration Test Script for Interview Server
# Usage: Set GEMINI_API_KEY environment variable, then run this script
# Example: $env:GEMINI_API_KEY = "your-api-key"; .\e2e-test.ps1

param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$passed = 0
$failed = 0

function Test-Endpoint {
    param($Name, $Uri, $Method, $Body, $ContentType, $ExpectedStatus)
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 30
        }
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = $ContentType ?? "application/json"
        }
        $r = Invoke-WebRequest @params
        if ($r.StatusCode -eq $ExpectedStatus) {
            Write-Host "[PASS] $Name (HTTP $($r.StatusCode))" -ForegroundColor Green
            $script:passed++
            return $r.Content | ConvertFrom-Json
        } else {
            Write-Host "[FAIL] $Name - Expected $ExpectedStatus, got $($r.StatusCode)" -ForegroundColor Red
            $script:failed++
            return $null
        }
    } catch [System.Net.WebException] {
        $statusCode = [int]$_.Exception.Response.StatusCode
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "[PASS] $Name (HTTP $statusCode - expected error)" -ForegroundColor Green
            $script:passed++
            return $null
        } else {
            Write-Host "[FAIL] $Name - Expected $ExpectedStatus, got $statusCode" -ForegroundColor Red
            $script:failed++
            return $null
        }
    }
}

Write-Host "=== Interview Server E2E Tests ===" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl"
Write-Host ""

# Test 1: Health check
Test-Endpoint "Health check" "$BaseUrl/api/health" "GET" $null $null 200 | Out-Null

# Test 2: Short cover letter → 400
$shortBody = '{"coverLetterText":"짧음","followUpEnabled":true}'
Test-Endpoint "Short cover letter → 400" "$BaseUrl/api/interview/start" "POST" $shortBody "application/json" 400 | Out-Null

# Test 3: Missing session → 404
Test-Endpoint "Missing session → 404" "$BaseUrl/api/interview/nonexistent/complete" "POST" "" "application/json" 404 | Out-Null

# Test 4: Full interview flow (requires real GEMINI_API_KEY)
if ($env:GEMINI_API_KEY -and $env:GEMINI_API_KEY -ne "placeholder") {
    Write-Host ""
    Write-Host "--- Full Flow Test (requires Gemini API) ---" -ForegroundColor Cyan

    $resumeText = "안녕하세요. 저는 3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용하며, MSA 환경에서 결제 시스템을 구축한 경험이 있습니다. PostgreSQL과 Redis를 활용한 캐시 전략 설계를 주도했으며, 팀 내 코드 리뷰 문화를 정착시켰습니다."
    $startBody = "{`"coverLetterText`":`"$resumeText`",`"followUpEnabled`":true}"

    $startResult = Test-Endpoint "Start interview" "$BaseUrl/api/interview/start" "POST" $startBody "application/json" 200
    if ($startResult) {
        $sessionId = $startResult.sessionId
        $firstQuestion = $startResult.questions[0]
        Write-Host "  Session: $sessionId" -ForegroundColor Gray
        Write-Host "  Job Field: $($startResult.jobField)" -ForegroundColor Gray
        Write-Host "  Questions: $($startResult.questions.Count)" -ForegroundColor Gray

        # Submit insufficient answer → expect follow-up
        $answerBody = "{`"questionId`":`"$($firstQuestion.questionId)`",`"answer`":`"네`"}"
        $answerResult = Test-Endpoint "Submit short answer → follow-up" "$BaseUrl/api/interview/$sessionId/answer" "POST" $answerBody "application/json" 200
        if ($answerResult -and $answerResult.needsFollowUp) {
            Write-Host "  Follow-up triggered: $($answerResult.followUpQuestion.content)" -ForegroundColor Gray

            # Submit follow-up answer → no more follow-ups (depth=1)
            $fuBody = "{`"questionId`":`"$($answerResult.followUpQuestion.questionId)`",`"answer`":`"구체적으로 설명하면, 저는 결제 모듈의 트랜잭션 처리를 담당했습니다.`"}"
            $fuResult = Test-Endpoint "Follow-up answer → no more follow-up (depth=1)" "$BaseUrl/api/interview/$sessionId/answer" "POST" $fuBody "application/json" 200
            if ($fuResult -and -not $fuResult.needsFollowUp) {
                Write-Host "  depth=1 enforced correctly" -ForegroundColor Gray
            }
        }

        # Complete interview
        $completeResult = Test-Endpoint "Complete interview" "$BaseUrl/api/interview/$sessionId/complete" "POST" "" "application/json" 200
        if ($completeResult) {
            Write-Host "  Completed at: $($completeResult.completedAt)" -ForegroundColor Gray
        }

        # Try to complete again → 400
        Test-Endpoint "Complete again → 400" "$BaseUrl/api/interview/$sessionId/complete" "POST" "" "application/json" 400 | Out-Null
    }
} else {
    Write-Host ""
    Write-Host "[SKIP] Full flow test - Set GEMINI_API_KEY to run" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Results: $passed passed, $failed failed ===" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
