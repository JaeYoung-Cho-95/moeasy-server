package com.moeasy.moeasy.dto.quesiton;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class QuestionsRequestDto {
    String title;
    List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;
    List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;

    @Builder
    public QuestionsRequestDto(String title, List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions, List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions) {
        this.title = title;
        this.multipleChoiceQuestions = multipleChoiceQuestions;
        this.shortAnswerQuestions = shortAnswerQuestions;
    }
}
