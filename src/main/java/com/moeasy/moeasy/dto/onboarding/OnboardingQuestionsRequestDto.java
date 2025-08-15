package com.moeasy.moeasy.dto.onboarding;

import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class OnboardingQuestionsRequestDto {

  private String purpose;
  private String description;
  private List<InfoItemDto> information;

  public static OnboardingQuestionsRequestDto from(OnboardingRequestDto onboardingRequestDto,
      InfoResponseDto infoResponseDto) {
    return OnboardingQuestionsRequestDto.builder()
        .description(onboardingRequestDto.getDescription())
        .information(infoResponseDto.getOnBoardingItems())
        .build();
  }
}
