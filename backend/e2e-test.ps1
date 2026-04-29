$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"

Write-Host "=== E2E: Full Interview Flow ==="

Write-Host "`n--- 1. Start Interview ---"
$body = '{"coverLetterText":"I am a 3-year backend developer. Spring Boot and Kotlin are my main stack. Built payment systems in MSA architecture. Designed cache strategy with PostgreSQL and Redis. Introduced message queue for high traffic handling.", "followUpEnabled":true}'
try {
    $resp = Invoke-WebRequest -Uri "$baseUrl/api/interview/start" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -TimeoutSec 90
    Write-Host "Status: $($resp.StatusCode)"
    $data = $resp.Content | ConvertFrom-Json
    Write-Host "SessionId: $($data.sessionId)"
    Write-Host "JobField: $($data.jobField)"
    Write-Host "Questions: $($data.questions.Count)"
    $sid = $data.sessionId
    $q1 = $data.questions[0].questionId
    Write-Host "Q1: $($data.questions[0].content)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    exit 1
}

Write-Host "`n--- 2. Short Answer -> Follow-up ---"
$ab = "{`"questionId`":`"$q1`", `"answer`":`"Yes.`"}"
try {
    $ar = Invoke-WebRequest -Uri "$baseUrl/api/interview/$sid/answer" -Method POST -ContentType "application/json" -Body $ab -UseBasicParsing -TimeoutSec 90
    $ad = $ar.Content | ConvertFrom-Json
    Write-Host "needsFollowUp: $($ad.needsFollowUp)"
    if ($ad.followUpQuestion) {
        Write-Host "FollowUp: $($ad.followUpQuestion.content)"
        $fqId = $ad.followUpQuestion.questionId
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
}

if ($fqId) {
    Write-Host "`n--- 3. Follow-up Answer (depth=1) ---"
    $fb = "{`"questionId`":`"$fqId`", `"answer`":`"Short.`"}"
    try {
        $fr = Invoke-WebRequest -Uri "$baseUrl/api/interview/$sid/answer" -Method POST -ContentType "application/json" -Body $fb -UseBasicParsing -TimeoutSec 30
        $fd = $fr.Content | ConvertFrom-Json
        Write-Host "needsFollowUp (expect false): $($fd.needsFollowUp)"
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)"
    }
}

Write-Host "`n--- 4. Complete ---"
try {
    $cr = Invoke-WebRequest -Uri "$baseUrl/api/interview/$sid/complete" -Method POST -ContentType "application/json" -UseBasicParsing -TimeoutSec 10
    $cd = $cr.Content | ConvertFrom-Json
    Write-Host "completedAt: $($cd.completedAt)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
}

Write-Host "`n--- 5. Error Cases ---"
try { Invoke-WebRequest -Uri "$baseUrl/api/interview/start" -Method POST -ContentType "application/json" -Body '{"coverLetterText":"short","followUpEnabled":true}' -UseBasicParsing -TimeoutSec 10; Write-Host "Short: UNEXPECTED OK" } catch { Write-Host "Short text: 400 OK" }
try { Invoke-WebRequest -Uri "$baseUrl/api/interview/fake-id/complete" -Method POST -ContentType "application/json" -UseBasicParsing -TimeoutSec 10; Write-Host "NotFound: UNEXPECTED OK" } catch { Write-Host "Not found: 404 OK" }
try { Invoke-WebRequest -Uri "$baseUrl/api/interview/$sid/complete" -Method POST -ContentType "application/json" -UseBasicParsing -TimeoutSec 10; Write-Host "Already: UNEXPECTED OK" } catch { Write-Host "Already completed: 400 OK" }

Write-Host "`n=== E2E COMPLETE ==="
