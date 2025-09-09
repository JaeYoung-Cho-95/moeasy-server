package com.moeasy.moeasy.dto.onboarding.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingQuestionDto {

  @Schema(description = "온보딩 1~3단계 질문", example = "해당 제품의 적정 가격대는 얼마라고 생각하나요?")
  private String question;

  @Schema(description = "위 질문에 해당하는 선택지 내용", example = "[10000원, 30000원, 50000원 ... , 200000원 이상]")
  private List<String> answers;
}
