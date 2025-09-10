package com.moeasy.moeasy.dto.quesiton.response;

import com.moeasy.moeasy.domain.question.Question;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class QuestionListResponseDto {

  private final Long id;
  private final Long surveyId;
  private final String title;
  private final LocalDateTime createdTime;
  private final LocalDateTime expiredTime;
  private final Boolean expired;
  private final String url;
  private final String qrCode;
  private final Integer count;

  public static QuestionListResponseDto from(Question question, String presignedUrl) {
    return QuestionListResponseDto.builder()
        .id(question.getId())
        .surveyId(question.getSurvey().getId())
        .title(question.getTitle())
        .createdTime(question.getCreatedTime())
        .expiredTime(question.getExpiredTime())
        .url(question.getUrlInQrCode())
        .qrCode(presignedUrl)
        .expired(question.getExpired())
        .count(question.getCount())
        .build();
  }
}
