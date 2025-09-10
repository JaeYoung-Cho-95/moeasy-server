package com.moeasy.moeasy.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class OnboardingQuestionAnswerDto {

  @Schema(description = "온보딩 3~5단계 질문", example = "해당 제품에서 추측되는 가장 큰 불편함은 무엇인가요?")
  private String question;
  @Schema(description = "온보딩 3~5단계 답변", example = "가격")
  private String answer;
}
