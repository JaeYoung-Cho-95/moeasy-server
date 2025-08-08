package com.moeasy.moeasy.dto.quesiton;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
public class QuestionResponseDto {
    private String title;
    private List<MultipleChoiceQuestionDto> multipleChoiceQuestions;
    private List<ShortAnswerQuestionDto> shortAnswerQuestions;

    @Builder
    public QuestionResponseDto(String title, List<MultipleChoiceQuestionDto> multipleChoiceQuestions, List<ShortAnswerQuestionDto> shortAnswerQuestions) {
        this.title = title;
        this.multipleChoiceQuestions = multipleChoiceQuestions;
        this.shortAnswerQuestions = shortAnswerQuestions;
    }
}
