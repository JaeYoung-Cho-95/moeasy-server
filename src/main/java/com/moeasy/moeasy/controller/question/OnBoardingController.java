package com.moeasy.moeasy.controller.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moeasy.moeasy.dto.quesiton.*;
import com.moeasy.moeasy.response.ErrorApiResponseDto;
import com.moeasy.moeasy.response.FailApiResponseDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import com.moeasy.moeasy.response.swagger.SwaggerExamples;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.question.OnBoardingService;
import com.moeasy.moeasy.service.question.MakeQuestionService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
@Tag(name = "Question", description = "'설문지' 제작 및 저장 관련 API")
public class OnBoardingController {

    private final OnBoardingService onBoardingService;
    private final MakeQuestionService makeQuestionService;

    @Operation(summary = "'설문지' 온보딩 객관식 생성",
            description = "온보딩 1단계, 2단계에서 입력한 값에 맞게 생성된 3~5단계 온보딩 질문을 반환합니다.",
            security = @SecurityRequirement(name = "jwtAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = SuccessApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.OnBoarding_Questions))),
            @ApiResponse(responseCode = "400", description = "product type 일치 실패",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.ONBOARDING_BAD_REQUEST))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            schema = @Schema(implementation = FailApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생",
                    content = @Content(
                            schema = @Schema(implementation = ErrorApiResponseDto.class),
                            examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    })
    @GetMapping("/onBoarding")
    public ResponseEntity<SuccessApiResponseDto<List<OnboardingQuestionDto>>> testLLM(@AuthenticationPrincipal CustomUserDetails user, @RequestBody OnboardingRequestDto onboardingRequestDto) throws JsonProcessingException {
        List<OnboardingQuestionDto> nextOnBoardingQuestions = onBoardingService.getNextOnBoardingQuestions(onboardingRequestDto);
        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(200, "success", nextOnBoardingQuestions));
    }


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
    public ResponseEntity<SuccessApiResponseDto<QuestionResponseDto>> makeQuestions(@AuthenticationPrincipal CustomUserDetails user, OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {

        List<MultipleChoiceQuestionDto> listMultipleChoicesQuestionDto = makeQuestionService.makeMultipleChoiceQuestions(onboardingMakeQuestionRequestDto);
        List<ShortAnswerQuestionDto> listShortAnswerQuestionDto = makeQuestionService.makeShortAnswerQuestions(onboardingMakeQuestionRequestDto);

        QuestionResponseDto questionResponseDto = QuestionResponseDto.builder()
                .title("sample title")
                .multipleChoiceQuestions(listMultipleChoicesQuestionDto)
                .shortAnswerQuestions(listShortAnswerQuestionDto)
                .build();

        return ResponseEntity.ok()
                .body(SuccessApiResponseDto.success(
                        200, "successfully generated the problems", questionResponseDto
                ));
    }
}