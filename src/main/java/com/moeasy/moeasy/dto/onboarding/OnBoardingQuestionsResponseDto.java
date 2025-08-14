package com.moeasy.moeasy.dto.onboarding;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnBoardingQuestionsResponseDto {

  private List<OnboardingQuestionDto> questions;

  public static OnBoardingQuestionsResponseDto from(
      List<OnboardingQuestionDto> onboardingQuestionDtoList) {
    return OnBoardingQuestionsResponseDto.builder()
        .questions(onboardingQuestionDtoList)
        .build();
  }
}
