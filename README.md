# job_interviewer
<br>
job_interviewer/<br>
├── assets/                 # 이미지, 폰트<br>
├── src/<br>
  │   ├── screens/            # 화면들<br>
  │   │   ├── HomeScreen.tsx<br>
  │   │   ├── ResumeScreen.tsx      # 자기소개서 입력<br>
  │   │   ├── CompanyScreen.tsx     # 회사 정보 입력<br>
  │   │   ├── QuestionScreen.tsx    # 예상 질문 목록<br>
  │   │   ├── InterviewScreen.tsx   # 모의면접 진행<br>
  │   │   └── ResultScreen.tsx      # 결과/피드백<br>
  │   ├── components/         # 재사용 UI 컴포넌트<br>
  │   ├── services/           # AI API 연동<br>
  │   │   └── ai.ts<br>
  │   ├── navigation/         # 화면 이동 설정<br>
  │   ├── hooks/              # 커스텀 훅<br>
  │   ├── types/              # TypeScript 타입 정의<br>
  │   ├── constants/          # 색상, 폰트 등 상수<br>
  │   └── utils/              # 유틸 함수<br>
  ├── App.tsx<br>
  ├── app.json<br>
  └── .gitignore<br>
