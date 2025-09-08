package com.moeasy.moeasy.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Schema(name = "MobileKakasSdkTokenDto", description = "카카오 SDK에서 전달받은 토큰 페이로드")
public class MobileKakasSdkTokenDto {

  @Schema(description = "kakao oauth 에서 전달받은 accessToken", example = "asdnjklasednmdasczrwqe...asdqweino")
  private String accessToken;

  @Schema(description = "kakao oauth 에서 전달받은 refreshToken", example = "asdhklweqjmlk;sd...aq1@czxkjlpasdj")
  private String refreshToken;
}
