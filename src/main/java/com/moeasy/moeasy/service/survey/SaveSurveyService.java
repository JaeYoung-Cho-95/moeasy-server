package com.moeasy.moeasy.service.survey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.dto.survey.QuestionAnswerDto;
import com.moeasy.moeasy.dto.survey.SurveySaveDto;
import com.moeasy.moeasy.dto.survey.request.SurveySaveRequestDto;
import com.moeasy.moeasy.dto.survey.response.SurveySaveResponseDto;
import com.moeasy.moeasy.repository.question.QuestionRepository;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class SaveSurveyService {

  private final SurveyRepository surveyRepository;
  private final QuestionRepository questionRepository;
  private final ObjectMapper objectMapper;

  public Long updateSurvey(SurveySaveRequestDto surveySaveRequestDto) {
    Survey survey = extractSurvey(surveySaveRequestDto);
    String resultsJson = survey.getResultsJson();

    try {
      TypeReference<List<Map<String, QuestionAnswerDto>>> typeRef =
          new TypeReference<>() {
          };
      List<Map<String, QuestionAnswerDto>> aggregates =
          objectMapper.readValue(resultsJson, typeRef);

      for (Map<String, String> answerMap : surveySaveRequestDto.getResults()) {
        for (Map.Entry<String, String> e : answerMap.entrySet()) {
          String question = e.getKey();
          String answer = e.getValue();

          Map<String, QuestionAnswerDto> perQuestion = findByQuestion(aggregates, question);
          if (perQuestion == null) {
            log.warn("집계 템플릿에서 질문을 찾지 못했습니다. question={}", question);
            continue;
          }

          QuestionAnswerDto qa = perQuestion.get(question);
          Map<String, Object> counts = qa.getMultipleAnswers();
          if (counts == null) {
            counts = new LinkedHashMap<>();
            counts.put("others", new ArrayList<>());
            qa.setMultipleAnswers(counts);
          }

          if (counts.containsKey(answer) && counts.get(answer) instanceof Number) {
            Number n = (Number) counts.get(answer);
            counts.put(answer, n.intValue() + 1);
          } else {
            Object othersObj = counts.get("others");
            if (!(othersObj instanceof List)) {
              othersObj = new ArrayList<>();
              counts.put("others", othersObj);
            }
            @SuppressWarnings("unchecked")
            List<Object> others = (List<Object>) othersObj;
            others.add(answer);
          }
        }
      }

      String updatedJson = objectMapper.writeValueAsString(SurveySaveDto.from(aggregates));
      survey.updateResultsJson(updatedJson);
      survey.updateLastUpdated();
      surveyRepository.save(survey);

      Question question = survey.getQuestion();
      if (question == null) {
        log.warn("Survey(id={})에 연결된 Question이 없습니다.", survey.getId());
      } else {
        question.increaseCount();
        questionRepository.save(question);
      }
    } catch (IOException e) {
      throw new RuntimeException("Survey results JSON 처리 중 오류가 발생했습니다.", e);
    }

    return survey.getId();
  }

  private Survey extractSurvey(SurveySaveRequestDto surveySaveRequestDto) {
    return surveyRepository.findByQuestionId(surveySaveRequestDto.getQuestionId())
        .orElseThrow(() -> CustomErrorException.from(HttpStatus.NOT_FOUND,
            "Id : " + surveySaveRequestDto.getQuestionId() + "를 통해 조회되는 survey 가 없습니다."));
  }

  private Map<String, QuestionAnswerDto> findByQuestion(
      List<Map<String, QuestionAnswerDto>> aggregates, String question) {
    for (Map<String, QuestionAnswerDto> m : aggregates) {
      if (m.containsKey(question)) {
        return m;
      }
    }
    return null;
  }

  public SurveySaveResponseDto update(SurveySaveRequestDto surveySaveRequestDto) {
    String surveyId = String.valueOf(updateSurvey(surveySaveRequestDto));
    return SurveySaveResponseDto.from(surveyId, "https://mo-easy.com/reporting/" + surveyId);
  }
}
