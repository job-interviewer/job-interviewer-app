# Learnings — interview-mvp

## [2026-04-24] Session Start
- Spring AI `spring-ai-starter-model-google-genai` 사용 (커스텀 LLM 추상화 불필요)
- PDFBox 한국어 버그(PDFBOX-5350): outputEncoding=UTF-8, sortByPosition=true 필수
- POI 5.x DOCX 한국어 정상 지원
- 파일 업로드: inputStream.copyTo() 패턴 (절대 getBytes() 금지)
- MemoryUsageSetting.setupMixed(50MB) for PDFBox memory control
- Gemini 2.5 Flash 사용, temperature=0.7
- Spring AI BOM 1.0.0은 존재하지 않음 → 1.1.4 사용 (2026-03-26 릴리즈)
- spring-ai-starter-model-google-genai artifact 이름은 올바름
- 1.1.4는 Maven Central에 있으므로 spring milestone repo 불필요
- Android Base URL: debug=http://10.0.2.2:8080 (에뮬레이터→로컬호스트)
- JPA 엔티티는 Kotlin data class가 아닌 open class 사용
- Room exportSchema=true 설정 필요
