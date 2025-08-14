package com.moeasy.moeasy.dto.onboarding;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingQuestionDto {

  private String question;
  private List<String> answers;
}
