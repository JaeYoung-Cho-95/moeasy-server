package com.moeasy.moeasy.service.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.moeasy.moeasy.dto.quesiton.OnboardingQuestionDto;
import com.moeasy.moeasy.dto.quesiton.OnboardingRequestDto;
import com.moeasy.moeasy.dto.quesiton.enums.ProductType;
import com.moeasy.moeasy.response.custom.CustomFailException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class OnBoardingService extends NaverCloudStudioService {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private Map<ProductType, List<OnboardingQuestionDto>> questionsMap;

    public OnBoardingService(ResourceLoader resourceLoader,
                             @Value("${naver.cloud.studio.host}") String host,
                             @Value("${naver.cloud.studio.auth-token}") String authToken,
                             ObjectMapper objectMapper) {
        super(resourceLoader, host, authToken, objectMapper);
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + "onbarding/onBoarding_questions.json");
            InputStream inputStream = resource.getInputStream();
            TypeReference<Map<ProductType, List<OnboardingQuestionDto>>> typeReference = new TypeReference<>() {};
            questionsMap = objectMapper.readValue(inputStream, typeReference);
            log.info("Onboarding questions loaded successfully from JSON file.");
        } catch (IOException e) {
            log.error("Failed to load onboarding_questions.json", e);
            questionsMap = Collections.emptyMap();
        }

    }

    public List<OnboardingQuestionDto> getNextOnBoardingQuestions(OnboardingRequestDto onboardingRequestDto) {
        ProductType productType = onboardingRequestDto.getProductType();
        List<OnboardingQuestionDto> questions = questionsMap.getOrDefault(productType, Collections.emptyList());

        if (questions.isEmpty()) {
            throw new CustomFailException(HttpStatus.NOT_FOUND, "해당 productType에 대한 질문을 찾을 수 없습니다: " + productType);
        }
        return questions;
    }
}
