package com.moeasy.moeasy.dto.quesiton;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class PatchQuestionTitleResponseDto {

  @Schema(description = "수정된 설문지의 questionId", example = "1235")
  private Long id;

  @Schema(description = "수정된 title 제목", example = "비사이드에 대한 설문 결과")
  private String title;
}
