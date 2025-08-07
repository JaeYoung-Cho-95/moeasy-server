package com.moeasy.moeasy.dto.llm.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NaverChatRequestDto {
    private List<MessageDto> messages;
    private double topP;
    private int topK;
    private int maxTokens;
    private double temperature;
    private List<String> stop;

}