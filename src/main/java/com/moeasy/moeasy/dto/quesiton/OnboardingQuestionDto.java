package com.moeasy.moeasy.dto.quesiton;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OnboardingQuestionDto {
    private String question;
    private List<String> answers;
}
