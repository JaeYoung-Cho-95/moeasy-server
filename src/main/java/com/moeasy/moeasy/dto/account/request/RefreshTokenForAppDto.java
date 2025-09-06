package com.moeasy.moeasy.dto.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenForAppDto {

  @Schema(description = "refreshToken for App Login", example = "asdiowheqncxzwqdasnioczx.adojqwepnoksklweml...asdih")
  private String refreshToken;
}
