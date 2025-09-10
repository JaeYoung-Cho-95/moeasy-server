package com.moeasy.moeasy.dto.quesiton.request;

import com.moeasy.moeasy.dto.quesiton.MultipleChoiceIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerIncludeIdQuestionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
@Schema(name = "설문지 저장", description = "사용자가 수정 후 선택한 설문지 제목 및 객관식/주관식 항목")
public class QuestionsRequestDto {

  @Schema(description = "설문지 제목", example = "신메뉴 수박 쥬스에 대한 설문조사")
  private final String title;

  @Schema(description = "객관식 모음", example = "[]")
  private final List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;

  @Schema(description = "주관식 모음", example = "[]")
  private final List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;

  public static QuestionsRequestDto from(String title,
      List<MultipleChoiceIncludeIdQuestionDto> MultiDto,
      List<ShortAnswerIncludeIdQuestionDto> ShortDto
  ) {
    return QuestionsRequestDto.builder()
        .title(title)
        .multipleChoiceQuestions(MultiDto)
        .shortAnswerQuestions(ShortDto)
        .build();
  }
}