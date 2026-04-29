# Decisions — interview-mvp

## [2026-04-24] Session Start
- LLM: Gemini API (Spring AI로 추상화 — 향후 OpenAI/Claude config-only 전환)
- 사용자 식별: 디바이스 UUID (로그인 없음)
- 구독 모델: MVP에서 완전 무료
- 파일 스토리지: 서버 로컬 (향후 S3 마이그레이션)
- 레포 구조: 이 레포 = backend + android + docs 모두 포함
- 꼬리질문: 기본 5문항 외 추가 (최대 10문항, depth=1)
- 면접 완료 후: 평가 없이 Q&A 리뷰만 표시
- 직무 목록: 6개 하드코딩 (SW_DEV, SEMICONDUCTOR, SALES, MARKETING, HR, FINANCE)
- DB: Docker Compose PostgreSQL 15
- 테스트: 구현 후 작성 (TDD 아님)
