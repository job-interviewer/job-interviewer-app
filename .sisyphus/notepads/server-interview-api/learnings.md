# Learnings — server-interview-api

## Key Constraints (from plan)
- NO database (no JPA, Flyway, PostgreSQL, Docker)
- In-memory only: ConcurrentHashMap
- Gemini API key via env var: GEMINI_API_KEY
- Base URL: localhost:8080
- Korean language responses
- followUpEnabled toggle: must be respected server-side
- depth=1 for follow-up questions: enforced in InterviewService.submitAnswer()

## Known Technical Risks
- PDFBox Korean bug PDFBOX-5350: sortByPosition=true REQUIRED
- InputStream: use copyTo() pattern, NEVER getBytes() (memory explosion risk)
- Spring AI BOM version: must use stable version (1.1.4+, NOT 1.0.0 which doesn't exist)
- PDF memory: MemoryUsageSetting.setupMixed(50MB) to prevent OOM

## Package Structure
- Root: com.interview.server
- Subpackages: controller/, service/, model/, dto/, config/, exception/
- Prompt files: src/main/resources/prompts/

## API Endpoints
1. POST /api/interview/start
2. POST /api/interview/{sessionId}/answer
3. POST /api/interview/{sessionId}/complete
4. GET /api/health (health check)

## [T1-T6 Complete]
- Spring AI BOM version: 1.1.5 (with Spring Boot 3.4.5)
- Spring AI artifact: spring-ai-starter-model-google-genai
- Note: spring-ai-starter-model-google-genai only exists from 1.1.x series, NOT 1.0.x
- ChatClient bean: injected via ChatClient.Builder in ChatClientConfig
- JSON extraction: extractJson() helper strips surrounding text from LLM responses
- InterviewService: startInterview, submitAnswer, completeInterview
- depth=1 enforced: if question.isFollowUp == true -> skip LLM eval, return needsFollowUp=false
- followUpEnabled=false: skip LLM eval entirely
- Health check confirmed working: http://localhost:8080/api/health returns {"status":"ok"}
