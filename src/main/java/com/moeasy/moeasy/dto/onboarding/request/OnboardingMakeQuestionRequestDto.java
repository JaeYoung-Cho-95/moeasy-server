package com.moeasy.moeasy.dto.onboarding.request;

import com.moeasy.moeasy.dto.onboarding.OnboardingQuestionAnswerDto;
import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Getter
@Schema(name = "온보딩 1~5단계 질문/답변 모음", description = "온보딩의 1~5단계에 해당하는 질문/답변 모음입니다.")
public class OnboardingMakeQuestionRequestDto {

  @Schema(description = "칩 첫 번째 질문에 해당하는 값 ", example = "PRODUCT")
  private ProductType productType;

  @Schema(description = "칩 두 번째 질문에 해당하는 값", example = "DIGITAL·IT")
  private String domain;

  @Schema(description = "칩 세 번째 질문에 해당하는 값", example = "사용자 고객 피드백 수집")
  private String purpose;

  @Schema(description = "설문 대상에 대해 제작자가 작성해준 내용", example = "이번에 출시된 신메뉴에 대한 리뷰를 받아보고 싶어!")
  private String description;

  @Schema(description = "온보딩 3~5단계 질문과 정답")
  private List<OnboardingQuestionAnswerDto> onboardingQuestionAnswers;
}