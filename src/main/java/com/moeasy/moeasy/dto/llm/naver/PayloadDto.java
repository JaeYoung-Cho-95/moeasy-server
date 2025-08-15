package com.moeasy.moeasy.dto.llm.naver;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayloadDto {

  private String question;
  private List<String> answers;
  private PolicyDto answer_policy;
}
