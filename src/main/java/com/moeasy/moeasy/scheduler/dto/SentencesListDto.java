package com.moeasy.moeasy.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentencesListDto {
    @JsonAlias({"insights", "sentences"})
    private List<SentenceItemDto> sentences;
}