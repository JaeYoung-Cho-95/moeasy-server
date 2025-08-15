package com.moeasy.moeasy.dto.llm.naver;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyDto {

  private List<String> must_include_one_of;
  private Integer max_options;
}
