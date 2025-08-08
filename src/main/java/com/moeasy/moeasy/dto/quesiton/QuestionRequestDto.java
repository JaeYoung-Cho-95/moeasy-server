package com.moeasy.moeasy.dto.quesiton;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class QuestionRequestDto {
    String title;
    List<MultipleChoiceQuestionDto> multipleChoiceQuestions;
    List<ShortAnswerQuestionDto> shortAnswerQuestions;

    @Builder
    public QuestionRequestDto(String title, List<MultipleChoiceQuestionDto> multipleChoiceQuestions, List<ShortAnswerQuestionDto> shortAnswerQuestions) {
        this.title = title;
        this.multipleChoiceQuestions = multipleChoiceQuestions;
        this.shortAnswerQuestions = shortAnswerQuestions;
    }
}
