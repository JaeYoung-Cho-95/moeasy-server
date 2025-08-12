package com.moeasy.moeasy.scheduler.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GraphItemDto {
    private String type;
    private Map<String, Integer> contents;
}
