package com.moeasy.moeasy.service.llm;

import com.moeasy.moeasy.dto.llm.ChatRequestDto;
import com.moeasy.moeasy.dto.llm.ChatResponseDto;

public interface NaverCloudStudio {
    /**
     * 단발성 질문
     */
    String generate(ChatRequestDto request);

    /**
     * 여러 번의 질문과 답변을 통해 연속적인 대화
     */
    ChatResponseDto chat(ChatRequestDto request);

    /**
     * load prompt txtfile
     */
    String getPromptWithFilePath(String promptFilePath);


}
