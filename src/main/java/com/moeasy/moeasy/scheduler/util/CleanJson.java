package com.moeasy.moeasy.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CleanJson {
    private CleanJson() {}

    // 입력: [{ "질문": { "선택지A": 3, "선택지B": 5, "others": [] } }, ...]
    // 출력: [{ "question": "...", "topChoices": ["선택지B"], "count": 5 }, ...]
    public static String toCleanSummaryJson(String resultsJson, ObjectMapper objectMapper) throws IOException {
        if (resultsJson == null || resultsJson.isBlank()) {
            return "[]";
        }

        TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        List<Map<String, Map<String, Object>>> aggregates = objectMapper.readValue(resultsJson, typeRef);

        List<Map<String, Object>> out = new ArrayList<>();

        for (Map<String, Map<String, Object>> perQuestion : aggregates) {
            if (perQuestion == null || perQuestion.isEmpty()) continue;

            String question = perQuestion.keySet().iterator().next();
            Map<String, Object> answers = perQuestion.get(question);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("question", question);

            if (answers == null || answers.isEmpty()) {
                out.add(row);
                continue;
            }

            // others 후보 수집
            Object others = answers.get("others");
            List<String> othersList = null;
            if (others instanceof List<?> list && !list.isEmpty()) {
                othersList = new ArrayList<>();
                for (Object v : list) {
                    othersList.add(String.valueOf(v));
                }
            }

            // 숫자 응답 중 최대값 계산
            Long maxVal = null;
            for (Map.Entry<String, Object> e : answers.entrySet()) {
                String k = e.getKey();
                if ("others".equals(k)) continue;
                Object v = e.getValue();
                if (v instanceof Number n) {
                    long lv = n.longValue();
                    if (maxVal == null || lv > maxVal) maxVal = lv;
                }
            }

            if (maxVal == null) {
                // 숫자 항목이 없으므로 주관식으로 판단
                if (othersList != null) {
                    row.put("answers", othersList);
                }
            } else {
                // 최대값과 일치하는 선택지만 수집
                List<String> topChoices = new ArrayList<>();
                for (Map.Entry<String, Object> e : answers.entrySet()) {
                    String k = e.getKey();
                    if ("others".equals(k)) continue;
                    Object v = e.getValue();
                    if (v instanceof Number n && n.longValue() == maxVal) {
                        topChoices.add(k);
                    }
                }
                row.put("topChoices", topChoices);
                row.put("count", maxVal.intValue());

                // 주관식 텍스트가 섞여 있으면 필요 시 포함
                if (othersList != null) {
                    row.put("othersText", othersList);
                }
            }

            out.add(row);
        }

        return objectMapper.writeValueAsString(out);
    }

}
