package com.moeasy.moeasy.response.swagger;

public class SwaggerExamples {

  public static final String INVALID_KAKAO_TOKEN_EXAMPLE = """
      {
          "status": "fail",
          "code": 401,
          "message": "유효하지 않은 카카오 액세스 토큰입니다.",
          "timestamp": "2025-08-06T09:30:00.123Z"
      }
      """;

  public static final String EXPIRED_QR_CODE_EXAMPLE = """
      {
          "status": "fail",
          "code": 410,
          "message": "만료되었거나 유효하지 않은 QR 코드입니다.",
          "timestamp": "2025-08-06T10:00:00.000Z"
      }
      """;

  public static final String INTERNAL_SERVER_ERROR_EXAMPLE = """
      {
        "status": "error",
        "code": 500,
        "error": {
          "type": "Exception",
          "errorDetail": "server error"
        },
        "timestamp": "2025-08-06T04:22:21.425Z"
      }
      """;

  public static final String REISSUE_SUCCESS_EXAMPLE = """
      {
          "status": "success",
          "code": 200,
          "message": "토큰이 성공적으로 갱신되었습니다.",
          "data": {
              "accessToken": "new_access_token_example.eyJzdWI...",
              "refreshToken": "new_refresh_token_example.eyJzdWI..."
          },
          "timestamp": "2025-08-06T11:00:00.000Z"
      }
      """;

  public static final String LOGOUT_SUCCESS_EXAMPLE = """
      {
          "status": "success",
          "code": 200,
          "message": "로그아웃 되었습니다.",
          "data": null,
          "timestamp": "2025-08-06T11:05:00.000Z"
      }
      """;

  public static final String SUCCESS_LOGIN_EXAMPLE = """
      {
          "status": "success",
          "code": 200,
          "message": "success",
          "data": {
              "email": "whwo9745@naver.com",
              "name": "조재영",
              "profileUrl": "https://moeasy-profile.s3.ap-northeast-2.amazonaws.com/152/profile.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250809T065205Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIATQZCSEKJTREXNVOB%2F20250809%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=0513f418ac9752714c9a7a293d76d9ef17da0a83924396303cd387703e5ba5d3"
          },
          "timestamp": "2025-08-09T15:52:05.824034"
      }
      """;

  public static final String INVALID_REFRESH_TOKEN_EXAMPLE = """
      {
          "status": "fail",
          "code": 401,
          "message": "Refresh Token 정보가 일치하지 않습니다.",
          "timestamp": "2025-08-06T11:10:00.000Z"
      }
      """;

  public static final String INVALID_ACCESS_TOKEN_EXAMPLE = """
      {
          "status": "fail",
          "code": 401,
          "message": "Access Token이 유효하지 않습니다.",
          "timestamp": "2025-08-06T11:15:00.000Z"
      }
      """;

  public static final String MAKE_QUESTION_SUCCESS_EXAMPLE = """
      {
        "status": "success",
        "code": 200,
        "message": "successfully generated the problems",
        "data": {
          "title": "샘플 데이터 입니다."
          "multipleChoiceQuestions": [
            {
              "id": 0,
              "fixFlag": true,
              "question": "다음 중 HTTP 메서드가 아닌 것은?",
              "choices": [
                "GET",
                "POST",
                "DELETE",
                "UPDATE"
              ]
            }
          ],
          "shortAnswerQuestions": [
            {
              "id": 1,
              "fixFlag": true,
              "question": "OSI 7계층에 대해 설명하시오.",
              "keywords": [
                "물리 계층",
                "데이터 링크 계층",
                "네트워크 계층",
                "전송 계층",
                "세션 계층",
                "표현 계층",
                "응용 계층"
              ]
            }
          ]
        },
        "timestamp": "2025-08-06T12:00:00.000Z"
      }
      """;

  public static final String SAVE_QUESTION_SUCCESS_EXAMPLE = """
      {
        "status": "success",
        "code": 201,
        "message": "success",
        "data": {
          "qrCode": "https://your-s3-bucket.s3.amazonaws.com/qr-codes/some-question-id.png",
          "url": "https://mo-easy.com/question/252?expires=1545531571960&signature=WbpGdi-VUy54zcxnlkdaWQEdasxuomOf0ZF4IMQQg7gk"
        },
        "timestamp": "2025-08-06T12:05:00.000Z"
      }
      """;

  public static final String OnBoarding_Questions = """
      {
        "status": "success",
        "code": 200,
        "message": "success",
        "data": [
          {
            "question": "현재 제품은 어떤 상태인가요?",
            "answers": [
              "이미 판매 중",
              "출시 예정(1개월 이내)",
              "출시 예정(3개월 이내)",
              "테스트/시범 운영 중",
              "판매 종료 예정"
            ]
          },
          {
            "question": "어디서 제품을 구매할 수 있나요?",
            "answers": [
              "오프라인 매장",
              "온라인 쇼핑몰",
              "배달앱/주문앱",
              "행사/팝업",
              "기타"
            ]
          },
          {
            "question": "이 제품의 가격대는 어떻게 되나요?",
            "answers": [
              "5,000원 미만",
              "5,000원 ~ 9,900원",
              "10,000원 ~ 19,900원",
              "20,000원 이상",
              "변동가(옵션별 상이)"
            ]
          }
        ],
        "timestamp": "2025-08-08T17:44:25.89016"
      }
      """;

  public static final String ONBOARDING_BAD_REQUEST = """
      {
        "status": "fail",
        "code": 400,
        "message": "요청 형식이 올바르지 않습니다.",
        "timestamp": "2025-08-08T17:47:06.468638"
      }
      """;

  public static final String QUESTION_LIST_SUCCESS = """
      {
        "status": "success",
        "code": 200,
        "message": "success",
        "data": [
          {
            "id": 702,
            "title": "third questions",
            "createdTime": "2025-08-09T17:32:17.780815",
            "expiredTime": "2025-08-16T17:32:17.780815",
            "expired": false,
            "url": "https://mo-easy.com/question/702?expires=1755333137796&signature=d67uOMAEUHcfUEEOeu6selSVDpHYl-oqQYcHpWSmkIU",
            "qrCode": "https://moeasy-qr-images.s3.ap-northeast-2.amazonaws.com/702/qr_code.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250809T083225Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIATQZCSEKJTREXNVOB%2F20250809%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=71cb7c044c1efcf2714d7537a13afbcc2ee76ccac2d16962a4ea399436166837",
            "count": 0
          },
          {
            "id": 552,
            "title": "string",
            "createdTime": "2025-08-09T16:11:25.089992",
            "expiredTime": null,
            "expired": true,
            "url": https://mo-easy.com/question/702?expires=1755333137796&signature=d67uOMAEUHcfUEEOeu6selSVDpHYl-oqQYcHpWSmkIU,
            "qrCode": "https://moeasy-qr-images.s3.ap-northeast-2.amazonaws.com/552/qr_code.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250809T083225Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIATQZCSEKJTREXNVOB%2F20250809%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=3600&X-Amz-Signature=3309670b8c00b0066ce799799c0e25e0c693c7c805fca349cb8ae1031f2b1269",
            "count": 0
          }
        ],
        "timestamp": "2025-08-09T17:32:25.30336"
      }
      """;

  public static final String SURVEY_SAVE_SUCCESS_EXAMPLE = """
      {
        "status": "success",
        "code": 200,
        "message": "success",
        "data": {
          "surveyId" : 123
          "surveyUrl" : "https://mo-easy.com/report/{surveyId}"
          },
        "timestamp": "2025-08-10T12:00:00.000Z"
      }
      """;

  public static final String SURVEY_NOT_FOUND_EXAMPLE = """
      {
        "status": "fail",
        "code": 404,
        "message": "Id : 702를 통해 조회되는 survey 가 없습니다.",
        "timestamp": "2025-08-10T12:00:00.000Z"
      }
      """;

  public static final String SURVEY_RESULT_SUCCESS_EXAMPLE = """
      {
        "status": "success",
        "code": 200,
        "message": "success",
        "data": {
                  "graphs": [
                    {
                      "type": "bar",
                      "contents": {
                        "18–24세": 0,
                        "25–34세": 8,
                        "35–44세": 24,
                        "45–54세": 0,
                        "55세 이상": 0
                      }
                    },
                    {
                      "type": "circle",
                      "contents": {
                        "남성": 14,
                        "여성": 18,
                        "응답 거부": 0,
                        "해당 없음(논바이너리 등)": 0
                      }
                    }
                  ],
                  "content": "네이버 클라우드는 AI 및 머신러닝 서비스 사용이 많고, 빠른 배포 속도와 메세징 시스템 편의성이 장점이지만 고객 지원 응답 시간 단축과 메뉴 단순화가 필요해요",
                  "subject": "네이버 클라우드 PRODUCT 편의성 설문",
                  "keywords": [
                    "#빠른배포",
                    "#사용편의성_LLM",
                    "#메세징시스템_편리",
                    "#고객지원지연",
                    "#메뉴복잡"
                  ],
                  "sentences": [
                    {
                      "title": "연령대는 35–44세가 다수입니다.",
                      "comment": "{73%}의 응답자가 {35–44세}에 응답했어요. 설문 참여자의 주요 연령대가 젊은 층임을 시사합니다.",
                      "percent": 73
                    },
                    {
                      "title": "인지 경로는 검색 엔진이 압도적입니다.",
                      "comment": "{97%}의 응답자가 {검색 엔진}에 응답했어요. 구글이나 네이버를 통해 접근하는 경우가 대부분이라는 점을 강조합니다.",
                      "percent": 97
                    },
                    {
                      "title": "AI 서비스가 주력 활용 분야입니다.",
                      "comment": "{97%}의 응답자가 {AI 및 머신러닝 서비스}에 응답했어요. 기술적 기능 활용도가 높음을 보여주는 지표입니다.",
                      "percent": 97
                    },
                    {
                      "title": "인터페이스는 약간 편리한 편입니다.",
                      "comment": "{67%}의 응답자가 {약간 편리함}에 응답했어요. 사용자 친화성에 대한 중간 평가를 나타내며 개선 여지가 있음을 암시합니다.",
                      "percent": 67
                    },
                    {
                      "title": "안정성 및 보안이 최우선 고려사항입니다.",
                      "comment": "{97%}의 응답자가 {서비스 안정성 및 보안}에 응답했어요. 신뢰성과 안전성을 가장 중요시하는 소비자 심리를 반영합니다.",
                      "percent": 97
                    },
                    {
                      "title": "가격 정책 개선 요구가 두드러집니다.",
                      "comment": "{73%}의 응답자가 {가격 정책 및 요금제}에 응답했어요. 비용 효율성에 대한 부정적 인식이 상대적으로 높게 나타났습니다.",
                      "percent": 73
                    }
                  ],
                  "totalCount": 33
                },
        "timestamp": "2025-08-10T12:00:00.000Z"
      }
      """;

  public static final String QUESTION_UPDATE_TITLE = """
      {
        "status": "success",
        "code": 200,
        "message": "success",
        "data": {
          "id" : 1235,
          "title": "new title 입니다."
        },
        "timestamp": "2025-08-09T17:32:25.30336"
      }
      """;
}



