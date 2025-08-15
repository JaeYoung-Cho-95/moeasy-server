package com.moeasy.moeasy.dto.llm.naver;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RetrievedItem {

  private String question;
  private List<String> answers;
  private PolicyDto policy;

  public static RetrievedItem from(PayloadDto payload) {
    if (payload == null) {
      return RetrievedItem.builder()
          .question(null)
          .answers(null)
          .policy(null)
          .build();
    }
    return RetrievedItem.builder()
        .question(payload.getQuestion())
        .answers(payload.getAnswers())
        .policy(payload.getAnswer_policy())
        .build();
  }
}
