package com.moeasy.moeasy.controller.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.quesiton.*;
import com.moeasy.moeasy.response.ErrorApiResponseDto;
import com.moeasy.moeasy.response.FailApiResponseDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.response.swagger.SwaggerExamples;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.question.MakeQuestionService;
import com.moeasy.moeasy.service.question.QrCodeService;
import com.moeasy.moeasy.service.question.SaveQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
@Tag(name = "Question", description = "'설문지' 제작 및 저장 관련 API")
public class QuestionController {

    private final MakeQuestionService makeQuestionService;
    private final SaveQuestionService saveQuestionService;
    private final QrCodeService qrCodeService;

    @Operation(summary = "'설문지' 생성",
            description = "객관식 및 주관식 문제를 랜덤으로 생성하여 반환합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.MAKE_QUESTION_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생",
                    content = @Content(
                            schema = @Schema(implementation = ErrorApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    })
    @GetMapping("/make")
    public ResponseEntity<SuccessApiResponseDto> makeQuestions(@AuthenticationPrincipal CustomUserDetails user) {

        String title = "샘플 제목입니다.";
        List<MultipleChoiceQuestionDto> multipleChoiceQuestions = makeQuestionService.makeMultipleChoiceQuestions();
        List<ShortAnswerQuestionDto> shortAnswerQuestions = makeQuestionService.makeShortAnswerQuestions();

        QuestionRequestDto questionRequestDto = QuestionRequestDto.builder()
                .title(title)
                .multipleChoiceQuestions(multipleChoiceQuestions)
                .shortAnswerQuestions(shortAnswerQuestions)
                .build();

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(
                        200, "successfully generated the problems", questionRequestDto
                ));
    }

    @Operation(summary = "'설문지' 저장 및 QR코드 생성",
            description = "생성된 설문지를 사용자와 매핑하여 저장하고, 저장된 설문지에 접근할 수 있는 QR코드를 반환합니다.",
            security = @SecurityRequirement(name = "jwtAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 및 QR코드 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.SAVE_QUESTION_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생 (QR코드 생성 실패 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    })
    @PostMapping
    public ResponseEntity<SuccessApiResponseDto> saveQuestions(@AuthenticationPrincipal CustomUserDetails user, @RequestBody QuestionRequestDto questionRequestDto
    ) throws Exception {
        Long id = user.getId();
        Question question = saveQuestionService.saveQuestionsJoinUser(id, questionRequestDto);
        Long questionId = question.getId();
        Map<String, String> data = qrCodeService.getQrCodeS3Url(questionId);

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(201, "success", data));
    }


    @Operation(summary = "QR코드 검증 및 '설문지' 조회",
            description = "QR코드를 통해 전달받은 정보(만료시간, 서명)를 검증하고, 유효한 경우 설문지 데이터를 반환합니다. 이 API는 인증이 필요하지 않습니다.",
            security = {})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR코드 검증 및 설문지 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.MAKE_QUESTION_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "410", description = "만료되었거나 유효하지 않은 QR 코드",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.EXPIRED_QR_CODE_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생 (설문지 데이터 파싱 오류 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    })
    @PostMapping("/verifyQrCode")
    public ResponseEntity<?> verifyExpireWithSignature(@RequestBody VerifyQrCodeDto verifyQrCodeDto) {
        Optional<Question> optionalQuestion = qrCodeService.verifyQrCode(verifyQrCodeDto);
        if (optionalQuestion.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            FailApiResponseDto.fail(
                                    410,
                                    "resource gone"
                            )
                    );
        }

        try {
            Question question = optionalQuestion.get();
            String content = question.getContent();
            ObjectMapper objectMapper = new ObjectMapper();
            QuestionDto questionDto = objectMapper.readValue(content, QuestionDto.class);

            List<MultipleChoiceQuestionDto> multipleChoiceQuestions = questionDto.getMultipleChoiceQuestions();
            List<ShortAnswerQuestionDto> shortAnswerQuestions = questionDto.getShortAnswerQuestions();

            return ResponseEntity.ok()
                    .body(
                            SuccessApiResponseDto.success(
                                    200,
                                    "success",
                                    QuestionRequestDto.builder()
                                            .title(question.getTitle())
                                            .multipleChoiceQuestions(multipleChoiceQuestions)
                                            .shortAnswerQuestions(shortAnswerQuestions)
                                            .build()
                            )
                    );
        } catch (IOException e) {
            log.info(e.getMessage());
            ErrorApiResponseDto.ErrorResponse errorResponse = ErrorApiResponseDto.ErrorResponse.builder()
                    .type("JsonProcessingException")
                    .errorDetail(e.getMessage())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ErrorApiResponseDto.error(500, errorResponse)
                    );

        }
    }
}
