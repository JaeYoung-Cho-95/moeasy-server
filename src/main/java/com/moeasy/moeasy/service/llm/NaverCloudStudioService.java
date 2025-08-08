package com.moeasy.moeasy.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.dto.llm.naver.ContentPart;
import com.moeasy.moeasy.dto.llm.naver.MessageDto;
import com.moeasy.moeasy.dto.llm.naver.NaverChatRequestDto;
import com.moeasy.moeasy.dto.llm.naver.NaverChatResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Service
public class NaverCloudStudioService implements NaverCloudStudio {
    private final ResourceLoader resourceLoader;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NaverCloudStudioService(ResourceLoader resourceLoader,
                             @Value("${naver.cloud.studio.host}") String host,
                             @Value("${naver.cloud.studio.auth-token}") String authToken, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + authToken) // 인증 헤더 변경
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public <T> T chat(String systemPrompt, String userPrompt, Class<T> responseType) {
        NaverChatResponseDto naverChatResponseDto = getNaverChatResponse(systemPrompt, userPrompt);

        String response;
        if (naverChatResponseDto != null && "20000".equals(naverChatResponseDto.getStatus().getCode())) {
            response = cleanResponse(naverChatResponseDto.getFirstTextMessage());
            try {
                return objectMapper.readValue(response, responseType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 파싱에 실패했습니다." + e);
            }

        } else {
            String errorMessage = naverChatResponseDto != null ? naverChatResponseDto.getStatus().getMessage() : "Unknown error";
            throw new RuntimeException("네이버 클라우드 API 호출에 실패했습니다: " + errorMessage);
        }
    }

    @Override
    public String getPromptWithFilePath(String promptFilePath) {
        Resource resource = resourceLoader.getResource("classpath:" + promptFilePath);
        try {
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 파일을 읽는데 실패했습니다 : " + e.getMessage());
        }
    }

    private NaverChatResponseDto getNaverChatResponse(String systemPrompt, String userPrompt) {
        List<ContentPart> systemContent = List.of(new ContentPart("text", systemPrompt, null));
        List<ContentPart> userContent = List.of(new ContentPart("text", userPrompt, null));

        List<MessageDto> messages = List.of(
                new MessageDto("system", systemContent),
                new MessageDto("user", userContent)
        );

        NaverChatRequestDto naverRequest = NaverChatRequestDto.builder()
                .messages(messages)
                .topP(0.8).topK(0).maxTokens(512).temperature(0.5)
                .stop(List.of())
                .build();

        return webClient.post()
                .uri("/v3/chat-completions/HCX-005")
                .bodyValue(naverRequest)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API call failed: " + errorBody))))
                .bodyToMono(NaverChatResponseDto.class)
                .block();
    }
}
