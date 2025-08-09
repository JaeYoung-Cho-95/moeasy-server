package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceQuestionDto;
import com.moeasy.moeasy.dto.quesiton.QuestionDto;
import com.moeasy.moeasy.dto.quesiton.QuestionRequestDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerQuestionDto;
import com.moeasy.moeasy.dto.survey.QuestionAnswerDto;
import com.moeasy.moeasy.dto.survey.SurveySaveDto;
import com.moeasy.moeasy.repository.account.MemberRepository;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SaveQuestionService {

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final SurveyRepository surveyRepository;
    private final ObjectMapper objectMapper;

    public Question saveQuestionsJoinUser(Long id, QuestionRequestDto questionRequestDto) {
        Optional<Member> findMember = memberRepository.findById(id);
        if (findMember.isEmpty()) throw new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + id);

        try {
            QuestionDto contentDto = new QuestionDto(questionRequestDto.getMultipleChoiceQuestions(), questionRequestDto.getShortAnswerQuestions());
            List<MultipleChoiceQuestionDto> multipleChoiceQuestions = contentDto.getMultipleChoiceQuestions();
            List<ShortAnswerQuestionDto> shortAnswerQuestions = contentDto.getShortAnswerQuestions();

            Survey survey = makeAndGetSurvey(multipleChoiceQuestions, shortAnswerQuestions);

            Question question = Question.builder()
                    .member(findMember.get())
                    .title(questionRequestDto.getTitle())
                    .content(objectMapper.writeValueAsString(contentDto))
                    .build();
            question.linkSurvey(survey);

            return questionRepository.save(question);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("\"QuestionDto를 JSON으로 변환하는 중 오류가 발생했습니다. : " + e);
        }
    }

    private Survey makeAndGetSurvey(List<MultipleChoiceQuestionDto> multipleChoiceQuestions, List<ShortAnswerQuestionDto> shortAnswerQuestions) throws JsonProcessingException {
        SurveySaveDto data = getSurveySaveDto(multipleChoiceQuestions, shortAnswerQuestions);
        String surveyJson = objectMapper.writeValueAsString(data);
        Survey survey = Survey.builder()
                .resultsJson(surveyJson)
                .build();
        surveyRepository.save(survey);
        return survey;
    }

    private SurveySaveDto getSurveySaveDto(List<MultipleChoiceQuestionDto> multipleChoiceQuestions, List<ShortAnswerQuestionDto> shortAnswerQuestions) {
        List<Map<String, QuestionAnswerDto>> temp1 = new ArrayList<>();
        for (MultipleChoiceQuestionDto multipleChoiceQuestionDto : multipleChoiceQuestions) {
            String question = multipleChoiceQuestionDto.getQuestion();
            List<String> choices = multipleChoiceQuestionDto.getChoices();
            temp1.add(extractedSurveyForm(question, choices));
        }

        List<Map<String, QuestionAnswerDto>> temp2 = new ArrayList<>();
        for (ShortAnswerQuestionDto shortAnswerQuestionDto : shortAnswerQuestions) {
            String question = shortAnswerQuestionDto.getQuestion();
            temp2.add(extractedSurveyForm(question));
        }

        temp1.addAll(temp2);
        SurveySaveDto data = SurveySaveDto.from(temp1);
        return data;
    }

    private Map<String, QuestionAnswerDto> extractedSurveyForm(String question, List<String> choices) {
        Map<String, QuestionAnswerDto> temp = new LinkedHashMap<>();

        Map<String, Object> choiceCount = new HashMap<>();
        for (String choice : choices) {
            choiceCount.put(choice, 0);
        }
        choiceCount.put("others", new ArrayList<>());
        temp.put(question, QuestionAnswerDto.from(choiceCount));
        return temp;
    }

    private Map<String, QuestionAnswerDto> extractedSurveyForm(String question) {
        Map<String, QuestionAnswerDto> temp = new LinkedHashMap<>();
        Map<String, Object> choiceCount = new HashMap<>();
        choiceCount.put("others", new ArrayList<>());
        temp.put(question, QuestionAnswerDto.from(choiceCount));
        return temp;
    }
}
