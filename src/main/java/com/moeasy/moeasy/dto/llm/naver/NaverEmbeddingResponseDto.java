package com.moeasy.moeasy.dto.llm.naver;

import java.util.List;
import lombok.Data;

@Data
public class NaverEmbeddingResponseDto {

  private Status status;
  private Result result;

  @Data
  public static class Status {

    private String code;
    private String message;
  }

  @Data
  public static class Result {

    // 임베딩 벡터
    private List<Float> embedding;
    // 입력 토큰 수
    private int inputTokens;
  }
}
