package com.moeasy.moeasy.dto.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AppLoginDataDto", description = "앱 로그인 성공 응답")
public class AppLoginDataDto {

  @Schema(description = "서버 access token", example = "asdkzxcnkoawej1239adjkcmz.asdklsadnkcx...")
  private String accessToken;

  @Schema(description = "서버 refresh token", example = "asdkzxcnkoawej1239adjkcmz.asdklsadnkcx...")
  private String refreshToken;

  @Schema(description = "유저 email", example = "test.com@gmail.com")
  private String email;

  @Schema(description = "유저 name", example = "김옥순")
  private String name;


  public static AppLoginDataDto from(KakaoDto dto, List<String> tokens) {
    return AppLoginDataDto.builder()
        .accessToken(tokens.get(0))
        .refreshToken(tokens.get(1))
        .email(dto.getEmail())
        .name(dto.getNickname())
        .build();
  }
}
