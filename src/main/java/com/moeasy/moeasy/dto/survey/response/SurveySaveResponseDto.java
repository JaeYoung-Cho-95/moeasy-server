package com.moeasy.moeasy.dto.survey.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
@Schema(name = "설문지 응답자가 작성 후 저장 요청")
public class SurveySaveResponseDto {

  @Schema(description = "설문 결과지의 id 값", example = "316")
  private final String surveyId;

  @Schema(description = "설문 경과지를 조회할 수 있는 url", example = "https://mo-easy.com/reporting/356")
  private final String surveyUrl;

  public static SurveySaveResponseDto from(String id, String url) {
    return SurveySaveResponseDto.builder()
        .surveyId(id)
        .surveyUrl(url)
        .build();
  }
}
