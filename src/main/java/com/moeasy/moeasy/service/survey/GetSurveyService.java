package com.moeasy.moeasy.service.survey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetSurveyService {

  private final SurveyRepository surveyRepository;
  private final ObjectMapper objectMapper;

  // 추가: summarizeJson을 ObjectMapper로 파싱하여 JsonNode로 반환
  @Transactional(readOnly = true)
  public JsonNode getSurveyAsJson(Long surveyId) {
    Survey survey = extractSurvey(surveyId);

    String summarizeJson = survey.getSummarizeJson();
    Question question = survey.getQuestion();
    Boolean expired = question != null ? question.getExpired() : null;
    LocalDateTime expiredTime = question != null ? question.getExpiredTime() : null;

    if (summarizeJson == null || summarizeJson.isBlank()) {
      return putExpiredFields(expired, expiredTime);
    }

    JsonNode jsonNode = extractedSummarizeObject(summarizeJson);
    return putExpiredFields(jsonNode, expired, expiredTime);
  }

  private JsonNode extractedSummarizeObject(String summarizeJson) {
    try {
      return objectMapper.readTree(summarizeJson);
    } catch (JsonProcessingException e) {
      throw CustomErrorException.from(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "설문 결과지를 json 으로 변환 중 에러 발생");
    }
  }

  private Survey extractSurvey(Long surveyId) {
    return surveyRepository.findById(surveyId)
        .orElseThrow(() -> CustomErrorException.from(
            HttpStatus.NOT_FOUND,
            "Id : " + surveyId + "를 통해 조회되는 survey 가 없습니다.")
        );
  }

  private ObjectNode putExpiredFields(JsonNode parsed, Boolean expired, LocalDateTime expiredTime) {
    ObjectNode node = objectMapper.createObjectNode();
    node.set("data", parsed);
    ObjectNode nodeInsertedExpired = insertExpired(node, expired);
    return insertExpiredTime(nodeInsertedExpired, expiredTime);
  }

  private ObjectNode putExpiredFields(Boolean expired, LocalDateTime expiredTime) {
    ObjectNode node = objectMapper.createObjectNode();
    ObjectNode nodeInsertedExpired = insertExpired(node, expired);
    return insertExpiredTime(nodeInsertedExpired, expiredTime);
  }

  private ObjectNode insertExpiredTime(ObjectNode node, LocalDateTime expiredTime) {
    if (expiredTime == null) {
      node.set("expiredTime", NullNode.getInstance());
    } else {
      node.put("expiredTime", expiredTime.toString());
    }
    return node;
  }

  private ObjectNode insertExpired(ObjectNode node, Boolean expired) {
    if (expired == null) {
      node.set("expired", NullNode.getInstance());
    } else {
      node.put("expired", expired);
    }
    return node;
  }
}