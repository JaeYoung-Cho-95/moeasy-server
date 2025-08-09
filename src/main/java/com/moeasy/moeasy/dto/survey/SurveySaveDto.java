package com.moeasy.moeasy.dto.survey;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@Builder
@Data
public class SurveySaveDto {
    List<Map<String, QuestionAnswerDto>> questionAndAnswers;

    public static SurveySaveDto from(List<Map<String, QuestionAnswerDto>> listData) {
        return SurveySaveDto.builder()
                .questionAndAnswers(listData)
                .build();
    }

    @JsonValue
    public List<Map<String, QuestionAnswerDto>> jsonValue() {
        return questionAndAnswers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SurveySaveDto{\n  questionAndAnswers=[\n");
        for (int i = 0; i < (questionAndAnswers == null ? 0 : questionAndAnswers.size()); i++) {
            Map<String, QuestionAnswerDto> map = questionAndAnswers.get(i);
            sb.append("    {");
            int j = 0;
            for (Map.Entry<String, QuestionAnswerDto> e : map.entrySet()) {
                sb.append('\n')
                        .append("      ").append(e.getKey()).append(" = ")
                        .append(e.getValue());
                if (++j < map.size()) sb.append(',');
            }
            sb.append("\n    }");
            if (i < questionAndAnswers.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}


