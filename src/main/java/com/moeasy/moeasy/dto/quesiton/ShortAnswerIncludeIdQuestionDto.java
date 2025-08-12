package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ShortAnswerIncludeIdQuestionDto {
    private Long id;
    private Boolean fixFlag;
    private String question;
    private List<String> keywords;
}