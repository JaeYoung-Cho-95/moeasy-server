package com.moeasy.moeasy.service.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.domain.account.Member;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.dto.quesiton.MultipleChoiceIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.quesiton.PatchQuestionTitleDto;
import com.moeasy.moeasy.dto.quesiton.PatchQuestionTitleResponseDto;
import com.moeasy.moeasy.dto.quesiton.QuestionsDto;
import com.moeasy.moeasy.dto.quesiton.QuestionsRequestDto;
import com.moeasy.moeasy.dto.quesiton.ShortAnswerIncludeIdQuestionDto;
import com.moeasy.moeasy.dto.survey.QuestionAnswerDto;
import com.moeasy.moeasy.dto.survey.SurveySaveDto;
import com.moeasy.moeasy.repository.account.MemberRepository;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SaveQuestionService {

  private final QuestionRepository questionRepository;
  private final MemberRepository memberRepository;
  private final SurveyRepository surveyRepository;
  private final ObjectMapper objectMapper;

  public Question saveQuestionsJoinUser(Long id, QuestionsRequestDto questionsRequestDto) {
    Optional<Member> findMember = memberRepository.findById(id);
    if (findMember.isEmpty()) {
      throw new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + id);
    }

    try {
      QuestionsDto contentDto = new QuestionsDto(questionsRequestDto.getMultipleChoiceQuestions(),
          questionsRequestDto.getShortAnswerQuestions());
      List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions = contentDto.getMultipleChoiceQuestions();
      List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions = contentDto.getShortAnswerQuestions();

      Survey survey = makeAndGetSurvey(multipleChoiceQuestions, shortAnswerQuestions);

      Question question = Question.builder()
          .member(findMember.get())
          .title(questionsRequestDto.getTitle())
          .content(objectMapper.writeValueAsString(contentDto))
          .build();
      question.linkSurvey(survey);

      return questionRepository.save(question);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("\"QuestionDto를 JSON으로 변환하는 중 오류가 발생했습니다. : " + e);
    }
  }

  public PatchQuestionTitleResponseDto updateQuestionTitle(
      PatchQuestionTitleDto patchQuestionTitleDto) {
    Long id = patchQuestionTitleDto.getId();
    String title = patchQuestionTitleDto.getTitle();

    Question question = updateQuestion(id, title);

    return PatchQuestionTitleResponseDto.builder()
        .title(question.getTitle())
        .id(question.getId())
        .build();
  }
  
  public Question updateQuestion(Long id, String title) {
    Question question = questionRepository.findById(id)
        .orElseThrow(
            () -> new EntityNotFoundException("Id : " + id + " 에 해당하는 설문지를 찾을 수 없습니다."));

    question.updateTitle(title);
    return question;
  }

  private Survey makeAndGetSurvey(List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions,
      List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions) throws JsonProcessingException {
    SurveySaveDto data = getSurveySaveDto(multipleChoiceQuestions, shortAnswerQuestions);
    String surveyJson = objectMapper.writeValueAsString(data);
    Survey survey = Survey.builder()
        .resultsJson(surveyJson)
        .build();
    surveyRepository.save(survey);
    return survey;
  }

  private SurveySaveDto getSurveySaveDto(
      List<MultipleChoiceIncludeIdQuestionDto> multipleChoiceQuestions,
      List<ShortAnswerIncludeIdQuestionDto> shortAnswerQuestions) {
    List<Map<String, QuestionAnswerDto>> temp1 = new ArrayList<>();
    for (MultipleChoiceIncludeIdQuestionDto multipleChoiceQuestionDto : multipleChoiceQuestions) {
      String question = multipleChoiceQuestionDto.getQuestion();
      List<String> choices = multipleChoiceQuestionDto.getChoices();

      Long questionId = multipleChoiceQuestionDto.getId();
      Map<String, Long> temp = new HashMap<>();

      temp1.add(extractedSurveyForm(question, choices));
    }

    List<Map<String, QuestionAnswerDto>> temp2 = new ArrayList<>();
    for (ShortAnswerIncludeIdQuestionDto shortAnswerQuestionDto : shortAnswerQuestions) {
      String question = shortAnswerQuestionDto.getQuestion();
      temp2.add(extractedSurveyForm(question));
    }

    temp1.addAll(temp2);
    return SurveySaveDto.from(temp1);
  }

  private Map<String, QuestionAnswerDto> extractedSurveyForm(String question,
      List<String> choices) {
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
