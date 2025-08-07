package com.moeasy.moeasy.dto.llm.naver;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummarizeDto {
    private boolean searchWeb;

    private String properNoun;

    private List<String> category;
}