package com.moeasy.moeasy.dto.onboarding.response;

import com.moeasy.moeasy.dto.quesiton.MultipleChoiceIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerIncludeIdQuestionDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
public class QuestionResponseDto {

  private String title;
  private List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;
  private List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;

  @Builder
  public QuestionResponseDto(String title,
      List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions,
      List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions) {
    this.title = title;
    this.multipleChoiceQuestions = multipleChoiceQuestions;
    this.shortAnswerQuestions = shortAnswerQuestions;
  }
}
