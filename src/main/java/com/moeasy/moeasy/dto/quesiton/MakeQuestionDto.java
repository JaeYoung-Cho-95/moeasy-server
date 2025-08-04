package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MakeQuestionDto {
    List<Map<String, List<String>>> multipleChoiceQuestions;
    List<Map<String, List<String>>> shortAnswerQuestions;
}
