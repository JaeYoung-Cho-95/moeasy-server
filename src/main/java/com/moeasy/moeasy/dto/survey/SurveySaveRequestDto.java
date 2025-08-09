package com.moeasy.moeasy.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class SurveySaveRequestDto {
    @Schema(description = "설문이 연결된 questionId", example = "702")
    private Long questionId;

    @Schema(
            description = "질문-답변 페어 리스트(각 요소는 단일 키-값 맵)",
            example = """
        [
            { "현재 제품은 어떤 상태인가요?": "이미 판매 중" },
            { "어디서 제품을 구매할 수 있나요?": "기타 자사 앱" },
            { "제품 가격은 얼마인가요?": "49,000원" },
            { "제품의 주요 기능은 무엇인가요?": "실시간 번역, 음성 인식, 오프라인 모드 지원" },
            { "배송은 얼마나 걸리나요?": "평균 2~3일" },
            { "A/S나 보증은 어떻게 되나요?": "구매일로부터 1년 무상 보증" },
            { "제품 색상 옵션은 무엇이 있나요?": "블랙, 화이트, 네이비" },
            { "환불이나 교환이 가능한가요?": "구매 후 14일 이내 가능" }
        ]
        """
    )
    private List<Map<String, String>> results;
}
