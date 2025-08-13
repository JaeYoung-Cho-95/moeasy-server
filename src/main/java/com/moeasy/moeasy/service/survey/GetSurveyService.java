package com.moeasy.moeasy.service.survey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moeasy.moeasy.domain.question.Question;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    Survey survey = surveyRepository.findById(surveyId)
        .orElseThrow(() -> new EntityNotFoundException(
            "Id : " + surveyId + "를 통해 조회되는 survey 가 없습니다."));

    String summarizeJson = survey.getSummarizeJson();

    Question question = survey.getQuestion();
    Boolean expired = question != null ? question.getExpired() : null;
    LocalDateTime expiredTime = question != null ? question.getExpiredTime() : null;

    // summarizeJson이 비어있으면 빈 데이터 + 만료정보만 담아 반환
    if (summarizeJson == null || summarizeJson.isBlank()) {
      log.warn("Survey(id={})의 summarizeJson이 비어 있습니다.", survey.getId());
      ObjectNode out = objectMapper.createObjectNode();
      putExpiredFields(out, expired, expiredTime);
      return out; // { "expired": ..., "expiredTime": ... }
    }

    try {
      JsonNode parsed = objectMapper.readTree(summarizeJson);

      if (parsed.isObject()) {
        ObjectNode obj = (ObjectNode) parsed;
        putExpiredFields(obj, expired, expiredTime);
        return obj;
      } else {
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("data", parsed);
        putExpiredFields(wrapper, expired, expiredTime);
        return wrapper;
      }
    } catch (Exception e) {
      log.error("summarizeJson 파싱 실패. surveyId={}, summarizeJson={}", survey.getId(), summarizeJson,
          e);
      ObjectNode out = objectMapper.createObjectNode();
      putExpiredFields(out, expired, expiredTime);
      return out;
    }
  }

  private void putExpiredFields(ObjectNode node, Boolean expired, LocalDateTime expiredTime) {
    if (expired == null) {
      node.set("expired", NullNode.getInstance());
    } else {
      node.put("expired", expired);
    }

    if (expiredTime == null) {
      node.set("expiredTime", NullNode.getInstance());
    } else {
      node.put("expiredTime", expiredTime.toString());
    }
  }
}