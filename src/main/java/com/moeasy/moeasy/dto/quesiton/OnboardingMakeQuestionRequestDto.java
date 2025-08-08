package com.moeasy.moeasy.dto.quesiton;

import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import lombok.Getter;

import java.util.List;

@Getter
public class OnboardingMakeQuestionRequestDto {
    private ProductType productType;
    private String domain;
    private String purpose;
    private String description;
    private List<OnboardingQuestionAnswerDto> onboardingQuestionAnswers;
}
