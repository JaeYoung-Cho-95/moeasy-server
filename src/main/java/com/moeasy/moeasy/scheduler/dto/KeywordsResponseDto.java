package com.moeasy.moeasy.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KeywordsResponseDto {
    private List<KeywordsItemsDto> positives;
    private List<KeywordsItemsDto> negatives;
}