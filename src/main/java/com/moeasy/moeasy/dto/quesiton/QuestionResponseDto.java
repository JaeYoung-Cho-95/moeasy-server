package com.moeasy.moeasy.dto.quesiton;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
public class QuestionResponseDto {
    private String title;
    private List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;
    private List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;

    @Builder
    public QuestionResponseDto(String title, List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions, List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions) {
        this.title = title;
        this.multipleChoiceQuestions = multipleChoiceQuestions;
        this.shortAnswerQuestions = shortAnswerQuestions;
    }
}
