# Server Interview API — MVP Work Plan

## TL;DR

> **Quick Summary**: Spring Boot 3.x (Kotlin) 서버를 구축하여 이력서 기반 AI 면접 질문을 생성한다. Gemini API로 질문 생성 + 꼬리질문 평가를 처리하고, 데이터는 인메모리에만 저장한다 (DB 없음).
>
> **Deliverables**:
> - Spring Boot REST API 서버 (3개 endpoint)
> - AI 면접관 프롬프트 (질문 생성 + 꼬리질문 평가 + 직무 감지)
> - PDF/DOCX 텍스트 추출 서비스
> - 인메모리 세션 관리
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES — 4 waves
> **Critical Path**: T1 → T2/T3 → T4/T5 → T6 → T7/T8 → F1-F4

---

## Context

### Original Request
requirements_en.md 기반 면접 연습 앱의 서버 API를 Spring Boot(Kotlin)으로 구축. Android 앱(별도 플랜)이 호출하는 REST API 제공.

### Interview Summary
**Key Discussions**:
- **범위**: 서버 API만 (Android, iOS, Discord Bot 제외)
- **DB**: 없음 — 인메모리 저장 (ConcurrentHashMap). Docker 불필요
- **배포**: 로컬 머신 (`localhost:8080`)
- **AI**: Gemini API (API 키 보유 확인)
- **직무 감지**: AI 자유 감지 (정해진 목록 아님)
- **인증**: 없음
- **테스트**: 구현 후 테스트

### Metis Review
**Identified Gaps** (addressed):
- **직무 감지 형식**: AI 자유 감지로 결정 (predefined list 아님)
- **인메모리 세션 만료**: 서버 재시작 시 소실 허용 (MVP)
- **PDFBox 한국어 버그**: PDFBOX-5350 대응 설정 필수
- **파일 크기 제한**: 10MB (application.yml에서 설정)
- **Spring AI BOM 버전**: 안정 버전 사용 확인 필요

---

## Work Objectives

### Core Objective
Android 앱이 호출하는 3개 REST API를 Spring Boot(Kotlin)으로 구현한다. Gemini API로 면접 질문을 생성하고, 답변을 평가하여 꼬리질문을 생성한다.

### Concrete Deliverables
- Spring Boot 서버 (`./gradlew bootRun`으로 실행)
- 3개 REST endpoint (start, answer, complete)
- AI 프롬프트 3종 (질문 생성, 꼬리질문 평가, 직무 감지)
- PDF/DOCX 텍스트 추출
- 인메모리 세션 저장소

### Definition of Done
- [ ] `./gradlew bootRun`으로 서버 정상 기동 (8080 포트)
- [ ] curl로 전체 면접 플로우 동작: 이력서 업로드 → 5개 질문 생성 → 답변 제출 → 꼬리질문 → 면접 완료
- [ ] Gemini API가 한국어 면접 질문 5개 생성
- [ ] 꼬리질문이 불충분한 답변에 대해 정상 동작
- [ ] PDF/DOCX 파일에서 한국어 텍스트 정상 추출
- [ ] `./gradlew test` 전체 통과

### Must Have
- POST /api/interview/start — 이력서(텍스트 or 파일) + followUpEnabled → sessionId + jobField + 5개 질문
- POST /api/interview/{sessionId}/answer — 답변 제출 → needsFollowUp + followUpQuestion
- POST /api/interview/{sessionId}/complete — 면접 완료
- Gemini API 기반 질문 생성 (이력서 + 감지된 직무 기반)
- 꼬리질문 5가지 기준 평가 (역할, 성과, 근거, PAR, 직무연관)
- PDF (.pdf), DOCX (.docx) 텍스트 추출 (한국어 지원)
- 직접 입력 텍스트 처리
- 꼬리질문 ON/OFF (followUpEnabled 파라미터)
- 꼬리질문 depth=1 (1회만)
- 한국어 AI 응답

### Must NOT Have (Guardrails)
- ❌ 데이터베이스 (PostgreSQL, H2, JPA, Flyway 등)
- ❌ Docker / Docker Compose
- ❌ Spring Security / JWT / 인증
- ❌ 평가/피드백/스코어링 로직
- ❌ 사용자 관리 / 회원가입
- ❌ 파일 영구 저장 (업로드 파일은 텍스트 추출 후 폐기)
- ❌ 서킷 브레이커 / 복잡한 재시도 로직
- ❌ WebSocket / gRPC / GraphQL (REST only)
- ❌ 모니터링 / ELK (console 로깅만)
- ❌ 모범답안 생성
- ❌ 이력서 구조화 파싱 (섹션 분리) — 원문 텍스트 통째로 AI에 전달
- ❌ OCR
- ❌ HWP 파일 지원
- ❌ OpenAI/Claude 어댑터 코드 (Gemini만)
- ❌ 관리자 API / 대시보드

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: NO (새 프로젝트)
- **Automated tests**: Tests-after
- **Framework**: JUnit 5 + MockK

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **서버 기동**: `./gradlew bootRun` (백그라운드) + `curl` 요청
- **API 검증**: curl로 요청 + 상태 코드 + 응답 필드 검증
- **빌드 검증**: `./gradlew build` (exit code 0)

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 단독):
└── Task 1: Spring Boot 프로젝트 스캐폴딩 [quick]

Wave 2 (After Wave 1 — 병렬):
├── Task 2: AI 면접관 프롬프트 설계 [deep]
└── Task 3: PDF/DOCX 텍스트 추출 서비스 [unspecified-high]

Wave 3 (After Wave 2 — 병렬):
├── Task 4: 인메모리 세션 관리 + 면접 시작 API [unspecified-high]
└── Task 5: 답변 제출 + 꼬리질문 API [deep]

Wave 4 (After Wave 3 — 병렬):
├── Task 6: 면접 완료 API + 에러 핸들링 [quick]
├── Task 7: 단위 테스트 [unspecified-high]
└── Task 8: E2E curl 통합 테스트 [deep]

Wave FINAL (After ALL — 4 parallel reviews):
├── F1: Plan compliance audit (oracle)
├── F2: Code quality review (unspecified-high)
├── F3: Real manual QA (unspecified-high)
└── F4: Scope fidelity check (deep)
→ Present results → Get explicit user okay

Critical Path: T1 → T2 → T4 → T5 → T8 → F1-F4
Max Concurrent: 2 (Waves 2, 3, 4)
```

### Dependency Matrix

| Task | Depends On | Blocks | Wave |
|------|-----------|--------|------|
| 1 | - | 2, 3, 4, 5 | 1 |
| 2 | 1 | 4, 5 | 2 |
| 3 | 1 | 4 | 2 |
| 4 | 1, 2, 3 | 5, 6 | 3 |
| 5 | 2, 4 | 6 | 3 |
| 6 | 4, 5 | 8 | 4 |
| 7 | 3, 4, 5 | F1-F4 | 4 |
| 8 | 4, 5, 6 | F1-F4 | 4 |

### Agent Dispatch Summary

- **Wave 1**: **1** — T1 → `quick`
- **Wave 2**: **2** — T2 → `deep`, T3 → `unspecified-high`
- **Wave 3**: **2** — T4 → `unspecified-high`, T5 → `deep`
- **Wave 4**: **3** — T6 → `quick`, T7 → `unspecified-high`, T8 → `deep`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

- [x] 1. Spring Boot 프로젝트 스캐폴딩

  **What to do**:
  - Spring Initializr 패턴으로 Kotlin + Spring Boot 3.x 프로젝트 생성 (Gradle Kotlin DSL)
  - **프로젝트 디렉토리**: `backend/` (루트 아래)
  - **의존성**:
    - `spring-boot-starter-web`
    - `spring-ai-starter-model-google-genai` (Gemini API)
    - `jackson-module-kotlin` (JSON 직렬화)
    - `org.apache.pdfbox:pdfbox:2.0.32` (PDF 텍스트 추출)
    - `org.apache.poi:poi-ooxml:5.2.5` (DOCX 텍스트 추출)
    - Test: `spring-boot-starter-test`, `io.mockk:mockk`
  - **의존성 제외**: JPA, Flyway, PostgreSQL, Security, Docker
  - **application.yml**:
    ```yaml
    server:
      port: 8080
    spring:
      ai:
        google:
          genai:
            api-key: ${GEMINI_API_KEY:placeholder}
            chat:
              options:
                model: gemini-2.5-flash
                temperature: 0.7
      servlet:
        multipart:
          max-file-size: 10MB
          max-request-size: 10MB
    ```
  - **패키지 구조**:
    ```
    com.interview.server/
    ├── controller/     # REST 컨트롤러
    ├── service/        # 비즈니스 로직
    ├── model/          # 도메인 모델 (data class)
    ├── dto/            # 요청/응답 DTO
    ├── prompt/         # AI 프롬프트 파일
    ├── config/         # 설정 클래스
    └── exception/      # 예외 + 글로벌 핸들러
    ```
  - Health check: `GET /api/health` → `{"status": "ok"}`
  - `./gradlew bootRun`으로 정상 기동 확인

  **Must NOT do**:
  - JPA/Flyway/PostgreSQL 의존성 추가 금지
  - Docker Compose 파일 생성 금지
  - Spring Security 의존성 금지

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (Wave 1 단독)
  - **Blocks**: Tasks 2, 3, 4, 5
  - **Blocked By**: None

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 서버 정상 기동
    Tool: Bash
    Steps:
      1. `cd backend && ./gradlew bootRun &` (백그라운드 실행)
      2. 5초 대기
      3. `curl -s http://localhost:8080/api/health`
    Expected Result: `{"status":"ok"}`, HTTP 200
    Failure Indicators: 서버 기동 실패, 포트 충돌
    Evidence: .sisyphus/evidence/task-1-health-check.txt

  Scenario: 잘못된 endpoint 404 확인
    Tool: Bash
    Steps:
      1. `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/nonexistent`
    Expected Result: HTTP 404
    Evidence: .sisyphus/evidence/task-1-404.txt
  ```

  **Commit**: YES
  - Message: `chore(backend): init Spring Boot project with Spring AI + PDFBox`
  - Files: `backend/**`
  - Pre-commit: `./gradlew build`

- [x] 2. AI 면접관 프롬프트 설계

  **What to do**:
  - 프롬프트 파일 위치: `backend/src/main/resources/prompts/`
  - **`interview-system.md`** — 면접관 시스템 프롬프트:
    - 페르소나: 차분하고 전문적인 한국어 면접관
    - 원칙: 한 번에 한 질문만, 이력서에 없는 정보 가정 금지, 평가/점수 비공개
    - 한국어로만 대화
  - **`question-generation.md`** — 질문 생성 프롬프트:
    - 입력: `{{resume}}` (이력서 원문), `{{jobField}}` (감지된 직무)
    - 출력: JSON 배열 5개 (`[{questionId, content, category}]`)
    - category: 직무역량, 프로젝트경험, 문제해결, 커뮤니케이션, 성장목표 중 분배
    - 이력서 내용에 기반한 구체적 질문 생성 (일반론 금지)
  - **`followup-evaluation.md`** — 꼬리질문 평가 프롬프트:
    - 입력: `{{question}}` (원 질문), `{{answer}}` (사용자 답변), `{{resume}}` (이력서)
    - 5가지 불충분 기준:
      1. 지원자의 구체적 역할 불명확
      2. 정량적 성과/결과 미제시
      3. 의사결정 근거 미제시
      4. 문제→행동→결과(PAR) 흐름 깨짐
      5. 직무 연관성 약함
    - 출력: JSON `{needsFollowUp: boolean, followUpQuestion: string|null, reason: string}`
    - 기준 하나라도 해당 시 needsFollowUp=true
  - **`job-detection.md`** — 직무 감지 프롬프트:
    - 입력: `{{resume}}` (이력서 원문)
    - 출력: JSON `{jobField: string}` (AI 자유 감지 — "데이터 엔지니어링", "프론트엔드 개발" 등)
    - 이력서 내용에서 핵심 직무/직군을 한국어로 추출

  **Must NOT do**:
  - 평가/스코어링 프롬프트 금지
  - 모범답안 생성 프롬프트 금지
  - 영어 프롬프트 금지 (한국어로 작성)

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: LLM 프롬프트 엔지니어링은 SRS 원칙을 정확히 반영해야 하며 신중한 설계 필요
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: Tasks 4, 5
  - **Blocked By**: Task 1

  **References**:
  - `requirements_en.md` Section 3.2-3.3: 면접 세션 + 꼬리질문 규칙
  - Android 플랜의 API 계약 (Task 1): 서버 응답 JSON 구조

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 프롬프트 파일 완전성 확인
    Tool: Bash
    Steps:
      1. `ls backend/src/main/resources/prompts/`
      2. 4개 파일 확인: interview-system.md, question-generation.md, followup-evaluation.md, job-detection.md
      3. 각 파일에 {{resume}} 등 placeholder 존재 확인
    Expected Result: 4개 프롬프트 파일 존재, placeholder 포함
    Failure Indicators: 파일 누락, placeholder 없음
    Evidence: .sisyphus/evidence/task-2-prompt-files.txt

  Scenario: 꼬리질문 기준 5가지 포함 확인
    Tool: Bash
    Steps:
      1. followup-evaluation.md 파일 읽기
      2. 5가지 기준 (역할, 성과, 근거, PAR, 직무연관) 모두 명시되었는지 확인
    Expected Result: 5가지 기준 모두 프롬프트에 포함
    Failure Indicators: 기준 누락
    Evidence: .sisyphus/evidence/task-2-followup-criteria.txt
  ```

  **Commit**: YES
  - Message: `feat(backend): design AI interviewer prompts`
  - Files: `backend/src/main/resources/prompts/**`

- [x] 3. PDF/DOCX 텍스트 추출 서비스

  **What to do**:
  - **`TextExtractorService.kt`** (`service/`):
    - `fun extractText(inputStream: InputStream, fileName: String): String`
    - 파일 확장자로 분기: `.pdf` → PDFBox, `.docx` → POI
    - 지원하지 않는 확장자 → `UnsupportedFileException` throw
  - **PDF 추출** (PDFBox):
    - `PDDocument.load(inputStream, MemoryUsageSetting.setupMixed(50 * 1024 * 1024))`
    - `PDFTextStripper()` 사용
    - **한국어 대응 (PDFBOX-5350)**: `stripper.sortByPosition = true`
    - UTF-8 인코딩 강제
    - 추출 텍스트 50자 미만 시: `EmptyDocumentException` (스캔 PDF 의심)
  - **DOCX 추출** (Apache POI):
    - `XWPFDocument(inputStream)` → paragraphs 순회 → 텍스트 합치기
    - 한국어 정상 처리 (POI 5.x에서 이슈 없음)
  - **입력 검증**:
    - `inputStream.copyTo()` 패턴 사용 (절대 `getBytes()` 아님 — 메모리 폭발 방지)
    - 파일 없음 / 빈 파일 → 적절한 예외 throw
  - **텍스트 직접 입력**: 별도 추출 없이 그대로 사용

  **Must NOT do**:
  - 파일 영구 저장 금지 (추출 후 폐기)
  - OCR 금지
  - HWP 파일 지원 금지
  - 이력서 구조화 파싱 (섹션 분리) 금지

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: PDFBox 한국어 인코딩 이슈 + InputStream 메모리 관리 주의 필요
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 2)
  - **Blocks**: Task 4
  - **Blocked By**: Task 1

  **References**:
  - PDFBox Korean bug: PDFBOX-5350
  - `requirements_en.md` Section 3.1: PDF, Word (.docx), direct text input

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 텍스트 추출 서비스 빌드 확인
    Tool: Bash
    Steps:
      1. `./gradlew compileKotlin`
      2. TextExtractorService.kt 존재 + extractText 메서드 확인
    Expected Result: 컴파일 성공
    Failure Indicators: 의존성 에러, PDFBox import 실패
    Evidence: .sisyphus/evidence/task-3-extractor-build.txt

  Scenario: 지원하지 않는 파일 확장자 처리
    Tool: Bash (단위 테스트로 확인 — Task 7에서 정식 테스트)
    Steps:
      1. TextExtractorService에 .hwp 파일명 전달 시 예외 throw 코드 확인
    Expected Result: UnsupportedFileException throw
    Failure Indicators: 예외 없이 실행, 빈 문자열 반환
    Evidence: .sisyphus/evidence/task-3-unsupported-format.txt
  ```

  **Commit**: NO (groups with Wave 2)

- [ ] 4. 인메모리 세션 관리 + 면접 시작 API

  **What to do**:
  - **`InterviewSession.kt`** (`model/`) — 세션 도메인 모델:
    ```kotlin
    data class InterviewSession(
        val sessionId: String = UUID.randomUUID().toString(),
        val jobField: String,
        val coverLetterText: String,
        val followUpEnabled: Boolean,
        val questions: MutableList<InterviewQuestion>,
        var currentQuestionIndex: Int = 0,
        var status: SessionStatus = SessionStatus.IN_PROGRESS,
        val createdAt: Instant = Instant.now(),
        var completedAt: Instant? = null
    )
    data class InterviewQuestion(
        val questionId: String,
        val content: String,
        val category: String,
        var answer: String? = null,
        var followUpQuestion: InterviewQuestion? = null,
        val isFollowUp: Boolean = false
    )
    enum class SessionStatus { IN_PROGRESS, COMPLETED }
    ```
  - **`SessionStore.kt`** (`service/`) — 인메모리 저장소:
    ```kotlin
    @Service
    class SessionStore {
        private val sessions = ConcurrentHashMap<String, InterviewSession>()
        fun save(session: InterviewSession) { sessions[session.sessionId] = session }
        fun findById(sessionId: String): InterviewSession? = sessions[sessionId]
    }
    ```
  - **`InterviewService.kt`** (`service/`) — 면접 시작 로직:
    - `startInterview(coverLetterText: String?, file: MultipartFile?, followUpEnabled: Boolean): InterviewSession`
    - 파일 업로드 시: `textExtractorService.extractText(file.inputStream, file.originalFilename)` → 텍스트 추출
    - 텍스트 직접 입력 시: 그대로 사용
    - 텍스트 최소 10자 확인 → 미달 시 `CoverLetterTooShortException`
    - `jobDetectionService.detectJobField(coverLetterText)` → 직무 감지 (Gemini API)
    - `questionGenerationService.generateQuestions(coverLetterText, jobField)` → 5개 질문 생성 (Gemini API)
    - 세션 생성 → `sessionStore.save(session)` → 반환
  - **`QuestionGenerationService.kt`** (`service/`):
    - Spring AI `ChatClient` 사용
    - `question-generation.md` 프롬프트 로드 + `{{resume}}`, `{{jobField}}` 치환
    - Gemini API 호출 → JSON 응답 파싱 → `List<InterviewQuestion>` 반환
    - JSON 파싱 실패 시: 1회 재시도
  - **`JobDetectionService.kt`** (`service/`):
    - `job-detection.md` 프롬프트 로드 + `{{resume}}` 치환
    - Gemini API 호출 → `{jobField: "..."}` 파싱 → String 반환
  - **`InterviewController.kt`** (`controller/`):
    - `POST /api/interview/start`:
      - Multipart: `file` (PDF/DOCX) + `followUpEnabled` (Boolean)
      - 또는 JSON body: `{coverLetterText: String, followUpEnabled: Boolean}`
      - Response: `{sessionId, jobField, questions: [{questionId, content, orderIndex}]}`
  - **DTOs** (`dto/`):
    - `StartInterviewRequest.kt`: `{coverLetterText: String?, followUpEnabled: Boolean}`
    - `StartInterviewResponse.kt`: `{sessionId, jobField, questions: [QuestionDto]}`
    - `QuestionDto.kt`: `{questionId, content, orderIndex}`

  **Must NOT do**:
  - DB 저장 금지
  - 세션 만료/정리 로직 금지 (MVP에서는 서버 재시작으로 정리)
  - Gemini API 키를 코드에 하드코딩 금지 (환경변수/yml에서 읽기)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Spring AI + Gemini 통합 + 세션 관리 + 컨트롤러가 복합적
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 5)
  - **Blocks**: Tasks 5, 6
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - Task 2 프롬프트: question-generation.md, job-detection.md
  - Task 3 TextExtractorService: 파일 텍스트 추출
  - Android 플랜 API 계약 (Task 1): POST /api/interview/start 명세

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 텍스트 이력서로 면접 시작 성공
    Tool: Bash (curl)
    Preconditions: 서버 실행 중, GEMINI_API_KEY 환경변수 설정
    Steps:
      1. curl -X POST http://localhost:8080/api/interview/start \
           -H "Content-Type: application/json" \
           -d '{"coverLetterText":"안녕하세요. 3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용하며 MSA 환경에서 결제 시스템을 구축한 경험이 있습니다. PostgreSQL과 Redis를 활용한 캐시 전략 설계를 주도했습니다.", "followUpEnabled":true}'
      2. 응답에서 sessionId, jobField, questions 확인
      3. questions 배열 길이 5 확인
      4. 모든 질문이 한국어인지 확인
    Expected Result: HTTP 200, sessionId + jobField + 5개 한국어 질문
    Failure Indicators: 500 에러, 질문 수 불일치, 영어 질문
    Evidence: .sisyphus/evidence/task-4-start-interview.json

  Scenario: 짧은 이력서 텍스트 시 400 에러
    Tool: Bash (curl)
    Steps:
      1. curl -X POST http://localhost:8080/api/interview/start \
           -H "Content-Type: application/json" \
           -d '{"coverLetterText":"짧음", "followUpEnabled":true}'
    Expected Result: HTTP 400, 에러 메시지
    Failure Indicators: 200 반환, 질문 생성 시도
    Evidence: .sisyphus/evidence/task-4-short-text-400.json
  ```

  **Commit**: YES
  - Message: `feat(backend): add interview start API with Gemini question generation`
  - Files: `controller/InterviewController.kt`, `service/*.kt`, `model/*.kt`, `dto/*.kt`
  - Pre-commit: `./gradlew build`

- [ ] 5. 답변 제출 + 꼬리질문 API

  **What to do**:
  - **`FollowUpEvaluationService.kt`** (`service/`):
    - `fun evaluateAnswer(question: String, answer: String, resume: String): FollowUpResult`
    - `followup-evaluation.md` 프롬프트 로드 + 변수 치환
    - Gemini API 호출 → `{needsFollowUp, followUpQuestion, reason}` 파싱
    - 빈 답변 / 10자 미만: LLM 호출 없이 자동 needsFollowUp=true + 기본 꼬리질문 ("조금 더 구체적으로 말씀해주시겠어요?")
  - **InterviewService에 추가** (`submitAnswer()`):
    - `submitAnswer(sessionId: String, questionId: String, answer: String): AnswerResult`
    - 세션 조회 → 질문 찾기 → 답변 저장
    - followUpEnabled=false: needsFollowUp=false 강제 (LLM 평가 스킵)
    - followUpEnabled=true:
      - 현재 질문이 이미 꼬리질문(isFollowUp=true)이면: needsFollowUp=false (depth=1 강제)
      - 그 외: `followUpEvaluationService.evaluateAnswer()` 호출
    - needsFollowUp=true: 새 InterviewQuestion 생성 (isFollowUp=true, 부모 질문에 연결)
    - needsFollowUp=false: currentQuestionIndex 증가 → 다음 질문 또는 면접 종료 준비
  - **InterviewController에 추가**:
    - `POST /api/interview/{sessionId}/answer`:
      - Request: `{questionId, answer}`
      - Response: `{needsFollowUp, followUpQuestion: {questionId, content} | null}`
  - **DTOs**:
    - `SubmitAnswerRequest.kt`: `{questionId, answer}`
    - `SubmitAnswerResponse.kt`: `{needsFollowUp, followUpQuestion: QuestionDto?}`
  - **Gemini API 실패 처리**: 1회 재시도 → 실패 시 503 응답

  **Must NOT do**:
  - 답변 평가 점수/스코어 생성 금지
  - 피드백 텍스트 생성 금지
  - 꼬리질문 depth > 1 금지
  - 복잡한 재시도 로직 금지 (1회만)

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: LLM 기반 답변 평가 + 꼬리질문 상태 머신이 핵심 비즈니스 로직
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Task 4)
  - **Blocks**: Task 6
  - **Blocked By**: Tasks 2, 4

  **References**:
  - Task 2 프롬프트: followup-evaluation.md
  - Task 4: InterviewSession 모델 + SessionStore
  - `requirements_en.md` Section 3.3: 꼬리질문 규칙

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 불충분한 답변 → 꼬리질문 생성
    Tool: Bash (curl)
    Preconditions: 면접 세션 시작됨 (Task 4 완료)
    Steps:
      1. curl -X POST http://localhost:8080/api/interview/{sessionId}/answer \
           -H "Content-Type: application/json" \
           -d '{"questionId":"q1", "answer":"네, 그렇습니다."}'
      2. needsFollowUp: true 확인
      3. followUpQuestion 존재 확인
    Expected Result: HTTP 200, needsFollowUp=true, 한국어 꼬리질문 포함
    Failure Indicators: needsFollowUp=false (불충분한 답변인데 충분으로 판단)
    Evidence: .sisyphus/evidence/task-5-followup-triggered.json

  Scenario: 꼬리질문 답변 → 추가 꼬리질문 없음 (depth=1)
    Tool: Bash (curl)
    Steps:
      1. 꼬리질문의 questionId로 불충분한 답변 제출
      2. needsFollowUp: false 확인 (depth=1 강제)
    Expected Result: needsFollowUp=false (꼬리질문의 꼬리질문 없음)
    Failure Indicators: needsFollowUp=true (depth > 1)
    Evidence: .sisyphus/evidence/task-5-depth-1-enforced.json

  Scenario: followUpEnabled=false 시 꼬리질문 스킵
    Tool: Bash (curl)
    Steps:
      1. followUpEnabled=false로 면접 시작
      2. 불충분한 답변 제출
      3. needsFollowUp: false 확인
    Expected Result: LLM 평가 없이 needsFollowUp=false 즉시 반환
    Failure Indicators: needsFollowUp=true (토글 무시)
    Evidence: .sisyphus/evidence/task-5-followup-disabled.json
  ```

  **Commit**: YES
  - Message: `feat(backend): add answer submission with follow-up evaluation`
  - Files: `service/FollowUpEvaluationService.kt`, `controller/InterviewController.kt`, `dto/SubmitAnswer*.kt`
  - Pre-commit: `./gradlew build`

- [ ] 6. 면접 완료 API + 글로벌 에러 핸들링

  **What to do**:
  - **InterviewService에 추가** (`completeInterview()`):
    - `completeInterview(sessionId: String): CompletionResult`
    - 세션 조회 → status 확인 (이미 COMPLETED면 400)
    - status = COMPLETED, completedAt = Instant.now() 설정
    - 응답: sessionId + jobField + completedAt
  - **InterviewController에 추가**:
    - `POST /api/interview/{sessionId}/complete`:
      - Request: 빈 body
      - Response: `{sessionId, jobField, completedAt}`
  - **글로벌 예외 핸들러** (`exception/`):
    - `GlobalExceptionHandler.kt` — `@RestControllerAdvice`:
      ```kotlin
      @ExceptionHandler(SessionNotFoundException::class)
      fun handleSessionNotFound(e: SessionNotFoundException): ResponseEntity<ErrorResponse> {
          return ResponseEntity.status(404).body(ErrorResponse("SESSION_NOT_FOUND", e.message ?: "세션을 찾을 수 없습니다."))
      }
      // CoverLetterTooShortException → 400
      // UnsupportedFileException → 400
      // SessionAlreadyCompletedException → 400
      // GeminiApiException → 503
      // Exception (기타) → 500
      ```
    - `ErrorResponse.kt`: `{error: String, message: String}`
  - **커스텀 예외 클래스들** (`exception/`):
    - `SessionNotFoundException`
    - `CoverLetterTooShortException`
    - `UnsupportedFileException`
    - `SessionAlreadyCompletedException`
    - `GeminiApiException`

  **Must NOT do**:
  - 스택트레이스를 클라이언트에 노출 금지
  - 복잡한 에러 코드 체계 금지 (단순 문자열 코드만)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 완료 API는 단순 + 예외 핸들러는 패턴 반복
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (Task 5 완료 후)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 4, 5

  **References**:
  - Android 플랜 API 계약: POST /api/interview/{sessionId}/complete 명세
  - Task 4, 5: InterviewService, InterviewController 확장

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 면접 완료 성공
    Tool: Bash (curl)
    Steps:
      1. curl -X POST http://localhost:8080/api/interview/{sessionId}/complete
      2. sessionId, jobField, completedAt 확인
    Expected Result: HTTP 200, completedAt 포함
    Failure Indicators: 500 에러
    Evidence: .sisyphus/evidence/task-6-complete-interview.json

  Scenario: 존재하지 않는 세션 404
    Tool: Bash (curl)
    Steps:
      1. curl -X POST http://localhost:8080/api/interview/nonexistent/complete
    Expected Result: HTTP 404, {"error":"SESSION_NOT_FOUND","message":"..."}
    Failure Indicators: 200 반환, 스택트레이스 노출
    Evidence: .sisyphus/evidence/task-6-session-not-found.json

  Scenario: 이미 완료된 세션 재완료 시도 400
    Tool: Bash (curl)
    Steps:
      1. 이미 완료된 sessionId로 complete 재호출
    Expected Result: HTTP 400, {"error":"SESSION_ALREADY_COMPLETED","message":"..."}
    Evidence: .sisyphus/evidence/task-6-already-completed.json
  ```

  **Commit**: YES
  - Message: `feat(backend): add interview completion API and global error handling`
  - Files: `controller/InterviewController.kt`, `exception/*.kt`, `dto/CompletionResponse.kt`
  - Pre-commit: `./gradlew build`

- [ ] 7. 단위 테스트

  **What to do**:
  - **`TextExtractorServiceTest.kt`** (`src/test/`):
    - PDF 텍스트 추출 (테스트용 한국어 PDF 생성 또는 fixture)
    - DOCX 텍스트 추출
    - 지원하지 않는 파일 → UnsupportedFileException
    - 빈 PDF → EmptyDocumentException
  - **`InterviewServiceTest.kt`**:
    - startInterview: 정상 텍스트 → 세션 생성 + 5개 질문
    - startInterview: 짧은 텍스트 → CoverLetterTooShortException
    - submitAnswer: 불충분 답변 → needsFollowUp=true
    - submitAnswer: 꼬리질문 답변 → needsFollowUp=false (depth=1)
    - submitAnswer: followUpEnabled=false → needsFollowUp=false
    - completeInterview: 정상 완료
    - completeInterview: 이미 완료 → SessionAlreadyCompletedException
    - MockK로 QuestionGenerationService, FollowUpEvaluationService mock
  - **`SessionStoreTest.kt`**:
    - save + findById 정상 동작
    - 존재하지 않는 ID → null 반환

  **Must NOT do**:
  - 실제 Gemini API 호출하는 테스트 금지 (MockK로 mock)
  - Integration 테스트 (서버 기동) 이 태스크에서 금지

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 여러 서비스에 대한 MockK 기반 단위 테스트
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 6, 8)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 3, 4, 5

  **References**:
  - Task 3 TextExtractorService
  - Task 4 InterviewService + SessionStore
  - Task 5 FollowUpEvaluationService

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 전체 단위 테스트 통과
    Tool: Bash
    Steps:
      1. `cd backend && ./gradlew test`
      2. exit code 0 확인
      3. 테스트 리포트 확인
    Expected Result: 모든 테스트 GREEN, 0 failures
    Failure Indicators: 1개 이상 failure
    Evidence: .sisyphus/evidence/task-7-unit-tests.txt
  ```

  **Commit**: NO (groups with Task 8)

- [ ] 8. E2E curl 통합 테스트

  **What to do**:
  - **사전 조건**: `GEMINI_API_KEY` 환경변수 설정 + `./gradlew bootRun` 서버 실행
  - **전체 면접 플로우 테스트** (curl 스크립트):
    1. **면접 시작** (텍스트):
       ```bash
       curl -X POST http://localhost:8080/api/interview/start \
         -H "Content-Type: application/json" \
         -d '{"coverLetterText":"안녕하세요. 3년차 백엔드 개발자입니다...(100자 이상)", "followUpEnabled":true}'
       ```
       → sessionId, jobField, 5개 questions 확인
    2. **불충분한 답변 제출**:
       ```bash
       curl -X POST http://localhost:8080/api/interview/{sessionId}/answer \
         -H "Content-Type: application/json" \
         -d '{"questionId":"q1", "answer":"네"}'
       ```
       → needsFollowUp=true, followUpQuestion 존재 확인
    3. **꼬리질문 답변**:
       ```bash
       curl -X POST http://localhost:8080/api/interview/{sessionId}/answer \
         -H "Content-Type: application/json" \
         -d '{"questionId":"{followUpQuestionId}", "answer":"구체적 답변..."}'
       ```
       → needsFollowUp=false (depth=1 확인)
    4. **나머지 질문 답변** (2~5번 질문 순회):
       → 각 답변 제출 + 정상 응답 확인
    5. **면접 완료**:
       ```bash
       curl -X POST http://localhost:8080/api/interview/{sessionId}/complete
       ```
       → completedAt 존재 확인
  - **에러 케이스 테스트**:
    - 짧은 이력서 → 400
    - 존재하지 않는 sessionId → 404
    - 이미 완료된 세션 재완료 → 400
  - **PDF 파일 업로드 테스트** (한국어 PDF 파일이 있으면):
    ```bash
    curl -X POST http://localhost:8080/api/interview/start \
      -F "file=@test_resume.pdf" \
      -F "followUpEnabled=true"
    ```

  **Must NOT do**:
  - 비용이 많이 드는 반복 테스트 금지 (최소한으로)
  - 서버 없이 통합 테스트 수행 금지

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 전체 플로우를 실제 서버 + Gemini API로 검증
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 6, 7)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 4, 5, 6

  **References**:
  - Android 플랜 API 계약 (Task 1): 모든 endpoint 검증

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 전체 면접 플로우 E2E 성공
    Tool: Bash (curl)
    Preconditions: 서버 실행 중, GEMINI_API_KEY 설정
    Steps:
      1. POST /api/interview/start → sessionId 획득
      2. POST /api/interview/{sessionId}/answer (불충분 답변) → needsFollowUp=true
      3. POST /api/interview/{sessionId}/answer (꼬리질문 답변) → needsFollowUp=false
      4. 나머지 질문 순회 답변
      5. POST /api/interview/{sessionId}/complete → completedAt 확인
    Expected Result: 전체 플로우 성공, 4xx/5xx 없음
    Failure Indicators: 서버 에러, 응답 필드 누락
    Evidence: .sisyphus/evidence/task-8-e2e-full-flow.json

  Scenario: 에러 케이스 3종 확인
    Tool: Bash (curl)
    Steps:
      1. 짧은 텍스트 → 400
      2. 존재하지 않는 sessionId → 404
      3. 이미 완료된 세션 → 400
    Expected Result: 각 에러 코드 정확히 반환
    Evidence: .sisyphus/evidence/task-8-error-cases.json
  ```

  **Commit**: YES (groups Tasks 7, 8)
  - Message: `test(backend): add unit tests and E2E integration tests`
  - Files: `src/test/**`
  - Pre-commit: `./gradlew test`

---

## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE.

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read plan. For each "Must Have": verify implementation. For each "Must NOT Have": search for forbidden patterns. Check evidence files.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | VERDICT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `./gradlew build` + `./gradlew test`. Review: empty catches, println in prod, commented-out code, AI slop.
  Output: `Build [PASS/FAIL] | Tests [N/N] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high`
  Execute EVERY QA scenario from EVERY task. Test edge cases.
  Output: `Scenarios [N/N pass] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: verify actual diff matches "What to do". Check "Must NOT do" compliance.
  Output: `Tasks [N/N compliant] | VERDICT`

---

## Commit Strategy

- T1: `chore(backend): init Spring Boot project with Spring AI + PDFBox`
- T2: `feat(backend): design AI interviewer prompts`
- T3+T2: `feat(backend): add PDF/DOCX text extraction and AI prompts`
- T4: `feat(backend): add interview start API with Gemini question generation`
- T5: `feat(backend): add answer submission with follow-up evaluation`
- T6: `feat(backend): add interview completion API and global error handling`
- T7+T8: `test(backend): add unit tests and E2E integration tests`

---

## Success Criteria

### Verification Commands
```bash
cd backend
./gradlew build         # Expected: BUILD SUCCESSFUL
./gradlew test          # Expected: N tests, 0 failures
./gradlew bootRun       # Expected: 서버 8080 포트 기동
# 별도 터미널에서:
curl http://localhost:8080/api/health   # Expected: {"status":"ok"}
```

### Final Checklist
- [ ] 서버 `./gradlew bootRun`으로 정상 기동
- [ ] 전체 면접 플로우 curl 테스트 통과
- [ ] Gemini API 한국어 질문 생성 확인
- [ ] 꼬리질문 depth=1 강제 확인
- [ ] followUpEnabled=false 시 꼬리질문 스킵 확인
- [ ] PDF/DOCX 한국어 텍스트 추출 확인
- [ ] 전체 단위 테스트 통과
