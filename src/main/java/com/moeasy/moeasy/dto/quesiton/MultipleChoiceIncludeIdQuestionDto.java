package com.moeasy.moeasy.dto.quesiton;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@Builder
@Schema(name = "객관식 데이터", description = "객관식 문항 저장 시 필요한 데이터 모음")
public class MultipleChoiceIncludeIdQuestionDto {

  @Schema(description = "설문지에서 해당 문항의 순서", example = "1")
  private final Long id;

  @Schema(description = "수정 가능 여부", example = "False")
  private final Boolean fixFlag;

  @Schema(description = "객관식 질문", example = "적절하다고 생각하는 가격은 얼마인가요?")
  private final String question;

  @Schema(description = "객관식 선택지 모음", example = "['2000원 미만', '2000원 ~ 3000원', '3000원 ~ 4000원', '4000원 ~ 5000원']")
  private final List<String> choices;

  public static MultipleChoiceIncludeIdQuestionDto from(Long id, Boolean fixFlag, String question,
      List<String> choices) {
    return MultipleChoiceIncludeIdQuestionDto.builder()
        .id(id)
        .fixFlag(fixFlag)
        .question(question)
        .choices(choices)
        .build();
  }
}
