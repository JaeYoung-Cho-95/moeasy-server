package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
public class MultipleChoiceIncludeIdQuestionDto {
    private Long id;
    private Boolean fixFlag;
    private String question;
    private List<String> choices;
}
