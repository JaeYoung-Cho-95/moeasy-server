package com.moeasy.moeasy.dto.llm.naver;

import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewriterRequestDto {

  private ProductType productType;
  private String purpose;
  private String description;

  public static RewriterRequestDto from(OnboardingRequestDto onboardingRequestDto) {
    return RewriterRequestDto.builder()
        .productType(onboardingRequestDto.getProductType())
        .purpose(onboardingRequestDto.getPurpose())
        .description(onboardingRequestDto.getDescription())
        .build();
  }
}