package com.moeasy.moeasy.dto.quesiton;

import com.moeasy.moeasy.domain.question.Question;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionListDto {

  private Long id;
  private Long surveyId;
  private String title;
  private LocalDateTime createdTime;
  private LocalDateTime expiredTime;
  private Boolean expired;
  private String url;
  private String qrCode;
  private Integer count;

  @Builder
  public QuestionListDto(Long id, Long surveyId, String title, LocalDateTime createdTime,
      LocalDateTime expiredTime, Boolean expired, String url, String qrCode, Integer count) {
    this.id = id;
    this.surveyId = surveyId;
    this.title = title;
    this.createdTime = createdTime;
    this.expiredTime = expiredTime;
    this.expired = expired;
    this.url = url;
    this.qrCode = qrCode;
    this.count = count;
  }

  public static QuestionListDto from(Question question, String presignedUrl) {
    return QuestionListDto.builder()
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
