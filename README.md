# MoEasy Backend API Server 🚀
2025년 8월 "비사이드 X 네이버 클라우드" 에서 개최한 해커톤 대회에 참석했습니다.<br>
'Moeasy(모이지)' 라는 서비스 의 Spring Boot 기반 API 서버입니다.

- 대회 성적 : 2등
- 성적 확인 : https://bside.notion.site/508-AI-23822020273581a5a612fe592e878a10
- 서비스 url : https://mo-easy.com/welcome
<br>

## 목차 📚
- [프로젝트 소개](#intro)
- [기술 스택](#tech-stack)
- [시스템 아키텍처](#architecture)
- [주요 기능](#features)
- [빠른 시작](#getting-started)
- [디렉터리 구조](#directory-structure)
- [API 규약](#api-conventions)
- [개선 및 추후 계획](#roadmap)
- [라이센스](#license)

<br>

<a id="intro"></a>
## 프로젝트 소개 📌
스타트업·소상공인 등 작은 조직이 의사결정을 위해 설문조사가 필요한 경우가 잦습니다. <br>
하지만 설문지 설계·배포·응답 수집·분석까지의 과정은 현실적으로 부담이 큽니다. <br>
이 프로젝트는 설문 생성부터 공유, 응답 분석까지의 흐름을 단순화·자동화 합니다.<br><br>
-> 이를 통해 누구나 쉽고 빠르게 데이터 기반 결정을 내릴 수 있도록 돕는 경량 설문 플랫폼을 목표로 합니다.
<br></br>

<a id="tech-stack"></a>
## 기술 스택 🛠️
- Language/Build: Java 24, Gradle
- Framework: Spring Boot, Spring Web(WebMVC/WebFlux 필요 시), Spring Security, OAuth2 Client
- Persistence: Spring Data JPA, QueryDSL
- Cloud/Storage: AWS S3, AWS RDS MySQL
- Search/Vector: Milvus
- Docs: springdoc-openapi
- Container: Docker, Docker Compose
- 기타: Lombok, Jackson
<br>

<a id="architecture"></a>
## 시스템 아키텍처 🏗️
<img width="1645" height="622" alt="스크린샷 2025-08-19 오후 5 41 47" src="https://github.com/user-attachments/assets/e6fa6879-0f15-4151-8398-deaf60edc376" />
제한된 예산에서 서비스를 끊지 않기 위해서.. 고가용성(HA) 구성을 최소화했습니다.<br>
불편하시더라도 양해 부탁드리겠습니다.<br>
scheduler 서버는 : https://github.com/JaeYoung-Cho-95/moeasy-scheduler 를 참조해주시면 감사하겠습니다.
<br><br>

<a id="features"></a>
## 주요 기능 ✨
- 인증/인가
    - Kakao OAuth2 로그인만 지원
    - 로그인 성공 시 JWT Access/Refresh Token 발급
- 공유: URL + QR Code
    - 설문 링크(URL)와 QR Code를 통해 손쉬운 공유
    - QR 이미지 생성/조회, 생성된 QR 이미지는 S3에 저장
- 설문지 생성을 위한 정보 획득 질문 생성(온보딩)
    - RAG 구성: NAVER Cloud Studio Embedding + Milvus + HCX-005
    - 온보딩 단계에서 사용자/비즈니스 맥락을 파악하는 질문을 자동 생성
- 설문지 생성
    - HCX-007 단일 모델 기반 자동 문항 생성
    - 온보딩 결과를 반영하여 맞춤형 설문 초안 생성
- 결과지 생성
    - HCX-005 튜닝 모델과 연동하여 결과 요약 리포트 자동 생성
    - 스케줄러가 10분 간격으로 신규 응답 여부를 점검 → 통계 갱신 후 요약 리포트 업데이트
<br>

<a id="getting-started"></a>
## 빠른 시작 ⚡
사전 요구사항:
- Java 24, Gradle 8+, Docker, Docker Compose
- src/main/resources/application.example.yml 에서 값들을 채워줍니다.
```
mv src/main/resources/application.example.yml src/main/resources/application.yml

docker compose up -d

./gradlew bootRun
```
<br>

<a id="directory-structure"></a>
## 핵심 디렉토리 구조 🗂️
```
moeasy
├── MoeasyApplication.java
├── config/               # 공통 설정(Web, Swagger, CORS, Security 등)
│   ├── SwaggerConfig.java
│   └── WebConfig.java
├── controller/           # REST API 엔드포인트
│   ├── member
│   ├── question
│   └── survey
├── domain/               # JPA 엔티티
│   ├── member
│   ├── question
│   └── survey
├── dto/                  # 다양한 DTO 모음
├── jwt/                  # json web token 관련 파일 
├── repository/
│   ├── account/
│   ├── question/
│   └── survey/
├── response/             # 공통 API 응답/예외
│   ├── custom/
│   │   └── CustomFailException.java
│   ├── swagger/
│   │   └── SwaggerExamples.java    # swagger 양식
│   ├── ErrorApiResponseDto.java
│   ├── FailApiResponseDto.java
│   └── SuccessApiResponseDto.java
├── security/             # Security 설정/필터(옵션)
├── service/
│   ├── account/          # 로그인 및 계정
│   ├── aws/              # aws bucket
│   ├── llm/              # naver cloud studio
│   ├── question/         # 설문지 생성 및 공유
│   └── survey/           # 설문의 결과지 생성
└── util/                 # 공용 유틸(옵션)
```
<br>

<a id="api-conventions"></a>
## API 규약 📐
- 인증: Bearer 토큰(JWT), Authorization 헤더 사용
- 에러 포맷: code, message, timestamp, path
- 공통 응답: data, meta(pagination), error
- 페이징: page, size, sort(필드,ASC|DESC)
<br>

<a id="roadmap"></a>
## 개선 및 추후 계획 🧭
- [ ] : 리팩토링 하기
- [ ] : 테스트 코드 넣기
- [ ] : 현재 상태에서 핵심 api 속도 측정하기
- [ ] : 스케쥴러 -> 배치로 변경하기
- [ ] : redis 로 적절히 캐싱하기
- [ ] : 비동기 처리를 통해 2회 이상 요청하는 llm 추론 대기 단축시키기
- [ ] : llm 응답 성능 개선하기
- [ ] : 기능 자체에 대한 
<br>

<a id="license"></a>
## License ⚖️
소스 코드는 PolyForm Noncommercial License 1.0.0 하에 배포됩니다.  
비상업적 용도(학습, 연구, 개인/비영리 프로젝트)에서는 자유롭게 사용/수정/배포할 수 있으나,
상업적 사용(영리 목적의 제품/서비스/조직 내 운영 포함)은 금지됩니다.
