package com.moeasy.moeasy.controller.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moeasy.moeasy.dto.onboarding.OnboardingQuestionDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingMakeQuestionRequestDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.dto.quesiton.QuestionResponseDto;
import com.moeasy.moeasy.response.ErrorApiResponseDto;
import com.moeasy.moeasy.response.FailApiResponseDto;
import com.moeasy.moeasy.response.SuccessApiResponseDto;
import com.moeasy.moeasy.response.custom.CustomFailException;
import com.moeasy.moeasy.response.swagger.SwaggerExamples;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.question.MakeQuestionService;
import com.moeasy.moeasy.service.question.OnBoardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
  @PostMapping("/onBoarding")
  public SuccessApiResponseDto<List<OnboardingQuestionDto>> makeOnboardingQuestions(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody OnboardingRequestDto onboardingRequestDto) throws JsonProcessingException {
    return SuccessApiResponseDto.success(
        200,
        "success",
        onBoardingService.makeOnBoardingQuestions(
            onboardingRequestDto)
    );
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
  @PostMapping("/make")
  public ResponseEntity<SuccessApiResponseDto<QuestionResponseDto>> makeQuestions(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    return ResponseEntity.ok()
        .body(SuccessApiResponseDto.success(
            200, "successfully generated the problems",
            makeQuestionService.makeQuestions(onboardingMakeQuestionRequestDto)
        ));
  }

  @ExceptionHandler(CustomFailException.class)
  public ResponseEntity<FailApiResponseDto> handleMakeQuestionsBadRequest(CustomFailException e) {
    return ResponseEntity
        .status(e.getHttpStatus())
        .body(FailApiResponseDto.fail(e.getCode(), e.getMessage()));
  }
}