package com.moeasy.moeasy.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KeywordsItemsDto {
    private String hashtag;
    private String label;
    private String weight;
    private String objective_frac;
    private String subjective_frac;
    private List<Map<String, String>> evidence;
}