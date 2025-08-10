package com.moeasy.moeasy.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.domain.survey.Survey;
import com.moeasy.moeasy.repository.survey.SurveyRepository;
import com.moeasy.moeasy.scheduler.util.CleanJson;
import com.moeasy.moeasy.scheduler.util.SurveyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyResultScheduler {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void logSurveyResultsUpdatedInLast10Min() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDateTime tenMinutesAgo = now.minusMinutes(10);

        List<Survey> updated = surveyRepository
                .findAllByLastUpdatedGreaterThanEqualAndLastUpdatedLessThan(tenMinutesAgo, now);

        if (updated.isEmpty()) {
            log.info("[SurveyResults] 최근 10분({} ~ {}) 동안 갱신된 레코드가 없습니다.", tenMinutesAgo, now);
            return;
        }

        log.info("[SurveyResults] 최근 10분({} ~ {}) 갱신건수: {}", tenMinutesAgo, now, updated.size());

        try {
            for (Survey s : updated) {
                String json = s.getResultsJson();
                ObjectMapper objectMapper = new ObjectMapper();
                String summarizedJson = SurveyUtils.summarizeMaxChoices(json, objectMapper);
                String cleanSummarize = CleanJson.toCleanSummaryJson(summarizedJson, objectMapper);
                String toLog = json == null ? "null" : (json.length() > 5000 ? json.substring(0, 5000) + "...(truncated)" : json);
                log.info("[Survey:{}] resultsJson={}", s.getId(), cleanSummarize);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }
}
