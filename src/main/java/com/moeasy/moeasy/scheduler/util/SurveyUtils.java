package com.moeasy.moeasy.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SurveyUtils {
    public static String summarizeMaxChoices(String resultsJson, ObjectMapper objectMapper) throws IOException {
        if (resultsJson == null || resultsJson.isBlank()) {
            return resultsJson;
        }

        TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        List<Map<String, Map<String, Object>>> aggregates = objectMapper.readValue(resultsJson, typeRef);

        List<Map<String, Map<String, Object>>> summarized = new ArrayList<>();

        for (Map<String, Map<String, Object>> perQuestion : aggregates) {
            // 각 perQuestion에는 단 하나의 키(질문)만 있다고 가정
            if (perQuestion.isEmpty()) {
                summarized.add(perQuestion);
                continue;
            }

            String question = perQuestion.keySet().iterator().next();
            Map<String, Object> answers = perQuestion.get(question);
            if (answers == null || answers.isEmpty()) {
                summarized.add(perQuestion);
                continue;
            }

            // 최대값 계산(others, 숫자 아님 값 제외)
            Long maxVal = null;
            for (Map.Entry<String, Object> e : answers.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if ("others".equals(key)) continue;
                if (val instanceof Number n) {
                    long v = n.longValue();
                    if (maxVal == null || v > maxVal) {
                        maxVal = v;
                    }
                }
            }

            // 최대값이 없으면(숫자 항목 없음) 원본 유지
            if (maxVal == null) {
                summarized.add(perQuestion);
                continue;
            }

            // 최대값만 남긴 맵 구성(others는 있으면 유지)
            Map<String, Object> filteredAnswers = new LinkedHashMap<>();
            if (answers.containsKey("others")) {
                filteredAnswers.put("others", answers.get("others"));
            }
            for (Map.Entry<String, Object> e : answers.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if ("others".equals(key)) continue;
                if (val instanceof Number n && n.longValue() == maxVal) {
                    filteredAnswers.put(key, n);
                }
            }

            Map<String, Map<String, Object>> one = new LinkedHashMap<>();
            one.put(question, filteredAnswers);
            summarized.add(one);
        }

        return objectMapper.writeValueAsString(summarized);
    }
}
