package com.moeasy.moeasy.dto.survey;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class QuestionAnswerDto {
    private Map<String, Object> multipleAnswers;

    public static QuestionAnswerDto from(Map<String, Object> map) {
        return QuestionAnswerDto.builder()
                .multipleAnswers(map)
                .build();
    }

    @JsonValue
    public Map<String, Object> jsonValue() {
        return multipleAnswers;
    }

    @Override
    public String toString() {
        return multipleAnswers == null ? "{}" : multipleAnswers.toString();
    }
}
