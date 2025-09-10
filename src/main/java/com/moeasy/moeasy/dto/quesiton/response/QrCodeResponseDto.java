package com.moeasy.moeasy.dto.quesiton.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
@Schema(name = "설문지 저장 후 반환되는 데이터", description = "설문지에 참여할 수 있는 url 과 qrcode 가 저장된 s3 url ")
public class QrCodeResponseDto {

  @Schema(description = "설문에 참여할 수 있는 url 입니다.", example = "")
  private final String url;

  @Schema(description = "", example = "")
  private final String qrCode;

  public static QrCodeResponseDto from(String url, String qrCode) {
    return QrCodeResponseDto.builder()
        .url(url)
        .qrCode(qrCode)
        .build();
  }
}
