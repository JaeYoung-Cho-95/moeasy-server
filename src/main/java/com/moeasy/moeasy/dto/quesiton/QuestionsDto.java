package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class QuestionsDto {
    List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions;
    List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions;
}
