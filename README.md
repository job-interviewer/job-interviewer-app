# job_interviewer

job_interviewer/
├── assets/                 # 이미지, 폰트
├── src/                                                                                           
  │   ├── screens/            # 화면들
  │   │   ├── HomeScreen.tsx                                                                         
  │   │   ├── ResumeScreen.tsx      # 자기소개서 입력                                                
  │   │   ├── CompanyScreen.tsx     # 회사 정보 입력                                                 
  │   │   ├── QuestionScreen.tsx    # 예상 질문 목록
  │   │   ├── InterviewScreen.tsx   # 모의면접 진행
  │   │   └── ResultScreen.tsx      # 결과/피드백
  │   ├── components/         # 재사용 UI 컴포넌트
  │   ├── services/           # AI API 연동
  │   │   └── ai.ts
  │   ├── navigation/         # 화면 이동 설정
  │   ├── hooks/              # 커스텀 훅
  │   ├── types/              # TypeScript 타입 정의
  │   ├── constants/          # 색상, 폰트 등 상수
  │   └── utils/              # 유틸 함수
  ├── App.tsx
  ├── app.json
  └── .gitignore