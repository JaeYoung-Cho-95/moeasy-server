package com.moeasy.moeasy.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {
    @Schema(description = "서버 access token", example = "asdkzxcnkoawej1239adjkcmz.asdklsadnkcx...")
    private String accessToken;

    @Schema(description = "서버 refresh token", example = "asdkzxcnkoawej1239adjkcmz.asdklsadnkcx...")
    private String refreshToken;
}
