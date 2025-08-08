package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class QuestionResponseDto {
    String title;
    List<MultipleChoiceQuestionDto> multipleChoiceQuestions;
    List<ShortAnswerQuestionDto> shortAnswerQuestions;
    LocalDateTime createdTime;
    LocalDateTime expirationTime;
    boolean expired;
}
