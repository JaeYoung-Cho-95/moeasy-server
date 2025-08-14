package com.moeasy.moeasy.dto.onboarding;

import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class OnboardingQuestionsRequestDto {

  private ProductType productType;
  private String domain;
  private String purpose;
  private String description;
  private List<InfoItemDto> information;

  public static OnboardingQuestionsRequestDto from(OnboardingRequestDto onboardingRequestDto,
      InfoResponseDto infoResponseDto) {
    return OnboardingQuestionsRequestDto.builder()
        .productType(onboardingRequestDto.getProductType())
        .domain(onboardingRequestDto.getDomain())
        .description(onboardingRequestDto.getDescription())
        .information(infoResponseDto.getOnBoardingItems())
        .build();
  }
}
