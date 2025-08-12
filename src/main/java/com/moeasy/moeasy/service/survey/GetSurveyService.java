package com.moeasy.moeasy.service.survey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.dto.survey.SurveyGetRequestDto;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetSurveyService {
    private final SurveyRepository surveyRepository;
    private final ObjectMapper objectMapper;

    // 기존: summarizeJson 문자열 그대로 반환
    public String getSurvey(SurveyGetRequestDto surveyGetRequestDto) {
        Survey survey = surveyRepository.findById(surveyGetRequestDto.getSurveyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Id : " + surveyGetRequestDto.getSurveyId() + "를 통해 조회되는 survey 가 없습니다."));

        String summarizeJson = survey.getSummarizeJson();
        if (summarizeJson == null || summarizeJson.isBlank()) {
            log.warn("Survey(id={})의 summarizeJson이 비어 있습니다.", survey.getId());
            return "{}";
        }
        return summarizeJson;
    }

    // 추가: summarizeJson을 ObjectMapper로 파싱하여 JsonNode로 반환
    public JsonNode getSurveyAsJson(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Id : " + surveyId + "를 통해 조회되는 survey 가 없습니다."));

        String summarizeJson = survey.getSummarizeJson();
        if (summarizeJson == null || summarizeJson.isBlank()) {
            log.warn("Survey(id={})의 summarizeJson이 비어 있습니다.", survey.getId());
            return objectMapper.createObjectNode(); // 빈 JSON 객체 {}
        }

        try {
            // 저장된 summarizeJson이 객체든 배열이든 그대로 파싱해 반환
            return objectMapper.readTree(summarizeJson);
        } catch (Exception e) {
            log.error("summarizeJson 파싱 실패. surveyId={}, summarizeJson={}", survey.getId(), summarizeJson, e);
            return objectMapper.createObjectNode();
        }
    }
}