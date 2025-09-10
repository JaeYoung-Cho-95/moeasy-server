package com.moeasy.moeasy.controller.question;

import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import com.moeasy.moeasy.config.swagger.SwaggerExamples;
import com.moeasy.moeasy.dto.onboarding.request.OnboardingMakeQuestionRequestDto;
import com.moeasy.moeasy.dto.onboarding.request.OnboardingRequestDto;
import com.moeasy.moeasy.dto.onboarding.response.OnboardingQuestionDto;
import com.moeasy.moeasy.dto.onboarding.response.QuestionResponseDto;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
@Tag(name = "Question", description = "'설문지' 제작 및 저장 관련 API")
@ApiResponses(
    value = {
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
        @ApiResponse(responseCode = "500", description = "서버 에러 발생",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    }
)
public class OnBoardingController {

  private final OnBoardingService onBoardingService;
  private final MakeQuestionService makeQuestionService;

  @Operation(summary = "'설문지' 온보딩 객관식 생성",
      description = "온보딩 1단계, 2단계에서 입력한 값에 맞게 생성된 3~5단계 온보딩 질문을 반환합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @PostMapping("/onBoarding")
  public List<OnboardingQuestionDto> makeOnboardingQuestions(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody OnboardingRequestDto onboardingRequestDto) {
    return onBoardingService.makeOnBoardingQuestions(onboardingRequestDto);
  }


  @Operation(summary = "'설문지' 생성",
      description = "객관식 및 주관식 문제를 랜덤으로 생성하여 반환합니다.",
      security = @SecurityRequirement(name = "jwtAuth")
  )
  @PostMapping("/make")
  public QuestionResponseDto makeQuestions(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    return makeQuestionService.makeQuestions(onboardingMakeQuestionRequestDto);
  }
}