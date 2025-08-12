package com.moeasy.moeasy.dto.quesiton;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortAnswerIncludeIdQuestionDto {
    private Long id;
    private Boolean fixFlag;
    private String question;
    private List<String> keywords;
}