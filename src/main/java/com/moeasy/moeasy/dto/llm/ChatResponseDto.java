package com.moeasy.moeasy.dto.llm;

import lombok.Getter;

import java.util.List;

public class ChatResponseDto {
    private String id;
    private List<ChatChoice> choices;
    private long created;
    private String model;

    @Getter
    public static class ChatChoice {
        private int index;
        private ChatMessageDto message;
        private String finishReason;
    }
}
