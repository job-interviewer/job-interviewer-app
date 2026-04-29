# Issues — interview-mvp

## [2026-04-24] Docker 미실행
- Docker Desktop이 실행되지 않음 → T19 E2E 테스트 스킵
- `docker ps` → "failed to connect to the docker API"
- PostgreSQL 컨테이너 기동 불가 → Backend 실제 서버 기동 불가
- **영향**: T19 E2E 테스트 BLOCKED, F3 실제 QA 제한적

## [2026-04-24] 에이전트 타임아웃
- 큰 태스크(T14-T18 합본, T6-T8 합본)가 30분 타임아웃
- **해결**: 화면별 개별 태스크로 분할 발송
