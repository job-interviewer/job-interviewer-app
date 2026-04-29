# REST API 계약 — AI 면접 연습 서버

**Base URL**: `http://10.0.2.2:8080` (Android 에뮬레이터)  
**Content-Type**: `application/json`  
**인증**: 없음

---

## 공통 에러 응답

모든 에러는 동일한 형식으로 반환됩니다:

```json
{
  "error": "ERROR_CODE",
  "message": "한국어 오류 메시지"
}
```

### 에러 코드 표

| 코드 | HTTP | 설명 |
|------|------|------|
| `COVER_LETTER_TOO_SHORT` | 400 | 이력서 텍스트가 너무 짧음 (10자 미만) |
| `UNSUPPORTED_FILE_TYPE` | 400 | PDF/DOCX 이외의 파일 형식 |
| `EMPTY_DOCUMENT` | 400 | 파일에서 텍스트를 추출할 수 없음 (스캔 PDF 등) |
| `SESSION_NOT_FOUND` | 404 | 세션 ID가 존재하지 않음 |
| `SESSION_ALREADY_COMPLETED` | 400 | 이미 완료된 세션에 재완료 요청 |
| `LLM_SERVICE_ERROR` | 503 | Gemini API 호출 실패 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

---

## Endpoints

### 1. 면접 시작

#### 텍스트 이력서

```
POST /api/interview/start
Content-Type: application/json
```

**Request Body:**
```json
{
  "coverLetterText": "3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용하며...",
  "followUpEnabled": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `coverLetterText` | String | 조건부 | 텍스트 직접 입력 (파일 없을 때 필수, 최소 10자) |
| `followUpEnabled` | Boolean | 필수 | 꼬리질문 ON/OFF. `false`이면 서버가 평가 스킵 |

#### 파일 이력서 (PDF/DOCX)

```
POST /api/interview/start
Content-Type: multipart/form-data
```

**Form Fields:**
| 필드 | 타입 | 설명 |
|------|------|------|
| `file` | File | PDF 또는 DOCX 파일 (최대 10MB) |
| `followUpEnabled` | Boolean | 꼬리질문 ON/OFF |

**Response (200 OK):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "jobField": "백엔드 개발",
  "questions": [
    {
      "questionId": "q1-uuid",
      "content": "Spring Boot 프로젝트에서 MSA 환경의 결제 시스템을 구축하셨다고 하셨는데, 구체적으로 어떤 역할을 담당하셨나요?",
      "orderIndex": 1
    },
    {
      "questionId": "q2-uuid",
      "content": "PostgreSQL과 Redis를 함께 사용하셨는데, 캐시 전략을 어떻게 설계하셨나요?",
      "orderIndex": 2
    },
    {
      "questionId": "q3-uuid",
      "content": "대용량 트래픽을 처리하기 위해 메시지 큐를 도입하셨는데, 어떤 문제를 해결하기 위한 것이었나요?",
      "orderIndex": 3
    },
    {
      "questionId": "q4-uuid",
      "content": "팀 내에서 기술적 결정을 내려야 할 때 어떻게 합의를 이끌어내셨나요?",
      "orderIndex": 4
    },
    {
      "questionId": "q5-uuid",
      "content": "앞으로 어떤 기술적 역량을 키우고 싶으신가요?",
      "orderIndex": 5
    }
  ]
}
```

**Errors:** `400 COVER_LETTER_TOO_SHORT`, `400 UNSUPPORTED_FILE_TYPE`, `400 EMPTY_DOCUMENT`, `503 LLM_SERVICE_ERROR`

---

### 2. 답변 제출

```
POST /api/interview/{sessionId}/answer
Content-Type: application/json
```

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `sessionId` | String (UUID) | 면접 시작 시 받은 세션 ID |

**Request Body:**
```json
{
  "questionId": "q1-uuid",
  "answer": "저는 결제 서비스의 백엔드 개발을 담당했으며, Spring Boot로 결제 API를 구현하고 PostgreSQL 트랜잭션 처리를 설계했습니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `questionId` | String | 필수 | 답변할 질문의 ID (기본 질문 또는 꼬리질문 ID) |
| `answer` | String | 필수 | 지원자 답변 텍스트 |

**Response — 꼬리질문 필요 없음 (200 OK):**
```json
{
  "needsFollowUp": false,
  "followUpQuestion": null
}
```

**Response — 꼬리질문 필요 (200 OK):**
```json
{
  "needsFollowUp": true,
  "followUpQuestion": {
    "questionId": "fq1-uuid",
    "content": "결제 API에서 트랜잭션 처리 시 어떤 문제가 있었고, 어떻게 해결하셨나요?",
    "orderIndex": 0
  }
}
```

> **꼬리질문 규칙**: 꼬리질문(followUpQuestion)에 대한 답변은 추가 꼬리질문을 생성하지 않습니다 (depth=1 고정).  
> **followUpEnabled=false**: 서버가 LLM 평가를 건너뛰고 항상 `needsFollowUp: false` 반환.

**Errors:** `404 SESSION_NOT_FOUND`, `503 LLM_SERVICE_ERROR`

---

### 3. 면접 완료

```
POST /api/interview/{sessionId}/complete
Content-Type: application/json
```

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `sessionId` | String (UUID) | 면접 세션 ID |

**Request Body:** 없음 (빈 body 또는 `{}`)

**Response (200 OK):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "jobField": "백엔드 개발",
  "completedAt": "2026-04-29T08:00:00.000Z"
}
```

**Errors:** `404 SESSION_NOT_FOUND`, `400 SESSION_ALREADY_COMPLETED`

---

## 전체 플로우 예시

```
1. POST /api/interview/start
   → sessionId, 5개 questions 수신

2. 질문 1에 답변:
   POST /api/interview/{sessionId}/answer { questionId: "q1", answer: "..." }
   → needsFollowUp: true, followUpQuestion: { questionId: "fq1", ... }

3. 꼬리질문에 답변:
   POST /api/interview/{sessionId}/answer { questionId: "fq1", answer: "..." }
   → needsFollowUp: false (depth=1 강제)

4. 질문 2~5 답변 반복 (각각 꼬리질문 가능)

5. POST /api/interview/{sessionId}/complete
   → completedAt 수신, 면접 종료
```

---

## Android 구현 참고

- **에뮬레이터 Base URL**: `http://10.0.2.2:8080`
- **실제 기기 Base URL**: `http://{서버_IP}:8080`
- **파일 업로드**: Multipart 방식, MIME type 자동 감지 (`application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`)
- **Retry 정책**: LLM 오류(503) 시 1회 재시도 권장
