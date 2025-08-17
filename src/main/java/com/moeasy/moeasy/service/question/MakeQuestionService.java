package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceQuestionDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingMakeQuestionRequestDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingQuestionAnswerDto;
import com.moeasy.moeasy.dto.quesiton.QuestionResponseDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerQuestionDto;
import com.moeasy.moeasy.response.custom.CustomFailException;
import com.moeasy.moeasy.service.llm.NaverCloudStudioService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MakeQuestionService extends NaverCloudStudioService {

  private final ObjectMapper objectMapper;

  public MakeQuestionService(ResourceLoader resourceLoader,
      @Value("${naver.cloud.studio.host}") String host,
      @Value("${naver.cloud.studio.auth-token}") String authToken,
      ObjectMapper objectMapper) {
    super(resourceLoader, host, authToken, objectMapper);
    this.objectMapper = objectMapper;
  }

  public QuestionResponseDto makeQuestions(
      OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    try {
      String title = makeTitle(onboardingMakeQuestionRequestDto);

      List<MultipleChoiceQuestionDto> listMultipleChoicesQuestionDto =
          makeMultipleChoiceQuestions(onboardingMakeQuestionRequestDto);
      List<ShortAnswerQuestionDto> listShortAnswerQuestionDto =
          makeShortAnswerQuestions(onboardingMakeQuestionRequestDto);

      List<String> ageChoices = Arrays.asList("18–24세", "25–34세", "35–44세", "45–54세", "55세 이상");
      MultipleChoiceIncludeIdQuestionDto ageQuestionDto = MultipleChoiceIncludeIdQuestionDto.builder()
          .id(0L)
          .fixFlag(true)
          .question("귀하의 연령대를 선택해 주세요.")
          .choices(ageChoices)
          .build();

      List<String> genderChoices = Arrays.asList("여성", "남성", "해당 없음(논바이너리 등)", "응답 거부");
      MultipleChoiceIncludeIdQuestionDto genderQuestionDto = MultipleChoiceIncludeIdQuestionDto.builder()
          .id(1L)
          .fixFlag(true)
          .question("귀하의 성별을 선택해 주세요.")
          .choices(genderChoices)
          .build();

      List<MultipleChoiceIncludeIdQuestionDto> listMultipleChoiceIncludeIdQuestionDto = new ArrayList<>();
      listMultipleChoiceIncludeIdQuestionDto.add(ageQuestionDto);
      listMultipleChoiceIncludeIdQuestionDto.add(genderQuestionDto);

      int index = 2;
      for (MultipleChoiceQuestionDto multipleChoiceQuestionDto : listMultipleChoicesQuestionDto) {
        listMultipleChoiceIncludeIdQuestionDto.add(
            MultipleChoiceIncludeIdQuestionDto.builder()
                .id((long) index)
                .fixFlag(false)
                .question(multipleChoiceQuestionDto.getQuestion())
                .choices(multipleChoiceQuestionDto.getChoices())
                .build()
        );
        index++;
      }

      List<ShortAnswerIncludeIdQuestionDto> listShortAnswerIncludeIdQuestionsDto = new ArrayList<>();
      for (ShortAnswerQuestionDto shortAnswerQuestionDto : listShortAnswerQuestionDto) {
        listShortAnswerIncludeIdQuestionsDto.add(
            ShortAnswerIncludeIdQuestionDto.builder()
                .id((long) index)
                .fixFlag(false)
                .question(shortAnswerQuestionDto.getQuestion())
                .keywords(shortAnswerQuestionDto.getKeywords())
                .build()
        );
        index++;
      }

      return QuestionResponseDto.builder()
          .title(title)
          .multipleChoiceQuestions(listMultipleChoiceIncludeIdQuestionDto)
          .shortAnswerQuestions(listShortAnswerIncludeIdQuestionsDto)
          .build();

    } catch (RuntimeException e) {
      if (isJsonRelated(e)) {
        throw new CustomFailException(
            HttpStatus.BAD_REQUEST,
            "입력하신 정보가 부족하거나 형식이 올바르지 않습니다. 조금 더 정확히 작성해 주세요."
        );
      }
      throw e;
    }
  }

  private boolean isJsonRelated(Throwable t) {
    while (t != null) {
      String msg = t.getMessage() == null ? "" : t.getMessage().toLowerCase();
      if (t instanceof JsonProcessingException) {
        return true;
      }
      if (msg.contains("json") || msg.contains("파싱")) {
        return true;
      }
      t = t.getCause();
    }
    return false;
  }

  public List<MultipleChoiceQuestionDto> makeMultipleChoiceQuestions(
      OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    String systemPrompt = getPromptWithFilePath("prompts/makeMultipleChoicesQuestionsPrompt.txt");
    String userPrompt = extractUserPrompt(onboardingMakeQuestionRequestDto);
    return chatHcx007(systemPrompt, userPrompt, new TypeReference<>() {
    });
  }

  private String extractUserPrompt(OnboardingMakeQuestionRequestDto dto) {
    Map<String, Object> userJsonPrompt = new LinkedHashMap<>();
    userJsonPrompt.put("프로덕트 유형", dto.getProductType());
    userJsonPrompt.put("산업/도메인 유형", dto.getDomain());
    userJsonPrompt.put("설문 목적", dto.getPurpose());
    userJsonPrompt.put("사용자 부연 설명", dto.getDescription());

    if (dto.getOnboardingQuestionAnswers() != null && !dto.getOnboardingQuestionAnswers()
        .isEmpty()) {
      for (OnboardingQuestionAnswerDto qa : dto.getOnboardingQuestionAnswers()) {
        userJsonPrompt.put(qa.getQuestion(), qa.getAnswer());
      }
    }

    try {
      return objectMapper.writeValueAsString(Collections.singletonList(userJsonPrompt));
    } catch (Exception e) {
      return "[{}]";
    }
  }

  private String extractUserPromptAtTitle(OnboardingMakeQuestionRequestDto dto) {
    Map<String, Object> userJsonPrompt = new LinkedHashMap<>();
    userJsonPrompt.put("프로덕트 유형", dto.getProductType());
    userJsonPrompt.put("설문 목적", dto.getPurpose());
    userJsonPrompt.put("사용자 부연 설명", dto.getDescription());

    try {
      return objectMapper.writeValueAsString(Collections.singletonList(userJsonPrompt));
    } catch (Exception e) {
      return "[{}]";
    }
  }

  public List<ShortAnswerQuestionDto> makeShortAnswerQuestions(
      OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    String systemPrompt = getPromptWithFilePath("prompts/makeShortsAnswerQuestionsPrompt.txt");
    String userPrompt = extractUserPromptAtTitle(onboardingMakeQuestionRequestDto);

    return chat(systemPrompt, userPrompt, new TypeReference<List<ShortAnswerQuestionDto>>() {
    });
  }

  public String makeTitle(OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
    String systemPrompt = getPromptWithFilePath("prompts/makeTitlePrompt.txt");
    String userPrompt = extractUserPrompt(onboardingMakeQuestionRequestDto);

    Map<String, String> result = chat(systemPrompt, userPrompt,
        new TypeReference<Map<String, String>>() {
        });
    return result.get("title");
  }
}