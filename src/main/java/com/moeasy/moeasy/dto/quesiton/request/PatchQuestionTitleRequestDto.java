package com.moeasy.moeasy.dto.quesiton.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Schema(name = "설문지 제목 수정에 필요한 데이터", description = "questionId 와 변경할 title")
public class PatchQuestionTitleRequestDto {

  @Schema(description = "questionId", example = "1235")
  private Long id;

  @Schema(description = "수정할 title 제목", example = "비사이드에 대한 설문 결과")
  private String title;
}
