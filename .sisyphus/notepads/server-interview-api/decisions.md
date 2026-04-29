# Decisions — server-interview-api

## Architectural Decisions
- In-memory session storage: ConcurrentHashMap (no DB = no Docker needed)
- Job field: AI free detection (not predefined list)
- Follow-up depth: 1 (hard limit, enforced in service layer)
- Error format: {error: "ERROR_CODE", message: "한국어 메시지"}
- Multipart OR JSON body for start endpoint (supports both file upload and text input)

## Tech Stack
- Spring Boot 3.x + Kotlin + Gradle Kotlin DSL
- Spring AI: spring-ai-starter-model-google-genai
- PDF: Apache PDFBox 2.0.32
- DOCX: Apache POI 5.2.5
- Test: JUnit 5 + MockK

## Session Model
- sessionId: UUID (server-generated)
- Session lost on server restart (acceptable for MVP)
- No session timeout/cleanup needed (MVP)
