package com.moeasy.moeasy.controller.survey;

import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import com.moeasy.moeasy.config.response.responseDto.FailResponseDto;
import com.moeasy.moeasy.config.response.responseDto.SuccessResponseDto;
import com.moeasy.moeasy.config.swagger.SwaggerExamples;
import com.moeasy.moeasy.dto.survey.SurveySaveRequestDto;
import com.moeasy.moeasy.service.survey.GetSurveyService;
import com.moeasy.moeasy.service.survey.SaveSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "Survey", description = "'설문지' 응답 관련 API")
@RequiredArgsConstructor
public class SurveyController {

  private final SaveSurveyService saveSurveyService;
  private final GetSurveyService getSurveyService;

  @Operation(
      summary = "설문 결과 조회",
      description = "설문 결과를 반환합니다. 현재는 access token 을 받지 않습니다.",
      security = {}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "집계 성공",
          content = @Content(
              schema = @Schema(implementation = SuccessResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SURVEY_RESULT_SUCCESS_EXAMPLE))),
      @ApiResponse(responseCode = "404", description = "설문 리소스 없음",
          content = @Content(
              schema = @Schema(implementation = FailResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SURVEY_NOT_FOUND_EXAMPLE))),
      @ApiResponse(responseCode = "500", description = "서버 에러(결과 JSON 파싱/직렬화 실패 등)",
          content = @Content(
              schema = @Schema(implementation = ErrorResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
  })
  @GetMapping("/survey")
  public SuccessResponseDto saveSurvey(
      @Parameter(description = "조회할 설문 ID", example = "123") @RequestParam("surveyId") Long surveyId) {
    return SuccessResponseDto.success(200, "성공적으로 결과지 조회를 완료했습니다.",
        getSurveyService.getSurveyAsJson(surveyId));
  }

  @Operation(
      summary = "설문 응답 저장/집계",
      description = "설문 응답을 집계 템플릿(JSON)에 반영합니다. 인증이 필요하지 않습니다.",
      security = {}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "집계 성공",
          content = @Content(
              schema = @Schema(implementation = SuccessResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SURVEY_SAVE_SUCCESS_EXAMPLE))),
      @ApiResponse(responseCode = "404", description = "설문 리소스 없음",
          content = @Content(
              schema = @Schema(implementation = FailResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.SURVEY_NOT_FOUND_EXAMPLE))),
      @ApiResponse(responseCode = "500", description = "서버 에러(결과 JSON 파싱/직렬화 실패 등)",
          content = @Content(
              schema = @Schema(implementation = ErrorResponseDto.class),
              examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
  })
  @PostMapping("/survey")
  public SuccessResponseDto saveSurvey(@RequestBody SurveySaveRequestDto surveySaveRequestDto) {
    return SuccessResponseDto.success(200, "성공적으로 저장하였습니다.",
        saveSurveyService.update(surveySaveRequestDto));
  }
}
