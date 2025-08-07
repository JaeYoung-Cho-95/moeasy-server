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
}
