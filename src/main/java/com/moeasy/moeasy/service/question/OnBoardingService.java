package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.onboarding.InfoResponseDto;
import com.moeasy.moeasy.dto.onboarding.OnBoardingQuestionsResponseDto;
import com.moeasy.moeasy.dto.onboarding.OnboardingQuestionsRequestDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.service.llm.NaverCloudStudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class OnBoardingService extends NaverCloudStudioService {

  private final ObjectMapper objectMapper;

  public OnBoardingService(ResourceLoader resourceLoader,
      @Value("${naver.cloud.studio.host}") String host,
      @Value("${naver.cloud.studio.auth-token}") String authToken,
      ObjectMapper objectMapper) {
    super(resourceLoader, host, authToken, objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * onboarding 1~3단계 사용자 입력값을 통해 4~6단계 질문을 llm 을 통해 생성
   */
  public OnBoardingQuestionsResponseDto makeOnBoardingQuestions(
      OnboardingRequestDto onboardingRequestDto)
      throws JsonProcessingException {
    InfoResponseDto infoResponseDto = requestInfo(onboardingRequestDto);
    OnboardingQuestionsRequestDto requestOnboardingQuestionsDto = makeRequestOnboardingQuestionsDto(
        onboardingRequestDto, infoResponseDto);
    return requestOnboardingQuestions(requestOnboardingQuestionsDto);
  }

  /**
   * 어떤 정보가 추가적으로 필요한지 llm 에게 묻기
   */
  private InfoResponseDto requestInfo(OnboardingRequestDto onboardingRequestDto)
      throws JsonProcessingException {
    String systemPrompt = getPromptWithFilePath("prompts/onboardingRequestInfo.txt");
    String userPrompt = objectMapper.writeValueAsString(onboardingRequestDto);

    return chat(systemPrompt, userPrompt, new TypeReference<InfoResponseDto>() {
    });
  }

  /**
   * 1~3단계 사용자 입력값 + 추가적으로 필요한 정보들을 dto 로 정리
   */
  private OnboardingQuestionsRequestDto makeRequestOnboardingQuestionsDto(
      OnboardingRequestDto onboardingRequestDto, InfoResponseDto infoResponseDto) {
    return OnboardingQuestionsRequestDto.from(onboardingRequestDto, infoResponseDto);
  }

  /**
   * 1~3단계 사용자 입력값 + 추가적으로 필요한 정보들을 합친 dto 로 4~6단계 정보를 llm 에 요청
   */
  private OnBoardingQuestionsResponseDto requestOnboardingQuestions(
      OnboardingQuestionsRequestDto requestOnboardingQuestionsDto)
      throws JsonProcessingException {
    String systemPrompt = getPromptWithFilePath("prompts/onboardingMakeInfoQuestions.txt");
    String userPrompt = objectMapper.writeValueAsString(requestOnboardingQuestionsDto);
    return OnBoardingQuestionsResponseDto.from(chatHcx007(systemPrompt, userPrompt,
        new TypeReference<>() {
        })
    );
  }
}
