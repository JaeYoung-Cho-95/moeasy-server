package com.moeasy.moeasy.dto.quesiton;

import com.moeasy.moeasy.dto.quesiton.request.QuestionsRequestDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class QuestionsDto {

  List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;
  List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;

  public static QuestionsDto from(QuestionsRequestDto dto) {
    return QuestionsDto.builder()
        .multipleChoiceQuestions(dto.getMultipleChoiceQuestions())
        .shortAnswerQuestions(dto.getShortAnswerQuestions())
        .build();
  }
}
