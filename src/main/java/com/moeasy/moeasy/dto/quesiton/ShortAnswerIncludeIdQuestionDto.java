package com.moeasy.moeasy.dto.quesiton;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
@Schema(name = "주관식 데이터", description = "주관식 문항 저장 시 필요한 데이터 모음")
public class ShortAnswerIncludeIdQuestionDto {

  @Schema(description = "전체 설문지에서 해당 문항의 순서", example = "1")
  private final Long id;

  @Schema(description = "수정 가능 여부", example = "False")
  private final Boolean fixFlag;

  @Schema(description = "주관식 질문", example = "가장 불편했던 부분은 무엇인가요?")
  private final String question;

  @Schema(description = "답변 추천 키워드", example = "['맛', '용량', '위생', '가격']")
  private final List<String> keywords;

  public static ShortAnswerIncludeIdQuestionDto from(Long id, Boolean fixFlag, String question,
      List<String> keywords) {
    return ShortAnswerIncludeIdQuestionDto.builder()
        .id(id)
        .fixFlag(fixFlag)
        .question(question)
        .keywords(keywords)
        .build();
  }
}
