  # Android Interview Practice App — MVP Work Plan

## TL;DR

> **Quick Summary**: Kotlin + Jetpack Compose로 이력서 기반 AI 면접 연습 Android 앱 MVP를 처음부터 구축한다. 사용자는 이력서(PDF/DOCX/텍스트)를 업로드하고 꼬리질문 ON/OFF 설정 후 Gemini API가 생성한 ~10문항 텍스트 채팅 면접을 진행한다.
>
> **Deliverables**:
> - REST API 계약 문서 (`docs/api-contract.md`) — 서버 개발자와 공유
> - Android 앱 (Kotlin + Jetpack Compose + MVVM + Clean Architecture)
> - 5개 화면: Home → Upload → Interview Setup → Interview → Completion
> - Room DB (로컬 면접 세션 저장)
>
> **Estimated Effort**: Large
> **Parallel Execution**: YES — 7 waves
> **Critical Path**: T1/T2 → T3/T4 → T5/T6/T7 → T8 → T9/T10/T11 → T12/T13 → T14/T15 → F1-F4

---

## Context

### Original Request
requirements_en.md 기반으로 이력서 업로드 + Gemini AI 면접 연습 Android MVP 앱을 처음부터 구축.

### Interview Summary
**Key Discussions**:
- **범위**: Android 앱 전용 + API 계약 정의 (iOS/서버/Discord Bot 제외)
- **화면**: 5개 (Home, Upload, Interview Setup, Interview, Completion) — 히스토리 화면 없음
- **서버**: Spring Boot Kotlin, 별도 구축 예정. Android 개발은 서버 완성 후 진행
- **PDF/DOCX 파싱**: Android에서 수행하지 않음 — 파일 바이너리를 서버로 전송, 서버에서 추출
- **테스트 전략**: 구현 후 단위/통합 테스트 추가 (TDD 아님)
- **인증**: 없음 (로그인 불필요, deviceId 기반 식별)
- **언어**: 한국어 하드코딩

**Research Findings**:
- 꼬리질문 평가 기준 5가지: 역할 불명확, 정량적 성과 없음, 의사결정 근거 없음, PAR 흐름 깨짐, 직무 연관성 약함
- ~10문항 = 기본 5문항 + 꼬리질문 최대 5회 (문항당 1회 제한, depth=1)
- PDF/DOCX 파싱은 서버에서 PDFBox/POI 사용 — Android는 파일 업로드만

### Metis Review
**Identified Gaps** (addressed):
- **PDF/DOCX 파싱 위치 명확화**: Android는 파일만 전송, 서버가 추출 처리
- **서버 없이 개발 불가**: 서버를 먼저 구축한 후 Android 개발 진행
- **히스토리 화면 없음 확인**: Room에 저장하되 조회 화면은 MVP 외
- **꼬리질문 토글 상태**: API 계약에 `followUpEnabled` 파라미터 포함
- **AI slop 가드레일 적용**: 불필요한 추상화, 과도한 UseCase 금지

---

## Work Objectives

### Core Objective
이력서 기반 텍스트 채팅 AI 면접 연습 Android MVP를 구축한다. 사용자는 이력서를 업로드하고, 꼬리질문 설정 후 Gemini LLM이 생성한 ~10문항 면접을 텍스트로 진행한 뒤 완료 요약을 확인한다.

### Concrete Deliverables
- `docs/api-contract.md` — REST API 계약 (서버 개발자 공유용)
- Android 앱 APK (디버그 빌드)
- 5개 화면 Compose UI
- Room DB 스키마 (InterviewSession + ChatMessage)

### Definition of Done
- [ ] `./gradlew assembleDebug` 성공 (exit code 0)
- [ ] `./gradlew testDebugUnitTest` 전체 통과
- [ ] 5개 화면 모두 네비게이션 가능
- [ ] Upload → Interview Setup → Interview → Completion 전체 플로우 서버 연동 동작
- [ ] 꼬리질문 ON 상태에서 불충분한 답변 → 꼬리질문 표시 확인
- [ ] 면접 완료 후 Room DB에 세션 데이터 저장 확인

### Must Have
- PDF, DOCX, 텍스트 3가지 이력서 입력 방식
- 꼬리질문 ON/OFF 토글 (면접 시작 전 설정)
- 텍스트 채팅 UI (Q&A 말풍선 형식)
- ~10문항 (기본 5문항 + 꼬리질문 최대 5회, 문항당 depth=1)
- 꼬리질문 기준 5가지 서버 평가
- 면접 완료 화면 (모든 Q&A 요약)
- Room 로컬 세션 저장
- 한국어 전용 UI

### Must NOT Have (Guardrails)
- ❌ 히스토리 조회 화면 — 5개 화면만
- ❌ Android에서 PDF/DOCX 텍스트 파싱 — 파일 업로드만
- ❌ Gemini API 키를 Android 앱에 하드코딩
- ❌ 로그인/JWT/인증 코드
- ❌ 평가/피드백/스코어링 기능
- ❌ 음성 입력
- ❌ 카드 스와이프 UI (텍스트 채팅 UI만)
- ❌ 히스토리 화면 관련 UI 또는 Room 쿼리
- ❌ 오프라인 동기화 로직
- ❌ String resource XML — 한국어 하드코딩
- ❌ Timber, Coil, Glide, Paging3 등 계획에 없는 새 라이브러리 추가
- ❌ i18n, 다국어 지원
- ❌ 추상화된 BaseViewModel, BaseRepository 등
- ❌ pass-through UseCase (검증/변환 로직 없는 단순 래퍼)

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: NO (새 프로젝트)
- **Automated tests**: Tests-after (구현 완료 후 테스트 추가)
- **Framework**: JUnit 5 + MockK + Coroutines Test + Compose UI Test

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **Build 검증**: `./gradlew assembleDebug` (exit code 0)
- **단위 테스트**: `./gradlew testDebugUnitTest`
- **API 계약 검증**: Bash로 문서 내용 확인 (curl은 서버 완성 후)
- **네비게이션 검증**: Compose Preview 컴파일 + NavHost route 존재 확인

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 병렬):
├── Task 1: REST API 계약 정의 [unspecified-high]
└── Task 2: Android 프로젝트 스캐폴딩 [quick]

Wave 2 (After Wave 1 — 병렬):
├── Task 3: Navigation 그래프 + UI 테마 [visual-engineering]
└── Task 4: Domain Models + Repository Interface [quick]

Wave 3 (After Wave 2 — 병렬):
├── Task 5: Retrofit API 클라이언트 + DTOs [unspecified-high]
├── Task 6: Room DB 설정 [quick]
└── Task 7: UseCases 구현 [unspecified-high]

Wave 4 (After Wave 3 — 단독):
└── Task 8: Repository Implementation + Hilt DI [unspecified-high]

Wave 5 (After Wave 4 — 병렬):
├── Task 9: Home 화면 + HomeViewModel [visual-engineering]
├── Task 10: Upload 화면 + UploadViewModel [visual-engineering]
└── Task 11: Interview Setup 화면 + SetupViewModel [visual-engineering]

Wave 6 (After Wave 5 — 병렬):
├── Task 12: Interview 화면 + InterviewViewModel [deep]
└── Task 13: Completion 화면 + CompletionViewModel [visual-engineering]

Wave 7 (After Wave 6 — 병렬):
├── Task 14: 단위 테스트 [unspecified-high]
└── Task 15: E2E 통합 테스트 [deep]

Wave FINAL (After ALL tasks — 4 parallel reviews):
├── F1: Plan compliance audit (oracle)
├── F2: Code quality review (unspecified-high)
├── F3: Real manual QA (unspecified-high)
└── F4: Scope fidelity check (deep)
→ Present results → Get explicit user okay

Critical Path: T1 → T4 → T7 → T8 → T12 → T15 → F1-F4
Parallel Speedup: ~60% faster than sequential
Max Concurrent: 3 (Waves 2, 3, 5, 7)
```

### Dependency Matrix

| Task | Depends On | Blocks | Wave |
|------|-----------|--------|------|
| 1 | - | 5, 7 | 1 |
| 2 | - | 3, 4, 5, 6 | 1 |
| 3 | 2 | 9, 10, 11 | 2 |
| 4 | 2 | 5, 7, 8 | 2 |
| 5 | 1, 2, 4 | 8 | 3 |
| 6 | 2 | 8 | 3 |
| 7 | 1, 4 | 8 | 3 |
| 8 | 4, 5, 6, 7 | 9, 10, 11 | 4 |
| 9 | 3, 8 | 15 | 5 |
| 10 | 3, 8 | 11 | 5 |
| 11 | 3, 8, 10 | 12 | 5 |
| 12 | 8, 11 | 13, 15 | 6 |
| 13 | 8, 12 | 15 | 6 |
| 14 | 5, 6, 7, 8 | F1-F4 | 7 |
| 15 | 9-13 | F1-F4 | 7 |

### Agent Dispatch Summary

- **Wave 1**: **2** — T1 → `unspecified-high`, T2 → `quick`
- **Wave 2**: **2** — T3 → `visual-engineering`, T4 → `quick`
- **Wave 3**: **3** — T5 → `unspecified-high`, T6 → `quick`, T7 → `unspecified-high`
- **Wave 4**: **1** — T8 → `unspecified-high`
- **Wave 5**: **3** — T9 → `visual-engineering`, T10 → `visual-engineering`, T11 → `visual-engineering`
- **Wave 6**: **2** — T12 → `deep`, T13 → `visual-engineering`
- **Wave 7**: **2** — T14 → `unspecified-high`, T15 → `deep`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

> Implementation + Test = ONE Task. Never separate.
> EVERY task MUST have: Recommended Agent Profile + Parallelization info + QA Scenarios.

- [ ] 1. REST API 계약 정의

  **What to do**:
  - `docs/api-contract.md` 파일 작성
  - **Endpoints** (총 3개):
    1. `POST /api/interview/start`
       - Request: `{ coverLetterText: String, followUpEnabled: Boolean }`
       - 또는 Multipart: `coverLetterFile` (PDF/DOCX 바이너리) + `followUpEnabled: Boolean`
       - Response: `{ sessionId: String, jobField: String, questions: [{ questionId: String, content: String, orderIndex: Int }] }`
       - questions 배열 길이: 5 (기본 질문)
       - Error: 400 (이력서 텍스트 너무 짧음), 503 (LLM 실패)
    2. `POST /api/interview/{sessionId}/answer`
       - Request: `{ questionId: String, answer: String }`
       - Response: `{ needsFollowUp: Boolean, followUpQuestion: { questionId: String, content: String } | null }`
       - `needsFollowUp: false`이면 `followUpQuestion: null`
       - Error: 404 (세션 없음), 503 (LLM 실패)
    3. `POST /api/interview/{sessionId}/complete`
       - Request: 없음 (빈 body)
       - Response: `{ sessionId: String, jobField: String, completedAt: String (ISO 8601) }`
       - Error: 404 (세션 없음), 400 (미완료 질문 존재)
  - 각 endpoint에 요청/응답 JSON 예시 포함
  - 공통 에러 응답 형식: `{ "error": "ERROR_CODE", "message": "한국어 메시지" }`
  - 에러 코드 표: `COVER_LETTER_TOO_SHORT`, `LLM_SERVICE_ERROR`, `SESSION_NOT_FOUND`, `SESSION_ALREADY_COMPLETED`

  **Must NOT do**:
  - 인증/JWT 관련 헤더 정의 금지
  - 히스토리 조회 endpoint 정의 금지 (MVP 범위 외)
  - 평가/피드백 endpoint 정의 금지

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: API 계약은 서버 개발과 Android 개발 양쪽을 연결하는 핵심 산출물. 정확성 중요
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Tasks 5, 7
  - **Blocked By**: None (can start immediately)

  **References**:
  - `requirements_en.md` Section 3: MVP Core Features (면접 플로우)
  - `requirements_en.md` Section 3.3: Follow-up question criteria (5가지)
  - `requirements_en.md` Section 4.1: Communication flow diagram

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: API 계약 문서 완전성 확인
    Tool: Bash
    Preconditions: docs/api-contract.md 생성됨
    Steps:
      1. Read docs/api-contract.md
      2. 3개 endpoint 모두 정의되었는지 확인
      3. 각 endpoint에 request/response JSON 예시 존재 확인
      4. 에러 코드 표 존재 확인
    Expected Result: 3개 endpoint + 에러 코드 표 + JSON 예시 모두 존재
    Failure Indicators: 누락된 endpoint, JSON 예시 없음, 에러 코드 미정의
    Evidence: .sisyphus/evidence/task-1-api-contract-completeness.txt

  Scenario: followUpEnabled 파라미터 정의 확인
    Tool: Bash
    Preconditions: 문서 존재
    Steps:
      1. POST /api/interview/start request body에 followUpEnabled 필드 확인
      2. 서버가 followUpEnabled=false 시 꼬리질문 평가 스킵하도록 명시되었는지 확인
    Expected Result: followUpEnabled 파라미터 정의 + 동작 설명 존재
    Failure Indicators: 필드 누락, 동작 설명 없음
    Evidence: .sisyphus/evidence/task-1-followup-param.txt
  ```

  **Commit**: YES
  - Message: `docs: define REST API contract for MVP`
  - Files: `docs/api-contract.md`
  - Pre-commit: N/A (텍스트 파일)

- [ ] 2. Android 프로젝트 스캐폴딩

  **What to do**:
  - Android Studio에서 Empty Activity (Compose) 템플릿으로 새 프로젝트 생성
  - **패키지명**: `com.interview.app` (또는 사용자 도메인에 맞게)
  - **앱 이름**: `"면접 연습"` (strings.xml 불필요, 하드코딩)
  - `build.gradle.kts` (app) 의존성:
    - Jetpack Compose BOM (최신 안정 버전)
    - Hilt: `com.google.dagger:hilt-android`, `hilt-android-compiler`
    - Retrofit + OkHttp + Moshi: `com.squareup.retrofit2:retrofit`, `converter-moshi`, `okhttp3:logging-interceptor`
    - Room: `androidx.room:room-runtime`, `room-ktx`, `room-compiler` (KSP)
    - Navigation Compose: `androidx.navigation:navigation-compose`
    - Coroutines: `kotlinx-coroutines-android`
    - ViewModel Compose: `androidx.lifecycle:lifecycle-viewmodel-compose`
    - Hilt Navigation Compose: `androidx.hilt:hilt-navigation-compose`
    - Activity Result (파일 피커용): `androidx.activity:activity-ktx`
  - **프로젝트 패키지 구조** (Clean Architecture):
    ```
    com.interview.app/
    ├── presentation/
    │   ├── screen/          # Composable 화면들
    │   ├── viewmodel/       # ViewModel 클래스들
    │   └── theme/           # Material3 테마
    ├── domain/
    │   ├── model/           # Domain 모델 (순수 Kotlin data class)
    │   ├── repository/      # Repository 인터페이스
    │   └── usecase/         # UseCase 클래스들
    ├── data/
    │   ├── remote/
    │   │   ├── api/         # Retrofit 인터페이스
    │   │   └── dto/         # 네트워크 DTO
    │   ├── local/
    │   │   ├── entity/      # Room Entity
    │   │   └── dao/         # Room DAO
    │   └── repository/      # Repository 구현체
    ├── di/                  # Hilt 모듈
    └── util/                # 유틸리티 (DeviceIdManager 등)
    ```
  - `InterviewApp.kt`: `@HiltAndroidApp` Application 클래스
  - `MainActivity.kt`: `@AndroidEntryPoint`, NavHost 진입점
  - `BuildConfig` or `local.properties`에 BASE_URL 설정:
    - debug: `http://10.0.2.2:8080` (에뮬레이터 localhost)
    - release: TBD (서버 배포 후)
  - `./gradlew assembleDebug` 빌드 성공 확인

  **Must NOT do**:
  - Firebase/FCM 의존성 추가 금지
  - 결제 라이브러리 추가 금지
  - Timber, Coil 등 계획에 없는 라이브러리 추가 금지
  - String resource XML 생성 금지 (한국어 하드코딩)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 프로젝트 초기화는 정해진 패턴의 반복 작업
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Tasks 3, 4, 5, 6
  - **Blocked By**: None (can start immediately)

  **References**:
  - `requirements_en.md` Section 2: Tech Stack (Kotlin, MVVM)
  - Jetpack Compose BOM: https://developer.android.com/develop/ui/compose/bom
  - Hilt setup: https://developer.android.com/training/dependency-injection/hilt-android

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: APK 빌드 성공
    Tool: Bash
    Preconditions: Android SDK 설치, ANDROID_HOME 설정
    Steps:
      1. `./gradlew assembleDebug` 실행 (android/ 디렉토리에서)
      2. exit code 0 확인
      3. `ls app/build/outputs/apk/debug/app-debug.apk` 파일 존재 확인
    Expected Result: BUILD SUCCESSFUL, APK 파일 생성
    Failure Indicators: BUILD FAILED, 의존성 충돌, 컴파일 에러
    Evidence: .sisyphus/evidence/task-2-build-success.txt

  Scenario: Hilt 어노테이션 정상 처리
    Tool: Bash
    Preconditions: 빌드 성공
    Steps:
      1. `grep -r "@HiltAndroidApp" app/src/main/` — Application 클래스 확인
      2. `grep -r "@AndroidEntryPoint" app/src/main/` — Activity 확인
      3. `./gradlew compileDebugKotlin` exit code 0 확인
    Expected Result: @HiltAndroidApp과 @AndroidEntryPoint 존재, 컴파일 성공
    Failure Indicators: Hilt 관련 컴파일 에러, 어노테이션 없음
    Evidence: .sisyphus/evidence/task-2-hilt-annotations.txt
  ```

  **Commit**: YES
  - Message: `chore(android): init project with Compose + Hilt + Retrofit + Room`
  - Files: `android/**`
  - Pre-commit: `./gradlew assembleDebug`

- [ ] 3. Navigation 그래프 + UI 테마

  **What to do**:
  - **Navigation 설정** (`presentation/navigation/`):
    - `sealed class Screen` — 5개 화면 route 정의:
      ```kotlin
      sealed class Screen(val route: String) {
          object Home : Screen("home")
          object Upload : Screen("upload")
          object InterviewSetup : Screen("interview_setup")
          object Interview : Screen("interview/{sessionId}/{followUpEnabled}")
          object Completion : Screen("completion/{sessionId}")
      }
      ```
    - `AppNavHost.kt`: NavHost + NavGraph, 각 Screen에 Composable 연결
    - Interview 화면으로 sessionId + followUpEnabled 전달 (NavArguments)
    - Completion 화면으로 sessionId 전달
  - **Material3 테마** (`presentation/theme/`):
    - `Color.kt`: 면접 앱에 어울리는 전문적 색상 (네이비/블루 계열 Primary)
    - `Type.kt`: Material3 Typography, 한국어 적합 시스템 폰트
    - `Theme.kt`: `InterviewAppTheme` — LightColorScheme만 (다크모드 MVP 제외)
  - **공통 컴포저블** (`presentation/component/`):
    - `LoadingIndicator.kt`: CircularProgressIndicator + 배경 딤
    - `ErrorSnackbar.kt`: Snackbar 기반 에러 표시

  **Must NOT do**:
  - 다크모드 테마 구현 금지
  - String resource XML 생성 금지
  - 복잡한 커스텀 애니메이션 금지 (기본 Compose 트랜지션만)
  - 6번째 화면 추가 금지 — 5개만

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: UI 테마, 색상, 네비게이션 구조는 프론트엔드 디자인 영역
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 4)
  - **Blocks**: Tasks 9, 10, 11
  - **Blocked By**: Task 2

  **References**:
  - `requirements_en.md` Section 7: Screen Flow (5개 화면)
  - Material3 Navigation: https://developer.android.com/develop/ui/compose/navigation

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 5개 화면 route 빌드 성공
    Tool: Bash
    Preconditions: Task 2 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. Screen.kt에서 sealed class 5개 확인: Home, Upload, InterviewSetup, Interview, Completion
      3. AppNavHost.kt에서 5개 composable() 블록 존재 확인
    Expected Result: 컴파일 성공, 5개 route 정의됨
    Failure Indicators: 컴파일 에러, route 누락
    Evidence: .sisyphus/evidence/task-3-navigation-build.txt

  Scenario: 테마 적용 확인
    Tool: Bash
    Preconditions: 테마 파일 생성됨
    Steps:
      1. `grep -r "InterviewAppTheme" app/src/main/` — MainActivity에 테마 적용 확인
      2. Color.kt에 Primary 색상 정의 확인
    Expected Result: InterviewAppTheme이 MainActivity에 감싸져 있음
    Failure Indicators: 기본 Material 테마 그대로, 커스텀 테마 미적용
    Evidence: .sisyphus/evidence/task-3-theme-applied.txt
  ```

  **Commit**: YES
  - Message: `feat(android): setup navigation graph and Material3 theme`
  - Files: `android/app/src/**/presentation/navigation/`, `**/presentation/theme/`, `**/presentation/component/`
  - Pre-commit: `./gradlew compileDebugKotlin`

- [ ] 4. Domain Models + Repository Interface

  **What to do**:
  - **Domain 모델** (`domain/model/`) — 순수 Kotlin data class, 의존성 없음:
    - `CoverLetter.kt`:
      ```kotlin
      /** 이력서 데이터. text는 직접입력 시, filePath는 파일 선택 시. */
      data class CoverLetter(
          val text: String?,
          val filePath: String?,
          val fileType: CoverLetterType
      )
      enum class CoverLetterType { PDF, DOCX, TEXT }
      ```
    - `InterviewSession.kt`:
      ```kotlin
      /** 서버에서 생성된 면접 세션. */
      data class InterviewSession(
          val sessionId: String,
          val jobField: String,
          val questions: List<Question>
      )
      ```
    - `Question.kt`:
      ```kotlin
      /** 면접 질문. isFollowUp=true이면 꼬리질문. */
      data class Question(
          val questionId: String,
          val content: String,
          val orderIndex: Int
      )
      ```
    - `AnswerResult.kt`:
      ```kotlin
      /** 답변 제출 결과. */
      data class AnswerResult(
          val needsFollowUp: Boolean,
          val followUpQuestion: Question?
      )
      ```
    - `Result.kt` (sealed interface):
      ```kotlin
      sealed interface Result<out T> {
          data class Success<T>(val data: T) : Result<T>
          data class Error(val message: String, val code: String? = null) : Result<Nothing>
      }
      ```
  - **Repository 인터페이스** (`domain/repository/`):
    - `InterviewRepository.kt`:
      ```kotlin
      interface InterviewRepository {
          suspend fun startInterview(coverLetter: CoverLetter, followUpEnabled: Boolean): Result<InterviewSession>
          suspend fun submitAnswer(sessionId: String, questionId: String, answer: String): Result<AnswerResult>
          suspend fun completeInterview(sessionId: String): Result<Unit>
          suspend fun saveSessionLocally(sessionId: String, jobField: String, messages: List<ChatMessage>)
      }
      ```
    - `ChatMessage.kt` (domain model):
      ```kotlin
      enum class MessageType { QUESTION, ANSWER, FOLLOWUP_QUESTION, FOLLOWUP_ANSWER }
      data class ChatMessage(val type: MessageType, val content: String, val questionId: String?)
      ```
  - KDoc on all interfaces and non-obvious fields

  **Must NOT do**:
  - Moshi/@Json 어노테이션 금지 (Domain model != DTO)
  - Room/@Entity 어노테이션 금지 (Domain != DB entity)
  - data 패키지 import 금지

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 단순 data class 생성 + 인터페이스 정의
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: Tasks 5, 7, 8
  - **Blocked By**: Task 2

  **References**:
  - Task 1 API 계약: 응답 구조 → Domain model 필드 매핑
  - `requirements_en.md` Section 3.2-3.3: 질문/꼬리질문 구조

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Domain 모델 컴파일 + data 패키지 의존성 없음
    Tool: Bash
    Preconditions: domain/model/ 파일 생성됨
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. `grep -r "import com.interview.app.data" app/src/main/kotlin/com/interview/app/domain/`
      3. grep 결과 비어있는지 확인
    Expected Result: 컴파일 성공, data 패키지 import 없음
    Failure Indicators: 컴파일 에러, data 패키지 의존성
    Evidence: .sisyphus/evidence/task-4-domain-models-compile.txt

  Scenario: Result sealed interface 동작 확인
    Tool: Bash
    Preconditions: Result.kt 생성됨
    Steps:
      1. Result.kt에 Success와 Error 두 서브클래스 존재 확인
      2. 컴파일 성공 확인
    Expected Result: sealed interface, 2개 서브클래스
    Failure Indicators: kotlin.Result와 충돌, 컴파일 에러
    Evidence: .sisyphus/evidence/task-4-result-type.txt
  ```

  **Commit**: YES
  - Message: `feat(domain): add domain models and repository interface`
  - Files: `android/app/src/**/domain/`
  - Pre-commit: `./gradlew compileDebugKotlin`

- [ ] 5. Retrofit API 클라이언트 + DTOs

  **What to do**:
  - **DTO 클래스** (`data/remote/dto/`) — Moshi 어노테이션 포함:
    - `StartInterviewRequest.kt`: `{ coverLetterText: String?, followUpEnabled: Boolean }`
    - `StartInterviewResponse.kt`: `{ sessionId: String, jobField: String, questions: List<QuestionDto> }`
    - `QuestionDto.kt`: `{ questionId: String, content: String, orderIndex: Int }`
    - `SubmitAnswerRequest.kt`: `{ questionId: String, answer: String }`
    - `SubmitAnswerResponse.kt`: `{ needsFollowUp: Boolean, followUpQuestion: QuestionDto? }`
    - `CompleteInterviewResponse.kt`: `{ sessionId: String, jobField: String, completedAt: String }`
    - `ApiErrorResponse.kt`: `{ error: String, message: String }`
  - **Retrofit 인터페이스** (`data/remote/api/InterviewApi.kt`):
    ```kotlin
    interface InterviewApi {
        @POST("api/interview/start")
        suspend fun startInterview(@Body request: StartInterviewRequest): Response<StartInterviewResponse>

        @Multipart
        @POST("api/interview/start")
        suspend fun startInterviewWithFile(
            @Part file: MultipartBody.Part,
            @Part("followUpEnabled") followUpEnabled: RequestBody
        ): Response<StartInterviewResponse>

        @POST("api/interview/{sessionId}/answer")
        suspend fun submitAnswer(
            @Path("sessionId") sessionId: String,
            @Body request: SubmitAnswerRequest
        ): Response<SubmitAnswerResponse>

        @POST("api/interview/{sessionId}/complete")
        suspend fun completeInterview(@Path("sessionId") sessionId: String): Response<CompleteInterviewResponse>
    }
    ```
  - **Hilt NetworkModule** (`di/NetworkModule.kt`):
    - OkHttpClient: HttpLoggingInterceptor (DEBUG 빌드만)
    - Moshi: KotlinJsonAdapterFactory 포함
    - Retrofit: BASE_URL from BuildConfig
  - **DTO → Domain mapper** (`data/remote/mapper/InterviewMapper.kt`):
    - `StartInterviewResponse.toDomain(): InterviewSession`
    - `SubmitAnswerResponse.toDomain(): AnswerResult`

  **Must NOT do**:
  - Gemini API 키 Android 앱에 하드코딩 금지
  - 인증 토큰 Interceptor 금지
  - Gson 사용 금지 (Moshi 사용)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: API 계약을 Retrofit 인터페이스로 정확히 매핑 + Hilt DI 설정
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 6, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 2, 4

  **References**:
  - Task 1 API 계약: 모든 endpoint 명세 — Retrofit 인터페이스의 근거
  - Task 4 Domain models: mapper의 target 타입
  - `requirements_en.md` Section 4.1: 통신 흐름 다이어그램

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Retrofit 인터페이스 빌드 성공
    Tool: Bash
    Preconditions: Tasks 1, 2, 4 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. InterviewApi.kt에 4개 메서드 존재 확인 (startInterview, startInterviewWithFile, submitAnswer, completeInterview)
    Expected Result: 컴파일 성공, 4개 메서드 존재
    Failure Indicators: 컴파일 에러, 메서드 누락
    Evidence: .sisyphus/evidence/task-5-retrofit-build.txt

  Scenario: DTO mapper 변환 로직 확인
    Tool: Bash
    Preconditions: mapper 파일 생성됨
    Steps:
      1. InterviewMapper.kt에 toDomain() 확장 함수 2개 존재 확인
      2. `./gradlew compileDebugKotlin` 성공
    Expected Result: mapper 컴파일 성공, 2개 확장 함수
    Failure Indicators: 변환 필드 누락, 컴파일 에러
    Evidence: .sisyphus/evidence/task-5-dto-mapper.txt
  ```

  **Commit**: NO (groups with Tasks 6, 7)

- [ ] 6. Room DB 설정

  **What to do**:
  - **Room Entities** (`data/local/entity/`):
    - `InterviewSessionEntity.kt`:
      ```kotlin
      @Entity(tableName = "interview_sessions")
      data class InterviewSessionEntity(
          @PrimaryKey val sessionId: String,
          val jobField: String,
          val followUpEnabled: Boolean,
          val createdAt: Long, // timestamp ms
          val completedAt: Long?
      )
      ```
    - `ChatMessageEntity.kt`:
      ```kotlin
      @Entity(tableName = "chat_messages")
      data class ChatMessageEntity(
          @PrimaryKey(autoGenerate = true) val id: Long = 0,
          val sessionId: String,
          val orderIndex: Int,
          val type: String, // "QUESTION", "ANSWER", "FOLLOWUP_QUESTION", "FOLLOWUP_ANSWER"
          val content: String,
          val questionId: String?
      )
      ```
  - **DAOs** (`data/local/dao/`):
    - `InterviewSessionDao.kt`: `insertSession()`, `getSessionById()`, `getAllSessions()`
    - `ChatMessageDao.kt`: `insertMessage()`, `getMessagesBySessionId()`
  - **Database** (`data/local/InterviewDatabase.kt`):
    - `@Database(entities = [...], version = 1, exportSchema = true)`
    - `@TypeConverters` 불필요 (기본 타입만 사용)
  - **Hilt DatabaseModule** (`di/DatabaseModule.kt`):
    - Room.databaseBuilder 인스턴스 제공

  **Must NOT do**:
  - 히스토리 조회용 복잡한 쿼리 추가 금지 (MVP 범위 외)
  - exportSchema = false 금지 (항상 true)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 단순 Room 설정, 2개 테이블, 기본 CRUD
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 5, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:
  - Task 4 Domain models: Entity 필드 설계 참고
  - `requirements_en.md` Section 5: Data Management (로컬 저장)
  - Room: https://developer.android.com/training/data-storage/room

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Room 스키마 생성 확인
    Tool: Bash
    Preconditions: Task 2 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. Room schema 폴더 확인: `ls app/schemas/`
      3. JSON 스키마 파일에 interview_sessions + chat_messages 테이블 존재 확인
    Expected Result: 컴파일 성공, 2개 테이블 스키마 파일 생성
    Failure Indicators: 컴파일 에러, exportSchema 에러, 테이블 누락
    Evidence: .sisyphus/evidence/task-6-room-schema.txt

  Scenario: DAO 컴파일 성공
    Tool: Bash
    Preconditions: DAO 파일 생성됨
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. InterviewSessionDao, ChatMessageDao에 필수 메서드 존재 확인
    Expected Result: 컴파일 성공, 각 DAO에 최소 2개 메서드
    Failure Indicators: KSP 처리 에러, 메서드 누락
    Evidence: .sisyphus/evidence/task-6-dao-compile.txt
  ```

  **Commit**: NO (groups with Tasks 5, 7)

- [ ] 7. UseCases 구현

  **What to do**:
  - **UseCase 클래스** (`domain/usecase/`):
    - `StartInterviewUseCase.kt`:
      - 입력 검증: CoverLetter.text?.length >= 10 (텍스트 입력 시), 파일 존재 확인 (파일 업로드 시)
      - 검증 실패 시: `Result.Error(message = "이력서 내용이 너무 짧습니다.")`
      - 검증 통과 시: repository.startInterview() 호출 후 Result 반환
    - `SubmitAnswerUseCase.kt`:
      - 입력 검증: answer.isNotBlank()
      - 검증 실패 시: `Result.Error("답변을 입력해주세요.")`
      - 통과 시: repository.submitAnswer() 호출
    - `CompleteInterviewUseCase.kt`:
      - 단순 repository.completeInterview() 호출
      - (검증 없음 — 서버가 검증)
    - `SaveSessionUseCase.kt`:
      - repository.saveSessionLocally() 호출 (Room 저장)
      - 결과 반환 없음 (fire-and-forget, 에러는 로깅만)
  - 각 UseCase: `operator fun invoke(...)` 패턴 + `@Inject constructor` + KDoc

  **Must NOT do**:
  - pass-through UseCase (검증 또는 변환 없이 Repository 그대로 반환) 금지
  - UseCase 내 UI 관련 로직 금지
  - 5개 이상 UseCase 생성 금지

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 비즈니스 규칙(검증 로직)을 포함한 UseCase 설계
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 5, 6)
  - **Blocks**: Task 8
  - **Blocked By**: Tasks 1, 4

  **References**:
  - Task 4 Repository interface: UseCase가 호출할 인터페이스
  - Task 4 Result sealed interface: 반환 타입
  - `requirements_en.md` Section 3.3: 꼬리질문 불충분 기준 5가지 (서버 평가 — UseCase는 관여 않음)

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: UseCase 컴파일 + 구조 검증
    Tool: Bash
    Preconditions: Tasks 4, 5 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. domain/usecase/ 폴더에 4개 파일 존재 확인
      3. 각 UseCase에 `operator fun invoke` 존재 확인
    Expected Result: 컴파일 성공, 4개 UseCase, invoke 패턴
    Failure Indicators: 컴파일 에러, invoke 없음
    Evidence: .sisyphus/evidence/task-7-usecase-compile.txt

  Scenario: StartInterviewUseCase 짧은 텍스트 검증
    Tool: Bash (단위 테스트 형태로 검증, Wave 7에서 정식 테스트 추가)
    Preconditions: UseCase 구현됨
    Steps:
      1. StartInterviewUseCase에 text="안녕" (2자) CoverLetter 전달 코드 확인
      2. Result.Error 반환하는 분기 로직 존재 확인
    Expected Result: 10자 미만 텍스트에 대해 Error 반환 로직 존재
    Failure Indicators: 검증 로직 없음, 무조건 API 호출
    Evidence: .sisyphus/evidence/task-7-usecase-validation.txt
  ```

  **Commit**: YES (groups Tasks 5, 6, 7)
  - Message: `feat(data): add Retrofit API client, DTOs, Room DB, and use cases`
  - Files: `data/remote/`, `data/local/`, `domain/usecase/`, `di/`
  - Pre-commit: `./gradlew compileDebugKotlin`

- [ ] 8. Repository Implementation + Hilt DI

  **What to do**:
  - **`InterviewRepositoryImpl.kt`** (`data/repository/`):
    - `startInterview()`:
      - CoverLetter.fileType = TEXT: `api.startInterview(StartInterviewRequest(coverLetterText, followUpEnabled))`
      - CoverLetter.fileType = PDF/DOCX: `api.startInterviewWithFile(MultipartBody.Part, followUpEnabled)`
      - Response 성공: mapper.toDomain() → `Result.Success`
      - 네트워크 에러 (IOException): `Result.Error("네트워크 연결을 확인해주세요.")`
      - 서버 에러 (4xx/5xx): `Result.Error(apiErrorBody.message, apiErrorBody.error)`
    - `submitAnswer()`: API 호출 + mapper + Result 래핑
    - `completeInterview()`: API 호출 + Result 래핑 (data 없이 Unit)
    - `saveSessionLocally()`: dao.insertSession() + dao.insertMessages()
    - 모든 API 호출: `try/catch IOException` + `response.errorBody()` 파싱
  - **Hilt `RepositoryModule.kt`** (`di/`):
    - `@Binds InterviewRepository → InterviewRepositoryImpl`

  **Must NOT do**:
  - 복잡한 재시도/서킷 브레이커 로직 금지
  - 오프라인 캐시 동기화 로직 금지
  - Room 쿼리로 히스토리 조회 기능 추가 금지

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 여러 계층(Retrofit, Room, Domain)을 연결하는 핵심 클래스. 에러 처리 패턴 중요
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (Wave 4 단독 — Tasks 5, 6, 7 모두 완료 후)
  - **Parallel Group**: Wave 4 (solo)
  - **Blocks**: Tasks 9, 10, 11
  - **Blocked By**: Tasks 4, 5, 6, 7

  **References**:
  - Task 4 Repository interface: 구현해야 할 인터페이스
  - Task 5 Retrofit API + DTOs + Mapper
  - Task 6 Room DAOs
  - Task 7 UseCases: Repository를 호출하는 상위 계층

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 전체 레이어 컴파일 성공
    Tool: Bash
    Preconditions: Tasks 4-7 완료
    Steps:
      1. `./gradlew assembleDebug`
      2. BUILD SUCCESSFUL 확인
    Expected Result: BUILD SUCCESSFUL (전체 레이어 연결됨)
    Failure Indicators: DI 에러, 인터페이스-구현체 타입 불일치
    Evidence: .sisyphus/evidence/task-8-full-build.txt

  Scenario: Hilt 바인딩 확인
    Tool: Bash
    Preconditions: RepositoryModule 생성됨
    Steps:
      1. `grep -r "@Binds" app/src/main/kotlin/com/interview/app/di/`
      2. InterviewRepository → InterviewRepositoryImpl 바인딩 존재 확인
    Expected Result: @Binds 어노테이션으로 인터페이스→구현체 연결됨
    Failure Indicators: @Binds 없음, Hilt DI 에러
    Evidence: .sisyphus/evidence/task-8-hilt-bindings.txt
  ```

  **Commit**: YES
  - Message: `feat(data): add repository implementation with Hilt DI`
  - Files: `data/repository/InterviewRepositoryImpl.kt`, `di/RepositoryModule.kt`
  - Pre-commit: `./gradlew assembleDebug`

- [ ] 9. Home 화면 + HomeViewModel

  **What to do**:
  - **`HomeScreen.kt`** (`presentation/screen/`):
    - 화면 구성: 앱 타이틀("면접 연습"), 설명 문구, "새 면접 시작" 버튼
    - 버튼 클릭 → `navController.navigate(Screen.Upload.route)`
    - 단순 정적 화면 (데이터 로딩 없음)
    - 간단한 일러스트레이션 또는 아이콘 (면접 관련 Material Icon)
  - **`HomeViewModel.kt`** (`presentation/viewmodel/`):
    - `@HiltViewModel`, `@Inject constructor()`
    - 상태 없음 (단순 네비게이션 트리거만)
    - 또는 아예 ViewModel 생략 가능 (화면에 비즈니스 로직 없으면)

  **Must NOT do**:
  - 히스토리 목록 표시 금지
  - 이전 세션 데이터 로딩 금지
  - 복잡한 상태 관리 금지

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 단순 UI 화면 구성
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (with Tasks 10, 11)
  - **Blocks**: Task 15
  - **Blocked By**: Tasks 3, 8

  **References**:
  - Task 3 Navigation: `Screen.Upload.route`로 이동
  - `requirements_en.md` Section 7: Home = "Entry point for cover letter upload"

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Home 화면 빌드 및 컴포저블 확인
    Tool: Bash
    Preconditions: Tasks 3, 8 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. HomeScreen.kt에 @Composable 함수 존재 확인
      3. "새 면접 시작" 텍스트가 버튼에 포함되었는지 확인
    Expected Result: 컴파일 성공, 버튼 텍스트 존재
    Failure Indicators: 컴파일 에러, 버튼 없음
    Evidence: .sisyphus/evidence/task-9-home-screen-build.txt

  Scenario: Home → Upload 네비게이션 연결 확인
    Tool: Bash
    Preconditions: AppNavHost.kt에 HomeScreen 등록됨
    Steps:
      1. AppNavHost.kt에서 HomeScreen이 Upload route로 navigate하는 코드 확인
      2. `./gradlew assembleDebug` 빌드 성공
    Expected Result: 네비게이션 연결됨, 빌드 성공
    Failure Indicators: 빌드 실패, navigate 코드 없음
    Evidence: .sisyphus/evidence/task-9-home-navigation.txt
  ```

  **Commit**: NO (groups with Tasks 10, 11)

- [ ] 10. Upload 화면 + UploadViewModel

  **What to do**:
  - **`UploadScreen.kt`**:
    - 상단 탭 또는 SegmentedButton: "파일 업로드" | "직접 입력"
    - **파일 업로드 탭**:
      - "파일 선택" 버튼 → `ActivityResultContracts.GetContent("*/*")`로 파일 피커 실행
      - 지원 MIME: `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
      - 선택된 파일명 표시 (선택 후)
    - **직접 입력 탭**:
      - `OutlinedTextField` (multiline, 최대 4000자)
      - 글자수 카운터 표시 (`X / 4000`)
      - 힌트 텍스트: "이력서 내용을 입력해주세요. (최소 10자)"
    - "다음" 버튼 → 입력 검증 후 `navController.navigate(Screen.InterviewSetup.route)`
    - 다음으로 넘어갈 때 UploadViewModel에 coverLetter 데이터 저장
  - **`UploadViewModel.kt`**:
    ```kotlin
    @HiltViewModel
    class UploadViewModel @Inject constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(UploadUiState())
        val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

        fun onTextChanged(text: String) { ... }
        fun onFileSelected(uri: Uri) { ... } // 파일 URI 저장
        fun getCoverLetter(): CoverLetter? { ... } // 유효하면 CoverLetter 반환
    }

    data class UploadUiState(
        val selectedTab: Int = 0, // 0=파일, 1=텍스트
        val fileUri: Uri? = null,
        val fileName: String? = null,
        val text: String = "",
        val errorMessage: String? = null
    )
    ```
  - 파일 URI → `CoverLetter(filePath=uri.toString(), fileType=PDF or DOCX)`
  - 텍스트 → `CoverLetter(text=text, fileType=TEXT)`

  **Must NOT do**:
  - Android에서 PDF/DOCX 텍스트 파싱 금지 (PDFBox, POI 등)
  - 파일 내용 미리보기 금지
  - HWP 파일 지원 금지

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 파일 피커 + 텍스트 입력 UI + 탭 구성
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (with Tasks 9, 11)
  - **Blocks**: Task 11 (coverLetter 데이터 전달)
  - **Blocked By**: Tasks 3, 8

  **References**:
  - Task 3 Navigation: `Screen.InterviewSetup.route`
  - Task 4 CoverLetter domain model: 생성 타입
  - `requirements_en.md` Section 3.1: "PDF, Word (.docx), direct text input"
  - ActivityResultContracts.GetContent: https://developer.android.com/training/data-storage/shared/documents-files

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Upload 화면 빌드 + 탭 UI 확인
    Tool: Bash
    Preconditions: Tasks 3, 8 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. UploadScreen.kt에 SegmentedButton 또는 Tab 컴포저블 확인
      3. UploadViewModel.kt에 onTextChanged, onFileSelected 함수 확인
    Expected Result: 컴파일 성공, UI 컴포넌트 존재
    Failure Indicators: 컴파일 에러, 탭 UI 없음
    Evidence: .sisyphus/evidence/task-10-upload-screen-build.txt

  Scenario: 빈 입력으로 다음 버튼 클릭 시 에러 처리
    Tool: Bash
    Preconditions: UploadViewModel 구현됨
    Steps:
      1. UploadViewModel.getCoverLetter()에서 파일도 선택 안 되고 텍스트도 없을 때 null 반환 로직 확인
      2. UploadScreen에서 null 시 errorMessage 표시 로직 확인
    Expected Result: 빈 입력 → errorMessage 상태 업데이트 → 에러 표시
    Failure Indicators: 빈 입력으로 다음 화면 이동, 에러 없음
    Evidence: .sisyphus/evidence/task-10-upload-validation.txt
  ```

  **Commit**: NO (groups with Tasks 9, 11)

- [ ] 11. Interview Setup 화면 + SetupViewModel

  **What to do**:
  - **`InterviewSetupScreen.kt`**:
    - 화면 구성:
      - 안내 텍스트: "면접을 시작합니다"
      - 선택된 이력서 파일명 또는 텍스트 미리보기 (앞 100자)
      - Switch/Toggle: "꼬리질문 사용" (ON/OFF)
      - 꼬리질문 안내: "꼬리질문이 활성화되면 답변이 불충분할 경우 추가 질문을 드립니다."
      - "면접 시작" 버튼 (클릭 시 API 호출)
      - 로딩 상태: LoadingIndicator + "질문을 생성 중입니다..." 텍스트
  - **`SetupViewModel.kt`**:
    ```kotlin
    @HiltViewModel
    class SetupViewModel @Inject constructor(
        private val startInterviewUseCase: StartInterviewUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SetupUiState())
        val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

        fun setFollowUpEnabled(enabled: Boolean) { ... }
        fun startInterview(coverLetter: CoverLetter) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                when (val result = startInterviewUseCase(coverLetter, _uiState.value.followUpEnabled)) {
                    is Result.Success -> _uiState.update { it.copy(session = result.data, isLoading = false) }
                    is Result.Error -> _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
            }
        }
    }
    data class SetupUiState(
        val followUpEnabled: Boolean = true,
        val isLoading: Boolean = false,
        val session: InterviewSession? = null, // non-null이면 Interview 화면으로 이동
        val errorMessage: String? = null
    )
    ```
  - `session != null` 감지 → `navController.navigate(Screen.Interview.createRoute(session.sessionId, followUpEnabled))`
  - "면접 시작" 버튼 클릭 시: `coverLetter`는 이전 화면에서 전달 (SavedStateHandle 또는 공유 ViewModel)

  **Must NOT do**:
  - 직무 선택 화면 추가 금지 (자동 감지)
  - 꼬리질문 세부 기준 화면 표시 금지
  - 버튼 클릭 전 미리 API 호출 금지

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Toggle UI + 로딩 상태 + API 트리거 화면
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Task 10과 병렬 가능, 단 Upload 완성 후 데이터 전달 방식 확인 필요)
  - **Parallel Group**: Wave 5 (with Tasks 9, 10)
  - **Blocks**: Task 12
  - **Blocked By**: Tasks 3, 8, 10

  **References**:
  - Task 3 Navigation: `Screen.Interview` route (sessionId, followUpEnabled 파라미터)
  - Task 7 StartInterviewUseCase: 이 화면에서 호출
  - Task 4 InterviewSession domain model: 결과 타입
  - `requirements_en.md` Section 3.1: "Interview Setup" — follow-up ON/OFF

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Interview Setup 화면 빌드 + Switch UI 확인
    Tool: Bash
    Preconditions: Tasks 3, 8 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. InterviewSetupScreen.kt에 Switch 또는 Toggle 컴포저블 확인
      3. SetupViewModel.kt에 setFollowUpEnabled, startInterview 함수 확인
    Expected Result: 컴파일 성공, Switch + 두 함수 존재
    Failure Indicators: 컴파일 에러, Switch 없음
    Evidence: .sisyphus/evidence/task-11-setup-screen-build.txt

  Scenario: 면접 시작 성공 시 Interview 화면 이동
    Tool: Bash
    Preconditions: SetupViewModel 구현됨
    Steps:
      1. SetupViewModel에서 session != null 시 navigate 이벤트 발생 로직 확인
      2. LaunchedEffect 또는 collectAsState로 session 변화 감지 로직 확인
    Expected Result: session 값 변화 → Interview 화면으로 navigate
    Failure Indicators: navigate 로직 없음
    Evidence: .sisyphus/evidence/task-11-navigate-to-interview.txt
  ```

  **Commit**: YES (groups Tasks 9, 10, 11)
  - Message: `feat(android): add Home, Upload, and Interview Setup screens`
  - Files: `presentation/screen/HomeScreen.kt`, `UploadScreen.kt`, `InterviewSetupScreen.kt`, `presentation/viewmodel/*.kt`
  - Pre-commit: `./gradlew assembleDebug`

- [ ] 12. Interview 화면 + InterviewViewModel

  **What to do**:
  - **`InterviewScreen.kt`** (채팅 UI):
    - 상단: 진행률 표시 ("질문 3/5" 또는 "진행 중...")
    - 중앙: `LazyColumn` — 채팅 메시지 목록
      - 질문 말풍선 (왼쪽 정렬, 배경색 구분): `QuestionBubble`
      - 답변 말풍선 (오른쪽 정렬): `AnswerBubble`
      - 꼬리질문 말풍선 (왼쪽 정렬, 다른 색상): `FollowUpBubble`
    - 하단: 답변 입력 영역
      - `OutlinedTextField` (multiline)
      - "전송" 버튼
      - 로딩 시: 버튼 비활성화 + 로딩 인디케이터
    - 새 메시지 추가 시 자동 스크롤 (`LazyListState.animateScrollToItem`)
  - **`InterviewViewModel.kt`**:
    ```kotlin
    @HiltViewModel
    class InterviewViewModel @Inject constructor(
        private val submitAnswerUseCase: SubmitAnswerUseCase,
        private val completeInterviewUseCase: CompleteInterviewUseCase,
        savedStateHandle: SavedStateHandle
    ) : ViewModel() {
        private val sessionId: String = savedStateHandle["sessionId"] ?: ""
        private val followUpEnabled: Boolean = savedStateHandle["followUpEnabled"] ?: true

        private val _uiState = MutableStateFlow(InterviewUiState())
        val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

        fun initSession(questions: List<Question>) { ... } // 화면 진입 시 질문 초기화
        fun onAnswerChanged(text: String) { ... }
        fun submitAnswer() { ... } // 답변 제출 → 서버 평가 → 꼬리질문 or 다음 질문
        fun onInterviewComplete() { ... } // 모든 질문 완료 → completeInterviewUseCase 호출
    }

    data class InterviewUiState(
        val messages: List<ChatMessage> = emptyList(),
        val currentAnswer: String = "",
        val isLoading: Boolean = false,
        val isComplete: Boolean = false, // true이면 Completion 화면으로 이동
        val questionCount: Int = 0,
        val currentQuestionIndex: Int = 0,
        val errorMessage: String? = null
    )
    ```
  - **면접 진행 로직**:
    1. 화면 진입 시: InterviewSession의 questions로 초기화, 첫 질문을 messages에 추가
    2. 사용자 답변 제출 → ANSWER 메시지 추가 → `submitAnswerUseCase(sessionId, questionId, answer)` 호출
    3. `needsFollowUp == true` AND `followUpEnabled == true`: FOLLOWUP_QUESTION 메시지 추가, 꼬리질문 대기
    4. `needsFollowUp == false` OR `followUpEnabled == false`: 다음 질문 QUESTION 메시지 추가
    5. 모든 질문 완료: `completeInterviewUseCase(sessionId)` → isComplete = true → navigate to Completion
  - 꼬리질문 답변 후 추가 꼬리질문 없음 (depth=1 고정)

  **Must NOT do**:
  - 카드 스와이프 UI 금지 (채팅 UI만)
  - 답변 타이머/카운트다운 금지
  - 평가 점수 표시 금지
  - 꼬리질문 2중 이상 (depth > 1) 금지

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 채팅 UI + 면접 진행 상태 머신 + 꼬리질문 로직이 복합적
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Task 13과 병렬)
  - **Parallel Group**: Wave 6 (with Task 13)
  - **Blocks**: Tasks 13, 15
  - **Blocked By**: Tasks 8, 11

  **References**:
  - Task 7 SubmitAnswerUseCase, CompleteInterviewUseCase
  - Task 4 ChatMessage domain model: 메시지 타입 (QUESTION/ANSWER/FOLLOWUP_QUESTION/FOLLOWUP_ANSWER)
  - Task 3 Navigation: `Screen.Interview` route params (sessionId, followUpEnabled)
  - `requirements_en.md` Section 3.3: 꼬리질문 규칙 (depth=1, 5가지 기준)

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Interview 화면 채팅 UI 빌드 확인
    Tool: Bash
    Preconditions: Tasks 8, 11 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. InterviewScreen.kt에 LazyColumn 존재 확인
      3. QuestionBubble 또는 ChatMessage 렌더링 컴포저블 확인
      4. 답변 TextField + 전송 버튼 확인
    Expected Result: 컴파일 성공, LazyColumn + 말풍선 + 입력 영역
    Failure Indicators: 컴파일 에러, LazyColumn 없음
    Evidence: .sisyphus/evidence/task-12-interview-chat-ui-build.txt

  Scenario: 답변 제출 후 다음 질문 표시 로직 확인
    Tool: Bash
    Preconditions: InterviewViewModel 구현됨
    Steps:
      1. submitAnswer() 함수 내에서 needsFollowUp=false 시 다음 질문을 messages에 추가하는 로직 확인
      2. 마지막 질문 완료 시 isComplete=true 설정 로직 확인
    Expected Result: 답변 후 다음 질문 추가 + 마지막 완료 처리 로직 존재
    Failure Indicators: 상태 전이 로직 없음
    Evidence: .sisyphus/evidence/task-12-interview-state-machine.txt

  Scenario: 꼬리질문 depth=1 강제 확인
    Tool: Bash
    Preconditions: InterviewViewModel 구현됨
    Steps:
      1. submitAnswer() 내에서 현재 처리 중인 질문이 이미 꼬리질문(isFollowUp=true)인 경우
         needsFollowUp 결과와 무관하게 꼬리질문 생성 안 하는 로직 확인
    Expected Result: isFollowUp=true 질문의 답변에는 추가 꼬리질문 없음
    Failure Indicators: depth>1 꼬리질문 생성 가능한 코드
    Evidence: .sisyphus/evidence/task-12-followup-depth.txt
  ```

  **Commit**: NO (groups with Task 13)

- [ ] 13. Completion 화면 + CompletionViewModel

  **What to do**:
  - **`CompletionScreen.kt`**:
    - 상단: "면접 완료!" 헤더 + 감사 메시지
    - 직무 필드 표시: "직무: [jobField]"
    - 전체 Q&A 요약:
      - `LazyColumn`으로 질문-답변 쌍 나열
      - 꼬리질문과 꼬리답변도 포함
    - 하단: "홈으로" 버튼 → Home 화면으로 이동 (백스택 클리어)
  - **`CompletionViewModel.kt`**:
    ```kotlin
    @HiltViewModel
    class CompletionViewModel @Inject constructor(
        private val saveSessionUseCase: SaveSessionUseCase,
        savedStateHandle: SavedStateHandle
    ) : ViewModel() {
        private val sessionId: String = savedStateHandle["sessionId"] ?: ""

        // InterviewViewModel에서 전달받은 messages로 초기화
        fun initWithMessages(sessionId: String, jobField: String, messages: List<ChatMessage>) {
            viewModelScope.launch {
                saveSessionUseCase(sessionId, jobField, messages) // Room에 저장
            }
        }
    }
    ```
  - 화면 진입 시: `InterviewViewModel`에서 messages 데이터를 Completion 화면으로 전달
    - 방법: `NavBackStackEntry` 공유 또는 `SavedStateHandle` + 직렬화 (간단한 경우) 
    - 추천: `Activity`-scoped shared ViewModel (Hilt `@ActivityRetainedScoped`)
  - "홈으로" 버튼: `navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false } }`

  **Must NOT do**:
  - 피드백/점수 표시 금지
  - 공유 버튼 금지 (MVP 범위 외)
  - 재시작 기능 금지 (새 면접은 Home에서 시작)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 데이터 표시 위주의 요약 화면
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Task 12와 병렬 가능)
  - **Parallel Group**: Wave 6 (with Task 12)
  - **Blocks**: Task 15
  - **Blocked By**: Tasks 8, 12

  **References**:
  - Task 12 InterviewViewModel: messages 데이터 전달
  - Task 7 SaveSessionUseCase: Room 저장
  - Task 3 Navigation: Home으로 popUpTo

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: Completion 화면 빌드 확인
    Tool: Bash
    Preconditions: Tasks 8, 12 완료
    Steps:
      1. `./gradlew compileDebugKotlin`
      2. CompletionScreen.kt에 LazyColumn 존재 확인 (Q&A 목록)
      3. "홈으로" 버튼 텍스트 포함 확인
    Expected Result: 컴파일 성공, LazyColumn + 홈으로 버튼
    Failure Indicators: 컴파일 에러
    Evidence: .sisyphus/evidence/task-13-completion-build.txt

  Scenario: Room 저장 트리거 확인
    Tool: Bash
    Preconditions: CompletionViewModel 구현됨
    Steps:
      1. CompletionViewModel.initWithMessages()에서 saveSessionUseCase 호출 로직 확인
      2. `./gradlew assembleDebug` 성공
    Expected Result: 화면 진입 시 Room 저장 로직 존재
    Failure Indicators: saveSessionUseCase 호출 없음
    Evidence: .sisyphus/evidence/task-13-room-save-trigger.txt
  ```

  **Commit**: YES (groups Tasks 12, 13)
  - Message: `feat(android): add Interview and Completion screens`
  - Files: `presentation/screen/InterviewScreen.kt`, `CompletionScreen.kt`, `presentation/viewmodel/*.kt`
  - Pre-commit: `./gradlew assembleDebug`

- [ ] 14. 단위 테스트 (Unit Tests)

  **What to do**:
  - **UseCase 테스트** (`src/test/.../domain/usecase/`):
    - `StartInterviewUseCaseTest.kt`:
      - 텍스트 10자 미만 → `Result.Error` 반환 확인
      - 정상 텍스트 → `repository.startInterview()` 호출 + Result 반환
      - 파일 입력 → `repository.startInterview()` 정상 호출
      - MockK로 `InterviewRepository` mock
    - `SubmitAnswerUseCaseTest.kt`:
      - 빈 답변 → `Result.Error` 반환
      - 정상 답변 → repository 호출
    - `CompleteInterviewUseCaseTest.kt`: 기본 성공 케이스
  - **Repository 테스트** (`src/test/.../data/repository/`):
    - `InterviewRepositoryImplTest.kt`:
      - startInterview 성공 → `Result.Success<InterviewSession>` 반환 + DTO→Domain 매핑 확인
      - 네트워크 에러 (IOException 주입) → `Result.Error("네트워크 연결을 확인해주세요.")` 반환
      - 서버 에러 (503 mock 응답) → `Result.Error` 반환
      - MockK로 `InterviewApi` mock
  - **ViewModel 테스트** (`src/test/.../presentation/viewmodel/`):
    - `SetupViewModelTest.kt`:
      - startInterview 성공 → uiState.session != null
      - startInterview 실패 → uiState.errorMessage 설정
    - `InterviewViewModelTest.kt`:
      - 답변 제출 → needsFollowUp=true이면 FOLLOWUP_QUESTION 메시지 추가
      - 꼬리질문 답변 후 추가 꼬리질문 없음 (depth=1)
  - **Mapper 테스트** (`src/test/.../data/remote/mapper/`):
    - `InterviewMapperTest.kt`: StartInterviewResponse → InterviewSession 매핑 필드 확인

  **Must NOT do**:
  - Integration/E2E 테스트 (실제 네트워크 호출) 이 태스크에 추가 금지
  - Compose UI 테스트 이 태스크에 추가 금지 (단위 테스트만)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: MockK 기반 단위 테스트, 여러 레이어 커버
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 7 (with Task 15)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 5, 6, 7, 8

  **References**:
  - `domain/usecase/` — 테스트 대상 UseCase
  - `data/repository/InterviewRepositoryImpl.kt` — 테스트 대상 Repository
  - `presentation/viewmodel/` — 테스트 대상 ViewModel
  - Coroutines Test: `kotlinx-coroutines-test`, `runTest{}`

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 전체 단위 테스트 통과
    Tool: Bash
    Preconditions: Tasks 5-8 완료
    Steps:
      1. `./gradlew testDebugUnitTest`
      2. 테스트 결과 파일 확인: `app/build/reports/tests/testDebugUnitTest/index.html`
      3. 0 failures 확인
    Expected Result: 모든 테스트 GREEN, 0 failures
    Failure Indicators: 1개 이상 test failure
    Evidence: .sisyphus/evidence/task-14-unit-tests-pass.txt

  Scenario: StartInterviewUseCase 짧은 텍스트 테스트
    Tool: Bash
    Preconditions: 테스트 파일 생성됨
    Steps:
      1. `./gradlew testDebugUnitTest --tests "*StartInterviewUseCaseTest*"`
      2. exit code 0 확인
    Expected Result: 짧은 텍스트 검증 테스트 GREEN
    Failure Indicators: 테스트 실패
    Evidence: .sisyphus/evidence/task-14-usecase-text-validation.txt
  ```

  **Commit**: NO (groups with Task 15)

- [ ] 15. E2E 통합 테스트

  **What to do**:
  - **사전 조건**: 서버(Spring Boot) 로컬에서 실행 중 (`http://localhost:8080` 또는 에뮬레이터 `10.0.2.2:8080`)
  - **전체 플로우 curl 검증** (서버 API 동작 확인):
    1. `POST http://10.0.2.2:8080/api/interview/start` — 이력서 텍스트 + followUpEnabled=true
       - 응답: sessionId + jobField + 5개 questions 확인
    2. `POST http://10.0.2.2:8080/api/interview/{sessionId}/answer` — 첫 번째 questionId + 불충분한 답변
       - 응답: `needsFollowUp: true` + followUpQuestion 존재 확인
    3. `POST http://10.0.2.2:8080/api/interview/{sessionId}/answer` — 꼬리질문 questionId + 답변
       - 응답: `needsFollowUp: false` (depth=1 강제 확인)
    4. `POST http://10.0.2.2:8080/api/interview/{sessionId}/complete`
       - 응답: sessionId + completedAt 확인
  - **Android 앱 빌드 + 서버 연동 확인** (에뮬레이터):
    - `./gradlew assembleDebug` 성공
    - 에뮬레이터에서 APK 설치 + Upload → Setup → Interview → Completion 플로우 직접 실행
    - 각 화면 스크린샷 캡처 (`adb shell screencap` 또는 에뮬레이터 스크린샷)
  - **에러 케이스 검증**:
    - 서버 미실행 상태에서 면접 시작 → 에러 메시지 표시 확인

  **Must NOT do**:
  - 실제 Gemini API 비용을 많이 쓰는 반복 테스트 금지 (최소한으로)
  - 서버 없이 통합 테스트 수행 금지 (E2E는 서버 필요)

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 여러 환경(서버, 에뮬레이터, curl)에서 전체 플로우 검증
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 7 (with Task 14)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 9-13

  **References**:
  - Task 1 API 계약: 검증할 endpoint 명세
  - `requirements_en.md` Section 4.1: 통신 흐름

  **Acceptance Criteria**:

  **QA Scenarios (MANDATORY):**

  ```
  Scenario: 전체 면접 플로우 서버 연동 확인 (curl)
    Tool: Bash (curl)
    Preconditions: 서버 로컬 실행 중 (8080), Gemini API 키 설정
    Steps:
      1. POST /api/interview/start with text cover letter
      2. 응답에서 sessionId, 5개 questions 확인
      3. POST /api/interview/{sessionId}/answer with short answer ("네")
      4. needsFollowUp: true 확인
      5. POST /api/interview/{sessionId}/complete
      6. completedAt 필드 존재 확인
    Expected Result: 전체 플로우 성공, 4xx/5xx 없음
    Failure Indicators: 서버 에러, 응답 필드 누락
    Evidence: .sisyphus/evidence/task-15-e2e-curl.json

  Scenario: Android 앱 빌드 + 기본 구동 확인
    Tool: Bash
    Preconditions: Android SDK, 에뮬레이터 실행
    Steps:
      1. `./gradlew assembleDebug`
      2. `adb install app/build/outputs/apk/debug/app-debug.apk`
      3. `adb shell am start -n com.interview.app/.MainActivity`
    Expected Result: 앱 설치 + 구동 성공 (Home 화면 표시)
    Failure Indicators: 설치 실패, 앱 크래시
    Evidence: .sisyphus/evidence/task-15-app-launch.txt
  ```

  **Commit**: YES (groups Tasks 14, 15)
  - Message: `test(android): add unit tests and E2E integration test`
  - Files: `src/test/`, `src/androidTest/`
  - Pre-commit: `./gradlew testDebugUnitTest`

---

## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
>
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists. For each "Must NOT Have": search codebase for forbidden patterns. Check evidence files exist in .sisyphus/evidence/. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `./gradlew assembleDebug` + `./gradlew testDebugUnitTest`. Review all changed files for: `as Any`, empty catches, `println`/`Log.d` in prod, commented-out code. Check AI slop: excessive comments, over-abstraction, generic names.
  Output: `Build [PASS/FAIL] | Tests [N pass/N fail] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high`
  Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence. Test edge cases: empty cover letter, very short answer, follow-up toggle ON vs OFF. Save to `.sisyphus/evidence/final-qa/`.
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", verify actual diff 1:1. Check "Must NOT do" compliance. Detect cross-task contamination. Flag unaccounted changes.
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | VERDICT`

---

## Commit Strategy

- Wave 1: `docs: define REST API contract for MVP` / `chore(android): init project scaffolding`
- Wave 2: `feat(android): setup navigation and Material3 theme` / `feat(domain): add domain models and repository interfaces`
- Wave 3: `feat(data): add Retrofit API client and DTOs` / `feat(data): add Room database` / `feat(domain): add use cases`
- Wave 4: `feat(data): add repository implementations with Hilt DI`
- Wave 5-6: `feat(android): add [screen] screen and ViewModel`
- Wave 7: `test(android): add unit and integration tests`

---

## Success Criteria

### Verification Commands
```bash
./gradlew assembleDebug     # Expected: BUILD SUCCESSFUL
./gradlew testDebugUnitTest # Expected: N tests, 0 failures
```

### Final Checklist
- [ ] 모든 "Must Have" 구현됨
- [ ] 모든 "Must NOT Have" 부재 확인
- [ ] 전체 테스트 통과
- [ ] 5개 화면 네비게이션 동작
- [ ] 서버 연동 플로우 동작 (Upload → Interview Setup → Interview → Completion)
