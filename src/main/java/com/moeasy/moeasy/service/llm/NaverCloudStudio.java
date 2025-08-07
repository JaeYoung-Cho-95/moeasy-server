package com.moeasy.moeasy.service.llm;

import com.moeasy.moeasy.dto.llm.ChatRequestDto;
import com.moeasy.moeasy.dto.llm.ChatResponseDto;

import java.util.List;

public interface NaverCloudStudio {
    /**
     * 단발성 질문
     */
    <T> T chat(String systemPrompt, String userPrompt, Class<T> responseType);

    /**
     * 여러 번의 질문과 답변을 통해 연속적인 대화
     */
//    ChatResponseDto chat(ChatRequestDto request);

    /**
     * load prompt txtfile
     */
    String getPromptWithFilePath(String promptFilePath);

    /**
     * 응답이 이상하게 올 때 정리
     */
    default String cleanResponse(String response) {
        return response
                .replaceAll("(?i)```json", "")
                .replaceAll("(?i)```", "")
                .trim();
    }
}
