package com.moeasy.moeasy.dto.quesiton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionListDto {
    private Long id;
    private String title;
    private LocalDateTime createdTime;
    private LocalDateTime expiredTime;
    private Boolean expired;
    private String url;
    private String qrCode;
    private Integer count;
}
