package com.moeasy.moeasy.service.question;

import com.moeasy.moeasy.dto.quesiton.MultipleChoiceQuestionDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerQuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MakeQuestionService {
    public List<MultipleChoiceQuestionDto> makeMultipleChoiceQuestions() {
        List<MultipleChoiceQuestionDto> questions = new ArrayList<>();
        for (int i=0; i<8; i++) {
            questions.add(makeMultipleChoiceQuestionDto());
        }

        return questions;
    }

    public MultipleChoiceQuestionDto makeMultipleChoiceQuestionDto() {
        return new MultipleChoiceQuestionDto(
                "객관식 예시 질문지 입니다. 너의 취미는 무엇이니?",
                Arrays.asList(
                        "헬스",
                        "수영",
                        "런닝",
                        "테니스",
                        "맛집 탐방"
                )
        );
    }

    public List<ShortAnswerQuestionDto> makeShortAnswerQuestions() {
        List<ShortAnswerQuestionDto> questions = new ArrayList<>();
        for (int i=0; i<2; i++) {
            questions.add(makeShortAnswerQuestion());
        }

        for (int i=0; i<2; i++) {
            questions.add(makeShortAnswerQuestion2());
        }

        return questions;
    }

    public ShortAnswerQuestionDto makeShortAnswerQuestion() {
        return new ShortAnswerQuestionDto(
                "주관식 예시 질문지 입니다. 당신이 '자유의지'를 가지고 있다고 느끼는 바로 그 순간, 그 느낌조차 정해진 것이라면, 우리는 무엇을 기준으로 자유롭다고 말할 수 있을까? (매핑된 리스트는 추후 답변에서 사용할 키워드들 입니다.",
                Arrays.asList(
                        "억지 같아",
                        "그냥 그래",
                        "아닌 듯해",
                        "대충 살아",
                        "그럴수도",
                        "잘 모르겠어",
                        "너무 어렵다"
                )
        );
    }

    public ShortAnswerQuestionDto makeShortAnswerQuestion2() {
        return new ShortAnswerQuestionDto(
                "주관식 예시 질문지 입니다. 지금 이 순간 당신이 답변하고 있는 이 문항이, 사실은 타인의 꿈 속 장면이라면, 그 꿈 안에서 철학하는 당신은 진짜라고 할 수 있는가? (매핑된 리스트는 추후 답변에서 사용할 키워드들 입니다.)",
                Arrays.asList(
                        "몰라",
                        "너무 어렵다",
                        "아닌 듯해",
                        "대충 살아"
                )
        );
    }
}
