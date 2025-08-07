package com.moeasy.moeasy.service.llm;

import com.moeasy.moeasy.dto.llm.ChatRequestDto;
import com.moeasy.moeasy.dto.llm.ChatResponseDto;

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

import lombok.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class OnBoardingService implements NaverCloudStudio {

    private final ResourceLoader resourceLoader;
    private final WebClient webClient;

    public OnBoardingService(ResourceLoader resourceLoader,
                             @Value("${naver.cloud.studio.host}") String host,
                             @Value("${naver.cloud.studio.auth-token}") String authToken) {
        this.resourceLoader = resourceLoader;
        this.webClient = WebClient.builder()
                .baseUrl(host)
                .defaultHeader("Authorization", "Bearer " + authToken) // 인증 헤더 변경
                .defaultHeader("Content-Type", "application/json")
                .build();

    }

    @Override
    public String generate(ChatRequestDto request) {
        String systemPrompt = this.getPromptWithFilePath("prompts/SummarizePrompt.txt");

        log.info(request.toString());
        List<ContentPart> systemContent = List.of(new ContentPart("text", systemPrompt, null));
        List<ContentPart> userContent = List.of(new ContentPart("text", request.getUserMessage(), null));


        List<MessageDto> messages = List.of(
                new MessageDto("system", systemContent),
                new MessageDto("user", userContent)
        );

        NaverChatRequestDto naverRequest = NaverChatRequestDto.builder()
                .messages(messages)
                .topP(0.8).topK(0).maxTokens(512).temperature(0.5)
                .stop(List.of())
                .build();

        NaverChatResponseDto naverChatResponseDto = webClient.post()
                .uri("/v3/chat-completions/HCX-005")
                .bodyValue(naverRequest)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API call failed: " + errorBody))))
                .bodyToMono(NaverChatResponseDto.class)
                .block();


        if (naverChatResponseDto != null && "20000".equals(naverChatResponseDto.getStatus().getCode())) {
            return naverChatResponseDto.getFirstTextMessage();
        } else {
            String errorMessage = naverChatResponseDto != null ? naverChatResponseDto.getStatus().getMessage() : "Unknown error";
            throw new RuntimeException("네이버 클라우드 API 호출에 실패했습니다: " + errorMessage);
        }

    }

    @Override
    public ChatResponseDto chat(ChatRequestDto request) {
        return null;
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

//    public String searchWeb(String string) {
//
//    }





    // v3 API 응답 DTO (참고: 실제 응답 구조에 따라 수정이 필요할 수 있습니다)

}
