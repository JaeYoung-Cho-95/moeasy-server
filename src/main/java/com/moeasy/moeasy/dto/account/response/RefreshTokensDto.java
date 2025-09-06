package com.moeasy.moeasy.dto.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
@Schema(name = "RefreshTokensDto", description = "jwt token refresh 후 반환")
public class RefreshTokensDto {

  @Schema(description = "accessToken", example = "xzciqwefd.asioeuwqczx....[fgdkvxccvxcv].iweqonsdasd")
  private final String accessToken;

  @Schema(description = "refreshToken", example = "asdjiashdk.asdjzcxbdsa....asdnkjczxnjkwda.asdkcxz")
  private final String refreshToken;

  public static RefreshTokensDto from(String accessToken, String refreshToken) {
    return RefreshTokensDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
}
