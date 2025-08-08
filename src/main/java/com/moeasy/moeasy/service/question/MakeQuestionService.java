package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.quesiton.*;
import com.moeasy.moeasy.service.llm.NaverCloudStudioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.*;

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

//        ListMultipleChoicesQuestionDto wrapper = new ListMultipleChoicesQuestionDto();
//        wrapper.setMultipleChoiceQuestionDtoList(chat);
//        return wrapper;

    }

    private String extractUserPrompt(OnboardingMakeQuestionRequestDto dto) {
        Map<String, Object> userJsonPrompt = new LinkedHashMap<>();
        userJsonPrompt.put("프로덕트 유형", dto.getProductType());
        userJsonPrompt.put("산업/도메인 유형", dto.getProductType());
        userJsonPrompt.put("설문 목적", dto.getProductType());
        userJsonPrompt.put("사용자 부연 설명", dto.getProductType());

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

    public List<ShortAnswerQuestionDto> makeShortAnswerQuestions(OnboardingMakeQuestionRequestDto onboardingMakeQuestionRequestDto) {
        String systemPrompt = getPromptWithFilePath("prompts/makeShortsAnswerQuestionsPrompt.txt");
        String userPrompt = extractUserPrompt(onboardingMakeQuestionRequestDto);

        return chat(systemPrompt, userPrompt, new TypeReference<List<ShortAnswerQuestionDto>>() {
        });

//        ListShortAnswerQuestionDto wrapper = new ListShortAnswerQuestionDto();
//        wrapper.setMultipleChoiceQuestionDtoList(chat);
//
//        return wrapper;
    }
}
