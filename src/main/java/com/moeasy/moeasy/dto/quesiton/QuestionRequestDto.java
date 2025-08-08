package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class QuestionRequestDto {
    String title;
    List<MultipleChoiceQuestionDto> multipleChoiceQuestions;
    List<ShortAnswerQuestionDto> shortAnswerQuestions;
}
