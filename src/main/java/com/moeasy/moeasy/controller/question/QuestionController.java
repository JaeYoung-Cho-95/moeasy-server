package com.moeasy.moeasy.controller.question;

import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import com.moeasy.moeasy.config.response.responseDto.SuccessResponseDto;
import com.moeasy.moeasy.config.swagger.SwaggerExamples;
import com.moeasy.moeasy.dto.onboarding.response.QrCodeResponseDto;
import com.moeasy.moeasy.dto.quesiton.PatchQuestionTitleDto;
import com.moeasy.moeasy.dto.quesiton.PatchQuestionTitleResponseDto;
import com.moeasy.moeasy.dto.quesiton.VerifyQrCodeDto;
import com.moeasy.moeasy.dto.quesiton.request.QuestionsRequestDto;
import com.moeasy.moeasy.dto.quesiton.response.QuestionListResponseDto;
import com.moeasy.moeasy.dto.quesiton.response.QuestionsResponseDto;
import com.moeasy.moeasy.service.account.CustomUserDetails;
import com.moeasy.moeasy.service.aws.AwsService;
import com.moeasy.moeasy.service.question.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("questions")
@Tag(name = "Question", description = "'설문지' 제작 및 저장 관련 API")
@ApiResponses(
    value = {
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = SwaggerExamples.INVALID_ACCESS_TOKEN_EXAMPLE))),
        @ApiResponse(responseCode = "500", description = "서버 에러 발생 (QR코드 생성 실패 등)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(value = SwaggerExamples.INTERNAL_SERVER_ERROR_EXAMPLE)))
    }
)
public class QuestionController {

  private final QuestionService questionService;
  private final AwsService awsService;

  @Operation(summary = "'설문지' 저장 및 QR코드 생성",
      description = "생성된 설문지를 사용자와 매핑하여 저장하고, 저장된 설문지에 접근할 수 있는 QR코드를 반환합니다.",
      security = @SecurityRequirement(name = "jwtAuth"))
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public QrCodeResponseDto saveQuestions(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody QuestionsRequestDto dto) {
    return questionService.saveQuestionsANdGetQrCodeS3Url(user, dto);
  }


  @Operation(summary = "QR코드 검증 및 '설문지' 조회",
      description = "QR코드를 통해 전달받은 정보(만료시간, 서명)를 검증하고, 유효한 경우 설문지 데이터를 반환합니다. 이 API는 인증이 필요하지 않습니다.",
      security = {})
  @PostMapping("/verifyQrCode")
  public QuestionsResponseDto verifyExpireWithSignature(@RequestBody VerifyQrCodeDto dto) {
    return questionService.verifyQrCode(dto);
  }


  @Operation(summary = "생성한 설문지 list 일괄 조회",
      description = "accesstoken 을 header 에 담아 전달해주면 해당 유저의 설문지 리스트를 반환합니다.",
      security = {})
  @GetMapping
  public List<QuestionListResponseDto> getQuestions(
      @AuthenticationPrincipal CustomUserDetails user) {
    return questionService.findQuestionListByUser(user);
  }

  @Operation(summary = "설문지 제목 수정",
      description = "accesstoken 을 header / questionId, title 을 request body 에 담아주면 update 후 반홥합니다.",
      security = @SecurityRequirement(name = "jwtAuth"))
  @PatchMapping
  public SuccessResponseDto<PatchQuestionTitleResponseDto> patchQuestion(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody PatchQuestionTitleDto patchQuestionDto) {
    return SuccessResponseDto.success(
        200,
        "success update title",
        questionService.updateQuestionTitle(patchQuestionDto)
    );
  }
}
