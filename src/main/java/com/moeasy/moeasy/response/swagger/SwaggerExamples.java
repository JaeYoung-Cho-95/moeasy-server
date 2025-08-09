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
              "data": null,
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

}
