package com.moeasy.moeasy.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurveyGetRequestDto {
    @Schema(description = "설문이 연결된 surveyId", example = "1351")
    private Long surveyId;
}