package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.quesiton.*;
import com.moeasy.moeasy.service.llm.NaverCloudStudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MakeQuestionService extends NaverCloudStudioService {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public MakeQuestionService(ResourceLoader resourceLoader,
                             @Value("${naver.cloud.studio.host}") String host,
                             @Value("${naver.cloud.studio.auth-token}") String authToken,
                             ObjectMapper objectMapper) {
        super(resourceLoader, host, authToken, objectMapper);
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public List<MultipleChoiceQuestionDto> makeMultipleChoiceQuestions(OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
        String systemPrompt = getPromptWithFilePath("prompts/makeMultipleChoicesQuestionsPrompt.txt");
        String userPrompt = extractUserPrompt(onboardingMakeQuestionRequestDto);

        return chat(systemPrompt, userPrompt, new TypeReference<List<MultipleChoiceQuestionDto>>() {
        });
    }

    private String extractUserPrompt(OnboardingMakeQuestionRequestDto dto) {
        Map<String, Object> userJsonPrompt = new LinkedHashMap<>();
        userJsonPrompt.put("프로덕트 유형", dto.getProductType());
        userJsonPrompt.put("산업/도메인 유형", dto.getDomain());
        userJsonPrompt.put("설문 목적", dto.getPurpose());
        userJsonPrompt.put("사용자 부연 설명", dto.getDescription());

        if (dto.getOnboardingQuestionAnswers() != null && !dto.getOnboardingQuestionAnswers().isEmpty()) {
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

    public List<ShortAnswerQuestionDto> makeShortAnswerQuestions(OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
        String systemPrompt = getPromptWithFilePath("prompts/makeShortsAnswerQuestionsPrompt.txt");
        String userPrompt = extractUserPromptAtTitle(onboardingMakeQuestionRequestDto);

        return chat(systemPrompt, userPrompt, new TypeReference<List<ShortAnswerQuestionDto>>() {
        });
    }

    public String makeTitle(OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
        String systemPrompt = getPromptWithFilePath("prompts/makeTitlePrompt.txt");
        String userPrompt = extractUserPrompt(onboardingMakeQuestionRequestDto);

        Map<String, String> result = chat(systemPrompt, userPrompt, new TypeReference<Map<String, String>>() {});
        return result.get("title");
    }
}
