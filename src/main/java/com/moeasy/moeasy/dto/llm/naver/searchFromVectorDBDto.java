package com.moeasy.moeasy.dto.llm.naver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class searchFromVectorDBDto {

  private Float distance;
  private PayloadDto payload;
}
